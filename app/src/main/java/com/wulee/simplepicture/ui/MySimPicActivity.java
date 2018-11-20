package com.wulee.simplepicture.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.wulee.simplepicture.R;
import com.wulee.simplepicture.adapter.PicAdapter;
import com.wulee.simplepicture.base.BaseActivity;
import com.wulee.simplepicture.base.RefreshEvent;
import com.wulee.simplepicture.bean.StickFigureImgObj;
import com.wulee.simplepicture.bean.UserInfo;
import com.wulee.simplepicture.utils.UIUtils;
import com.wulee.simplepicture.view.BaseTitleLayout;
import com.wulee.simplepicture.view.FullyGridLayoutManager;
import com.wulee.simplepicture.view.SpaceItemDecoration;
import com.wulee.simplepicture.view.TitleLayoutClickListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DeleteBatchListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * create by  wulee   2018/9/6 19:01
 * desc:我的简笔画界面
 */
public class MySimPicActivity extends BaseActivity {

    @BindView(R.id.recyclerview)
    RecyclerView recyclerview;
    @BindView(R.id.swipeLayout)
    SwipeRefreshLayout swipeLayout;
    @BindView(R.id.titlelayout)
    BaseTitleLayout titlelayout;

    private PicAdapter mAdapter;
    private ArrayList<StickFigureImgObj> mDataList = new ArrayList<>();

    private static final int STATE_REFRESH = 0;// 下拉刷新
    private static final int STATE_MORE = 1;// 加载更多
    private int PAGE_SIZE = 20;
    private int curPage = 0;
    private boolean isRefresh = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_my_sim_pic);
        ButterKnife.bind(this);

        initView();
        addListener();

        getMySimPicList(curPage,STATE_REFRESH);
    }

    private void initView() {
        swipeLayout.setColorSchemeResources(R.color.com_app_color,R.color.colorPrimary);

        mAdapter = new PicAdapter(R.layout.pic_list_item, mDataList, this);
        recyclerview.setLayoutManager(new FullyGridLayoutManager(this, 3));
        recyclerview.addItemDecoration(new SpaceItemDecoration(3, UIUtils.dip2px(10), false));
        recyclerview.setAdapter(mAdapter);

        mAdapter.setLikeOpt(false);
        mAdapter.setShowUserAvatar(false);
    }


    private void addListener() {
        EventBus.getDefault().register(this);
        titlelayout.setOnTitleClickListener(new TitleLayoutClickListener() {
            @Override
            public void onLeftClickListener() {
                finish();
            }
            @Override
            public void onRightImg1ClickListener() {
                Intent  intent = new Intent(MySimPicActivity.this,UploadPicActivity.class);
                startActivity(intent);
            }
        });

        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isRefresh  = true;
                getMySimPicList(0,STATE_REFRESH);
            }
        });

        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                List<StickFigureImgObj> dataList  = mAdapter.getData();
                if(null != dataList && dataList.size()>0){
                    StickFigureImgObj stickFigureImgObj = dataList.get(i);
                    if(stickFigureImgObj !=null && stickFigureImgObj.getImageGroup().length>0){
                        Intent intent = new Intent(MySimPicActivity.this, BigMultiImgActivity.class);
                        intent.putExtra(BigMultiImgActivity.IMAGES_URL, stickFigureImgObj.getImageGroup());
                        intent.putExtra(BigMultiImgActivity.IMAGE_INDEX, 0);
                        startActivity(intent);
                    }
                }
            }
        });

        mAdapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
                List<StickFigureImgObj> dataList  = mAdapter.getData();
                if(null != dataList && dataList.size()>0){
                    StickFigureImgObj stickFigureImgObj = dataList.get(position);
                    if(stickFigureImgObj !=null){
                        showDelDialog(stickFigureImgObj);
                    }
                }
                return false;
            }
        });

        mAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                getMySimPicList(curPage,STATE_MORE);
            }
        }, recyclerview);
    }


    private void showDelDialog(final StickFigureImgObj stickFigureImgObj) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("删除此作品");
        builder.setMessage("确定要删除吗？");
        final String[] urls = stickFigureImgObj.getImageGroup();
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                stickFigureImgObj.delete(stickFigureImgObj.getObjectId(), new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        if(e == null){
                            BmobFile.deleteBatch(urls, new DeleteBatchListener() {
                                @Override
                                public void done(String[] failUrls, BmobException e) {
                                    //此处删除有时会出现失败，不管失败还是成功都去更新界面，不提示用户
                                    if(e==null){
                                        Log.i("del","删除成功");
                                    }else{
                                        Log.i("del","删除失败");
                                    }
                                    EventBus.getDefault().post(new RefreshEvent());
                                }
                            });
                        }else{
                            toast("删除失败");
                        }
                    }
                });
            }
        });
        builder.setNegativeButton("取消", null);
        builder.create().show();
    }



    @Subscribe
    public void onEventMainThread(RefreshEvent event) {
        isRefresh  = true;
        getMySimPicList(0,STATE_REFRESH);
    }

    /**
     * 分页获取数据
     */
    private void getMySimPicList(final int page, final int actionType){
        UserInfo userInfo = BmobUser.getCurrentUser(UserInfo.class);
        BmobQuery<StickFigureImgObj> query = new BmobQuery<>();
        query.addWhereEqualTo("userInfo", userInfo);    // 查询当前用户的作品
        query.include("userInfo");// 希望在查询作品信息的同时也把当前用户的作品查询出来
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
                        mAdapter.setEmptyView(LayoutInflater.from(MySimPicActivity.this).inflate(R.layout.com_view_empty, (ViewGroup) recyclerview.getParent(), false));
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
