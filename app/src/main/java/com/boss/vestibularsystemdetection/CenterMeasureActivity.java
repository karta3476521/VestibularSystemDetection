package com.boss.vestibularsystemdetection;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CenterMeasureActivity extends AppCompatActivity implements SurfaceTexture.OnFrameAvailableListener, SensorEventListener {
    private MyMediaTool mp_centerStart, mp_ding;
    private Handler handler = new Handler();
    private SensorManager mSensorManager;
    private Sensor gyroscopeSensor, magneticSensor, accelerometerSensor;
    private List<Float> list_AxisX, list_AxisY, list_AxisZ;
    private float[] values, r, gravity, geomagnetic;
    private boolean isSensorStart = false;
    private Intent balanceMeasure;
    private float timestamp;
    private int index_ding = 0;
    //以下為VR 物件
    private CameraManager mCameraManager;
    private MyGLSurfaceView glSurfaceView;
    MyGLRenderer renderer;
    private SurfaceTexture surface;
    private String mCameraId;
    private CameraDevice mCameraDevice;
    private Handler mBackgroundHandler;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CameraCaptureSession mCaptureSession;
    private CaptureRequest mPreviewRequest;
    private HandlerThread mBackgroundThread;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private Size mPreviewSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_center_measure);
        getSupportActionBar().hide();

        init();
        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setVrSurfaceView();
            mp_centerStart = new MyMediaTool(this, R.raw.center_start);
            mp_centerStart.startPlayer();
            handler.postDelayed(delayToSensorStart, 2000);
        }
    }

    private void init(){
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        magneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        accelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        balanceMeasure = new Intent(CenterMeasureActivity.this, BalanceMeasureActivity.class);
        list_AxisX = new ArrayList<Float>();
        list_AxisY = new ArrayList<Float>();
        list_AxisZ = new ArrayList<Float>();
        values = new float[3];
        gravity = new float[3];
        r = new float[9];
        geomagnetic = new float[3];
    }

    private void setVrSurfaceView(){
        glSurfaceView = new MyGLSurfaceView(this, MyGLSurfaceView.MY_GLRENDERER_FOR_CENTER);           // Allocate a GLSurfaceView
        renderer = glSurfaceView.getRenderer();
        setContentView(glSurfaceView);
    }

    public void startCamera(int texture) {
        surface = new SurfaceTexture(texture);
        surface.setOnFrameAvailableListener(this);
        renderer.setSurface(surface);

        startBackgroundThread();
        openCamera();
    }

    //設置相機參數
    private void setUpCamera() {
        mCameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            //获取可用摄像头列表
            for (String cameraId : mCameraManager.getCameraIdList()) {
                //获取相机的相关参数
                CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
                // 不使用前置摄像头。
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null)
                    continue;
                else
                    mPreviewSize = map.getOutputSizes(SurfaceTexture.class)[0];
//                // 检查闪光灯是否支持。
//                Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
//                mFlashSupported = available == null ? false : available;
                mCameraId = cameraId;
                Log.e("TAG", " 相机可用");
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            //不支持Camera2API
        }
    }

    //開啟相機
    private void openCamera() {
        setUpCamera();

        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            Log.e("TAG", "打开相机预览");
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                return;
            //打开相机预览
            mCameraManager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }

    }

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            // 打开相机时调用此方法。 在这里开始相机预览。
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            //创建CameraPreviewSession
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

    };

    //創建CameraCaptureSession，android Device 跟Camera Device 溝通的橋樑
    private void createCameraPreviewSession() {
        try {
            //設置緩衝區大小為相機大小
            surface.setDefaultBufferSize(mPreviewSize.getWidth(),mPreviewSize.getHeight());
            Log.e("TAG", "创建PreviewSession");
            //设置了一个具有输出Surface的CaptureRequest.Builder。
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            Log.e("TAG", "添加mSurface");
            Surface mSurface = new Surface(surface);
            mPreviewRequestBuilder.addTarget(mSurface);
            Log.e("TAG", "添加完成");
            //创建一个CameraCaptureSession来进行相机预览。
            mCameraDevice.createCaptureSession(Arrays.asList(mSurface),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // 相机已经关闭
                            if (null == mCameraDevice) {
                                Log.e("TAG", "相机已经关闭");
                                return;
                            }
                            // 会话准备好后，我们开始显示预览
                            mCaptureSession = cameraCaptureSession;
                            Log.e("TAG", "会话准备好后，我们开始显示预览");
                            try {
                                // 自动对焦应
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
//                                // 闪光灯
//                                setAutoFlash(mPreviewRequestBuilder);
                                // 最终开启相机预览并添加事件
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCaptureSession.setRepeatingRequest(mPreviewRequest,
                                        null, mBackgroundHandler);
                                Log.e("TAG", " 最终开启相机预览并添加事件");
                                // 相机開始对焦
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                            Log.e("TAG", " onConfigureFailed 开启预览失败");
                        }
                    }, null);
        } catch (CameraAccessException e) {
            Log.e("TAG", " CameraAccessException 开启预览失败2");
            e.printStackTrace();
        }
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        //告訴系統重繪
        glSurfaceView.requestRender();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (isSensorStart) {
            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                float x, y, z;
                if (timestamp != 0) {
                    x = event.values[0];
                    y = event.values[1];
                    z = event.values[2];
                    if (Math.abs(x) > 3 || Math.abs(y) > 3 || Math.abs(z) > 3) {
                        System.err.println("X：" + Math.abs(x));
                        System.err.println("Y：" + Math.abs(y));
                        System.err.println("Z：" + Math.abs(z));
                        isSensorStart = false;
                        //remove 所有在執行的任務
                        handler.removeCallbacksAndMessages(null);
                        //刪除資料夾
                        DeletData mDeletData = new DeletData();
                        mDeletData.deleteFile();
                        MediaPlayer mPlayer = MediaPlayer.create(CenterMeasureActivity.this, R.raw.fail);
                        mPlayer.start();
                        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            public void onCompletion(MediaPlayer mp) {
                                releasePlayer(mp);
                                Intent main = new Intent(CenterMeasureActivity.this, MainActivity.class);
                                startActivity(main);
                            }
                        });
                    }
                }
                timestamp = event.timestamp;
            }

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                gravity = event.values;
                getValues();
            }

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
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

    private float averageRadOfAxis(List<Float> list){
        float ave = 0;

        for(float value : list)
            ave += value;
        ave /= list.size();

        return ave;
    }

    private Runnable delayToSensorStart = new Runnable() {
        @Override
        public void run() {
            isSensorStart = true;
            mp_centerStart.releasePlayer();
            mp_ding = new MyMediaTool(CenterMeasureActivity.this, R.raw.ding);
            handler.postDelayed(updataDing, 1000);
        }
    };

    private Runnable updataDing = new Runnable() {
        @Override
        public void run() {
            index_ding++;
            mp_ding.stopPlayer();
            mp_ding.resetPlayer();
            mp_ding = new MyMediaTool(CenterMeasureActivity.this, R.raw.ding);
            mp_ding.startPlayer();

            if(index_ding<3)
                handler.postDelayed(updataDing, 1000);
            else
                handler.postDelayed(delayToDingStop, 1000);
        }
    };

    private Runnable delayToDingStop = new Runnable() {
        @Override
        public void run() {
            isSensorStart = false;
            mp_ding.releasePlayer();
            float[] center = new float[3];
            center[0] = averageRadOfAxis(list_AxisX);
            center[1] = averageRadOfAxis(list_AxisY);
            center[2] = averageRadOfAxis(list_AxisZ);
            InputData mInputData = new InputData(CenterMeasureActivity.this, "center", InputData.LAST);
            mInputData.record(center);
            handler.postDelayed(delayToBlanceStart, 500);
        }
    };

    private Runnable delayToBlanceStart = new Runnable() {
        @Override
        public void run() {
            startActivity(balanceMeasure);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        //KITKAT 版本>19
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }
        getWindow().setBackgroundDrawable(null);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//設置視窗全屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//螢幕保持橫向
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//保持螢幕常亮，需配合getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //每0.1秒針測一次
        mSensorManager.registerListener(this, magneticSensor, 10 * 1000);
        mSensorManager.registerListener(this, accelerometerSensor, 10*1000);
        mSensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mSensorManager.unregisterListener(this);
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

    //使返回鍵失效
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }
}
