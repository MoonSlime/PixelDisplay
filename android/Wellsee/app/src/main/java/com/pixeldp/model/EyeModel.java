package com.pixeldp.model;


import java.util.Date;

public class EyeModel extends ResponseModel {
    private Integer id;
    private Integer userNum;
    private Float level_visualAcuity;
    private Integer level_astigmatism; // 있음:3, yes:2, no:1, 없음:0
    private Integer level_colorBlindness; // 있음:3, 틀림:2, 다맞춤:1, 없음:0
    private Date infoTime;
    private Boolean isOptimized;

    public EyeModel(int error, String message, Integer id, Integer userNum, Float level_visualAcuity, Integer level_astigmatism, Integer level_colorBlindness, Date infoTime, Boolean isOptimized) {
        super(error, message);
        this.id = id;
        this.userNum = userNum;
        this.level_visualAcuity = level_visualAcuity;
        this.level_astigmatism = level_astigmatism;
        this.level_colorBlindness = level_colorBlindness;
        this.infoTime = infoTime;
        this.isOptimized = isOptimized;
    }

    public EyeModel(int error, String message, Integer userNum, Float level_visualAcuity, Integer level_astigmatism, Integer level_colorBlindness) {
        this(error, message, null, userNum, level_visualAcuity, level_astigmatism, level_colorBlindness, null, false);
    }

    public EyeModel(int error, String message, Integer userNum, Float level_visualAcuity) {
        this(error, message, null, userNum, level_visualAcuity, 0, 0, null, false);
    }

    public EyeModel() {}

    public Integer getId() {
        return id;
    }
    public Integer getUserNum() {
        return userNum;
    }
    public Float getLevel_visualAcuity() {
        return level_visualAcuity;
    }
    public Integer getLevel_astigmatism() {
        return level_astigmatism;
    }
    public Integer getLevel_colorBlindness() {
        return level_colorBlindness;
    }
    public Date getInfoTime() {
        return infoTime;
    }
    public Boolean getOptimized() {
        return isOptimized;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    public void setUserNum(Integer userNum) {
        this.userNum = userNum;
    }
    public void setLevel_visualAcuity(Float level_visualAcuity) {
        this.level_visualAcuity = level_visualAcuity;
    }
    public void setLevel_astigmatism(Integer level_astigmatism) {
        this.level_astigmatism = level_astigmatism;
    }
    public void setLevel_colorBlindness(Integer level_colorBlindness) {
        this.level_colorBlindness = level_colorBlindness;
    }
    public void setInfoTime(Date infoTime) {
        this.infoTime = infoTime;
    }
    public void setOptimized(Boolean optimized) {
        isOptimized = optimized;
    }
}