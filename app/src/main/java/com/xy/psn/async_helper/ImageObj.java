package com.xy.psn.async_helper;

import android.graphics.Bitmap;

public class ImageObj {
    public String imgURL, imgURL2, imgURL3, imgURL4, imgURL5;   //圖片網址
    public Bitmap img, img2, img3, img4, img5;      //圖片

    public ImageObj() {}

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

    public Bitmap getImg() {
        return img;
    }

    public Bitmap getImg2() {
        return img2;
    }

    public Bitmap getImg3() {
        return img3;
    }

    public Bitmap getImg4() {
        return img4;
    }

    public Bitmap getImg5() {
        return img5;
    }

    public void setImgURL(String url) {
        this.imgURL = url;
    }

    public void setImgURL2(String url) {
        this.imgURL2 = url;
    }

    public void setImg(Bitmap bitmap) {
        this.img = bitmap;
    }

    public void recycleImg() {
        img.recycle();
        img = null;
    }

}
