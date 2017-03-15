package com.pixeldp.model;

public class UserModel extends ResponseModel {
    private Integer userNum;
    private String accountName;
    private Integer sex;
    private Integer age;

    public UserModel(int error, String message, Integer userNum, String accountName) {
        super(error, message);
        this.userNum = userNum;
        this.accountName = accountName;
    }

    public UserModel(int error, String message, String accountName) {
        this(error, message, null, accountName);
    }

    public Integer getUserNum() {
        return userNum;
    }
    public String getAccountName() {
        return accountName;
    }
    public Integer getAge() {
        return age;
    }
    public int getSex() {
        return sex;
    }

    public void setUserNum(Integer userNum) {
        this.userNum = userNum;
    }
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
    public void setAge(Integer age) {
        this.age = age;
    }
    public void setSex(Integer sex) {
        this.sex = sex;
    }
}
