package com.xy.psn.boardcast_helper.apis;

import android.content.Context;

import com.xy.network.base.BaseApiManager;
import com.xy.network.base.IApiResponseListener;
import com.xy.network.base.JsonObjApiFactory;
import com.xy.network.beans.BaseHeaderBean;

import org.json.JSONObject;

/**
 * Created by Xavier Yin on 10/4/16.
 */

public class PostFirebaseApiManager extends BaseApiManager {
    private static PostFirebaseApiManager FIREBASE_MANAGER_INSTANCE = null;

    private JsonObjApiFactory<String> postFirebaseApi;

    private PostFirebaseApiManager(Context context) {
        this.context = context;
    }

    public synchronized final static PostFirebaseApiManager getMemberApiManager(Context context) {
        if (FIREBASE_MANAGER_INSTANCE == null) {
            FIREBASE_MANAGER_INSTANCE = new PostFirebaseApiManager(context);
        }

        return FIREBASE_MANAGER_INSTANCE;
    }

    public void launchPostFirebaseApi(BaseHeaderBean headerBean, JSONObject jsonBean, IApiResponseListener<String> apiResponseListener) {
        this.postFirebaseApi = new PostFirebaseApi<>(this.context, headerBean, jsonBean, apiResponseListener);
        this.postFirebaseApi.launchRequest();
    }

    public void cancelPostFirebaseApi() {
        if (this.postFirebaseApi != null) {
            this.postFirebaseApi.cancelAllRequest();
        }
    }
}
