package com.cvbotunion.cvtwipush.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.cvbotunion.cvtwipush.Adapters.ImagePagerAdapter;
import com.cvbotunion.cvtwipush.Model.TwitterMedia;
import com.cvbotunion.cvtwipush.Model.TwitterStatus;
import com.cvbotunion.cvtwipush.R;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.File;
import java.util.ArrayList;
// TODO 考虑修改为Fragment
public class ImageViewer extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private ViewPager2 viewPager2;
    private ImagePagerAdapter imagePagerAdapter;
    private TextView pageNum;

    private int page;
    private ArrayList<TwitterMedia> mediaList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediaList = new ArrayList<>();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().getDecorView().setSystemUiVisibility(0);
        setContentView(R.layout.activity_image_viewer);
        toolbar = findViewById(R.id.image_viewer_toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if(itemId==R.id.save_menu_item) {
                saveImage();
            } else if(itemId==R.id.share_menu_item) {
                shareImage();
            }
            return true;
        });
        viewPager2 = findViewById(R.id.image_view_pager);
        pageNum = findViewById(R.id.page_num);
        initData();
        initViewPager();
    }

    public void saveImage(){
        TwitterMedia currentMedia = mediaList.get(viewPager2.getCurrentItem());
        String result = getString(R.string.success);
        if(!currentMedia.saveToFile(this))
            result = getString(R.string.failure);
        Toast.makeText(this, getString(R.string.save)+result, Toast.LENGTH_SHORT).show();
    }

    public void shareImage() {
        TwitterMedia currentMedia = mediaList.get(viewPager2.getCurrentItem());
        if(currentMedia.cached_image != null) {
            Uri imageUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName()+".fileProvider",
                    new File(TwitterMedia.mediaFilesDir, Uri.parse(currentMedia.url).getLastPathSegment()));
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.setType("image/*");
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_image)));
        }
    }

    public void initData() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle != null) {
            page = bundle.getInt("page");
            TwitterStatus status = bundle.getParcelable("twitterStatus");
            mediaList.addAll(status.media);
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

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                ImagePagerAdapter adapter = (ImagePagerAdapter) viewPager2.getAdapter();
                adapter.resetScale();
            }
        });
    }
}