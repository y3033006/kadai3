package com.example.y3033006.kadai3;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//MediaPlayerを扱うクラス
public class MyMedia {
    //MediaPlayerの動的リスト
    private final List<MediaPlayer> musicPlayer;
    //MediaPlayerが再生可能か（prepared状態になったかを）入れる順番は上のリストと同じにして対応づけられている
    private final List<Boolean> checkPrepared;

    //コンストラクタ
    public MyMedia(){
        musicPlayer = new ArrayList<>();
        checkPrepared = new ArrayList<>();
    }

    //動画を先送りするメソッド
    public void skipMusic(){
        for(int i=0;i<musicPlayer.size();i++){
            //現在位置を15s先にする
            musicPlayer.get(i).seekTo(musicPlayer.get(i).getCurrentPosition()+15000);
            //もし現在位置が曲の長さを超えていたら現在位置を曲の最後に変更
            if(musicPlayer.get(i).getCurrentPosition()>musicPlayer.get(i).getDuration()){
                musicPlayer.get(i).seekTo(musicPlayer.get(i).getDuration());
            }
        }
    }

    //動画を前戻りするメソッド
    public void rewindMusic(){
        for(int i=0;i<musicPlayer.size();i++){
            //現在位置を15s前にする
            musicPlayer.get(i).seekTo(musicPlayer.get(i).getCurrentPosition()-15000);
            //もし現在位置が0より小さくなってしまったら現在位置を0にする
            if(musicPlayer.get(i).getCurrentPosition()<0){
                musicPlayer.get(i).seekTo(0);
            }
        }
    }

    //再生中かを返すメソッド、どれか一つでも再生中ならtrueを返す
    public boolean isCheckPlaying(){
        for(int i=0;i<musicPlayer.size();i++){
            if(musicPlayer.get(i).isPlaying()){
                return true;
            }
        }
        return false;
    }

    //引数が曲の数と同じならtrueを返す
    public boolean isCheckSize(int i){
        return i == musicPlayer.size();
    }

    //ファイル名から曲の準備をするメソッド、Assetにあるファイルの再生に使う、推奨されていた非同期にしてある
    public void selectedMusic(String name, Context context){
        //まだ再生できないからfalseを入れる、非同期のため、エラー防止に必要
        checkPrepared.add(false);
        //MediaPlayer生成
        MediaPlayer player = new MediaPlayer();
        //MediaPlayerの準備をしていく
        try{
            AssetFileDescriptor assetFileDescriptor = context.getAssets().openFd(name);
            player.setDataSource(assetFileDescriptor.getFileDescriptor(),assetFileDescriptor.getStartOffset(),assetFileDescriptor.getLength());
            assetFileDescriptor.close();
        }catch(IllegalArgumentException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        //MediaPlayerリストに追加
        musicPlayer.add(player);
        //indexを用意
        int indexNum = musicPlayer.indexOf(player);
        //準備を完了したら目印としてcheckPreparedに入れたfalseをtrueに変更してからソートを呼ぶもの
        musicPlayer.get(indexNum).setOnPreparedListener(mp -> {
            checkPrepared.set(indexNum,true);
            sortList(indexNum);
        });
        //非同期で準備をしてくれるもの、これをよんだら状態遷移図でInitializedからPreparingになり完了したらPreparedになる
        musicPlayer.get(indexNum).prepareAsync();
    }

    //上のメソッドとのちがいは絶対パスから曲を作る点だけ
    public void selectedMusicPath(String path){
        checkPrepared.add(false);
        MediaPlayer player = new MediaPlayer();
        try{
            player.setDataSource(path);
        }catch(IllegalArgumentException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        musicPlayer.add(player);
        int indexNum = musicPlayer.indexOf(player);
        musicPlayer.get(indexNum).setOnPreparedListener(mp -> {
            checkPrepared.set(indexNum,true);
            sortList(indexNum);
        });
        musicPlayer.get(indexNum).prepareAsync();
    }

    //ファイル名から曲の準備をするメソッド、2個上との違いはこっちは非同期ではない、reStart（）で呼ばれる
    public void reCreateMusic(String name,Context context){
        //準備完了してからしかこのメソッドを抜けないため最初から準備完了を入れる
        checkPrepared.add(true);
        MediaPlayer player = new MediaPlayer();
        try{
            AssetFileDescriptor assetFileDescriptor = context.getAssets().openFd(name);
            player.setDataSource(assetFileDescriptor.getFileDescriptor(),assetFileDescriptor.getStartOffset(),assetFileDescriptor.getLength());
            assetFileDescriptor.close();
        }catch(IllegalArgumentException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        musicPlayer.add(player);
        int indexNum = musicPlayer.indexOf(player);
        //同期した準備を行う、準備完了しないと抜けられない
        try {
            musicPlayer.get(indexNum).prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //ソート
        sortList(indexNum);
    }

    //上との違いは絶対パスを使用する点
    public void reCreateMusicPath(String path){
        checkPrepared.add(true);
        MediaPlayer player = new MediaPlayer();
        try{
            player.setDataSource(path);
        }catch(IllegalArgumentException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        musicPlayer.add(player);
        int indexNum = musicPlayer.indexOf(player);
        try {
            musicPlayer.get(indexNum).prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        sortList(indexNum);
    }

    //リストをソートするもの、引数のindexの要素を移動させる、ソート結果は曲が長い程Indexが０に近くなる
    private void sortList(int indexNum){
        //要素が一個の時はそのままで要素が2個以上なら中へ
        if(indexNum!=0){
            for (int i = 0; i<indexNum;i++){
                //移動させる要素の曲の長さを前のよそのもの比べていって移動させるものが大きければそこに移動
                if(musicPlayer.get(i).getDuration()<musicPlayer.get(indexNum).getDuration()){
                    musicPlayer.add(i,musicPlayer.get(indexNum));
                    checkPrepared.add(i,checkPrepared.get(indexNum));
                    musicPlayer.remove(indexNum+1);
                    checkPrepared.remove(indexNum+1);
                    return;
                }
            }
        }
    }

    //いちばん長い曲の長さを返す、ソートしているので長いのはIndexが０の時
    public int getIndex0Duration(){
        return musicPlayer.get(0).getDuration();
    }

    //引数のindexの曲の現在位置返す
    public int getIndexCurrentPosition(int i){
        return musicPlayer.get(i).getCurrentPosition();
    }

    //曲の再生
    public void playMusic(){
        for(int i=0;i<musicPlayer.size();i++){
            if(musicPlayer.get(i).getDuration()!=musicPlayer.get(i).getCurrentPosition()) {
                musicPlayer.get(i).start();
            }
        }
    }

    //現在位置の変更
    public void setSeekTo(int position){
        for (int i = 0; i < musicPlayer.size(); i++) {
            musicPlayer.get(i).seekTo(Math.min(musicPlayer.get(i).getDuration(), position));
        }

    }

    //曲の再生、reStart(）で呼ばれる
    public void playMusicRestart(List<Integer> list) {
        //再生してた曲がなかったらそのまま終了
        if (checkPrepared.isEmpty()) {
            return;
        }
        //list内に入れた情報から現在位置を変えて再生
        for (int i = 0; i < musicPlayer.size(); i++) {
            musicPlayer.get(i).seekTo(list.get(i));
            musicPlayer.get(i).start();
        }
    }

    //再生可能かを返す、
    public boolean isCheckCanPlay(){
        //再生する曲がないときfalse
        if(checkPrepared.isEmpty()){
            return false;
        }
        //準備中のものがあればfalse
        for(int i=0; i<checkPrepared.size();i++){
            if(checkPrepared.get(i).equals(false)){
                return false;
            }
        }
        //問題なければtrue
        return true;
    }

    //リストが空かを返すメソッド,空ならTrue
    public boolean isListNull(){
        if(!checkPrepared.isEmpty()){
            return false;
        }else {
            return true;
        }
    }

    //曲を止めてreleaseする
    public void stopMusic(){
        for(int i=0;i<musicPlayer.size();i++){
            musicPlayer.get(i).stop();
        }
        releaseAll();
    }

    //releaseする
    public void releaseAll(){
        while(musicPlayer.size()>0){
            musicPlayer.get(0).release();
            musicPlayer.remove(0);
            checkPrepared.remove(0);
        }
    }

    //一時停止する
    public void pauseMusic(){
        for(int i=0;i<musicPlayer.size();i++){
            musicPlayer.get(i).pause();
        }
    }
}
