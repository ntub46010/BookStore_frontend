package com.xy.psn.data;

import com.xy.psn.async_helper.ImageObj;

public class Member extends ImageObj {
    private String imgURL;
    private String name, department, email;
    private String positive, negative;

    //編輯個人檔案
    public Member (String imgURL, String name, String department, String email) {
        this.imgURL = imgURL;
        this.name = name;
        this.department = department;
        this.email = email;
    }

    //顯示個人檔案
    public Member (String imgURL, String name, String department, String positive, String negative, String email) {
        this.imgURL = imgURL;
        this.name = name;
        this.department = department;
        this.email = email;
        this.positive = positive;
        this.negative = negative;
    }

    public String getImgURL() {
        return imgURL;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public String getEmail() {
        return email;
    }

    public String getPositive() {
        return positive;
    }

    public String getNegative() {
        return negative;
    }
}
