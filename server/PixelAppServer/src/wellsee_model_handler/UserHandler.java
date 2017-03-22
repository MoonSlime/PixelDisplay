package wellsee_model_handler;

import model.ResponseModel;
import model.UserModel;
import connect.Database;

import javax.servlet.http.HttpSession;

public class UserHandler {
    private static Database db;

    public UserHandler(Database _db) {
        db = _db;
    }

    public ResponseModel loginUser(String accountName, HttpSession session) throws Exception {
        boolean isExistAccount = db.exist(
                "SELECT * FROM WELLSEE_USER WHERE accountName=?",
                accountName
        );

        int affectedRowsNum = db.update(
                isExistAccount ? "UPDATE WELLSEE_USER SET lastLoginTime=now() WHERE accountName=?" : "INSERT INTO WELLSEE_USER (accountName, lastLoginTime ,joinTime) VALUES (?, now(), now())", // 존재 할 경우==>로그인 시간 최신화 / 아닐 경우==>계정 등록
                accountName
        );

        if (affectedRowsNum == 0) {
            return new ResponseModel(201, "There is no affected rows num.");
        }

        Integer userNum = ((Long) db.value(
                "SELECT * FROM WELLSEE_USER WHERE accountName=?",
                "userNum",
                accountName
        )).intValue();

        UserModel userModel = new UserModel(200, "success", userNum, accountName);
        session.setAttribute("userModel", userModel);
        session.setMaxInactiveInterval(Integer.MAX_VALUE);

        return userModel;
    }

    /*
    ERROR CODE 200 개인 정보 추가 성공
    ERROR CODE 201 실패
    */
    public ResponseModel update(UserModel model) throws Exception {

        int affectedRowsNum = db.update(
                "UPDATE WELLSEE_USER SET sex=?, age=? WHERE accountName=?",
                model.getSex(),
                model.getAge(),
                model.getAccountName()
        );

        if (affectedRowsNum > 0) {
            return new ResponseModel(200, "success");
        } else {
            return new ResponseModel(201, "There is no affected rows while updating user's age and sex");
        }
    }

    /*
    ERROR CODE 200 계정 삭제 성공
    ERROR CODE 201 계정 삭제 실패
    */
    public ResponseModel deleteUser(UserModel model) throws Exception {
        int affectedRowsNum = db.update(
                "DELETE FROM WELLSEE_USER WHERE accountName=?",
                model.getAccountName()
        );

        if (affectedRowsNum > 0) {
            return new ResponseModel(200, "success");
        } else {
            return new ResponseModel(201, "There is no affected rows while deleting user data");
        }
    }
}