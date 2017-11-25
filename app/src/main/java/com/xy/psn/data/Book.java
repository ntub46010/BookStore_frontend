package com.xy.psn.data;

import com.xy.psn.async_helper.ImageObj;

import java.io.Serializable;

public class Book extends ImageObj implements Serializable {
    private String imgURL, imgURL2, imgURL3, imgURL4, imgURL5;
    private String id, title;
    private String price, dep, seller, sellerName, sellerAvatar;
    private String status, note, ps, postDate, editDate;

    public Book(String id, String imgURL, String title) {
        this.id = id;
        this.imgURL = imgURL;
        this.title = title;
    }

    public Book(String id, String imgURL, String title, String price) {
        this.id = id;
        this.imgURL = imgURL;
        this.title = title;
        this.price = price;
    }

    public Book(String id, String imgURL, String title, String price, String seller) {
        this.id = id;
        this.imgURL = imgURL;
        this.title = title;
        this.price = price;
        this.seller = seller;
    }

    //用於商品編輯
    public Book(String id, String imgURL, String imgURL2, String imgURL3, String imgURL4, String imgURL5, String title, String status, String note, String price, String ps, String seller) {
        this.id = id;
        this.imgURL = imgURL;
        this.imgURL2 = imgURL2;
        this.imgURL3 = imgURL3;
        this.imgURL4 = imgURL4;
        this.imgURL5 = imgURL5;
        this.title = title;
        this.price = price;
        this.seller = seller;
        this.status = status;
        this.note = note;
        this.ps = ps;
    }

    //用於商品詳情、聊天室
    public Book(String id, String imgURL, String imgURL2, String imgURL3, String imgURL4, String imgURL5, String title, String status, String note, String price, String ps, String seller, String sellerName, String sellerAvatar, String dep, String postDate, String editDate) {
        this.id = id;
        this.imgURL = imgURL;
        this.imgURL2 = imgURL2;
        this.imgURL3 = imgURL3;
        this.imgURL4 = imgURL4;
        this.imgURL5 = imgURL5;
        this.title = title;
        this.price = price;
        this.dep = dep;
        this.seller = seller;
        this.sellerName = sellerName;
        this.status = status;
        this.note = note;
        this.ps = ps;
        this.postDate = postDate;
        this.editDate = editDate;
        this.sellerAvatar = sellerAvatar;
    }

    public Book(String id, String imgURL, String title, String status, String note, String price, String ps, String seller, String sellerName) {
        this.id = id;
        this.imgURL = imgURL;
        this.title = title;
        this.price = price;
        this.seller = seller;
        this.sellerName = sellerName;
        this.status = status;
        this.note = note;
        this.ps = ps;
    }

    public String getImgURL() {
        return imgURL;
    }

    public String getImgURL2() {
        return imgURL2;
    }

    public String getImgURL3() {
        return imgURL3;
    }

    public String getImgURL4() {
        return imgURL4;
    }

    public String getImgURL5() {
        return imgURL5;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getPrice() {
        return price;
    }

    public String getDep() {
        return dep;
    }

    public String getSeller() {
        return this.seller;
    }

    public String getSellerName() {
        return this.sellerName;
    }

    public String getSellerAvatar() {
        return this.sellerAvatar;
    }

    public String getStatus(){return this.status;}

    public String getNote(){return this.note;}

    public String getPs(){return this.ps;}

    public String getPostDate(){return this.postDate;}

    public String getEditDate(){return this.editDate;}

    public void setTitle(String title) {
        this.title = title;
    }
}