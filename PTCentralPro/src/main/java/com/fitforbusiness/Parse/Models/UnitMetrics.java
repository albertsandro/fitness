package com.fitforbusiness.Parse.Models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by Adeel on 6/20/2015.
 */
@ParseClassName("UnitMetrics")
public class UnitMetrics extends ParseObject {

//    public final static int Distance = 1;
//    public final static int Weight = 2;
//    public final static int Time = 3;

    public UnitMetrics () {
    }

//    public void setQuantity (int q) {
//        put("quantity", q);
//    }
    public void setUnitAbbreviation (String abbrev) {
        put("unitAbbreviation", abbrev);
    }
    public void setUnitName (String unitName) {
        put("unitName", unitName);
    }
    public void setTrainer (Trainer t) {
        put("trainer", t);
    }
//    public int getQuantity () {
//        return getInt("quantity");
//    }
    public String getUnitAbbreviation () {
        return getString("unitAbbreviation");
    }
    public String getUnitName () {
        return getString("unitName");
    }
    public Trainer getTrainer () {
        return (Trainer) getParseUser("trainer");
    }

}
