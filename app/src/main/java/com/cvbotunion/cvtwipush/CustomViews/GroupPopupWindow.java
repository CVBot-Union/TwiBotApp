package com.cvbotunion.cvtwipush.CustomViews;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cvbotunion.cvtwipush.Adapters.GroupRecyclerAdapter;
import com.cvbotunion.cvtwipush.Model.User;
import com.cvbotunion.cvtwipush.R;

public class GroupPopupWindow extends PopupWindow {
    private View view;
    private TextView usernameView;
    private ImageView avatarView;
    private ImageButton exit;
    private RecyclerView groupListView;
    public GroupRecyclerAdapter grAdapter;
    public User user;
    public String currentGroup;

    public void initView(final Context context){
        usernameView = view.findViewById(R.id.user_name_text_view);
        avatarView = view.findViewById(R.id.user_avatar);
        groupListView = view.findViewById(R.id.group_list_recycler_view);
        exit = view.findViewById(R.id.exit_btn);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        setOutsideTouchable(true);

        if(user != null){
            usernameView.setText(user.name);
            if(user.avatar != null){
                avatarView.setImageBitmap(user.avatar);
            }  // TODO 下载用户头像并刷新UI
        }
        groupListView.setLayoutManager(new LinearLayoutManager(context));
        grAdapter = new GroupRecyclerAdapter(context,this,user.jobs,currentGroup);
        groupListView.setAdapter(grAdapter);
    }

    public GroupPopupWindow(Context context, User user,String currentGroup) {
        super(context);
        this.user = user;
        this.currentGroup = currentGroup;
        initView(context);
    }

    public GroupPopupWindow(Context context, AttributeSet attrs, int defStyleAttr, User user,String currentGroup) {
        super(context, attrs, defStyleAttr);
        this.user = user;
        this.currentGroup = currentGroup;
        initView(context);
    }

    public GroupPopupWindow(Context context, AttributeSet attrs, User user,String currentGroup) {
        super(context, attrs);
        this.user = user;
        this.currentGroup = currentGroup;
        initView(context);
    }

    public GroupPopupWindow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, User user,String currentGroup) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.user = user;
        this.currentGroup = currentGroup;
        initView(context);
    }

    public GroupPopupWindow(View contentView, int width, int height, boolean focusable, User user,String currentGroup) {
        super(contentView, width, height, focusable);
        view = contentView;
        this.user = user;
        this.currentGroup = currentGroup;
        initView(contentView.getContext());
    }

    public GroupPopupWindow(View contentView, int width, int height, User user,String currentGroup) {
        super(contentView, width, height);
        view = contentView;
        this.user = user;
        this.currentGroup = currentGroup;
        initView(contentView.getContext());
    }

    public GroupPopupWindow() {
        super();
    }

    public GroupPopupWindow(View contentView, User user,String currentGroup) {
        super(contentView);
        view = contentView;
        this.user = user;
        this.currentGroup = currentGroup;
        initView(contentView.getContext());
    }

    public GroupPopupWindow(int width, int height, User user,String currentGroup) {
        super(width, height);
        this.user = user;
        this.currentGroup = currentGroup;
    }

    public void dimBehind() {
        View container;
        if (getBackground() == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                container = (View) getContentView().getParent();
            } else {
                container = getContentView();
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                container = (View) getContentView().getParent().getParent();
            } else {
                container = (View) getContentView().getParent();
            }
        }
        Context context = getContentView().getContext();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.3f;
        if(wm != null) {
            wm.updateViewLayout(container, p);
        }
        setTouchable(true);
        setFocusable(true);
        setOutsideTouchable(true);
        update();
    }
}
