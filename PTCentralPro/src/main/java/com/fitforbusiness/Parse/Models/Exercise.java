package com.fitforbusiness.Parse.Models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

import java.util.ArrayList;

/**
 * Created by Adeel on 6/7/2015.
 */
@ParseClassName("Exercise")
public class Exercise extends ParseObject {

    public Exercise () {

    }

    public void setName (String name) {
        put("name", name);
    }
    public void setMuscleGroup (String muscleGroup) {
        put("muscleGroup", muscleGroup);
    }
    public void setTags (String tags) {
        put("tags", tags);
    }
    public void setTrainer (Trainer trainer) {
        put("trainer", trainer);
    }

    public void setMeasurements (ArrayList<Measurements> measurements) {
        put("measurements", measurements);
    }

    public void addMeasurements (Measurements measurements) {
        ArrayList a = (ArrayList) get("measurements");
        if (a == null) a = new ArrayList();
        a.add(measurements);
        put("measurements", a);
    }

    public String getName() {
        return getString("name");
    }
    public String getTags() {
        return getString("tags");
    }
    public String getMuscleGroup() {
        return getString("muscleGroup");
    }
    public Trainer getTrainer () {
        return (Trainer) getParseUser("trainer");
    }
    public ArrayList<Measurements> getMeasurements () {
        if (get("measurements") != null)
            return (ArrayList) get("measurements");
        return new ArrayList<>();
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

    public void setExerciseImage(ParseFile parseFile){
        put("exerciseImage",parseFile);
    }
    public ParseFile getExerciseImage(){
        return getParseFile("exerciseImage");
    }

}
