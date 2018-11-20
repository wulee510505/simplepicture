package com.wulee.simplepicture.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * create by  wulee   2018/9/7 13:45
 * desc:
 */
public class SwitchTabBar extends RelativeLayout {
    private HorizontalScrollView mHzScrollView;
    private LinearLayout mLinearLayout;
    private List<SwitchTab> mTabs;

    //屏幕宽度, 高度
    private int mScreenWidth;
    private int mScreenHeight;
    //是否可滑动
    private boolean isScroll;
    //是否平板
    private boolean isTablet;

    private int mPosition;


    public SwitchTabBar(Context context) {
        this(context, null);
    }

    public SwitchTabBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwitchTabBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(Context context) {
        mHzScrollView = new HorizontalScrollView(context);
        mHzScrollView.setOverScrollMode(HorizontalScrollView.OVER_SCROLL_NEVER);
        mHzScrollView.setHorizontalScrollBarEnabled(false);

        mLinearLayout = new LinearLayout(context);
        mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        mHzScrollView.addView(mLinearLayout);

        mTabs = new ArrayList<>();
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(dm);
        //获取屏幕宽高
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (this.getWidth() != 0 && mTabs.size() != 0) {
            notifyLayoyut();
        }
    }

    private void notifyLayoyut() {
        super.removeAllViews();
        mLinearLayout.removeAllViews();

        if (!isTablet) {
            //适配手机
            int minWidth = mScreenWidth / 4;

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            int tabTotalWidth = 0;

            for (int i = 0; i < mTabs.size(); i++) {
                View tabView = mTabs.get(i).getView();
                tabView.setMinimumWidth(minWidth);
                mLinearLayout.addView(tabView, params);

                tabTotalWidth += mTabs.get(i).getTabWidth();
            }

            if (tabTotalWidth > mScreenWidth) {
                isScroll = true;
            }

        } else {
            //适配平板,平板的屏幕比手机大
            //...
        }

        RelativeLayout.LayoutParams paramsScroll = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        this.addView(mHzScrollView, paramsScroll);

    }

    public void addTab(SwitchTab tab) {
        tab.setPosition(mTabs.size());
        mTabs.add(tab);

    }

    /**
     * 设置选中Tab
     * @param position
     */
    public void setSelectItem(int position) {
        if (mTabs != null && mTabs.size() > 0) {
            if (position < mTabs.size()) {
                mTabs.get(position).setTabPress();
            }

            for (int c = mTabs.size() - 1; c >= 0; c--) {
                if (c != position) {
                    mTabs.get(c).setTabNormal();
                }
            }
        }
        scrollTo(position);
    }

    private void scrollTo(int position) {
        int totalWidth = 0;
        for (int i = 0; i < position; i++) {
            int width = mTabs.get(i).getTabWidth();

            totalWidth += width;
        }
        mHzScrollView.smoothScrollTo(totalWidth, 0);
    }

    public interface OnSwitchTabListener {
        void onSelectTab(SwitchTab tab);
    }



}
