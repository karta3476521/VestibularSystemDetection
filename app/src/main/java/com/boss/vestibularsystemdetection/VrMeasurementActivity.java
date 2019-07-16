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

public class VrMeasurementActivity extends AppCompatActivity implements SurfaceTexture.OnFrameAvailableListener, SensorEventListener {
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
    private double distance = 0.0;
    private MyMediaTool mp_targetObject, mp_putInGlasses, mp_centerStart, mp_doCorrect;
    private SensorManager mSensorManager;
    private Sensor magneticSensor, accelerometerSensor;
    private float[] values, r, gravity, geomagnetic;
    private boolean isGetValue = false;
    private Handler handler = new Handler();
    private double roll_temp = 5.0, roll = 0.0;
    private List<Boolean> timesOfCheck;
    private double targetHeight;
    private Size mPreviewSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_vr_measurement);
        getSupportActionBar().hide();
        //KITKAT 版本>19
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }

        init();
        //判斷手機方向
        //ORIENTATION_LANDSCAPE 橫向, ORIENTATION_PORTRAIT 直向
        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            setDialog();
            setVrSurfaceView();
            mp_putInGlasses = new MyMediaTool(VrMeasurementActivity.this, R.raw.put_in_glasses);
            mp_putInGlasses.startPlayer();
            handler.postDelayed(delayToTarget, 13000);
        }
    }

    private void init(){
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        magneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        timesOfCheck = new ArrayList<Boolean>();
        values = new float[3];
        gravity = new float[3];
        r = new float[9];
        geomagnetic = new float[3];
        Intent intent = getIntent();
        int mode = intent.getIntExtra("Mode", -1);
        if(mode == 0){
//            ReadData rd = new ReadData();
//            targetHeight = rd.getTargetHeight("childHeight") == -1.0 ? 0.0 : rd.getTargetHeight("childHeight");
            targetHeight = 60;
            distance = 120;
        }else if(mode == 1){
//            ReadData rd = new ReadData();
//            targetHeight = rd.getTargetHeight("adultHeight") == -1.0 ? 120.0 : rd.getTargetHeight("adultHeight");
            targetHeight = 0;
            distance = 60;
        }else {
            System.err.println("沒有收到mode");
        }
    }

    private void setVrSurfaceView(){
        glSurfaceView = new MyGLSurfaceView(this, MyGLSurfaceView.MY_GLRENDERER_FOR_VR);           // Allocate a GLSurfaceView
        renderer = glSurfaceView.getRenderer();
        setContentView(glSurfaceView);                // This activity sets to GLSurfaceView
    }

    public void startCamera(int texture) {
        surface = new SurfaceTexture(texture);
        surface.setOnFrameAvailableListener(this);
        renderer.setSurface(surface);

        startBackgroundThread();
        openCamera();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values;
        }
    }

    private boolean checkValue(){
        SensorManager.getRotationMatrix(r, null, gravity, geomagnetic);
        SensorManager.getOrientation(r, values);
        roll_temp = roll;
        roll = Math.toDegrees(values[2]);
        if(Math.abs(roll - roll_temp) <= 2)
            return true;
        else
            return false;
    }

    private void getValue(){
        SensorManager.getRotationMatrix(r, null, gravity, geomagnetic);
        SensorManager.getOrientation(r, values);

        mp_doCorrect = new MyMediaTool(this, R.raw.do_correct);
        if(Math.abs(Math.toDegrees(values[2])) > 85){
            mp_doCorrect.startPlayer();
            timesOfCheck.clear();
            handler.postDelayed(updataToCheck, 2000);
        }else {
            float rad = (float) (Math.toRadians(90.0) - Math.abs(values[2]));
            angleToDistance(rad);
        }
    }

    private void angleToDistance(float rad){
        float height = (float) Math.tan(rad) * (float) distance + (float)targetHeight;
        InputData mInputData = new InputData(this, "height", InputData.NEW);
        mInputData.record(height);
        mp_centerStart = new MyMediaTool(VrMeasurementActivity.this, R.raw.center_for_vr);
        mp_centerStart.startPlayer();
        handler.postDelayed(delayToIntent, 11000);
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
                Log.e("TAG", " 相机可用 ");
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

    private Runnable delayToTarget = new Runnable() {
        @Override
        public void run() {
            mp_targetObject = new MyMediaTool(VrMeasurementActivity.this, R.raw.target_object);
            mp_targetObject.startPlayer();
            handler.postDelayed(updataToCheck, 5000);
        }
    };

    private Runnable updataToCheck = new Runnable() {
        @Override
        public void run() {
            timesOfCheck.add(checkValue());
            boolean isContinue = true;
            int times = 0;
            for(boolean check : timesOfCheck){
                if(check == true)
                    times++;
                else
                    times = 0;

                if(times >= 4)
                    isContinue = false;
            }

            if(isContinue)
                handler.postDelayed(updataToCheck, 1000);
            else
                getValue();
        }
    };

    private Runnable delayToIntent = new Runnable() {
        @Override
        public void run() {
            releaseMp();
            Intent intent = new Intent(VrMeasurementActivity.this, CenterMeasureActivity.class);
            startActivity(intent);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//螢幕保持橫向
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//設置視窗全屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//保持螢幕常亮，需配合getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mSensorManager.registerListener(this, accelerometerSensor, 100 * 1000);
        mSensorManager.registerListener(this, magneticSensor, 100 * 1000);
    }

    private void releaseMp(){
        mp_targetObject.releasePlayer();
        mp_putInGlasses.releasePlayer();
        mp_centerStart.releasePlayer();
        mp_doCorrect.releasePlayer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

//    private void setDialog() {
//        mp_inputDistance = new MyMediaTool(this, R.raw.input_distance);
//        mp_inputDistance.startPlayer();
//
//        // get dialog.xml view
//        LayoutInflater mlayoutInflater = LayoutInflater.from(this);
//        View dialogView = mlayoutInflater.inflate(R.layout.dialog, null);
//
//        final EditText userInput = (EditText) dialogView.findViewById(R.id.edtDialogInput);
//
//        final AlertDialog mAlertDialog = new AlertDialog.Builder(this)
//                .setView(dialogView)
//                .setCancelable(false)
//                .setPositiveButton("完成", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        if (userInput.getText().toString().matches("")) {
//                            mp_inputDistance.stopPlayer();
//                            mp_inputDistance.resetPlayer();
//                            dialog.cancel();
//                            setDialog();
//                        }else {
//                            mp_inputDistance.stopPlayer();
//                            distance = Double.parseDouble(userInput.getText().toString());
//                            mp_putInGlasses = new MyMediaTool(VrMeasurementActivity.this, R.raw.put_in_glasses);
//                            mp_putInGlasses.startPlayer();
//                            handler.postDelayed(delayToTarget, 13000);
//                            VrMeasurementActivity.this.setContentView(glSurfaceView);                // This activity sets to GLSurfaceView
//                        }
//                    }
//                })
//                .create();
//
//        mAlertDialog.show();
//    }