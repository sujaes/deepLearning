package com.example.sujae.onlyyou;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FourthActivity extends AppCompatActivity {
    private final int CAMERA_CODE = 1111;
    private final int GALLETY_CODE = 1112;
    private final int REQUEST_PERMISSION_CODE = 2222;




    JSch jsch;
    Session session;
    Channel channel=null;
    ChannelSftp sftpChannel;
    //    String localPath = "//media/external/images/0/";
    String localPath = "/storage/emulated/0/";
    String remotePathUp = "/home/cgvmu/project/OnlyYou/data/";
    String remotePathDown = "/home/cgvmu/project/OnlyYou/newdata/";

    String host = "117.16.44.14";
    Integer port = 8022;
    String user = "cgvmu";
    String password ="g411";



    private Uri photoUri;
    private String currentPhotoPath;    //실제 사진 파일 경로
    String mImageCaptureName;   //이미지 이름
    private Button btn_agreeJoin;
    private ImageView iv_UserPhoto,iv_UserPhoto1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fourth);
        iv_UserPhoto = (ImageView) this.findViewById(R.id.user_image);
        iv_UserPhoto1 = (ImageView) this.findViewById(R.id.user_image1);

        btn_agreeJoin = (Button) this.findViewById(R.id.btn_UploadPicture);


        btn_agreeJoin.setOnClickListener(onClick);
        btn_agreeJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectGalley();
            }
        });

    }


    private void selectGalley() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, GALLETY_CODE);
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == GALLETY_CODE) {
            sendoriginPicture(data.getData());    //갤러리에서 가져오기
        }




        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    download("/123.jpg",localPath);
                } catch (Exception ex) {
                    System.out.println(ex);
                }
            }
        });
        thread.start();
        try{
            System.out.println("보내려는 이미지 값 : " +Uri.parse(localPath+"/123.jpg"));
            sendeditPicture(Uri.parse(localPath+"/123.jpg"));

        }catch (Exception exx){
            exx.printStackTrace();
        }
    }

    private void sendoriginPicture(Uri imgUri) {
        String imagePath = getRealPathFromURI(imgUri);  //path 경로
        System.out.println("originimagePath값 : "+ imagePath);

        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int exifDegree = exifOrientationToDegrees(exifOrientation);

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);//경로를 통해 비트맵으로 전환
        iv_UserPhoto.setImageBitmap(rotate(bitmap, exifDegree));//이미지 뷰에 비트맵 넣기
    }

    private void sendeditPicture(Uri imgUri) {

        String imagePath = getPath(imgUri);  //path 경로
        System.out.println("editimagePath값 : "+ imagePath);
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int exifDegree = exifOrientationToDegrees(exifOrientation);

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);//경로를 통해 비트맵으로 전환
        iv_UserPhoto1.setImageBitmap(rotate(bitmap, exifDegree));//이미지 뷰에 비트맵 넣기
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

    private Bitmap rotate(Bitmap src, float degree) {

// Matrix 객체 생성
        Matrix matrix = new Matrix();
// 회전 각도 셋팅
        matrix.postRotate(degree);
// 이미지와 Matrix 를 셋팅해서 Bitmap 객체 생성
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(),
                src.getHeight(), matrix, true);

    }

    private String getRealPathFromURI(Uri contentUri) {
        int column_index = 0;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        }

        return cursor.getString(column_index);
    }

    public String getPath(Uri uri) {
        // uri가 null일경우 null반환
        if( uri == null ) {
            return null;
        }
        // 미디어스토어에서 유저가 선택한 사진의 URI를 받아온다.
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        // URI경로를 반환한다.
        return uri.getPath();
    }

    public void connect() throws JSchException, SftpException {
        System.out.println("connecting..."+host);
        // 1. JSch 객체를 생성한다.
        jsch = new JSch();
        // 2. 세션 객체를 생성한다(사용자 이름, 접속할 호스트, 포트를 인자로 전달한다.)
        session = jsch.getSession(user,host,port);
        // 4. 세션과 관련된 정보를 설정한다.
        session.setConfig("StrictHostKeyChecking", "no");
        // 4. 패스워드를 설정한다.
        session.setPassword(password);
        // 5. 접속한다.
        session.connect();

        // 6. sftp 채널을 연다.
        channel = session.openChannel("sftp");
        // 7. 채널에 연결한다.
        channel.connect();
        // 8. 채널을 FTP용 채널 객체로 캐스팅한다.
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



    public void download(String fileName, String localDir) throws Exception{
        byte[] buffer = new byte[1024];
        BufferedInputStream bis;
        connect();
        try {
            // Change to output directory

            sftpChannel.cd(remotePathUp);


            File file = new File(fileName);
            bis = new BufferedInputStream(sftpChannel.get(file.getName()));

            File newFile = new File(localDir + "/" + file.getName());

            // Download file
            OutputStream os = new FileOutputStream(newFile);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            int readCount;
            while ((readCount = bis.read(buffer)) > 0) {
                bos.write(buffer, 0, readCount);
            }
            bis.close();
            bos.close();
            System.out.println("File downloaded successfully - "+ file.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
        disconnect();

    }


//    Uri a = Uri.fromFile(new File(remotePathUp+"/123.jpg"));
//                    System.out.println("a값 : "+a);
//    String contentPath = a.getPath();
//                    System.out.println("contentPath 값 : "+contentPath);
//
//    Cursor c = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,null, "_data = '" + contentPath + "'", null,null);
//                    c.moveToNext();
//    int id = c.getInt(0);
//    Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
//                    System.out.println("editContetnURI값 : "+uri);






    public void savePhotos() // 편집이미지 저장하기
    {

    }


    private View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {


            switch (view.getId()) {


                case R.id.btn_UploadPicture:

                    int permissionCheck = ContextCompat.checkSelfPermission(FourthActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE);

                    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                        selectGalley();


                    } else {
                        requestPermission();
                        if (ActivityCompat.shouldShowRequestPermissionRationale(FourthActivity.this
                                , Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//거부했을 경우
                            Toast toast=Toast.makeText(FourthActivity.this,
                                    "기능 사용을 위한 권한 동의가 필요합니다.", Toast.LENGTH_SHORT);
                            toast.show();
                        }else{

                            Toast toast=Toast.makeText(FourthActivity.this,
                                    "기능 사용을 위한 권한 동의가 필요합니다.", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                    break;
            }

        }
    };

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_PERMISSION_CODE);

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){

            case REQUEST_PERMISSION_CODE:

                if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
//동의 했을 경우
                    selectGalley();
                }else{
//거부했을 경우
                    Toast toast=Toast.makeText(this,
                            "기능 사용을 위한 권한 동의가 필요합니다.", Toast.LENGTH_SHORT);
                    toast.show();
                }

                break;
        }
    }

}