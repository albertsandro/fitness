package com.fitforbusiness.nafc;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.webservice.Synchronise;

/**
 * Created by Sanjeet on 6/3/14.
 */
public class DataDownloadAsyncTask extends AsyncTask<Void, Void, Void> {
    Context context;
    int trainer_id;

    public DataDownloadAsyncTask(Context context, String trainer_id) {
        this.context = context;
        this.trainer_id = Integer.valueOf(Utils.getTrainerId(context));
    }

    @Override
    protected Void doInBackground(Void... voids) {
        //  new Synchronise(context, Utils.getLastSyncTime(context)).sync();
        /*
        new Client(context, lastSyncTime).sync();
        new Exercise(context, lastSyncTime).sync();
        new Qualification(context, lastSyncTime).sync();
        new Group(context, lastSyncTime).sync();*/

//        SystemClock.sleep(2000);
        if (trainer_id > 0) {
            new Synchronise.Trainer(context, Utils.getLastSyncTime(context)).sync();
        }
        if (trainer_id > 0) {
            new Synchronise.Exercise(context, Utils.getLastSyncTime(context)).sync();
        }
        if (trainer_id > 0) {
            new Synchronise.Qualification(context, Utils.getLastSyncTime(context)).sync();
        }
        if (trainer_id > 0) {
            new Synchronise.Client(context, Utils.getLastSyncTime(context)).sync();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
       /* Utils.saveImage(Utils.LOCAL_RESOURCE_PATH + "trainer" + ".JPG", "trainer" + ".JPG");*/

        //
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                if (trainer_id > 0) {
                    new Synchronise.Workout(context, Utils.getLastSyncTime(context)).sync();
                }
                if (trainer_id > 0) {
                    new Synchronise.Assessment(context, Utils.getLastSyncTime(context)).sync();
                }

                if (trainer_id > 0) {
                    new Synchronise.Group(context, Utils.getLastSyncTime(context)).sync();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);


                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        if (trainer_id > 0) {
                            new Synchronise.Session(context, Utils.getLastSyncTime(context)).sync();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        Utils.setLastExerciseSyncTime(context);
                        Utils.setLastWorkoutSyncTime(context);
                        Utils.setLastQualificationSyncTime(context);
                        Utils.setLastClientSyncTime(context);
                        Utils.setLastGroupSyncTime(context);
                        Utils.setLastAssessmentSyncTime(context);
                        Utils.setLastSessionSyncTime(context);
                        Utils.setLastSyncTime(context);
                        SharedPreferences settings = context.getSharedPreferences(Utils.TRAINER_PREFS, 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putBoolean("download_data", false);
                        editor.commit();
                    }
                }.execute();
            }
        }.execute();
    }

}
