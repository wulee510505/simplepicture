package com.wulee.simplepicture.base;

import com.wulee.simplepicture.utils.SDCardUtils;

/**
 * Created by wulee on 2017/5/22 16:08
 */

public interface Constant {

    String ROOT_PATH = String.format("%s%s", SDCardUtils.getESDString(), "/simplepicture/");// 根目录
    String LOG_PATH = String.format("%slog/", ROOT_PATH);// 日志目录
    String AVATAR_PATH = String.format("%savatar/", ROOT_PATH);// 头像目录
    String CRASH_PATH = String.format("%scrash/", ROOT_PATH);// 异常信息的目录
    String SAVE_PIC = String.format("%ssavepic/", ROOT_PATH);// 图片保存的目录
    String SAVE_AUDIO = String.format("%ssaveaudio/", ROOT_PATH);// 音频保存的目录


    String TEMP_FILE_PATH = String.format("%stemp/", ROOT_PATH);// 临时文件存放的目录

    String BOMB_APP_ID = "f13f2de697a3e7d165f9572f77af51bc";
    //String BOMB_APP_ID = "994528a22f6d76397dc7fcef656a902f";//测试版


    String KEY_LAST_CHECK_UPDATE_TIME = "key_last_check_update_time";
    long CHECK_UPDATE_INTERVAL = 10 * 60 * 1000;// 10分钟

    String KEY_LAST_UPDATE_CURR_USERINFO_TIME = "key_last_update_curr_userinfo_time";
    long UPDATE_CURR_USERINFO_INTERVAL = 5 * 60 * 1000;// 5分钟
}
