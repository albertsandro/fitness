package com.fitforbusiness.Parse.Models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import org.json.JSONArray;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Adeel on 6/7/2015.
 */
@ParseClassName("Status")
public class Status extends ParseObject {

    public Status () {

    }

    public void setName (String name) {
        put("name", name);
    }

    public String getName() {
        return getString("name");
    }


}
