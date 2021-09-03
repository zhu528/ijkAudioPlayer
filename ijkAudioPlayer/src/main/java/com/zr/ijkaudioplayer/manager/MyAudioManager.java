package com.zr.ijkaudioplayer.manager;

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.zr.ijkaudioplayer.AudioPlayEnum;
import com.zr.ijkaudioplayer.MyAbstractAudioPlayer;
import com.zr.ijkaudioplayer.audioCache.HttpProxyCacheServer;
import com.zr.ijkaudioplayer.bean.SongInfo;
import com.zr.ijkaudioplayer.listener.AudioPlayerListener;
import com.zr.ijkaudioplayer.listener.MyPhoneStateListener;
import com.zr.ijkaudioplayer.utils.LogUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.zr.ijkaudioplayer.tv.danmaku.ijk.media.player.IMediaPlayer;
import com.zr.ijkaudioplayer.tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * 集成IjkPlayer音頻播放
 * 音频管理类
 * @author zr
 */
public class MyAudioManager extends MyAbstractAudioPlayer {
    private String TAG = "MyAudioManager";
    /**
     * 定时器检测播放进度时间间隔
     */
    private final int TIMER_PROGRESS_INTERVAL = 200;
    /**
     * 最大连续播放错误数
     */
    private static final int MAX_CONTINUE_ERROR_NUM = 3;
 
    private IjkMediaPlayer mMediaPlayer;
    private final Context mContext;
    private final Context appContext;
    /**
     * 播放错误次数，连续三次错误，则不进行下一次播放了
     */
    private int mErrorPlayNum = 0;
    /**
     * 播放路径资源存储
     */
    private final List<SongInfo<Object>> mDataSourceList = new ArrayList<>();
    /**
     * 播放位置存储
     */
    private int currPlayPotion = -1;
    /**
     * 是否列表循环播放
     */
    private boolean mListLooping = false;
    /**
     * 是否单曲循环
     */
    private boolean mSingleLooping = false;
    /**
     * 缓存百分比
     */
    private int mBufferedPercent;
    /**
     * 是否正在拖动进度条
     */
    private boolean mSeekTouch = false;
    /**
     * 监听播放器进度Timer以及Task
     */
    private Timer mProgressTimer;
    private TimerTask mProgressTask;
    private final Object mProgressLock = new Object();
    /**
     * 进度线程活动
     */
    private boolean mAudioStateAlive = true;
    /**
     * 音频缓存
     */
    private HttpProxyCacheServer mCacheServer;
    /**
     * 记录上一次播放进度，用于判断是否正在缓冲
     */
    private long prevPlayPosition;
    /**
     * 电话管理者对象
     */
    private TelephonyManager mTelephonyManager;
    /**
     * 电话状态监听者
     */
    private MyPhoneStateListener myPhoneStateListener;
    /**
     * 用于发送是否在缓冲的消息Handler
     */
    private final Handler mBufferHandler = new Handler(Looper.getMainLooper());
 
    /**
     * 当前播放器状态
     * */
    private AudioPlayEnum mPlayerStatus = AudioPlayEnum.PLAYER_FREE;
    private AudioPlayerListener mAudioPlayerEvent;

    /*
    * 是否开启缓存
    */
    private boolean isEnableCache;

    public MyAudioManager(Context context) {
        mContext = context;
        appContext = context.getApplicationContext();
        initPlayer();
    }
 
    private void initPlayer() {
        //初始化
        mMediaPlayer = new IjkMediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //添加监听
        mMediaPlayer.setOnErrorListener(onErrorListener);
        mMediaPlayer.setOnCompletionListener(onCompletionListener);
        mMediaPlayer.setOnInfoListener(onInfoListener);
        mMediaPlayer.setOnBufferingUpdateListener(onBufferingUpdateListener);
        mMediaPlayer.setOnPreparedListener(onPreparedListener);
        //来电监听
        mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        myPhoneStateListener = new MyPhoneStateListener(this);
        mTelephonyManager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }
 
    /**
     * 播放
     */
    private void prepareAsync(int position, SongInfo<Object> songInfo, boolean isCache) {

        if (isCache) {
            try {
                mMediaPlayer.reset();
                sendPlayerStatus(AudioPlayEnum.PLAYER_PREPARING);
                mCacheServer = AudioCacheManager.getProxy(appContext);
                String path = (String) songInfo.getSongUrl();
                AudioCacheManager.getInstance(appContext).registerCacheListener(path, (file, s, i) -> {
                    mBufferedPercent = i;
                    if (mAudioPlayerEvent != null) {
                        mAudioPlayerEvent.onBufferingUpdate(mBufferedPercent);
                    }
                });
                String proxyPath = AudioCacheManager.getInstance(appContext).getProxyUrl(path);
                if (mCacheServer.isCached(path)
                        || proxyPath.startsWith("file://")
                        || proxyPath.startsWith(mContext.getFilesDir().getPath())) {
                    mBufferedPercent = 100;
                    if (mAudioPlayerEvent != null) {
                        sendBufferingHandler(true, false);
                    }
                    //不要在这里发进度，上层调用不到duration，设置不了max
                } else {
                    mBufferedPercent = 0;
                    if (mAudioPlayerEvent != null) {
                        sendBufferingHandler(false, true);
                    }
                }
                mMediaPlayer.setDataSource(proxyPath);
                mMediaPlayer.prepareAsync();
                currPlayPotion = position;
            } catch (Exception e) {
                e.printStackTrace();
                onErrorPlay();
            }
        } else {
            Object path = songInfo.getSongUrl();
            Uri uri;
            String newPath;
            try {
                mMediaPlayer.reset();
                sendPlayerStatus(AudioPlayEnum.PLAYER_PREPARING);
                if (path instanceof Uri){
                    uri = (Uri) path;
                    LogUtils.d(TAG + "uri: " + uri);
                    mMediaPlayer.setDataSource(mContext, uri);
                } else {
                    newPath = (String) path;
                    LogUtils.d(TAG + "newPath: " + newPath);
                    mMediaPlayer.setDataSource(newPath);
                }
                mMediaPlayer.prepareAsync();
                currPlayPotion = position;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
 
    @Override
    public void reStart() {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            sendPlayerStatus(AudioPlayEnum.PLAYER_PLAYING);
            mMediaPlayer.start();
            synchronized (mProgressLock) {
                mProgressLock.notifyAll();
            }
        }
    }
 
    @Override
    public void start(String path) {
        if(!TextUtils.isEmpty(path)){
            try {
                mMediaPlayer.reset();
                sendPlayerStatus(AudioPlayEnum.PLAYER_PREPARING);
                mMediaPlayer.setDataSource(path);
                mMediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void startUri(Uri uri) {
        if(uri != null){
            try {
                mMediaPlayer.reset();
                sendPlayerStatus(AudioPlayEnum.PLAYER_PREPARING);
                mMediaPlayer.setDataSource(mContext, uri);
                mMediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void startList(List<SongInfo<Object>> pathList, int playPosition, boolean isCache) {
        isEnableCache = isCache;
        if (pathList == null || pathList.isEmpty()) {
            return;
        }
        mDataSourceList.clear();
        mDataSourceList.addAll(pathList);
        if (playPosition != 0){
            prepareAsync(playPosition, mDataSourceList.get(playPosition), isEnableCache);
        } else {
            prepareAsync(0, mDataSourceList.get(0), isEnableCache);
        }
    }
 
    @Override
    public void setAudioPlayerListener(AudioPlayerListener event) {
        this.mAudioPlayerEvent = event;
    }
 
    @Override
    public void pause() {
        try {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                sendBufferingHandler(true, false);
                sendPlayerStatus(AudioPlayEnum.PLAYER_PAUSE);
                mMediaPlayer.pause();
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
    }

    @Override
    public void stopCacheAndShutdown(String path) {
        LogUtils.d("path: " + path);
        if (mCacheServer != null && !TextUtils.isEmpty(path)) {
            mCacheServer.stopCacheAndShutdown(path);
        }
    }
 
    @Override
    public void nextPlay() {
        if (mDataSourceList.isEmpty()) {
            return;
        }
        if (currPlayPotion < 0) {
            prepareAsync(0, mDataSourceList.get(0), isEnableCache);
        } else {
            if (mDataSourceList.size() > currPlayPotion + 1) {
                prepareAsync(currPlayPotion + 1, mDataSourceList.get(currPlayPotion + 1), isEnableCache);
            } else {
                if (mListLooping) {
                    prepareAsync(0, mDataSourceList.get(0), isEnableCache);
                }
            }
        }
    }
 
    @Override
    public void prevPlay() {
        if (mDataSourceList.isEmpty() || currPlayPotion < 0) {
            return;
        }
        if (currPlayPotion == 0) {
            if (mListLooping) {
                prepareAsync(mDataSourceList.size() - 1, mDataSourceList.get(mDataSourceList.size() - 1), isEnableCache);
            }
        } else {
            prepareAsync(currPlayPotion - 1, mDataSourceList.get(currPlayPotion - 1), isEnableCache);
        }
    }
 
    @Override
    public boolean isPlaying() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.isPlaying();
        }
        return false;
    }

    @Override
    public AudioPlayEnum getPlayerStatus() {
        return mPlayerStatus;
    }
 
    @Override
    public void seekTo(long time) {
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.seekTo(time);
                mSeekTouch = false;
                if (mMediaPlayer.isPlaying()) {
                    synchronized (mProgressLock) {
                        mProgressLock.notifyAll();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    @Override
    public void seekStart() {
        mSeekTouch = true;
    }
 
    @Override
    public void release() {
        mTelephonyManager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        mBufferHandler.removeCallbacksAndMessages(null);
        if (mCacheServer != null) {
            AudioCacheManager.getInstance(appContext).unregisterCacheListener();
            mCacheServer.shutdown();
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mAudioStateAlive = false;
            currPlayPotion = -1;
            mMediaPlayer.release();
        }
    }
 
    @Override
    public long getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }
 
    @Override
    public long getDuration() {
        return mMediaPlayer.getDuration();
    }
 
    @Override
    public int getBufferedPercentage() {
        return mBufferedPercent;
    }
 
    @Override
    public void setListLooping(boolean isLooping) {
        mListLooping = isLooping;
    }
 
    @Override
    public void setSingleLooping(boolean isLooping) {
        mSingleLooping = isLooping;
    }
 
    /**
     * 销毁Timer
     */
    private void destroyTimer() {
        if (mProgressTimer != null) {
            mProgressTimer.cancel();
            mProgressTimer = null;
        }
    }
 
    /**
     * 错误时，自动播放下一首
     */
    private void onErrorPlay() {
        sendPlayerStatus(AudioPlayEnum.PLAYER_ERROR);
        destroyTimer();
        if (mErrorPlayNum < MAX_CONTINUE_ERROR_NUM) {
            mErrorPlayNum++;
            if (mSingleLooping) {
                //单曲循环
                prepareAsync(currPlayPotion, mDataSourceList.get(currPlayPotion), isEnableCache);
            } else if (mListLooping || mDataSourceList.size() < currPlayPotion + 1) {
                //列表循环
                nextPlay();
            }
        }
    }
 
    /**
     * 延时发送是否在缓冲的状态，防止假缓冲
     */
    private void sendBufferingHandler(boolean sendNow, final boolean isBuffering) {
        if (mAudioPlayerEvent != null) {
            mAudioPlayerEvent.onBuffering(isBuffering);
        }
    }
 
    /**
     * 设置当前播放状态
     */
    private void sendPlayerStatus(AudioPlayEnum mStatus){
        mPlayerStatus = mStatus;
        if(mAudioPlayerEvent != null){
            mAudioPlayerEvent.onStatusChange(mPlayerStatus,currPlayPotion);
        }
    }
 
    /**
     * 定时器检测播放进度
     */
    private void playProgressListener() {
        if (mProgressTimer == null) {
            mProgressTimer = new Timer();
        }
        if (mProgressTask != null) {
            mProgressTask.cancel();
            mProgressTask = null;
        }
        mProgressTask = new TimerTask() {
            @Override
            public void run() {
                while (mAudioStateAlive) {
                    if (mMediaPlayer == null
                            || !mMediaPlayer.isPlaying()
                            || mSeekTouch) {
                        synchronized (mProgressLock) {
                            try {
                                mProgressLock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        try {
                            Thread.sleep(TIMER_PROGRESS_INTERVAL);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (mAudioPlayerEvent != null
                            && !mSeekTouch
                            && mMediaPlayer.isPlaying()
                            && mMediaPlayer != null) {
                        long currPosition = mMediaPlayer.getCurrentPosition();
                        //播放之前的缓冲在onPrepare时已经缓冲完了，所以这里要排除进度为0
                        if (currPosition != 0 && prevPlayPosition >= currPosition) {
                            sendBufferingHandler(false, true);
                        } else {
                            sendBufferingHandler(true, false);
                        }
                        prevPlayPosition = currPosition;
                        mAudioPlayerEvent.onPlayProgress(mMediaPlayer.getDuration(), currPosition);
                    }
                }
            }
        };
        mProgressTimer.schedule(mProgressTask, 0);
    }
 
    private final IMediaPlayer.OnErrorListener onErrorListener = (iMediaPlayer, frameworkErr, implErr) -> {
        sendBufferingHandler(true, false);
        onErrorPlay();
        LogUtils.e(frameworkErr + "==>chenpin error " + implErr);
        return true;
    };
    private final IMediaPlayer.OnInfoListener onInfoListener = (iMediaPlayer, what, extra) -> true;
 
    private final IMediaPlayer.OnBufferingUpdateListener onBufferingUpdateListener = (iMediaPlayer, percent) -> {
    };
 
    private final IMediaPlayer.OnPreparedListener onPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {
            if (mAudioPlayerEvent != null) {
                synchronized (mProgressLock) {
                    mProgressLock.notifyAll();
                }
                mErrorPlayNum = 0;
                //准备完毕后发送一次缓存（有可能已经缓存完毕）
                mAudioPlayerEvent.onBufferingUpdate(mBufferedPercent);
                //当前正播放，播放器已经预缓存完毕，开始播放了
                sendBufferingHandler(true, false);
                //记录并发送当前播放状态
                sendPlayerStatus(AudioPlayEnum.PLAYER_PLAYING);
                //开始监听播放进度
                playProgressListener();
            }
        }
    };
    private final IMediaPlayer.OnCompletionListener onCompletionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer iMediaPlayer) {
            //播放完毕
            if (mAudioPlayerEvent != null) {
                prevPlayPosition = 0;
                sendPlayerStatus(AudioPlayEnum.PLAYER_COMPLETE);
                destroyTimer();
                if (mSingleLooping) {
                    //单曲循环
                    prepareAsync(currPlayPotion, mDataSourceList.get(currPlayPotion), isEnableCache);
                } else if (mListLooping || mDataSourceList.size() < currPlayPotion + 1) {
                    //列表循环
                    nextPlay();
                }
 
            }
        }
    };
}