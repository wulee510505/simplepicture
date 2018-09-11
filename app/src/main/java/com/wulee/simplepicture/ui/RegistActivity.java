package com.wulee.simplepicture.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.wulee.simplepicture.R;
import com.wulee.simplepicture.base.BaseActivity;
import com.wulee.simplepicture.bean.UserInfo;
import com.wulee.simplepicture.utils.OtherUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;

/**
 * Created by wulee on 2017/11/7 09:57
 */

public class RegistActivity extends BaseActivity {

    @BindView(R.id.et_mobile)
    EditText etMobile;
    @BindView(R.id.et_pwd)
    EditText etPwd;
    @BindView(R.id.btn_pincode)
    Button btnPincode;
    @BindView(R.id.et_pincode)
    EditText etPincode;
    @BindView(R.id.btn_regist)
    Button btnRegist;


    private String mobile, authCode, pwd;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);
        ButterKnife.bind(this);

        initView();
    }


    private void initView() {

    }




    private void doRegist(String mobile, String pwd) {
        UserInfo piInfo = new UserInfo();
        piInfo.setMobilePhoneNumber(mobile);
        piInfo.setUsername(mobile);
        piInfo.setPassword(pwd);
        showProgressDialog(true);
        piInfo.signUp(new SaveListener<UserInfo>() {
            @Override
            public void done(UserInfo user, BmobException e) {
                stopProgressDialog();
                if (e == null) {
                    Toast.makeText(RegistActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                    RegistActivity.this.finish();
                } else {
                    showToast("注册失败:" + e.getMessage());
                }
            }
        });
    }

    @OnClick({R.id.btn_pincode, R.id.btn_regist})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_pincode:
                mobile = etMobile.getText().toString().trim();
                if (TextUtils.isEmpty(mobile)) {
                    Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show();
                    return;
                }
                //获取短信验证码
                BmobSMS.requestSMSCode(mobile, "regist", new QueryListener<Integer>() {
                    @Override
                    public void done(Integer integer, BmobException e) {
                        if (e == null) {
                            showToast("发送短信成功");
                        } else {
                            showToast("code =" + e.getErrorCode() + "\nmsg = " + e.getLocalizedMessage());
                        }
                    }
                });
                break;
            case R.id.btn_regist:
                mobile = etMobile.getText().toString().trim();
                pwd = etPwd.getText().toString().trim();
                authCode = etPincode.getText().toString().trim();

                if (TextUtils.isEmpty(mobile)) {
                    Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!OtherUtil.isMobile(mobile)) {
                    Toast.makeText(this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(pwd)) {
                    Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }
               /* if (!OtherUtil.isPassword(pwd)) {
                    Toast.makeText(this, "密码由6~16位数字和英文字母组成", Toast.LENGTH_SHORT).show();
                    return;
                }*/

              /* if(TextUtils.isEmpty(authCode)){
                    Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
                    return;
                }*/
                doRegist(mobile, pwd);
                break;
        }
    }
}
