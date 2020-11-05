package com.cvbotunion.cvtwipush.Activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cvbotunion.cvtwipush.R;
import com.cvbotunion.cvtwipush.Utils.LoginRunnable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    private EditText usernameText;
    private EditText passwordText;
    public TextView messageView;
    public ProgressBar progressBar;
    private Button loginButton;

    public ExecutorService pool;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        pool = Executors.newCachedThreadPool();
        initView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        pool.shutdownNow();
    }

    private void initView() {
        usernameText = findViewById(R.id.login_username_text);
        passwordText = findViewById(R.id.login_password_text);
        messageView = findViewById(R.id.login_message_view);
        progressBar = findViewById(R.id.login_progressBar);
        loginButton = findViewById(R.id.login_btn);

        messageView.setText("");
        passwordText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length()!=0) {
                    loginButton.setEnabled(true);
                    loginButton.setBackgroundColor(getColor(R.color.colorPrimary));
                } else {
                    loginButton.setEnabled(false);
                    loginButton.setBackgroundColor(getColor(R.color.colorGray));
                }
            }
        });
        loginButton.setEnabled(false);
        loginButton.setBackgroundColor(getColor(R.color.colorGray));
        loginButton.setOnClickListener(v -> {
            messageView.setText("");
            final String username = usernameText.getText().toString();
            final String password = passwordText.getText().toString();
            if(username.length()==0 || username.contains(" ")) {
                messageView.setText("用户名为空或包含空格");
            } else {
                progressBar.setVisibility(View.VISIBLE);
                pool.execute(new LoginRunnable(this, username, password));
            }
        });
    }
}
