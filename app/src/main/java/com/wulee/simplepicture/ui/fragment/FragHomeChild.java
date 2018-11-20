package com.wulee.simplepicture.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.wulee.simplepicture.R;
import com.wulee.simplepicture.adapter.PicAdapter;
import com.wulee.simplepicture.base.BaseFragment;
import com.wulee.simplepicture.base.RefreshEvent;
import com.wulee.simplepicture.bean.StickFigureImgObj;
import com.wulee.simplepicture.ui.BigMultiImgActivity;
import com.wulee.simplepicture.utils.UIUtils;
import com.wulee.simplepicture.view.FullyGridLayoutManager;
import com.wulee.simplepicture.view.SpaceItemDecoration;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

/**
 * Created by wulee
 */
public class FragHomeChild extends BaseFragment {

    @BindView(R.id.recyclerview)
    RecyclerView recyclerview;
    @BindView(R.id.swipeLayout)
    SwipeRefreshLayout swipeLayout;
    Unbinder unbinder;
    private Context mContext;


    private PicAdapter mAdapter;
    private ArrayList<StickFigureImgObj> mDataList = new ArrayList<>();

    private static final int STATE_REFRESH = 0;// 下拉刷新
    private static final int STATE_MORE = 1;// 加载更多
    private int PAGE_SIZE = 20;
    private int curPage = 0;
    private boolean isRefresh = false;

    private int mType;

    public static FragHomeChild newInstance(int type) {
        FragHomeChild fragment = new FragHomeChild();
        Bundle args = new Bundle();
        args.putInt("type", type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        mType = getArguments() != null ? getArguments().getInt("type") : 0;
    }

    @Override
    protected boolean isLazyLoad() {
        return true;
    }

    @Override
    protected int getContentLayout() {
        return R.layout.home_frag_child_list;
    }

    @Override
    protected void initView() {
        swipeLayout.setColorSchemeResources(R.color.com_app_color,R.color.colorPrimary);

        mAdapter = new PicAdapter(R.layout.pic_list_item, mDataList, mContext);
        recyclerview.setLayoutManager(new FullyGridLayoutManager(mContext,3));
        if(recyclerview.getItemDecorationCount()>0){//解决重复调用addItemDecoration方法使间隔增大
            RecyclerView.ItemDecoration decoration = recyclerview.getItemDecorationAt(0);
            if(decoration == null){
                recyclerview.addItemDecoration(new SpaceItemDecoration(3, UIUtils.dip2px(10), false));
            }
        }else{
            recyclerview.addItemDecoration(new SpaceItemDecoration(3, UIUtils.dip2px(10), false));
        }
        recyclerview.setAdapter(mAdapter);
    }


    @Override
    protected void initData() {
        isRefresh = true;
        curPage = 0;
        getPicList(0,STATE_REFRESH);
    }

    @Override
    protected void initEvent() {
        EventBus.getDefault().register(this);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isRefresh  = true;
                curPage = 0;
                getPicList(0,STATE_REFRESH);
            }
        });

       mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
           @Override
           public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
               List<StickFigureImgObj> dataList  = mAdapter.getData();
               if(null != dataList && dataList.size()>0){
                   StickFigureImgObj stickFigureImgObj = dataList.get(i);
                   if(stickFigureImgObj !=null && stickFigureImgObj.getImageGroup().length>0){
                       Intent intent = new Intent(getActivity(), BigMultiImgActivity.class);
                       intent.putExtra(BigMultiImgActivity.IMAGES_URL, stickFigureImgObj.getImageGroup());
                       intent.putExtra(BigMultiImgActivity.IMAGE_INDEX, 0);
                       getActivity().startActivity(intent);
                   }
               }
           }
       });
        mAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                getPicList(curPage,STATE_MORE);
            }
        }, recyclerview);
    }


    @Subscribe
    public void onEventMainThread(RefreshEvent event) {
        isRefresh  = true;
        curPage = 0;
        getPicList(0,STATE_REFRESH);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        EventBus.getDefault().unregister(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    /**
     * 分页获取数据
     */
    private void getPicList(final int page, final int actionType){
        BmobQuery<StickFigureImgObj> query = new BmobQuery<>();
        query.addWhereEqualTo("type",String.valueOf(mType));
        query.addWhereNotEqualTo("isHide",true);//查询没有隐藏的
        query.include("userInfo");
        query.order("-likeNum,-createdAt");//按点赞数降序、时间倒序排
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
           showProgressDialog(getActivity(),true);
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
                        mAdapter.setEmptyView(LayoutInflater.from(getContext()).inflate(R.layout.com_view_empty, (ViewGroup) recyclerview.getParent(), false));
                    }
                }else{
                    mAdapter.loadMoreFail();
                    Log.d("","查询失败"+e.getMessage()+","+e.getErrorCode());
                }
            }
        });
    }

}
