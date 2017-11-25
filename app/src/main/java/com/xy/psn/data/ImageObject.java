package com.xy.psn.data;

import android.graphics.Bitmap;

public class ImageObject {
    private Bitmap bitmap;
    private String imagePath = null;
    private String fileName = null; //伺服器上的圖檔名稱
    private boolean isEntity = true; //是否為剛剛從手機選取的圖，而非透過下載或空白圖

    public ImageObject(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public ImageObject(Bitmap bitmap, boolean isEntity) {
        this.bitmap = bitmap;
        this.isEntity = isEntity;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setEntity(boolean isEntity) {
        this.isEntity = isEntity;
    }

    public boolean isEntity() {
        return isEntity;
    }
}
