package com.wulee.simplepicture.view;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.wulee.simplepicture.R;

/**
 * create by  wulee   2018/9/7 13:45
 * desc:
 */
public class SwitchTab implements View.OnTouchListener {
    private View mView;
    private TextView mTvTitle;
    private ImageView mImgDivider;

    private int mPosition;

    private SwitchTabBar.OnSwitchTabListener listener;

    private Context mContext;

    public SwitchTab(Context context) {
        mContext = context;
        mView = LayoutInflater.from(context).inflate(R.layout.text_tab, null);
        mTvTitle = mView.findViewById(R.id.text_tab_title);
        mImgDivider = mView.findViewById(R.id.text_tab_divider);

        int mPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                2, context.getResources().getDisplayMetrics());
        mTvTitle.setPadding(mPadding, 0, mPadding, 0);

        Log.e("SwitchTab(..)____", "创建TAG");
        mView.setOnTouchListener(this);
    }

    public void setTabTitle(String title) {
        mTvTitle.setText(title);
    }

    protected View getView() {
        if (mView != null) {
            return mView;
        }
        return null;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                return true;

            case MotionEvent.ACTION_MOVE:

                return true;

            case MotionEvent.ACTION_UP:
                //选中
                setTabPress();

                if (listener != null) {
                    listener.onSelectTab(this);
                }

                return true;

        }
        return false;
    }

    /**
     * 设置tabView的正常效果
     */
    protected void setTabNormal() {
        mTvTitle.setTextColor(ContextCompat.getColor(mContext,R.color.tv_color_2));
        mImgDivider.setBackgroundColor(0x00000000);
    }

    /**
     * 设置tabView的按下效果
     */
    protected void setTabPress() {
        mTvTitle.setTextColor(ContextCompat.getColor(mContext,R.color.white));
        mImgDivider.setBackgroundColor(0xFFF08080);
    }

    protected void setPosition(int position) {
        this.mPosition = position;
    }

    public int getPosition() {
        return mPosition;
    }

    /**
     * 获取显示字符区域的宽度
     * @return
     */
    private int getTextLenght() {
        String textString = mTvTitle.getText().toString();
        Rect bounds = new Rect();
        Paint textPaint = mTvTitle.getPaint();
        textPaint.getTextBounds(textString, 0, textString.length(), bounds);
        return bounds.width();
    }

    protected int getTabWidth() {
        return getTextLenght();
    }

    public void setSwitchTabListener(SwitchTabBar.OnSwitchTabListener listener) {
        if (listener != null) {
            this.listener = listener;
        }
    }

}
