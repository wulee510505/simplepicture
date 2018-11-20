package com.wulee.simplepicture.ui;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.wulee.simplepicture.R;
import com.wulee.simplepicture.base.BaseActivity;
import com.wulee.simplepicture.base.Constant;
import com.wulee.simplepicture.bean.UserInfo;
import com.wulee.simplepicture.ui.fragment.FragHome;
import com.wulee.simplepicture.ui.fragment.FragMine;
import com.wulee.simplepicture.utils.AppUtils;
import com.wulee.simplepicture.view.BottomNavigationViewEx;
import com.wulee.simplepicture.view.NoScroViewPager;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.update.BmobUpdateAgent;

import static cn.bmob.v3.BmobUser.getCurrentUser;
import static com.wulee.simplepicture.App.mACache;

public class MainActivity extends BaseActivity {
    private BottomNavigationViewEx bnve;
    private VpAdapter adapter;
    private List<Fragment> fragments;
    private NoScroViewPager viewPager;
    private FloatingActionButton floatingActionButton;
    private String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        initBNVE();
        initEvent();


        AndPermission
                .with(this)
                .runtime()
                .permission(permissions)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissions) {
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(@NonNull List<String> permissions) {

                    }
                }).start();
    }


    @Override
    protected void onResume() {
        super.onResume();

        long lastUpdateCurrPersonInfoTime = 0L;
        try {
            String timeStr = mACache.getAsString(Constant.KEY_LAST_UPDATE_CURR_USERINFO_TIME);
            if(!TextUtils.isEmpty(timeStr)){
                lastUpdateCurrPersonInfoTime = Long.parseLong(timeStr);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        long interal = System.currentTimeMillis() - lastUpdateCurrPersonInfoTime;
        if(interal > Constant.UPDATE_CURR_USERINFO_INTERVAL){
            final UserInfo userInfo = getCurrentUser(UserInfo.class);
            if(null == userInfo)
                return;
            userInfo.update(new UpdateListener() {
                @Override
                public void done(BmobException e) {
                    if(e == null){
                        mACache.put(Constant.KEY_LAST_UPDATE_CURR_USERINFO_TIME,String.valueOf(System.currentTimeMillis()));
                    }else{
                        if(e.getErrorCode() == 206){
                            toast("您的账号在其他地方登录，请重新登录");
                            mACache.put("has_login", "no");
                            AppUtils.AppExit(MainActivity.this);
                            UserInfo.logOut();
                            startActivity(new Intent(MainActivity.this,LoginActivity.class));
                        }
                    }
                }
            });
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        long lastCheckUpdateTime = 0L;
        try {
            String timeStr = mACache.getAsString(Constant.KEY_LAST_CHECK_UPDATE_TIME);
            if(!TextUtils.isEmpty(timeStr)){
                lastCheckUpdateTime = Long.parseLong(timeStr);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        long interal = System.currentTimeMillis() - lastCheckUpdateTime;
        if(interal > Constant.CHECK_UPDATE_INTERVAL){
            BmobUpdateAgent.setUpdateOnlyWifi(true);
            checkUpdate();
            mACache.put(Constant.KEY_LAST_CHECK_UPDATE_TIME,String.valueOf(System.currentTimeMillis()));
        }
    }

    /**
     * init BottomNavigationViewEx envent
     */
    private void initEvent() {
        bnve.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            private int previousPosition = -1;

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int position = 0;
                switch (item.getItemId()) {
                    case R.id.menu_main:
                        position = 0;
                        break;
                    case R.id.menu_me:
                        position = 2;
                        break;
                    case R.id.menu_empty: {
                        position = 1;
                        //此处return false且在FloatingActionButton没有自定义点击事件时 会屏蔽点击事件
                        //return false;
                    }
                    default:
                        break;
                }
                if (previousPosition != position) {
                    viewPager.setCurrentItem(position, false);
                    previousPosition = position;
                }

                return true;
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (1 == position) {
                    floatingActionButton.setImageResource(R.mipmap.icon_add);
                } else {
                    floatingActionButton.setImageResource(R.mipmap.icon_add);
                }
               /* // 1 is center 此段结合屏蔽FloatingActionButton点击事件的情况使用
                  //在viewPage滑动的时候 跳过最中间的page也
                if (position >= 1) {
                    position++;
                }*/
                bnve.setCurrentItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        /**
         * fab 点击事件结合OnNavigationItemSelectedListener中return false使用
         */
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,DrawPicActivity.class));
                overridePendingTransition(R.anim.push_bottom_in,0);
            }
        });

    }


    private void initView() {
        floatingActionButton = findViewById(R.id.fab);
        viewPager = findViewById(R.id.vp);
        bnve = findViewById(R.id.bnve);
    }

    /**
     * create fragments
     */
    private void initData() {
        fragments = new ArrayList<>(3);
        FragHome homeFragment = new FragHome();
        Bundle bundle = new Bundle();
        bundle.putString("title", "首页");
        homeFragment.setArguments(bundle);


        FragMine meFragment = new FragMine();
        bundle = new Bundle();
        bundle.putString("title", "我的");
        meFragment.setArguments(bundle);


        fragments.add(homeFragment);
        fragments.add(meFragment);
    }

    /**
     * init BottomNavigationViewEx
     */
    private void initBNVE() {

        bnve.enableAnimation(false);
        bnve.enableShiftingMode(false);
        bnve.enableItemShiftingMode(false);

        adapter = new VpAdapter(getSupportFragmentManager(), fragments);
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(adapter);

    }

    /**
     * view pager adapter
     */
    private static class VpAdapter extends FragmentPagerAdapter {
        private List<Fragment> data;

        public VpAdapter(FragmentManager fm, List<Fragment> data) {
            super(fm);
            this.data = data;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Fragment getItem(int position) {
            return data.get(position);
        }
    }


    private void checkUpdate(){
        AndPermission
                .with(this)
                .runtime()
                .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        BmobUpdateAgent.update(MainActivity.this);
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {

                    }
                })
                .start();
    }
}
