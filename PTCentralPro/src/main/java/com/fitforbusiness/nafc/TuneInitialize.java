package com.fitforbusiness.nafc;

import android.content.Context;
import android.content.SharedPreferences;

import com.fitforbusiness.framework.Utils;
import com.mobileapptracker.MobileAppTracker;

/**
 * Created by Adeel on 3/5/2015.
 */
public class TuneInitialize {

    public static MobileAppTracker initialize(Context c){
        SharedPreferences settings = c.getSharedPreferences(Utils.TRAINER_PREFS, 0);

        // Initialize MAT
        MobileAppTracker.init(c, "166532", "88787cb6460c020ae9ffc77013c3e517");
        MobileAppTracker mobileAppTracker = MobileAppTracker.getInstance();

            String trainer_id = settings.getString("trainer_id", "-1");
            boolean download_data = settings.getBoolean("download_data", false);
            if (!trainer_id.equalsIgnoreCase("-1")) {
                mobileAppTracker.setExistingUser(true);
            }
            return mobileAppTracker;
    }
}
