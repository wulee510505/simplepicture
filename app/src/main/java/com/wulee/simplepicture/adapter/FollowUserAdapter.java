package com.wulee.simplepicture.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.wulee.simplepicture.R;
import com.wulee.simplepicture.base.RefreshEvent;
import com.wulee.simplepicture.bean.UserInfo;
import com.wulee.simplepicture.utils.ImageUtil;
import com.wulee.simplepicture.utils.PhoneUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;


public class FollowUserAdapter extends BaseQuickAdapter<UserInfo,BaseViewHolder> {

    private Context context;
    private UserInfo mCurruser;

    public FollowUserAdapter(int layoutResId, ArrayList<UserInfo> dataList, Context context) {
        super(layoutResId, dataList);
        this.context = context;
        mCurruser = BmobUser.getCurrentUser(UserInfo.class);
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, UserInfo userInfo) {

        TextView tvName = baseViewHolder.getView(R.id.tv_user_name);
        if(!TextUtils.isEmpty(userInfo.getNickName())){
            tvName.setText(userInfo.getNickName());
        }else if(!TextUtils.isEmpty(userInfo.getUsername())){
            tvName.setText(PhoneUtil.encryptTelNum(userInfo.getUsername()));
        }
        ImageView ivAvatar = baseViewHolder.getView(R.id.iv_header);
        if(userInfo != null && !TextUtils.isEmpty(userInfo.getUserImage())) {
            ImageUtil.setCircleImageView(ivAvatar, userInfo.getUserImage(), R.mipmap.icon_user_avatar_def, context);
        }
        final TextView tvFollow = baseViewHolder.getView(R.id.tv_follow);
        tvFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserInfo userInfo = new UserInfo();
                userInfo.setObjectId(mCurruser.getObjectId());
                //将用户_User表中的follow字段移除，表明当前用户取消关注该用户
                BmobRelation relation = new BmobRelation();
                relation.remove(userInfo);
                //多对多关联指向`userInfo`的`follow`字段
                userInfo.setFollow(relation);
                userInfo.update(new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        if(e==null){
                            Log.i("bmob","取消关注成功");
                            EventBus.getDefault().post(new RefreshEvent());
                        }else{
                            Log.i("bmob","取消关注失败："+e.getMessage());
                        }
                    }
                });

            }
        });
    }
}
