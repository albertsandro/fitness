package com.fitforbusiness.Parse.Models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseRelation;

import java.util.Date;

/**
 * Created by Adeel on 6/7/2015.
 */
@ParseClassName("AssessmentForm")
public class AssessmentForm extends ParseObject {

    public AssessmentForm () {
    }

    public void setAssessmentFormField (AssessmentFormField assessmentFormField) {
        ParseRelation<AssessmentFormField> relation=this.getRelation("fields");
        relation.add(assessmentFormField);
        this.saveInBackground();
    }

    public boolean isNullAssessmentFormField(){
        ParseRelation<AssessmentFormField> relation=this.getRelation("fields");
        if (relation==null){
            return true;
        }else return false;
    }

    public void removeAssessmentFormField(AssessmentFormField assessmentFormField){
        ParseRelation<AssessmentFormField> relation=this.getRelation("fields");
        relation.remove(assessmentFormField);
    }
    public void setFormType (AssessmentFormType assessmentFormType) {
        put("assessmentFormType", assessmentFormType);
    }
    public AssessmentFormType getFormType () {
        return (AssessmentFormType)get("assessmentFormType");
    }

    public void setName (String name) {
        put("name", name);
    }
    public String getName () {
        return getString("name");
    }

    public Date getUpdatedDate(){
        return getDate("updatedAt");
    }

}
