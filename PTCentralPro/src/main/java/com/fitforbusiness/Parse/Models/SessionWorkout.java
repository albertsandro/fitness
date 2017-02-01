package com.fitforbusiness.Parse.Models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by Adeel on 6/7/2015.
 */
@ParseClassName("SessionWorkout")
public class SessionWorkout extends ParseObject {

    public SessionWorkout () {
    }

    public void setSession (Session session) {
        put("session", session);
    }
    public Session getSession () {
        return (Session)get("session");
    }

    public void setSessionExercise (SessionExercise sessionExercise) {
        put("sessionExercise", sessionExercise);
    }
    public SessionExercise getSessionExercise () {
        return (SessionExercise)get("sessionExcercise");
    }

    public void setWorkout (Workout workout) {
        put("workout", workout);
    }
    public Workout getWorkout () {
        return (Workout)get("workout");
    }

}

