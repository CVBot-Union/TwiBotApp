package com.cvbotunion.cvtwipush.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.cvbotunion.cvtwipush.Adapters.ImagePagerAdapter;
import com.cvbotunion.cvtwipush.Model.TwitterMedia;
import com.cvbotunion.cvtwipush.Model.TwitterStatus;
import com.cvbotunion.cvtwipush.R;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;

public class ImageViewer extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private ViewPager2 viewPager2;
    private ImagePagerAdapter imagePagerAdapter;

    private TwitterStatus status;
    private ArrayList<String> mediaIdArrayList;
    private ArrayList<Bitmap> images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().getDecorView().setSystemUiVisibility(0);
        setContentView(R.layout.activity_image_viewer);
        toolbar = (MaterialToolbar) findViewById(R.id.image_viewer_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int menuID = item.getItemId();
                if (menuID == R.id.save_menu_item) {
                    saveImage();
                }
                return true;
            }
        });
        viewPager2 = (ViewPager2) findViewById(R.id.image_view_pager);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        assert bundle != null;
        mediaIdArrayList = bundle.getStringArrayList("twitterMediaIdArrayList");

        //start 模拟数据
        initViewPager();
    }

    public void saveImage(){

    }

    public void initViewPager(){
        images = new ArrayList<>();
        ArrayList<TwitterMedia> media = new ArrayList<>();
        media.add(new TwitterMedia("1","http://101.200.184.98:8080/media/MO4FkO4N_400x400.jpg",1,"test"));
        media.add(new TwitterMedia("1","http://101.200.184.98:8080/media/MO4FkO4N_400x400.jpg",1,"test"));
        //end 模拟数据
        imagePagerAdapter = new ImagePagerAdapter(this,media);
        viewPager2.setAdapter(imagePagerAdapter);
    }
}