package com.example.sujae.onlyyou;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;


public class ThirdActivity extends AppCompatActivity {


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.third);

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
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0x00 && resultCode == RESULT_OK) {
            try {
                InputStream in = getContentResolver().openInputStream(
                        data.getData());
                Bitmap img = BitmapFactory.decodeStream(in);
                in.close();
                ImageView ivPreview = (ImageView) findViewById(R.id.photos);
                ivPreview.setImageBitmap(img);
            } catch (Exception e) {
            }
        }
    }
}