package com.boss.vestibularsystemdetection;

import android.content.Context;
import android.media.MediaPlayer;

public class MyMediaTool {
    private Context mContext;
    private MediaPlayer mMediaPlayer;

    public MyMediaTool(Context context, int mediaResource){
        mContext = context;
        mMediaPlayer = MediaPlayer.create(mContext, mediaResource);
    }

    public void startPlayer(){
         mMediaPlayer.start();
    }

    public void stopPlayer(){
        mMediaPlayer.stop();
    }

    public boolean isPlay(){
        return mMediaPlayer.isPlaying();
    }

    public void resetPlayer(){
        mMediaPlayer.reset();
    }

    public void releasePlayer() {

        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            //必須 reset 某項資源才會重置，避免內存外洩
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
}
