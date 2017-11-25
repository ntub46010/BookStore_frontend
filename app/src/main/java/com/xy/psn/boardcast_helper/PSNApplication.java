package com.xy.psn.boardcast_helper;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import com.xy.network.helpers.VolleyRequestHelper;
import com.xy.psn.boardcast_helper.managers.UserDataManager;
import com.xy.utils.Logger;

/**
 * Created by Xavier Yin on 9/30/16.
 */

public class PSNApplication extends Application {
    private static Logger LOGGER = Logger.getInstance(PSNApplication.class);

    private static Context APPLICATION;
    private static Resources RESOURCE;

    @Override
    public void onCreate() {
        super.onCreate();
        this.initData();
    }

    public static Context getAPPLICATION() {
        return APPLICATION;
    }

    public static Resources getRESOURCE() {
        return RESOURCE;
    }

    private void initData() {
        LOGGER.d("PSNApplication");

        APPLICATION = this;
        RESOURCE = this.getResources();

        VolleyRequestHelper.init(this);
        UserDataManager.initialize(this.getApplicationContext());
    }
}
