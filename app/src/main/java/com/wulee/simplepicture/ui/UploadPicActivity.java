package com.wulee.simplepicture.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.wulee.simplepicture.R;
import com.wulee.simplepicture.adapter.PicFileAdapter;
import com.wulee.simplepicture.adapter.SpinnerAdapter;
import com.wulee.simplepicture.base.BaseActivity;
import com.wulee.simplepicture.base.Constant;
import com.wulee.simplepicture.base.RefreshEvent;
import com.wulee.simplepicture.bean.StickFigureImgObj;
import com.wulee.simplepicture.bean.UserInfo;
import com.wulee.simplepicture.utils.FileUtils;
import com.wulee.simplepicture.utils.OtherUtil;
import com.wulee.simplepicture.utils.UIUtils;
import com.wulee.simplepicture.view.BaseTitleLayout;
import com.wulee.simplepicture.view.FullyGridLayoutManager;
import com.wulee.simplepicture.view.SpaceItemDecoration;
import com.wulee.simplepicture.view.TitleLayoutClickListener;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadBatchListener;

/**
 * create by  wulee   2018/9/6 19:01
 * desc:上传简笔画界面
 */
public class UploadPicActivity extends BaseActivity {

    @BindView(R.id.recyclerview)
    RecyclerView recyclerview;
    @BindView(R.id.swipeLayout)
    SwipeRefreshLayout swipeLayout;
    @BindView(R.id.titlelayout)
    BaseTitleLayout titlelayout;
    @BindView(R.id.spinner_type)
    Spinner spinnerType;

    private PicFileAdapter mFileAdapter;
    private String mType = "";
    private List<String> mFilePathList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_upload_pic);
        ButterKnife.bind(this);

        initView();
        initData();
        addListener();

    }

    private void initView() {
        swipeLayout.setColorSchemeResources(R.color.com_app_color, R.color.colorPrimary);
        mFileAdapter = new PicFileAdapter(R.layout.pic_file_list_item, null, this);
        recyclerview.setLayoutManager(new FullyGridLayoutManager(this, 3));
        recyclerview.addItemDecoration(new SpaceItemDecoration(3, UIUtils.dip2px(10), false));
        recyclerview.setAdapter(mFileAdapter);

        List<String> picTypes = Arrays.asList(getResources().getStringArray(R.array.array_pic_type));
        SpinnerAdapter spinnerAdapter = new SpinnerAdapter(this,picTypes);
        spinnerType.setAdapter(spinnerAdapter);
    }

    private void initData() {
        mFilePathList.clear();
        List<File> fileList = FileUtils.listFilesInDirWithFilter(Constant.SAVE_PIC, ".png");
        if (fileList != null && fileList.size() > 0) {
            for (File file : fileList){
                mFilePathList.add(file.getPath());
            }
        }
        mFileAdapter.setNewData(mFilePathList);
    }

    private void addListener() {
        titlelayout.setOnTitleClickListener(new TitleLayoutClickListener() {
            @Override
            public void onLeftClickListener() {
                finish();
            }

            @Override
            public void onRightImg1ClickListener() {
                if (OtherUtil.hasLogin()) {
                    uploadPic();
                } else {
                    startActivity(new Intent(UploadPicActivity.this, LoginActivity.class));
                }
            }

            @Override
            public void onRightImg2ClickListener() {
                final List<String> filePathList = mFileAdapter.getSelFilePath();
                if (filePathList == null) {
                    showToast("请选择要删除的图片");
                    return;
                }
                for (int i = 0; i < filePathList.size(); i++) {
                    String path = filePathList.get(i);
                    FileUtils.deleteFile(path);

                    Iterator<String> it = filePathList.iterator();
                    while(it.hasNext()){
                        if(it.next().equals(path)){
                            it.remove();
                        }
                    }
                }

                initData();
            }
        });

        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                mType = index + "";
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mType = "";
            }
        });

        mFileAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                List<String> dataList  = mFileAdapter.getData();
                if(null != dataList && dataList.size()>0){
                    String filepath = dataList.get(i);
                    if(!TextUtils.isEmpty(filepath)){
                        Intent intent = new Intent(UploadPicActivity.this, BigMultiImgActivity.class);
                        intent.putExtra(BigMultiImgActivity.IMAGES_URL, new String[]{filepath});
                        intent.putExtra(BigMultiImgActivity.IMAGE_INDEX, 0);
                        startActivity(intent);
                    }
                }
            }
        });
    }

    private void uploadPic() {
        List<String> filePathList = mFileAdapter.getData();
        if (filePathList == null || filePathList.size() == 0) {
            showToast("还没有图片哦@~@");
            return;
        }
        final List<String> selfilePathList = mFileAdapter.getSelFilePath();
        if (selfilePathList == null) {
            showToast("请选择要上传的图片");
            return;
        }
        final String[] filePaths = new String[selfilePathList.size()];
        for (int i = 0; i < selfilePathList.size(); i++) {
            String filePath = selfilePathList.get(i);
            filePaths[i] = filePath;
        }
        showProgressDialog(true);
        BmobFile.uploadBatch(filePaths, new UploadBatchListener() {
            @Override
            public void onSuccess(List<BmobFile> files, List<String> urls) {
                //1、files-上传完成后的BmobFile集合，是为了方便大家对其上传后的数据进行操作，例如你可以将该文件保存到表中
                //2、urls-上传文件的完整url地址
                if (urls.size() == filePaths.length) {//如果数量相等，则代表文件全部上传完成
                    //do something
                    String[] imgUrls = new String[urls.size()];
                    for (int i = 0; i < urls.size(); i++) {
                        imgUrls[i] = urls.get(i);
                    }
                    UserInfo userInfo = BmobUser.getCurrentUser(UserInfo.class);
                    StickFigureImgObj stickFigureImgObj = new StickFigureImgObj();
                    stickFigureImgObj.setImageGroup(imgUrls);
                    stickFigureImgObj.setUserInfo(userInfo);
                    stickFigureImgObj.setType(mType);
                    showProgressDialog(true);
                    stickFigureImgObj.save(new SaveListener<String>() {
                        @Override
                        public void done(String s, BmobException e) {
                            stopProgressDialog();
                            if (e == null) {
                                showToast("上传成功！");

                                EventBus.getDefault().post(new RefreshEvent());

                                for (int i = 0; i < selfilePathList.size(); i++) {
                                    File file = new File(selfilePathList.get(i));
                                    FileUtils.deleteFile(file);

                                    mFilePathList.remove(selfilePathList.get(i));
                                    mFileAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(int statuscode, String errormsg) {
                showToast("错误码" + statuscode + ",错误描述：" + errormsg);
            }

            @Override
            public void onProgress(int curIndex, int curPercent, int total, int totalPercent) {
                //1、curIndex--表示当前第几个文件正在上传
                //2、curPercent--表示当前上传文件的进度值（百分比）
                //3、total--表示总的上传文件数
                //4、totalPercent--表示总的上传进度（百分比）
            }
        });
    }

}
