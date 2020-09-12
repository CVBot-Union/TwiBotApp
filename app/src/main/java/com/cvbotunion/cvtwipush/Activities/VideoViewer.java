package com.cvbotunion.cvtwipush.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.cvbotunion.cvtwipush.Model.TwitterMedia;
import com.cvbotunion.cvtwipush.R;
import com.dueeeke.videoplayer.ijk.IjkPlayer;
import com.dueeeke.videoplayer.player.VideoView;
import com.google.android.material.appbar.MaterialToolbar;

public class VideoViewer extends AppCompatActivity {
    VideoView<IjkPlayer> playerView;
    TwitterMedia video;
    MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_viewer);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().getDecorView().setSystemUiVisibility(0);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        assert bundle != null;
        video = bundle.getParcelable("videoMedia");

        playerView = findViewById(R.id.video_player_view);
        playerView.setUrl(video.url);

        toolbar = findViewById(R.id.video_viewer_toolbar);
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

    public void saveVideo(){
        String result = "成功";
        if(!video.saveToFile(this))
            result = "失败";
        Toast.makeText(this, "保存"+result, Toast.LENGTH_SHORT).show();
    }
}