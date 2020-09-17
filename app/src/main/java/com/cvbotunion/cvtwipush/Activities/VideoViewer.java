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
import com.cvbotunion.cvtwipush.TwiPush;
import com.danikula.videocache.HttpProxyCacheServer;
import com.dueeeke.videocontroller.StandardVideoController;
import com.dueeeke.videoplayer.ijk.IjkPlayer;
import com.dueeeke.videoplayer.player.VideoView;
import com.google.android.material.appbar.MaterialToolbar;

import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class VideoViewer extends AppCompatActivity {
    public static final String cacheDir = TwiPush.getContext().getExternalCacheDir().getPath();
    VideoView<IjkPlayer> playerView;
    TwitterMedia video;
    MaterialToolbar toolbar;
    HttpProxyCacheServer cacheServer;
    private File videoFile;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().getDecorView().setSystemUiVisibility(0);
        setContentView(R.layout.activity_video_viewer);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        assert bundle != null;
        url = bundle.getString("url");
        video = LitePal.where("url = ?", url).findFirst(DBTwitterMedia.class).toTwitterMedia();
        videoFile = new File(TwitterMedia.savePath, Uri.parse(url).getLastPathSegment());
        String verifiedUrl;
        if(videoFile.exists()) {
            verifiedUrl = videoFile.toURI().getPath();
        }
        else {
            cacheServer = new HttpProxyCacheServer.Builder(this)
                    .maxCacheSize(256*1024*1024)  // 256MB
                    .build();
            verifiedUrl = cacheServer.getProxyUrl(url);
        }

        playerView = findViewById(R.id.video_player_view);
        playerView.setUrl(verifiedUrl);
        StandardVideoController controller = new StandardVideoController(this);
        controller.addDefaultControlComponent("推特视频", false);
        controller.setEnableInNormal(true); //竖屏也开启手势操作
        playerView.setVideoController(controller); //设置控制器
        playerView.start();

        toolbar = findViewById(R.id.video_viewer_toolbar);
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
                    saveVideo();
                }
                return true;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playerView.pause();
        playerView.release();
    }

    @Override
    protected void onPause() {
        super.onPause();
        playerView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        playerView.resume();
    }

    @Override
    public void onBackPressed() {
        if (!playerView.onBackPressed()) {
            super.onBackPressed();
        }
    }

    public void saveVideo() {
        String result = "成功";
        if(cacheServer != null && cacheServer.isCached(url)) {
            Path source = cacheServer.getCacheFile(url).toPath();
            Path target = videoFile.toPath();
            try {
                Files.copy(source, target);
            } catch (IOException e) {
                e.printStackTrace();
                result = "失败";
            }
        }
        else if(!video.saveToFile(this))
            result = "失败";
        Toast.makeText(this, "保存"+result, Toast.LENGTH_SHORT).show();
    }
}