package com.fitforbusiness.Parse.Models;

import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseRelation;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Adeel on 6/7/2015.
 */
@ParseClassName("Group")
public class Group extends ParseObject {
    private String name;
    private Trainer trainer;
    public Group () {
        this.name=null;
        this.trainer=null;
    }

    public void setAssessmentForm (AssessmentForm assessmentForm) {
        put("assessmentForm", assessmentForm);
    }
    public AssessmentForm getAssessmentform () {
        return (AssessmentForm) get("assessmentForm");
    }

    public void setName (String name) {
        put ("name", name);
    }
    public String getName () {
        return getString("name");
    }

    public void setSession (Session session) {
        ParseRelation<Session> relation=this.getRelation("sessions");
        relation.add(session);
        this.saveInBackground();
    }
//    public Session getSession () {
//        return (Session)get("session");
//    }

    public void setTrainer (Trainer trainer) {
        put("trainer", trainer);
    }
    public Trainer getTrainer () {
        return (Trainer) getParseUser("trainer");
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

    public void setGroupImage(ParseFile parseFile){
        put("groupImage",parseFile);
    }
    public ParseFile getGroupImage(){
        return getParseFile("groupImage");
    }

    public void addClient(Client client) {
        ArrayList a = (ArrayList) get("clients");
        if (a == null) a = new ArrayList();
        a.add(client);
        put("clients", a);
    }
    public ArrayList<Client> getClients() {
        if (get("clients") != null)
            return (ArrayList) get("clients");
        return new ArrayList<>();
    }
    public void setClients(ArrayList<Client> clients) {
        put("clients", clients);
    }

}

