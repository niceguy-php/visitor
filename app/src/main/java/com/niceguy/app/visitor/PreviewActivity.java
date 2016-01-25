package com.niceguy.app.visitor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class PreviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        String path = getIntent().getStringExtra("path");
        ImageView imageView = (ImageView) findViewById(R.id.preview_avatar);
        Bitmap img = BitmapFactory.decodeFile(path);
        imageView.setImageBitmap(img);

        Button sure_pic = (Button) findViewById(R.id.sure_pic);
        Button re_capture = (Button) findViewById(R.id.re_capture);
        sure_pic.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("status",1);
                intent.setClass(PreviewActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        re_capture.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("status",0);
                intent.setClass(PreviewActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
