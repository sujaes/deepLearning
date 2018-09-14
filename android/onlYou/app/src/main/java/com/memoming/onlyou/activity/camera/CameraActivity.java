package com.memoming.onlyou.activity.camera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
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
import com.memoming.onlyou.activity.model.ModelCameraActivity;
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

public class CameraActivity extends FragmentActivity {

    public  static Activity     mCameraActivity;
    private static Context      mContext;
    private String              mImageUploadPath    = "/home/cgvmu/project/OnlyYou/data/edit_before/";
    private String              mImageDownloadPath  = "/home/cgvmu/project/OnlyYou/data/edit_after/";
//    private String          video_uploadPath  = "/home/cgvmu/project/OnlyYou/data/training-video/"; // TODO :: 업로드 필요한경우 구현


    private static final int    REQUEST_CAMERA_PERMISSION_RESULT                   = 0;
    private static final int    REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT   = 1;
    private static final int    STATE_PREVIEW                                      = 0;
    private static final int    STATE_WAIT_LOCK                                    = 1;
    private int                 mCaptureState                                      = STATE_PREVIEW;

    private TextureView mTextureView;
    private TextureView.SurfaceTextureListener  mSurfaceTextureListener     = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            setupCamera(width, height, CameraCharacteristics.LENS_FACING_BACK);
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
    private HandlerThread   mBackgroundHandlerThread;
    private Handler         mBackgroundHandler;
    private String          mCameraId;
    private Size            mPreviewSize;
    private Size            mVideoSize;
    private Size            mImageSize;
    private ImageReader     mImageReader;
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
//                        Toast.makeText(getApplicationContext(), "AF Locked!", Toast.LENGTH_SHORT).show(); // TODO :: 캡쳐 완료되었을때 뭐 소리라던가
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
    private Button  mRotationButton;
    private Button  mAlbumButton;
    private Boolean mIsRecording    = false;
    private Boolean mIsVertical     = true;
    private int     mCurrentCameraLens = CameraCharacteristics.LENS_FACING_BACK;


    private File    mVideoFolder;
    private String  mVideoFileName;
    private File    mImageFolder;
    private String  mImageFileName;
    private String  mConvertedImageFileName;

    private ProgressDialog mProgress;

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
        setContentView(R.layout.activity_camera);

        mContext                = this;
        mCameraActivity         = CameraActivity.this;
        mChronometer            = (Chronometer) findViewById(R.id.chronometer);
        mTextureView            = (TextureView) findViewById(R.id.textureView);

        createVideoFolder();
        createImageFolder();

        mSwitchCameraButton = (Button)findViewById(R.id.btn_switch_camera);
        mSwitchCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCamera();
                if ( mCurrentCameraLens == CameraCharacteristics.LENS_FACING_BACK )
                    setupCamera(mTextureView.getWidth(), mTextureView.getHeight(), CameraCharacteristics.LENS_FACING_FRONT);
                else
                    setupCamera(mTextureView.getWidth(), mTextureView.getHeight(), CameraCharacteristics.LENS_FACING_BACK);
                connectCamera();
            }
        });

        mStillButton    = (Button)findViewById(R.id.btn_still);
        mStillButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lockFocus();
            }
        });

        mRecordButton   = (Button)findViewById(R.id.btn_record);
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( mIsRecording ) {
                    mChronometer.stop();
                    mChronometer.setVisibility(View.INVISIBLE);
                    mIsRecording = false;
                    mRecordButton.setBackgroundResource(R.drawable.recording_icon_else);
                    mMediaRecoder.stop();
                    mMediaRecoder.reset();
                    startPreview();
                }
                else {
                    mRecordButton.setBackgroundResource(R.drawable.isrecording_icon_else);
                    mMediaRecoder   = new MediaRecorder();
                    checkWriteStoragePermission();
                }
            }
        });

        mRotationButton = (Button)findViewById(R.id.btn_rotation);
        mRotationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsVertical) {
                    mRotationButton.setBackgroundResource(R.drawable.horizontal_text_icon);
                    mIsVertical = false;
                }
                else {
                    mRotationButton.setBackgroundResource(R.drawable.vertical_text_icon);
                    mIsVertical = true;
                }
            }
        });

        mAlbumButton = (Button)findViewById(R.id.btn_album);
        mAlbumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectGalley();
            }
        });



    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgourndThread();

        if(mTextureView.isAvailable()) {
            setupCamera(mTextureView.getWidth(), mTextureView.getHeight(), CameraCharacteristics.LENS_FACING_BACK);
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
                mRecordButton.setBackgroundResource(R.drawable.isrecording_icon_else);
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
                boolean rotation            = mTotalRotation == 90 || mTotalRotation == 270;
                int rotateWidth             = width/2; // TODO :: 사이즈 수정해야함
                int rotateHeight            = height/2;
//                if (rotation) {
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

                            refreshGallery(mImageFileName,mContext);

                            Handler uploadHandler = new Handler(Looper.getMainLooper()) {
                                @Override
                                public void handleMessage(Message msg) {
                                    if( msg.what == 0 ) {
                                        try {
                                            Thread image_upload_thread = new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        Server server = new Server();
                                                        server.upload(mImageFileName, mImageUploadPath);
                                                    }
                                                    catch (Exception e) { e.printStackTrace(); }
                                                }
                                            });
                                            image_upload_thread.start();
                                            image_upload_thread.join();
                                        }
                                        catch ( Exception e ) { e.printStackTrace(); }
                                    }

                                    if ( msg.what == 1 ) {
                                        if ( mIsVertical ) {
                                            try {
                                                Thread convert_thread = new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        new progress_serverTask("이미지를 변환 중입니다 ... !").execute("http://117.16.44.14:8054/post");
                                                    }
                                                });
                                                convert_thread.start();
                                                convert_thread.join();
                                            }
                                            catch (InterruptedException e) { e.printStackTrace(); }
                                        }
                                        else {
                                            if (mCurrentCameraLens == CameraCharacteristics.LENS_FACING_BACK) {
                                                try {
                                                    Thread rotate_thread = new Thread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            new progress_serverTask("이미지를 변환 중입니다 ... !").execute("http://117.16.44.14:8061/post");
                                                        }
                                                    });
                                                    rotate_thread.start();
                                                    rotate_thread.join();
                                                }
                                                catch (InterruptedException e) { e.printStackTrace(); }

                                            }
                                            else {
                                                try {
                                                    Thread rotate_thread = new Thread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            new progress_serverTask("이미지를 변환 중입니다 ... !").execute("http://117.16.44.14:8060/post");
                                                        }
                                                    });
                                                    rotate_thread.start();
                                                    rotate_thread.join();
                                                }
                                                catch (InterruptedException e) { e.printStackTrace(); }

                                            }
                                            try {
                                                Thread convert_thread = new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        new progress_serverTask("이미지를 변환 중입니다 ... !").execute("http://117.16.44.14:8054/post");
                                                    }
                                                });
                                                convert_thread.start();
                                                convert_thread.join();
                                            }
                                            catch (InterruptedException e) { e.printStackTrace(); }
                                        }

                                    }

                                    if (msg.what == 2 ) {
                                        try {
                                            Thread image_download_thread = new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        Server server = new Server();
                                                        System.out.println(mImageFolder.getAbsolutePath());
                                                        server.download(mConvertedImageFileName,mImageDownloadPath,mImageFolder.getAbsolutePath());
                                                        refreshGallery(mImageFolder.getAbsolutePath()+"/"+mConvertedImageFileName,mContext);


//                                                        AlertDialog.Builder alert = new AlertDialog.Builder(CameraActivity.this);
//                                                        alert.setTitle("onlYou");
//                                                        alert.setCancelable(false);
//                                                        alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
//                                                            @Override
//                                                            public void onClick(DialogInterface dialog, int which) {
//                                                                dialog.dismiss();
//                                                            }
//                                                        });

                                                    }
                                                    catch (Exception e) { e.printStackTrace(); }
                                                }
                                            });
                                            image_download_thread.start();
                                            image_download_thread.join();
                                        }
                                        catch ( Exception e ) { e.printStackTrace(); }
//                                        System.out.println(mConvertedImageFileName);
//                                        System.out.println(mImageDownloadPath);
//                                        System.out.println(mImageFolder.getAbsolutePath());
//                                        System.out.println(mImageFolder.getAbsolutePath()+"/"+mConvertedImageFileName);
                                    }


                                }
                            };

                            uploadHandler.sendEmptyMessage(0);
                            uploadHandler.sendEmptyMessage(1);

                            uploadHandler.sendEmptyMessage(2);




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

    private static int sensorToDeviceRotation(CameraCharacteristics cameraCharacteristics, int deviceOrientation ) {
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
        String timeStamp            = new SimpleDateFormat("yyyyMMdd_HHmmsss").format(new Date());
        String prepend              = "IMAGE_" + timeStamp + "_";
        File imageFile              = File.createTempFile(prepend,".jpg",mImageFolder);
        mImageFileName              = imageFile.getAbsolutePath();
        System.out.println("mmmmmmmmmmmmmmmmmmmm:"+mImageFileName);
        mConvertedImageFileName     = imageFile.getName();
        mConvertedImageFileName     = mConvertedImageFileName.substring(0,mConvertedImageFileName.length()-4)+"_onlyYou.jpg";
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
                mRecordButton.setBackgroundResource(R.drawable.isrecording_icon_else);
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
            mRecordButton.setBackgroundResource(R.drawable.isrecording_icon_else);
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
        mMediaRecoder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecoder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecoder.setOutputFile(mVideoFileName);
        mMediaRecoder.setVideoEncodingBitRate(10000000);
        mMediaRecoder.setAudioEncodingBitRate(32000);
        mMediaRecoder.setAudioChannels(2);
        mMediaRecoder.setVideoFrameRate(30);
        mMediaRecoder.setVideoSize(1280, 720);
        mMediaRecoder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
        mMediaRecoder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecoder.setOrientationHint(mTotalRotation);

        mMediaRecoder.setMaxDuration(3000); // TODO :: 3초 촬영

        mMediaRecoder.setOnInfoListener( new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mediaRecorder, int what, int extra) {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    mChronometer.stop();
                    mChronometer.setVisibility(View.INVISIBLE);
                    mIsRecording = false;

                    refreshGallery(mVideoFileName,mContext);

                    mRecordButton.setBackgroundResource(R.drawable.recording_icon_else);
                    mMediaRecoder.stop();
                    mMediaRecoder.reset();
                    mMediaRecoder.release();


//                    Thread takeVideo_upload_thread = new Thread(new Runnable() { // TODO :: 업로드 하긴 해야하는데 모델 비디오가 아니라 ~
//                        @Override
//                        public void run() {
//                            try {
////                                new uploadTask().execute();
//                                Server server = new Server();
//                                server.upload(mVideoFileName,video_uploadPath);
//                            }
//                            catch (Exception e) { e.printStackTrace(); }
//                        }
//                    });
//                    takeVideo_upload_thread.start();
//                    try     { takeVideo_upload_thread.join(); }
//                    catch   (InterruptedException e) { e.printStackTrace(); }
//
//
//
//                    Thread server_thread = new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            new makeModel_serverTask().execute("http://117.16.44.14:8058/post"); // TODO :: 당연히 포트 바꿔야 겠찌 ?
//                        }
//                    });
//                    server_thread.start();
//                    try {
//                        System.out.println("영상으로 부터 이미지를 추출하는 중입니다 ...");
//                        server_thread.join();
//                    }
//                    catch (InterruptedException e) { e.printStackTrace(); }


//                    new ModelMaker(mContext).exec(); // TODO :: 이자리에 모델 만드는것 대신 영상에 모델을 입히는것을 해야함

                    startPreview();
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

    private class progress_serverTask extends ServerTask {

        private Handler         mHandler;
        private ProgressDialog  mProgressDialog;
        private String          displayMsg;

        progress_serverTask(String text) {
            displayMsg = text;
            mHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    if( msg.what == 0 ) {
                        mProgressDialog = new ProgressDialog(CameraActivity.this);
                        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        mProgressDialog.setMessage(displayMsg);
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

    private void selectGalley() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, 1112);
    }
}
