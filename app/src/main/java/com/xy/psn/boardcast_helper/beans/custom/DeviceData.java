package com.xy.psn.boardcast_helper.beans.custom;

import com.xy.utils.Logger;

import java.io.Serializable;

/**
 * Created by Xavier Yin on 10/4/16.
 */

public class DeviceData implements Serializable {
    private static final long serialVersionUID = 1L;

    private Logger LOGGER = Logger.getInstance(DeviceData.class);

    private String device;
    private String token;

    // Important Constructor without Arguments
    public DeviceData() {
    }

    public DeviceData(String token) {
        this.setDevice("android");
        this.setToken(token);
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
