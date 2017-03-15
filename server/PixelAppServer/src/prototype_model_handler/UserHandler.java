package prototype_model_handler;

import connect.Database;
import prototype_model.ResponseModel;
import prototype_model.UserModel;

import java.util.List;
import java.util.Map;

public class UserHandler {
    private static Database db;

    public UserHandler(Database _db) {
        db = _db;
    }

    /*
    RESPONSE CODE 200 가입 성공
    RESPONSE CODE 201 이미 존재하는 계정
    RESPONSE CODE 202 가입 실패
    */
    public ResponseModel join(UserModel userModel) throws Exception {
        boolean isExistAccount = db.exist(
                "SELECT * FROM PROTOTYPE_USER WHERE accountName=?",
                userModel.getEmailAddress()
        );

        if (!isExistAccount) {
            return new ResponseModel(201, "There is already existing account.");
        } else {
            int affectedRowsNum = db.update(
                    "INSERT INTO PROTOTYPE_USER (accountName, password, phoneNum, sex, age, lastLoginTime, joinTime) VALUES (?, ?, ?, ?, ?, now(), now())",
                    userModel.getEmailAddress(),
                    userModel.getPassword(),
                    userModel.getPhoneNum(),
                    userModel.getSex(),
                    userModel.getAge()
            );

            if (affectedRowsNum == 0) {
                return new ResponseModel(202, "Join failed");
            } else {
                userModel.setCode(200);
                userModel.setMessage("success");
                return userModel;
            }
        }
    }

    /*
    RESPONSE CODE 200 로그인 성공
    RESPONSE CODE 201 계정 없음
    RESPONSE CODE 202 비밀번호 불일치
    RESPONSE CODE 203 로그인 시간 최신화 실패
    */
    public ResponseModel login(UserModel userModel) throws Exception {
        List<Map<String, Object>> resultList = db.select(
                "SELECT * FROM PROTOTYPE_USER WHERE accountName=?",
                userModel.getEmailAddress()
        );

        if (resultList.size() <= 0) {
            return new ResponseModel(201, "There is no requested account.");
        } else {
            Map<String, Object> userData = resultList.get(0);
            String password = (String) userData.get("password");

            if (!userModel.getPassword().equals(password)) {
                return new ResponseModel(202, "Wrong password.");
            } else {
                int affectedRowsNum = db.update(
                        "UPDATE PROTOTYPE_USER SET lastLoginTime=now() WHERE userNum=?",
                        (Long) userData.get("userNum")
                );

                if (affectedRowsNum == 0) {
                    return new ResponseModel(203, "Fail to update last login time.");
                }

                Long userNum = (Long) userData.get("userNum");
                String phoneNum = (String) userData.get("phoneNum");
                Integer sex = (Integer) userData.get("sex");
                Integer age = (Integer) userData.get("age");

                userModel.setCode(200);
                userModel.setMessage("success");
                userModel.setUserNum(userNum.intValue());
                userModel.setPhoneNum(phoneNum);
                userModel.setSex(sex);
                userModel.setAge(age);

                return userModel;
            }
        }
    }
}