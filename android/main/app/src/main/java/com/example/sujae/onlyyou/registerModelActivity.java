package com.example.sujae.onlyyou;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.sujae.onlyyou.serverUtil.ServerTask;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class registerModelActivity extends AppCompatActivity {
    private static final String TAG     = "";

    String      imgcode                 = null;
    JSch        jsch                    = null;
    Session     session                 = null;
    Channel     channel                 = null;
    ChannelSftp sftpChannel             = null;
    String      localPath               = "/storage/emulated/0/";
    String      face_uploadPath        = "/home/cgvmu/project/OnlyYou/data/model/user";
    String      host                    = "117.16.44.14";
    Integer     port                    = 8022;
    String      user                    = "cgvmu";
    String      password                = "g411";


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
        Toast.makeText(registerModelActivity.this, "score : " + score, Toast.LENGTH_LONG).show();
        return score;
    }

    public registerModelActivity() throws FileNotFoundException {
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_register_model);

//        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//                .detectDiskReads()
//                .detectDiskWrites()
//                .detectNetwork()
//                .penaltyLog().build());



        //사진고르는 페이지로 넘어가기
        //현재 UI에 맞지않아 잠시 빼둠
//        findViewById(R.id.registerBtn1).setOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Intent intent = new Intent();
//                        //Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//
//                        //Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
//                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
//                        intent.setType("image/*");
//                        intent.setAction(Intent.ACTION_GET_CONTENT);
//                        startActivityForResult(intent, 0x00);
//
//                    }
//                });
//        Activity activity = this;
//        verifyStoragePermissions(activity);


        //사진촬영으로 넘어가기
        findViewById(R.id.imgBtn_create_model).setOnClickListener(
                new View.OnClickListener(){
                    public void onClick(View v){
                        Thread server_thread = new Thread(new Runnable() {
                            @Override
                            public void run() { new ServerTask().execute("http://117.16.44.14:6000/post"); }
                        });
                        server_thread.start();
                        try {
                            System.out.println("서버를 초기화 중입니다.");
                            server_thread.join();
                        }
                        catch (InterruptedException e) { e.printStackTrace(); }
                        Intent intent = new Intent(getApplicationContext(), registerModelCameraActivity.class);
                        startActivity(intent);
                    }
                }
        );


    } // end of onCreate


    public void connect() throws JSchException, SftpException {
        System.out.println("connecting..." + host);
        //  JSch 객체를 생성한다.
        jsch = new JSch();
        //  세션 객체를 생성한다(사용자 이름, 접속할 호스트, 포트를 인자로 전달한다.)
        session = jsch.getSession(user, host, port);
        //  세션과 관련된 정보를 설정한다.
        session.setConfig("StrictHostKeyChecking", "no");
        // 패스워드를 설정한다.
        session.setPassword(password);
        //  접속한다.
        session.connect();
        //  sftp 채널을 연다.
        channel = session.openChannel("sftp");
        // 채널에 연결한다.
        channel.connect();
        // 채널을 FTP용 채널 객체로 캐스팅한다.
        sftpChannel = (ChannelSftp) channel;
    }

    //연결 끊기
    public void disconnect() {
        if (session.isConnected()) {
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

    //서버에서 이미지 다운로드
    public void download(String fileName, String localDir) throws Exception {
        //빈버퍼 생성
        byte[] buffer = new byte[1024];
        //버퍼스트림생성
        BufferedInputStream bis;
        //서버연결
        connect();
        try {
            // Change to output directory
            //경로로 이동
            sftpChannel.cd(face_uploadPath);

            File file = new File(fileName);
            bis = new BufferedInputStream(sftpChannel.get(file.getName()));

            File newFile = new File(localDir + "/" + file.getName());

            // 파일다운로드
            OutputStream os = new FileOutputStream(newFile);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            int readCount;
            while ((readCount = bis.read(buffer)) > 0) {
                bos.write(buffer, 0, readCount);
            }
            bis.close();
            bos.close();
            System.out.println("File downloaded successfully - " + file.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
        disconnect();
    }

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

            //정확도 높이기 클릭시 갤러리에 있는 사진이 face_uploadPath로 사진업로드됨
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        upload(imgcode, face_uploadPath);
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

    public static void verifyStoragePermissions(Activity activity) {
        // 퍼미션 확인하기
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // 권한없으므로 사용자에게 문의
            ActivityCompat.requestPermissions(
                    activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1
            ); //여기코드 수정하기
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

}


