package com.boss.vestibularsystemdetection;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private Intent intent;
    private SensorManager mSensorManager;
    private Sensor magneticSensor, accelerometerSensor;
    private boolean isHaveSensor = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        magneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(magneticSensor == null)
            Toast.makeText(this, "沒有磁力儀，無法測量", Toast.LENGTH_SHORT).show();
        else if(accelerometerSensor == null)
            Toast.makeText(this, "沒有加速度儀，無法測量", Toast.LENGTH_SHORT).show();
        else
            isHaveSensor = true;

        getCameraPermission();
        getWritePermission();
    }

    private void getWritePermission(){
        int permissionCheck = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    private void getCameraPermission(){
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
        }
    }

    public void measure_onClick(View v){
        //確認相機權限
        if(!MyUtils.checkPermission(this)) {
            Toast.makeText(this, "請開啟權限才可量測", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
        }else if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "請開啟權限才可量測", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }else if(isHaveSensor){
            intent = new Intent(MainActivity.this, SelectMeasurementMethodActivity.class);
            startActivity(intent);
        }
    }

    public void view_onClick(View v){
        intent = new Intent(MainActivity.this, NewListOfViewActivity.class);
        startActivity(intent);
    }

    //使返回鍵失效
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
