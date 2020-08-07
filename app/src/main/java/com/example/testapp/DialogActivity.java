package com.example.testapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class DialogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        this.setTitle("推文内容");
        Intent intent = getIntent();
        String twiContent = intent.getStringExtra("twi_content");
        String img = intent.getStringExtra("img");
        final TextView textView = findViewById(R.id.twi_content);
        textView.setText(twiContent);
        ImageView imageView = findViewById(R.id.twi_img);
        imageView.setImageResource(R.mipmap.ic_launcher_round);
        Button copy_button = findViewById(R.id.copy_button);
        copy_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                assert clipboardManager != null;
                clipboardManager.setPrimaryClip(ClipData.newPlainText(null,textView.getText()));
                Toast.makeText(DialogActivity.this,"复制成功",Toast.LENGTH_SHORT).show();
            }
        });
    }
}