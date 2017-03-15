package prototype_model;

public class ValueModel extends ResponseModel {
    private Object value;

    public ValueModel(Integer code, String message, Object value) {
        super(code, message);
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
