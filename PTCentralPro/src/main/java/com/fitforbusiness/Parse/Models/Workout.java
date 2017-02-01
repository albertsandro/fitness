package com.fitforbusiness.Parse.Models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

import org.json.JSONArray;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Adeel on 6/7/2015.
 */
@ParseClassName("Workout")
public class Workout extends ParseObject {

    public Workout () {

    }
    public void setExercises (ArrayList<Exercise> exercises) {
        put("exercises", exercises);
    }
    public void addExercises (Exercise exercise) {
        ArrayList a = (ArrayList) get("exercises");
        if (a == null) a = new ArrayList();
        a.add(exercise);
        put("exercises", a);
    }
    public void setExcercise (Array exercise) {
        put("exercise", exercise);
    }
    public void setImageUrl (String imageUrl) {
        put("imageUrl", imageUrl);
    }
    public void setName (String name) {
        put("name", name);
    }
    public void setWorkoutDescription (String workoutDescription) {
        put("workoutDescription", workoutDescription);
    }
    public void setTrainer (Trainer trainer) {
        put("trainer", trainer);
    }

    public ArrayList<Exercise> getExercises () {
        if (get("exercises") != null)
            return (ArrayList) get("exercises");
        return new ArrayList<>();
    }
    public String getImageUrl() {
        return getString("imageUrl");
    }
    public String getName() {
        return getString("name");
    }
    public String getWorkoutDescription () {
        return  getString("workoutDescription");
    }
    public Trainer getTrainer () {
        return (Trainer) getParseUser("trainer");
    }
    public void setImageName (String imageName) {
        put("imageName", imageName);
    }
    public String getImageName () {
        return getString("imageUrl");
    }

    public void setImageFile (String imageFile) {
        put("imageFile", imageFile);
    }
    public String getImageFile () {
        return (String)get("imageFile");
    }

    public void setWorkoutImage(ParseFile parseFile){
        put("workoutImage",parseFile);
    }
    public ParseFile getWorkoutImage(){
        return getParseFile("workoutImage");
    }
}
