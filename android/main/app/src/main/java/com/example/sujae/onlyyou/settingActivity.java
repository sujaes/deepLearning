package com.example.sujae.onlyyou;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;


public class settingActivity extends AppCompatActivity {


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
        Toast.makeText(settingActivity.this, "score : " + score, Toast.LENGTH_LONG).show();
        return score;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_setting);




//        TextView textView1 = (TextView) findViewById(R.id.view);
//
//        AssetManager aMgr = getResources().getAssets();
//        InputStream is = null;
//        OutputStream ot = null;
//
//        Ini ini = null;
//
//
//
//        try {
//            is = aMgr.open("ui_setting.ini");
//            ini = new Ini(is);
//
//            String text ="face=true";
//            FileOutputStream fos = null;
//            fos = new FileOutputStream("ui_setting.ini");
//
//            fos.write((text.getBytes()));
//
//            ini.store(fos);
//            fos.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        Ini.Section section = ini.get("list");
//
//        textView1.setText(section.get("face"));



    }

}
