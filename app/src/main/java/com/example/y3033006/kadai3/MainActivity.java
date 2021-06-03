package com.example.y3033006.kadai3;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    private MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume(){
        super.onResume();
        player = MediaPlayer.create(this,R.raw.saigetsu);
        player.start();
    }

    @Override
    protected void onPause(){
        super.onPause();
        player.stop();
        player.release();
    }
}