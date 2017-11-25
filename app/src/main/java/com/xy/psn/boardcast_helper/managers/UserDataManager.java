package com.xy.psn.boardcast_helper.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.xy.utils.Logger;

/**
 * Created by Xavier Yin on 10/4/16.
 */

public class UserDataManager {
    private static Logger LOGGER = Logger.getInstance(UserDataManager.class);

    private static final String PUSH_TOKEN = "push_token";
    private static UserDataManager INSTANCE = null;

    private SharedPreferences noClearSharedPreferences;
    private SharedPreferences.Editor noClearEditor;
    private String pushToken;

    private UserDataManager(Context context) {
        this.init(context);
    }

    public synchronized static UserDataManager getInstance() {
        return INSTANCE;
    }

    public synchronized static void initialize(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new UserDataManager(context);
        }
    }

    public void setPushToken(String pushToken) {
        LOGGER.d("commit token: " + pushToken);

        this.pushToken = pushToken;
        this.noClearEditor.putString(PUSH_TOKEN, pushToken);
        this.noClearEditor.commit();
    }

    public String getPushToken() {
        return this.pushToken;
    }

    private void init(Context context) {
        LOGGER.d("UserDataManager Init");

        this.noClearSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.noClearEditor = noClearSharedPreferences.edit();
        this.pushToken = noClearSharedPreferences.getString(PUSH_TOKEN, null);
    }
}
