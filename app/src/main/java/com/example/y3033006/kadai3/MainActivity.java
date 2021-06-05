package com.example.y3033006.kadai3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private MyMedia myMedia;

    public RecyclerView recyclerView;
    public RecyclerView.Adapter myAdapter;
    private List<String> mFileName;
    private List<Integer> mSelectedFile;
    private List<Integer> mCurrentPosition;


    private SeekBar musicBar;
    //Threadが1系統しか生まれないようにするための判定変数
    private Boolean checkBarThread;
    //Threadを更新するかの判定変数
    private Boolean checkUpdateTread;
    //SeekBarのつまみを触った時に音楽が再生中だったかを入れる変数、trueで再生中だった
    private Boolean checkBarPlay;


    private ImageButton startPause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myMedia = new MyMedia();

        recyclerView=findViewById(R.id.list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager((layoutManager));
        mFileName = new ArrayList<>();
        mSelectedFile = new ArrayList<>();
        mFileName.add("drbpm140.wav");
        mFileName.add("kamigami.mp3");
        mFileName.add("saigetsu.mp3");
        System.out.println(mFileName);
        myAdapter = new MyAdapter(mFileName,mSelectedFile);
        recyclerView.setAdapter(myAdapter);

        mCurrentPosition = new ArrayList<>();

        musicBar = findViewById((R.id.musicBar));
        checkBarPlay=false;

        musicBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(myMedia.checkIsPlayingAll()) {
                    myMedia.pauseMusic();
                    checkUpdateTread = false;
                    checkBarPlay=true;
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                System.out.println(musicBar.getProgress());
                myMedia.setSeekTo(musicBar.getProgress());
                if(checkBarPlay) {
                    myMedia.playMusic();
                    makeBarThread();
                    checkBarPlay=false;
                }
            }
        });

        checkBarThread = false;
        checkUpdateTread = false;

        startPause=findViewById(R.id.startPause);

        startPause.setOnClickListener(v -> {
            if(myMedia.checkIsPlayingAll()){
                myMedia.pauseMusic();
                checkUpdateTread=false;
                startPause.setImageResource(android.R.drawable.ic_media_play);
            } else if(myMedia.checkCanPlay()){
                myMedia.playMusic();
                musicBar.setMax(myMedia.getIndex0Duration());
                makeBarThread();
                startPause.setImageResource(android.R.drawable.ic_media_pause);
            }else{
                //mFileName.add("saigetsu.mp3");
                //myAdapter.notifyItemInserted(mFileName.lastIndexOf("saigetsu.mp3"));
                //myAdapter.notifyDataSetChanged();
                System.out.println("読み込み中"+mSelectedFile);
            }
        });

        Button decisionMusicButton = findViewById(R.id.decisionMusicButton);
        decisionMusicButton.setOnClickListener(v -> {
            if(myMedia.checkIsPlayingAll()){
                myMedia.pauseMusic();
                checkUpdateTread=false;
                startPause.setImageResource(android.R.drawable.ic_media_play);
            }
            musicBar.setProgress(0);
            myMedia.releaseAll();
            for(int i=0;i<mSelectedFile.size();i++){
                myMedia.selectedMusic(mFileName.get(mSelectedFile.get(i)), getApplicationContext());
            }
            System.out.println("MainActivity120");
            myMedia.printList();
        });
        ImageButton skipButton = findViewById(R.id.skip15s);
        skipButton.setOnClickListener(v-> {
            myMedia.skipMusic();
            if(!myMedia.isListNull()) {
                musicBar.setProgress(myMedia.getIndexCurrentPosition(0));
            }
        });

        ImageButton rewindButton = findViewById(R.id.rewind15s);
        rewindButton.setOnClickListener(v-> {
            myMedia.rewindMusic();
            if(!myMedia.isListNull()) {
                musicBar.setProgress(myMedia.getIndexCurrentPosition(0));
            }
        });
    }

    private void makeBarThread(){
        if(!checkBarThread) {
            checkBarThread = true;
            checkUpdateTread=true;
            Handler handler = new Handler();
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (checkUpdateTread) {
                        int currentPosition = myMedia.getIndexCurrentPosition(0);
                        musicBar.setProgress(currentPosition);
                        handler.postDelayed(this, 1000);
                    } else{
                        checkBarThread=false;
                    }
                }
            });
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    @Override
    protected void onStop(){
        int index=0;
        while(!myMedia.isCheckSize(index)){
            System.out.println(index);
            mCurrentPosition.add(myMedia.getIndexCurrentPosition(index));
            index=index+1;
        }
        myMedia.stopMusic();
        checkUpdateTread=false;
        super.onStop();
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        for (String s : mFileName) {
            myMedia.reCreateMusic(s, getApplicationContext());
        }
        int index =0;
        while(!myMedia.isCheckSize(index)){
            myMedia.playMusicRestart(mCurrentPosition);
            index++;
        }

    }
}