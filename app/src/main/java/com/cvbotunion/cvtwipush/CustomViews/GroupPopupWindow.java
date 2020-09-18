package com.cvbotunion.cvtwipush.CustomViews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cvbotunion.cvtwipush.Adapters.GroupRecyclerAdapter;
import com.cvbotunion.cvtwipush.Model.RTGroup;
import com.cvbotunion.cvtwipush.Model.User;
import com.cvbotunion.cvtwipush.R;

import java.util.ArrayList;

public class GroupPopupWindow extends PopupWindow {
    private View view;
    private TextView username;
    private ImageView avatar;
    private ImageButton exit;
    private RecyclerView groupList;
    public GroupRecyclerAdapter grAdapter;
    public User user;
    public String currentGroup;

    public void initView(final Context context){
        username = view.findViewById(R.id.user_name_text_view);
        avatar = view.findViewById(R.id.user_avatar);
        groupList = view.findViewById(R.id.group_list_recycler_view);
        exit = view.findViewById(R.id.exit_btn);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        setOutsideTouchable(true);

        if(user != null){
            if(user.name != null) {
                username.setText(user.name);
            }
            if(user.avatar != null){
                avatar.setImageBitmap(user.avatar);
            }
        }
        groupList.setLayoutManager(new LinearLayoutManager(context));
        grAdapter = new GroupRecyclerAdapter(context,this,user.jobs,currentGroup);
        groupList.setAdapter(grAdapter);
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
}
