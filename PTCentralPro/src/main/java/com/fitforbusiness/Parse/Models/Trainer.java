package com.fitforbusiness.Parse.Models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;

/**
 * Created by Adeel on 6/4/2015.
 */
@ParseClassName("_User")
public class Trainer extends ParseUser {

    public Trainer () {

    }

    public void setFirstName (String name) {
        put ("firstName", name);
    }
    public void setEmail (String email) {
        put ("email", email);
    }

    public void setLastName (String name) {
        put ("lastName", name);
    }

    public void setGender (boolean gender) {
        put ("gender", gender);
    }
    public void setPhone (String phone) {
        put ("phone", phone);
    }
    public void setDateOfBirth (Date dob) {
        put ("dateOfBirth", dob);
    }
    public void setEmergencyContact (String emergencyContact) {
        put ("emergencyContact", emergencyContact);
    }
    public void setEmergencyContactPhone (String emergencyContactPhone) {
        put ("emergencyContactPhone", emergencyContactPhone);
    }
    public void setTrainerBusinessData (TrainerBusinessData trainerBusinessData) {
        put ("trainerBusinessData", trainerBusinessData);
    }
    public void setParseUser  (boolean isParseUser) {
        put ("ParseUser", isParseUser);
    }

    public String getFirstName () {
        return getString("firstName");
    }

    public String getEmail () {
        return getString("email");
    }
    public String getLastName () {
        return getString("lastName");
    }
    public boolean getGender () {
        return getBoolean("gender");
    }
    public String getPhone () {
        return getString("phone");
    }
    public Date getDateOfBirth () {
        return getDate("dateOfBirth");
    }
    public String getEmergencyContact () {
        return getString("emergencyContact");
    }
    public String getEmergencyContactPhone () {
        return getString("emergencyContactPhone");
    }
    public boolean isParseUser () {
        return getBoolean("ParseUser");
    }
    public TrainerBusinessData getTrainerBusinessData () {
        return (TrainerBusinessData) getParseObject("trainerBusinessData");
    }

    public static Trainer getCurrent (){
        return (Trainer) Trainer.getCurrentUser();
    }
    public void setImageName (String imageName) {
        put("imageName", imageName);
    }
    public String getImageName () {
        return (String)get("imageName");
    }

    public void setImageFile (String  imageFile) {
        put("imageFile", imageFile);
    }
    public String getImageFile () {
        return (String)get("imageFile");
    }

    public void setTrainerImage(ParseFile parseFile){
        put("groupImage",parseFile);
    }
    public ParseFile getTrainerImage(){
        return getParseFile("groupImage");
    }
}
