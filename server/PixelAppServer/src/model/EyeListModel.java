package model;

import util.GsonUtil;

import java.util.ArrayList;

public class EyeListModel extends ResponseModel {
    ArrayList<EyeModel> eyeModels;

    public EyeListModel(int code, String message, ArrayList<EyeModel> eyeModels) {
        super(code, message);
        this.eyeModels = eyeModels;
    }

    public ArrayList<EyeModel> getEyeModels() {
        return eyeModels;
    }
    public void setEyeModels(ArrayList<EyeModel> eyeModels) {
        this.eyeModels = eyeModels;
    }
}
