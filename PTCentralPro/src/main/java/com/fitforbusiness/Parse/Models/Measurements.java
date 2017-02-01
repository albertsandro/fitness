package com.fitforbusiness.Parse.Models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by Adeel on 6/7/2015.
 */
@ParseClassName("Measurements")
public class Measurements extends ParseObject {

    public Measurements () {
    }

    public void setName (String name) {
        put("name", name);
    }
//    public void setUnitMetrics (UnitMetrics unitMetrics) {
//        put("unitMetrics", unitMetrics);
//    }

    public String getName () {
        return getString("name");
    }
//    public UnitMetrics getUnitMetrics () {
//        return (UnitMetrics) getParseObject("unitMetrics");
//    }

    public void setTrainer (Trainer t) {
        put("trainer", t);
    }
    public Trainer getTrainer () {
        return (Trainer) getParseUser("trainer");
    }
}
