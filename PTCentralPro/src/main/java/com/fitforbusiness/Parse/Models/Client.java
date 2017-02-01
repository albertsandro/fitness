package com.fitforbusiness.Parse.Models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseRelation;

import java.io.File;
import java.util.Date;

/**
 * Created by Adeel on 6/7/2015.
 */
@ParseClassName("Client")
public class Client extends ParseObject {

    public Client () {
    }

    public void setAllergyInformation (String allergyInformation) {
        put("allergyInformation", allergyInformation);
    }
    public String getAllergyInformation () {
        return getString("allergyInformation");
    }

    public void setContactPhone (String contactPhone) {
        put("contactPhone", contactPhone);
    }
    public String getContactPhone () {
        return getString("contactPhone");
    }

    public void setDateOfBirth (Date dateOfBirth) {
        put("dateOfBirth", dateOfBirth);
    }
    public Date getDateOfBirth () {
        return getDate("dateOfBirth");
    }

    public void setEmail (String email) {
        put("email", email);
    }
    public String getEmail () {
        return getString("email");
    }

    public void setEmergencyContact (String emergencyContact) {
        put("emergencyContact", emergencyContact);
    }
    public String getEmergencyContact () {
        return getString("emergencyContact");
    }

    public void setEmergencyContactNumber (String emergencyContactNumber) {
        put("emergencyContactNumber", emergencyContactNumber);
    }
    public String getEmergencyContactNumber () {
        return getString("emergencyContactNumber");
    }

    public void setFirstName (String firstName) {
        put ("firstName", firstName);
    }
    public String getFirstName () {
        return getString("firstName");
    }

    public void setLastName (String lastName) {
        put ("lastName", lastName);
    }
    public String getLastName () {
        return getString("lastName");
    }

    public void setGender (boolean gender) {
        put ("gender", gender);
    }
    public boolean getGender () {
        return getBoolean("gender");
    }

    public void setMedicalInformation (String medicalInformation) {
        put ("medicalInformation", medicalInformation);
    }
    public String getMedicalInformation () {
        return getString("medicalInformation");
    }

    public void setSession (Session session) {
        ParseRelation<Session> relation=this.getRelation("sessions");
        relation.add(session);
        this.saveInBackground();
    }

    public void setTrainer (Trainer trainer) {
        put("trainer", trainer);
    }
    public Trainer getTrainer () {
        return (Trainer) getParseUser("trainer");
    }

    public void setAssessmentForms (AssessmentForm assessmentForms) {
        put("assessmentForms", assessmentForms);
    }
    public AssessmentForm getAssessmentforms () {
        return (AssessmentForm) get("assessmentForms");
    }

    public void setImageName (String imageName) {
        put("imageName", imageName);
    }
    public String getImageName () {
        return getString("imageName");
    }

    public void setImageFile (String imageFile) {
        put("imageFile", imageFile);
    }
    public String getImageFile () {
        return (String)get("imageFile");
    }

    public void setClientImage(ParseFile parseFile){
        put("clientImage",parseFile);
    }
    public ParseFile getClientImage(){
        return getParseFile("clientImage");
    }
}

