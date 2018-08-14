package com.example.sujae.onlyyou;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FourthActivity extends AppCompatActivity {
    private final int CAMERA_CODE = 1111;
    private final int GALLETY_CODE = 1112;
    private final int REQUEST_PERMISSION_CODE = 2222;



    private Uri photoUri;
    private String currentPhotoPath;    //실제 사진 파일 경로
    String mImageCaptureName;   //이미지 이름
    private Button btn_agreeJoin;
    private ImageView iv_UserPhoto;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fourth);
        iv_UserPhoto = (ImageView) this.findViewById(R.id.user_image);
        btn_agreeJoin = (Button) this.findViewById(R.id.btn_UploadPicture);


        btn_agreeJoin.setOnClickListener(onClick);
        btn_agreeJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectGalley();
            }
        });




        Button savePhoto = (Button) this.findViewById(R.id.savePhoto);
        savePhoto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                savePhotos();
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
            sendPicture(data.getData());    //갤러리에서 가져오기

        }
    }

    private void sendPicture(Uri imgUri) {

        String imagePath = getRealPathFromURI(imgUri);  //path 경로
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
