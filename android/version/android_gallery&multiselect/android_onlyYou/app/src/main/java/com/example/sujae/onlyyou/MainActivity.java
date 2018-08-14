package com.example.sujae.onlyyou;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button b1,b2,b3,b4;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //첫번째기능 촬영
        b1 = (Button) findViewById(R.id.b1);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(getApplicationContext(), SecondActivity.class);
                Intent intent2 = new Intent(getApplicationContext(), Second_oneActivity.class);
                //기존에 데이터가 있고없고에 따라서 달라진다.
                //데이터가 있으면
//                if(){
//                    startActivity(intent1);
//                }
                //데이터가 없으면 등록
//                else{
//                    startActivity(intent2);
//                }
                startActivity(intent1);
            }
        });
        //두번째기능 얼굴등록
        b2 = (Button) findViewById(R.id.b2);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(getApplicationContext(), ThirdActivity.class);
                startActivity(intent1);
            }
        });
        //세번째기능 편집
        b3 = (Button) findViewById(R.id.b3);
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(getApplicationContext(), FourthActivity.class);
                startActivity(intent1);
            }
        });
        //네번째기능 설정
        b4 = (Button) findViewById(R.id.b4);
        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(getApplicationContext(), FifthActivity.class);
                startActivity(intent1);
            }
        });
    }
}
