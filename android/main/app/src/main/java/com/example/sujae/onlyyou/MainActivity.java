package com.example.sujae.onlyyou;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.example.sujae.onlyyou.serverUtil.ServerTask;

public class MainActivity extends AppCompatActivity {

    private Button btn_camera;
    private Button btn_register_model;
    private Button btn_edit;
    private Button btn_setting;

    private int             modelFlag;
    private static final int REQUEST_CAMERA_PERMISSION = 200; //카메라 퍼미션 200으로 임의선언

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_main);

        btn_camera  = (Button) findViewById(R.id.btn_camera);
        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread server_thread = new Thread(new Runnable() {
                    @Override
                    public void run() { new NodeTask().execute("http://117.16.44.14:5000/post"); }
                });
                server_thread.start();
                try {
                    System.out.println("모델 생성 여부를 체크중입니다 ...");
                    server_thread.join();
                }
                catch (InterruptedException e) { e.printStackTrace(); }
            }
        });
        btn_register_model = (Button) findViewById(R.id.btn_register_model);
        btn_register_model.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(getApplicationContext(), registerModelActivity.class);
                startActivity(intent1);
            }
        });
        btn_edit = (Button) findViewById(R.id.btn_edit);
        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(getApplicationContext(), editActivity.class);
                startActivity(intent1);
            }
        });
        btn_setting = (Button) findViewById(R.id.btn_setting);
        btn_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(getApplicationContext(), settingActivity.class);
                startActivity(intent1);
            }
        });
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

    private class NodeTask extends ServerTask {
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            modelFlag       = Integer.parseInt(result);
            Intent intent1  = new Intent(getApplicationContext(), cameraActivity.class);
            Intent intent2  = new Intent(getApplicationContext(), cameraRegisterActivity.class);
            if(modelFlag != 0)  startActivity(intent1);
            else                startActivity(intent2);
        }
    }

}
