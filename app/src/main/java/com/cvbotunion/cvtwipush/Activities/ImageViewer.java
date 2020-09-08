package com.cvbotunion.cvtwipush.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.graphics.Bitmap;
import android.os.Bundle;

import com.cvbotunion.cvtwipush.Model.TwitterStatus;
import com.cvbotunion.cvtwipush.R;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;

public class ImageViewer extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private ViewPager2 viewPager2;

    private TwitterStatus status;
    private ArrayList<Bitmap> images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        toolbar = (MaterialToolbar) findViewById(R.id.image_viewer_toolbar);
        viewPager2 = (ViewPager2) findViewById(R.id.image_view_pager);
    }
}