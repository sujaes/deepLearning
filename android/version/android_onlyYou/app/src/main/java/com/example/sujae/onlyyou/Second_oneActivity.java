package com.example.sujae.onlyyou;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class Second_oneActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_one);
        Toast.makeText(Second_oneActivity.this,"얼굴등록을 해주세요", Toast.LENGTH_LONG).show();
    }
}
