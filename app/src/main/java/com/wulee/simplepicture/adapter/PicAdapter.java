package com.wulee.simplepicture.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.wulee.simplepicture.R;
import com.wulee.simplepicture.bean.StickFigureImgObj;
import com.wulee.simplepicture.ui.LoginActivity;
import com.wulee.simplepicture.utils.ImageUtil;
import com.wulee.simplepicture.utils.NoFastClickUtils;
import com.wulee.simplepicture.utils.OtherUtil;
import com.wulee.simplepicture.utils.UIUtils;
import com.wx.goodview.GoodView;

import java.util.ArrayList;
import java.util.HashMap;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;


public class PicAdapter extends BaseQuickAdapter<StickFigureImgObj,BaseViewHolder> {

    private Context context;
    private HashMap<String,Integer> likeNumMap = new HashMap<>();
    private boolean mIsLikeOpt = true; //是否可以点赞

    public PicAdapter(int layoutResId, ArrayList<StickFigureImgObj> dataList, Context context) {
        super(layoutResId, dataList);
        this.context = context;
    }

    public void setLikeOpt(boolean likeOpt) {
        mIsLikeOpt = likeOpt;
        notifyDataSetChanged();
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder,final StickFigureImgObj pic) {

        ImageView ivPic = baseViewHolder.getView(R.id.iv_pic);
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) ivPic.getLayoutParams();
        int itemWidth = (UIUtils.getScreenWidthAndHeight(context)[0]-UIUtils.dip2px(10)*2)/3;
        rlp.width = itemWidth;
        rlp.height= itemWidth;
        ivPic.setLayoutParams(rlp);

        if(null != pic.getImageGroup() && pic.getImageGroup().length>0){
            ImageUtil.setDefaultImageView(ivPic,pic.getImageGroup()[0],R.mipmap.bg_pic_def_rect,context);
        }

        ImageView ivLike = baseViewHolder.getView(R.id.iv_like);
        final TextView tvLikeNum = baseViewHolder.getView(R.id.tv_like_num);
        LinearLayout ll = baseViewHolder.getView(R.id.ll_likes);
        tvLikeNum.setText(pic.getLikeNum()+"");

        if(mIsLikeOpt){
            ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!OtherUtil.hasLogin()) {
                        context.startActivity(new Intent(context, LoginActivity.class));
                        return;
                    }
                    int num = pic.getLikeNum();
                    addLikes(pic.getObjectId(),num,tvLikeNum);
                }
            });
        }
    }


    /**
     * 点赞
     */
    private  void addLikes(final String objId, int likeNum,final TextView tv){
        if (NoFastClickUtils.isFastClick()) {
            Toast.makeText(context, "点击过快", Toast.LENGTH_SHORT).show();
            return;
        }
        StickFigureImgObj imgObj = new StickFigureImgObj();
        imgObj.setObjectId(objId);
        int num = ++likeNum;
        if(likeNumMap.containsKey(objId)){
             num = likeNumMap.get(objId);
             num ++ ;
            likeNumMap.remove(objId);
        }
        imgObj.setLikeNum(num);
        likeNumMap.put(objId,num);
        imgObj.update(new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if(e == null){
                    tv.setText(likeNumMap.get(objId)+"");
                    //EventBus.getDefault().post(new RefreshEvent());
                    GoodView goodView = new GoodView(context);
                    goodView.setTextInfo("+1",Color.RED,50);
                    goodView.show(tv);
                }else{
                    Toast.makeText(context, "点赞失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
