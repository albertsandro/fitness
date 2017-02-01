package com.fitforbusiness.Parse.Models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.Date;

/**
 * Created by Adeel on 6/7/2015.
 */
@ParseClassName("AssessmentFormField")
public class AssessmentFormField extends ParseObject {

    public AssessmentFormField () {
    }

    public void setForm (AssessmentForm assessmentForm) {
        put("form", assessmentForm);
    }
    public AssessmentForm getAssessmentForm () {
        return (AssessmentForm) get("form");
    }

    public void setHeading(Boolean heading) {
        put("heading", heading);
    }
    public Boolean getHeading(){
        return getBoolean("heading");
    }

    public void setSortOrder(Number sortOrder) {
        put("sortOrder", sortOrder);
    }
    public Number getSortOrder() {
        return getNumber("sortOrder");
    }

    public String getTitle () {
        return getString("title");
    }
    public void setTitle (String title) {
        put("title",title);
    }

    public void setType (String type) {
        put("type", type);
    }
    public String getType () {
        return getString("type");
    }
}
