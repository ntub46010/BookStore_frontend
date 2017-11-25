package com.xy.psn.boardcast_helper.beans.custom;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xavier Yin on 10/5/16.
 */

public class UserData {
    public static final String DATABASE_USERS = "users";

    private List<DeviceData> deviceList;

    public UserData() {
        this.deviceList = new ArrayList<>();
    }

    public void addDevice(DeviceData device) {
        deviceList.add(device);
    }

    public List<DeviceData> getDeviceList() {
        return deviceList;
    }
}
