package com.xy.psn.boardcast_helper.apis;

import android.content.Context;

import com.xy.network.base.IApiResponseListener;
import com.xy.network.base.JsonObjApiFactory;
import com.xy.network.beans.BaseHeaderBean;
import com.xy.network.builders.JsonObjRequestBuilder;
import com.xy.network.configs.RequestMethod;
import com.xy.network.helpers.URLHelper;

import org.json.JSONObject;

/**
 * Created by Xavier Yin on 10/5/16.
 */

public class PostFirebaseApi<T> extends JsonObjApiFactory<T> {

    public PostFirebaseApi(Context context, BaseHeaderBean baseHeaderBean, JSONObject jsonBean, IApiResponseListener<T> apiResponseListener) {
        super(context, baseHeaderBean, jsonBean, apiResponseListener);
    }

    @Override
    protected JsonObjRequestBuilder<T> createBJRB() {
        return new JsonObjRequestBuilder<T>(this.requestMethod, this.url, this.jsonBean).
                buildContext(this.context).
                buildClazz(String.class).
                buildHeader(this.header).
                buildTag(this.context).
                buildApiResponseListener(this.apiResponseListener);
    }

    @Override
    protected int initRequestMethod() {
        return RequestMethod.API_POST.getApiRequestMethod();
    }

    @Override
    protected String initUrl() {
        return URLHelper.contactUrl("https://fcm.googleapis.com/fcm/send", null);
    }
}
