package com.xy.psn.async_helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.net.URL;
import java.util.List;

public class GetBitmapPair  extends AsyncTask<Void, Void, Void> {
    private Context context;
    private List<ImageObj> imageObjs;

    // 宣告一個TaskListener介面, 由接收結果的物件實作.
    public interface TaskListener {
        void onFinished();
    }

    // 接收結果的物件
    private final TaskListener taskListener;

    public GetBitmapPair(Context context, List<ImageObj> imageObjs, TaskListener taskListener){
        this.context = context;
        this.imageObjs = imageObjs;
        this.taskListener = taskListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        taskListener.onFinished();
    }

    //  由圖片地址下載圖片
    @Override
    protected Void doInBackground(Void... params) {
        for(int i=0; i<imageObjs.size(); i++){
            ImageObj imageObj = imageObjs.get(i);
            imageObj.img = getImage(imageObj.getImgURL());
            imageObj.img2 = getImage(imageObj.getImgURL2());
        }
        return null;
    }

    private Bitmap getImage(String bitmapUrl) {
        URL url;
        Bitmap image = null;
        try {
            url = new URL(bitmapUrl);
            image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        }catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }
}
