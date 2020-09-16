package com.cvbotunion.cvtwipush.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.cvbotunion.cvtwipush.Adapters.ImagePagerAdapter;
import com.cvbotunion.cvtwipush.DBModel.DBTwitterMedia;
import com.cvbotunion.cvtwipush.Model.TwitterMedia;
import com.cvbotunion.cvtwipush.R;
import com.google.android.material.appbar.MaterialToolbar;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class ImageViewer extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private ViewPager2 viewPager2;
    private ImagePagerAdapter imagePagerAdapter;
    private TextView pageNum;

    private int page;
    private ArrayList<TwitterMedia> mediaList = new ArrayList<>();

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
        pageNum = (TextView) findViewById(R.id.page_num);
        initData();
        initViewPager();
    }

    public void saveImage(){
        TwitterMedia currentMedia = mediaList.get(viewPager2.getCurrentItem());
        String result = "成功";
        if(!currentMedia.saveToFile(this))
            result = "失败";
        Toast.makeText(this, "保存"+result, Toast.LENGTH_SHORT).show();
    }

    public void initData() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        page = bundle.getInt("page");
        String statusId = bundle.getString("twitterStatusId");
        List<DBTwitterMedia> dbMediaList = LitePal.where("statusId = ?", statusId).find(DBTwitterMedia.class);
        for(DBTwitterMedia m:dbMediaList) {
            mediaList.add(m.toTwitterMedia());
        }
    }

    public void initViewPager(){
        pageNum.setText("第 "+ page + "/" + mediaList.size() + " 页");
        imagePagerAdapter = new ImagePagerAdapter(this, mediaList);
        viewPager2.setAdapter(imagePagerAdapter);
        viewPager2.setCurrentItem(page-1,false);
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                pageNum.setText("第 "+ (position + 1) + "/" + mediaList.size() + " 页");
            }
        });
    }
}