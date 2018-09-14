package com.memoming.onlyou.activity.main;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.memoming.onlyou.R;
import com.memoming.onlyou.activity.model.ModelChecker;
import com.memoming.onlyou.activity.setting.SettingActivity;

public class MainActivity extends FragmentActivity {

    public static Activity mMainActivity;
    private LoadingActivity mLoadingActivity;
    private Button  mCamaraButton;
    private Button  mModelButton;
    private Button  mEditButton;
    private Button  mSettingButton;
    private static final int REQUEST_CAMERA_PERMISSION = 200; //카메라 퍼미션 200으로 임의선언

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        mMainActivity       = MainActivity.this;
        mLoadingActivity    = (LoadingActivity)LoadingActivity.mLoadingActivity;
        mLoadingActivity.finish();

        mCamaraButton       = (Button)findViewById(R.id.btn_camera);
        mModelButton        = (Button)findViewById(R.id.btn_model);
        mEditButton         = (Button)findViewById(R.id.btn_edit);
        mSettingButton      = (Button)findViewById(R.id.btn_setting);
        setClickListener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(MainActivity.this, "카메라 사용이 승인되지 않았습니다.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void setClickListener() {
        mCamaraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new com.memoming.onlyou.activity.camera.ModelChecker(MainActivity.this).exec();
            }
        });

        mModelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new com.memoming.onlyou.activity.model.ModelChecker(MainActivity.this).exec();
            }
        });

        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new com.memoming.onlyou.activity.edit.ModelChecker(MainActivity.this).exec();
//                new ModelCheck_Edit(MainActivity.this).exec();
            }
        });

        mSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingIntent = new Intent(getApplicationContext(), SettingActivity.class);
                startActivity(settingIntent);
            }
        });
    }

}
