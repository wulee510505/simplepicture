package com.wulee.simplepicture.bean;

import cn.bmob.v3.BmobUser;

/**
 * Created by wulee on 2018/09/05
 */

public class UserInfo extends BmobUser {
    private String userImage;
    private String nickName;

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
}
