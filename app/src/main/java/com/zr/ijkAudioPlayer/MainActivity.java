package com.zr.ijkAudioPlayer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.zr.ijkAudioPlayer.R;
import com.zr.ijkAudioPlayer.ui.LocalPlayActivity;
import com.zr.ijkAudioPlayer.ui.OnLinePlayActivity;


public class MainActivity extends AppCompatActivity{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button localBtn = findViewById(R.id.bt_local);
        Button onLineBtn = findViewById(R.id.bt_on_line);

        localBtn.setOnClickListener(v -> startActivity(new Intent(this, LocalPlayActivity.class)));
        onLineBtn.setOnClickListener(v -> startActivity(new Intent(this, OnLinePlayActivity.class)));
    }
}