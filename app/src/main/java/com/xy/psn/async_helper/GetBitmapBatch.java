package com.xy.psn.async_helper;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.xy.psn.R;

import java.net.URL;
import java.util.List;

public class GetBitmapBatch extends AsyncTask<Void, Void, Void> {
    private Context context;
    private List<ImageObj> imageObjs;
    private Resources res;

    // 宣告一個TaskListener介面, 由接收結果的物件實作.
    public interface TaskListener {
        void onFinished();
    }

    // 接收結果的物件
    private final TaskListener taskListener;

    public GetBitmapBatch(Context context, Resources res, List<ImageObj> imageObjs, TaskListener taskListener){
        this.context = context;
        this.imageObjs = imageObjs;
        this.taskListener = taskListener;
        this.res = res;
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
            if (!imageObj.getImgURL().equals(res.getString(R.string.image_link))) imageObj.img = getImage(imageObj.getImgURL());
            if (!imageObj.getImgURL2().equals(res.getString(R.string.image_link))) imageObj.img2 = getImage(imageObj.getImgURL2());
            if (!imageObj.getImgURL3().equals(res.getString(R.string.image_link))) imageObj.img3 = getImage(imageObj.getImgURL3());
            if (!imageObj.getImgURL4().equals(res.getString(R.string.image_link))) imageObj.img4 = getImage(imageObj.getImgURL4());
            if (!imageObj.getImgURL5().equals(res.getString(R.string.image_link))) imageObj.img5 = getImage(imageObj.getImgURL5());
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
