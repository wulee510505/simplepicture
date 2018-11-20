package com.wulee.simplepicture.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.wulee.simplepicture.R;
import com.wulee.simplepicture.adapter.PicAdapter;
import com.wulee.simplepicture.base.BaseActivity;
import com.wulee.simplepicture.bean.StickFigureImgObj;
import com.wulee.simplepicture.bean.UserInfo;
import com.wulee.simplepicture.utils.ImageUtil;
import com.wulee.simplepicture.utils.OtherUtil;
import com.wulee.simplepicture.utils.PhoneUtil;
import com.wulee.simplepicture.utils.UIUtils;
import com.wulee.simplepicture.view.BaseTitleLayout;
import com.wulee.simplepicture.view.FullyGridLayoutManager;
import com.wulee.simplepicture.view.SpaceItemDecoration;
import com.wulee.simplepicture.view.TitleLayoutClickListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * create by  wulee   2018/9/6 19:01
 * desc个人主页界面
 */
public class UserHomeActivity extends BaseActivity {

    @BindView(R.id.recyclerview)
    RecyclerView recyclerview;
    @BindView(R.id.titlelayout)
    BaseTitleLayout titlelayout;

    public static final String USER_INFO = "user_info";

    private PicAdapter mAdapter;
    private ArrayList<StickFigureImgObj> mDataList = new ArrayList<>();

    private static final int STATE_REFRESH = 0;// 下拉刷新
    private static final int STATE_MORE = 1;// 加载更多
    private int PAGE_SIZE = 20;
    private int curPage = 0;
    private boolean isRefresh = false;


    private UserInfo mUserInfo;
    private UserInfo mCurruser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_home);
        ButterKnife.bind(this);

        mUserInfo = (UserInfo) getIntent().getSerializableExtra(USER_INFO);
        mCurruser = BmobUser.getCurrentUser(UserInfo.class);
        if(mUserInfo == null)
            return;

        initView();
        addListener();

        getSimPicList(curPage,STATE_REFRESH);
    }

    private void initView() {
        mAdapter = new PicAdapter(R.layout.pic_list_item, mDataList, this);
        recyclerview.setLayoutManager(new FullyGridLayoutManager(this, 3));
        recyclerview.addItemDecoration(new SpaceItemDecoration(3, UIUtils.dip2px(10), false));

        ImageView ivHeader = findViewById(R.id.iv_header);
        TextView tvName = findViewById(R.id.tv_name);
        TextView tvNickName = findViewById(R.id.tv_nick_name);
        final TextView tvFollow = findViewById(R.id.tv_follow);
        if (null != mUserInfo) {
            String encryptTelNum = PhoneUtil.encryptTelNum(mUserInfo.getUsername());
            titlelayout.setCenterText(!TextUtils.isEmpty(mUserInfo.getNickName())?mUserInfo.getNickName():encryptTelNum);

            ImageUtil.setCircleImageView(ivHeader, mUserInfo.getUserImage(), R.mipmap.icon_user_avatar_def, this);
            tvName.setText(encryptTelNum);
            if (!TextUtils.isEmpty(mUserInfo.getNickName()))
                tvNickName.setText("昵称：" + mUserInfo.getNickName());
            else
                tvNickName.setText("昵称：游客");

            if(mCurruser != null){
                if(TextUtils.equals(mUserInfo.getObjectId(),mCurruser.getObjectId())){
                    //隐藏关注按钮
                    tvFollow.setVisibility(View.GONE);
                }else{
                    tvFollow.setVisibility(View.VISIBLE);
                    // 查询当前用户关注的所有用户，因此查询的是用户表
                    BmobQuery<UserInfo> query = new BmobQuery<>();
                    UserInfo userInfo = new UserInfo();
                    userInfo.setObjectId(mCurruser.getObjectId());
                    //follow是_User表中的字段，用来存储所有关注的用户
                    query.addWhereRelatedTo("follow", new BmobPointer(userInfo));
                    query.findObjects(new FindListener<UserInfo>() {
                        @Override
                        public void done(List<UserInfo> followers,BmobException e) {
                            if(e == null){
                                if(followers != null && followers.size()>0){
                                    for(UserInfo user:followers){
                                        if(TextUtils.equals(user.getObjectId(),mUserInfo.getObjectId())){
                                            tvFollow.setText("已关注");
                                            break;
                                        }
                                    }
                                }
                            }else{
                                Log.i("bmob","失败："+e.getMessage());
                            }
                        }
                    });
                    tvFollow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!OtherUtil.hasLogin()) {
                                startActivity(new Intent(UserHomeActivity.this, LoginActivity.class));
                                return;
                            }
                            UserInfo userInfo = new UserInfo();
                            userInfo.setObjectId(mCurruser.getObjectId());
                            //将用户添加到_User表中的follow字段值中，表明当前用户关注该用户
                            BmobRelation relation = new BmobRelation();
                            relation.add(mUserInfo);
                            //多对多关联指向`userInfo`的`follow`字段
                            userInfo.setFollow(relation);
                            userInfo.update(new UpdateListener() {
                                @Override
                                public void done(BmobException e) {
                                    if(e==null){
                                        Log.i("bmob","关注成功");
                                        tvFollow.setText("已关注");
                                    }else{
                                        Log.i("bmob","关注失败："+e.getMessage());
                                    }
                                }
                            });

                        }
                    });
                }
            }
        }
        recyclerview.setAdapter(mAdapter);

        mAdapter.setShowUserAvatar(false);
        mAdapter.setLikeOpt(false);
    }


    private void addListener() {
        titlelayout.setOnTitleClickListener(new TitleLayoutClickListener() {
            @Override
            public void onLeftClickListener() {
                finish();
            }
        });

        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                List<StickFigureImgObj> dataList  = mAdapter.getData();
                if(null != dataList && dataList.size()>0){
                    StickFigureImgObj stickFigureImgObj = dataList.get(i);
                    if(stickFigureImgObj !=null && stickFigureImgObj.getImageGroup().length>0){
                        Intent intent = new Intent(UserHomeActivity.this, BigMultiImgActivity.class);
                        intent.putExtra(BigMultiImgActivity.IMAGES_URL, stickFigureImgObj.getImageGroup());
                        intent.putExtra(BigMultiImgActivity.IMAGE_INDEX, 0);
                        startActivity(intent);
                    }
                }
            }
        });
        mAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                getSimPicList(curPage,STATE_MORE);
            }
        }, recyclerview);
    }


    /**
     * 分页获取数据
     */
    private void getSimPicList(final int page, final int actionType){
        BmobQuery<StickFigureImgObj> query = new BmobQuery<>();
        query.addWhereEqualTo("userInfo", mUserInfo);    // 查询指定用户的作品
        query.include("userInfo");// 希望在查询作品信息的同时也把指定用户的作品查询出来
        query.order("-createdAt");
        // 如果是加载更多
        if(actionType == STATE_MORE){
            // 跳过之前页数并去掉重复数据
            query.setSkip(page * PAGE_SIZE);
        }else{
            query.setSkip(0);
        }
        // 设置每页数据个数
        query.setLimit(PAGE_SIZE);
        if(!isRefresh)
          showProgressDialog(true);
        query.findObjects(new FindListener<StickFigureImgObj>() {
            @Override
            public void done(List<StickFigureImgObj> dataList, BmobException e) {
                stopProgressDialog();
                if(e == null){
                    curPage++;
                    if (isRefresh){//下拉刷新需清理缓存
                        mAdapter.setNewData(dataList);
                        isRefresh = false;
                    }else {//正常请求 或 上拉加载更多时处理流程
                        if (dataList.size() > 0) {
                            mAdapter.addData(dataList);
                        }
                    }
                    if (dataList.size() < PAGE_SIZE) {
                        //第一页如果不够一页就不显示没有更多数据布局
                        mAdapter.loadMoreEnd(true);
                    } else {
                        mAdapter.loadMoreComplete();
                    }
                    if (mAdapter.getData().size() == 0) {
                        mAdapter.setEmptyView(LayoutInflater.from(UserHomeActivity.this).inflate(R.layout.com_view_empty, (ViewGroup) recyclerview.getParent(), false));
                    }
                }else{
                    mAdapter.loadMoreFail();
                    Log.d("","查询失败"+e.getMessage()+","+e.getErrorCode());
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
