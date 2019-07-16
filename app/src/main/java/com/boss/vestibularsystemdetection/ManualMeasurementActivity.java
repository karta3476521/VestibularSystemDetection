package com.boss.vestibularsystemdetection;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class ManualMeasurementActivity extends AppCompatActivity implements SensorEventListener {
    private CameraManager mCameraManager;
    private String mCameraId;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CameraCaptureSession mCaptureSession;
    private CaptureRequest mPreviewRequest;
    private TextureView textureView;
    private Size mPreviewSize;
    private MyMediaTool mp_checkHeight, mp_centerStart;
    private double distance = 0.0, height = 0.0;
    private SensorManager mSensorManager;
    private Sensor magneticSensor, accelerometerSensor;
    private float[] values, r, gravity, geomagnetic;
    private Handler handler;
    private double targetHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_measurement);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        init();
//        setDialog_inputDistance();
    }

    private void init(){
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        magneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        handler = new Handler();
        textureView = (TextureView) findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(mTextureListener);
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
            distance = 120.0;
        }else if(mode == 1){
//            ReadData rd = new ReadData();
//            targetHeight = rd.getTargetHeight("adultHeight") == -1.0 ? 120.0 : rd.getTargetHeight("adultHeight");
            targetHeight = 0;
            distance = 60.0;
        }else {
            System.err.println("沒有收到mode");
        }
    }

    private void setDialog_checkHeight(){
        mp_checkHeight = new MyMediaTool(this, R.raw.check_height);
        mp_checkHeight.startPlayer();
        // get dialog.xml view
        LayoutInflater mlayoutInflater = LayoutInflater.from(this);
        View dialogView = mlayoutInflater.inflate(R.layout.dialog_check, null);

        TextView mTextView = (TextView)dialogView.findViewById(R.id.textView2);
        DecimalFormat nf = new DecimalFormat("0.00");
        mTextView.setText("高度為：" + nf.format(height) + "cm");

        final AlertDialog mAlertDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .setPositiveButton("完成", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        mp_checkHeight.stopPlayer();
                        InputData mInputData = new InputData(ManualMeasurementActivity.this, "height", InputData.NEW);
                        mInputData.record((float) height);
                        handler.postDelayed(delayToCenter, 16000);
                        mp_centerStart.startPlayer();
                        dialog.cancel();
                    }
                })
                .setNegativeButton("重新測量", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mp_checkHeight.stopPlayer();
                        mp_checkHeight.resetPlayer();
//                        distance = 0.0;
//                        setDialog_inputDistance();
                        dialog.cancel();
                    }
                })
                .create();

        mAlertDialog.show();
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

    public void getValue(){
        SensorManager.getRotationMatrix(r, null, gravity, geomagnetic);
        SensorManager.getOrientation(r, values);

        float rad = (float) (Math.toRadians(90.0) - Math.abs(values[2]));
        angleToDistance(rad);
    }

    private void angleToDistance(float rad){
        height = Math.tan(rad) * distance + (float)targetHeight;
        setDialog_checkHeight();
    }

    TextureView.SurfaceTextureListener mTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //当SurefaceTexture可用的时候，设置相机参数并打开相机
            setUpCamera(width, height);
            openCamera();

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    //設置相機參數
    private void setUpCamera(int width, int height) {
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
                if (map == null) {
                    continue;
                }
//                // 检查闪光灯是否支持。
//                Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
//                mFlashSupported = available == null ? false : available;
                //根据TextureView的尺寸设置预览尺寸
                mPreviewSize = getOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height);
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

        Log.e("TAG", "添加mSurface");
        SurfaceTexture mSurfaceTexture = textureView.getSurfaceTexture();
        //设置TextureView的缓冲区大小
        mSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        //获取Surface显示预览数据
        Surface mSurface = new Surface(mSurfaceTexture);
        try {
            Log.e("TAG", "创建PreviewSession");
            //设置了一个具有输出Surface的CaptureRequest.Builder。
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
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

    //选择sizeMap中大于并且最接近width和height的size
    private Size getOptimalSize(Size[] sizeMap, int width, int height) {
        List<Size> sizeList = new ArrayList<>();
        for (Size option : sizeMap) {
            if (width > height) {
                if (option.getWidth() > width && option.getHeight() > height) {
                    sizeList.add(option);
                }
            } else {
                if (option.getWidth() > height && option.getHeight() > width) {
                    sizeList.add(option);
                }
            }
        }
        if (sizeList.size() > 0) {
            return Collections.min(sizeList, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getWidth() * rhs.getHeight());
                }
            });
        }
        return sizeMap[0];
    }

    private Runnable delayToCenter = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(ManualMeasurementActivity.this, CenterMeasureActivity.class);
            startActivity(intent);
        }
    };

    public void btOnClick(View v){
        if(height == 0.0)
            getValue();
//        if(distance != 0.0) {
//            mp_straightLine.stopPlayer();
//            getValue();
//        }else
//            setDialog_inputDistance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, accelerometerSensor, 100 * 1000);
        mSensorManager.registerListener(this, magneticSensor, 100 * 1000);
        startBackgroundThread();
        if (!textureView.isAvailable()) {
            textureView.setSurfaceTextureListener(mTextureListener);
        } else {
            createCameraPreviewSession();
        }

//        mp_straightLine = new MyMediaTool(this, R.raw.straight_line);
        mp_centerStart = new MyMediaTool(this, R.raw.center_for_manual);
    }

    private void releaseMp(){
//        mp_measureStart.releasePlayer();
//        mp_straightLine.releasePlayer();
        mp_checkHeight.releasePlayer();
        mp_centerStart.releasePlayer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        releaseMp();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

//    private void setDialog_inputDistance(){
//        mp_measureStart = new MyMediaTool(this, R.raw.measure_start);
//        mp_measureStart.startPlayer();
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
//                    public void onClick(DialogInterface dialog,int id) {
//                        if(userInput.getText().toString().matches("")) {
//                            mp_measureStart.stopPlayer();
//                            mp_measureStart.resetPlayer();
//                            dialog.cancel();
//                        }else {
//                            mp_measureStart.stopPlayer();
//                            distance = Double.parseDouble(userInput.getText().toString());
//                            mp_straightLine.startPlayer();
//                        }
//                    }
//                })
//                .create();
//
//        mAlertDialog.show();
//    }
