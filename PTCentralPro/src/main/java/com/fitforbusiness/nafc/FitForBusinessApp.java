package com.fitforbusiness.nafc;

import android.app.Application;
import android.util.Log;

import com.fitforbusiness.Parse.Models.Accreditation;
import com.fitforbusiness.Parse.Models.AssessmentField;
import com.fitforbusiness.Parse.Models.AssessmentForm;
import com.fitforbusiness.Parse.Models.AssessmentFormField;
import com.fitforbusiness.Parse.Models.AssessmentFormType;
import com.fitforbusiness.Parse.Models.Client;
import com.fitforbusiness.Parse.Models.CompletedAssessmentForm;
import com.fitforbusiness.Parse.Models.CompletedAssessmentFormField;
import com.fitforbusiness.Parse.Models.Exercise;
import com.fitforbusiness.Parse.Models.Group;
import com.fitforbusiness.Parse.Models.Measurements;
import com.fitforbusiness.Parse.Models.Membership;
import com.fitforbusiness.Parse.Models.Session;
import com.fitforbusiness.Parse.Models.SessionExercise;
import com.fitforbusiness.Parse.Models.SessionMeasurements;
import com.fitforbusiness.Parse.Models.SessionStatus;
import com.fitforbusiness.Parse.Models.SessionWorkout;
import com.fitforbusiness.Parse.Models.Status;
import com.fitforbusiness.Parse.Models.Trainer;
import com.fitforbusiness.Parse.Models.TrainerBusinessData;
import com.fitforbusiness.Parse.Models.UnitMetrics;
import com.fitforbusiness.Parse.Models.Workout;
import com.fitforbusiness.database.DatabaseHelper;
import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.nafc.client.ClientFragment;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Sanjeet on 6/2/14.
 */
public class FitForBusinessApp extends Application {

    public ArrayList<HashMap<String, Object>> mapArrayList = new ArrayList<>();
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            DatabaseHelper.initialize(getApplicationContext());

        } catch (Exception e) {
            Log.d("Exception found is ", e.toString());
        }
        Log.d("was this method called", "");

        try {
            Utils.createLocalResourceDirectory(getApplicationContext());
            Utils.createThumbNailDirectory(getApplicationContext());
            Utils.createProfileImageDirectory(getApplicationContext());
        } catch (Exception ex) {

        }

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        ParseUser.registerSubclass(Trainer.class);
        ParseObject.registerSubclass(Status.class);
        ParseObject.registerSubclass(TrainerBusinessData.class);
        ParseObject.registerSubclass(Exercise.class);
        ParseObject.registerSubclass(Measurements.class);
        ParseObject.registerSubclass(UnitMetrics.class);
        ParseObject.registerSubclass(Client.class);
        ParseObject.registerSubclass(Group.class);
        ParseObject.registerSubclass(AssessmentForm.class);
        ParseObject.registerSubclass(AssessmentFormField.class);
        ParseObject.registerSubclass(AssessmentFormType.class);
        ParseObject.registerSubclass(AssessmentField.class);
        ParseObject.registerSubclass(Session.class);
        ParseObject.registerSubclass(SessionExercise.class);
        ParseObject.registerSubclass(SessionMeasurements.class);
        ParseObject.registerSubclass(SessionWorkout.class);
        ParseObject.registerSubclass(SessionStatus.class);
        ParseObject.registerSubclass(Workout.class);
        ParseObject.registerSubclass(Accreditation.class);
        ParseObject.registerSubclass(Membership.class);
        ParseObject.registerSubclass(CompletedAssessmentForm.class);
        ParseObject.registerSubclass(CompletedAssessmentFormField.class);

        Parse.initialize(this, "rIz0jNXoPfakMCHtBfEeEiW0CM0B8hLUN9pIh3Mg", "PWx92TNEGp9F37LXX0iPcWTGcO0As4E8iXTCyvg7");

    }
}
