package com.pixeldp.model;

public class UserModel extends ResponseModel {
    private Integer userNum;
    private String emailAddress;
    private String password;
    private String phoneNum;
    private Integer sex;
    private Integer age;

    public UserModel(Integer error, String message, Integer userNum, String emailAddress, String password, String phoneNum, Integer sex, Integer age) {
        super(error, message);
        this.userNum = userNum;
        this.emailAddress = emailAddress;
        this.password = password;
        this.phoneNum = phoneNum;
        this.sex = sex;
        this.age = age;
    }

    public UserModel(String emailAddress, String password, String phoneNum, Integer age) {
        this(null, null, null, emailAddress, password, phoneNum, null, age);
    }

    public UserModel(String emailAddress, String password) {
        this(null, null, null, emailAddress, password, null, null, null);
    }

    public UserModel() {}

    public Integer getUserNum() {
        return userNum;
    }
    public String getEmailAddress() {
        return emailAddress;
    }
    public String getPassword() {
        return password;
    }
    public String getPhoneNum() {
        return phoneNum;
    }
    public Integer getSex() {
        return sex;
    }
    public Integer getAge() {
        return age;
    }

    public void setUserNum(Integer userNum) {
        this.userNum = userNum;
    }
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }
    public void setSex(Integer sex) {
        this.sex = sex;
    }
    public void setAge(Integer age) {
        this.age = age;
    }
}
