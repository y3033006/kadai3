package com.example.y3033006.kadai3;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyMedia {
    private final List<MediaPlayer> musicPlayer;
    private final List<Boolean> checkPrepared;

    public MyMedia(){
        musicPlayer = new ArrayList<>();
        checkPrepared = new ArrayList<>();
    }

    public void printList(){
        System.out.println(musicPlayer);
        System.out.println(checkPrepared);
    }

    public void skipMusic(){
        for(int i=0;i<musicPlayer.size();i++){
            musicPlayer.get(i).seekTo(musicPlayer.get(i).getCurrentPosition()+15000);
            if(musicPlayer.get(i).getCurrentPosition()>musicPlayer.get(i).getDuration()){
                musicPlayer.get(i).seekTo(musicPlayer.get(i).getDuration());
            }
        }
    }

    public void rewindMusic(){
        for(int i=0;i<musicPlayer.size();i++){
            musicPlayer.get(i).seekTo(musicPlayer.get(i).getCurrentPosition()-15000);
            if(musicPlayer.get(i).getCurrentPosition()<0){
                musicPlayer.get(i).seekTo(0);
            }
        }
    }

    public boolean checkIsPlayingAll(){
        for(int i=0;i<musicPlayer.size();i++){
            if(musicPlayer.get(i).isPlaying()){
                return true;
            }
        }
        return false;
    }

    public boolean isCheckSize(int i){
        return i == musicPlayer.size();
    }

    public void selectedMusic(String name, Context context){
        checkPrepared.add(false);
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
        musicPlayer.get(indexNum).setOnPreparedListener(mp -> {
                checkPrepared.set(indexNum,true);
                sortList(indexNum);
                printList();
        });
        musicPlayer.get(indexNum).prepareAsync();
    }

    public void selectedMusicPass(String path){
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
            printList();
        });
        musicPlayer.get(indexNum).prepareAsync();
    }
    public void reCreateMusic(String name,Context context){
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
        try {
            musicPlayer.get(indexNum).prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        sortList(indexNum);
        System.out.println("re");
        printList();
    }

    private void sortList(int indexNum){
        if(indexNum!=0){
            for (int i = 0; i<indexNum;i++){
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

    public int getIndex0Duration(){
        return musicPlayer.get(0).getDuration();
    }

    public int getIndexCurrentPosition(int i){
        return musicPlayer.get(i).getCurrentPosition();
    }

    public void playMusic(){
        for(int i=0;i<musicPlayer.size();i++){
            if(musicPlayer.get(i).getDuration()!=musicPlayer.get(i).getCurrentPosition()) {
                musicPlayer.get(i).start();
            }
        }
    }

    public void setSeekTo(int position){
        for (int i = 0; i < musicPlayer.size(); i++) {
            musicPlayer.get(i).seekTo(Math.min(musicPlayer.get(i).getDuration(), position));
            System.out.println("seek"+i+":"+musicPlayer.get(i).getCurrentPosition());
        }

    }

    public void playMusicRestart(List<Integer> list) {
        if (checkPrepared.isEmpty()) {
            return;
        }
        for (int i = 0; i < musicPlayer.size(); i++) {
            musicPlayer.get(i).seekTo(list.get(i));
            musicPlayer.get(i).start();
        }
    }

    public boolean checkCanPlay(){
        if(checkPrepared.isEmpty()){
            System.out.println("選択されてなあいです");
            return false;
        }
        for(int i=0; i<checkPrepared.size();i++){
            if(checkPrepared.get(i).equals(false)){
                return false;
            }
        }
        return true;
    }

    public boolean isListNull(){
        if(!checkPrepared.isEmpty()){
            System.out.println("選択されてなあいです");
            return false;
        }else {
            return true;
        }
    }

    public void stopMusic(){
        while(musicPlayer.size()>0) {
            musicPlayer.get(0).stop();
        }
        releaseAll();
    }

    public void releaseAll(){
        while(musicPlayer.size()>0){
            musicPlayer.get(0).release();
            musicPlayer.remove(0);
            checkPrepared.remove(0);
        }
    }

    public void pauseMusic(){
        for(int i=0;i<musicPlayer.size();i++){
            musicPlayer.get(i).pause();
        }
    }
}
