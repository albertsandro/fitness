package com.fitforbusiness.Parse.Models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by Adeel on 6/7/2015.
 */
@ParseClassName("AssessmentField")
public class AssessmentField extends ParseObject {

    public AssessmentField () {
    }

    public void setTitle (String title) {
        put("title", title);
    }
    public String getTitle () {
        return getString("title");
    }

    public void setType (String type) {
        put("title", type);
    }
    public String getType () {
        return getString("type");
    }
}
