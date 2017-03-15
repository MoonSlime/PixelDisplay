package com.pixeldp.model;


import com.pixeldp.util.GsonUtil;

public class ResponseModel {
    private int code;
    private String message;

    public ResponseModel(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public ResponseModel(int code) {
        this(code, "no message");
    }

    public ResponseModel() { this(0, "no message"); }

    public int getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }
    public void setCode(int code) {
        this.code = code;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return GsonUtil.serialize(this);
    }
}