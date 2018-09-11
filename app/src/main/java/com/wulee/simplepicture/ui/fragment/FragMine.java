package com.wulee.simplepicture.ui.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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

import com.wulee.simplepicture.R;
import com.wulee.simplepicture.base.MainBaseFrag;
import com.wulee.simplepicture.bean.UserInfo;
import com.wulee.simplepicture.ui.LoginActivity;
import com.wulee.simplepicture.ui.MainActivity;
import com.wulee.simplepicture.ui.MySimPicActivity;
import com.wulee.simplepicture.utils.AppUtils;
import com.wulee.simplepicture.utils.ImageUtil;
import com.wulee.simplepicture.utils.OtherUtil;
import com.wulee.simplepicture.view.CoolImageView;
import com.wulee.simplepicture.view.GlideEngineImpl;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhouwei.blurlibrary.EasyBlur;

import java.io.File;

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
    private Bitmap finalBitmap;
    public static final int REQUEST_CODE_CHOOSE = 100;

    @Override
    protected boolean isLazyLoad() {
        return true;
    }

    @Override
    protected int getContentLayout() {
        return R.layout.fragment_mine;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        MainActivity activity = (MainActivity) getActivity();
        activity.setListener(new MainActivity.onUserImageSelectedListener() {
            @Override
            public void userImageSelected(String path) {
                Bitmap suitBitmap = BitmapFactory.decodeFile(path);
                if (null != suitBitmap)
                    ivHeader.setImageBitmap(ImageUtil.toRoundBitmap(suitBitmap));

                uploadImgFile(path);
            }
        });
    }

    @Override
    protected void initView() {
        UserInfo user = BmobUser.getCurrentUser(UserInfo.class);
        if (null != user) {
            ImageUtil.setDefaultImageView(ivHeader, user.getUserImage(), R.mipmap.bg_pic_def_rect, getContext());
            tvName.setText(user.getUsername());
            if(!TextUtils.isEmpty(user.getNickName()))
               tvNickName.setText("昵称："+user.getNickName());
            else
               tvNickName.setText("昵称：游客");
        } else {
            tvNickName.setText("未登录");
        }
    }

    @Override
    protected void initData() {

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
        return rootView;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        if (!finalBitmap.isRecycled()) {
            finalBitmap.recycle();//回收图片所占的内存
            finalBitmap = null;
            System.gc();  //提醒系统及时回收
        }
    }

    @OnClick({R.id.iv_header, R.id.tv_check_update, R.id.tv_logout, R.id.rl_my_sim_pic,R.id.tv_nick_name})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_header:
                if (!OtherUtil.hasLogin()) {
                    startActivity(new Intent(getContext(), LoginActivity.class));
                } else {
                    Matisse.from(getActivity())
                            .choose(MimeType.ofAll())
                            .countable(true)
                            .maxSelectable(1)
                            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                            .thumbnailScale(0.85f)
                            .imageEngine(new GlideEngineImpl())
                            .forResult(REQUEST_CODE_CHOOSE);
                }
                break;
            case R.id.tv_check_update:
                BmobUpdateAgent.setUpdateListener(new BmobUpdateListener() {
                    @Override
                    public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
                        // TODO Auto-generated method stub
                        if (updateStatus == UpdateStatus.Yes) {//版本有更新

                        }else if(updateStatus == UpdateStatus.No){
                            Toast.makeText(getContext(), "版本无更新", Toast.LENGTH_SHORT).show();
                        }else if(updateStatus==UpdateStatus.EmptyField){//此提示只是提醒开发者关注那些必填项，测试成功后，无需对用户提示
                            Toast.makeText(getContext(), "请检查你AppVersion表的必填项，1、target_size（文件大小）是否填写；2、path或者android_url两者必填其中一项。", Toast.LENGTH_SHORT).show();
                        }else if(updateStatus==UpdateStatus.IGNORED){
                            Toast.makeText(getContext(), "该版本已被忽略更新", Toast.LENGTH_SHORT).show();
                        }else if(updateStatus==UpdateStatus.ErrorSizeFormat){
                            Toast.makeText(getContext(), "请检查target_size填写的格式，请使用file.length()方法获取apk大小。", Toast.LENGTH_SHORT).show();
                        }else if(updateStatus==UpdateStatus.TimeOut){
                            Toast.makeText(getContext(), "查询出错或查询超时", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                BmobUpdateAgent.update(getContext());
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
                showLogoutDialog();
                break;
            case R.id.tv_nick_name:
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("修改昵称");
                View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edittext,null);
                final EditText editText = dialogView.findViewById(R.id.edittext);
                builder.setView(dialogView);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        final String nickName = editText.getText().toString().trim();

                        UserInfo newUser = new UserInfo();
                        newUser.setNickName(nickName);
                        UserInfo userInfo = getCurrentUser(UserInfo.class);
                        showProgressDialog(getActivity(),true);
                        newUser.update(userInfo.getObjectId(),new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                stopProgressDialog();
                                if (e == null) {
                                    tvNickName.setText("昵称："+ nickName);
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
                    newUser.update(userInfo.getObjectId(),new UpdateListener() {
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
}
