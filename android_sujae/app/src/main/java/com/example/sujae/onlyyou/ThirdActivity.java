package com.example.sujae.onlyyou;

import android.Manifest;
import android.app.Activity;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static java.lang.System.in;

public class ThirdActivity extends AppCompatActivity {
    private static final String TAG = "";
    String imgcode;

    private File uploadFilePath;

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
    public ThirdActivity() throws FileNotFoundException {
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.third);

//        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//                .detectDiskReads()
//                .detectDiskWrites()
//                .detectNetwork()
//                .penaltyLog().build());

        findViewById(R.id.registerBtn).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        //Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

                        //Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(intent, 0x00);

                    }
                });
        Activity activity = this;
        verifyStoragePermissions(activity);
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

    public void upload(String fileName, String remoteDir) throws Exception {
        FileInputStream fis = null;
        // 앞서 만든 접속 메서드를 사용해 접속한다.
        connect();
        try {
            // Change to output directory
            sftpChannel.cd(remoteDir);

            // Upload file
            File file = new File(fileName);
            // 입력 파일을 가져온다.
            fis = new FileInputStream(file);
            // 파일을 업로드한다.
            sftpChannel.put(fis, file.getName());

            fis.close();
            System.out.println("File uploaded successfully - "+ file.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (sftpChannel.getExitStatus()==-1) {
            System.out.println("file uploaded");
            Log.v("upload result", "succeeded");
        } else {
            Log.v("upload faild ", "faild");
        }

        disconnect();
    }

//    public void download(String dir, String downloadFileName, String path) {
//        InputStream in = null;
//        FileOutputStream out = null;
//        try {
//            sftpChannel.cd(dir);
//            System.out.print(sftpChannel.lpwd());
//            in = sftpChannel.get(downloadFileName);
//
//        } catch (SftpException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        try {
//            out = new FileOutputStream(new File(path));
//            int i;
//
//            while ((i = in.read()) != -1) {
//                out.write(i);
//            }
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        finally {
//            try {
//                out.close();
//                in.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

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


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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

                Bitmap bitmap = BitmapFactory.decodeFile(imgcode);//경로를 통해 비트맵으로 전환
                ivPreview.setImageBitmap(rotate(bitmap, exifDegree));//이미지 뷰에 비트맵 넣기


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
                        upload(imgcode, remotePathUp);
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
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
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

    private String getRealPathFromURI(Uri contentUri) {
        if (contentUri.getPath().startsWith("/storage")) {
           return contentUri.getPath();
        }
        String id = DocumentsContract.getDocumentId(contentUri).split(":")[1];
        String[] columns = { MediaStore.Files.FileColumns.DATA };
        String selection = MediaStore.Files.FileColumns._ID + " = " + id;
        Cursor cursor = getContentResolver().query(MediaStore.Files.getContentUri("external"), columns, selection, null, null);
        try {
            int columnIndex = cursor.getColumnIndex(columns[0]);
            if (cursor.moveToFirst()) {
                return cursor.getString(columnIndex);
            }
        } finally { cursor.close();
        }
        return null;
    }
}


