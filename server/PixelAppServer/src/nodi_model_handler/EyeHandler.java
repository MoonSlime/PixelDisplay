package nodi_model_handler;

import model.EyeListModel;
import model.ResponseModel;
import model.EyeModel;
import model.UserModel;
import connect.Database;

import java.sql.*;
import java.util.*;
import java.util.Date;

public class EyeHandler {
    private static Database db;

    public EyeHandler(Database _db) {
        db = _db;
    }

    public boolean isExistEyeInfo(EyeModel eyeModel) throws Exception {
        return db.exist(
                "SELECT * FROM NODI_EYE WHERE userNum=?",
                eyeModel.getUserNum()
        );
    }

    public ResponseModel insertEye(EyeModel eyeModel) throws Exception {
        int affectedRowsNum = db.update(
                "INSERT INTO NODI_EYE(userNum, level_visualAcuity, level_astigmatism, level_colorBlindness, infoTime) VALUES (?, ?, ?, ?, ?)",
                eyeModel.getUserNum(),
                eyeModel.getLevel_visualAcuity(),
                eyeModel.getLevel_astigmatism(),
                eyeModel.getLevel_colorBlindness(),
                new Timestamp(System.currentTimeMillis())
        );

        if (affectedRowsNum > 0) {
            return new ResponseModel(200, "success");
        } else {
            return new ResponseModel(201, "There is no affected rows while inserting eye data");
        }
    }

    public ResponseModel optimize(EyeModel eyeModel) throws Exception {
        db.transaction_start();
        db.update(
                "UPDATE NODI_EYE SET isOptimized=0 WHERE id=? AND !(isOptimized=0)",
                eyeModel.getId()
        );
        int affectedRowsNum = db.update(
                "UPDATE NODI_EYE SET isOptimized=? WHERE id=?",
                eyeModel.getOptimized() ? 1 : 0,
                eyeModel.getId()
        );
        db.transaction_finish();

        if (affectedRowsNum > 0) {
            return new ResponseModel(200, "success");
        } else {
            return new ResponseModel(201, "There is no affected rows while updating isOptimized");
        }
    }

    public ResponseModel getEyeInfo(UserModel userModel) throws Exception {
        List<Map<String, Object>> result = db.select(
                "SELECT * FROM NODI_EYE WHERE userNum=? ORDER BY infoTime DESC",
                userModel.getUserNum()
        );

        if (result.size() <= 0) {
            return new ResponseModel(201, "There is no eye info.");
        }

        ArrayList<EyeModel> eyeModels = new ArrayList<>();
        for (Map<String, Object> eyeInfo : result) {
            int id = (Integer) eyeInfo.get("id");
            int userNum = ((Long) eyeInfo.get("userNum")).intValue();
            float level_visualAcuity = (Float) eyeInfo.get("level_visualAcuity");
            int level_astigmatism = (Integer) eyeInfo.get("level_astigmatism");
            int level_colorBlindness = (Integer) eyeInfo.get("level_colorBlindness");
            Date infoTime = (Date) eyeInfo.get("infoTime");
            boolean isOptimized = (Boolean) eyeInfo.get("isOptimized");

            EyeModel eyeModel = new EyeModel(200, "success", id, userNum, level_visualAcuity, level_astigmatism, level_colorBlindness, infoTime, isOptimized);
            eyeModels.add(eyeModel);
        }

        return new EyeListModel(200, "success", eyeModels);
    }

    public ResponseModel getLastEyeInfo(UserModel userModel) throws Exception {
        List<Map<String, Object>> result = db.select(
                "SELECT * FROM NODI_EYE WHERE userNum=? ORDER BY infoTime DESC LIMIT 1",
                userModel.getUserNum()
        );

        if (result.size() <= 0) {
            return new ResponseModel(202, "There is no eye data.");
        }

        Map<String, Object> eyeInfo = result.get(0);

        int id = (Integer) eyeInfo.get("id");
        int userNum = ((Long) eyeInfo.get("userNum")).intValue();
        float level_visualAcuity = (Float) eyeInfo.get("level_visualAcuity");
        int level_astigmatism = (Integer) eyeInfo.get("level_astigmatism");
        int level_colorBlindness = (Integer) eyeInfo.get("level_colorBlindness");
        Date infoTime = (Date) eyeInfo.get("infoTime");
        boolean isOptimized = (Boolean) eyeInfo.get("isOptimized");

        return new EyeModel(200, "success", id, userNum, level_visualAcuity, level_astigmatism, level_colorBlindness, infoTime, isOptimized);
    }

    public ResponseModel deleteEyeInfo(int id) throws Exception {
        int affectedRowsNum = db.update(
                "DELETE FROM NODI_EYE WHERE id=?",
                id
        );

        if (affectedRowsNum > 0) {
            return new ResponseModel(200, "success");
        } else {
            return new ResponseModel(201, "There is no affected rows while deleting eye data");
        }
    }
}