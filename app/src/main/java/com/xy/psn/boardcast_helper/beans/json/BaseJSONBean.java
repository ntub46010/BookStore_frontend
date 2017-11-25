package com.xy.psn.boardcast_helper.beans.json;

import com.xy.utils.Logger;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Xavier Yin on 10/4/16.
 */

public abstract class BaseJSONBean implements Serializable {
    protected static final long serialVersionUID = 1L;
    protected Logger LOGGER = Logger.getInstance(BaseJSONBean.class);

    public abstract JSONObject toJSONObject();
}
