package com.fitforbusiness.webservice;

import android.content.Context;
import android.os.AsyncTask;

import java.util.Map;

/**
 * Created by Sanjeet on 6/2/14.
 */
public class UpdateTrainerData extends AsyncTask<Void, Void, Void> {

    Context context;
    Map<String, Object> params;
    String trainerId;
    String trainerDetailURL = "http://ptcentralwcf.itpluspoint.com/TrainingAppService.svc/";


    public UpdateTrainerData(Context context, Map<String, Object> params, String trainerId) {

        this.context = context;
        this.params = params;
        this.trainerId = trainerId;

    }

    @Override
    protected Void doInBackground(Void... voids) {

        TrainerWebService.updateTrainerProfile(context, params, "UpdateAllTrainerDetailsResult");
        TrainerWebService.updateTrainerImage(context, trainerId, "UpdateTrainerImageResult");

        return null;
    }
}
