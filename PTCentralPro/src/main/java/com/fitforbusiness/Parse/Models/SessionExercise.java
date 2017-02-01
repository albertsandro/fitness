package com.fitforbusiness.Parse.Models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseRelation;

import java.util.Date;

/**
 * Created by Adeel on 6/7/2015.
 */
@ParseClassName("SessionExercise")
public class SessionExercise extends ParseObject {

    public SessionExercise() {
    }


    public void setSortOrder(Number sortOrder) {
        put("sortOrder", sortOrder);
    }
    public Number getSortOrder() {
        return getNumber("sortOrder");
    }

    public void setExercise (Exercise exercise) {
        put("exercise", exercise);
    }
    public Exercise getExercise () {
        return (Exercise)get("exercise");
    }

    public void setSessionWorkout (SessionWorkout sessionWorkout) {
        put("sessionWorkout",sessionWorkout );
    }
    public SessionWorkout getSessionWorkout () {
        return (SessionWorkout)get("sessionWorkout");
    }

    public void addSessionMeasurements (SessionMeasurements sessionMeasurements) {
        ParseRelation<SessionMeasurements> relation=getRelation("sessionMeasurements");
        relation.add(sessionMeasurements);
    }
    public SessionMeasurements getSessionMeasurements () {
        return (SessionMeasurements)get("sessionMeasurements");
    }
}

