package com.zr.ijkAudioPlayer.ui;

import static com.zr.ijkaudioplayer.AudioPlayEnum.PLAYER_COMPLETE;
import static com.zr.ijkaudioplayer.AudioPlayEnum.PLAYER_PAUSE;
import static com.zr.ijkaudioplayer.AudioPlayEnum.PLAYER_PLAYING;
import static com.zr.ijkaudioplayer.utils.Utils.formatMusicTime;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.permissionx.guolindev.PermissionX;
import com.zr.ijkAudioPlayer.R;
import com.zr.ijkAudioPlayer.adapter.LocalListAdapter;
import com.zr.ijkaudioplayer.AudioPlayEnum;
import com.zr.ijkaudioplayer.bean.SongInfo;
import com.zr.ijkaudioplayer.listener.AudioPlayerListener;
import com.zr.ijkaudioplayer.manager.MyAudioManager;
import com.zr.ijkaudioplayer.utils.LogUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LocalPlayActivity<T> extends AppCompatActivity implements View.OnClickListener {
    private LocalListAdapter adapter;
    private List<SongInfo> songs;
    private MyAudioManager audioManager;
    private ImageView pause_btn;
    private ImageView playing_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.com_local_activity);
        RecyclerView mRecycler = findViewById(R.id.my_recycler);
        ImageView play_cycle = findViewById(R.id.play_cycl);
        ImageView skip_to_previous = findViewById(R.id.skip_to_previous);
        pause_btn = findViewById(R.id.pause_btn);
        playing_btn = findViewById(R.id.playing_btn);
        ImageView skip_to_next = findViewById(R.id.skip_to_next);
        ImageView play_single_cycle = findViewById(R.id.play_single_cycle);

        TextView start_time = findViewById(R.id.start_time);
        start_time.setText("00:00");
        TextView all_time = findViewById(R.id.all_time);


        play_cycle.setOnClickListener(this);
        skip_to_previous.setOnClickListener(this);
        pause_btn.setOnClickListener(this);
        playing_btn.setOnClickListener(this);
        skip_to_next.setOnClickListener(this);
        play_single_cycle.setOnClickListener(this);

        songs = new ArrayList<>();
        adapter = new LocalListAdapter(songs);
        mRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecycler.setAdapter(adapter);
        PermissionX.init(LocalPlayActivity.this)
                .permissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .request((allGranted, grantedList, deniedList) -> {
                    if (allGranted) {
                        getAllSongs(this);
                    }
                });


        audioManager = new MyAudioManager(getApplication());
        adapter.setOnItemClickListener(position -> {
            audioManager.startList(songs, position, false);
            pause_btn.setVisibility(View.VISIBLE);
            playing_btn.setVisibility(View.GONE);
        });

        SeekBar audio_seek = findViewById(R.id.audio_seek);
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

            }

            @Override
            public void onBuffering(boolean isBuffering) {

            }

            @Override
            public void onPlayProgress(long duration, long currPosition) {
                LogUtils.d("duration " + duration + "currPosition " + currPosition);
                runOnUiThread(() -> {
                    audio_seek.setMax((int) duration);
                    all_time.setText(formatMusicTime(duration));
                    start_time.setText(formatMusicTime(currPosition));
                    audio_seek.setProgress((int) currPosition);
                });
            }

            @Override
            public void onStatusChange(AudioPlayEnum mStatus, int currPlayPotion) {
                if (mStatus == PLAYER_PLAYING){
                    runOnUiThread(() -> {
                        pause_btn.setVisibility(View.GONE);
                        playing_btn.setVisibility(View.VISIBLE);
                    });
                } else if (mStatus == PLAYER_COMPLETE){
                    start_time.setText(formatMusicTime(audioManager.getDuration()));
                    audio_seek.setProgress((int) audioManager.getDuration());
                }
            }
        });

        updateMedia(this, "/storage/emulated/0/Music");
    }

    public static void updateMedia(final Context context, String path){
        //当大于等于Android 4.4时
        MediaScannerConnection.scanFile(context, new String[]{path}, null, (path1, uri) -> {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(uri);
            context.sendBroadcast(mediaScanIntent);
        });
    }

    /**
     * 获取sd卡所有的音乐文件
     */
    private void getAllSongs(Context context) {
        @SuppressLint("Recycle") Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        while (cursor != null && cursor.moveToNext()) {
            //id
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
            //uri
            Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
            //歌曲名
            String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
            //大小
            String duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
            //路径
            String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
            File file = new File(path);
            if (!file.exists()) {
                continue;
            }
            Log.d("lws", "音乐path:" + path);
            SongInfo song = new SongInfo();
            song.setSongPlay_Url(path);
            song.setSong_Name(name);
            song.setDuration(duration);
            songs.add(song);
        }
        adapter.notifyDataSetChanged();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pause_btn:
                if (audioManager.getPlayerStatus() == PLAYER_PAUSE){
                    audioManager.reStart();
                } else {
                    audioManager.startList(songs, 0, false);
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
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioManager != null) {
            audioManager.release();
        }
    }

}
