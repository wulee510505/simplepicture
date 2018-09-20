package com.wulee.simplepicture.bean;

import cn.bmob.v3.BmobUser;

/**
 * Created by wulee on 2018/09/05
 */

public class UserInfo extends BmobUser {
    private String userImage;
    private String nickName;
    private String platfotm;

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getPlatfotm() {
        return platfotm;
    }

    public void setPlatfotm(String platfotm) {
        this.platfotm = platfotm;
    }
}
