package com.fitforbusiness.Parse.Models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.Date;

/**
 * Created by Adeel on 6/7/2015.
 */
@ParseClassName("SessionMeasurement")
public class SessionMeasurements extends ParseObject {

    public SessionMeasurements () {
    }
    public void setMeasuredDate (Date measuredDate) {
        put("measuredDate", measuredDate);
    }
    public Date getMeasuredDate () {
        return getDate("measuredDate");
    }

    public void setSetNumber (Number setNumber) {
        put("setNumber", setNumber);
    }
    public Number getSetNumber () {
        return getNumber("setNumber");
    }

    public void setMeasuredValue (String measuredValue) {
        put("measuredValue", measuredValue);
    }

    public String getMeasuredValue () {
        return getString("measureValue");
    }

    public void setTargetValue (String targetValue) {
        put("targetValue", targetValue);
    }

    public String getTargetValue () {
        return getString("targetValue");
    }

    public void setPackageID (String packageID) {
        put("venue", getString("venue"));
    }

    public String getPackageID () {
        return getString("packageID");
    }



    public void setSessionExercise (SessionExercise sessionExercise) {
        put("sessionExercise", sessionExercise);
    }
    public SessionExercise getSessionExercise () {
        return (SessionExercise)get("sessionExercise");
    }

    public void setMeasurements (Measurements measurements) {
        put("measurement", measurements);
    }
    public Measurements getMeasurements () {
        return (Measurements)get("measurement");
    }

}

