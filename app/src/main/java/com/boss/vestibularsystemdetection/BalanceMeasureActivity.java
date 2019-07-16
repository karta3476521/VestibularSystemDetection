package com.boss.vestibularsystemdetection;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

public class BalanceMeasureActivity extends AppCompatActivity implements SensorEventListener {
    private MyMediaTool mp_balanceStart, mp_balanceEnd, mp_background;
    private Handler handler = new Handler();
    private SensorManager mSensorManager;
    private Sensor gyroscopeSensor, magneticSensor, accelerometerSensor;
    private List<Float> list_AxisX, list_AxisY, list_AxisZ;
    private float[] values, r, gravity, geomagnetic;
    private boolean isSensorStart = false;
    private int sec = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance_measure);
        getSupportActionBar().hide();

        init();
        createTimer();
    }

    private void init(){
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        magneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        accelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        list_AxisX = new ArrayList<Float>();
        list_AxisY = new ArrayList<Float>();
        list_AxisZ = new ArrayList<Float>();
        values = new float[3];
        gravity = new float[3];
        r = new float[9];
        geomagnetic = new float[3];
    }

    public void createTimer() {
        mp_balanceStart = new MyMediaTool(this, R.raw.balance_start);
        mp_balanceStart.startPlayer();
        handler.postDelayed(delayToBackgroundMusic, 8000);
    }

    private Runnable delayToBackgroundMusic = new Runnable() {
        @Override
        public void run() {
            mp_background.startPlayer();
            updateTimerSec.run();
        }
    };

    private Runnable updateTimerSec = new Runnable() {
        @Override
        public void run() {
            isSensorStart = true;
            sec -= 1;

            if (sec == -1) {
                finish30Sec();
            }else
                handler.postDelayed(updateTimerSec,1000);
        }
    };

    private Runnable delayToMain = new Runnable() {
        @Override
        public void run() {
            Intent main = new Intent(BalanceMeasureActivity.this, MainActivity.class);
            startActivity(main);
        }
    };

    private void stopTimer(){
        isSensorStart = false;
        //remove 所有在執行的任務
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(isSensorStart) {

            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {

                if (Math.abs(event.values[0]) > 3 || Math.abs(event.values[1]) > 3 || Math.abs(event.values[2]) > 3) {
                    stopTimer();
                    //刪除資料夾
                    DeletData mDeletData = new DeletData();
                    mDeletData.deleteFile();
                    MediaPlayer mPlayer = MediaPlayer.create(BalanceMeasureActivity.this, R.raw.fail);
                    mPlayer.start();
                    //播放直到結束後動作
                    mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        public void onCompletion(MediaPlayer mp) {
                            releasePlayer(mp);
                            Intent main = new Intent(BalanceMeasureActivity.this, MainActivity.class);
                            startActivity(main);
                        }
                    });
                }
            }

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                gravity = event.values;
                getValues();
            }

            if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
                geomagnetic = event.values;
            }
        }
    }

    private void getValues(){
        SensorManager.getRotationMatrix(r, null, gravity, geomagnetic);
        SensorManager.getOrientation(r, values);
        addValues();
    }

    private void addValues(){
        list_AxisZ.add(values[0]);
        list_AxisX.add(values[1]);
        list_AxisY.add(values[2]);
    }

    private void finish30Sec(){
        stopTimer();

        InputData mInputData = new InputData(this, "angleX", InputData.LAST);
        mInputData.record(list_AxisX);
        mInputData = new InputData(this, "angleY", InputData.LAST);
        mInputData.record(list_AxisY);
        mInputData = new InputData(this, "angleZ", InputData.LAST);
        mInputData.record(list_AxisZ);
        mp_balanceEnd.startPlayer();
        handler.postDelayed(delayToMain, 7000);
    }

    private void releasePlayer(MediaPlayer mp) {
        if (mp != null) {
            mp.stop();
            //必須 reset 某項資源才會重置，避免內存外洩
            mp.reset();

            mp.release();
            mp = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //KITKAT 版本>19
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//設置視窗全屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//保持螢幕常亮，需配合getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mSensorManager.registerListener(this, magneticSensor, 10 * 1000);
        mSensorManager.registerListener(this, gyroscopeSensor, 100 * 1000);
        mSensorManager.registerListener(this, accelerometerSensor, 10 * 1000);
        mp_balanceEnd = new MyMediaTool(this, R.raw.balance_end);
        mp_background = new MyMediaTool(BalanceMeasureActivity.this, R.raw.background30);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mSensorManager.unregisterListener(BalanceMeasureActivity.this);

        mp_balanceStart.releasePlayer();
        mp_balanceEnd.releasePlayer();
        mp_background.releasePlayer();
    }

    protected void onStop(){
        super.onStop();
    }

//    //使返回鍵失效
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }
}
