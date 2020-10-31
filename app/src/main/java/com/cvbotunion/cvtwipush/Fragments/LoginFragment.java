package com.cvbotunion.cvtwipush.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cvbotunion.cvtwipush.Activities.Timeline;
import com.cvbotunion.cvtwipush.Model.User;
import com.cvbotunion.cvtwipush.R;
import com.cvbotunion.cvtwipush.Utils.LoginCallable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LoginFragment extends Fragment {
    private EditText usernameText;
    private EditText passwordText;
    private TextView messageView;
    private ProgressBar progressBar;
    private Button loginButton;
    private Timeline parentActivity;

    public ExecutorService pool;
    public Future<User> future;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pool = Executors.newCachedThreadPool();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_fragment, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.parentActivity = (Timeline)getActivity();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        pool.shutdownNow();
    }

    private void initView(View view) {
        usernameText = view.findViewById(R.id.login_username_text);
        passwordText = view.findViewById(R.id.login_password_text);
        messageView = view.findViewById(R.id.login_message_view);
        progressBar = view.findViewById(R.id.login_progressBar);
        loginButton = view.findViewById(R.id.login_btn);

        messageView.setText("");
        passwordText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length()!=0) {
                    loginButton.setClickable(true);
                    loginButton.setBackgroundColor(getContext().getColor(R.color.colorPrimary));
                } else {
                    loginButton.setClickable(false);
                    loginButton.setBackgroundColor(getContext().getColor(R.color.colorGray));
                }
            }
        });
        loginButton.setClickable(false);
        loginButton.setBackgroundColor(getContext().getColor(R.color.colorGray));
        loginButton.setOnClickListener(v -> {
            loginButton.setClickable(false);
            final String username = usernameText.getText().toString();
            final String password = passwordText.getText().toString();
            if(username.length()==0 || username.contains(" ")) {
                messageView.setText("用户名为空或包含空格");
            } else {
                future = pool.submit(new LoginCallable(username, password));
                progressBar.setVisibility(View.VISIBLE);
                try {
                    User user = future.get();
                    parentActivity.setCurrentUser(user);
                    // TODO 在适当时机保存(可能在Timeline中更合适)：user.writeToDisk();
                    parentActivity.onBackPressed();
                } catch (Exception e) {
                    Log.e("LoginFragment.onLoginButtonClicked", e.toString());
                    if(e.getMessage().equals("403")) {
                        messageView.setText("用户名或密码错误");
                    } else {
                        messageView.setText("登录失败，请检查网络连接");
                    }
                }
                progressBar.setVisibility(View.GONE);
            }
            loginButton.setClickable(true);
        });
    }
}
