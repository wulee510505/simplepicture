package com.wulee.simplepicture.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wulee.simplepicture.R;
import com.wulee.simplepicture.base.MainBaseFrag;
import com.wulee.simplepicture.view.NoScroViewPager;
import com.wulee.simplepicture.view.SwitchTab;
import com.wulee.simplepicture.view.SwitchTabBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * create by  wulee
 * desc:
 */

public class FragHome extends MainBaseFrag implements ViewPager.OnPageChangeListener {

    @BindView(R.id.viewpager)
    NoScroViewPager mViewPager;
    Unbinder unbinder;
    @BindView(R.id.switchbar)
    SwitchTabBar mSwitchTabBar;

    private MyFragmentPageAdapter pagerAdapter;
    private List<Fragment> mFragments;


    @Override
    protected boolean isLazyLoad() {
        return false;
    }

    @Override
    protected int getContentLayout() {
        return R.layout.fragment_home;
    }

    @Override
    protected void initView() {
        mFragments = new ArrayList<>();
        List<String> picTypes = Arrays.asList(getResources().getStringArray(R.array.array_pic_type));
        for (int i = 0; i < picTypes.size(); i++) {
            SwitchTab tab = new SwitchTab(getContext());
            tab.setTabTitle(picTypes.get(i));
            mSwitchTabBar.addTab(tab);
            tab.setSwitchTabListener(new SwitchTabBar.OnSwitchTabListener() {
                @Override
                public void onSelectTab(SwitchTab tab) {
                    mViewPager.setCurrentItem(tab.getPosition());
                }
            });
            FragHomeChild fragment = FragHomeChild.newInstance(i);
            mFragments.add(fragment);
        }

        pagerAdapter = new MyFragmentPageAdapter(getChildFragmentManager(), mFragments);
        mViewPager.setScroll(false);
        mViewPager.setOffscreenPageLimit(5);
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setAdapter(pagerAdapter);
        mSwitchTabBar.setSelectItem(0);
    }


    @Override
    protected void initData() {

    }

    @Override
    protected void initEvent() {

    }

    @Override
    public void onFragmentFirstSelected() {

    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mSwitchTabBar.setSelectItem(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    class MyFragmentPageAdapter extends FragmentPagerAdapter {
        List<Fragment> fragmentList;

        public MyFragmentPageAdapter(FragmentManager fm, List<Fragment> list) {
            super(fm);
            this.fragmentList = list;
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();

    }


}
