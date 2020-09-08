package com.cvbotunion.cvtwipush.CustomViews;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cvbotunion.cvtwipush.R;

public class GroupPopupWindow extends PopupWindow {
    private View view;
    private TextView username;
    private ImageView avatar;
    private ImageButton exit;
    private RecyclerView groupList;

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
        setBackgroundDrawable(new BitmapDrawable(context.getResources(), (Bitmap) null));
        setOutsideTouchable(true);
    }

    public GroupPopupWindow(Context context) {
        super(context);
        initView(context);
    }

    public GroupPopupWindow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public GroupPopupWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public GroupPopupWindow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    public GroupPopupWindow(View contentView, int width, int height, boolean focusable) {
        super(contentView, width, height, focusable);
        view = contentView;
        initView(contentView.getContext());
    }

    public GroupPopupWindow(View contentView, int width, int height) {
        super(contentView, width, height);
        view = contentView;
        initView(contentView.getContext());
    }

    public GroupPopupWindow() {
        super();
    }

    public GroupPopupWindow(View contentView) {
        super(contentView);
        view = contentView;
        initView(contentView.getContext());
    }

    public GroupPopupWindow(int width, int height) {
        super(width, height);
    }
}
