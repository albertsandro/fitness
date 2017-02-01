package com.fitforbusiness.webservice;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.fitforbusiness.database.DBOHelper;
import com.fitforbusiness.database.DatabaseHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.framework.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Sanjeet on 16-Sep-14.
 */
public class ProgramManagementWebService {

    private Context context;
    private String lastSyncTime;
    private ContentValues exerciseIds;
    private JSONArray workoutsArray;
    private JSONArray exerciseArray;
    private static String programManagementWebServiceURL = Utils.BASE_URL
            + Utils.PROGRAM_MANAGEMENT_SERVICE;

    public ProgramManagementWebService(Context context, String lastSyncTime) {
        this.context = context;
        this.lastSyncTime = lastSyncTime;
        exerciseIds = new ContentValues();
    }

    public void addExercisesToServer() {
        String LAST_SYNC = "datetime(\'" + lastSyncTime + "\')";
        String query = "select * from " + Table.Exercise.TABLE_NAME + " where deleted= 0 and "
                + Table.CREATED + " >= " + LAST_SYNC;
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            Cursor cursor = sqlDB.rawQuery(query, null);
            while (cursor.moveToNext()) {
                Map<String, Object> exerciseFields = new HashMap<String, Object>();
                exerciseFields.put("Title", cursor.getString(cursor.getColumnIndex(Table.Exercise.NAME)));
                exerciseFields.put("Description", cursor.getString(cursor.getColumnIndex(Table.Exercise.DESCRIPTION)));
                exerciseFields.put("Active", "1");
                exerciseFields.put("Description", cursor.getString(cursor.getColumnIndex(Table.Exercise.DESCRIPTION)));
                exerciseFields.put("Muscle_Group", cursor.getString(cursor.getColumnIndex(Table.Exercise.DESCRIPTION)));
                exerciseFields.put("Tags", cursor.getString(cursor.getColumnIndex(Table.Exercise.TAG)));
                exerciseFields.put("Sortorder", "1");
                exerciseFields.put("Trainer_Id", Utils.getTrainerId(context));
                exerciseFields.put("syncID", cursor.getString(cursor.getColumnIndex(Table.SYNC_ID)));
                exerciseFields.put("Imageurl", Utils.encodeBase64(Utils.THUMBNAIL_PATH +
                        cursor.getString(cursor.getColumnIndex(Table.Exercise.PHOTO_URL))));
                HashMap<String, Object> exercise = new HashMap<String, Object>();
                String _id = cursor.getString(cursor.getColumnIndex(Table.Exercise.ID));
                exercise.put("Exercise", exerciseFields);
                //addExerciseSync(exercise, _id);
                WebService w = new WebService();
                String response = w.webInvoke(programManagementWebServiceURL, "AddExercise",
                        exercise);
                JSONObject json = null;
                try {
                    if (response != null) {
                        json = new JSONObject(response);
                    }
                    if (json != null) {
                        json = json.getJSONObject("AddExerciseResult");
                        int exercise_id = json.getInt("Result");
                        ContentValues mContentValues = new ContentValues();
                        mContentValues.put(Table.Exercise.EXERCISE_ID, exercise_id);
                        DBOHelper.update(Table.Exercise.TABLE_NAME, mContentValues, _id);
                        exerciseIds.put(_id, exercise_id);
                        addMeasurementsToServer(exercise_id, _id);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (sqlDB != null) {
                sqlDB.close();
            }
        }

    }

    private void addMeasurementsToServer(int exercise_id, String _id) {
        String query = "select * from " + Table.ExerciseMeasurements.TABLE_NAME
                + " where " + Table.ExerciseMeasurements.EXERCISE_ID + " = " + _id;
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            Cursor cursor = sqlDB.rawQuery(query, null);
            while (cursor.moveToNext()) {
                Map<String, Object> measurementsFields = new HashMap<String, Object>();
                measurementsFields.put("Measurement_Id", cursor.getString(
                        cursor.getColumnIndex(Table.ExerciseMeasurements.MEASUREMENT_ID)));
                measurementsFields.put("Exercise_Id", exercise_id);
                HashMap<String, Object> measurement = new HashMap<String, Object>();
                measurement.put("Measurement", measurementsFields);
                //addMeasurementSync(measurement);
                WebService w = new WebService();
                String response = w.webInvoke(programManagementWebServiceURL, "AddMeasurementToExercise",
                        measurement);
                JSONObject json = null;
                try {
                    json = new JSONObject(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (json != null) {
                        json = json.getJSONObject("AddMeasurementToExerciseResult");
                        int measurement_id = json.getInt("Result");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("value of json is", response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (sqlDB != null) {
                sqlDB.close();
            }
        }
    }

    public void getExercisesFromServer() {
        Map<String, String> param = new HashMap<String, String>();
        param.put("Trainer_Id", Utils.getTrainerId(context));
        WebService w = new WebService();
        String response = w.webGet(programManagementWebServiceURL,
                "GetExerciseByTrainerId", param);
        JSONObject obj = null;
        try {
            if (response != null) {
                obj = new JSONObject(response);
                Log.d("exercises", obj.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (obj != null) {
            try {
                exerciseArray = obj.getJSONArray("GetExerciseByTrainerIdResult");

                for (int i = 0; i < exerciseArray.length(); i++) {
                    JSONObject exercise = exerciseArray.getJSONObject(i);
                    String createdDate = exercise.getString("CreatedDate");
                    String createdDateUTC = createdDate.replace("Z", "");
                    Boolean isActive = exercise.getBoolean("Active");
                    Log.d("isActive", "" + Utils.isDateAfterLastSyncTime(lastSyncTime, createdDateUTC));
                    Log.d("createdDateUTC", createdDateUTC + "");
                    Log.d("Utils.getLastSyncTime(context)", lastSyncTime);
                    if (isActive && Utils.isDateAfterLastSyncTime(lastSyncTime, createdDateUTC)) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(Table.Exercise.NAME, exercise.getString("Title"));
                        contentValues.put(Table.Exercise.MUSCLE_GROUP, exercise.getString("Muscle_Group"));
                        contentValues.put(Table.Exercise.DESCRIPTION, exercise.getString("Description"));
                        contentValues.put(Table.Exercise.TAG, exercise.getString("Tags"));
                        String imageName = new Date().getTime() + ".jpg";
                        contentValues.put(Table.Exercise.EXERCISE_ID, exercise.getString("Exercise_Id"));
                        contentValues.put(Table.Exercise.PHOTO_URL, imageName);
                        try {
                            if (exercise.getString("Imageurl") != null && !(exercise.getString("Imageurl")
                                    .equalsIgnoreCase(""))
                                    && Utils.createLocalResourceDirectory(context)) {
                                Utils.decodeFromBase64(exercise.getString("Imageurl"),
                                        imageName);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        contentValues.put(Table.SYNC_ID, exercise.getString("syncID"));
                        long rowId = DBOHelper.insert(context, Table.Exercise.TABLE_NAME, contentValues);
                        Log.d("added Exercise is", rowId + "");
                        if (rowId > 0) {
                            JSONArray measurementArray = exercise.getJSONArray("Measurement");
                            for (int j = 0; j < measurementArray.length(); j++) {
                                JSONObject measurementJSONObject = measurementArray.getJSONObject(j);
                                ContentValues mContentValue = new ContentValues();
                                mContentValue.put(Table.ExerciseMeasurements.EXERCISE_ID, rowId);
                                mContentValue.put(Table.ExerciseMeasurements.MEASUREMENT_ID,
                                        measurementJSONObject.getString("Measurement_Id"));
                                long mRowId = DBOHelper.insert(context, Table.ExerciseMeasurements.TABLE_NAME, mContentValue);
                                Log.d("added Measurement is", mRowId + "");
                            }
                        }

                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void addWorkoutsToServer() {
        getWorkoutsFromServer();
        String LAST_SYNC = "datetime(\'" + lastSyncTime + "\')";
        String query = "select * from " + Table.Workout.TABLE_NAME + " where deleted= 0 and "
                + Table.CREATED + " >= " + LAST_SYNC;
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            Cursor cursor = sqlDB.rawQuery(query, null);
            while (cursor.moveToNext()) {
                Map<String, Object> workoutsFields = new HashMap<String, Object>();

                workoutsFields.put("Title", cursor.getString(cursor.getColumnIndex(Table.Workout.NAME)));
                workoutsFields.put("Description", cursor.getString(cursor.getColumnIndex(Table.Workout.DESCRIPTION)));
                workoutsFields.put("Active", "1");
                workoutsFields.put("syncID", cursor.getString(cursor.getColumnIndex(Table.SYNC_ID)));
                try {
                    workoutsFields.put("Image_Path", Utils.encodeBase64(
                            Utils.THUMBNAIL_PATH + cursor.getString(cursor.getColumnIndex(Table.Workout.PHOTO_URL))));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                HashMap<String, Object> workout = new HashMap<String, Object>();
                String workout_id = cursor.getString(cursor.getColumnIndex(Table.Workout.ID));
                workout.put("workout", workoutsFields);
                workout.put("trainerId", Utils.getTrainerId(context));
                //addExerciseSync(exercise, _id);
                WebService w = new WebService();
                String response = w.webInvoke(programManagementWebServiceURL, "CreateWorkout",
                        workout);
                JSONObject json = null;
                try {
                    if (response != null) {
                        json = new JSONObject(response);
                        Log.d("CreateWorkout", json.toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (json != null) {
                        json = json.getJSONObject("CreateWorkoutResult");
                        int result = json.getInt("Result");
                        if (result > 0) {
                            ContentValues mContentValues = new ContentValues();
                            mContentValues.put(Table.Workout.WORKOUT_ID, result);
                            DBOHelper.update(Table.Workout.TABLE_NAME, mContentValues, workout_id);

                            String ExerciseQuery = "select exercise_id from exercise where " +
                                    "_id in (select exercise_id from workout_exercises where" +
                                    " workout_id=(select _id from workout where workout_id=" + result + "))";
                            SQLiteDatabase sqLiteDatabase = null;
                            Log.d("ExerciseQuery", ExerciseQuery);
                            try {
                                sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
                                Cursor mCursor = sqLiteDatabase.rawQuery(ExerciseQuery, null);
                                while (mCursor.moveToNext()) {
                                    Map<String, Object> exercise = new HashMap<String, Object>();
                                    exercise.put("workoutId", result);
                                    exercise.put("exerciseid", mCursor.getString(mCursor.getColumnIndex(Table.Exercise.EXERCISE_ID)));
                                    WebService mWebservice = new WebService();
                                    String mResponse = mWebservice.webInvoke(programManagementWebServiceURL, "AddExerciseToWorkout",
                                            exercise);
                                    JSONObject mJson = null;
                                    try {
                                        if (mResponse != null) {
                                            mJson = new JSONObject(mResponse);
                                            Log.d("CreateWorkoutExercise", mJson.toString());
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    try {
                                        if (mJson != null) {
                                                   /* mJson = mJson.getJSONObject("AddExerciseToWorkoutResult");
                                                    int mResult = mJson.getInt("Result");*/
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                if (sqLiteDatabase != null) {
                                    sqlDB.close();
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (sqlDB != null) {
                sqlDB.close();
            }
        }
    }

    public void getWorkoutsFromServer() {
        Map<String, String> param = new HashMap<String, String>();
        param.put("trainerId", Utils.getTrainerId(context));
        WebService w = new WebService();
        String response = w.webGet(programManagementWebServiceURL,
                "GetAllWorkouts", param);
        JSONObject obj = null;
        try {
            if (response != null) {
                obj = new JSONObject(response);
                Log.d("GetAllWorkouts response", obj.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (obj != null) {
            try {
                workoutsArray = obj.getJSONArray("GetAllWorkoutsResult");
                for (int i = 0; i < workoutsArray.length(); i++) {
                    JSONObject workout = workoutsArray.getJSONObject(i);
                    String createdDate = workout.getString("CreatedDate");
                    String createdDateUTC = createdDate.replace("Z", "");
                    Boolean isActive = workout.getBoolean("Active");
                    Log.d("createdDateUTC :workouts", createdDateUTC + "");
                    Log.d("lastSyncTime in :workouts", lastSyncTime);
                    if (isActive && Utils.isDateAfterLastSyncTime(lastSyncTime, createdDateUTC)) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(Table.Workout.NAME, workout.getString("Title"));
                        contentValues.put(Table.Workout.DESCRIPTION, workout.getString("Description"));
                        String imageName = new Date().getTime() + ".jpg";
                        contentValues.put(Table.Workout.WORKOUT_ID, workout.getString("Workout_Id"));
                        contentValues.put(Table.Workout.PHOTO_URL, imageName);
                        try {
                            if (workout.getString("Image_Path") != null && !(workout.getString("Image_Path")
                                    .equalsIgnoreCase(""))
                                    && Utils.createLocalResourceDirectory(context)) {
                                Utils.decodeFromBase64(workout.getString("Image_Path"),
                                        imageName);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        contentValues.put(Table.SYNC_ID, workout.getString("syncID"));
                        long rowId = DBOHelper.insert(context, Table.Workout.TABLE_NAME, contentValues);
                        Log.d("added Exercise is", rowId + "");
                        if (rowId > 0) {
                            JSONArray exerciseArray = workout.getJSONArray("Exercises");
                            for (int j = 0; j < exerciseArray.length(); j++) {
                                JSONObject exerciseJSONObject = exerciseArray.getJSONObject(j);
                                ContentValues mContentValue = new ContentValues();
                                mContentValue.put(Table.WorkoutExercises.WORKOUT_ID, rowId);

                                SQLiteDatabase sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
                                Cursor mCursor = sqLiteDatabase.rawQuery("select _id from exercise where exercise_id="
                                        + exerciseJSONObject.getString("Exercise_Id"), null);
                                if (mCursor.moveToFirst()) {
                                    mContentValue.put(Table.WorkoutExercises.EXERCISE_ID,
                                            mCursor.getString(mCursor.getColumnIndex(Table.Exercise.ID)));
                                    long mRowId = DBOHelper.insert(context, Table.WorkoutExercises.TABLE_NAME, mContentValue);
                                    Log.d("added Measurement is", mRowId + "");
                                }
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void propagateExerciseAToB() {

        String LAST_SYNC = "datetime(\'" + lastSyncTime + "\')";
        String query = "select * from " + Table.Exercise.TABLE_NAME + " where "
                + Table.UPDATED + " >= " + LAST_SYNC;
        Log.d("Query", query);

        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            Cursor cursor = sqlDB.rawQuery(query, null);
            while (cursor.moveToNext()) {
                String syncId = cursor.getString(cursor.getColumnIndex(Table.SYNC_ID));
                if (!Utils.isDateAfterLastSyncTime(lastSyncTime, getServerModifiedDateFromSyncId(syncId))) {
                    Map<String, Object> exerciseFields = new HashMap<String, Object>();
                    exerciseFields.put("ID", cursor.getString(cursor.getColumnIndex(Table.Exercise.EXERCISE_ID)));
                    exerciseFields.put("Title", cursor.getString(cursor.getColumnIndex(Table.Exercise.NAME)));
                    exerciseFields.put("Description", cursor.getString(cursor.getColumnIndex(Table.Exercise.DESCRIPTION)));
                    exerciseFields.put("Active", cursor.getInt(cursor.getColumnIndex(Table.DELETED)) == 0 ? 1 : 0);
                    exerciseFields.put("Description", cursor.getString(cursor.getColumnIndex(Table.Exercise.DESCRIPTION)));
                    exerciseFields.put("Muscle_Group", cursor.getString(cursor.getColumnIndex(Table.Exercise.DESCRIPTION)));
                    exerciseFields.put("Tags", cursor.getString(cursor.getColumnIndex(Table.Exercise.TAG)));
                    exerciseFields.put("Sortorder", "1");
                    exerciseFields.put("Trainer_Id", Utils.getTrainerId(context));
                    exerciseFields.put("syncID", syncId);
                    exerciseFields.put("Imageurl", Utils.encodeBase64(Utils.THUMBNAIL_PATH +
                            cursor.getString(cursor.getColumnIndex(Table.Exercise.PHOTO_URL))));
                    HashMap<String, Object> exercise = new HashMap<String, Object>();
                    String _id = cursor.getString(cursor.getColumnIndex(Table.Exercise.ID));
                    exercise.put("exercise", exerciseFields);
                    WebService w = new WebService();
                    String response = w.webInvoke(programManagementWebServiceURL, "UpdateExercise",
                            exercise);
                    JSONObject json = null;
                    try {
                        if (response != null) {
                            json = new JSONObject(response);
                            Log.d("UpdateExercise", response);
                        }
                        if (json != null) {
                            json = json.getJSONObject("UpdateExerciseResult");
                            int exercise_id = json.getInt("Result");
                            ContentValues mContentValues = new ContentValues();
                            mContentValues.put(Table.Exercise.EXERCISE_ID, exercise_id);
                            DBOHelper.update(Table.Exercise.TABLE_NAME, mContentValues, _id);
                            exerciseIds.put(_id, exercise_id);
                            // addMeasurementsToServer(exercise_id, _id);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d("A conflict is arrived on Id ", cursor.getPosition() + "");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (sqlDB != null) {
                sqlDB.close();
            }
        }
    }

    private String getServerModifiedDateFromSyncId(String syncId) {
        for (int i = 0; i < exerciseArray.length(); i++) {
            JSONObject object = null;
            try {
                object = exerciseArray.getJSONObject(i);
                String serverSyncId = object.getString("syncID");
                if (syncId.equalsIgnoreCase(serverSyncId)) {
                    return object.getString("ModifiedDate").replace("Z", "");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public void propagateExerciseBToA() {
        if (exerciseArray != null) {
            try {
                for (int i = 0; i < exerciseArray.length(); i++) {
                    JSONObject exercise = exerciseArray.getJSONObject(i);
                    String modifiedDate = exercise.getString("ModifiedDate");
                    String modifiedDateUTC = modifiedDate.replace("Z", "");
                    Boolean isActive = exercise.getBoolean("Active");
                    Log.d("isModified", "" + Utils.isDateAfterLastSyncTime(lastSyncTime, modifiedDateUTC));
                    Log.d("modifiedDateUTC", modifiedDateUTC + "");
                    Log.d("Utils.getLastSyncTime(context)", lastSyncTime);
                    if (Utils.isDateAfterLastSyncTime(lastSyncTime, modifiedDateUTC)) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(Table.Exercise.NAME, exercise.getString("Title"));
                        contentValues.put(Table.Exercise.MUSCLE_GROUP, exercise.getString("Muscle_Group"));
                        contentValues.put(Table.Exercise.DESCRIPTION, exercise.getString("Description"));
                        contentValues.put(Table.Exercise.TAG, exercise.getString("Tags"));
                        contentValues.put(Table.DELETED, exercise.getBoolean("Active") ? 0 : 1);
                        String imageName = new Date().getTime() + ".jpg";
                        contentValues.put(Table.Exercise.EXERCISE_ID, exercise.getString("Exercise_Id"));
                        contentValues.put(Table.Exercise.PHOTO_URL, imageName);
                        try {
                            if (exercise.getString("Imageurl") != null && !(exercise.getString("Imageurl")
                                    .equalsIgnoreCase(""))
                                    && Utils.createLocalResourceDirectory(context)) {
                                Utils.decodeFromBase64(exercise.getString("Imageurl"),
                                        imageName);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //contentValues.put(Table.SYNC_ID, exercise.getString("syncID"));
                        long rowId = DBOHelper.update(Table.Exercise.TABLE_NAME, contentValues, Table.SYNC_ID, exercise.getString("syncID"));
                        Log.d("updated Exercise is", rowId + "");
                        if (rowId > 0) {
                            /*JSONArray measurementArray = exercise.getJSONArray("Measurement");
                            for (int j = 0; j < measurementArray.length(); j++) {
                                JSONObject measurementJSONObject = measurementArray.getJSONObject(j);
                                ContentValues mContentValue = new ContentValues();
                                mContentValue.put(Table.ExerciseMeasurements.EXERCISE_ID, rowId);
                                mContentValue.put(Table.ExerciseMeasurements.MEASUREMENT_ID,
                                        measurementJSONObject.getString("Measurement_Id"));
                                long mRowId = DBOHelper.insert(context, Table.ExerciseMeasurements.TABLE_NAME, mContentValue);
                                Log.d("added Measurement is", mRowId + "");
                            }*/
                        }

                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
