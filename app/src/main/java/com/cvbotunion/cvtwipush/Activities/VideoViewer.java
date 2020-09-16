package com.cvbotunion.cvtwipush.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.cvbotunion.cvtwipush.DBModel.DBTwitterMedia;
import com.cvbotunion.cvtwipush.Model.TwitterMedia;
import com.cvbotunion.cvtwipush.R;
import com.dueeeke.videoplayer.ijk.IjkPlayer;
import com.dueeeke.videoplayer.player.VideoView;
import com.google.android.material.appbar.MaterialToolbar;

import org.litepal.LitePal;

import java.io.File;

public class VideoViewer extends AppCompatActivity {
    VideoView<IjkPlayer> playerView;
    TwitterMedia video;
    MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().getDecorView().setSystemUiVisibility(0);
        setContentView(R.layout.activity_video_viewer);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        assert bundle != null;
        String url = bundle.getString("url");
        video = LitePal.where("url = ?", url).findFirst(DBTwitterMedia.class).toTwitterMedia();
        File videoFile = new File(TwitterMedia.savePath, Uri.parse(url).getLastPathSegment());
        if(videoFile.exists())
            url = videoFile.toURI().getPath();

        playerView = findViewById(R.id.video_player_view);
        playerView.setUrl(url);
        playerView.start();

        toolbar = findViewById(R.id.video_viewer_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerView.pause();
                playerView.release();
                onBackPressed();
            }
        });
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int menuID = item.getItemId();
                if (menuID == R.id.save_menu_item) {
                    saveVideo();
                }
                return true;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        playerView.pause();
        playerView.release();
    }

    public void saveVideo(){
        String result = "成功";
        if(!video.saveToFile(this))
            result = "失败";
        Toast.makeText(this, "保存"+result, Toast.LENGTH_SHORT).show();
    }
}