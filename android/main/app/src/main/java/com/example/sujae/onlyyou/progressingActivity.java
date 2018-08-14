package com.example.sujae.onlyyou;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class progressingActivity extends AppCompatActivity {

    public TextView tv_message;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_dl_progressing);

        tv_message = findViewById(R.id.tv_dl_message);
        Intent intent = getIntent();
        String message = intent.getStringExtra("message");
        tv_message.setText(message);

    } // end of onCreate

}
