package com.example.sujae.onlyyou;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
//얼굴등록 안 했을경우
public class cameraRegisterActivity extends AppCompatActivity {

    //flag변수 k
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
        Toast.makeText(cameraRegisterActivity.this, "score : " + score, Toast.LENGTH_LONG).show();
        return score;
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_camera_register);
        Toast.makeText(cameraRegisterActivity.this,"얼굴등록을 먼저 해주세요", Toast.LENGTH_LONG).show();
    }
}
