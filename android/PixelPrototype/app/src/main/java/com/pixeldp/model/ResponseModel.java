package com.pixeldp.model;


import com.pixeldp.util.GsonUtil;

public class ResponseModel {
    private Integer code;
    private String message;

    public ResponseModel(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public ResponseModel(Integer code) {
        this(code, "no message");
    }

    public ResponseModel() { this(0, "no message"); }

    public Integer getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }
    public void setCode(Integer code) {
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