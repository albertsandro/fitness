package com.fitforbusiness.Parse.Models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseRelation;

import java.util.Date;

/**
 * Created by Sandro on 9/3/2015.
 */
@ParseClassName("CompletedAssessmentForm")
public class CompletedAssessmentForm extends ParseObject {
    public CompletedAssessmentForm () {
    }
    public void setAssessmentFormField (CompletedAssessmentFormField completedAssessmentFormField) {
        ParseRelation<CompletedAssessmentFormField> relation=this.getRelation("fields");
        relation.add(completedAssessmentFormField);
        this.saveInBackground();
    }

    public void removeAssessmentFormField(AssessmentFormField assessmentFormField){
        ParseRelation<AssessmentFormField> relation=this.getRelation("fields");
        relation.remove(assessmentFormField);
    }
    public void setFormType (AssessmentFormType assessmentFormType) {
        put("formType", assessmentFormType);
    }
    public AssessmentFormType getFormType () {
        return (AssessmentFormType)get("formType");
    }

    public void setName (String name) {
        put("name", name);
    }
    public String getName () {
        return getString("name");
    }

    public void setDate(Date date){
        put("date",date);
    }
    public Date getDate(){
        return getDate("date");
    }

    public void setTime(Number time){
        put("time",time);
    }
    public Number getTime(){
        return (Number)get("time");
    }


    public void setClient (Client client) {
        put("client", client);
    }
    public Client getClient() {
        return (Client)get("client");
    }

    public void setGroup (Group group) {
        put("group", group);
    }
    public Group getGroup() {
        return (Group)get("group");
    }

}
