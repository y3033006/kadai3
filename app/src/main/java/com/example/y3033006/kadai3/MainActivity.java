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

//layoutのイベント処理などを行っているメインクラス
public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback{
    //実行時permissionの確認に使う変数
    final int REQUEST_CODE = 1;
    //自作MyMediaクラスの宣言、MediaPlayerを扱うクラス
    private MyMedia myMedia;

    //RecyclerViewの宣言、再生させるものを選ぶときに使用
    public RecyclerView recyclerView;
    //RecyclerView.Adapterを継承した自作クラス宣言、RecyclerViewの中身を扱うやつ
    public RecyclerView.Adapter recyclerAdapter;
    //再生できる音楽のfile名を保存、ただしdownloadディレクトリ（assetsとraw以外）にあるものは絶対パスを入れている
    private List<String> mFileName;
    //実際に再生される音楽の選択段階のfile名を保存、保存方法同上、後述decisionMusicButtonが押されたら保存してある数字に対応するmFileNameの音楽を準備
    private List<Integer> mSelectedFile;
    //OnStopメソッドが呼ばれたときに各曲の現在位置を保存していくリスト
    private List<Integer> mCurrentPosition;

    //downloadディレクトリの絶対パスを入れる変数
    private String downloadsPath;
    //downloadディレクトリ下にあるファイルとディレクトリを入れる配列
    private File[] downloadUnderFile;
    //downloadディレクトリ下にある再生可能な音楽ファイルのファイル名を入れる配列
    private final List<String> songList = new ArrayList<>();
    //downloadディレクトリ下にある再生可能な音楽ファイルのファイルを絶対パスに変えてから入れる変数
    private String loadFilePath;

    //音楽のシークバー
    private SeekBar musicBar;

    //シークバーを進ませるThreadが1系統しか生まれないようにするための判定変数
    private Boolean checkBarThread;
    //シークバーを進ませるThreadを更新するかの判定変数
    private Boolean checkUpdateTread;

    //SeekBarのつまみを触った時に音楽が再生中だったかを入れる変数、trueで再生中だった
    private Boolean checkBarPlay;

    //再生と一時停止を行うボタン
    private ImageButton startPause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //自作クラスの生成
        myMedia = new MyMedia();

        //recyclerViewを使うための処理など
        recyclerView=findViewById(R.id.list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager((layoutManager));
        mFileName = new ArrayList<>();
        mSelectedFile = new ArrayList<>();
        //サンプルとしてassetに入れてある音楽ファイルの名前を入れる
        mFileName.add("drbpm140.wav");
        mFileName.add("kamigami.mp3");
        mFileName.add("saigetsu.mp3");
        //RecyclerView.Adapterを継承した自作クラスを生成してセット
        recyclerAdapter = new MyAdapter(mFileName,mSelectedFile);
        recyclerView.setAdapter(recyclerAdapter);

        //生成
        mCurrentPosition = new ArrayList<>();

        //生成
        musicBar = findViewById((R.id.musicBar));
        //初期値入力
        checkBarPlay=false;

        //シークバーを動かしたときに呼ばれる
        musicBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            //つまみをドラッグしたとき
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }
            //つまみに触った時
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //再生中かを判別再生中なら中へ
                if(myMedia.isCheckPlaying()) {
                    //再生を一時停止
                    myMedia.pauseMusic();
                    //シークバーを進めるスレッドの更新を止める
                    checkUpdateTread = false;
                    //触った時音楽を再生中だったことを記憶
                    checkBarPlay=true;
                }
            }
            //つまみを離した時
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //現在のシークバーの位置から音楽の再生位置を変更
                myMedia.setSeekTo(musicBar.getProgress());
                //触った時音楽を再生中だったかで判別、再生中だったら中へ
                if(checkBarPlay) {
                    //音楽を再生する
                    myMedia.playMusic();
                    //シークバーを進ませるスレッドを作る
                    makeBarThread();
                    //初期値に戻す
                    checkBarPlay=false;
                }
            }
        });

        //初期値入力,falseでまだスレッドがない状態
        checkBarThread = false;
        //初期値入力,falseで更新しない
        checkUpdateTread = false;

        //idからlayoutと関連付け
        startPause=findViewById(R.id.startPause);

        //再生一時停止ボタンが押されたら呼ばれる
        startPause.setOnClickListener(v -> {
            //音楽が再生中かで判別、再生中だったら中へ
            if(myMedia.isCheckPlaying()){
                //音楽を一時停止
                myMedia.pauseMusic();
                //シークバーの更新をするスレッドを止める
                checkUpdateTread=false;
                //ボタンの画像を再生ボタンに変更
                startPause.setImageResource(android.R.drawable.ic_media_play);
            //音楽が再生中ではなくて音楽が再生可能状態かで判別、再生可能なら中へ
            } else if(myMedia.isCheckCanPlay()){
                //音楽を再生
                myMedia.playMusic();
                //シークバーの最大値を一番長い曲の時間にする
                musicBar.setMax(myMedia.getIndex0Duration());
                //シークバーを更新するスレッドを作る
                makeBarThread();
                //ボタンの画像を一時停止ボタンに変更
                startPause.setImageResource(android.R.drawable.ic_media_pause);
            //音楽が再生中ではなくて再生をできないとき
            }else{
                //読み込み中と表示、後で変更
                System.out.println("読み込み中"+mSelectedFile);
            }
        });

        //再生する音楽の決定ボタン、おしたら保存してある数字に対応するmFileNameの音楽の再生準備をする
        Button decisionMusicButton = findViewById(R.id.decisionMusicButton);
        //再生する音楽の決定ボタンが押されたら呼ばれる
        decisionMusicButton.setOnClickListener(v -> {
            //音楽が再生中だったらなかへ
            if(myMedia.isCheckPlaying()){
                //音楽を一時停止
                myMedia.pauseMusic();
                //シークバーの更新をするスレッドを止める
                checkUpdateTread=false;
                //ボタンの画像を再生ボタンに変更
                startPause.setImageResource(android.R.drawable.ic_media_play);
            }
            //シークバーの位置を0にする
            musicBar.setProgress(0);
            //今使っているMediaPlayerを全部消してrelease
            myMedia.releaseAll();
            //再生する音楽の数だけループ
            for(int i=0;i<mSelectedFile.size();i++){
                //ファイル名が絶対パスで保存されていたら中へ、判別方法は簡易的なもの
                if(mFileName.get(mSelectedFile.get(i)).contains("/")){
                    //絶対パスからMediaPlayerを再生できるよう作る
                    myMedia.selectedMusicPath(mFileName.get(mSelectedFile.get(i)));
                }else{
                    //ファイル名からediaPlayerを再生できるよう作る
                    myMedia.selectedMusic(mFileName.get(mSelectedFile.get(i)), getApplicationContext());
                }
            }
        });

        //downloadの絶対パスをもらって変数の中へ
        File pathExternalPublicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        downloadsPath =pathExternalPublicDir.getPath();
        //Permission関連の処理があるのでそっちで処理、問題なくできたらdownloadUnderFileの中にdownload下のファイルとディレクトリが入る
        CheckPermission();
        //NullPointer対策のif
        if(downloadUnderFile !=null) {
            //downloadUnderFileの中のファイルやディレクトリをから再生可能な音楽ファイルだけを判別してsongListの中へ入れる
            for (File file : downloadUnderFile) {
                if (file.isFile() && file.getName().endsWith(".mp3")) {
                    songList.add(file.getName());
                }else if(file.isFile() && file.getName().endsWith(".wav")){
                    songList.add(file.getName());
                }else if(file.isFile() && file.getName().endsWith(".ogg")){
                    songList.add(file.getName());
                }else if(file.isFile() && file.getName().endsWith(".mid")){
                    songList.add(file.getName());
                }else if(file.isFile() && file.getName().endsWith(".m4a")){
                    songList.add(file.getName());
                }else if(file.isFile() && file.getName().endsWith(".aac")){
                    songList.add(file.getName());
                }
            }
        }
        //もし再生可能な音楽ファイルがなければ代わりにダミーデータを兼ねる"なし"を入れる
        if(songList.isEmpty()){
            songList.add("なし");
        }

        //スピナーを扱うための処理
        ArrayAdapter spinnerAdapter;
        spinnerAdapter = new ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,songList);
        Spinner spinnerDownload = findViewById(R.id.downloadSpinner);
        spinnerDownload.setAdapter((spinnerAdapter));
        //スピナーのイベント処理
        spinnerDownload.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //アイテムが選択されたとき
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //もし"なし"以外が選ばれたときは
                if(parent.getAdapter().getItem(position)!="なし") {
                    //絶対パスを作る
                    loadFilePath = downloadsPath + "/" + parent.getAdapter().getItem(position);
                }else{
                    //そのまま"なし"を伝える
                    loadFilePath = "なし";
                }
            }
            //現在のものが選ばれたとき
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //動画を15ｓ先送りするボタン
        ImageButton skipButton = findViewById(R.id.skip15s);
        ////動画を15ｓ先送りするボタンが押されたとき呼ばれる
        skipButton.setOnClickListener(v-> {
            //動画を飛ばす
            myMedia.skipMusic();
            //再生するものが空でないなら
            if(!myMedia.isListNull()) {
                //シークバーを現在位置に変更する
                musicBar.setProgress(myMedia.getIndexCurrentPosition(0));
            }
        });

        //動画を15ｓ前戻りするボタン
        ImageButton rewindButton = findViewById(R.id.rewind15s);
        ////動画を15ｓ前戻りするボタンが押されたとき呼ばれる
        rewindButton.setOnClickListener(v-> {
            //動画を戻す
            myMedia.rewindMusic();
            //再生するものが空でないなら
            if(!myMedia.isListNull()) {
                //シークバーを現在位置に変更する
                musicBar.setProgress(myMedia.getIndexCurrentPosition(0));
            }
        });

        //再生できる音楽を追加するボタン,downloadディレクトリにある音楽ファイルから追加
        Button loadButton=findViewById(R.id.loadButton);
        //再生する音楽を追加するボタンが押されたら呼ばれる
        loadButton.setOnClickListener(v->{
            //再生可能な音楽ファイルとして追加、絶対パスで保存
            mFileName.add(loadFilePath);
            //recyclerViewのリストを更新する
            recyclerAdapter.notifyItemInserted(mFileName.lastIndexOf(loadFilePath));
        });
    }

    //実行時permissionの処理
    public void CheckPermission(){
        //権利があるかを判定、なかったら中へ
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //権利がもらえないかリクエストしてみる
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA}, REQUEST_CODE);
            //結果を見てみる
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE);//ここはよくわからないけど、必要っぽい？
        //権利があったら
        }else{
            //ファイルとディレクトリを読み込んで保存
            downloadUnderFile = new File(downloadsPath).listFiles();
        }
    }

    //権利がもらえないかのリクエストするものだと思われる
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            //権利がもらえたら中へ
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // パーミッションが必要な処理
                downloadUnderFile = new File(downloadsPath).listFiles();
            //もらえなかった時
            } else {
                //機能の一つが使えないままにする
                System.out.println("permissionsもらえず");
                // パーミッションが得られなかった時
                // 処理を中断する・エラーメッセージを出す・アプリケーションを終了する等
            }
        }
    }

    //シークバーを進ませるスレッド
    private void makeBarThread(){
        //まだおなじけいれつがないならなかへ
        if(!checkBarThread) {
            //別の系列のスレッドを作らないようにする
            checkBarThread = true;
            //スレッドの更新を許可
            checkUpdateTread=true;
            //UIスレッド状で別のスレッドを動かすためのやつ
            Handler handler = new Handler();
            //スレッドを生成
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //スレッド更新が許可されているかで判別
                    if (checkUpdateTread) {
                        //現在位置を読み込む
                        int currentPosition = myMedia.getIndexCurrentPosition(0);
                        //シークバー現在位置に変更する
                        musicBar.setProgress(currentPosition);
                        //1s後に同じスレッドを実行する
                        handler.postDelayed(this, 1000);
                    } else{
                        //すべてのスレッドが消えたため別のスレッドの許可
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
        super.onStop();
        //再生している音楽の現在の位置を記憶していく
        int index=0;
        while(!myMedia.isCheckSize(index)){
            mCurrentPosition.add(myMedia.getIndexCurrentPosition(index));
            index=index+1;
        }
        //MediaPlayerを止めてrelease
        myMedia.stopMusic();
        checkUpdateTread=false;
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        for(int i=0;i<mSelectedFile.size();i++){
            //ファイル名が絶対パスで保存されていたら中へ、判別方法は簡易的なもの
            if(mFileName.get(mSelectedFile.get(i)).contains("/")){
                //絶対パスからMediaPlayerを再生できるよう作る
                myMedia.reCreateMusicPath(mFileName.get(mSelectedFile.get(i)));
            }else{
                //ファイル名からediaPlayerを再生できるよう作る
                myMedia.reCreateMusic(mFileName.get(mSelectedFile.get(i)), getApplicationContext());
            }
        }
        //onStopで保存した現在位置を反映させる
        int index =0;
        while(!myMedia.isCheckSize(index)){
            myMedia.playMusicRestart(mCurrentPosition);
            index=index+1;
        }
        //いらなくなったものを消す
        mCurrentPosition.clear();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }
}