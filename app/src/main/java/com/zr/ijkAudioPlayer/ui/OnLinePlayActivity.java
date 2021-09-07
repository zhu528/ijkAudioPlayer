package com.zr.ijkAudioPlayer.ui;

import static com.zr.ijkaudioplayer.AudioPlayEnum.PLAYER_PAUSE;
import static com.zr.ijkaudioplayer.utils.Utils.formatMusicTime;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.permissionx.guolindev.PermissionX;
import com.zr.ijkAudioPlayer.R;
import com.zr.ijkaudioplayer.AudioPlayEnum;
import com.zr.ijkaudioplayer.bean.SongInfo;
import com.zr.ijkaudioplayer.listener.AudioPlayerListener;
import com.zr.ijkaudioplayer.manager.MyAudioManager;
import com.zr.ijkaudioplayer.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

public class OnLinePlayActivity extends AppCompatActivity implements View.OnClickListener {
    private MyAudioManager audioManager;
    private final List<SongInfo> mList = new ArrayList<>();
    private ImageView pause_btn;
    private ImageView playing_btn;
    private SeekBar audio_seek;
    private static final long REWIND_INTERVAL = 15000;   // 快退间隔（单位：ms）
    private static final long FORWARD_INTERVAL = 15000;  // 快进间隔（单位：ms）

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.com_on_line_player_activity);

        audioManager = new MyAudioManager(getApplication());


        setList();

        ImageView imageView = findViewById(R.id.iv_back_groud);
        Glide.with(this).asGif().load(R.drawable.com_backgroud).into(imageView);
        ImageView play_cycle = findViewById(R.id.play_cycl);
        ImageView skip_to_previous = findViewById(R.id.skip_to_previous);
        pause_btn = findViewById(R.id.pause_btn);
        playing_btn = findViewById(R.id.playing_btn);
        ImageView skip_to_next = findViewById(R.id.skip_to_next);
        ImageView play_single_cycle = findViewById(R.id.play_single_cycle);
        ImageView iv_rewind = findViewById(R.id.iv_rewind);
        ImageView iv_forward = findViewById(R.id.iv_forward);

        TextView start_time = findViewById(R.id.start_time);
        start_time.setText("00:00");
        TextView all_time = findViewById(R.id.all_time);


        play_cycle.setOnClickListener(this);
        skip_to_previous.setOnClickListener(this);
        pause_btn.setOnClickListener(this);
        playing_btn.setOnClickListener(this);
        skip_to_next.setOnClickListener(this);
        play_single_cycle.setOnClickListener(this);
        iv_rewind.setOnClickListener(this);
        iv_forward.setOnClickListener(this);

        audio_seek = findViewById(R.id.audio_seek);

        audio_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                audioManager.seekStart();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                audioManager.seekTo(seekBar.getProgress());
            }
        });
        audioManager.setAudioPlayerListener(new AudioPlayerListener() {
            @Override
            public void onBufferingUpdate(int percent) {
                LogUtils.d("percent: " + percent);
                audio_seek.setSecondaryProgress(percent);
            }

            @Override
            public void onBuffering(boolean isBuffering) {

            }

            @Override
            public void onPlayProgress(long duration, long currPosition) {
                LogUtils.d("duration: " + duration + "currPosition： " + currPosition);
                runOnUiThread(() ->{
                    audio_seek.setMax((int) duration);
                    all_time.setText(formatMusicTime(duration));
                    start_time.setText(formatMusicTime(currPosition));
                    audio_seek.setProgress((int) currPosition);
                });
            }

            @Override
            public void onStatusChange(AudioPlayEnum mStatus, int currPlayPotion) {

            }
        });
    }

    private void setList() {
        for (int i = 0; i < 2; i++){
            SongInfo songInfo = new SongInfo();
           if (i == 0){
               songInfo.setSongPlay_Url("http://m701.music.126.net/20210831175458/861842c46de1e8bfd6457399e62a0e22/jdyyaac/obj/w5rDlsOJwrLDjj7CmsOj/5542797965/e423/0961/91a3/efba4ddaaf3e51750b2b8a218cd035ef.m4a");
           } else {
               songInfo.setSongPlay_Url("http://m801.music.126.net/20210831175736/be1aed65030239cda423ffd3781fc47e/jdyyaac/obj/w5rDlsOJwrLDjj7CmsOj/9385607807/cbbd/04c5/f017/5a7f2f722df72ce12b5b348d9277b9f4.m4a");
           }
            mList.add(songInfo);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        long position = audioManager.getCurrentPosition();  // 获取当前播放进度
        PermissionX.init(OnLinePlayActivity.this)
                .permissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .request((allGranted, grantedList, deniedList) -> {
                    if (allGranted){
                        switch (v.getId()) {
                            case R.id.pause_btn:
                                if (audioManager.getPlayerStatus() == PLAYER_PAUSE){
                                    audioManager.reStart();
                                } else {
                                    //默认播放第一首
                                    audioManager.startList(mList, 0, true);
                                    //设置列表循环
                                    audioManager.setListLooping(true);
                                    audioManager.setSingleLooping(false);
                                }
                                pause_btn.setVisibility(View.GONE);
                                playing_btn.setVisibility(View.VISIBLE);
                                break;
                            case R.id.playing_btn:
                                audioManager.pause();
                                pause_btn.setVisibility(View.VISIBLE);
                                playing_btn.setVisibility(View.GONE);
                                break;
                            case R.id.play_cycl:
                                audioManager.setListLooping(true);
                                audioManager.setSingleLooping(false);
                                break;
                            case R.id.skip_to_previous:
                                audioManager.prevPlay();
                                break;
                            case R.id.skip_to_next:
                                audioManager.nextPlay();
                                break;
                            case R.id.play_single_cycle:
                                audioManager.setSingleLooping(true);
                                audioManager.setListLooping(false);
                                break;
                            case R.id.iv_rewind:
                                long targetPosition = position - REWIND_INTERVAL;
                                if (targetPosition > 0) {
                                    audioManager.seekTo(targetPosition);
                                    audio_seek.setProgress((int) targetPosition);
                                } else {
                                    audioManager.seekTo(0);
                                    audio_seek.setProgress(0);
                                }
                                break;
                            case R.id.iv_forward:
                                long duration = audioManager.getDuration();         // 获取总进度
                                long forwardTargetPosition = position + FORWARD_INTERVAL;
                                if (forwardTargetPosition <= duration) {
                                    audioManager.seekTo(forwardTargetPosition);
                                    audio_seek.setProgress((int) forwardTargetPosition);
                                } else {
                                    audioManager.seekTo(duration);
                                    audio_seek.setProgress((int) duration);
                                }
                                break;
                        }
                    }
                });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioManager != null) {
            audioManager.release();
        }
    }


}