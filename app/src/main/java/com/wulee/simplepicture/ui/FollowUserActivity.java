package com.wulee.simplepicture.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.wulee.simplepicture.R;
import com.wulee.simplepicture.adapter.FollowUserAdapter;
import com.wulee.simplepicture.base.BaseActivity;
import com.wulee.simplepicture.base.RefreshEvent;
import com.wulee.simplepicture.bean.UserInfo;
import com.wulee.simplepicture.view.BaseTitleLayout;
import com.wulee.simplepicture.view.RecycleViewDivider;
import com.wulee.simplepicture.view.TitleLayoutClickListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

/**
 * create by  wulee   2018/9/25 17:39
 * desc:关注的用户列表界面
 */
public class FollowUserActivity extends BaseActivity {

    @BindView(R.id.recyclerview)
    RecyclerView recyclerview;
    @BindView(R.id.swipeLayout)
    SwipeRefreshLayout swipeLayout;
    @BindView(R.id.titlelayout)
    BaseTitleLayout titlelayout;

    private FollowUserAdapter mAdapter;
    private ArrayList<UserInfo> mDataList = new ArrayList<>();

    private static final int STATE_REFRESH = 0;// 下拉刷新
    private static final int STATE_MORE = 1;// 加载更多
    private int PAGE_SIZE = 20;
    private int curPage = 0;
    private boolean isRefresh = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.follow_user_list_main);
        ButterKnife.bind(this);

        initView();
        addListener();

        getFollowUserList(curPage,STATE_REFRESH);
    }

    private void initView() {
        swipeLayout.setColorSchemeResources(R.color.com_app_color,R.color.colorPrimary);

        mAdapter = new FollowUserAdapter(R.layout.follow_user_list_item, mDataList, this);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        recyclerview.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager.HORIZONTAL, 1, ContextCompat.getColor(this, R.color.tv_color_4)));
        recyclerview.setAdapter(mAdapter);

    }


    private void addListener() {
        EventBus.getDefault().register(this);
        titlelayout.setOnTitleClickListener(new TitleLayoutClickListener() {
            @Override
            public void onLeftClickListener() {
                finish();
            }
        });

        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isRefresh  = true;
                getFollowUserList(0,STATE_REFRESH);
            }
        });

        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                List<UserInfo> dataList  = mAdapter.getData();
                if(null != dataList && dataList.size()>0){
                    UserInfo userInfo = dataList.get(i);
                    if(userInfo != null){
                        Intent intent = new Intent(FollowUserActivity.this, UserHomeActivity.class);
                        intent.putExtra(UserHomeActivity.USER_INFO,userInfo);
                        startActivity(intent);
                    }
                }
            }
        });
        mAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                getFollowUserList(curPage,STATE_MORE);
            }
        }, recyclerview);
    }

    @Subscribe
    public void onEventMainThread(RefreshEvent event) {
        isRefresh  = true;
        getFollowUserList(0,STATE_REFRESH);
    }

    /**
     * 分页获取数据
     */
    private void getFollowUserList(final int page, final int actionType){
        UserInfo curruserInfo = BmobUser.getCurrentUser(UserInfo.class);
        BmobQuery<UserInfo> query = new BmobQuery<>();
        UserInfo userInfo = new UserInfo();
        userInfo.setObjectId(curruserInfo.getObjectId());
        //follow是_User表中的字段，用来存储所有关注的用户
        query.addWhereRelatedTo("follow", new BmobPointer(userInfo));
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
        query.findObjects(new FindListener<UserInfo>() {
            @Override
            public void done(List<UserInfo> dataList, BmobException e) {
                stopProgressDialog();
                if (swipeLayout != null && swipeLayout.isRefreshing()){
                    swipeLayout.setRefreshing(false);
                }
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
                        mAdapter.setEmptyView(LayoutInflater.from(FollowUserActivity.this).inflate(R.layout.com_view_empty, (ViewGroup) recyclerview.getParent(), false));
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
        EventBus.getDefault().unregister(this);
    }
}
