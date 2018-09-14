package com.memoming.onlyou.activity.edit;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.memoming.onlyou.R;
import com.memoming.onlyou.activity.camera.CameraActivity;
import com.memoming.onlyou.serverUtil.Server;
import com.memoming.onlyou.serverUtil.ServerTask;

import java.io.File;
import java.io.IOException;

public class Compare extends FragmentActivity {
    private Button btn_album,btn_convert,btn_compare;
    private ImageView userImg;
    private final int GALLETY_CODE = 1112;
    private int comparecode = 0;
    private static Context      mContext;
    private String imagePath;
    private String filename;
    private Uri imagePathUri;
    private String  mConvertedImageFileName;
    private File mImageFolder;
    private String              mImageUploadPath    = "/home/cgvmu/project/OnlyYou/data/edit_before/";
    private String              mImageDownloadPath  = "/home/cgvmu/project/OnlyYou/data/edit_after/";



    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare);
        userImg = (ImageView) this.findViewById(R.id.userImg);
        btn_album = (Button) this.findViewById(R.id.btn_album);
        btn_convert = (Button) this.findViewById(R.id.btn_convert);
        btn_compare = (Button) this.findViewById(R.id.btn_compare);
        mContext = Compare.this;

        btn_album.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                selectPhoto();
            }
        });

        btn_convert.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                convertPhoto();
            }
        });

        btn_compare.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        imagePath = mImageFolder+"/"+mConvertedImageFileName;
                        comparePhoto();
                        break;
                    case MotionEvent.ACTION_UP:
                        imagePath = mImageFolder+"/"+filename;
                        comparePhoto();
                        break;
                }
                return false;
            }
        });


//        btn_compare.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                comparePhoto();
//            }
//        });
    }
    private class progress_serverTask extends ServerTask {

        private Handler mHandler;
        private ProgressDialog mProgressDialog;
        private String          displayMsg;

        progress_serverTask(String text) {
            displayMsg = text;
            mHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    if( msg.what == 0 ) {
                        mProgressDialog = new ProgressDialog(Compare.this);
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

    public void selectPhoto(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, GALLETY_CODE);
    }

    public void convertPhoto() {

//        try {
            Thread convert_thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    new Compare.progress_serverTask("이미지를 변환 중입니다 ... !").execute("http://117.16.44.14:8054/post");
                }
            });
            convert_thread.start();
            System.out.println("변환시작");
        try {
            convert_thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        Thread image_download_thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        File imageFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                        mImageFolder = new File(imageFile, "OnlyYou_IMG");
                        Server server = new Server();
                        System.out.println("mImageFolder : "+mImageFolder.getAbsolutePath());
                        System.out.println("imagePath : "+imagePath);
                        System.out.println("imagePathUri : "+imagePathUri);
                        filename = imagePath.substring(imagePath.lastIndexOf("/")+1);

                        System.out.println("filename :"+filename);
                        mConvertedImageFileName = filename;
                        mConvertedImageFileName = mConvertedImageFileName.substring(0,mConvertedImageFileName.length()-4)+"_onlyYou.jpg";
                        imagePath = mImageFolder+"/"+mConvertedImageFileName;
                        server.download(mConvertedImageFileName,mImageDownloadPath,mImageFolder.getAbsolutePath());
                        refreshGallery(imagePath,mContext);
                    }
                    catch (Exception e) { e.printStackTrace(); }
                }
            });
            image_download_thread.start();
        try {
            image_download_thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void comparePhoto(){
//        if(comparecode==0){
//            imagePath = mImageFolder+"/"+mConvertedImageFileName;
//            comparecode=1;
//        }
//        else{
//            imagePath = mImageFolder+"/"+filename;
//            comparecode=0;
//        }
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int exifDegree = exifOrientationToDegrees(exifOrientation);

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);//경로를 통해 비트맵으로 전환
        userImg.setImageBitmap(rotate(bitmap, exifDegree));//이미지 뷰에 비트맵 넣기
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == GALLETY_CODE) {
            sendPicture(data.getData());    //갤러리에서 가져오기
            imagePathUri = data.getData();
            imagePath = getRealPathFromURI(imagePathUri);
            try {
                Thread image_upload_thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Server server = new Server();
                            server.upload(imagePath, mImageUploadPath);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                image_upload_thread.start();
                image_upload_thread.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendPicture(Uri imgUri) {

        imagePath = getRealPathFromURI(imgUri);  //path 경로
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int exifDegree = exifOrientationToDegrees(exifOrientation);

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);//경로를 통해 비트맵으로 전환
        userImg.setImageBitmap(rotate(bitmap, exifDegree));//이미지 뷰에 비트맵 넣기
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

    private static void refreshGallery(String mCurrentPhotoPath, Context context) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);

    }



}
