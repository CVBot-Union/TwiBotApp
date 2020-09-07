package com.cvbotunion.cvtwipush.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.Manifest;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListPopupWindow;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.cvbotunion.cvtwipush.CustomViews.GroupPopupWindow;
import com.cvbotunion.cvtwipush.Model.TwitterMedia;
import com.cvbotunion.cvtwipush.Model.TwitterStatus;
import com.cvbotunion.cvtwipush.Model.TwitterUser;
import com.cvbotunion.cvtwipush.Utils.NetworkStateReceiver;
import com.cvbotunion.cvtwipush.R;
import com.cvbotunion.cvtwipush.Utils.RefreshTask;
import com.cvbotunion.cvtwipush.Adapters.TweetCardAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class TweetList extends AppCompatActivity {

    private CoordinatorLayout coordinatorLayout;
    private RecyclerView tweetListView;
    private TweetCardAdapter tAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NetworkStateReceiver networkStateReceiver;
    private ChipGroup chipGroup;
    private Chip all;

    private MaterialToolbar mdToolbar;
    private TextView title;

    private ArrayList<TwitterStatus> dataSet = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_list);

        //动态权限申请
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        initBackground();

        //start 模拟数据
        TwitterUser user = new TwitterUser("1","sb","sbsb","SB","http://101.200.184.98:8080/media/MO4FkO4N_400x400.jpg");
        TwitterMedia media = new TwitterMedia("1","http://101.200.184.98:8080/media/MO4FkO4N_400x400.jpg",TwitterMedia.IMAGE,"http://101.200.184.98:8080/media/MO4FkO4N_400x400.jpg");
        ArrayList<TwitterMedia> newList = new ArrayList<>();
        newList.add(media);
        newList.add(media);
        newList.add(media);
        TwitterStatus status = new TwitterStatus("11:14","1","测试",user,newList);

        dataSet.add(status);
        dataSet.add(status);
        dataSet.add(status);
        dataSet.add(status);

        String groupName = "蔷薇之心";

        ArrayList<String> userList = new ArrayList<>();
        userList.add("相羽あいな");
        userList.add("工藤晴香");
        userList.add("中島由貴");
        userList.add("櫻川めぐ");
        userList.add("志崎樺音");
        //end 模拟数据

        initView();
        initRecyclerView();


        title.setText(groupName);//待更改
        mdToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int menuID = item.getItemId();
                if (menuID == R.id.group_menu_item){
                    View view = getLayoutInflater().inflate(R.layout.group_switch_menu,null);
                    GroupPopupWindow popupWindow = new GroupPopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,true);
                    popupWindow.showAsDropDown(findViewById(R.id.group_menu_item),0, 0, Gravity.END);
                    dimBehind(popupWindow);
                }
                return true;
            }
        });

        final TwitterStatus newStatus = status;
        for(String twitterUser:userList){//待更改
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_view,chipGroup,false);
            chip.setText(twitterUser);
            chip.setId(ViewCompat.generateViewId());
            chipGroup.addView(chip);
        }

        chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                dataSet = new ArrayList<>();
                dataSet.add(newStatus);
                initRecyclerView();
            }
        });

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                netRefresh(chipGroup.getCheckedChipId());
                initRecyclerView();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkStateReceiver);
    }

    private void initRecyclerView(){
        layoutManager = new LinearLayoutManager(this){
            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                try {
                    super.onLayoutChildren(recycler, state);
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }

            }
        };

        tweetListView.setLayoutManager(layoutManager);
        tAdapter = new TweetCardAdapter(dataSet,this);
        tweetListView.setAdapter(tAdapter);
        ((SimpleItemAnimator) tweetListView.getItemAnimator()).setSupportsChangeAnimations(false);
    }

    private void initView(){
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.tweet_list_parent_view);
        tweetListView = (RecyclerView) findViewById(R.id.tweet_list_recycler_view);
        mdToolbar = (MaterialToolbar) findViewById(R.id.top_app_bar);
        title = (TextView) findViewById(R.id.title);
        chipGroup = (ChipGroup) findViewById(R.id.group_chip_group);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
    }

    private void initBackground() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        //自定义广播
        //intentFilter.addAction("android.net.conn.CONNECTIVITY_STATE");
        networkStateReceiver = new NetworkStateReceiver();
        registerReceiver(networkStateReceiver, intentFilter);
        //数据库初始化，暂时不要取消注释，避免在手机里倒垃圾
        //LitePalDB litePalDB = new LitePalDB("twitterData", 1);
        //litePalDB.addClassName(DBTwitterStatus.class.getName());
        //litePalDB.addClassName(DBTwitterUser.class.getName());
        //litePalDB.addClassName(DBTwitterMedia.class.getName());
        //LitePal.use(litePalDB);
        //SQLiteDatabase db = LitePal.getDatabase();
    }

    private void netRefresh(int checkedId) {
        RefreshTask task = new RefreshTask(coordinatorLayout, swipeRefreshLayout, dataSet);
        task.setCheckedId(checkedId);
        task.execute();
    }

    public static void dimBehind(PopupWindow popupWindow) {
        View container;
        if (popupWindow.getBackground() == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                container = (View) popupWindow.getContentView().getParent();
            } else {
                container = popupWindow.getContentView();
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                container = (View) popupWindow.getContentView().getParent().getParent();
            } else {
                container = (View) popupWindow.getContentView().getParent();
            }
        }
        Context context = popupWindow.getContentView().getContext();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.3f;
        wm.updateViewLayout(container, p);
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}