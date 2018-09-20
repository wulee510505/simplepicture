package com.wulee.simplepicture.ui.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.lzy.imagepicker.view.CropImageView;
import com.wulee.simplepicture.R;
import com.wulee.simplepicture.base.Constant;
import com.wulee.simplepicture.base.MainBaseFrag;
import com.wulee.simplepicture.bean.UserInfo;
import com.wulee.simplepicture.ui.LoginActivity;
import com.wulee.simplepicture.ui.MySimPicActivity;
import com.wulee.simplepicture.utils.AppUtils;
import com.wulee.simplepicture.utils.DataCleanManager;
import com.wulee.simplepicture.utils.GlideImageLoader;
import com.wulee.simplepicture.utils.ImageUtil;
import com.wulee.simplepicture.utils.OtherUtil;
import com.wulee.simplepicture.view.CoolImageView;
import com.zhouwei.blurlibrary.EasyBlur;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.BmobUpdateListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;
import cn.bmob.v3.update.BmobUpdateAgent;
import cn.bmob.v3.update.UpdateResponse;
import cn.bmob.v3.update.UpdateStatus;

import static cn.bmob.v3.Bmob.getApplicationContext;
import static cn.bmob.v3.BmobUser.getCurrentUser;
import static com.wulee.simplepicture.App.mACache;

/**
 * create by  wulee
 * desc:
 */

public class FragMine extends MainBaseFrag {


    @BindView(R.id.iv_header)
    ImageView ivHeader;
    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.tv_nick_name)
    TextView tvNickName;
    @BindView(R.id.tv_app_version)
    TextView tvAppVersion;
    @BindView(R.id.tv_check_update)
    TextView tvCheckUpdate;
    @BindView(R.id.tv_logout)
    TextView tvLogout;
    Unbinder unbinder;
    @BindView(R.id.iv_header_bg)
    CoolImageView ivHeaderBg;
    @BindView(R.id.rl_my_sim_pic)
    RelativeLayout rlMySimPic;
    @BindView(R.id.tv_cache_size)
    TextView tvCacheSize;
    @BindView(R.id.rl_clear_cache)
    RelativeLayout rlClearCache;
    private Bitmap finalBitmap;
    public static final int REQUEST_CODE_CHOOSE = 100;
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0x01:
                    stopProgressDialog();
                    tvCacheSize.setText("0.0KB");
                    break;
                case 0x02:
                    stopProgressDialog();
                    break;
            }
        };
    };


    @Override
    protected boolean isLazyLoad() {
        return true;
    }

    @Override
    protected int getContentLayout() {
        return R.layout.fragment_mine;
    }

    @Override
    protected void initView() {
        UserInfo user = BmobUser.getCurrentUser(UserInfo.class);
        if (null != user) {
            ImageUtil.setCircleImageView(ivHeader, user.getUserImage(), R.mipmap.icon_user_avatar_def, getContext());
            tvName.setText(user.getUsername());
            if (!TextUtils.isEmpty(user.getNickName()))
                tvNickName.setText("昵称：" + user.getNickName());
            else
                tvNickName.setText("昵称：游客");
        } else {
            tvNickName.setText("未登录");
        }
        tvAppVersion.setText("软件版本：v"+ AppUtils.getVersionName());

        if (!OtherUtil.hasLogin()) {
            tvLogout.setText("登录");
        } else {
            tvLogout.setText("退出登录");
        }
    }


    @Override
    protected void initData() {
        File file =new File(getContext().getCacheDir().getPath());
        try {
            tvCacheSize.setText(DataCleanManager.getCacheSize(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initEvent() {

    }

    @Override
    public void onFragmentFirstSelected() {
        Drawable drawable = ivHeaderBg.getBackground();
        BitmapDrawable bd = (BitmapDrawable) drawable;
        Bitmap bm = bd.getBitmap();
        finalBitmap = EasyBlur.with(getContext())
                .bitmap(bm) //要模糊的图片
                .radius(50)//模糊半径
                .scale(4)//指定模糊前缩小的倍数
                .policy(EasyBlur.BlurPolicy.FAST_BLUR)//使用fastBlur
                .blur();
        ivHeaderBg.setImageBitmap(finalBitmap);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, rootView);

        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new GlideImageLoader());   //设置图片加载器
        imagePicker.setShowCamera(true);  //显示拍照按钮
        imagePicker.setCrop(true);        //允许裁剪（单选才有效）
        imagePicker.setSaveRectangle(true); //是否按矩形区域保存
        imagePicker.setSelectLimit(1);    //选中数量限制
        imagePicker.setStyle(CropImageView.Style.CIRCLE);  //裁剪框的形状
        imagePicker.setFocusWidth(300);   //裁剪框的宽度。单位像素（圆形自动取宽高最小值）
        imagePicker.setFocusHeight(300);  //裁剪框的高度。单位像素（圆形自动取宽高最小值）

        return rootView;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        if (finalBitmap != null && !finalBitmap.isRecycled()) {
            finalBitmap.recycle();//回收图片所占的内存
            finalBitmap = null;
            System.gc();  //提醒系统及时回收
        }
    }

    @OnClick({R.id.iv_header, R.id.tv_check_update, R.id.tv_logout, R.id.rl_my_sim_pic, R.id.tv_nick_name,R.id.rl_clear_cache})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_header:
                if (!OtherUtil.hasLogin()) {
                    startActivity(new Intent(getContext(), LoginActivity.class));
                } else {
                    Intent intent = new Intent(getContext(), ImageGridActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_CHOOSE);
                }
                break;
            case R.id.tv_check_update:
                BmobUpdateAgent.setUpdateListener(new BmobUpdateListener() {
                    @Override
                    public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
                        // TODO Auto-generated method stub
                        if (updateStatus == UpdateStatus.Yes) {//版本有更新

                        } else if (updateStatus == UpdateStatus.No) {
                            Toast.makeText(getContext(), "版本无更新", Toast.LENGTH_SHORT).show();
                        } else if (updateStatus == UpdateStatus.EmptyField) {//此提示只是提醒开发者关注那些必填项，测试成功后，无需对用户提示
                            Toast.makeText(getContext(), "请检查你AppVersion表的必填项，1、target_size（文件大小）是否填写；2、path或者android_url两者必填其中一项。", Toast.LENGTH_SHORT).show();
                        } else if (updateStatus == UpdateStatus.IGNORED) {
                            Toast.makeText(getContext(), "该版本已被忽略更新", Toast.LENGTH_SHORT).show();
                        } else if (updateStatus == UpdateStatus.ErrorSizeFormat) {
                            Toast.makeText(getContext(), "请检查target_size填写的格式，请使用file.length()方法获取apk大小。", Toast.LENGTH_SHORT).show();
                        } else if (updateStatus == UpdateStatus.TimeOut) {
                            Toast.makeText(getContext(), "查询出错或查询超时", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                BmobUpdateAgent.setUpdateOnlyWifi(true);
                BmobUpdateAgent.forceUpdate(getContext());
                break;
            case R.id.rl_my_sim_pic:
                Intent intent;
                if (OtherUtil.hasLogin()) {
                    intent = new Intent(getContext(), MySimPicActivity.class);
                } else {
                    intent = new Intent(getContext(), LoginActivity.class);
                }
                startActivity(intent);
                break;
            case R.id.tv_logout:
                if (!OtherUtil.hasLogin()) {
                    startActivity(new Intent(getContext(), LoginActivity.class));
                } else {
                    showLogoutDialog();
                }
                break;
            case R.id.rl_clear_cache:
                String cacheSize = tvCacheSize.getText().toString().trim();
                if(TextUtils.equals("0.0KB",cacheSize)){
                    return;
                }
                showProgressDialog(getActivity(),true);
                try {
                    DataCleanManager.cleanInternalCache(getApplicationContext());
                    mHandler.sendEmptyMessageDelayed( 0x01,1000);
                } catch (Exception e) {
                    mHandler.sendEmptyMessageDelayed( 0x02,1000);
                }
                break;
            case R.id.tv_nick_name:
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("修改昵称");
                View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edittext, null);
                final EditText editText = dialogView.findViewById(R.id.edittext);
                builder.setView(dialogView);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        final String nickName = editText.getText().toString().trim();

                        UserInfo newUser = new UserInfo();
                        newUser.setNickName(nickName);
                        UserInfo userInfo = getCurrentUser(UserInfo.class);
                        showProgressDialog(getActivity(), true);
                        newUser.update(userInfo.getObjectId(), new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                stopProgressDialog();
                                if (e == null) {
                                    tvNickName.setText("昵称：" + nickName);
                                    Toast.makeText(getContext(), "昵称修改成功", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
                builder.setNegativeButton("取消", null);
                builder.create().show();
                break;
        }
    }



    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("退出登录");
        builder.setMessage("确定要退出吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mACache.put("has_login", "no");

                AppUtils.AppExit(getActivity());
                UserInfo.logOut();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.create().show();
    }

    /**
     * 更新头像
     */
    private void uploadImgFile(final String path) {
        final BmobFile bmobFile = new BmobFile(new File(path));
        bmobFile.uploadblock(new UploadFileListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    String userimgurl = bmobFile.getFileUrl();

                    UserInfo newUser = new UserInfo();
                    newUser.setUserImage(userimgurl);
                    UserInfo userInfo = getCurrentUser(UserInfo.class);
                    newUser.update(userInfo.getObjectId(), new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if (e == null) {
                                Toast.makeText(getContext(), "更新个人头像成功", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "头像上传失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onProgress(Integer value) {
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
            if (data != null && requestCode == REQUEST_CODE_CHOOSE) {
                ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                if (images != null && images.size() > 0) {
                    String imgPath = images.get(0).path;
                    if (!TextUtils.isEmpty(imgPath)) {
                        Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
                        if (null != bitmap) {
                            ivHeader.setImageBitmap(ImageUtil.toRoundBitmap(bitmap));
                        }
                        try {
                            File dir = new File(Constant.TEMP_FILE_PATH);
                            if (!dir.exists()) {
                                dir.mkdirs();
                            }
                            UserInfo currUserInfo = BmobUser.getCurrentUser(UserInfo.class);
                            String dstFilePath = Constant.TEMP_FILE_PATH + "avatar_" + currUserInfo.getObjectId() + ".png";
                            File avatarFile = ImageUtil.resizeBitmapAndSave(bitmap, dstFilePath, 0.3f);
                            if (avatarFile != null && avatarFile.exists()) {
                                uploadImgFile(avatarFile.getPath());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

}
