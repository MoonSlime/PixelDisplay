package prototype_model;

import java.util.Date;

public class EyeModel extends ResponseModel {
    private Integer id;
    private Integer userNum;
    private Float leftDi;
    private Float rightDi;
    private Date infoTime;

    public EyeModel(Integer code, String message, Integer id, Integer userNum, Float leftDi, Float rightDi, Date infoTime) {
        super(code, message);
        this.id = id;
        this.userNum = userNum;
        this.leftDi = leftDi;
        this.rightDi = rightDi;
        this.infoTime = infoTime;
    }

    public Integer getId() {
        return id;
    }
    public Integer getUserNum() {
        return userNum;
    }
    public Float getLeftDi() {
        return leftDi;
    }
    public Float getRightDi() {
        return rightDi;
    }
    public Date getInfoTime() {
        return infoTime;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    public void setUserNum(Integer userNum) {
        this.userNum = userNum;
    }
    public void setLeftDi(Float leftDi) {
        this.leftDi = leftDi;
    }
    public void setRightDi(Float rightDi) {
        this.rightDi = rightDi;
    }
    public void setInfoTime(Date infoTime) {
        this.infoTime = infoTime;
    }
}
