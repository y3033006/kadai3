package com.example.y3033006.kadai3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback{
    final int REQUEST_CODE = 1;
    private MyMedia myMedia;

    public RecyclerView recyclerView;
    public RecyclerView.Adapter myAdapter;
    private List<String> mFileName;
    private List<Integer> mSelectedFile;
    private List<Integer> mCurrentPosition;

    String pass;
    File[] files;
    private final List<String> songList = new ArrayList<>();

    private SeekBar musicBar;
    //Threadが1系統しか生まれないようにするための判定変数
    private Boolean checkBarThread;
    //Threadを更新するかの判定変数
    private Boolean checkUpdateTread;
    //SeekBarのつまみを触った時に音楽が再生中だったかを入れる変数、trueで再生中だった
    private Boolean checkBarPlay;


    private ImageButton startPause;

    String loadFilePath;

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
            //String path = pass+"/toho_a.mp3";
            //myMedia.selectedMusicPass(path);
            for(int i=0;i<mSelectedFile.size();i++){
                if(mFileName.get(mSelectedFile.get(i)).contains("/")){
                    myMedia.selectedMusicPass(mFileName.get(mSelectedFile.get(i)));
                }else{
                    myMedia.selectedMusic(mFileName.get(mSelectedFile.get(i)), getApplicationContext());
                }
            }
            System.out.println("MainActivity120");
            myMedia.printList();
        });

        File pathExternalPublicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        pass =pathExternalPublicDir.getPath();
        CheckPermission();
        //files = new File(pass).listFiles();
        if(files!=null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".mp3")) {
                    songList.add(file.getName());
                }
            }
        }
        //songList.clear();
        if(songList.isEmpty()){
            songList.add("なし");
        }

        ArrayAdapter adapter;
        adapter = new ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,songList);
        Spinner spinnerDownload = findViewById(R.id.downloadSpinner);
        spinnerDownload.setAdapter((adapter));
        spinnerDownload.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(parent.getAdapter().getItem(position)!="なし") {
                    loadFilePath = pass + "/" + parent.getAdapter().getItem(position);
                }else{
                    loadFilePath = "なし";
                }
                //System.out.println(loadFilePath);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
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
        Button loadButton=findViewById(R.id.loadButton);
        loadButton.setOnClickListener(v->{
            mFileName.add(loadFilePath);
            myAdapter.notifyItemInserted(mFileName.lastIndexOf(loadFilePath));
        });
    }

    public  void CheckPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //ここでパーミッションがあるかチェックしてる？
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA}, REQUEST_CODE);
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE);//ここはよくわからないけど、必要っぽい？


        }else{
            files = new File(pass).listFiles();
                //ActivityCompat.requestPermissions(this,new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE},GetResultPermission);
                //このelse文でユーザ側にアクセス可能かどうかをポップアップで聞いてくる
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // パーミッションが必要な処理
                files = new File(pass).listFiles();
            } else {
                System.out.println("permissionsもらえず");
                // パーミッションが得られなかった時
                // 処理を中断する・エラーメッセージを出す・アプリケーションを終了する等
            }
        }
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