
package com.fitforbusiness.Parse.Models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by Adeel on 6/7/2015.
 */
@ParseClassName("SessionStatus")
public class SessionStatus extends ParseObject {

    public SessionStatus () {
    }

    public void setStatus (String  status) {
        put("status", status);
    }

    public String getStatus () {
        return getString("status");
    }
}

