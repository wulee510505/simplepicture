package com.wulee.simplepicture.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.wulee.simplepicture.R;
import com.wulee.simplepicture.base.BaseActivity;
import com.wulee.simplepicture.bean.UserInfo;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

import static com.wulee.simplepicture.App.mACache;

/**
 * Created by wulee on 2017/11/7 10:32
 */

public class LoginActivity extends BaseActivity {

    @BindView(R.id.et_mobile)
    EditText etMobile;
    @BindView(R.id.et_pwd)
    EditText etPwd;
    @BindView(R.id.btn_login)
    Button btnLogin;
    @BindView(R.id.tv_forget_pwd)
    TextView tvForgetPwd;
    @BindView(R.id.tv_regist)
    TextView tvRegist;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        initView();
    }

    private void initView() {

    }

    private void doLogin(final String mobile, String pwd) {
        UserInfo user = new UserInfo();
        user.setUsername(mobile);
        user.setPassword(pwd);
        showProgressDialog(true);
        user.login(new SaveListener<UserInfo>() {
            @Override
            public void done(UserInfo user, BmobException e) {
                stopProgressDialog();
                if (e == null) {
                    mACache.put("has_login", "yes");
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    LoginActivity.this.finish();
                } else {
                    showToast("登录失败:" + e.getMessage());
                }
            }
        });
    }

    @OnClick({R.id.btn_login, R.id.tv_forget_pwd, R.id.tv_regist})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                String mobile = etMobile.getText().toString().trim();
                String pwd = etPwd.getText().toString().trim();
                if (TextUtils.isEmpty(mobile)) {
                    Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(pwd)) {
                    Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                doLogin(mobile, pwd);
                break;
            case R.id.tv_forget_pwd:
                break;
            case R.id.tv_regist:
                startActivity(new Intent(this, RegistActivity.class));
                break;
        }
    }
}
