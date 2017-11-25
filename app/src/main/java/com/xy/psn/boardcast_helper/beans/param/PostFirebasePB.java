package com.xy.psn.boardcast_helper.beans.param;

import com.xy.network.beans.BaseHeaderBean;

/**
 * Created by Xavier Yin on 10/5/16.
 */

public class PostFirebasePB extends BaseHeaderBean {

    public PostFirebasePB(String key) {
        this.header.put("Authorization", key);
        this.header.put("Content-Type", "application/json; charset=utf-8");
    }
}
