package com.wulee.simplepicture.bean;

import cn.bmob.v3.BmobObject;

/**
 * create by  wulee   2018/9/7 13:05
 * desc:
 */
public class StickFigureImgObj extends BmobObject {

    private UserInfo userInfo;
    private String name;
    private  String[] imageGroup;
    private String type;
    private int likeNum;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getImageGroup() {
        return imageGroup;
    }

    public void setImageGroup(String[] imageGroup) {
        this.imageGroup = imageGroup;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public int getLikeNum() {
        return likeNum;
    }

    public void setLikeNum(int likeNum) {
        this.likeNum = likeNum;
    }
}
