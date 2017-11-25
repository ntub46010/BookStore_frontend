package com.xy.psn.data;

import android.graphics.Bitmap;

import com.xy.psn.async_helper.ImageObj;

public class Chat extends ImageObj {
    private String imgURL;
    private String msg, date, innerDate, time;
    private String name, product, member, sender;

    public Chat (String sender, Bitmap avatar, String msg, String date, String innerDate, String time) {//用於聊天室每則訊息2
        this.sender = sender;
        this.img = avatar;
        this.msg = msg;
        this.date = date;
        this.innerDate = innerDate;
        this.time = time;
    }

    public Chat (String imgURL, String name, String msg, String date, String time, String product, String member) {//信箱
        this.imgURL = imgURL;
        this.msg = msg;
        this.date = date;
        this.time = time;
        this.name = name;
        this.product = product;
        this.member = member;
    }

    public String getImgURL() {
        return imgURL;
    }

    public String getMsg() {
        return msg;
    }

    public String getDate() {
        return date;
    }

    public String getInnerDate() {
        return innerDate;
    }

    public String getTime() {
        return time;
    }

    public String getName() {
        return name;
    }

    public String getProduct() {
        return product;
    }

    public String getMember() {
        return member;
    }

    public String getSender() {
        return sender;
    }

    public void setDate(String date) {
        this.date = date;
    }

}
