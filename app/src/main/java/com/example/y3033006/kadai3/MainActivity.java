package com.example.y3033006.kadai3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<MediaPlayer> musicPlayer;
    int preparedNum;
    private List<Boolean> checkPrepared;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView=(RecyclerView)findViewById(R.id.list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager((layoutManager));
        String[] myDataset = {"drbpm140.wav","saigetsu.mp3","kamigami.mp3","aaa","kkkk"};
        RecyclerView.Adapter myAdapter = new MyAdapter(myDataset);
        recyclerView.setAdapter(myAdapter);

        musicPlayer = new ArrayList<>();
        preparedNum=0;
        checkPrepared = new ArrayList<>();
        selectedMusic("drbpm140.wav");
        selectedMusic("saigetsu.mp3");
        System.out.println("set_"+checkPrepared);

        Button play =findViewById(R.id.playButton);

        play.setOnClickListener(v -> playMusic());

        Button stop =findViewById(R.id.stopButton);

        stop.setOnClickListener(v -> pauseMusic());
    }

    private void selectedMusic(String name){
        checkPrepared.add(false);
        MediaPlayer player = new MediaPlayer();
        try{
            AssetFileDescriptor assetFileDescriptor = getAssets().openFd(name);
            player.setDataSource(assetFileDescriptor.getFileDescriptor(),assetFileDescriptor.getStartOffset(),assetFileDescriptor.getLength());
            assetFileDescriptor.close();
        }catch(IllegalArgumentException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        musicPlayer.add(player);
        int indexNum = musicPlayer.indexOf(player);
        musicPlayer.get(indexNum).setOnPreparedListener(mp -> checkPrepared.set(indexNum,true));
        musicPlayer.get(indexNum).prepareAsync();
    }

    private void playMusic(){
        if(checkPrepared.isEmpty()){
            System.out.println("選択されてなあいです");
            return;
        }
        if(!checkCanPlay()){
            System.out.println(("読み込み中です"));
            return;
        }
        for(int i=0;i<musicPlayer.size();i++){
            musicPlayer.get(i).start();
        }
    }

    private boolean checkCanPlay(){
        System.out.println("check_"+checkPrepared);
        for(int i=0; i<checkPrepared.size();i++){
            if(checkPrepared.get(i).equals(false)){
                return false;
            }
        }
        return true;
    }

    private void stopMusic(){
        while(musicPlayer.size()>0) {
            musicPlayer.get(0).stop();
            musicPlayer.get(0).release();
            musicPlayer.remove(0);
        }
    }

    private void pauseMusic(){
        for(int i=0;i<musicPlayer.size();i++){
            musicPlayer.get(i).pause();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();

    }

    @Override
    protected void onPause(){
        super.onPause();
        stopMusic();
    }
}