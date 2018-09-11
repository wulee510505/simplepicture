package com.wulee.simplepicture;

import android.app.Application;
import android.content.Context;

import com.wulee.simplepicture.utils.ACache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobConfig;

import static com.wulee.simplepicture.base.Constant.BOMB_APP_ID;

/**
 * Created by wulee on 2016/12/8 09:37
 */

public class App extends Application {

   public static Context context;

    private static App INSTANCE;

    public static App INSTANCE() {
        return INSTANCE;
    }

    private void setInstance(App app) {
        setBmobIMApplication(app);
    }

    private static void setBmobIMApplication(App a) {
        App.INSTANCE = a;
    }

    public static ACache mACache;
    @Override
    public void onCreate() {
        super.onCreate();
        setInstance(this);
        context = getApplicationContext();
        mACache =  ACache.get(this);
        initBmobSDK();
    }


    private void initBmobSDK() {
        BmobConfig config = new BmobConfig.Builder(this)
                .setApplicationId(BOMB_APP_ID)  //设置appkey
                .setConnectTimeout(30)//请求超时时间（单位为秒）：默认15s
                .setUploadBlockSize(1024 * 1024)//文件分片上传时每片的大小（单位字节），默认512*1024
                .setFileExpiration(2500)//文件的过期时间(单位为秒)：默认1800s
                .build();
        Bmob.initialize(config);
    }

    /**
     * 获取当前运行的进程名
     * @return
     */
    public static String getMyProcessName() {
        try {
            File file = new File("/proc/" + android.os.Process.myPid() + "/" + "cmdline");
            BufferedReader mBufferedReader = new BufferedReader(new FileReader(file));
            String processName = mBufferedReader.readLine().trim();
            mBufferedReader.close();
            return processName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
