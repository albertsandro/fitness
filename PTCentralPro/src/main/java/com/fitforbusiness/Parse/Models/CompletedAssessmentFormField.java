package com.fitforbusiness.Parse.Models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by Sandro on 9/3/2015.
 */
@ParseClassName("CompletedAssessmentFormField")
public class CompletedAssessmentFormField extends ParseObject {
    public void completedAssessmentFormField(){
    }
    public void setForm (CompletedAssessmentForm completedAssessmentForm) {
        put("form", completedAssessmentForm);
    }
    public CompletedAssessmentForm getCompletedAssessmentForm () {
        return (CompletedAssessmentForm) get("form");
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

    public void setAnswer (String answer) {
        put("answer", answer);
    }
    public String getAnswer () {
        return getString("answer");
    }
}
