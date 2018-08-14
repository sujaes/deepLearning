package com.example.sujae.onlyyou;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sujae.onlyyou.serverUtil.ServerTask;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;



// TODO :: Blur / Mosaic 선택 여부에 따른 함수 생성해주세요.
// TODO :: 비교 버튼을 눌렀을때 Image가 변경되도록 해주세요.
// 지금은 사진을 선택하기만 하면 서버에서 변경됩니다.


public class editActivity extends AppCompatActivity {
    private final int CAMERA_CODE               = 1111;
    private final int GALLETY_CODE              = 1112;
    private final int REQUEST_PERMISSION_CODE   = 2222;


    String      before_pic_path     = "/home/cgvmu/project/OnlyYou/data/edit_before";
    String      after_pic_path      = "/home/cgvmu/project/OnlyYou/data/edit_after";
    String      imgPath             = null;
    TextView    tvData;
    JSch        jsch;
    Session     session;
    Channel     channel             = null;
    ChannelSftp sftpChannel;
    String      host                = "117.16.44.14";
    Integer     port                = 8022;
    String      user                = "cgvmu";
    String      password            = "g411";
    String      edited_file_path    = "/home/cgvmu/project/OnlyYou/data/edit_after";
    String      localPath           = "/storage/emulated/0";

    private Uri         photoUri;
    private String      currentPhotoPath; //실제 사진 파일 경로
    String              mImageCaptureName; //이미지 이름
    private Button      btn_selectPic;
    private Button      btn_compare;
    private ImageView   iv_UserPhoto;
    private ImageView   iv_UserPhoto1;


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
        Toast.makeText(editActivity.this, "score : " + score, Toast.LENGTH_LONG).show();
        return score;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_edit);
        iv_UserPhoto    = (ImageView) this.findViewById(R.id.user_image);
        btn_compare     = (Button)findViewById(R.id.httpTest);
        btn_selectPic   = (Button) this.findViewById(R.id.btn_UploadPicture);
        btn_selectPic.setOnClickListener(onClick);
        btn_selectPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectGalley();
            }
        });
    }

    //사진선택화면으로 이동하는 함수
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
            get_picture(data.getData(), "gallery");

            imgPath = getPath(data.getData());
            if (imgPath == null) imgPath = getRealPathFromURI(data.getData());

            Thread upload_thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try                     { upload_picture(imgPath, before_pic_path); }
                    catch (Exception ex)    { System.out.println(ex); }
                }
            });
            upload_thread.start();
            try {
                System.out.println("이미지를 업로드 하는 중입니다 ...");
                upload_thread.join();
            }
            catch (InterruptedException e) {e.printStackTrace();}
            System.out.println("이미지 업로드가 완료 되었습니다.");


            Thread server_thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    new NodeTask().execute("http://117.16.44.14:4000/post");
                }
            });
            server_thread.start();

            Intent progressingIntent  = new Intent(getApplicationContext(), progressingActivity.class);
            progressingIntent.putExtra("message", "사진을 변환 중 입니다 ...\n 잠시만 기다려주세요.");
            startActivity(progressingIntent);

            try {
                System.out.println("이미지를 변환 하는 중입니다 ...");
                server_thread.join();
            }
            catch (InterruptedException e) { e.printStackTrace(); }

        }


    }

    //사진 uri로 사진 가지고와서 띄우기
    private void get_picture(Uri imgUri, String from){
        String imagePath;
        ExifInterface exif = null;

        if (from == "gallery")  imagePath = getRealPathFromURI(imgUri);  //path 경로
        else                    imagePath = getPath(imgUri);

        try                     { exif = new ExifInterface(imagePath); }
        catch (IOException e)   { e.printStackTrace(); }

        int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int exifDegree      = exifOrientationToDegrees(exifOrientation);
        Bitmap bitmap       = BitmapFactory.decodeFile(imagePath);
        // 경로를 통해 비트맵으로 전환
        if (from == "gallery")  iv_UserPhoto.setImageBitmap(rotate(bitmap, exifDegree));            // 이미지 뷰에 비트맵 넣기
        else                    iv_UserPhoto1.setImageBitmap(rotate(bitmap, exifDegree));           // 이미지 뷰에 비트맵 넣기
    }

    private int exifOrientationToDegrees(int exifOrientation) {
        int rotate;
        if      (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90)    rotate = 90;
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180)   rotate = 180;
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270)   rotate = 270;
        else                                                                rotate = 0;
        return rotate;
    }

    private Bitmap rotate(Bitmap src, float degree) {
        Matrix matrix = new Matrix();   // Matrix 객체 생성
        matrix.postRotate(degree);      // 회전 각도 셋팅
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(),src.getHeight(), matrix, true); // 이미지와 Matrix 를 셋팅해서 Bitmap 객체 생성
    }

    private String getRealPathFromURI(Uri contentUri) {
        int column_index    = 0;
        String[] proj       = {MediaStore.Images.Media.DATA};
        Cursor cursor       = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst())
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        return cursor.getString(column_index);
    }

    public String getPath(Uri uri) {
        if( uri == null ) return null;

        // 미디어스토어에서 유저가 선택한 사진의 URI를 받아온다.
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor       = managedQuery(uri, projection, null, null, null);
        if( cursor != null ) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        return uri.getPath();
    }

    public void connect() throws JSchException {
        jsch        = new JSch();
        session     = jsch.getSession(user,host,port);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword(password);
        session.connect();
        channel     = session.openChannel("sftp");
        channel.connect();
        sftpChannel = (ChannelSftp) channel;
    }

    public void disconnect() {
        if(session.isConnected()){
            sftpChannel.disconnect();
            channel.disconnect();
            session.disconnect();
        }
    }

    public void upload_picture(String fileName, String remoteDir) throws Exception {
        FileInputStream fis = null;
        connect();
        try {
            sftpChannel.cd(remoteDir);
            File file   = new File(fileName);
            fis         = new FileInputStream(file);
            sftpChannel.put(fis, file.getName());
            fis.close();
        }
        catch (Exception e) { e.printStackTrace(); }
        disconnect();
    }


    //사진 다운로드
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
            sftpChannel.cd(edited_file_path);

            File file       = new File(fileName);
            bis             = new BufferedInputStream(sftpChannel.get(file.getName()));
            File newFile    = new File(localDir + "/" + file.getName());

            // 파일다운로드
            OutputStream os             = new FileOutputStream(newFile);
            BufferedOutputStream bos    = new BufferedOutputStream(os);
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


    private View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_UploadPicture:
                    int permissionCheck = ContextCompat.checkSelfPermission(editActivity.this,
                                                                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (permissionCheck == PackageManager.PERMISSION_GRANTED) selectGalley();
                    else {
                        requestPermission();
                        if (ActivityCompat.shouldShowRequestPermissionRationale(editActivity.this,
                                                                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            Toast toast=Toast.makeText( editActivity.this,
                                                        "기능 사용을 위한 권한 동의가 필요합니다.",
                                                        Toast.LENGTH_SHORT);
                            toast.show();
                        }
                        else {
                            Toast toast=Toast.makeText( editActivity.this,
                                                        "기능 사용을 위한 권한 동의가 필요합니다.",
                                                        Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                    break;
            }
        }
    };


    private void requestPermission() {
        ActivityCompat.requestPermissions(  this,
                                            new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE },
                                            REQUEST_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE:
                if ( grantResults[0]== PackageManager.PERMISSION_GRANTED ) selectGalley();
                else {
                    Toast toast = Toast.makeText(   this,
                                                    "기능 사용을 위한 권한 동의가 필요합니다.",
                                                    Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;
        }
    }

    private class NodeTask extends ServerTask {
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            System.out.println("사진 변환 및 다운로드가 완료 되었습니다.");
            Intent editIntent  = new Intent(getApplicationContext(), editActivity.class);
            startActivity(editIntent);
            Toast.makeText( editActivity.this,
                    "이미지 변환이 완료되었습니다. 비교 버튼을 눌러보세요 !",
                    Toast.LENGTH_SHORT).show();
        }
    }
}