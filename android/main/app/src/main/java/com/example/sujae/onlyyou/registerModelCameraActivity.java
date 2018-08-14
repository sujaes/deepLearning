package com.example.sujae.onlyyou;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ExifInterface;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class registerModelCameraActivity extends AppCompatActivity {

    private static final String TAG     = "AndroidCameraApi";
    public int                  number  = 1;

    JSch        jsch                = null;
    Session     session             = null;
    Channel     channel             = null;
    ChannelSftp sftpChannel         = null;
    String      host                = "117.16.44.14";
    Integer     port                = 8022;
    String      user                = "cgvmu";
    String      password            = "g411";
    String      model_uploadPath    = "/home/cgvmu/project/OnlyYou/data/training-images/user";
    String      localPath           = "/storage/emulated/0/";
    String      imgcode             = null;


    private Button takePictureButton    = null; //촬영버튼
    private Button btn_accUp            = null;
    private Button btn_makeModel        = null;


    //동영상촬영용 코드












    //텍스쳐뷰(카메라 화면자리)
    private TextureView textureView     = null;
    //카메라 센서 방향 및 기기 회전을 보정하기 위해 이미지를 회전해야 하는 각도를 확인
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    public static final String          CAMERA_FRONT                = "1"; //전면카메라
    public static final String          CAMERA_BACK                 = "0"; //후면카메라
    private static final int            REQUEST_CAMERA_PERMISSION   = 200;
    private boolean                     mFlashSupported;

    private     String                  cameraId                = CAMERA_FRONT; //기본 후면
    protected   CameraDevice            cameraDevice            = null;         //카메라 제어
    protected   CameraCaptureSession    cameraCaptureSessions   = null;         //이미지를 취득,처리하는 Session
    protected   CaptureRequest          captureRequest          = null;         //이미지 취득 Request
    protected   CaptureRequest.Builder  captureRequestBuilder   = null;
    private     Size                    imageDimension          = null;
    private     ImageReader             imageReader             = null;
    private     File                    file                    = null;
    private     Handler                 mBackgroundHandler      = null;
    private     HandlerThread           mBackgroundThread       = null;


    int k;
    //쉐어드프리퍼런스 값저장
    private void saveScore(int a) {
        SharedPreferences pref = getSharedPreferences("flag", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("score", a);
        editor.commit();
    }
    //쉐어드프리퍼런스 값로드
    private int loadScore() {
        SharedPreferences pref = getSharedPreferences("flag", Activity.MODE_PRIVATE);
        int score = pref.getInt("score", 100);
        Toast.makeText(registerModelCameraActivity.this, "score : " + score, Toast.LENGTH_LONG).show();
        return score;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_register_model_camera);







                btn_makeModel       = (Button)findViewById(R.id.btn_makeModel);

        //카메라 프리뷰 설정
        textureView         = (TextureView) findViewById(R.id.texture);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);
        takePictureButton   = (Button) findViewById(R.id.btn_takepicture);
        assert takePictureButton != null;


        btn_makeModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { makeFaceModel(); }
        });

        //촬영버튼
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { takeselfie(); }
        });

        //사진골라서 정확도 높이는 페이지로 넘어가기
        findViewById(R.id.btn_accUp).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(intent, 0x00);
                    }
                });
        Activity activity = this;
        verifyStoragePermissions(activity);
    } // end of onCreate

    public static void verifyStoragePermissions(Activity activity) {
        int permission  = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(  activity,
                                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                1
            ); // TODO 여기코드 수정하기
        }
    }

    //카메라 다시오픈하기
//    private void reopenCamera() {
//        if (textureView.isAvailable())  openCamera();
//        else                            textureView.setSurfaceTextureListener(textureListener);
//    }

    //텍스쳐 뷰 인터페이스 사용
    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //open your camera here
            openCamera();
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // Transform you image captured size according to the surface width and height
        }
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    //카메라가 켜지면 카메라상태 콜백함수 실행
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is open
            Log.e(TAG, "onOpened");
            cameraDevice = camera;
            createCameraPreview();
        }
        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }
        @Override
        public void onError(CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };
    final CameraCaptureSession.CaptureCallback captureCallbackListener = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Toast.makeText(registerModelCameraActivity.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
            createCameraPreview();
        }
    };
    //카메라 스레드 시작
    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }
    //카메라 스레드 중단
    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        }
        catch (InterruptedException e) { e.printStackTrace(); }
    }

    //동영상 촬영시작
    protected void recording(){

    }

    //동영상 촬영중지


    protected void takeselfie() {
        if(null == cameraDevice) {
            Log.e(TAG, "cameraDevice is null");
            return;
        }
        //카메정보 추출을 위한 cameraManager사용
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            //사이즈 배열 선언
            Size[] jpegSizes = null;
            //카메라 정보 얻어진후
            if (characteristics != null) {
                //사진 사이즈 저장
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                                    .getOutputSizes(ImageFormat.JPEG);
            }
            int width   = 640;
            int height  = 480;
            if (jpegSizes != null && 0 < jpegSizes.length) {
                width   = jpegSizes[0].getWidth();
                height  = jpegSizes[0].getHeight();
            }

            //이미지 리더로 사진크기 저장
            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            //리스트 선언
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            //리더로 읽은 정보 리스트에 추가
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
            // 정지 영상 촬영을 시작한다
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            // 센서값으로 사진방향조절
            int sensorOrientation   =  characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            int rotation            = getWindowManager().getDefaultDisplay().getRotation();
            int jpegOrientation     = (ORIENTATIONS.get(rotation)+sensorOrientation + 270) % 360;

            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, jpegOrientation);

            //타임스템프로 사진이름 붙이기
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            final String mImageCaptureName = timeStamp + ".jpg";

            final File file = new File("/storage/emulated/0/"+ mImageCaptureName);
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                        try                 { upload("/storage/emulated/0/"+ mImageCaptureName,model_uploadPath); }
                        catch(Exception e)  { e.printStackTrace(); }


                    }
                    catch (FileNotFoundException e) { e.printStackTrace(); }
                    catch (IOException e)           { e.printStackTrace(); }
                    finally {
                        if (image != null) image.close();
                    }
                }

                //바이트형태로 저장하기
                private void save(byte[] bytes) throws IOException {
                    OutputStream output = null;
                    try {
                        output = new FileOutputStream(file);
                        output.write(bytes);
                    }
                    finally {
                        if (null != output) output.close();
                    }
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                }
            };

            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);

                    Toast.makeText (    registerModelCameraActivity.this,
                                        number+"번째 사진이 저장되었습니다.",
                                        Toast.LENGTH_SHORT      ).show();

//                    if(number<=1){
//                        Toast.makeText (    registerModelCameraActivity.this,
//                                            number+"번째 정면 사진이 저장되었습니다.",
//                                            Toast.LENGTH_SHORT ).show();
//                    }
//                    else if (1<number&&number<=2) {
//                        Toast.makeText(     registerModelCameraActivity.this,
//                                            number +"번째 우측면 사진이 저장되었습니다.",
//                                            Toast.LENGTH_SHORT ).show();
//                    }
//                    else if (2<number&&number<=3) {
//                        Toast.makeText(     registerModelCameraActivity.this,
//                                            number +"번째 좌측면 사진이 저장되었습니다.",
//                                            Toast.LENGTH_SHORT ).show();
//                    }
                    number++;
                    createCameraPreview();
                }
            };

            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try                             { session.capture(captureBuilder.build(), captureListener, mBackgroundHandler); }
                    catch (CameraAccessException e) { e.printStackTrace(); }
                }

                @Override
                public void onConfigureFailed( CameraCaptureSession session) { }

                }, mBackgroundHandler);
            }
            catch (CameraAccessException e) { e.printStackTrace(); }
    }

    protected void makeFaceModel() {
        Thread server_thread = new Thread(new Runnable() {
            @Override
            public void run() { new JSONTask().execute("http://117.16.44.14:3000/post"); }
        });
        server_thread.start();
        try {
            System.out.println("얼굴 모델을 만드는 중입니다.");
//            Toast.makeText (    registerModelCameraActivity.this,
//                                "얼굴 모델을 만드는 중입니다. \n 잠시만 기다려주세요.",
//                                Toast.LENGTH_LONG      ).show();
            server_thread.join();
        }
        catch (InterruptedException e) { e.printStackTrace(); }

        Intent progressingIntent  = new Intent(getApplicationContext(), progressingActivity.class);
        progressingIntent.putExtra("message", "얼굴 모델을 만드는 중입니다. \n 잠시만 기다려주세요.");
        startActivity(progressingIntent);
    }

    protected void checkFaceModel() {
        Thread server_thread = new Thread(new Runnable() {
            @Override
            public void run() { new JSONTask().execute("http://117.16.44.14:3000/post"); }
        });
        server_thread.start();
        try {
            System.out.println("얼굴 모델을 만드는 중입니다.");
            Toast.makeText (    registerModelCameraActivity.this,
                    "얼굴 모델을 만드는 중입니다. \n 잠시만 기다려주세요.",
                    Toast.LENGTH_LONG      ).show();
            server_thread.join();
        }
        catch (InterruptedException e) { e.printStackTrace(); }
    }

    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback(){
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(registerModelCameraActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        }
        catch (CameraAccessException e) { e.printStackTrace(); }
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "is camera open");
        try {
            //cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(registerModelCameraActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        }
        catch (CameraAccessException e) { e.printStackTrace(); }
    }

    protected void updatePreview() {

        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        }
        catch (CameraAccessException e) { e.printStackTrace(); }
    }

    //카메라 닫기
    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(registerModelCameraActivity.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    public void connect() throws JSchException, SftpException {
        System.out.println("connecting..."+host);
        //  JSch 객체를 생성한다.
        jsch = new JSch();
        // 세션 객체를 생성한다(사용자 이름, 접속할 호스트, 포트를 인자로 전달한다.)
        session = jsch.getSession(user,host,port);
        //  세션과 관련된 정보를 설정한다.
        session.setConfig("StrictHostKeyChecking", "no");
        //패스워드를 설정한다.
        session.setPassword(password);
        //  접속한다.
        session.connect();

        //  sftp 채널을 연다.
        channel = session.openChannel("sftp");
        //  채널에 연결한다.
        channel.connect();
        // 채널을 FTP용 채널 객체로 캐스팅한다.
        sftpChannel = (ChannelSftp) channel;
    }

    public void disconnect() {
        if(session.isConnected()){
            System.out.println("disconnecting...");
            sftpChannel.disconnect();
            channel.disconnect();
            session.disconnect();
        }
    }

    // 파일업로드
    public void upload(String fileName, String remoteDir) throws Exception {
        FileInputStream fis = null;
        // 앞서 만든 접속 메서드를 사용해 접속한다.
        connect();
        try {
            // 경로를 설정한다.
            sftpChannel.cd(remoteDir);
            File file = new File(fileName);
            // 입력 파일을 가져온다.
            fis = new FileInputStream(file);
            // 파일을 업로드한다.
            sftpChannel.put(fis, file.getName());
            System.out.println("File name : "+ file.getName());
            fis.close();
            System.out.println("File uploaded successfully - " + file.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
        //결과처리
        if (sftpChannel.getExitStatus() == -1) {
            System.out.println("file uploaded");
            Log.v("upload result", "succeeded");
        } else {
            Log.v("upload faild ", "faild");
        }

        disconnect();
    }

    public void initialize(String remoteDir) throws Exception{
        connect();
        try {
         //   sftpChannel.rmdir(remoteDir);
         //   sftpChannel.mkdir(remoteDir);
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("initialize complete");
        disconnect();
    }

    //정확도높이는 gallary에서 선택한 사진 업로드
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Uri를 가지고 이미지읽기
        if (requestCode == 0x00 && resultCode == RESULT_OK) {
            try {
                Uri selectedImgUri = data.getData();
                InputStream in = getContentResolver().openInputStream(selectedImgUri);
                imgcode = getPath(selectedImgUri);
                if (imgcode == null) {
                    imgcode = getRealPathFromURI(selectedImgUri);
                }
                System.out.println("selectedImgUri값: " + selectedImgUri);
                System.out.println("imgcode값: " + imgcode);

                //이미지뷰
                ImageView ivPreview = (ImageView) findViewById(R.id.imageView2);

                ExifInterface exif = null;
                try {
                    exif = new ExifInterface(imgcode);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                int exifDegree = exifOrientationToDegrees(exifOrientation);
                //경로를 통해 비트맵으로 전환
                Bitmap bitmap = BitmapFactory.decodeFile(imgcode);
                //이미지 뷰에 비트맵 넣기
                ivPreview.setImageBitmap(rotate(bitmap, exifDegree));


//                uploading(selectedImgUri);
//                String a= up.ftpGetCurrentWorkingDirectory();
//                Log.d(TAG,a);
//                up.ftpUpload("//media/external/images/media/","test.jpg","//project/OnlyYou/data");

            } catch (Exception e) {
            }

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        upload(imgcode, model_uploadPath);
//                        download("123.jpg",localPath);
//                        download(remotePathUp,"123.jpg",remotePathUp+"/123.jpg");

                    } catch (Exception ex) {
                        System.out.println(ex);
                    }
                }
            });
            thread.start();
        }
    }

    private Bitmap rotate(Bitmap src, float degree) {

// Matrix 객체 생성
        Matrix matrix = new Matrix();
// 회전 각도 셋팅
        matrix.postRotate(degree);
// 이미지와 Matrix 를 셋팅해서 Bitmap 객체 생성
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(),
                src.getHeight(), matrix, true);

    }

    private int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;

    }

    //***************버전에 따라서 getpath와 get realpath로 나뉨*********************

    public String getPath(Uri uri) {
        // uri가 null일경우 null반환
        if (uri == null) {
            return null;
        }
        // 미디어스토어에서 유저가 선택한 사진의 URI를 받아온다.
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        // URI경로를 반환한다.
        return uri.getPath();
    }

    private String getRealPathFromURI(Uri contentUri) {
        if (contentUri.getPath().startsWith("/storage")) {
            return contentUri.getPath();
        }
        String id = DocumentsContract.getDocumentId(contentUri).split(":")[1];
        String[] columns = {MediaStore.Files.FileColumns.DATA};
        String selection = MediaStore.Files.FileColumns._ID + " = " + id;
        Cursor cursor = getContentResolver().query(MediaStore.Files.getContentUri("external"), columns, selection, null, null);
        try {
            int columnIndex = cursor.getColumnIndex(columns[0]);
            if (cursor.moveToFirst()) {
                return cursor.getString(columnIndex);
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    public class JSONTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                HttpURLConnection con     = null;
                BufferedReader reader  = null;

                try {
                    URL url = new URL(urls[0]);
                    con     = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Cache-Control", "no-cache");
                    con.setRequestProperty("Content-Type", "application/json");
                    con.setRequestProperty("Accept", "text/html");
                    con.setDoOutput(true);
                    con.setDoInput(true);
                    con.connect();

                    //서버로 보내기위해서 스트림 만듬
                    OutputStream outStream  = con.getOutputStream();
                    BufferedWriter writer   = new BufferedWriter(new OutputStreamWriter(outStream));
                    writer.write("push server");
                    writer.flush();
                    writer.close();

                    //서버로 부터 데이터를 받음
                    InputStream stream  = con.getInputStream();
                    reader              = new BufferedReader(new InputStreamReader(stream));
                    StringBuffer buffer = new StringBuffer();
                    String line         = "";
                    while((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }
                    return buffer.toString(); //서버로 부터 받은 값을 리턴해줌 OK!!가 들어올것임
                }
                catch (MalformedURLException e) { e.printStackTrace(); }
                catch (IOException e)           { e.printStackTrace(); }
                finally {
                    if(con != null) con.disconnect();
                    try                     { if(reader != null) reader.close(); }
                    catch (IOException e)   { e.printStackTrace(); }
                }
            }
            catch (Exception e) {e.printStackTrace(); }
            return null;
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            System.out.println("모델 생성이 완료 되었습니다.");
            Intent editIntent  = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(editIntent);
            Toast.makeText (    registerModelCameraActivity.this,
                                "모델 생성이 완료 되었습니다.",
                                Toast.LENGTH_LONG      ).show();

            //서버로 부터 받은 값을 출력해주는 부
//            tvData.setText(result);
        }
    }



}

