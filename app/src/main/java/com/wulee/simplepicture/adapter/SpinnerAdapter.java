package com.wulee.simplepicture.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wulee.simplepicture.R;

import java.util.List;

/**
 * create by  wulee   2018/9/11 09:59
 * desc:
 */
public class SpinnerAdapter extends BaseAdapter {
    private List<String> mList;
    private Context mContext;

    public SpinnerAdapter(Context pContext, List<String> pList) {
        this.mContext = pContext;
        this.mList = pList;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater _LayoutInflater = LayoutInflater.from(mContext);
        convertView = _LayoutInflater.inflate(R.layout.spinner_item, null);
        if (convertView != null) {
            TextView textview = convertView.findViewById(R.id.textview);
            textview.setText(mList.get(position));
        }
        return convertView;
    }

}
