package com.wulee.simplepicture.adapter;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.wulee.simplepicture.R;
import com.wulee.simplepicture.utils.ImageUtil;
import com.wulee.simplepicture.utils.UIUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * create by  wulee   2018/9/7 16:48
 * desc:
 */
public class PicFileAdapter  extends BaseQuickAdapter<String,BaseViewHolder> {

    private Context context;
    private Map<String,Boolean> selFileMap;


    public PicFileAdapter(int layoutResId, List<String> dataList, Context context) {
        super(layoutResId, dataList);
        this.context = context;
        selFileMap = new HashMap<>();
        if(dataList != null && dataList.size()>0){
            for (int i = 0; i <dataList.size() ; i++) {
                selFileMap.put(dataList.get(i),false);
            }
        }
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder,final String filepath) {



        ImageView ivPic = baseViewHolder.getView(R.id.iv_pic);
        ImageUtil.setDefaultImageView(ivPic, filepath, R.mipmap.bg_pic_def_rect, context);

        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) ivPic.getLayoutParams();
        int itemWidth = (UIUtils.getScreenWidthAndHeight(context)[0]-UIUtils.dip2px(10)*2)/3;
        rlp.width = itemWidth;
        rlp.height= itemWidth;
        ivPic.setLayoutParams(rlp);

        CheckBox checkBox = baseViewHolder.getView(R.id.checkbox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked == true) {
                    selFileMap.put(filepath, true);
                } else {
                    selFileMap.remove(filepath);
                }
            }
        });
        if (selFileMap != null && selFileMap.containsKey(filepath)) {
            checkBox.setChecked(true);
        } else {
            checkBox.setChecked(false);
        }
    }

    public List<String> getSelFilePath(){
        List<String> selFilePath = null;
        if(selFileMap != null && selFileMap.size()>0){
            selFilePath = new ArrayList<>();
            Iterator<Map.Entry<String, Boolean>> entries = selFileMap.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, Boolean> entry = entries.next();
                System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
                if(entry.getValue()){
                    selFilePath.add(entry.getKey());
                }
            }
        }
        return selFilePath;
    }

    public void removeSelFilePath(String path){
        if(selFileMap != null && selFileMap.size()>0){
           if(selFileMap.containsKey(path)){
               selFileMap.remove(path);
           }
        }
    }

}