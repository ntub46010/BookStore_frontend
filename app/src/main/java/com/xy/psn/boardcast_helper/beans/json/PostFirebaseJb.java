package com.xy.psn.boardcast_helper.beans.json;

import com.xy.psn.boardcast_helper.constants.KeyData;
import com.xy.psn.boardcast_helper.constants.Priority;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Xavier Yin on 10/5/16.
 */

public class PostFirebaseJb extends BaseJSONBean {
    private String message;
    private String productPhoto;
    private String productName;
    private String registerId;
    private String targetRegisterId;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getProductPhoto() {
        return productPhoto;
    }

    public void setProductPhoto(String productPhoto) {
        this.productPhoto = productPhoto;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getRegisterId() {
        return registerId;
    }

    public void setRegisterId(String registerId) {
        this.registerId = registerId;
    }

    public String getTargetRegisterId() {
        return targetRegisterId;
    }

    public void setTargetRegisterId(String targetRegisterId) {
        this.targetRegisterId = targetRegisterId;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject object = new JSONObject();
        JSONObject dataObj = new JSONObject();

        try {
            dataObj.put(KeyData.MESSAGE, this.getMessage());
            dataObj.put(KeyData.PRODUCT_PHOTO, this.getProductPhoto());
            dataObj.put(KeyData.PRODUCT_NAME, this.getProductName());

            object.put(KeyData.TO, this.getTargetRegisterId());
            object.put(KeyData.DATA, dataObj);

            LOGGER.d("json: " + object.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return object;
    }
}
