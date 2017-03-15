package prototype_model_handler;

import connect.Database;
import prototype_model.EyeModel;
import prototype_model.ResponseModel;
import prototype_model.UserModel;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class EyeHandler {
    private static Database db;

    public EyeHandler(Database _db) {
        db = _db;
    }

    public boolean isExistEyeInfo(EyeModel eyeModel) throws Exception {
        return db.exist(
                "SELECT * FROM PROTOTYPE_EYE WHERE userNum=?",
                eyeModel.getUserNum()
        );
    }

    public ResponseModel getEyeInfo(UserModel userModel) throws Exception {
        List<Map<String, Object>> result = db.select(
                "SELECT * FROM PROTOTYPE_EYE WHERE userNum=? ORDER BY infoTime DESC LIMIT 1",
                userModel.getUserNum()
        );

        if (result.size() <= 0) {
            return new ResponseModel(202, "There is no eye data.");
        }

        Map<String, Object> eyeInfo = result.get(0);
        int id = ((Long) eyeInfo.get("id")).intValue();
        int userNum = ((Long) eyeInfo.get("userNum")).intValue();
        float leftDi = (Float) eyeInfo.get("leftDi");
        float rightDi = (Float) eyeInfo.get("rightDi");
        Date infoTime = (Date) eyeInfo.get("infoTime");

        return new EyeModel(200, "success", id, userNum, leftDi, rightDi, infoTime);
    }

    public ResponseModel insertEyeInfo(EyeModel eyeModel) throws Exception {
        int affectedRowsNum = db.update(
                "INSERT INTO PROTOTYPE_EYE(userNum, leftDi, rightDi, infoTime) VALUES (?, ?, ?, ?)",
                eyeModel.getUserNum(),
                eyeModel.getLeftDi(),
                eyeModel.getRightDi(),
                new Timestamp(System.currentTimeMillis())
        );

        if (affectedRowsNum > 0) {
            return new ResponseModel(200, "success");
        } else {
            return new ResponseModel(201, "There is no affected rows while inserting eye data");
        }
    }

    public ResponseModel updateEyeInfo(EyeModel eyeModel) throws Exception {
        int affectedRowsNum = db.update(
                "UPDATE PROTOTYPE_EYE SET leftDi=?, rightDi=?, infoTime=? WHERE userNum=?",
                eyeModel.getLeftDi(),
                eyeModel.getRightDi(),
                new Timestamp(System.currentTimeMillis()),
                eyeModel.getUserNum()
        );

        if (affectedRowsNum > 0) {
            return new ResponseModel(200, "success");
        } else {
            return new ResponseModel(201, "There is no affected rows while updating eye data");
        }
    }

    public ResponseModel deleteEyeInfo(int id) throws Exception {
        int affectedRowsNum = db.update(
                "DELETE FROM PROTOTYPE_EYE WHERE id=?",
                id
        );

        if (affectedRowsNum > 0) {
            return new ResponseModel(200, "success");
        } else {
            return new ResponseModel(201, "There is no affected rows while deleting eye data");
        }
    }
}