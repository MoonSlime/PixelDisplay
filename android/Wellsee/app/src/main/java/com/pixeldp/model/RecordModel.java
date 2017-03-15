package com.pixeldp.model;

import com.pixeldp.util.GsonUtil;

import java.text.SimpleDateFormat;

public class RecordModel extends ResponseModel {
    private int id;
    private int level_visualAcuity; // 0 ~ 9
    private int level_astigmatism; // 0 ~ 3
    private int level_colorBlindness; // 0 ~ 3
    private String date;

    public RecordModel(EyeModel eyeModel) {
        level_visualAcuity = 9 - (int) (eyeModel.getLevel_visualAcuity() * (9.0f / 15.0f)); // 0 ~ 9
        level_astigmatism = eyeModel.getLevel_astigmatism(); // 0 ~ 3
        level_colorBlindness = eyeModel.getLevel_colorBlindness(); // 0 ~ 3

        if(eyeModel.getId() != null) {
            id = eyeModel.getId();
        }
        if (eyeModel.getInfoTime() != null) {
            date = new SimpleDateFormat("yy.MM.dd").format(eyeModel.getInfoTime());
        }
    }

    public int getId() {
        return id;
    }
    public int getLevel_visualAcuity() {
        return level_visualAcuity;
    }
    public int getLevel_astigmatism() {
        return level_astigmatism;
    }
    public int getLevel_colorBlindness() {
        return level_colorBlindness;
    }
    public String getDate() {
        return date;
    }

    public void setId(int id) {
        this.id = id;
    }
    public void setLevel_visualAcuity(int level_visualAcuity) {
        this.level_visualAcuity = level_visualAcuity;
    }
    public void setLevel_astigmatism(int level_astigmatism) {
        this.level_astigmatism = level_astigmatism;
    }
    public void setLevel_colorBlindness(int level_colorBlindness) {
        this.level_colorBlindness = level_colorBlindness;
    }
    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return GsonUtil.serialize(this);
    }
}
