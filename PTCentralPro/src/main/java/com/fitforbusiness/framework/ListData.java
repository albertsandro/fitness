package com.fitforbusiness.framework;

public class ListData {
    String _id = "";
    Object object = null;
    String label_1 = "";
    String label_2 = "";
    String label_3 = "";
    String label_4 = "";
    String label_5 = "";
    boolean selected = false;


    public ListData(String _id, Object object, String label_1, String label_2, String label_3,
                    String label_4, String label_5, boolean selected) {
        super();
        this._id = _id;
        this.object = object;
        this.label_1 = label_1;
        this.label_2 = label_2;
        this.label_3 = label_3;
        this.label_4 = label_4;
        this.label_5 = label_5;
        this.selected = selected;
    }

    public ListData(String _id, Object object, String label_1) {
        super();
        this._id = _id;
        this.object = object;
        this.label_1 = label_1;
    }


    public String getLabel1() {
        return label_1;
    }

    public void setLabel1(String label_1) {
        this.label_1 = label_1;
    }

    public String getLabel2() {
        return label_2;
    }

    public void setLabel2(String label_2) {
        this.label_2 = label_2;
    }

    public String getId() {
        return _id;
    }

    public void setId(String _id) {
        this._id = _id;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setLabel4(String label_4) {
        this.label_4 = label_4;
    }

    public void setLabel5(String label_5) {
        this.label_5 = label_5;
    }

    public void setLabel3(String label_3) {
        this.label_3 = label_3;
    }

    public String getLabel3() {
        return label_3;
    }

    public String getLabel4() {
        return label_4;
    }

    public String getLabel5() {
        return label_5;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}