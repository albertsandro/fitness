package com.fitforbusiness.Parse.Models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by Adeel on 6/7/2015.
 */
@ParseClassName("AssessmentFormType")
public class AssessmentFormType extends ParseObject {

    public AssessmentFormType () {
    }

    public void setFormTypeName (String formTypeName) {
        put("formTypeName", formTypeName);
    }
    public String getFormTypeName () {
        return getString("formTypeName");
    }
}
