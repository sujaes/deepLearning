package com.example.sujae.onlyyou;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;


public class ThirdActivity extends AppCompatActivity {
    String imgcode;
    private UploadActivity fileUpFTP;
    String ftpURL = "117.16.44.14";
    int port = 8022;

    public ThirdActivity() {
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.third);




        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog().build());

        findViewById(R.id.registerBtn).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0x00 && resultCode == RESULT_OK) {
            try {

                Uri selectedImgUri = data.getData();
                InputStream in = getContentResolver().openInputStream(
                        selectedImgUri);
                imgcode = getPath(selectedImgUri);

                Bitmap img = BitmapFactory.decodeStream(in);
                in.close();
                ImageView ivPreview = (ImageView) findViewById(R.id.imageView2);
                ivPreview.setImageBitmap(img);

                uploading(selectedImgUri);

//                sendFileViaFTP();
            } catch (Exception e) {
            }

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
    public  void uploading(Uri uri){

        try {
            fileUpFTP = new UploadActivity(ftpURL,port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fileUpFTP.login("cgvmu","g411");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fileUpFTP.settingFTP( 10000, "utf-8","/uploadingfiles/");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            String realpath = getPath(uri);
            boolean isSucceess = fileUpFTP.FileUploadFtp(realpath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fileUpFTP.closedSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


//    public void sendFileViaFTP() {
//
//        FTPClient ftpClient = null;
//
//        try {
//            ftpClient = new FTPClient();
//            ftpClient.connect("117.16.44.14",8022);
//
//            if (ftpClient.login("cgvmu", "g411")) {
//
//                ftpClient.enterLocalPassiveMode(); // important!
//                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
//                String Location = Environment.getExternalStorageDirectory()
//                        .toString();
//                String data = Location + File.separator + "FileToSend.txt";
//                FileInputStream in = new FileInputStream(new File(data));
//                boolean result = ftpClient.storeFile("project/OnlyYou/data"+"FileToSend.txt", in);
//                in.close();
//                if (result)
//                    Log.v("upload result", "succeeded");
//                ftpClient.logout();
//                ftpClient.disconnect();
//
//            }
//        } catch (Exception e) {
//            Log.v("count", "error");
//            e.printStackTrace();
//        }
//
//    }


}


