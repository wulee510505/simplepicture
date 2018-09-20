package com.wulee.simplepicture.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;
import android.view.WindowManager;

import com.wulee.simplepicture.R;
import com.wulee.simplepicture.base.BaseActivity;
import com.wulee.simplepicture.moudle.whiteboard.fragment.WhiteBoardFragment;

/**
 * create by  wulee   2018/9/6 19:01
 * desc:画画界面
 */
public class DrawPicActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_draw_pic);

        //获取Fragment管理器
        FragmentTransaction ts = getSupportFragmentManager().beginTransaction();
        //获取WhiteBoardFragment实例
        WhiteBoardFragment whiteBoardFragment = WhiteBoardFragment.newInstance();
        //添加到界面中
        ts.add(R.id.rl_container, whiteBoardFragment, "wb").commit();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.push_bottom_out);
    }
}
