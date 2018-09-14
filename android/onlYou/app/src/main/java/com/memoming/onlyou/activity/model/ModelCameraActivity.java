package com.memoming.onlyou.activity.model;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import com.memoming.onlyou.R;
import com.memoming.onlyou.activity.main.MainActivity;
import com.memoming.onlyou.serverUtil.Server;
import com.memoming.onlyou.serverUtil.ServerTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ModelCameraActivity extends FragmentActivity {

    public  static Activity mModelCameraActivity;
    private static Context  mContext;
    private String          video_uploadPath  = "/home/cgvmu/project/OnlyYou/data/training-video/";


    private static final int    REQUEST_CAMERA_PERMISSION_RESULT                   = 0;
    private static final int    REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT   = 1;
    private static final int    STATE_PREVIEW                                      = 0;
    private static final int    STATE_WAIT_LOCK                                    = 1;
    private int                 mCaptureState                                      = STATE_PREVIEW;

    private TextureView mTextureView;
    private TextureView.SurfaceTextureListener  mSurfaceTextureListener     = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            setupCamera(width, height,CameraCharacteristics.LENS_FACING_BACK);
            connectCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };
    private CameraDevice mCameraDevice;
    private CameraDevice.StateCallback  mCameraDeviceStateCallback       = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            if(mIsRecording) {
                try {
                    createVideoFileName();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                startRecord();
                mMediaRecoder.start();
                mChronometer.setBase(SystemClock.elapsedRealtime());
                mChronometer.setVisibility(View.VISIBLE);
                mChronometer.start();
            }
            else {
                startPreview();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            cameraDevice.close();
            mCameraDevice = null;
        }
    };
    private HandlerThread mBackgroundHandlerThread;
    private Handler mBackgroundHandler;
    private String          mCameraId;
    private Size mPreviewSize;
    private Size            mVideoSize;
    private Size            mImageSize;
    private ImageReader mImageReader;
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {
                    mBackgroundHandler.post(new ImageSaver(imageReader.acquireLatestImage()));
                }
            };

    private class ImageSaver implements Runnable {

        private final Image mImage;

        public ImageSaver(Image image) {
            mImage = image;
        }

        @Override
        public void run() {
            ByteBuffer byteBuffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(mImageFileName);
                fileOutputStream.write(bytes);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                mImage.close();
                if(fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }
    private MediaRecorder mMediaRecoder;
    private Chronometer mChronometer;
    private int                     mTotalRotation;
    private CameraCaptureSession mPreviewCaptureSession;
    private CameraCaptureSession.CaptureCallback mPreviewCaptureCallback = new CameraCaptureSession.CaptureCallback() {
                private void process(CaptureResult captureResult) {
                    switch (mCaptureState) {
                        case STATE_PREVIEW :
                            // Do nothing
                            break;

                        case STATE_WAIT_LOCK:
                            mCaptureState   = STATE_PREVIEW;
                            Integer afState = captureResult.get(CaptureResult.CONTROL_AF_STATE);
                            if (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED || afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
                                Toast.makeText(getApplicationContext(), "AF Locked!", Toast.LENGTH_SHORT).show();
                                startStillCaptureRequest();
                            }
                            else { // TODO :: 오토 포커싱이 먹지않아서 일단 무조껀 넣음
                                startStillCaptureRequest();
                            }
                            break;
                    }
                }
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    process(result);
                }
            };
    private CaptureRequest.Builder  mCaptureRequestBuilder;
    private List<CaptureRequest> mCaptureRequestList;

    private Button  mRecordButton;
    private Button  mStillButton;
    private Button  mSwitchCameraButton;
    private Boolean mIsRecording = false;
    private int     mCurrentCameraLens = CameraCharacteristics.LENS_FACING_BACK;

    private File mVideoFolder;
    private String  mVideoFileName;
    private File    mImageFolder;
    private String  mImageFileName;

    private static SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0,     0);
        ORIENTATIONS.append(Surface.ROTATION_90,    90);
        ORIENTATIONS.append(Surface.ROTATION_180,   180);
        ORIENTATIONS.append(Surface.ROTATION_270,   270);
    }

    private static class CompareSizeByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() /
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_camera);

        mContext                = this;
        mModelCameraActivity    = ModelCameraActivity.this;
        mChronometer            = (Chronometer) findViewById(R.id.chronometer);
        mTextureView            = (TextureView) findViewById(R.id.textureView);

        createVideoFolder();
        createImageFolder();

//        mStillButton    = (Button) findViewById(R.id.btn_capture);
//        mStillButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                lockFocus();
//            }
//        });

        mRecordButton   = (Button)findViewById(R.id.btn_record);
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( mIsRecording ) {
                    mChronometer.stop();
                    mChronometer.setVisibility(View.INVISIBLE);
                    mIsRecording = false;
                    mRecordButton.setBackgroundResource(R.drawable.recording_icon_fit);
                    mMediaRecoder.stop();
                    mMediaRecoder.reset();
                    startPreview();
                }
                else {
                    mMediaRecoder   = new MediaRecorder();
                    checkWriteStoragePermission();
                }
            }
        });

        mSwitchCameraButton = (Button)findViewById(R.id.btn_switch_camera);
        mSwitchCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCamera();
                if ( mCurrentCameraLens == CameraCharacteristics.LENS_FACING_BACK )
                    setupCamera(mTextureView.getWidth(), mTextureView.getHeight(), CameraCharacteristics.LENS_FACING_FRONT);
                else {
                    setupCamera(mTextureView.getWidth(), mTextureView.getHeight(), CameraCharacteristics.LENS_FACING_BACK);
                }
                connectCamera();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgourndThread();

        if(mTextureView.isAvailable()) {
            setupCamera(mTextureView.getWidth(), mTextureView.getHeight(),CameraCharacteristics.LENS_FACING_BACK);
            connectCamera();
        }
        else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION_RESULT){

            if (grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getApplicationContext(),
                        "Application will not run without camera services", Toast.LENGTH_SHORT).show();
            }

            if (grantResults[1] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getApplicationContext(),
                        "Application will not have audio on record", Toast.LENGTH_SHORT).show();
            }

        }



        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mIsRecording = true;
                mRecordButton.setBackgroundResource(R.drawable.isrecording_icon_fit);
                try {
                    createVideoFileName();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(),
                        "Permission successfully granted!",Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(getApplicationContext(),
                        "App needs to save video to run", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        View devorView = getWindow().getDecorView();
        if (hasFocus) {
            devorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }

    private void setupCamera(int width, int height, int cameraLens) {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        mCurrentCameraLens = cameraLens;
        try {
            for( String cameraID : cameraManager.getCameraIdList()){
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraID);
//                if(cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK){ // TODO :: 카메라 앞뒤
//                    continue;
//                }
                if(cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == cameraLens){ // TODO :: 카메라 앞뒤
                    continue;
                }
                StreamConfigurationMap map  = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                int deviceOrientation       = getWindowManager().getDefaultDisplay().getRotation();
                mTotalRotation              = sensorToDeviceRotation(cameraCharacteristics, deviceOrientation);
                boolean swapRotation        = mTotalRotation == 90 || mTotalRotation == 270;
                int rotateWidth             = width/2; // TODO :: 사이즈 수정해야함
                int rotateHeight            = height/2;
//                if (swapRotation) {
//                    rotateWidth     = height/2;
//                    rotateHeight    = width/2;
//                }

                mPreviewSize    = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),rotateWidth, rotateHeight);
                mVideoSize      = chooseOptimalSize(map.getOutputSizes(MediaRecorder.class),rotateWidth, rotateHeight);
                mImageSize      = chooseOptimalSize(map.getOutputSizes(ImageFormat.JPEG),rotateWidth, rotateHeight);
                mImageReader    = ImageReader.newInstance(mImageSize.getWidth(),mImageSize.getHeight(),ImageFormat.JPEG, 2);
                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
                mCameraId       = cameraID;
                return;
            }
        }
        catch (CameraAccessException e) { e.printStackTrace(); }
    }

    private void connectCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    cameraManager.openCamera(mCameraId,mCameraDeviceStateCallback,mBackgroundHandler);
                }
                else {
                    if(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
                        Toast.makeText(this, "OnlyYou App required access to camera", Toast.LENGTH_SHORT).show();
                    }
                    requestPermissions(new String[] {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, REQUEST_CAMERA_PERMISSION_RESULT);
                }
            }
            else {
                cameraManager.openCamera(mCameraId,mCameraDeviceStateCallback,mBackgroundHandler);
            }
        }
        catch (CameraAccessException e) { e.printStackTrace(); }
    }

    private void startRecord() {
        try {
            setupMediaRecoder();
            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface previewSurface  = new Surface(surfaceTexture);
            Surface recordSurface   = mMediaRecoder.getSurface();
            mCaptureRequestBuilder  = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            mCaptureRequestBuilder.addTarget(previewSurface);
            mCaptureRequestBuilder.addTarget(recordSurface);
            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, recordSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            try {
                                cameraCaptureSession.setRepeatingRequest(
                                        mCaptureRequestBuilder.build(), null, null
                                );
                            }
                            catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }
                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                        }
                    }, null);

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startPreview() {
        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
        //TODO :: Size 현재 /2 해놓았으나 추후 재수정 요망
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth()/2, mPreviewSize.getHeight()/2);
        Surface previewSurface = new Surface(surfaceTexture);

        try {
            mCaptureRequestBuilder  = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(previewSurface);

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            mPreviewCaptureSession = cameraCaptureSession;
                            try {
                                mPreviewCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(),
                                        null, mBackgroundHandler);
                            }
                            catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                            Toast.makeText(getApplicationContext(),
                                    "Unable to setup camera preview",Toast.LENGTH_SHORT).show();

                        }
                    }, null);
        }
        catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startStillCaptureRequest() {
        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            mCaptureRequestBuilder.addTarget(mImageReader.getSurface());
            mCaptureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, mTotalRotation);


            CameraCaptureSession.CaptureCallback stillCaptureCallback =
                    new CameraCaptureSession.CaptureCallback() {
                        @Override
                        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                            super.onCaptureStarted(session, request, timestamp, frameNumber);
                            try {
                                createImageFileName();
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                            super.onCaptureCompleted(session, request, result);
                            // TODO :: SERVER Upload 구현
                        }



                    };

            mPreviewCaptureSession.capture(mCaptureRequestBuilder.build(), stillCaptureCallback, null);
        }
        catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if(mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    private void startBackgourndThread() {
        mBackgroundHandlerThread = new HandlerThread("CameraHanlerThread");
        mBackgroundHandlerThread.start();
        mBackgroundHandler = new Handler(mBackgroundHandlerThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundHandlerThread.quitSafely();
        try {
            mBackgroundHandlerThread.join();
            mBackgroundHandlerThread = null;
            mBackgroundHandler = null;
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static int sensorToDeviceRotation( CameraCharacteristics cameraCharacteristics, int deviceOrientation ) {
        int sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        deviceOrientation = ORIENTATIONS.get(deviceOrientation);
        return (sensorOrientation + deviceOrientation + 360) % 360 ;
    }

    private static Size chooseOptimalSize(Size[] choices, int width, int height) {
        List<Size> bigEnough = new ArrayList<>();
        for(Size option : choices ) {
            if (option.getHeight() == option.getWidth() * height / width &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }

        if(bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizeByArea());
        }
        else {
            return choices[0];
        }
    }

    private void createImageFolder() {
        File imageFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        mImageFolder = new File(imageFile, "OnlyYou_IMG");
        if (!mImageFolder.exists()) {
            mImageFolder.mkdirs();
        }
    }

    private File createImageFileName() throws IOException {
        String timeStamp    = new SimpleDateFormat("yyyyMMdd_HHmmsss").format(new Date());
        String prepend      = "IMAGE_" + timeStamp + "_";
        File imageFile      = File.createTempFile(prepend,".jpg",mImageFolder);
        mImageFileName      = imageFile.getAbsolutePath();
        return imageFile;
    }

    private void createVideoFolder() {
        File movieFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
//        File movieFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM+"/Camera");

        mVideoFolder = new File(movieFile, "OnlyYou_VIDEO");
        if (!mVideoFolder.exists()) {
            mVideoFolder.mkdirs();
        }
    }

    private void createVideoFileName() throws IOException {
        String timeStamp    = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String prepend      = "VIDEO_" + timeStamp + "_";
        File videoFile      = File.createTempFile(prepend,".mp4",mVideoFolder);
        mVideoFileName      = videoFile.getAbsolutePath();
    }

    private void checkWriteStoragePermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                mIsRecording = true;
                mRecordButton.setBackgroundResource(R.drawable.isrecording_icon_fit);
                try {
                    createVideoFileName();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                startRecord();
                mMediaRecoder.start();
                mChronometer.setBase(SystemClock.elapsedRealtime());
                mChronometer.setVisibility(View.VISIBLE);
                mChronometer.start();
            }
            else {
                if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this,"App needs to be able to save videos", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT);
            }
        }
        else {
            mIsRecording = true;
            mRecordButton.setBackgroundResource(R.drawable.isrecording_icon_fit);
            try {
                createVideoFileName();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            startRecord();
            mMediaRecoder.start();
        }
    }

    private void setupMediaRecoder() throws IOException {
        mMediaRecoder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
//        mMediaRecoder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecoder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecoder.setOutputFile(mVideoFileName);

        mMediaRecoder.setVideoEncodingBitRate(10000000);
//        mMediaRecoder.setAudioEncodingBitRate(32000);
//        mMediaRecoder.setAudioChannels(2);
        mMediaRecoder.setVideoFrameRate(30);
        //        mMediaRecoder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecoder.setVideoSize(1280, 720);
        mMediaRecoder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
//        mMediaRecoder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecoder.setOrientationHint(mTotalRotation);

        mMediaRecoder.setMaxDuration(30000);

        mMediaRecoder.setOnInfoListener( new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mediaRecorder, int what, int extra) {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    mChronometer.stop();
                    mChronometer.setVisibility(View.INVISIBLE);
                    mIsRecording = false;

                    refreshGallery(mVideoFileName,mContext);

                    mRecordButton.setBackgroundResource(R.drawable.recording_icon_fit);
                    mMediaRecoder.stop();
                    mMediaRecoder.reset();
                    mMediaRecoder.release();


                    Thread takeVideo_upload_thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
//                                new uploadTask().execute();
                                Server server = new Server();
                                server.upload(mVideoFileName,video_uploadPath);
                            }
                            catch (Exception e) { e.printStackTrace(); }
                        }
                    });
                    takeVideo_upload_thread.start();
                    try     { takeVideo_upload_thread.join(); }
                    catch   (InterruptedException e) { e.printStackTrace(); }

                    // TODO :: front 일경우 회전을 다르게 해서 변환한후 training+image로 옮기는것 구현


                    if ( mCurrentCameraLens == CameraCharacteristics.LENS_FACING_FRONT ) {
                        Thread server_thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                new makeModel_serverTask().execute("http://117.16.44.14:8059/post");
                            }
                        });
                        server_thread.start();
                        try {
                            System.out.println("영상으로 부터 이미지를 추출하는 중입니다 ...");
                            server_thread.join();
                        }
                        catch (InterruptedException e) { e.printStackTrace(); }

                    }

                    else {
                        Thread server_thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                new makeModel_serverTask().execute("http://117.16.44.14:8058/post");
                            }
                        });
                        server_thread.start();
                        try {
                            System.out.println("영상으로 부터 이미지를 추출하는 중입니다 ...");
                            server_thread.join();
                        }
                        catch (InterruptedException e) { e.printStackTrace(); }
                    }

                    new ModelMaker(mContext).exec();

//                    startPreview();
                }
            }
        });
        mMediaRecoder.prepare();
    }

    private void lockFocus() {
        mCaptureState = STATE_WAIT_LOCK;
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
        try {
            mPreviewCaptureSession.capture(mCaptureRequestBuilder.build(), mPreviewCaptureCallback, mBackgroundHandler);
        }
        catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private static void refreshGallery(String mCurrentPhotoPath, Context context) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    private class makeModel_serverTask extends ServerTask {

        private Handler mHandler;
        private ProgressDialog mProgressDialog;

        makeModel_serverTask() {
            mHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    if( msg.what == 0 ) {
                        mProgressDialog = new ProgressDialog(ModelCameraActivity.this);
                        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        mProgressDialog.setMessage("영상으로 부터 얼굴 이미지를 추출 중 입니다. ...\n");
                        mProgressDialog.setCancelable(false);
                        mProgressDialog.show();
                        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                    }
                }
            };
        }

        @Override
        protected void onPreExecute() {
            mHandler.sendEmptyMessage(0);
        }

        @Override
        protected void onPostExecute(String result) {
            mProgressDialog.dismiss();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            //initDialog();
        }


    }



}
