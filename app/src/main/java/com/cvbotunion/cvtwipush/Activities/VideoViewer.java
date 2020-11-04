package com.cvbotunion.cvtwipush.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
// TODO 考虑修改为Fragment
public class VideoViewer extends AppCompatActivity {
    public static final File videoCacheDir = new File(TwiPush.getContext().getCacheDir(), "video-cache");  // 视频缓存目录
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
                    .cacheDirectory(videoCacheDir)
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
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if(itemId==R.id.save_menu_item) {
                saveVideo();
            } else if(itemId==R.id.share_menu_item) {
                shareVideo();
            }
            return true;
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
            if(!videoFile.getParentFile().exists()) videoFile.getParentFile().mkdirs();
            try {
                Files.copy(source, target);
                this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + videoFile.getAbsolutePath())));
            } catch (IOException e) {
                e.printStackTrace();
                result = "失败";
            }
        }
        else if(!video.saveToFile(this))
            result = "失败";
        Toast.makeText(this, "保存"+result, Toast.LENGTH_SHORT).show();
    }

    public void shareVideo() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, url);
        shareIntent.setType("text/plain");
        startActivity(Intent.createChooser(shareIntent, "分享视频"));
    }
}