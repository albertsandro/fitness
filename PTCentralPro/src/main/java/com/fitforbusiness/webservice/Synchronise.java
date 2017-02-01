package com.fitforbusiness.webservice;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.appboy.Appboy;
import com.google.gson.Gson;
import com.fitforbusiness.database.DBOHelper;
import com.fitforbusiness.database.DatabaseHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.framework.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by Sanjeet on 24-Sep-14.
 */
public class Synchronise {
    private static final String scheduleManagementService = Utils.BASE_URL + Utils.SCHEDULE_MANAGEMENT_SERVICE;
    private static final String trainerWebServiceURL = Utils.BASE_URL + Utils.TRAINING_APP_SERVICE;
    private static final String clientManagementService = Utils.BASE_URL + Utils.CLIENT_MANAGEMENT_SERVICE;
    private static final String clientGroupManagementService = Utils.BASE_URL + Utils.CLIENT_GROUP_MANAGEMENT_SERVICE;
    private static final String programManagementWebServiceURL = Utils.BASE_URL + Utils.PROGRAM_MANAGEMENT_SERVICE;
    private Context context;
    private String lastSyncTime;

    public Synchronise(Context context, String lastSyncTime) {
        this.context = context;
        this.lastSyncTime = lastSyncTime;
    }

    public void sync() {
        /*new Trainer(context, lastSyncTime).sync();
        new Client(context, lastSyncTime).sync();
        new Exercise(context, lastSyncTime).sync();
        new Qualification(context, lastSyncTime).sync();
        new Group(context, lastSyncTime).sync();*/
        // new Assessment(context, lastSyncTime).sync();
        // new Workout(context, lastSyncTime).sync();
    }

    public static class Qualification {
        private Context context;
        private String lastSyncTime;
        private JSONArray serverQualificationsSet;
        private ArrayList<HashMap<String, Object>> deviceQualificationsSet;


        public Qualification(Context context, String lastSyncTime) {
            this.context = context;
            this.lastSyncTime = lastSyncTime;
            this.serverQualificationsSet = getAllServerQualifications();
            this.deviceQualificationsSet = getAllDeviceQualifications();
        }

        public Qualification(Context context) {
            this.context = context;
            this.lastSyncTime = "";
        }

        public Qualification() {

        }

        public void sync() {
            if (serverQualificationsSet != null) {
                syncObjectOnlyOnDevice();
                syncObjectOnlyOnServer();
                syncIntersection();
                //appBoy update
                updateOnAppBoy();
            }
        }

        void updateOnAppBoy(){
            Appboy.getInstance(context).getCurrentUser().
                    setCustomUserAttribute(
                            "Qualifications Points",
                            DBOHelper.totalPoints()
                    );
            Appboy.getInstance(context).getCurrentUser().
                    setCustomUserAttribute(
                            "Qualifications Hours",
                            DBOHelper.totalHours()
                    );
        }

        private void syncIntersection() {
            for (int i = 0; i < deviceQualificationsSet.size(); i++) {
                HashMap<String, Object> o = deviceQualificationsSet.get(i);
                String deviceObjectModDate = o.get(Table.UPDATED).toString();
                JSONObject serverObject = getServerObjectByWebRecordId(o.get(Table.TrainerProfileAccreditation.ACCREDITATION_ID).toString(), serverQualificationsSet);
                if (serverObject != null) {
                    try {
                        String serverObjectModDate = serverObject.getString("ModifiedDate").replace("Z", "");
                        if (Utils.isDateAfterLastSyncTime(lastSyncTime, serverObjectModDate)
                                && !Utils.isDateAfterLastSyncTime(lastSyncTime, deviceObjectModDate)) {
                            propagateServerObjectToDevice(o, serverObject);
                            Log.d("Qualification",
                                    serverObjectModDate + "deviceObjectModDate" + deviceObjectModDate);
                        } else if (Utils.isDateAfterLastSyncTime(lastSyncTime, deviceObjectModDate)
                                && !Utils.isDateAfterLastSyncTime(lastSyncTime, serverObjectModDate)) {
                            propagateDeviceObjectToServer(serverObject, o);
                            Log.d("Qualification",
                                    serverObjectModDate + "deviceObjectModDate" + deviceObjectModDate);
                        } else if (Utils.isDateAfterLastSyncTime(lastSyncTime, deviceObjectModDate)
                                && Utils.isDateAfterLastSyncTime(lastSyncTime, serverObjectModDate)) {
                            Log.d("Qualification", "" + i);
                            Log.d("Qualification",
                                    serverObjectModDate + "deviceObjectModDate" + deviceObjectModDate);
                            if (Utils.isDateAfterLastSyncTime(deviceObjectModDate, serverObjectModDate)) {
                                propagateServerObjectToDevice(o, serverObject);
                            } else {
                                propagateDeviceObjectToServer(serverObject, o);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (!Utils.isDateAfterLastSyncTime(lastSyncTime, deviceObjectModDate)) {
                        DBOHelper.delete(Table.TrainerProfileAccreditation.TABLE_NAME,
                                Table.ID, o.get(Table.ID).toString());
                    }
                }
            }
        }

        public void propagateDeviceObjectToServer(JSONObject serverObject, HashMap<String, Object> o) {
            updateOnServer(o, serverObject);

        }

        private void updateOnServer(HashMap<String, Object> o, JSONObject serverObject) {
            String device_qualification_id = o.get(Table.TrainerProfileAccreditation.ID).toString();
            HashMap<String, Object> qualificationFields = new HashMap<String, Object>();
            qualificationFields.put("Trainer_Id", Utils.getTrainerId(context));
            qualificationFields.put("Id", o.get(Table.TrainerProfileAccreditation.ACCREDITATION_ID));
            qualificationFields.put("CEC_CourseName", o.get(Table.TrainerProfileAccreditation.COURSE_NAME));
            qualificationFields.put("Points", o.get(Table.TrainerProfileAccreditation.POINTS_HOURS));
            qualificationFields.put("Expiry_Date", o.get(Table.TrainerProfileAccreditation.COMPLETED_DATE));
            qualificationFields.put("TrainingOrganization", o.get(Table.TrainerProfileAccreditation.REGISTERED_TRAINING_ORGANIZATION));
            qualificationFields.put("IsHours", o.get(Table.TrainerProfileAccreditation.IS_POINT));
            qualificationFields.put("Dropbox_Link", o.get(Table.TrainerProfileAccreditation.LINKED_FILE));
            HashMap<String, Object> qualification = new HashMap<>();
            qualification.put("accreditation", qualificationFields);
            WebService w = new WebService();
            String response = w.webInvoke(trainerWebServiceURL, "UpdateAccreditation",
                    qualification);
            JSONObject json = null;
            try {
                if (response != null) {
                    json = new JSONObject(response);
                    Log.d("UpdateAccreditation", response);
                }
                if (json != null) {
                    json = json.getJSONObject("UpdateAccreditationResult");
                    Log.d("Accreditation", json.toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void propagateServerObjectToDevice(HashMap<String, Object> o, JSONObject serverObject) {
            try {
                ContentValues contentValues = new ContentValues();
                contentValues.put(Table.TrainerProfileAccreditation.ACCREDITATION_ID, serverObject.getString("Id"));
                contentValues.put(Table.TrainerProfileAccreditation.COURSE_NAME, serverObject.getString("CEC_CourseName"));
                contentValues.put(Table.TrainerProfileAccreditation.COURSE_NO, serverObject.getString("Id"));
                contentValues.put(Table.TrainerProfileAccreditation.POINTS_HOURS, serverObject.getString("Points"));
                contentValues.put(Table.TrainerProfileAccreditation.IS_POINT, serverObject.getString("IsHours"));
                contentValues.put(Table.TrainerProfileAccreditation.REGISTERED_TRAINING_ORGANIZATION, serverObject.getString("TrainingOrganization"));
                contentValues.put(Table.TrainerProfileAccreditation.COMPLETED_DATE, serverObject.getString("Expiry_Date").replace("Z", ""));
                contentValues.put(Table.TrainerProfileAccreditation.LINKED_FILE, serverObject.getString("Dropbox_Link"));
                Log.d("accreditation id is", ""
                        + DBOHelper.updateAccreditation(contentValues, o.get(Table.TrainerProfileAccreditation.ID).toString()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void syncObjectOnlyOnServer() {
            for (int i = 0; i < serverQualificationsSet.length(); i++) {
                JSONObject accreditation;
                try {
                    accreditation = serverQualificationsSet.getJSONObject(i);
                    String createdDate = accreditation.getString("CreatedDate");
                    String createdDateUTC = createdDate.replace("Z", "");
                    Log.d("syncObjectOnlyOnServer", lastSyncTime);
                    Log.d("syncObjectOnlyOnServer", createdDateUTC);
                    Log.d("syncObjectOnlyOnServer", Utils.isDateAfterLastSyncTime(lastSyncTime, createdDateUTC) + "");
                    String accreditationId = accreditation.getString("Id");
                    if (Utils.isDateAfterLastSyncTime(lastSyncTime, createdDateUTC) && getDeviceObjectByWebRecordId(accreditationId, deviceQualificationsSet) == null) {
                        createOnDevice(accreditation);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        public void createOnDevice(JSONObject accreditation) {
            try {
                ContentValues contentValues = new ContentValues();
                contentValues.put(Table.TrainerProfileAccreditation.ACCREDITATION_ID, accreditation.getString("Id"));
                contentValues.put(Table.TrainerProfileAccreditation.COURSE_NAME, accreditation.getString("CEC_CourseName"));
                contentValues.put(Table.TrainerProfileAccreditation.COURSE_NO, accreditation.getString("Id"));
                contentValues.put(Table.TrainerProfileAccreditation.POINTS_HOURS, accreditation.getString("Points"));
                contentValues.put(Table.TrainerProfileAccreditation.IS_POINT, accreditation.getString("IsHours"));
                contentValues.put(Table.TrainerProfileAccreditation.REGISTERED_TRAINING_ORGANIZATION, accreditation.getString("TrainingOrganization"));
                contentValues.put(Table.TrainerProfileAccreditation.COMPLETED_DATE, accreditation.getString("Expiry_Date").replace("Z", ""));
                contentValues.put(Table.TrainerProfileAccreditation.LINKED_FILE, accreditation.getString("Dropbox_Link"));
                Log.d("inserted accreditation id is", ""
                        + DBOHelper.insert(context, Table.TrainerProfileAccreditation.TABLE_NAME, contentValues));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void syncObjectOnlyOnDevice() {
            for (HashMap<String, Object> o : deviceQualificationsSet) {
                Log.d("syncObjectOnlyOnDevice", lastSyncTime);
                Log.d("syncObjectOnlyOnDevice", o.get(Table.CREATED).toString());
                Log.d("syncObjectOnlyOnDevice", Utils.isDateAfterLastSyncTime(lastSyncTime, o.get(Table.CREATED).toString()) + "");
                String accreditationId = o.get(Table.TrainerProfileAccreditation.ACCREDITATION_ID).toString();
                if (Utils.isDateAfterLastSyncTime(lastSyncTime, o.get(Table.CREATED).toString()) && getServerObjectByWebRecordId(accreditationId, serverQualificationsSet) == null) {
                    createOnServer(o);
                }
            }
        }

        public void createOnServer(HashMap<String, Object> o) {
            String device_qualification_id = o.get(Table.TrainerProfileAccreditation.ID).toString();
            HashMap<String, Object> qualificationFields = new HashMap<String, Object>();
            qualificationFields.put("Trainer_Id", Utils.getTrainerId(context));
            qualificationFields.put("CEC_CourseName", o.get(Table.TrainerProfileAccreditation.COURSE_NAME));
            qualificationFields.put("Points", o.get(Table.TrainerProfileAccreditation.POINTS_HOURS));
            qualificationFields.put("Expiry_Date", o.get(Table.TrainerProfileAccreditation.COMPLETED_DATE));
            qualificationFields.put("TrainingOrganization", o.get(Table.TrainerProfileAccreditation.REGISTERED_TRAINING_ORGANIZATION));
            qualificationFields.put("IsHours", o.get(Table.TrainerProfileAccreditation.IS_POINT));
            qualificationFields.put("Dropbox_Link", o.get(Table.TrainerProfileAccreditation.LINKED_FILE));
            qualificationFields.put("syncID", o.get(Table.SYNC_ID));
            HashMap<String, Object> qualification = new HashMap<String, Object>();
            qualification.put("accreditation", qualificationFields);
            WebService w = new WebService();
            String response = w.webInvoke(trainerWebServiceURL, "AddAccreditation",
                    qualification);
            JSONObject json = null;
            try {
                if (response != null) {
                    json = new JSONObject(response);
                    Log.d("AddAccreditation", response);
                }
                if (json != null) {
                    json = json.getJSONObject("AddAccreditationResult");
                    int server_qualification_id = json.getInt("Accreditation_Id");
                    if (server_qualification_id > 0) {
                        ContentValues mContentValues = new ContentValues();
                        mContentValues.put(Table.TrainerProfileAccreditation.ACCREDITATION_ID, server_qualification_id);
                        long id = DBOHelper.update(Table.TrainerProfileAccreditation.TABLE_NAME, mContentValues, device_qualification_id);
                        Log.d("Error", id + ":(");
                    } else {
                        Log.d("Error", ":(");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public JSONArray getAllServerQualifications() {
            Map<String, String> param = new HashMap<String, String>();
            param.put("Trainer_Id", Utils.getTrainerId(context));
            param.put("Date", "1992-11-05 13:15:30Z");
            WebService w = new WebService();
            String response = w.webGet(trainerWebServiceURL,
                    "GetAllAccreditation", param);
            JSONObject obj;
            try {
                if (response != null) {
                    obj = new JSONObject(response);
                    Log.d(" GetAllAccreditation", obj.toString());
                    return obj.getJSONArray("GetAllAccreditation");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        private ArrayList<HashMap<String, Object>> getAllDeviceQualifications() {
            ArrayList qualificationsList = new ArrayList<Map<String, Object>>();
            SQLiteDatabase sqlDB = null;
            try {
                sqlDB = DatabaseHelper.instance().getReadableDatabase();
                String query = "select  * from " +
                        Table.TrainerProfileAccreditation.TABLE_NAME;
                Log.d("query is ", query);
                Cursor cursor = sqlDB.rawQuery(query, null);
                LinkedHashMap<String, Object> row;
                while (cursor.moveToNext()) {
                    row = new LinkedHashMap<String, Object>();
                    for (int i = 0; i < cursor.getColumnCount(); i++)
                        row.put(cursor.getColumnName(i), cursor.getString(i) != null ? cursor.getString(i) : "");
                    qualificationsList.add(row);
                }
                cursor.close();
            } catch (Exception e) {
                Log.d(this.getClass().getName(), e.toString());
            } finally {
            }
            Log.d("qualificationsList", qualificationsList.toString());
            return qualificationsList;
        }

        public JSONObject getServerObjectByWebRecordId(String accreditationId, JSONArray objectSet) {
            for (int i = 0; i < objectSet.length(); i++) {
                try {
                    JSONObject serverObject = objectSet.getJSONObject(i);
                    if (serverObject.getString("Id").equalsIgnoreCase(accreditationId))
                        return serverObject;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        public HashMap<String, Object> getDeviceObjectByWebRecordId(String accreditationId, ArrayList<HashMap<String, Object>> objectSet) {
            for (HashMap<String, Object> serverObject : objectSet) {
                if (serverObject.get(Table.TrainerProfileAccreditation.ACCREDITATION_ID).toString().equalsIgnoreCase(accreditationId))
                    return serverObject;
            }
            return null;
        }

    }

    public static class Exercise {
        private final Context context;
        private final String lastSyncTime;
        private final JSONArray serverExercisesSet;
        private final ArrayList<HashMap<String, Object>> deviceExercisesSet;

        public Exercise(Context context, String lastSyncTime) throws NullPointerException{
            this.context = context;
            this.lastSyncTime = lastSyncTime;
            this.serverExercisesSet = getAllServerExercises();
            this.deviceExercisesSet = getAllDeviceExercises();
//            Log.d("ExerciseSync", "ServerObjects = " + serverExercisesSet.length());
//            Log.d("ExerciseSync", "DeviceObjects = " + deviceExercisesSet.size());
        }

        public void sync() {
            if (serverExercisesSet != null) {
                Log.d("ExerciseSync", "set was not null, syncing..");
                syncObjectOnlyOnDevice();
                syncObjectOnlyOnServer();
                syncIntersection();
            }else{
                Log.d("ExerciseSync", "set was null");
            }
        }

        private void syncObjectOnlyOnServer() {
            for (int i = 0; i < serverExercisesSet.length(); i++) {
                JSONObject exercise;
                try {
                    exercise = serverExercisesSet.getJSONObject(i);
                    String createdDate = exercise.getString("CreatedDate");
                    String createdDateUTC = createdDate.replace("Z", "");
                    String syncId = exercise.getString("syncID");
                    Log.d("syncObjectOnlyOnServer", lastSyncTime);
                    Log.d("syncObjectOnlyOnServer", createdDateUTC);
                    Log.d("syncObjectOnlyOnServer:if-Condition", Utils.isDateAfterLastSyncTime(lastSyncTime, createdDateUTC) + "");

                    if (Utils.isDateAfterLastSyncTime(lastSyncTime, createdDateUTC)
                            && getDeviceObjectBySyncId(syncId, deviceExercisesSet) == null) {
                        Log.d("ExerciseSync", "Server only sync happened!");
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
                        Log.d("added Exercise", rowId + "");
                        if (rowId > 0) {
                            JSONArray measurementArray = exercise.getJSONArray("Measurement");
                            for (int j = 0; j < measurementArray.length(); j++) {
                                JSONObject measurementJSONObject = measurementArray.getJSONObject(j);
                                ContentValues mContentValue = new ContentValues();
                                mContentValue.put(Table.ExerciseMeasurements.EXERCISE_ID, rowId);
                                mContentValue.put(Table.ExerciseMeasurements.MEASUREMENT_ID,
                                        measurementJSONObject.getString("Measurement_Id"));
                                long mRowId = DBOHelper.insert(context, Table.ExerciseMeasurements.TABLE_NAME, mContentValue);
                                Log.d("added Measurement", mRowId + "");
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        private void syncObjectOnlyOnDevice() {
            for (HashMap<String, Object> o : deviceExercisesSet) {
                String syncId = o.get(Table.SYNC_ID).toString();
                Log.d("syncObjectOnlyOnDevice", lastSyncTime);
                Log.d("syncObjectOnlyOnDevice", o.get(Table.CREATED).toString());
                Log.d("syncObjectOnlyOnDevice", Utils.isDateAfterLastSyncTime(lastSyncTime, o.get(Table.CREATED).toString()) + "");
                Log.d("ExerciseSync", "DeviceOnlySyncCheck AND = (" + Utils.isDateAfterLastSyncTime(lastSyncTime, o.get(Table.CREATED).toString())
                + ", " + (getServerObjectBySyncId(syncId, serverExercisesSet) == null));
                if (Utils.isDateAfterLastSyncTime(lastSyncTime, o.get(Table.CREATED).toString())
                        && getServerObjectBySyncId(syncId, serverExercisesSet) == null) {
                    Log.d("ExerciseSync", "Device only sync happened!");
                    createExerciseOnServer(o);
                }
            }
        }

        private void syncIntersection() {
            for (int i = 0; i < deviceExercisesSet.size(); i++) {

                HashMap<String, Object> o = deviceExercisesSet.get(i);
                String deviceObjectModDate = o.get(Table.UPDATED).toString();
                JSONObject serverObject = getServerObjectBySyncId(o.get(Table.SYNC_ID).toString(), serverExercisesSet);
                if (serverObject != null) {
                    try {
                        String serverObjectModDate = serverObject.getString("ModifiedDate").replace("Z", "");
                        if (Utils.isDateAfterLastSyncTime(lastSyncTime, serverObjectModDate)
                                && !Utils.isDateAfterLastSyncTime(lastSyncTime, deviceObjectModDate)) {
                            Log.d("ExerciseSync", "Server To Device Sync happened");
                            propagateServerObjectToDevice(o, serverObject);
                            Log.d("exercise:syncIntersection():serverObjectModDate",
                                    serverObjectModDate + "deviceObjectModDate" + deviceObjectModDate);
                        } else if (Utils.isDateAfterLastSyncTime(lastSyncTime, deviceObjectModDate)
                                && !Utils.isDateAfterLastSyncTime(lastSyncTime, serverObjectModDate)) {
                            Log.d("ExerciseSync", "Device To Server Sync happened");
                            propagateDeviceObjectToServer(serverObject, o);
                            Log.d("exercise:syncIntersection():serverObjectModDate",
                                    serverObjectModDate + "deviceObjectModDate" + deviceObjectModDate);
                        } else if (Utils.isDateAfterLastSyncTime(lastSyncTime, deviceObjectModDate)
                                && Utils.isDateAfterLastSyncTime(lastSyncTime, serverObjectModDate)) {
                            Log.d("exercise:syncIntersection()", "conflict" + i);
                            Log.d("exercise:syncIntersection():serverObjectModDate",
                                    serverObjectModDate + "deviceObjectModDate" + deviceObjectModDate);
                            if (Utils.isDateAfterLastSyncTime(deviceObjectModDate, serverObjectModDate)) {
                                Log.d("ExerciseSync", "Server To Device Sync happened : Conflict");
                                propagateServerObjectToDevice(o, serverObject);
                            } else {
                                Log.d("ExerciseSync", "Device To Server Sync happened : Conflict");
                                propagateDeviceObjectToServer(serverObject, o);
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d("ExerciseSync", "Server Object was null!");
                    if (!Utils.isDateAfterLastSyncTime(lastSyncTime, deviceObjectModDate)) {
                        Log.d("ExerciseSync", "Record was deleted!");
                        DBOHelper.delete(Table.Exercise.TABLE_NAME,
                                Table.ID, o.get(Table.ID).toString());
                    }
                }
            }
        }

        private void propagateServerObjectToDevice(HashMap<String, Object> o, JSONObject serverObject) {
            ContentValues contentValues = new ContentValues();
            try {
                contentValues.put(Table.Exercise.NAME, serverObject.getString("Title"));
                contentValues.put(Table.Exercise.MUSCLE_GROUP, serverObject.getString("Muscle_Group"));
                contentValues.put(Table.Exercise.DESCRIPTION, serverObject.getString("Description"));
                contentValues.put(Table.Exercise.TAG, serverObject.getString("Tags"));
                //String imageName = new Date().getTime() + ".jpg";
                contentValues.put(Table.Exercise.EXERCISE_ID, serverObject.getString("Exercise_Id"));
                //  contentValues.put(Table.Exercise.PHOTO_URL, o.get());
                try {
                    if (serverObject.getString("Imageurl") != null && !(serverObject.getString("Imageurl")
                            .equalsIgnoreCase(""))
                            && Utils.createLocalResourceDirectory(context)) {
                        Utils.decodeFromBase64(serverObject.getString("Imageurl"),
                                o.get(Table.Exercise.PHOTO_URL).toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                contentValues.put(Table.SYNC_ID, serverObject.getString("syncID"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            long rowId = DBOHelper.update(Table.Exercise.TABLE_NAME, contentValues, o.get(Table.ID).toString());
            if (rowId > 0) {
                try {
                    DBOHelper.delete(Table.ExerciseMeasurements.TABLE_NAME, Table.ExerciseMeasurements.EXERCISE_ID, o.get(Table.ID).toString());
                    JSONArray measurement = serverObject.getJSONArray("Measurement");
                    for (int i = 0; i < measurement.length(); i++) {
                        JSONObject aMeasurement = (JSONObject) measurement.get(i);
                        ContentValues mContentValues = new ContentValues();
                        mContentValues.put(Table.ExerciseMeasurements.MEASUREMENT_ID, aMeasurement.get("Measurement_Id").toString());
                        mContentValues.put(Table.ExerciseMeasurements.EXERCISE_ID, o.get(Table.ID).toString());
                        long _id = DBOHelper.insert(context, Table.ExerciseMeasurements.TABLE_NAME, mContentValues);
                        Log.d("Updated workout_exercises", _id + "");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        public void propagateDeviceObjectToServer(JSONObject serverObject, HashMap<String, Object> o) {

            Map<String, Object> exerciseFields = new HashMap<String, Object>();
            String exercise_id = null;
            JSONArray measurement = null;
            try {
                exercise_id = serverObject.get("Exercise_Id").toString();
                measurement = serverObject.getJSONArray("Measurement");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            exerciseFields.put("ID", o.get(Table.Exercise.EXERCISE_ID));
            exerciseFields.put("Title", o.get(Table.Exercise.NAME));
            exerciseFields.put("Description", o.get(Table.Exercise.DESCRIPTION));
            exerciseFields.put("Active", "1");
            exerciseFields.put("Description", o.get(Table.Exercise.DESCRIPTION));
            exerciseFields.put("Muscle_Group", o.get(Table.Exercise.DESCRIPTION));
            exerciseFields.put("Tags", o.get(Table.Exercise.TAG));
            exerciseFields.put("Sortorder", "1");
            exerciseFields.put("Trainer_Id", Utils.getTrainerId(context));
            exerciseFields.put("syncID", o.get(Table.SYNC_ID));
            try {
                exerciseFields.put("Imageurl", Utils.encodeBase64(Utils.LOCAL_RESOURCE_PATH +
                        o.get(Table.Exercise.PHOTO_URL)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            exerciseFields.put("Trainer_Id", Utils.getTrainerId(context));
            HashMap<String, Object> exercise = new HashMap<String, Object>();
            exercise.put("exercise", exerciseFields);
            if (measurement != null && measurement.length() != 0) {
                deleteExerciseMeasurementsOnServer(measurement, exercise_id);
            }
            Log.d("measurement", measurement.toString());
            updateExerciseOnServer(exercise, o, o.get(Table.Workout.ID).toString());
        }

        private void updateExerciseOnServer(HashMap<String, Object> exercise, HashMap<String, Object> o, String exercise_id) {
            WebService w = new WebService();
            String response = w.webInvoke(programManagementWebServiceURL, "UpdateExercise",
                    exercise);
            JSONObject json;
            try {
                if (response != null) {
                    json = new JSONObject(response);
                    Log.d("UpdateExercise", json.toString());
                    json = json.getJSONObject("UpdateExerciseResult");
                    int result = json.getInt("Exercise_Id");
                    if (result > 0) {
                        ContentValues mContentValues = new ContentValues();
                        mContentValues.put(Table.Exercise.EXERCISE_ID, result);
                        long rowId = DBOHelper.update(Table.Exercise.TABLE_NAME, mContentValues, exercise_id);
                        if (rowId > 0) {
                            ArrayList<HashMap<String, Object>> measurements = (ArrayList<HashMap<String, Object>>) o.get("measurement");
                            if (measurements != null && measurements.size() > 0) {
                                for (HashMap<String, Object> aMeasurement : measurements) {
                                    HashMap<String, Object> measurement = new HashMap<String, Object>();
                                    measurement.put("Exercise_Id", result);
                                    measurement.put("Measurement_Id", aMeasurement.get(Table.ExerciseMeasurements.MEASUREMENT_ID));
                                    HashMap<String, Object> measurementObject = new HashMap<String, Object>();
                                    measurementObject.put("Measurement", measurement);
                                    Log.d("measurement object is", measurementObject.toString());
                                    addMeasurementToServer(measurementObject);
                                }
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void deleteExerciseMeasurementsOnServer(JSONArray measurement, String exercise_id) {
            for (int i = 0; i < measurement.length(); i++) {
                JSONObject measurementObject = null;
                try {
                    measurementObject = measurement.getJSONObject(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                HashMap<String, String> exerciseMeasurements = new HashMap<String, String>();
                try {
                    exerciseMeasurements.put("Measurement_Id", measurementObject != null ? measurementObject.get("Measurement_Id").toString() : null);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                exerciseMeasurements.put("Exercise_Id", exercise_id);
                WebService mWebservice = new WebService();
                String mResponse = mWebservice.webGet(programManagementWebServiceURL, "DeleteMeasurementFromExercise",
                        exerciseMeasurements);
                JSONObject mJson;
                try {
                    if (mResponse != null) {
                        mJson = new JSONObject(mResponse);
                        Log.d("DeleteMeasurementFromExercise", mJson.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void createExerciseOnServer(HashMap<String, Object> o) {

            HashMap<String, Object> exerciseFields = new HashMap<String, Object>();
            exerciseFields.put("Title", o.get(Table.Exercise.NAME));
            exerciseFields.put("Description", o.get(Table.Exercise.DESCRIPTION));
            exerciseFields.put("Active", "1");
            exerciseFields.put("Description", o.get(Table.Exercise.DESCRIPTION));
            exerciseFields.put("Muscle_Group", o.get(Table.Exercise.DESCRIPTION));
            exerciseFields.put("Tags", o.get(Table.Exercise.TAG));
            exerciseFields.put("Sortorder", "1");
            exerciseFields.put("Trainer_Id", Utils.getTrainerId(context));
            exerciseFields.put("syncID", o.get(Table.SYNC_ID));
            try {
                exerciseFields.put("Imageurl", Utils.encodeBase64(Utils.LOCAL_RESOURCE_PATH +
                        o.get(Table.Exercise.PHOTO_URL)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            HashMap<String, Object> exercise = new HashMap<String, Object>();
            exercise.put("Exercise", exerciseFields);
            WebService w = new WebService();
            String response = w.webInvoke(programManagementWebServiceURL, "AddExercise",
                    exercise);
            Log.d("ExerciseSync1", new Gson().toJson(exercise));
            Log.d("ExerciseSync", response);
            JSONObject json = null;
            try {
                if (response != null) {
                    json = new JSONObject(response);
                }
                if (json != null) {
                    json = json.getJSONObject("AddExerciseResult");
                    int exercise_id = json.getInt("Result");
                    Log.d("ExerciseSync", "Exercise created on server with ID = " + exercise_id);
                    Log.d("ExerciseSync", "Exercise created on server with msg = " + json.getJSONObject("Message"));
                    ContentValues mContentValues = new ContentValues();
                    mContentValues.put(Table.Exercise.EXERCISE_ID, exercise_id);
                    DBOHelper.update(Table.Exercise.TABLE_NAME, mContentValues, o.get(Table.Exercise.ID).toString());
                    ArrayList<HashMap<String, Object>> measurements = (ArrayList<HashMap<String, Object>>) o.get("measurement");
                    for (HashMap<String, Object> mMeasurement : measurements) {
                        HashMap<String, Object> measurementsFields = new HashMap<String, Object>();
                        measurementsFields.put("Measurement_Id", mMeasurement.get(
                                Table.ExerciseMeasurements.MEASUREMENT_ID));
                        measurementsFields.put("Exercise_Id", exercise_id);
                        HashMap<String, Object> measurement = new HashMap<String, Object>();
                        measurement.put("Measurement", measurementsFields);
                        addMeasurementToServer(measurement);
                    }
                }
            } catch (JSONException e) {
                Log.d("ExerciseSync", "Exercise not created error = " + e.getMessage());
                e.printStackTrace();
            }
        }

        private void addMeasurementToServer(HashMap<String, Object> measurement) {
            WebService w = new WebService();
            String response = w.webInvoke(programManagementWebServiceURL, "AddMeasurementToExercise",
                    measurement);
            Log.d("ExerciseSync", response);
            JSONObject json;
            try {
                if (response != null) {
                    json = new JSONObject(response);
                    json = json.getJSONObject("AddMeasurementToExerciseResult");
                    Log.d("added measurement", json.toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public JSONArray getAllServerExercises() {
            Map<String, String> param = new HashMap<String, String>();
            param.put("Trainer_Id", Utils.getTrainerId(context));
            WebService w = new WebService();
            String response = w.webGet(programManagementWebServiceURL,
                    "GetExerciseByTrainerId", param);
            JSONObject obj;
            try {
                if (response != null) {
                    obj = new JSONObject(response);
                    Log.d("exercises", obj.toString());
                    return obj.getJSONArray("GetExerciseByTrainerIdResult");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        public ArrayList<HashMap<String, Object>> getAllDeviceExercises() {
            ArrayList exercisesList = new ArrayList<Map<String, Object>>();
            SQLiteDatabase sqlDB = null;
            try {
                sqlDB = DatabaseHelper.instance().getReadableDatabase();
                String query = "select  * from " +
                        Table.Exercise.TABLE_NAME;
                Log.d("query is ", query);
                Cursor cursor = sqlDB.rawQuery(query, null);
                LinkedHashMap<String, Object> row;
                while (cursor.moveToNext()) {
                    row = new LinkedHashMap<String, Object>();
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        row.put(cursor.getColumnName(i), cursor.getString(i) != null ? cursor.getString(i) : "");
                    }
                    row.put("measurement", getMeasurements(cursor.getString(0)));
                    exercisesList.add(row);
                }
                cursor.close();
            } catch (Exception e) {
                Log.d(this.getClass().getName(), e.toString());
            } finally {
            }
            Log.d("exercisesList", exercisesList.toString());
            return exercisesList;
        }

        private ArrayList<HashMap<String, Object>> getMeasurements(String exercise_id) {
            ArrayList measurementsList = new ArrayList<Map<String, Object>>();
            SQLiteDatabase sqlDB = null;
            try {
                sqlDB = DatabaseHelper.instance().getReadableDatabase();
                String query = "select * from " + Table.ExerciseMeasurements.TABLE_NAME
                        + " where " + Table.ExerciseMeasurements.EXERCISE_ID + " = " + exercise_id;
                Log.d("query is ", query);
                Cursor cursor = sqlDB.rawQuery(query, null);
                LinkedHashMap<String, Object> row;
                while (cursor.moveToNext()) {
                    row = new LinkedHashMap<String, Object>();
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        row.put(cursor.getColumnName(i), cursor.getString(i) != null ? cursor.getString(i) : "");
                    }
                    measurementsList.add(row);
                }
                cursor.close();
            } catch (Exception e) {
                Log.d(this.getClass().getName(), e.toString());
            } finally {
            }
            return measurementsList;
        }
    }

    public static class Workout {
        private final Context context;
        private final String lastSyncTime;
        private JSONArray serverWorkoutsSet;
        private ArrayList<HashMap<String, Object>> deviceWorkoutsSet;

        public Workout(Context context, String lastSyncTime) {
            this.context = context;
            this.lastSyncTime = lastSyncTime;
            this.serverWorkoutsSet = getAllServerWorkouts();
            this.deviceWorkoutsSet = getAllDeviceWorkouts();
        }

        public Workout(Context context) {
            this.context = context;
            this.lastSyncTime = "";
        }

        public void sync() {
            if (serverWorkoutsSet != null) {
                syncObjectOnlyOnDevice();
                syncObjectOnlyOnServer();
                syncIntersection();
            }
        }

        private void syncObjectOnlyOnServer() {

            for (int i = 0; i < serverWorkoutsSet.length(); i++) {
                JSONObject workout;
                try {
                    workout = serverWorkoutsSet.getJSONObject(i);
                    String createdDate = workout.getString("CreatedDate");
                    String createdDateUTC = createdDate.replace("Z", "");
                    String syncId = workout.getString("syncID");
                    Log.d("syncObjectOnlyOnServer:lastSyncTime", lastSyncTime);
                    Log.d("syncObjectOnlyOnServer:o.get(Table.CREATED).toString())", createdDateUTC);
                    Log.d("syncObjectOnlyOnServer:if-Condition", Utils.isDateAfterLastSyncTime(lastSyncTime, createdDateUTC) + "");
                    if (Utils.isDateAfterLastSyncTime(lastSyncTime, createdDateUTC)
                            && getDeviceObjectBySyncId(syncId, deviceWorkoutsSet) == null) {
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
                        Log.d("added Workout is", rowId + "");
                        if (rowId > 0) {
                            JSONArray exerciseArray = workout.getJSONArray("Exercises");
                            for (int j = 0; j < exerciseArray.length(); j++) {
                                JSONObject exerciseJSONObject = exerciseArray.getJSONObject(j);
                                ContentValues mContentValue = new ContentValues();
                                mContentValue.put(Table.WorkoutExercises.WORKOUT_ID, rowId);

                                SQLiteDatabase sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
                                try {
                                    Cursor mCursor = sqLiteDatabase.rawQuery("select _id from exercise where exercise_id="
                                            + exerciseJSONObject.getString("Exercise_Id"), null);
                                    if (mCursor.moveToFirst()) {
                                        mContentValue.put(Table.WorkoutExercises.EXERCISE_ID,
                                                mCursor.getString(mCursor.getColumnIndex(Table.Exercise.ID)));
                                        long mRowId = DBOHelper.insert(context, Table.WorkoutExercises.TABLE_NAME, mContentValue);
                                        Log.d("added Exercise is", mRowId + "");
                                    }
                                    mCursor.close();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } finally {
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        private void syncObjectOnlyOnDevice() {
            for (HashMap<String, Object> o : deviceWorkoutsSet) {
                Log.d("workout:syncObjectOnlyOnDevice:lastSyncTime", lastSyncTime);
                Log.d("workout:syncObjectOnlyOnDevice:o.get(Table.CREATED).toString())", o.get(Table.CREATED).toString());
                Log.d("workout:syncObjectOnlyOnDevice:if-Condition", Utils.isDateAfterLastSyncTime(lastSyncTime, o.get(Table.CREATED).toString()) + "");
                String syncId = o.get(Table.SYNC_ID).toString();
                if (Utils.isDateAfterLastSyncTime(lastSyncTime, o.get(Table.CREATED).toString())
                        && getServerObjectBySyncId(syncId, serverWorkoutsSet) == null) {
                    createWorkoutOnServer(o);
                }
            }
        }

        private void syncIntersection() {
            for (int i = 0; i < deviceWorkoutsSet.size(); i++) {
                HashMap<String, Object> o = deviceWorkoutsSet.get(i);
                String deviceObjectModDate = o.get(Table.UPDATED).toString();
                JSONObject serverObject = getServerObjectBySyncId(o.get(Table.SYNC_ID).toString(), serverWorkoutsSet);
                if (serverObject != null) {
                    try {
                        String serverObjectModDate = serverObject.getString("ModifiedDate").replace("Z", "");
                        if (Utils.isDateAfterLastSyncTime(lastSyncTime, serverObjectModDate)
                                && !Utils.isDateAfterLastSyncTime(lastSyncTime, deviceObjectModDate)) {
                            propagateServerObjectToDevice(o, serverObject);
                            Log.d("workout:syncIntersection():serverObjectModDate",
                                    serverObjectModDate + "deviceObjectModDate" + deviceObjectModDate);
                        } else if (Utils.isDateAfterLastSyncTime(lastSyncTime, deviceObjectModDate)
                                && !Utils.isDateAfterLastSyncTime(lastSyncTime, serverObjectModDate)) {
                            propagateDeviceObjectToServer(serverObject, o);
                            Log.d("workout:syncIntersection():serverObjectModDate",
                                    serverObjectModDate + "deviceObjectModDate" + deviceObjectModDate);
                        } else if (Utils.isDateAfterLastSyncTime(lastSyncTime, deviceObjectModDate)
                                && Utils.isDateAfterLastSyncTime(lastSyncTime, serverObjectModDate)) {
                            Log.d("workout:syncIntersection()", "conflict" + i);
                            Log.d("workout:syncIntersection():serverObjectModDate",
                                    serverObjectModDate + "deviceObjectModDate" + deviceObjectModDate);
                            if (Utils.isDateAfterLastSyncTime(deviceObjectModDate, serverObjectModDate)) {
                                propagateServerObjectToDevice(o, serverObject);
                            } else {
                                propagateDeviceObjectToServer(serverObject, o);
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (!Utils.isDateAfterLastSyncTime(lastSyncTime, deviceObjectModDate)) {
                        DBOHelper.delete(Table.Workout.TABLE_NAME,
                                Table.ID, o.get(Table.ID).toString());
                    }
                }
            }
        }

        public void propagateDeviceObjectToServer(JSONObject serverObject, HashMap<String, Object> o) {

            Map<String, Object> workoutsFields = new HashMap<String, Object>();
            String workout_id = null;
            JSONArray exercises = null;
            try {
                workout_id = serverObject.get("Workout_Id").toString();
                exercises = serverObject.getJSONArray("Exercises");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            workoutsFields.put("Id", workout_id);
            workoutsFields.put("Title", o.get(Table.Workout.NAME));
            workoutsFields.put("Description", o.get(Table.Workout.DESCRIPTION));
            workoutsFields.put("Active", "1");
            workoutsFields.put("syncID", o.get(Table.SYNC_ID));
            try {
                workoutsFields.put("Image_Path", Utils.encodeBase64(Utils.LOCAL_RESOURCE_PATH + o.get(Table.Workout.PHOTO_URL)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            HashMap<String, Object> workout = new HashMap<String, Object>();
            workout.put("workout", workoutsFields);
            workout.put("trainer_id", Utils.getTrainerId(context));
            if (exercises != null && exercises.length() != 0) {
                deleteWorkoutExerciseOnServer(exercises, workout_id);
            }
            updateWorkoutOnServer(workout, o, o.get(Table.Workout.ID).toString());
        }

        private void updateWorkoutOnServer(HashMap<String, Object> workout, HashMap<String, Object> o, String workout_id) {
            WebService w = new WebService();
            String response = w.webInvoke(programManagementWebServiceURL, "UpdateWorkout",
                    workout);
            JSONObject json;
            try {
                if (response != null) {
                    json = new JSONObject(response);
                    Log.d("UpdateWorkout", json.toString());
                    json = json.getJSONObject("UpdateWorkoutResult");
                    int result = json.getInt("Workout_Id");
                    if (result > 0) {
                        ContentValues mContentValues = new ContentValues();
                        mContentValues.put(Table.Workout.WORKOUT_ID, result);
                        long rowId = DBOHelper.update(Table.Workout.TABLE_NAME, mContentValues, workout_id);
                        if (rowId > 0) {
                            ArrayList<HashMap<String, Object>> exercises = (ArrayList<HashMap<String, Object>>) o.get("exercise");
                            for (HashMap<String, Object> aExercise : exercises) {
                                Map<String, Object> exercise = new HashMap<String, Object>();
                                exercise.put("workoutId", result);
                                exercise.put("exerciseid", aExercise.get(Table.Exercise.EXERCISE_ID));
                                updateWorkoutExerciseOnServer(exercise);
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void deleteWorkoutExerciseOnServer(JSONArray exercises, String workout_id) {
            for (int i = 0; i < exercises.length(); i++) {
                JSONObject exerciseObject = null;
                try {
                    exerciseObject = exercises.getJSONObject(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                HashMap<String, String> workoutExercises = new HashMap<String, String>();
                try {
                    workoutExercises.put("Exercise_Id", exerciseObject != null ? exerciseObject.get("Exercise_Id").toString() : null);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                workoutExercises.put("Workout_Id", workout_id);
                WebService mWebservice = new WebService();
                String mResponse = mWebservice.webGet(programManagementWebServiceURL, "DeleteExerciseFromWorkout",
                        workoutExercises);
                JSONObject mJson;
                try {
                    if (mResponse != null) {
                        mJson = new JSONObject(mResponse);
                        Log.d("DeleteExerciseFromWorkoutExercise", mJson.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private void updateWorkoutExerciseOnServer(Map<String, Object> exercise) {
            WebService mWebservice = new WebService();
            String mResponse = mWebservice.webInvoke(programManagementWebServiceURL, "AddExerciseToWorkout",
                    exercise);
            JSONObject mJson;
            try {
                if (mResponse != null) {
                    mJson = new JSONObject(mResponse);
                    Log.d("CreateWorkoutExercise", mJson.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        private void propagateServerObjectToDevice(HashMap<String, Object> o, JSONObject serverObject) {
            ContentValues contentValues = null;
            try {
                contentValues = new ContentValues();
                contentValues.put(Table.Workout.NAME, serverObject.getString("Title"));
                contentValues.put(Table.Workout.DESCRIPTION, serverObject.getString("Description"));
                // String imageName = new Date().getTime() + ".jpg";
                contentValues.put(Table.Workout.WORKOUT_ID, serverObject.getString("Workout_Id"));
                // contentValues.put(Table.Workout.PHOTO_URL, imageName);
                try {
                    if (serverObject.getString("Image_Path") != null && !(serverObject.getString("Image_Path").equalsIgnoreCase(""))
                            && Utils.createLocalResourceDirectory(context)) {
                        Utils.decodeFromBase64(serverObject.getString("Image_Path"), o.get(Table.Workout.PHOTO_URL).toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            long rowId = DBOHelper.update(Table.Workout.TABLE_NAME, contentValues, o.get(Table.ID).toString());
            if (rowId > 0) {
                try {
                    DBOHelper.delete(Table.WorkoutExercises.TABLE_NAME, Table.WorkoutExercises.WORKOUT_ID, o.get(Table.ID).toString());
                    JSONArray exercises = serverObject.getJSONArray("Exercises");
                    for (int i = 0; i < exercises.length(); i++) {
                        JSONObject aExercise = (JSONObject) exercises.get(i);
                        ContentValues mContentValues = new ContentValues();
                        mContentValues.put(Table.WorkoutExercises.WORKOUT_ID, o.get(Table.ID).toString());
                        mContentValues.put(Table.WorkoutExercises.EXERCISE_ID, DBOHelper.getDeviceExerciseId(aExercise.get("Exercise_Id").toString()));
                        long _id = DBOHelper.insert(context, Table.WorkoutExercises.TABLE_NAME, mContentValues);
                        Log.d("Updated workout_exercises", _id + "");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        public void createWorkoutOnServer(HashMap<String, Object> o) {
            Map<String, Object> workoutsFields = new HashMap<String, Object>();
            workoutsFields.put("Title", o.get(Table.Workout.NAME));
            workoutsFields.put("Description", o.get(Table.Workout.DESCRIPTION));
            workoutsFields.put("Active", "1");
            workoutsFields.put("syncID", o.get(Table.SYNC_ID));
            try {
                workoutsFields.put("Image_Path", Utils.encodeBase64(Utils.LOCAL_RESOURCE_PATH + o.get(Table.Workout.PHOTO_URL)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            HashMap<String, Object> workout = new HashMap<String, Object>();
            workout.put("workout", workoutsFields);
            workout.put("trainerId", Utils.getTrainerId(context));

            WebService w = new WebService();
            String response = w.webInvoke(programManagementWebServiceURL, "CreateWorkout",
                    workout);
            Log.d("ExerciseSync", new Gson().toJson(workout));
            Log.d("ExerciseSync", response);
            JSONObject json;
            try {
                if (response != null) {
                    json = new JSONObject(response);
                    Log.d("CreateWorkout", json.toString());
                    json = json.getJSONObject("CreateWorkoutResult");
                    int result = json.getInt("Result");
                    if (result > 0) {
                        ContentValues mContentValues = new ContentValues();
                        mContentValues.put(Table.Workout.WORKOUT_ID, result);
                        long rowId = DBOHelper.update(Table.Workout.TABLE_NAME, mContentValues, o.get(Table.Workout.ID).toString());

                        if (rowId > 0) {
                            ArrayList<HashMap<String, Object>> exercises = (ArrayList<HashMap<String, Object>>) o.get("exercise");
                            for (HashMap<String, Object> aExercise : exercises) {
                                Map<String, Object> exercise = new HashMap<String, Object>();
                                exercise.put("workoutId", result);
                                exercise.put("exerciseid", aExercise.get(Table.Exercise.EXERCISE_ID));
                                createWorkoutExerciseOnServer(exercise);
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void createWorkoutExerciseOnServer(Map<String, Object> exercise) {
            WebService mWebservice = new WebService();
            String mResponse = mWebservice.webInvoke(programManagementWebServiceURL, "AddExerciseToWorkout",
                    exercise);
            JSONObject mJson;
            try {
                if (mResponse != null) {
                    mJson = new JSONObject(mResponse);
                    Log.d("CreateWorkoutExercise", mJson.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public ArrayList<HashMap<String, Object>> getAllDeviceWorkouts() {
            ArrayList workoutList = new ArrayList<Map<String, Object>>();
            SQLiteDatabase sqlDB = null;
            try {
                sqlDB = DatabaseHelper.instance().getReadableDatabase();
                String query = "select  * from " +
                        Table.Workout.TABLE_NAME;
                Log.d("query is ", query);
                Cursor cursor = sqlDB.rawQuery(query, null);
                LinkedHashMap<String, Object> row;
                while (cursor.moveToNext()) {
                    row = new LinkedHashMap<String, Object>();
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        row.put(cursor.getColumnName(i), cursor.getString(i) != null ? cursor.getString(i) : "");
                    }
                    row.put("exercise", getExercises(cursor.getString(0)));
                    workoutList.add(row);
                }
                cursor.close();
            } catch (Exception e) {
                Log.d(this.getClass().getName(), e.toString());
            } finally {
            }
            Log.d("exercisesList", workoutList.toString());
            return workoutList;
        }

        public ArrayList<HashMap<String, Object>> getExercises(String workout_id) {
            ArrayList<HashMap<String, Object>> exerciseList = new ArrayList<HashMap<String, Object>>();
            String ExerciseQuery = "select _id,exercise_id from exercise where " +
                    "_id in (select exercise_id from workout_exercises where" +
                    " workout_id=" + workout_id + ")";
            SQLiteDatabase sqLiteDatabase = null;
            Log.d("ExerciseQuery", ExerciseQuery);
            try {
                sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
                Cursor cursor = sqLiteDatabase.rawQuery(ExerciseQuery, null);
                LinkedHashMap<String, Object> row;
                while (cursor.moveToNext()) {
                    row = new LinkedHashMap<String, Object>();
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        row.put(cursor.getColumnName(i), cursor.getString(i) != null ? cursor.getString(i) : "");
                    }
                    exerciseList.add(row);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return exerciseList;
        }

        public JSONArray getAllServerWorkouts() {
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
                    return obj.getJSONArray("GetAllWorkoutsResult");
                }
            } catch (JSONException e) {
                e.printStackTrace();

            }
            return null;
        }
    }

    public static class Assessment {

        private Context context;
        private String lastSyncTime;
        private JSONArray serverAssessmentFormsSet;
        private ArrayList<HashMap<String, Object>> deviceAssessmentFormsSet;


        public Assessment(Context context, String lastSyncTime) {
            this.context = context;
            this.lastSyncTime = lastSyncTime;
            this.serverAssessmentFormsSet = getAllServerAssessmentForms();
            this.deviceAssessmentFormsSet = getAllDeviceAssessmentForms();
        }


        public Assessment(Context context) {
            this.context = context;
            this.lastSyncTime = "";
        }

        public Assessment() {
        }

        public ArrayList<HashMap<String, Object>> getDeviceAssessmentFormsSet() {
            return deviceAssessmentFormsSet;
        }

        public void sync() {
            if (serverAssessmentFormsSet != null) {
                syncObjectOnlyOnDevice();
                syncObjectOnlyOnServer();
                syncIntersection();
            }
        }

        private void syncIntersection() {
            for (int i = 0; i < deviceAssessmentFormsSet.size(); i++) {
                HashMap<String, Object> o = deviceAssessmentFormsSet.get(i);
                String deviceObjectModDate = o.get(Table.UPDATED).toString();
                JSONObject serverObject = getServerObjectBySyncId(o.get(Table.SYNC_ID).toString(), serverAssessmentFormsSet);
                if (serverObject != null) {
                    try {
                        String serverObjectModDate = serverObject.getString("ModifiedDate").replace("Z", "");
                        if (Utils.isDateAfterLastSyncTime(lastSyncTime, serverObjectModDate)
                                && !Utils.isDateAfterLastSyncTime(lastSyncTime, deviceObjectModDate)) {
                            propagateServerObjectToDevice(o, serverObject);
                            Log.d("Assessment:syncIntersection():serverObjectModDate",
                                    serverObjectModDate + "deviceObjectModDate" + deviceObjectModDate);
                        } else if (Utils.isDateAfterLastSyncTime(lastSyncTime, deviceObjectModDate)
                                && !Utils.isDateAfterLastSyncTime(lastSyncTime, serverObjectModDate)) {
                            propagateDeviceObjectToServer(o);
                            Log.d("Assessment:syncIntersection():serverObjectModDate",
                                    serverObjectModDate + "deviceObjectModDate" + deviceObjectModDate);
                        } else if (Utils.isDateAfterLastSyncTime(lastSyncTime, deviceObjectModDate)
                                && Utils.isDateAfterLastSyncTime(lastSyncTime, serverObjectModDate)) {
                            Log.d("Assessment:syncIntersection()", "conflict" + i);
                            Log.d("Assessment:syncIntersection():serverObjectModDate",
                                    serverObjectModDate + "deviceObjectModDate" + deviceObjectModDate);
                            if (Utils.isDateAfterLastSyncTime(deviceObjectModDate, serverObjectModDate)) {
                                propagateServerObjectToDevice(o, serverObject);
                            } else {
                                propagateDeviceObjectToServer(o);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (!Utils.isDateAfterLastSyncTime(lastSyncTime, deviceObjectModDate)) {
                        DBOHelper.delete(Table.CompletedAssessmentForm.TABLE_NAME,
                                Table.ID, o.get(Table.ID).toString());
                        DBOHelper.delete(Table.CompletedAssessmentFormField.TABLE_NAME,
                                Table.CompletedAssessmentFormField.FORM_ID, o.get(Table.ID).toString());
                    }
                }
            }
        }

        private void propagateServerObjectToDevice(HashMap<String, Object> assessment, JSONObject serverObject) {
            ContentValues contentValues = new ContentValues();
            try {
                contentValues.put(Table.CompletedAssessmentForm.FORM_NAME, serverObject.getString("Name"));
                contentValues.put(Table.CompletedAssessmentForm.COMPLETED_FORM_TYPE, serverObject.getString("FormTypeID"));
                contentValues.put(Table.CompletedAssessmentForm.FORM_ID, serverObject.getString("CompletedForm_Id"));
                if (serverObject.getString("Group_Id").equalsIgnoreCase("0")) {
                    contentValues.put(Table.CompletedAssessmentForm.GROUP_ID, DBOHelper.getDeviceClientId(serverObject.getString("Client_Id")));
                } else {
                    contentValues.put(Table.CompletedAssessmentForm.GROUP_ID, DBOHelper.getDeviceGroupId(serverObject.getString("Group_Id")));
                    contentValues.put(Table.CompletedAssessmentForm.FORM_TYPE, 1);
                }
                contentValues.put(Table.SYNC_ID, serverObject.getString("syncID"));
                String formId = assessment.get(Table.CompletedAssessmentForm.ID).toString();
                long updateId = DBOHelper.update(Table.CompletedAssessmentForm.TABLE_NAME, contentValues, Table.CompletedAssessmentForm.ID, formId);
                Log.d("updated AssessmentForm is", formId + "");
                if (updateId > 0) {
                    JSONArray fieldsArray = serverObject.getJSONArray("AssessmentFields");
                    createFieldsOnDevice(formId + "", fieldsArray);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void propagateDeviceObjectToServer(HashMap<String, Object> o) {
            Map<String, Object> assessmentForm = new HashMap<String, Object>();
            assessmentForm.put("Trainer_Id", Utils.getTrainerId(context));
            assessmentForm.put("CompletedForm_Id", o.get(Table.CompletedAssessmentForm.FORM_ID));
            assessmentForm.put("FormTypeID", o.get(Table.CompletedAssessmentForm.COMPLETED_FORM_TYPE));
            assessmentForm.put("Name", o.get(Table.CompletedAssessmentForm.FORM_NAME));
            assessmentForm.put("syncID", o.get(Table.SYNC_ID));
            if (o.get(Table.CompletedAssessmentForm.FORM_TYPE).toString().equalsIgnoreCase("0")) {
                assessmentForm.put("Client_Id", DBOHelper.getServerClientId(o.get(Table.CompletedAssessmentForm.GROUP_ID).toString()));
                assessmentForm.put("Group_Id", "0");
            } else {
                assessmentForm.put("Client_Id", "0");
                assessmentForm.put("Group_Id", DBOHelper.getServerGroupId(o.get(Table.CompletedAssessmentForm.GROUP_ID).toString()));
            }
            ArrayList<HashMap<String, Object>> formFields = new ArrayList<HashMap<String, Object>>();
            ArrayList<HashMap<String, Object>> oFields = (ArrayList<HashMap<String, Object>>) o.get("fields");
            HashMap<String, Object> field = null;
            for (HashMap<String, Object> aField : oFields) {
                field = new HashMap<String, Object>();
                field.put("Heading", "1");
                field.put("SortOrder", aField.get(Table.CompletedAssessmentFormField.SORT_ORDER));
                field.put("Title", aField.get(Table.CompletedAssessmentFormField.TITLE));
                field.put("Type", aField.get(Table.CompletedAssessmentFormField.TYPE));
                field.put("Answer", aField.get(Table.CompletedAssessmentFormField.ANSWER));
                field.put("syncID", aField.get(Table.SYNC_ID));
                field.put("CompletedForm_Id", o.get(Table.CompletedAssessmentForm.FORM_ID));
                field.put("CompletedFormField_Id", aField.get(Table.CompletedAssessmentFormField.FIELD_ID));
                formFields.add(field);
            }
            assessmentForm.put("AssessmentFields", formFields);
            HashMap<String, Object> form = new HashMap<String, Object>();
            form.put("CustomForms", assessmentForm);
            WebService w = new WebService();
            String response = w.webInvoke(trainerWebServiceURL, "UpdateCustomCompletedAssessmentForm", form);
            if (response != null) {
                Log.d("UpdateCustomCompletedAssessmentFormResult", response);
            }
        }

        private void syncObjectOnlyOnServer() {
            for (int i = 0; i < serverAssessmentFormsSet.length(); i++) {
                JSONObject assessment;
                try {
                    assessment = serverAssessmentFormsSet.getJSONObject(i);
                    String createdDate = assessment.getString("Created_Date");
                    String createdDateUTC = createdDate.replace("Z", "");
                    String syncId = assessment.getString("syncID");
                    Log.d("Assessment:syncObjectOnlyOnServer:lastSyncTime", lastSyncTime);
                    Log.d("Assessment:syncObjectOnlyOnServer:o.get(Table.CREATED).toString())", createdDateUTC);
                    Log.d("Assessment:syncObjectOnlyOnServer:if-Condition", Utils.isDateAfterLastSyncTime(lastSyncTime, createdDateUTC) + "");
                    if (Utils.isDateAfterLastSyncTime(lastSyncTime, createdDateUTC)
                            && getDeviceObjectBySyncId(syncId, deviceAssessmentFormsSet) == null) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(Table.CompletedAssessmentForm.FORM_NAME, assessment.getString("Name"));
                        contentValues.put(Table.CompletedAssessmentForm.COMPLETED_FORM_TYPE, assessment.getString("FormTypeID"));
                        contentValues.put(Table.CompletedAssessmentForm.FORM_ID, assessment.getString("CompletedForm_Id"));
                        if (assessment.getString("Group_Id").equalsIgnoreCase("0")) {
                            contentValues.put(Table.CompletedAssessmentForm.GROUP_ID, DBOHelper.getDeviceClientId(assessment.getString("Client_Id")));
                        } else {
                            contentValues.put(Table.CompletedAssessmentForm.GROUP_ID, DBOHelper.getDeviceGroupId(assessment.getString("Group_Id")));
                            contentValues.put(Table.CompletedAssessmentForm.FORM_TYPE, 1);
                        }
                        contentValues.put(Table.SYNC_ID, assessment.getString("syncID"));

                        long formId = DBOHelper.insert(context, Table.CompletedAssessmentForm.TABLE_NAME, contentValues);
                        Log.d("added AssessmentForm is", formId + "");
                        if (formId > 0) {
                            JSONArray fieldsArray = assessment.getJSONArray("AssessmentFields");
                            createFieldsOnDevice(formId + "", fieldsArray);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }

        private void syncObjectOnlyOnDevice() {
            for (HashMap<String, Object> o : deviceAssessmentFormsSet) {
                Log.d("Assessment:syncObjectOnlyOnDevice:lastSyncTime", lastSyncTime);
                Log.d("Assessment:syncObjectOnlyOnDevice:o.get(Table.CREATED).toString())", o.get(Table.CREATED).toString());
                Log.d("Assessment:syncObjectOnlyOnDevice:if-Condition", Utils.isDateAfterLastSyncTime(lastSyncTime, o.get(Table.CREATED).toString()) + "");
                String syncId = o.get(Table.SYNC_ID).toString();
                if (Utils.isDateAfterLastSyncTime(lastSyncTime, o.get(Table.CREATED).toString())
                        && getServerObjectBySyncId(syncId, serverAssessmentFormsSet) == null) {
                    createOnServer(o);
                }
            }
        }

        public void createOnServer(HashMap<String, Object> o) {
            Map<String, Object> assessmentForm = new HashMap<String, Object>();
            assessmentForm.put("Trainer_Id", Utils.getTrainerId(context));
            assessmentForm.put("FormTypeID", o.get(Table.CompletedAssessmentForm.COMPLETED_FORM_TYPE));
            assessmentForm.put("Name", o.get(Table.CompletedAssessmentForm.FORM_NAME));
            assessmentForm.put("syncID", o.get(Table.SYNC_ID));
            if (o.get(Table.CompletedAssessmentForm.FORM_TYPE).toString().equalsIgnoreCase("0")) {
                assessmentForm.put("Client_Id", DBOHelper.getServerClientId(o.get(Table.CompletedAssessmentForm.GROUP_ID).toString()));
                assessmentForm.put("Group_Id", "0");
            } else {
                assessmentForm.put("Client_Id", "0");
                assessmentForm.put("Group_Id", DBOHelper.getServerGroupId(o.get(Table.CompletedAssessmentForm.GROUP_ID).toString()));
            }
            ArrayList<HashMap<String, Object>> formFields = new ArrayList<HashMap<String, Object>>();
            ArrayList<HashMap<String, Object>> oFields = (ArrayList<HashMap<String, Object>>) o.get("fields");
            HashMap<String, Object> field = null;
            for (HashMap<String, Object> aField : oFields) {
                field = new HashMap<String, Object>();
                field.put("Heading", "1");
                field.put("Title", aField.get(Table.CompletedAssessmentFormField.TITLE));
                field.put("Type", aField.get(Table.CompletedAssessmentFormField.TYPE));
                field.put("Answer", aField.get(Table.CompletedAssessmentFormField.ANSWER));
                field.put("syncID", aField.get(Table.SYNC_ID));
                field.put("CompletedFormField_Id", 0);
                formFields.add(field);
            }
            assessmentForm.put("AssessmentFields", formFields);
            HashMap<String, Object> form = new HashMap<String, Object>();
            form.put("CustomForms", assessmentForm);
            WebService w = new WebService();
            String response = w.webInvoke(trainerWebServiceURL, "AddCustomCompletedAssessmentForm", form);
            if (response != null) {
                Log.d("response", response);
                try {
                    JSONObject json = new JSONObject(response);
                    json = json.getJSONObject("AddCustomCompletedAssessmentFormResult");
                    int formId = json.getInt("Result");
                    if (formId > 0) {
                        ContentValues mContentValues = new ContentValues();
                        mContentValues.put(Table.CompletedAssessmentForm.FORM_ID, formId);
                        long isUpdated = DBOHelper.update(Table.CompletedAssessmentForm.TABLE_NAME, mContentValues, Table.CompletedAssessmentForm.ID, o.get(Table.CompletedAssessmentForm.ID).toString());
                        if (isUpdated > 0) {
                            updateFieldsOnDevice(o, getServerAssessmentFormByFormId(formId + ""));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }

        private void updateFieldsOnDevice(HashMap<String, Object> o, JSONArray fields) {
            String deviceFormId = o.get(Table.CompletedAssessmentForm.FORM_ID).toString();
            ArrayList<HashMap<String, Object>> deviceFields = (ArrayList<HashMap<String, Object>>) o.get("fields");
            ContentValues contentValues;
            for (int i = 0; i < fields.length(); i++) {
                try {
                    contentValues = new ContentValues();
                    JSONObject aField = fields.getJSONObject(i);
                    // contentValues.put(Table.CompletedAssessmentFormField.FORM_ID, deviceFormId);
                    contentValues.put(Table.CompletedAssessmentFormField.FIELD_ID, aField.getString("CompletedFormField_Id"));
                    DBOHelper.update(Table.CompletedAssessmentFormField.TABLE_NAME, contentValues, deviceFields.get(i).get(Table.CompletedAssessmentFormField.ID).toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        private void createFieldsOnDevice(String deviceFormId, JSONArray fields) {
            DBOHelper.delete(Table.CompletedAssessmentFormField.TABLE_NAME, Table.CompletedAssessmentFormField.FORM_ID, deviceFormId);
            ContentValues contentValues;
            for (int i = 0; i < fields.length(); i++) {
                try {
                    contentValues = new ContentValues();
                    JSONObject aField = fields.getJSONObject(i);
                    contentValues.put(Table.CompletedAssessmentFormField.FORM_ID, deviceFormId);
                    contentValues.put(Table.CompletedAssessmentFormField.TITLE, aField.getString("Title"));
                    contentValues.put(Table.CompletedAssessmentFormField.FIELD_ID, aField.getString("CompletedFormField_Id"));
                    contentValues.put(Table.CompletedAssessmentFormField.SORT_ORDER, aField.getString("SortOrder"));
                    contentValues.put(Table.CompletedAssessmentFormField.TYPE, aField.getString("Type"));
                    contentValues.put(Table.CompletedAssessmentFormField.ANSWER, aField.getString("Answer"));
                    contentValues.put(Table.SYNC_ID, aField.getString("syncID"));
                    DBOHelper.insert(context, Table.CompletedAssessmentFormField.TABLE_NAME, contentValues);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        private ArrayList<HashMap<String, Object>> getAllDeviceAssessmentForms() {
            ArrayList assessmentFormList = new ArrayList<Map<String, Object>>();
            SQLiteDatabase sqlDB = null;
            try {
                sqlDB = DatabaseHelper.instance().getReadableDatabase();
                String query = "select  * from " + Table.CompletedAssessmentForm.TABLE_NAME;
                Log.d("query is ", query);
                Cursor cursor = sqlDB.rawQuery(query, null);
                LinkedHashMap<String, Object> row;
                while (cursor.moveToNext()) {
                    row = new LinkedHashMap<String, Object>();
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        row.put(cursor.getColumnName(i), cursor.getString(i) != null ? cursor.getString(i) : "");
                    }
                    //TODO: get by form id when needed on operations
                    row.put("fields", DBOHelper.getAssessmentFormFields(cursor.getString(0)));
                    assessmentFormList.add(row);
                }
                cursor.close();
            } catch (Exception e) {
                Log.d(this.getClass().getName(), e.toString());
            } finally {
            }
            Log.d("assessmentList", assessmentFormList.toString());
            return assessmentFormList;
        }


        private JSONArray getAllServerAssessmentForms() {
            Map<String, String> param = new HashMap<String, String>();
            param.put("TrainerId", Utils.getTrainerId(context));
            WebService w = new WebService();
            String response = w.webGet(trainerWebServiceURL,
                    "GetCustomCompletedAssessmentFormByTrainerId", param);
            JSONObject obj;
            try {
                if (response != null) {
                    obj = new JSONObject(response);
                    Log.d("response is", response);
                    return obj.getJSONArray("GetCustomCompletedAssessmentFormByTrainerIdResult");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        private JSONArray getServerAssessmentFormByFormId(String formId) {
            Map<String, String> param = new HashMap<String, String>();
            param.put("FormId", formId);
            WebService w = new WebService();
            String response = w.webGet(trainerWebServiceURL,
                    "GetCustomComletedAssessmentFormById", param);
            JSONObject obj;
            try {
                if (response != null) {
                    obj = new JSONObject(response);
                    Log.d("response is", response);
                    return obj.getJSONArray("GetCustomComletedAssessmentFormByIdResult");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static class Client {

        private Context context;
        private String lastSyncTime;
        private JSONArray serverClientsSet;
        private ArrayList<HashMap<String, Object>> deviceClientsSet;

        public Client(Context context, String lastSyncTime) {
            this.context = context;
            this.lastSyncTime = lastSyncTime;
            this.serverClientsSet = getAllServerClients();
            this.deviceClientsSet = getAllDeviceClients();
        }

        private ArrayList<HashMap<String, Object>> getAllDeviceClients() {
            ArrayList clientsList = new ArrayList<Map<String, Object>>();
            SQLiteDatabase sqlDB = null;
            try {
                sqlDB = DatabaseHelper.instance().getReadableDatabase();
                String query = "select  * from " +
                        Table.Client.TABLE_NAME;
                Log.d("query is ", query);
                Cursor cursor = sqlDB.rawQuery(query, null);
                LinkedHashMap<String, Object> row;
                while (cursor.moveToNext()) {
                    row = new LinkedHashMap<String, Object>();
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        row.put(cursor.getColumnName(i), cursor.getString(i) != null ? cursor.getString(i) : "");
                    }
                    clientsList.add(row);
                }
                cursor.close();
            } catch (Exception e) {
                Log.d(this.getClass().getName(), e.toString());
            } finally {
            }
            Log.d("clientsList", clientsList.toString());
            return clientsList;
        }

        public JSONArray getAllServerClients() {
            Map<String, String> param = new HashMap<String, String>();
            param.put("Trainer_Id", Utils.getTrainerId(context));
            WebService w = new WebService();
            String response = w.webGet(clientManagementService,
                    "GetClientsUnderTrainer", param);
            JSONObject obj;
            try {
                if (response != null) {
                    Log.d("response is", response);
                    obj = new JSONObject(response);
                    Log.d("GetAllWorkouts response", obj.toString());
                    return obj.getJSONArray("GetClientsUnderTrainerResult");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void sync() {
            if (serverClientsSet != null) {
                syncObjectOnlyOnDevice();
                syncObjectOnlyOnServer();
                syncIntersection();
            }
        }

        private void syncIntersection() {
            for (int i = 0; i < deviceClientsSet.size(); i++) {
                HashMap<String, Object> o = deviceClientsSet.get(i);
                String deviceObjectModDate = o.get(Table.UPDATED).toString();
                JSONObject serverObject = getServerObjectBySyncId(o.get(Table.SYNC_ID).toString(), serverClientsSet);
                if (serverObject != null) {
                    try {
                        String serverObjectModDate = serverObject.getString("ModifiedDate").replace("Z", "");
                        if (Utils.isDateAfterLastSyncTime(lastSyncTime, serverObjectModDate)
                                && !Utils.isDateAfterLastSyncTime(lastSyncTime, deviceObjectModDate)) {
                            propagateServerObjectToDevice(o, serverObject);
                            Log.d("Client:syncIntersection():serverObjectModDate",
                                    serverObjectModDate + "deviceObjectModDate" + deviceObjectModDate);
                        } else if (Utils.isDateAfterLastSyncTime(lastSyncTime, deviceObjectModDate)
                                && !Utils.isDateAfterLastSyncTime(lastSyncTime, serverObjectModDate)) {
                            propagateDeviceObjectToServer(serverObject, o);
                            Log.d("Client:syncIntersection():serverObjectModDate",
                                    serverObjectModDate + "deviceObjectModDate" + deviceObjectModDate);
                        } else if (Utils.isDateAfterLastSyncTime(lastSyncTime, deviceObjectModDate)
                                && Utils.isDateAfterLastSyncTime(lastSyncTime, serverObjectModDate)) {
                            Log.d("Client:syncIntersection()", "conflict" + i);
                            Log.d("Client:syncIntersection():serverObjectModDate",
                                    serverObjectModDate + "deviceObjectModDate" + deviceObjectModDate);
                            if (Utils.isDateAfterLastSyncTime(deviceObjectModDate, serverObjectModDate)) {
                                propagateServerObjectToDevice(o, serverObject);
                            } else {
                                propagateDeviceObjectToServer(serverObject, o);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (!Utils.isDateAfterLastSyncTime(lastSyncTime, deviceObjectModDate)) {
                        DBOHelper.delete(Table.TrainerProfileAccreditation.TABLE_NAME,
                                Table.ID, o.get(Table.ID).toString());
                    }
                }
            }
        }

        private void propagateServerObjectToDevice(HashMap<String, Object> o, JSONObject serverObject) {
            try {
                ContentValues contentValues = new ContentValues();
                contentValues.put(Table.Client.CLIENT_ID, serverObject.getString("Client_Id"));
                contentValues.put(Table.Client.FIRST_NAME, serverObject.getString("FirstName"));
                contentValues.put(Table.Client.LAST_NAME, serverObject.getString("LastName"));
                contentValues.put(Table.Client.DOB, serverObject.getString("DOB").replace("Z", ""));
                contentValues.put(Table.Client.GENDER, serverObject.getString("Gender"));
                contentValues.put(Table.Client.CONTACT_NO, serverObject.getString("ContactNo"));
                contentValues.put(Table.Client.EMAIL, serverObject.getString("EmailId"));
                contentValues.put(Table.Client.EMERGENCY_CONTACT_NUMBER, serverObject.getString("EmergencyContactNumber"));
                contentValues.put(Table.Client.EMERGENCY_CONTACT_ADDRESS, serverObject.getString("EmergencyContactName"));
                contentValues.put(Table.Client.MEDICAL_NOTES, serverObject.getString("MedicalNotes"));
                contentValues.put(Table.Client.ALLERGIES, serverObject.getString("Allergies"));
                // String imageName = new Date().getTime() + ".jpg";
                //contentValues.put(Table.Client.PHOTO_URL, imageName);
                try {
                    if (serverObject.getString("Image_Path") != null && !(serverObject.getString("Image_Path").equalsIgnoreCase(""))
                            && Utils.createLocalResourceDirectory(context)) {
                        Utils.decodeFromBase64(serverObject.getString("Image_Path"), o.get(Table.Client.PHOTO_URL).toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                contentValues.put(Table.SYNC_ID, serverObject.getString("syncID"));
                long rowId = DBOHelper.update(Table.Client.TABLE_NAME, contentValues, o.get(Table.ID).toString());
                Log.d("Update Client", rowId + "");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void syncObjectOnlyOnDevice() {
            for (HashMap<String, Object> o : deviceClientsSet) {
                Log.d("Client:syncObjectOnlyOnDevice:lastSyncTime", lastSyncTime);
                Log.d("Client:syncObjectOnlyOnDevice:o.get(Table.CREATED).toString())", o.get(Table.CREATED).toString());
                Log.d("Client:syncObjectOnlyOnDevice:if-Condition", Utils.isDateAfterLastSyncTime(lastSyncTime, o.get(Table.CREATED).toString()) + "");
                String syncId = o.get(Table.SYNC_ID).toString();
                if (Utils.isDateAfterLastSyncTime(lastSyncTime, o.get(Table.CREATED).toString()) && getServerObjectBySyncId(syncId, serverClientsSet) == null) {
                    createOnServer(o);
                }
            }
        }

        public void propagateDeviceObjectToServer(JSONObject serverObject, HashMap<String, Object> o) {
            updateOnServer(o, serverObject);
        }

        private void updateOnServer(HashMap<String, Object> o, JSONObject serverObject) {
            String device_client_id = o.get(Table.Client.ID).toString();
            HashMap<String, Object> clientFields = new HashMap<String, Object>();
            clientFields.put("Client_Id", o.get(Table.Client.CLIENT_ID));
            clientFields.put("EmailId", o.get(Table.Client.EMAIL));
            clientFields.put("Password", "");
            clientFields.put("isTempPass", 0);
            clientFields.put("FirstName", o.get(Table.Client.FIRST_NAME));
            clientFields.put("LastName", o.get(Table.Client.LAST_NAME));
            clientFields.put("DOB", o.get(Table.Client.DOB));
            clientFields.put("Gender", o.get(Table.Client.GENDER));
            clientFields.put("ContactNo", o.get(Table.Client.CONTACT_NO));
            clientFields.put("IsTrainerCreated", "1");
            try {
                clientFields.put("Image_Path", Utils.encodeBase64(Utils.LOCAL_RESOURCE_PATH +
                        o.get(Table.Client.PHOTO_URL)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            //TODO:Ensure if these fields are on iOS
            clientFields.put("City", "");
            clientFields.put("State", "0");
            clientFields.put("Zip", "0");

            clientFields.put("EmergencyContactName", o.get(Table.Client.EMERGENCY_CONTACT_ADDRESS));
            clientFields.put("EmergencyContactAddress", "");
            clientFields.put("EmergencyContactNumber", o.get(Table.Client.EMERGENCY_CONTACT_NUMBER));
            clientFields.put("MedicalNotes", o.get(Table.Client.MEDICAL_NOTES));
            clientFields.put("Allergies", o.get(Table.Client.ALLERGIES));
            clientFields.put("syncID", o.get(Table.SYNC_ID));
            HashMap<String, Object> client = new HashMap<String, Object>();
            client.put("Client", clientFields);
            //client.put("Trainer_Id", Utils.getTrainerId(context));
            WebService w = new WebService();
            String response = w.webInvoke(clientManagementService, "UpdateClient",
                    client);
            JSONObject json = null;
            try {
                if (response != null) {
                    Log.d("response", response);
                    json = new JSONObject(response);
                    Log.d("UpdateClient", response);
                }
                if (json != null) {
                    json = json.getJSONObject("UpdateClientResult");
                    int server_client_id = json.getInt("Client_Id");
                    if (server_client_id > 0) {
                        Log.d("added client to server", server_client_id + "");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void syncObjectOnlyOnServer() {
            for (int i = 0; i < serverClientsSet.length(); i++) {
                JSONObject client;
                try {
                    client = serverClientsSet.getJSONObject(i);
                    String createdDate = client.getString("date");
                    String createdDateUTC = createdDate.replace("Z", "");
                    Log.d("syncObjectOnlyOnServer:lastSyncTime", lastSyncTime);
                    Log.d("syncObjectOnlyOnServer:o.get(Table.CREATED).toString())", createdDateUTC);
                    Log.d("syncObjectOnlyOnServer:if-Condition", Utils.isDateAfterLastSyncTime(lastSyncTime, createdDateUTC) + "");
                    String syncId = client.getString("syncID");
                    if (Utils.isDateAfterLastSyncTime(lastSyncTime, createdDateUTC) && getDeviceObjectBySyncId(syncId, deviceClientsSet) == null) {
                        createOnDevice(client);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        public void createOnServer(HashMap<String, Object> o) {
            String device_client_id = o.get(Table.Client.ID).toString();
            HashMap<String, Object> clientFields = new HashMap<String, Object>();
            clientFields.put("EmailId", o.get(Table.Client.EMAIL));
            clientFields.put("Password", "");
            clientFields.put("isTempPass", 0);
            clientFields.put("FirstName", o.get(Table.Client.FIRST_NAME));
            clientFields.put("LastName", o.get(Table.Client.LAST_NAME));
            clientFields.put("DOB", o.get(Table.Client.DOB));
            clientFields.put("Gender", o.get(Table.Client.GENDER));
            clientFields.put("ContactNo", o.get(Table.Client.CONTACT_NO));
            clientFields.put("IsTrainerCreated", "1");
            try {
                clientFields.put("Image_Path", Utils.encodeBase64(Utils.LOCAL_RESOURCE_PATH +
                        o.get(Table.Client.PHOTO_URL)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            //TODO:Ensure if these fields are on iOS
            clientFields.put("City", "");
            clientFields.put("State", "0");
            clientFields.put("Zip", "0");

            clientFields.put("EmergencyContactName", o.get(Table.Client.EMERGENCY_CONTACT_ADDRESS));
            clientFields.put("EmergencyContactAddress", "");
            clientFields.put("EmergencyContactNumber", o.get(Table.Client.EMERGENCY_CONTACT_NUMBER));
            clientFields.put("MedicalNotes", o.get(Table.Client.MEDICAL_NOTES));
            clientFields.put("Allergies", o.get(Table.Client.ALLERGIES));
            clientFields.put("syncID", o.get(Table.SYNC_ID));
            HashMap<String, Object> client = new HashMap<String, Object>();
            client.put("Client", clientFields);
            client.put("Trainer_Id", Utils.getTrainerId(context));
            WebService w = new WebService();
            String response = w.webInvoke(clientManagementService, "CreateClient",
                    client);
            JSONObject json = null;
            try {
                if (response != null) {
                    Log.d("response", response);
                    json = new JSONObject(response);
                    Log.d("CreateClient", response);
                }
                if (json != null) {
                    json = json.getJSONObject("CreateClientResult");
                    int server_client_id = json.getInt("Client_Id");
                    if (server_client_id > 0) {
                        ContentValues mContentValues = new ContentValues();
                        mContentValues.put(Table.Client.CLIENT_ID, server_client_id);
                        long id = DBOHelper.update(Table.Client.TABLE_NAME, mContentValues, device_client_id);
                        Log.d("Error in adding client to server", id + ":(");
                    } else {
                        Log.d("Error in adding client to server", ":(");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void createOnDevice(JSONObject client) {
            try {
                ContentValues contentValues = new ContentValues();
                contentValues.put(Table.Client.CLIENT_ID, client.getString("Client_Id"));
                contentValues.put(Table.Client.FIRST_NAME, client.getString("FirstName"));
                contentValues.put(Table.Client.LAST_NAME, client.getString("LastName"));
                contentValues.put(Table.Client.DOB, client.getString("DOB").replace("Z", ""));
                contentValues.put(Table.Client.GENDER, client.getString("Gender"));
                contentValues.put(Table.Client.CONTACT_NO, client.getString("ContactNo"));
                contentValues.put(Table.Client.EMAIL, client.getString("EmailId"));
                contentValues.put(Table.Client.EMERGENCY_CONTACT_NUMBER, client.getString("EmergencyContactNumber"));
                //TODO:the service is not sending the EmergencyContactAddress
                contentValues.put(Table.Client.EMERGENCY_CONTACT_ADDRESS, client.getString("EmergencyContactName"));
                contentValues.put(Table.Client.MEDICAL_NOTES, client.getString("MedicalNotes"));
                contentValues.put(Table.Client.ALLERGIES, client.getString("Allergies"));
                String imageName = new Date().getTime() + ".jpg";
                contentValues.put(Table.Client.PHOTO_URL, imageName);
                try {
                    if (client.getString("Image_Path") != null && !(client.getString("Image_Path").equalsIgnoreCase(""))
                            && Utils.createLocalResourceDirectory(context)) {
                        Utils.decodeFromBase64(client.getString("Image_Path"), imageName);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                contentValues.put(Table.SYNC_ID, client.getString("syncID"));
                Log.d("inserted Client id is", ""
                        + DBOHelper.insert(context, Table.Client.TABLE_NAME, contentValues));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static class Trainer {
        private Context context;
        private String lastSyncTime;
        private HashMap<String, Object> deviceTrainerSet;
        private JSONObject serverTrainerSet;

        public Trainer(Context context, String lastSyncTime) {
            this.context = context;
            this.lastSyncTime = lastSyncTime;
            deviceTrainerSet = getDeviceTrainerSet();
            serverTrainerSet = getServerTrainerSet();
        }

        public void sync() {
            if (serverTrainerSet != null) {
                syncIntersection();
            }
        }

        private JSONObject getServerTrainerSet() {
            Map<String, String> param = new HashMap<String, String>();
            param.put("Trainer_Id", Utils.getTrainerId(context));
            WebService w = new WebService();
            String response = w.webGet(trainerWebServiceURL,
                    "GetAllTrainerDetails", param);
            JSONObject obj = null;
            try {
                if (response != null) {
                    obj = new JSONObject(response);
                    obj = obj.getJSONObject("GetAllTrainerDetailsResult");
                    Log.d("trainer", response);
                    return obj;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        private HashMap<String, Object> getDeviceTrainerSet() {
            HashMap<String, Object> trainerDetails = new LinkedHashMap<String, Object>();
            SQLiteDatabase sqlDB = null;
            try {
                sqlDB = DatabaseHelper.instance().getReadableDatabase();
                String query = "select  * from " +
                        Table.TrainerProfileDetails.TABLE_NAME + " where " + Table.TRAINER_ID + " = " + Utils.getTrainerId(context);
                Log.d("query is ", query);
                Cursor cursor = sqlDB.rawQuery(query, null);
                if (cursor.moveToFirst()) {
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        trainerDetails.put(cursor.getColumnName(i), cursor.getString(i) != null ? cursor.getString(i) : "");
                    }
                }
                cursor.close();
            } catch (Exception e) {
                Log.d("trainer exception", trainerDetails.toString());
            } finally {
            }
            Log.d("trainer", trainerDetails.toString());
            return trainerDetails;
        }

        private void syncIntersection() {
            if (!lastSyncTime.equalsIgnoreCase("1970-01-01 00:00:00")) {
                String deviceObjectModDate = deviceTrainerSet.get(Table.UPDATED).toString();
                if (serverTrainerSet != null) {
                    try {
                        JSONObject trainer = serverTrainerSet.getJSONObject("Trainer");
                        String serverObjectModDate = trainer.getString("ModifiedDate").replace("Z", "");
                        if (Utils.isDateAfterLastSyncTime(lastSyncTime, serverObjectModDate)
                                && !Utils.isDateAfterLastSyncTime(lastSyncTime, deviceObjectModDate)) {
                            propagateServerObjectToDevice(serverTrainerSet);
                            Log.d("Trainer:syncIntersection():serverObjectModDate",
                                    serverObjectModDate + "deviceObjectModDate" + deviceObjectModDate);
                        } else if (Utils.isDateAfterLastSyncTime(lastSyncTime, deviceObjectModDate)
                                && !Utils.isDateAfterLastSyncTime(lastSyncTime, serverObjectModDate)) {
                            propagateDeviceObjectToServer();
                            Log.d("Trainer:syncIntersection():serverObjectModDate",
                                    serverObjectModDate + "deviceObjectModDate" + deviceObjectModDate);
                        } else if (Utils.isDateAfterLastSyncTime(lastSyncTime, deviceObjectModDate)
                                && Utils.isDateAfterLastSyncTime(lastSyncTime, serverObjectModDate)) {
                            Log.d("Trainer:syncIntersection()", "conflict");
                            Log.d("Trainer:syncIntersection():serverObjectModDate",
                                    serverObjectModDate + "deviceObjectModDate" + deviceObjectModDate);
                            if (Utils.isDateAfterLastSyncTime(deviceObjectModDate, serverObjectModDate)) {
                                propagateServerObjectToDevice(serverTrainerSet);
                            } else {
                                propagateDeviceObjectToServer();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                propagateServerObjectToDevice(serverTrainerSet);
            }
        }

        public void propagateDeviceObjectToServer() {
            updateTrainerProfile();
            updateTrainerImage();
            updateTrainerOnAppboy();
        }

        public void updateTrainerOnAppboy(){
            Appboy.getInstance(context).getCurrentUser().
                    setCustomUserAttributeToSecondsFromEpoch(
                            "PT License Date",
                            getEpoch(deviceTrainerSet.get(Table.TrainerProfileDetails.PT_LICENSE_RENEWAL_DATE).toString())
                    );
            Appboy.getInstance(context).getCurrentUser().
                    setCustomUserAttributeToSecondsFromEpoch(
                            "Insurance Date",
                            getEpoch(deviceTrainerSet.get(Table.TrainerProfileDetails.INSURANCE_EXPIRY_DATE).toString())
                    );
            Appboy.getInstance(context).getCurrentUser().
                    setCustomUserAttributeToSecondsFromEpoch(
                            "First Aid Date",
                            getEpoch(deviceTrainerSet.get(Table.TrainerProfileDetails.FIRST_AID_CERT_RENEWAL_DATE).toString())
                    );
            Appboy.getInstance(context).getCurrentUser().
                    setCustomUserAttributeToSecondsFromEpoch(
                            "AED Date",
                            getEpoch(deviceTrainerSet.get(Table.TrainerProfileDetails.AED_CERT_RENEWAL_DATE).toString())
                    );
            Appboy.getInstance(context).getCurrentUser().
                    setCustomUserAttributeToSecondsFromEpoch(
                            "CPR Date",
                            getEpoch(deviceTrainerSet.get(Table.TrainerProfileDetails.CPR_CERT_RENEWAL_DATE).toString())
                    );
            Appboy.getInstance(context).getCurrentUser().
                    setCustomUserAttributeToSecondsFromEpoch(
                            "Date Of Birth",
                            getEpoch(deviceTrainerSet.get(Table.TrainerProfileDetails.DATE_OF_BIRTH).toString())
                    );
        }

        private String convertDate(String date){
            try {
                Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
                SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sim.setTimeZone(TimeZone.getTimeZone("GMT"));
                return sim.format(d);
            }catch (Exception e){
            }
            return "";
        }
        private long getEpoch(String date){
            try {
                SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                s.setTimeZone(TimeZone.getTimeZone(TimeZone.getDefault().getID()));
                Date d = s.parse(date);
                long t = d.getTime()/1000;
                t +=  ((12 * 60 * 60) + 61);
                Log.e("Date epoch = ", t+"");
                return t;
            }catch (Exception e){
            }
            return 0;
        }
        public void updateTrainerProfile() {
            try {
                WebService w = new WebService();
                String response = w.webInvoke(trainerWebServiceURL, "UpdateAllTrainerDetails",
                        getParams());
                JSONObject json;
                Log.d("value of json is", response);
                json = new JSONObject(response);
                json = json.getJSONObject("UpdateAllTrainerDetailsResult");
                Log.d("json is", json.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception ex) {
                Log.d("value of Exception is", ex.toString());
            }

        }

        public void updateTrainerImage() {
            try {
                Map<String, Object> details = new HashMap<String, Object>();
                details.put("trainerId", Utils.getTrainerId(context));
                details.put("imageName", Utils.encodeBase64(
                        Utils.LOCAL_RESOURCE_PATH + "trainer" + ".JPG"
                ));
                WebService w = new WebService();
                String response = w.webInvoke(trainerWebServiceURL, "UpdateTrainerImage",
                        details);
                JSONObject json;
                if (response != null) {
                    Log.d("value of json is", response);
                    json = new JSONObject(response);
                    json = json.getJSONObject("UpdateTrainerImageResult");
                    Log.d("response", response);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private Map<String, Object> getParams() {
            Map<String, Object> params = new HashMap<String, Object>();
            Map<String, Object> details = new HashMap<String, Object>();
            Map<String, Object> trainer = new HashMap<String, Object>();

            trainer.put("Trainer_Id", deviceTrainerSet.get(Table.TrainerProfileDetails.TRAINER_ID));
            trainer.put("FirstName", deviceTrainerSet.get(Table.TrainerProfileDetails.FIRST_NAME));
            trainer.put("LastName", deviceTrainerSet.get(Table.TrainerProfileDetails.LAST_NAME));
            trainer.put("ContactNo", deviceTrainerSet.get(Table.TrainerProfileDetails.PHONE_NO));
            trainer.put("EmergencyContact", deviceTrainerSet.get(Table.TrainerProfileDetails.EMERGENCY_CONTACT));
            trainer.put("EmergencyNumber", deviceTrainerSet.get(Table.TrainerProfileDetails.EMERGENCY_CONTACT_NO));
            trainer.put("DOB", deviceTrainerSet.get(Table.TrainerProfileDetails.DATE_OF_BIRTH));
            trainer.put("Gender", deviceTrainerSet.get(Table.TrainerProfileDetails.GENDER));
            trainer.put("Twitterlink", deviceTrainerSet.get(Table.TrainerProfileDetails.TWITTER_ID));
            trainer.put("Facebooklink", deviceTrainerSet.get(Table.TrainerProfileDetails.FACEBOOK_ID));

            params.put("Trainer", trainer);

            Map<String, Object> insurance = new HashMap<String, Object>();
            insurance.put("Trainer_Id", deviceTrainerSet.get(Table.TrainerProfileDetails.TRAINER_ID));
            insurance.put("Membership_No", deviceTrainerSet.get(Table.TrainerProfileDetails.INSURANCE_MEMBERSHIP_NO));
            insurance.put("Expiry_Date", deviceTrainerSet.get(Table.TrainerProfileDetails.INSURANCE_EXPIRY_DATE));
            insurance.put("Insurance_Provider", deviceTrainerSet.get(Table.TrainerProfileDetails.INSURANCE_PROVIDER));


            params.put("Insurance", insurance);

            Map<String, Object> business = new HashMap<String, Object>();
            business.put("Trainer_Id", deviceTrainerSet.get(Table.TrainerProfileDetails.TRAINER_ID));
            business.put("PT_LicenseNo", deviceTrainerSet.get(Table.TrainerProfileDetails.PT_LICENSE_NUMBER));
            business.put("PT_LicenseRenewal_Date", deviceTrainerSet.get(Table.TrainerProfileDetails.PT_LICENSE_RENEWAL_DATE));
            business.put("First_AidCertRenewal", deviceTrainerSet.get(Table.TrainerProfileDetails.FIRST_AID_CERT_RENEWAL_DATE));
            business.put("CPR_CertRenewal", deviceTrainerSet.get(Table.TrainerProfileDetails.CPR_CERT_RENEWAL_DATE));
            business.put("AED_CertRenewal", deviceTrainerSet.get(Table.TrainerProfileDetails.AED_CERT_RENEWAL_DATE));
            business.put("Website", deviceTrainerSet.get(Table.TrainerProfileDetails.WEBSITE));
            business.put("Experience", deviceTrainerSet.get(Table.TrainerProfileDetails.EXPERIENCE));
            business.put("Company_Name", deviceTrainerSet.get(Table.TrainerProfileDetails.COMPANY_NAME));
            business.put("Company_Id", deviceTrainerSet.get(Table.TrainerProfileDetails.COMPANY_ID));
            business.put("Company_TaxId", deviceTrainerSet.get(Table.TrainerProfileDetails.TAX_ID));
            //TODO:Verify that this field is on the iOS or not
            business.put("CEC_Renewal_Date", "1994-11-05 00:00:00");
            business.put("CEC_Points", 0.0000);
            business.put("DwollaAcId‏", "");

            params.put("Business", business);

            details.put("trainerBusinessInsuranceDetails", params);


            return details;

        }

        private void propagateServerObjectToDevice(JSONObject serverTrainerSet) {
            JSONObject trainer = null;
            ContentValues values = new ContentValues();
            try {
                if (serverTrainerSet != null) {
                    trainer = serverTrainerSet.getJSONObject("Trainer");
                }
                if (trainer != null) {
                    values.put(Table.TrainerProfileDetails.TRAINER_ID, Utils.getTrainerId(context));
                    values.put(Table.TrainerProfileDetails.FIRST_NAME,
                            trainer.getString("FirstName"));
                    values.put(Table.TrainerProfileDetails.LAST_NAME,
                            trainer.getString("LastName"));
                    values.put(Table.TrainerProfileDetails.EMAIL_ID,
                            trainer.getString("EmailId"));
                    values.put(Table.TrainerProfileDetails.PHONE_NO,
                            trainer.getString("ContactNo"));
                    values.put(Table.TrainerProfileDetails.EMERGENCY_CONTACT,
                            trainer.getString("EmergencyContact"));
                    values.put(Table.TrainerProfileDetails.EMERGENCY_CONTACT_NO,
                            trainer.getString("EmergencyNumber"));
                    values.put(Table.TrainerProfileDetails.DATE_OF_BIRTH,
                            trainer.getString("DOB").replace("Z", ""));
                    values.put(Table.TrainerProfileDetails.GENDER,
                            trainer.getString("Gender"));
                    values.put(Table.TrainerProfileDetails.TWITTER_ID,
                            trainer.getString("Twitterlink"));
                    values.put(Table.TrainerProfileDetails.FACEBOOK_ID,
                            trainer.getString("Facebooklink"));
                    if (trainer.getString("ImagePath") != null && !(trainer.getString("ImagePath")
                            .equalsIgnoreCase(""))
                            && Utils.createLocalResourceDirectory(context)) {
                        Utils.decodeFromBase64(trainer.getString("ImagePath"),
                                "trainer" + ".JPG");
                    }
                    Log.d("JSON trainer is ", trainer.toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JSONObject insurance = null;
            try {
                insurance = serverTrainerSet.getJSONObject("Insurance");
                if (insurance != null) {
                    values.put(Table.TrainerProfileDetails.INSURANCE_MEMBERSHIP_NO, insurance.getString("Membership_No"));
                    values.put(Table.TrainerProfileDetails.INSURANCE_EXPIRY_DATE, insurance.getString("Expiry_Date"));
                    values.put(Table.TrainerProfileDetails.INSURANCE_PROVIDER, insurance.getString("Insurance_Provider"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JSONObject business = null;
            try {
                business = serverTrainerSet.getJSONObject("Business");
                if (business != null) {
                    values.put(Table.TrainerProfileDetails.PT_LICENSE_NUMBER, business.getString("PT_LicenseNo"));
                    values.put(Table.TrainerProfileDetails.PT_LICENSE_RENEWAL_DATE, business.getString("PT_LicenseRenewal_Date"));
                    values.put(Table.TrainerProfileDetails.FIRST_AID_CERT_RENEWAL_DATE, business.getString("First_AidCertRenewal"));
                    values.put(Table.TrainerProfileDetails.CPR_CERT_RENEWAL_DATE, business.getString("CPR_CertRenewal").replace("Z", ""));
                    values.put(Table.TrainerProfileDetails.AED_CERT_RENEWAL_DATE, business.getString("AED_CertRenewal").replace("Z", ""));
                    values.put(Table.TrainerProfileDetails.COMPANY_NAME, business.getString("Company_Name"));
                    values.put(Table.TrainerProfileDetails.COMPANY_ID, business.getString("Company_Id"));
                    values.put(Table.TrainerProfileDetails.TAX_ID, business.getString("Company_TaxId"));
                    values.put(Table.TrainerProfileDetails.WEBSITE, business.getString("Website"));
                    values.put(Table.TrainerProfileDetails.EXPERIENCE, business.getString("Experience"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("updated trainer id is", "" + DBOHelper.update(Table.TrainerProfileDetails.TABLE_NAME,
                    values, Table.TrainerProfileDetails.TRAINER_ID, Utils.getTrainerId(context)));
        }
    }

    public static class Group {
        private final Context context;
        private final String lastSyncTime;
        private JSONArray serverGroupsSet;
        private ArrayList<HashMap<String, Object>> deviceGroupsSet;

        public Group(Context context, String lastSyncTime) {
            this.context = context;
            this.lastSyncTime = lastSyncTime;
            this.serverGroupsSet = getAllServerGroups();
            this.deviceGroupsSet = getAllDeviceGroups();
        }

        public Group(Context context) {
            this.context = context;
            this.lastSyncTime = "";
        }

        public void sync() {
            if (serverGroupsSet != null) {
                syncObjectOnlyOnDevice();
                syncObjectOnlyOnServer();
                syncIntersection();
            }
        }

        private void syncObjectOnlyOnServer() {
            for (int i = 0; i < serverGroupsSet.length(); i++) {
                JSONObject group;
                try {
                    group = serverGroupsSet.getJSONObject(i);
                    String createdDate = group.getString("CreatedDate");
                    String createdDateUTC = createdDate.replace("Z", "");
                    // String syncId = group.getString("syncID");
                    String groupId = group.getString("Group_Id");
                    Log.d("Group:syncObjectOnlyOnServer:lastSyncTime", lastSyncTime);
                    Log.d("Group:syncObjectOnlyOnServer:o.get(Table.CREATED).toString())", createdDateUTC);
                    Log.d("Group:syncObjectOnlyOnServer:if-Condition", Utils.isDateAfterLastSyncTime(lastSyncTime, createdDateUTC) + "");
                    if (Utils.isDateAfterLastSyncTime(lastSyncTime, createdDateUTC)
                            && getDeviceObjectByWebRecordId(groupId, deviceGroupsSet) == null) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(Table.Group.NAME, group.getString("Name"));
                        String imageName = new Date().getTime() + ".jpg";
                        contentValues.put(Table.Group.GROUP_ID, group.getString("Group_Id"));
                        contentValues.put(Table.Group.PHOTO_URL, imageName);
                        try {
                            if (group.getString("image_path") != null && !(group.getString("image_path")
                                    .equalsIgnoreCase(""))
                                    && Utils.createLocalResourceDirectory(context)) {
                                Utils.decodeFromBase64(group.getString("image_path"),
                                        imageName);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //contentValues.put(Table.SYNC_ID, group.getString("syncID"));
                        long rowId = DBOHelper.insert(context, Table.Group.TABLE_NAME, contentValues);
                        Log.d("added Group is", rowId + "");
                        if (rowId > 0) {
                            JSONArray clientArray = getAllServerClientsUnderGroup(group.getString("Group_Id"));
                            if (clientArray != null) {
                                for (int j = 0; j < clientArray.length(); j++) {
                                    JSONObject clientJSONObject = clientArray.getJSONObject(j);
                                    ContentValues mContentValue = new ContentValues();
                                    mContentValue.put(Table.GroupClients.GROUP_ID, rowId);
                                    SQLiteDatabase sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
                                    try {
                                        Cursor mCursor = sqLiteDatabase.rawQuery("select _id from client where client_id="
                                                + clientJSONObject.getString("Client_Id"), null);
                                        if (mCursor.moveToFirst()) {
                                            mContentValue.put(Table.GroupClients.CLIENT_ID,
                                                    mCursor.getString(mCursor.getColumnIndex(Table.Client.ID)));
                                            long mRowId = DBOHelper.insert(context, Table.GroupClients.TABLE_NAME, mContentValue);
                                            Log.d("added Client to th e group is", mRowId + "");
                                        }
                                        mCursor.close();
                                    } catch (JSONException e) {
                                        e.printStackTrace();

                                    } finally {
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

        private void syncObjectOnlyOnDevice() {
            for (HashMap<String, Object> o : deviceGroupsSet) {
                Log.d("Group:syncObjectOnlyOnDevice:lastSyncTime", lastSyncTime);
                Log.d("Group:syncObjectOnlyOnDevice:o.get(Table.CREATED).toString())", o.get(Table.CREATED).toString());
                Log.d("Group:syncObjectOnlyOnDevice:if-Condition", Utils.isDateAfterLastSyncTime(lastSyncTime, o.get(Table.CREATED).toString()) + "");
                //String syncId = o.get(Table.SYNC_ID).toString();
                String groupId = o.get(Table.Group.GROUP_ID).toString();
                if (Utils.isDateAfterLastSyncTime(lastSyncTime, o.get(Table.CREATED).toString())
                        && getServerObjectByWebRecordId(groupId, serverGroupsSet) == null) {
                    createGroupOnServer(o);
                }
            }
        }

        private void syncIntersection() {
            for (int i = 0; i < deviceGroupsSet.size(); i++) {
                HashMap<String, Object> o = deviceGroupsSet.get(i);
                String deviceObjectModDate = o.get(Table.UPDATED).toString();
                JSONObject serverObject = getServerObjectByWebRecordId(o.get(Table.Group.GROUP_ID).toString(), serverGroupsSet);
                if (serverObject != null) {
                    try {
                        String serverObjectModDate = serverObject.getString("ModifiedDate").replace("Z", "");
                        if (Utils.isDateAfterLastSyncTime(lastSyncTime, serverObjectModDate)
                                && !Utils.isDateAfterLastSyncTime(lastSyncTime, deviceObjectModDate)) {
                            propagateServerObjectToDevice(o, serverObject);
                            Log.d("Group:syncIntersection():serverObjectModDate",
                                    serverObjectModDate + "deviceObjectModDate" + deviceObjectModDate);
                        } else if (Utils.isDateAfterLastSyncTime(lastSyncTime, deviceObjectModDate)
                                && !Utils.isDateAfterLastSyncTime(lastSyncTime, serverObjectModDate)) {
                            propagateDeviceObjectToServer(serverObject, o);
                            Log.d("Group:syncIntersection():serverObjectModDate",
                                    serverObjectModDate + "deviceObjectModDate" + deviceObjectModDate);
                        } else if (Utils.isDateAfterLastSyncTime(lastSyncTime, deviceObjectModDate)
                                && Utils.isDateAfterLastSyncTime(lastSyncTime, serverObjectModDate)) {
                            Log.d("Group:syncIntersection()", "conflict" + i);
                            Log.d("Group:syncIntersection():serverObjectModDate",
                                    serverObjectModDate + "deviceObjectModDate" + deviceObjectModDate);
                            if (Utils.isDateAfterLastSyncTime(deviceObjectModDate, serverObjectModDate)) {
                                propagateServerObjectToDevice(o, serverObject);
                            } else {
                                propagateDeviceObjectToServer(serverObject, o);
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (!Utils.isDateAfterLastSyncTime(lastSyncTime, deviceObjectModDate)) {
                        DBOHelper.delete(Table.Group.TABLE_NAME,
                                Table.ID, o.get(Table.ID).toString());
                    }
                }
            }
        }

        public void propagateDeviceObjectToServer(JSONObject serverObject, HashMap<String, Object> o) {

            HashMap<String, Object> groupFields = new HashMap<String, Object>();
            String groupId = null;
            JSONArray clients = null;
            try {
                groupId = serverObject.get("Group_Id").toString();
                clients = getAllServerClientsUnderGroup(groupId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            groupFields.put("Group_Id", groupId);
            groupFields.put("Name", o.get(Table.Group.NAME));
            groupFields.put("Description", "");
            groupFields.put("Trainer_Id", Utils.getTrainerId(context));
            groupFields.put("syncID", o.get(Table.SYNC_ID));
            try {
                groupFields.put("Image_path", Utils.encodeBase64(Utils.LOCAL_RESOURCE_PATH + o.get(Table.Group.PHOTO_URL)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (clients != null && clients.length() != 0) {
                deleteGroupClientsOnServer(clients, groupId);
            }
            Log.d("groupFields", groupFields.toString());
            Log.d("o", o.toString());
            Log.d("o.get(Table.Group.ID).toString()", o.get(Table.Group.ID).toString());
            updateGroupOnServer(groupFields, o, o.get(Table.Group.ID).toString());
        }

        private void updateGroupOnServer(HashMap<String, Object> groupFields, HashMap<String, Object> o, String groupId) {
            WebService w = new WebService();
            String response = w.webInvoke(clientManagementService, "UpdateGroup",
                    groupFields);
            JSONObject json;
            try {
                if (response != null) {
                    json = new JSONObject(response);
                    Log.d("UpdateGroup", json.toString());
                    json = json.getJSONObject("UpdateGroupResult");
                    int result = json.getInt("Group_Id");
                    if (result > 0) {
                        ContentValues mContentValues = new ContentValues();
                        mContentValues.put(Table.Group.GROUP_ID, result);
                        long rowId = DBOHelper.update(Table.Group.TABLE_NAME, mContentValues, groupId);
                        if (rowId > 0) {
                            ArrayList<HashMap<String, Object>> clients = (ArrayList<HashMap<String, Object>>) o.get("clients");
                            for (HashMap<String, Object> aClient : clients) {
                                Map<String, Object> mClient = new HashMap<String, Object>();
                                mClient.put("Group_Id", result);
                                mClient.put("Client_Id", aClient.get(Table.Client.CLIENT_ID));
                                createGroupClientsOnServer(mClient);
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void deleteGroupClientsOnServer(JSONArray clients, String group_id) {
            for (int i = 0; i < clients.length(); i++) {
                JSONObject clientObject = null;
                try {
                    clientObject = clients.getJSONObject(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                HashMap<String, String> groupClients = new HashMap<String, String>();
                try {
                    groupClients.put("Client_Id", clientObject != null ? clientObject.get("Client_Id").toString() : null);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                groupClients.put("Group_Id", group_id);
                WebService mWebservice = new WebService();
                String mResponse = mWebservice.webGet(clientGroupManagementService, "DeleteClientsInGroup",
                        groupClients);
                JSONObject mJson;
                try {
                    if (mResponse != null) {
                        mJson = new JSONObject(mResponse);
                        Log.d("DeleteClientsInGroupResult", mJson.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private void propagateServerObjectToDevice(HashMap<String, Object> o, JSONObject serverObject) {
            ContentValues contentValues = null;
            try {
                contentValues = new ContentValues();
                contentValues.put(Table.Group.NAME, serverObject.getString("Name"));
                // String imageName = new Date().getTime() + ".jpg";
                contentValues.put(Table.Group.GROUP_ID, serverObject.getString("Group_Id"));
                // contentValues.put(Table.Group.PHOTO_URL, imageName);
                try {
                    if (serverObject.getString("image_path") != null && !(serverObject.getString("image_path")
                            .equalsIgnoreCase(""))
                            && Utils.createLocalResourceDirectory(context)) {
                        Utils.decodeFromBase64(serverObject.getString("image_path"),
                                o.get(Table.Group.PHOTO_URL).toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            long rowId = DBOHelper.update(Table.Group.TABLE_NAME, contentValues, o.get(Table.ID).toString());
            if (rowId > 0) {
                try {
                    DBOHelper.delete(Table.GroupClients.TABLE_NAME, Table.GroupClients.GROUP_ID, o.get(Table.ID).toString());
                    JSONArray clients = getAllServerClientsUnderGroup(serverObject.getString("Group_Id"));
                    for (int i = 0; i < clients.length(); i++) {
                        JSONObject aClient = (JSONObject) clients.get(i);
                        ContentValues mContentValues = new ContentValues();
                        mContentValues.put(Table.GroupClients.GROUP_ID, o.get(Table.ID).toString());
                        mContentValues.put(Table.GroupClients.CLIENT_ID, DBOHelper.getDeviceClientId(aClient.get("Client_Id").toString()));
                        long _id = DBOHelper.insert(context, Table.GroupClients.TABLE_NAME, mContentValues);
                        Log.d("Updated GroupClients", _id + "");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        public void createGroupOnServer(HashMap<String, Object> o) {
            Map<String, Object> groupFields = new HashMap<String, Object>();
            groupFields.put("Name", o.get(Table.Workout.NAME));
            groupFields.put("Trainer_Id", Utils.getTrainerId(context));
            groupFields.put("syncID", o.get(Table.SYNC_ID));
            Log.d("createGroup", o.get(Table.SYNC_ID).toString());
            try {
                groupFields.put("Image_path", Utils.encodeBase64(Utils.LOCAL_RESOURCE_PATH + o.get(Table.Group.PHOTO_URL)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            WebService w = new WebService();
            Log.d("createGroup", new Gson().toJson(groupFields));
            String response = w.webInvoke(clientManagementService, "CreateGroup",
                    groupFields);
            JSONObject json;
            try {
                if (response != null) {
                    Log.d("CreateGroup response", response);
                    json = new JSONObject(response);
                    Log.d("CreateGroup", json.toString());
                    json = json.getJSONObject("CreateGroupResult");
                    int result = json.getInt("Group_Id");
                    if (result > 0) {
                        ContentValues mContentValues = new ContentValues();
                        mContentValues.put(Table.Group.GROUP_ID, result);
                        long rowId = DBOHelper.update(Table.Group.TABLE_NAME, mContentValues, o.get(Table.Group.ID).toString());
                        if (rowId > 0) {
                            ArrayList<HashMap<String, Object>> groupClients = (ArrayList<HashMap<String, Object>>) o.get("clients");
                            for (HashMap<String, Object> aClient : groupClients) {
                                Map<String, Object> client = new HashMap<String, Object>();
                                client.put("Group_Id", result);
                                client.put("Client_Id", aClient.get(Table.Client.CLIENT_ID));

                                createGroupClientsOnServer(client);
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void createGroupClientsOnServer(Map<String, Object> client) {
            Log.d("create client for group", client.toString());
            WebService mWebservice = new WebService();
            String mResponse = mWebservice.webInvoke(clientManagementService, "AddClientToGroup",
                    client);
            JSONObject mJson;
            try {
                if (mResponse != null) {
                    mJson = new JSONObject(mResponse);
                    Log.d("AddClientToGroupResult", mJson.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public ArrayList<HashMap<String, Object>> getAllDeviceGroups() {
            ArrayList groupList = new ArrayList<Map<String, Object>>();
            SQLiteDatabase sqlDB = null;
            try {
                sqlDB = DatabaseHelper.instance().getReadableDatabase();
                String query = "select  * from " +
                        Table.Group.TABLE_NAME;
                Log.d("query is ", query);
                Cursor cursor = sqlDB.rawQuery(query, null);
                LinkedHashMap<String, Object> row;
                while (cursor.moveToNext()) {
                    row = new LinkedHashMap<String, Object>();
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        row.put(cursor.getColumnName(i), cursor.getString(i) != null ? cursor.getString(i) : "");
                    }
                    row.put("clients", getClient(cursor.getString(0)));
                    groupList.add(row);
                }
                cursor.close();
            } catch (Exception e) {
                Log.d(this.getClass().getName(), e.toString());
            } finally {
            }
            Log.d("getAllDeviceGroups", groupList.toString());
            return groupList;
        }

        private ArrayList<HashMap<String, Object>> getClient(String id) {
            ArrayList<HashMap<String, Object>> groupClientList = new ArrayList<HashMap<String, Object>>();
            String GroupClientsQuery = "select _id,client_id from client where " +
                    "_id in (select client_id from group_clients where" +
                    " group_id=" + id + ")";
            SQLiteDatabase sqLiteDatabase = null;
            Log.d("groupClientsQuery", GroupClientsQuery);
            try {
                sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
                Cursor cursor = sqLiteDatabase.rawQuery(GroupClientsQuery, null);
                LinkedHashMap<String, Object> row;
                while (cursor.moveToNext()) {
                    row = new LinkedHashMap<String, Object>();
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        row.put(cursor.getColumnName(i), cursor.getString(i) != null ? cursor.getString(i) : "");
                    }
                    groupClientList.add(row);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return groupClientList;
        }

        public JSONArray getAllServerGroups() {
            Map<String, String> param = new HashMap<>();
            param.put("Trainer_Id", Utils.getTrainerId(context));
            WebService w = new WebService();
            String response = w.webGet(clientGroupManagementService,
                    "GetGroups", param);
            JSONObject obj;
            try {
                if (response != null) {
                    obj = new JSONObject(response);
                    Log.d("GetGroups response", obj.toString());
                    return obj.getJSONArray("GetGroupsResult");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        public JSONArray getAllServerClientsUnderGroup(String groupId) {
            Map<String, String> param = new HashMap<String, String>();
            param.put("Group_Id", groupId);
            WebService w = new WebService();
            String response = w.webGet(clientGroupManagementService,
                    "GetClientsInGroup", param);
            JSONObject obj;
            try {
                if (response != null) {
                    obj = new JSONObject(response);
                    Log.d("GetClientsInGroupResult response", obj.toString());
                    return obj.getJSONArray("GetClientsInGroupResult");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        public JSONObject getServerObjectByWebRecordId(String groupId, JSONArray objectSet) {
            if (objectSet != null) {
                for (int i = 0; i < objectSet.length(); i++) {
                    try {
                        JSONObject serverObject = objectSet.getJSONObject(i);
                        if (serverObject.getString("Group_Id").equalsIgnoreCase(groupId))
                            return serverObject;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        public HashMap<String, Object> getDeviceObjectByWebRecordId(String groupId, ArrayList<HashMap<String, Object>> objectSet) {
            for (HashMap<String, Object> serverObject : objectSet) {
                if (serverObject.get(Table.Group.GROUP_ID).toString().equalsIgnoreCase(groupId))
                    return serverObject;
            }
            return null;
        }

    }

    public static class Session {

        private Context context;
        private String lastSyncTime;
        private JSONArray serverSessionsSet;
        private JSONArray serverSessionMeasurementsSet;
        private ArrayList<HashMap<String, Object>> deviceSessionsSet;


        public Session(Context context, String lastSyncTime) {
            this.context = context;
            this.lastSyncTime = lastSyncTime;
            this.serverSessionsSet = getAllServerSession();
            this.deviceSessionsSet = getAllDeviceSession();
            this.serverSessionMeasurementsSet = getAllServerSessionMeasurements();
        }


        public Session(Context context) {
            this.context = context;
            this.lastSyncTime = "";
        }

        public Session() {
        }

        public ArrayList<HashMap<String, Object>> getDeviceSessionsSet() {
            return deviceSessionsSet;
        }

        public void sync() {
            if (serverSessionsSet != null) {
                syncObjectOnlyOnDevice();
                syncObjectOnlyOnServer();
                syncIntersection();
            }
        }

        private void syncIntersection() {
            for (int i = 0; i < deviceSessionsSet.size(); i++) {
                HashMap<String, Object> o = deviceSessionsSet.get(i);
                String deviceObjectModDate = o.get(Table.UPDATED).toString();
                JSONObject serverObject = getServerObjectBySyncId(o.get(Table.SYNC_ID).toString(), serverSessionsSet);
                if (serverObject != null) {
                    try {
                        String serverObjectModDate = serverObject.getString("ModifiedDate").replace("Z", "");
                        if (Utils.isDateAfterLastSyncTime(lastSyncTime, serverObjectModDate)
                                && !Utils.isDateAfterLastSyncTime(lastSyncTime, deviceObjectModDate)) {
                            propagateServerObjectToDevice(o, serverObject);
                            Log.d("Session:syncIntersection():serverObjectModDate",
                                    serverObjectModDate + "deviceObjectModDate" + deviceObjectModDate);
                        } else if (Utils.isDateAfterLastSyncTime(lastSyncTime, deviceObjectModDate)
                                && !Utils.isDateAfterLastSyncTime(lastSyncTime, serverObjectModDate)) {
                            propagateDeviceObjectToServer(o);
                            Log.d("Session:syncIntersection():serverObjectModDate",
                                    serverObjectModDate + "deviceObjectModDate" + deviceObjectModDate);
                        } else if (Utils.isDateAfterLastSyncTime(lastSyncTime, deviceObjectModDate)
                                && Utils.isDateAfterLastSyncTime(lastSyncTime, serverObjectModDate)) {
                            Log.d("Session:syncIntersection()", "conflict" + i);
                            Log.d("Session:syncIntersection():serverObjectModDate",
                                    serverObjectModDate + "deviceObjectModDate" + deviceObjectModDate);
                            if (Utils.isDateAfterLastSyncTime(deviceObjectModDate, serverObjectModDate)) {
                                propagateServerObjectToDevice(o, serverObject);
                            } else {
                                propagateDeviceObjectToServer(o);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (!Utils.isDateAfterLastSyncTime(lastSyncTime, deviceObjectModDate)) {
                        DBOHelper.delete(Table.Sessions.TABLE_NAME,
                                Table.ID, o.get(Table.ID).toString());
                        DBOHelper.delete(Table.SessionMeasurements.TABLE_NAME,
                                Table.SessionMeasurements.SESSION_ID, o.get(Table.SessionMeasurements.SESSION_ID).toString());
                    }
                }
            }
        }

        public void propagateDeviceObjectToServer(HashMap<String, Object> o) {
            if (o == null) return;
            HashMap<String, Object> sessionValue = new HashMap<String, Object>();
            Log.e("workout err", o.get(Table.Sessions.TITLE) + " , "
                    + o.get(Table.Sessions.NATIVE_ID)
                    + o.get(Table.Sessions.START_DATE));
            if (o.get(Table.Workout.WORKOUT_ID) == null) return;
            sessionValue.put("Workout_Id", DBOHelper.getServerWorkoutId(o.get(Table.Workout.WORKOUT_ID).toString()));
            HashMap<String, Object> sessionStatus = new HashMap<String, Object>();
            HashMap<String, Object> session = new HashMap<String, Object>();
            session.put("Trainer_Id", Utils.getTrainerId(context));
            session.put("Title", o.get(Table.Sessions.TITLE));
            session.put("Venue", o.get(Table.Sessions.VENUE));
            session.put("Start_Date", o.get(Table.Sessions.START_DATE));
            session.put("End_Date", o.get(Table.Sessions.END_DATE));
            session.put("Start_Time", o.get(Table.Sessions.START_TIME));
            session.put("End_Time", o.get(Table.Sessions.END_TIME));
            session.put("Notes", o.get(Table.Sessions.NOTES));
            session.put("SessionType", o.get(Table.Sessions.SESSION_TYPE));
            sessionStatus.put("SessionType", o.get(Table.Sessions.SESSION_TYPE));
            session.put("Session_Id", o.get(Table.Sessions.SESSION_ID));
            sessionStatus.put("Session_Id", o.get(Table.Sessions.SESSION_ID));

            session.put("Duration", "0");
            if (o.get(Table.Sessions.SESSION_TYPE).toString().equalsIgnoreCase("0")) {
                String clientId = DBOHelper.getServerClientId(o.get(Table.Sessions.GROUP_ID).toString());
                session.put("Client_Id", clientId);
                session.put("Group_Id", "0");

                sessionStatus.put("Client_Id", clientId);
                sessionStatus.put("Group_Id", "0");
            } else {
                String groupId = DBOHelper.getServerGroupId(o.get(Table.Sessions.GROUP_ID).toString());
                session.put("Client_Id", "0");
                session.put("Group_Id", groupId);

                sessionStatus.put("Client_Id", groupId);
                sessionStatus.put("Group_Id", "0");
            }
            session.put("Package_Id", o.get(Table.Sessions.PACKAGE_ID));
            session.put("syncID", o.get(Table.SYNC_ID));
            sessionValue.put("SessionValue", session);

            WebService w = new WebService();
            String response = w.webInvoke(scheduleManagementService, "UpdateSession", sessionValue);
            if (response != null) {
                Log.d("response", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    jsonObject = jsonObject.getJSONObject("UpdateSessionResult");
                    int sessionId = Integer.valueOf(jsonObject.getString("Id"));
                    if (sessionId > 0) {
                        updateSessionStatus(sessionStatus, o.get(Table.Sessions.SESSION_STATUS).toString());
                        deleteSessionMeasurementOnServer(sessionId + "");
                        createSessionMeasurementOnServer(o.get(Table.Sessions.ID).toString(), sessionId + "");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        private void propagateServerObjectToDevice(HashMap<String, Object> o, JSONObject serverObject) {
            try {
                ContentValues contentValues = new ContentValues();
                contentValues.put(Table.Sessions.TITLE, serverObject.getString("Title"));
                contentValues.put(Table.Sessions.START_TIME, serverObject.getString("Start_Time"));
                contentValues.put(Table.Sessions.END_TIME, serverObject.getString("End_Time"));
                contentValues.put(Table.Sessions.START_DATE, serverObject.getString("Start_Date").replace("Z", ""));
                contentValues.put(Table.Sessions.END_DATE, serverObject.getString("End_Date").replace("Z", ""));
                contentValues.put(Table.Sessions.NOTES, serverObject.getString("Notes"));
                if (serverObject.getString("Group_Id").equalsIgnoreCase("0")) {
                    contentValues.put(Table.Sessions.GROUP_ID, DBOHelper.getDeviceClientId(serverObject.getString("Client_Id")));
                    contentValues.put(Table.Sessions.SESSION_TYPE, 0);
                    contentValues.put(Table.Sessions.SESSION_STATUS, getClientSessionStatus(serverObject.getString("Session_Id")));
                } else {
                    contentValues.put(Table.Sessions.GROUP_ID, DBOHelper.getDeviceGroupId(serverObject.getString("Group_Id")));
                    contentValues.put(Table.Sessions.SESSION_TYPE, 1);
                    contentValues.put(Table.Sessions.SESSION_STATUS, getGroupSessionStatus(serverObject.getString("Session_Id")));
                }
                contentValues.put(Table.Sessions.VENUE, serverObject.getString("Venue"));

                contentValues.put(Table.Sessions.PACKAGE_ID, serverObject.getString("Package_Id"));
                contentValues.put(Table.Sessions.SESSION_ID, serverObject.getString("Session_Id"));
                contentValues.put(Table.SYNC_ID, serverObject.getString("syncID"));
                long isUpdated = DBOHelper.update(Table.Sessions.TABLE_NAME, contentValues, o.get(Table.ID).toString());
                Log.d("update Sessions id is", "" + isUpdated);
                if (isUpdated > 0) {
                    JSONArray sessionMeasurements = getServerSessionMeasurementsBySessionId(serverObject.getString("Session_Id"), serverSessionMeasurementsSet);
                    DBOHelper.delete(Table.SessionMeasurements.TABLE_NAME, Table.SessionMeasurements.SESSION_ID, o.get(Table.ID).toString());
                    for (int i = 0; i < sessionMeasurements.length(); i++) {
                        JSONObject measurements = sessionMeasurements.getJSONObject(i);
                        ContentValues mContentValues = new ContentValues();
                        mContentValues.put(Table.SessionMeasurements.SESSION_MEASUREMENT_ID, measurements.getString("SessionMeasurement_Id"));
                        mContentValues.put(Table.SessionMeasurements.MEASUREMENT_ID, measurements.getString("Measurement"));
                        mContentValues.put(Table.SessionMeasurements.EXERCISE_ID, DBOHelper.getDeviceExerciseId(measurements.getString("Exercise_Id")));
                        mContentValues.put(Table.SessionMeasurements.WORKOUT_ID, DBOHelper.getDeviceWorkoutId(measurements.getString("Workout_Id")));
                        mContentValues.put(Table.SessionMeasurements.SESSION_ID, o.get(Table.ID).toString());
                        mContentValues.put(Table.SessionMeasurements.MEASURED_VALUE, measurements.getString("MeasuredValue"));
                        mContentValues.put(Table.SessionMeasurements.TARGET_VALUE, measurements.getString("TargetValue"));
                        mContentValues.put(Table.SessionMeasurements.SET_NO, measurements.getString("SetNumber"));
                        mContentValues.put(Table.SYNC_ID, measurements.getString("syncID"));
                        long sessionMeasurementId = DBOHelper.insert(context, Table.SessionMeasurements.TABLE_NAME, mContentValues);
                        Log.d("updated sessionMeasurement id is", "" + sessionMeasurementId);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void syncObjectOnlyOnServer() {
            for (int i = 0; i < serverSessionsSet.length(); i++) {
                JSONObject session;
                try {
                    session = serverSessionsSet.getJSONObject(i);
                    String createdDate = session.getString("CreatedDate");
                    String createdDateUTC = createdDate.replace("Z", "");
                    Log.d("Session:syncObjectOnlyOnServer:lastSyncTime", lastSyncTime);
                    Log.d("Session:syncObjectOnlyOnServer:o.get(Table.CREATED).toString())", createdDateUTC);
                    Log.d("Session:syncObjectOnlyOnServer:if-Condition", Utils.isDateAfterLastSyncTime(lastSyncTime, createdDateUTC) + "");
                    String syncId = session.getString("syncID");
                    if (Utils.isDateAfterLastSyncTime(lastSyncTime, createdDateUTC) && getDeviceObjectBySyncId(syncId, deviceSessionsSet) == null) {
                        createOnDevice(session);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        public void createOnDevice(JSONObject session) {
            try {
                ContentValues contentValues = new ContentValues();
                contentValues.put(Table.Sessions.TITLE, session.getString("Title"));
                contentValues.put(Table.Sessions.START_TIME, session.getString("Start_Time"));
                contentValues.put(Table.Sessions.END_TIME, session.getString("End_Time"));
                contentValues.put(Table.Sessions.START_DATE, session.getString("Start_Date").replace("Z", ""));
                contentValues.put(Table.Sessions.END_DATE, session.getString("End_Date").replace("Z", ""));
                contentValues.put(Table.Sessions.NOTES, session.getString("Notes"));
                if (session.getString("Group_Id").equalsIgnoreCase("0")) {
                    contentValues.put(Table.Sessions.GROUP_ID, DBOHelper.getDeviceClientId(session.getString("Client_Id")));
                    contentValues.put(Table.Sessions.SESSION_TYPE, 0);
                    contentValues.put(Table.Sessions.SESSION_STATUS, getClientSessionStatus(session.getString("Session_Id")));
                } else {
                    contentValues.put(Table.Sessions.GROUP_ID, DBOHelper.getDeviceGroupId(session.getString("Group_Id")));
                    contentValues.put(Table.Sessions.SESSION_TYPE, 1);
                    contentValues.put(Table.Sessions.SESSION_STATUS, getGroupSessionStatus(session.getString("Session_Id")));
                }
                contentValues.put(Table.Sessions.VENUE, session.getString("Venue"));
                contentValues.put(Table.Sessions.PACKAGE_ID, session.getString("Package_Id"));
                contentValues.put(Table.Sessions.SESSION_ID, session.getString("Session_Id"));
                contentValues.put(Table.SYNC_ID, session.getString("syncID"));
                long sessionId = DBOHelper.insert(context, Table.Sessions.TABLE_NAME, contentValues);
                Log.d("inserted Sessions id is", "" + sessionId);
                if (sessionId > 0) {
                    JSONArray sessionMeasurements = getServerSessionMeasurementsBySessionId(session.getString("Session_Id"), serverSessionMeasurementsSet);
                    for (int i = 0; i < sessionMeasurements.length(); i++) {
                        JSONObject measurements = sessionMeasurements.getJSONObject(i);
                        ContentValues mContentValues = new ContentValues();
                        mContentValues.put(Table.SessionMeasurements.SESSION_MEASUREMENT_ID, measurements.getString("SessionMeasurement_Id"));
                        mContentValues.put(Table.SessionMeasurements.MEASUREMENT_ID, measurements.getString("Measurement"));
                        mContentValues.put(Table.SessionMeasurements.EXERCISE_ID, DBOHelper.getDeviceExerciseId(measurements.getString("Exercise_Id")));
                        mContentValues.put(Table.SessionMeasurements.WORKOUT_ID, DBOHelper.getDeviceWorkoutId(measurements.getString("Workout_Id")));
                        mContentValues.put(Table.SessionMeasurements.SESSION_ID, sessionId);
                        mContentValues.put(Table.SessionMeasurements.MEASURED_VALUE, measurements.getString("MeasuredValue"));
                        mContentValues.put(Table.SessionMeasurements.TARGET_VALUE, measurements.getString("TargetValue"));
                        mContentValues.put(Table.SessionMeasurements.SET_NO, measurements.getString("SetNumber"));
                        mContentValues.put(Table.SYNC_ID, measurements.getString("syncID"));
                        long sessionMeasurementId = DBOHelper.insert(context, Table.SessionMeasurements.TABLE_NAME, mContentValues);
                        Log.d("inserted sessionMeasurement id is", "" + sessionMeasurementId);
                    }


                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void syncObjectOnlyOnDevice() {
            for (HashMap<String, Object> o : deviceSessionsSet) {
                Log.d("Session:syncObjectOnlyOnDevice:lastSyncTime", lastSyncTime);
                Log.d("Session:syncObjectOnlyOnDevice:o.get(Table.CREATED).toString())", o.get(Table.CREATED).toString());
                Log.d("Session:syncObjectOnlyOnDevice:if-Condition", Utils.isDateAfterLastSyncTime(lastSyncTime, o.get(Table.CREATED).toString()) + "");
                String syncId = o.get(Table.SYNC_ID).toString();
                if (Utils.isDateAfterLastSyncTime(lastSyncTime, o.get(Table.CREATED).toString())
                        && getServerObjectBySyncId(syncId, serverSessionsSet) == null) {
                    createOnServer(o);
                }
            }
        }

        public void createOnServer(HashMap<String, Object> o) {
            HashMap<String, Object> sessionValue = new HashMap<String, Object>();
            sessionValue.put("Trainer_Id", Utils.getTrainerId(context));
            if (o.containsKey(Table.Workout.WORKOUT_ID) && o.get(Table.Workout.WORKOUT_ID) != null)
                sessionValue.put("Workout_Id", DBOHelper.getServerWorkoutId(o.get(Table.Workout.WORKOUT_ID).toString()));
            else
                sessionValue.put("Workout_Id", "0");
            HashMap<String, Object> session = new HashMap<String, Object>();
            session.put("Title", o.get(Table.Sessions.TITLE));
            session.put("Venue", o.get(Table.Sessions.VENUE));
            session.put("Start_Date", o.get(Table.Sessions.START_DATE));
            session.put("End_Date", o.get(Table.Sessions.END_DATE));
            session.put("Start_Time", o.get(Table.Sessions.START_TIME));
            session.put("End_Time", o.get(Table.Sessions.END_TIME));
            session.put("Notes", o.get(Table.Sessions.NOTES));
            session.put("SessionType", o.get(Table.Sessions.SESSION_TYPE));
            session.put("Trainer_Id", Utils.getTrainerId(context));
            session.put("Duration", "0");
            if (o.get(Table.Sessions.SESSION_TYPE).toString().equalsIgnoreCase("0")) {
                session.put("Client_Id", DBOHelper.getServerClientId(o.get(Table.Sessions.GROUP_ID).toString()));
                session.put("Group_Id", "0");
            } else {
                session.put("Client_Id", "0");
                session.put("Group_Id", DBOHelper.getServerGroupId(o.get(Table.Sessions.GROUP_ID).toString()));
            }
            session.put("Package_Id", o.get(Table.Sessions.PACKAGE_ID));
            session.put("syncID", o.get(Table.SYNC_ID));
            sessionValue.put("SessionValue", session);

            WebService w = new WebService();
            String response = w.webInvoke(scheduleManagementService, "AddSession", sessionValue);
            if (response != null) {
                Log.d("response", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    jsonObject = jsonObject.getJSONObject("AddSessionResult");
                    int sessionId = jsonObject.getInt("Result");
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(Table.Sessions.SESSION_ID, sessionId);
                    DBOHelper.update(Table.Sessions.TABLE_NAME, contentValues, o.get(Table.Sessions.ID).toString());
                    createSessionMeasurementOnServer(o.get(Table.Sessions.ID).toString(), sessionId + "");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }

        private void deleteSessionMeasurementOnServer(String sessionId) {
            JSONArray sessionMeasurements = getServerSessionMeasurementsBySessionId(sessionId, serverSessionMeasurementsSet);
            Log.d("delete:sessionMeasurements", sessionMeasurements.toString());
            for (int i = 0; i < sessionMeasurements.length(); i++) {
                try {
                    JSONObject aSessionMeasurement = sessionMeasurements.getJSONObject(i);
                    HashMap<String, String> mSessionMeasurement = new HashMap<String, String>();
                    mSessionMeasurement.put("SessionMeasurement_Id", aSessionMeasurement.get("SessionMeasurement_Id").toString());
                    WebService w = new WebService();
                    String response = w.webGet(scheduleManagementService, "DeleteSessionMeasurement", mSessionMeasurement);
                    if (response != null) {
                        Log.d("response", response);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        private void createSessionMeasurementOnServer(String deviceSessionId, String serverSessionId) {
            ArrayList<HashMap<String, Object>> sessionMeasurementList = getAllDeviceSessionMeasurementsBySessionId(deviceSessionId);
            for (HashMap<String, Object> mSessionMeasurement : sessionMeasurementList) {
                HashMap<String, Object> sessionMeasurementValue = new HashMap<String, Object>();
                HashMap<String, Object> sessionMeasurement = new HashMap<String, Object>();
                sessionMeasurement.put("SetNumber", mSessionMeasurement.get(Table.SessionMeasurements.SET_NO));
                sessionMeasurement.put("Measurement", mSessionMeasurement.get(Table.SessionMeasurements.MEASUREMENT_ID));
                sessionMeasurement.put("Session_Id", serverSessionId);
                sessionMeasurement.put("Workout_Id", DBOHelper.getServerWorkoutId(mSessionMeasurement.get(Table.SessionMeasurements.WORKOUT_ID).toString()));
                sessionMeasurement.put("Exercise_Id", DBOHelper.getServerExerciseId(mSessionMeasurement.get(Table.SessionMeasurements.EXERCISE_ID).toString()));
                sessionMeasurement.put("sortOrder", "0");
                sessionMeasurement.put("MeasuredValue", mSessionMeasurement.get(Table.SessionMeasurements.MEASURED_VALUE).toString().equalsIgnoreCase("") ? "0" :
                        mSessionMeasurement.get(Table.SessionMeasurements.MEASURED_VALUE).toString());
                sessionMeasurement.put("TargetValue", mSessionMeasurement.get(Table.SessionMeasurements.TARGET_VALUE).toString().equalsIgnoreCase("") ? "0" :
                        mSessionMeasurement.get(Table.SessionMeasurements.TARGET_VALUE));

                sessionMeasurement.put("syncID", mSessionMeasurement.get(Table.SYNC_ID));
                sessionMeasurementValue.put("SessionMeasurement", sessionMeasurement);

                WebService w = new WebService();
                //  String jsonTest=""
                String response = w.webInvoke(scheduleManagementService, "AddSessionMeasurement", sessionMeasurementValue);
                if (response != null) {
                    Log.d("response", response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        jsonObject = jsonObject.getJSONObject("AddSessionMeasurementResult");
                        int sessionMeasurementId = jsonObject.getInt("Id");
                        if (sessionMeasurementId > 0) {
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(Table.SessionMeasurements.SESSION_MEASUREMENT_ID, sessionMeasurementId);
                            DBOHelper.update(Table.SessionMeasurements.TABLE_NAME, contentValues, mSessionMeasurement.get(Table.SessionMeasurements.ID).toString());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private void updateSessionStatus(HashMap<String, Object> sessionStatus, String status) {
            HashMap<String, Object> sessionValue = new HashMap<String, Object>();
            sessionValue.put("SessionValue", sessionStatus);
            sessionValue.put("Session_Status", String.valueOf(Integer.valueOf(status) + 1));
            WebService w = new WebService();
            String response = w.webInvoke(scheduleManagementService, "UpdateSessionStatus", sessionValue);
            if (response != null) {
                Log.d("response", response);
            }
        }

        private int getClientSessionStatus(String sessionId) {
            HashMap<String, String> sessionStatus = new HashMap<String, String>();
            sessionStatus.put("Session_Id", sessionId);
            WebService w = new WebService();
            String response = w.webGet(scheduleManagementService, "GetSessionStatusForClient", sessionStatus);
            if (response != null) {
                Log.d("response", response);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    int status = jsonObject.getInt("GetSessionStatusForClientResult");
                    if (status > 0) {
                        return status - 1;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            return -1;
        }

        private int getGroupSessionStatus(String sessionId) {
            HashMap<String, String> sessionStatus = new HashMap<String, String>();
            sessionStatus.put("Session_Id", sessionId);
            WebService w = new WebService();
            String response = w.webGet(scheduleManagementService, "GetSessionStatusForGroup", sessionStatus);
            if (response != null) {
                Log.d("response", response);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    int status = jsonObject.getInt("GetSessionStatusForGroupResult");
                    if (status > 0) {
                        return status - 1;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return -1;
        }

        private JSONArray getAllServerSession() {
            Map<String, String> param = new HashMap<String, String>();
            param.put("Trainer_Id", Utils.getTrainerId(context));
            WebService w = new WebService();
            String response = w.webGet(scheduleManagementService,
                    "GetAllSession", param);
            JSONObject obj = null;
            try {
                if (response != null) {
                    Log.d("response is", response);
                    obj = new JSONObject(response);
                    return obj.getJSONArray("GetAllSessionResult");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        private JSONArray getAllServerSessionMeasurements() {
            Map<String, String> param = new HashMap<String, String>();
            param.put("Trainer_Id", Utils.getTrainerId(context));
            WebService w = new WebService();
            String response = w.webGet(scheduleManagementService,
                    "GetSessionMeasurementByTrainerId", param);
            JSONObject obj = null;
            try {
                if (response != null) {
                    Log.d("response is", response);
                    obj = new JSONObject(response);
                    return obj.getJSONArray("GetSessionMeasurementByTrainerIdResult");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        private ArrayList<HashMap<String, Object>> getAllDeviceSession() {
            ArrayList sessionsList = new ArrayList<Map<String, Object>>();
            SQLiteDatabase sqlDB = null;
            try {
                sqlDB = DatabaseHelper.instance().getReadableDatabase();
                String query = "SELECT sessions.*,session_measurements.workout_id " +
                        "FROM sessions LEFT JOIN session_measurements " +
                        "ON session_measurements.session_id=sessions._id" +
                        " WHERE sessions.is_native = 0 " +
                        " group by sessions._id " +
                        ";";
                //"select  * from " + Table.Sessions.TABLE_NAME;
                Log.d("query is ", query);
                Cursor cursor = sqlDB.rawQuery(query, null);
                LinkedHashMap<String, Object> row;
                while (cursor.moveToNext()) {
                    row = new LinkedHashMap<String, Object>();
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        row.put(cursor.getColumnName(i), cursor.getString(i) != null ? cursor.getString(i) : "");
                    }
                    sessionsList.add(row);
                }
                cursor.close();
            } catch (Exception e) {
                Log.d(this.getClass().getName(), e.toString());
            } finally {
            }
            Log.d("sessionList", new Gson().toJson(sessionsList));
            return sessionsList;
        }

        private ArrayList<HashMap<String, Object>> getAllDeviceSessionMeasurementsBySessionId(String sessionId) {
            ArrayList sessionMeasurementList = new ArrayList<Map<String, Object>>();
            SQLiteDatabase sqlDB = null;
            try {
                sqlDB = DatabaseHelper.instance().getReadableDatabase();
                String query = "select  * from " + Table.SessionMeasurements.TABLE_NAME + " where session_id = " + sessionId;
                Log.d("query is ", query);
                Cursor cursor = sqlDB.rawQuery(query, null);
                LinkedHashMap<String, Object> row;
                while (cursor.moveToNext()) {
                    row = new LinkedHashMap<String, Object>();
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        row.put(cursor.getColumnName(i), cursor.getString(i) != null ? cursor.getString(i) : "");
                    }
                    sessionMeasurementList.add(row);
                }
                cursor.close();
            } catch (Exception e) {
                Log.d(this.getClass().getName(), e.toString());
            } finally {
            }
            Log.d("sessionMeasurementList", new Gson().toJson(sessionMeasurementList));
            return sessionMeasurementList;
        }

        private JSONArray getServerSessionMeasurementsBySessionId(String sessionId, JSONArray objectSet) {
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < objectSet.length(); i++) {
                try {
                    JSONObject serverObject = objectSet.getJSONObject(i);
                    if (serverObject.getString("Session_Id").equalsIgnoreCase(sessionId)) {
                        jsonArray.put(serverObject);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return jsonArray;
        }
    }

    public static class MemberShip {

        private Context context;
        private String lastSyncTime;
        private JSONArray serverSessionsSet;
        private JSONArray serverSessionMeasurementsSet;
        private ArrayList<HashMap<String, Object>> deviceSessionsSet;


        public MemberShip(Context context, String lastSyncTime) {
            this.context = context;
            this.lastSyncTime = lastSyncTime;
            this.serverSessionsSet = getAllServerMemberShips();
            this.deviceSessionsSet = getAllDeviceMemberships();
            this.serverSessionMeasurementsSet = getAllServerSessionMeasurements();
        }


        public MemberShip(Context context) {
            this.context = context;
            this.lastSyncTime = "";
        }

        public MemberShip() {
        }

        public ArrayList<HashMap<String, Object>> getDeviceMemberShipSet() {
            return deviceSessionsSet;
        }

        public void sync() {
            if (serverSessionsSet != null) {
                syncObjectOnlyOnDevice();
                syncObjectOnlyOnServer();
                syncIntersection();
            }
        }

        private void syncIntersection() {
            for (int i = 0; i < deviceSessionsSet.size(); i++) {
                HashMap<String, Object> o = deviceSessionsSet.get(i);
                String deviceObjectModDate = o.get(Table.UPDATED).toString();
                JSONObject serverObject = getServerObjectBySyncId(o.get(Table.SYNC_ID).toString(), serverSessionsSet);
                if (serverObject != null) {
                    try {
                        String serverObjectModDate = serverObject.getString("ModifiedDate").replace("Z", "");
                        if (Utils.isDateAfterLastSyncTime(lastSyncTime, serverObjectModDate)
                                && !Utils.isDateAfterLastSyncTime(lastSyncTime, deviceObjectModDate)) {
                            propagateServerObjectToDevice(o, serverObject);
                            Log.d("Membership:syncIntersection():serverObjectModDate",
                                    serverObjectModDate + "deviceObjectModDate" + deviceObjectModDate);
                        } else if (Utils.isDateAfterLastSyncTime(lastSyncTime, deviceObjectModDate)
                                && !Utils.isDateAfterLastSyncTime(lastSyncTime, serverObjectModDate)) {
                            propagateDeviceObjectToServer(o);
                            Log.d("Membership:syncIntersection():serverObjectModDate",
                                    serverObjectModDate + "deviceObjectModDate" + deviceObjectModDate);
                        } else if (Utils.isDateAfterLastSyncTime(lastSyncTime, deviceObjectModDate)
                                && Utils.isDateAfterLastSyncTime(lastSyncTime, serverObjectModDate)) {
                            Log.d("Membership:syncIntersection()", "conflict" + i);
                            Log.d("Membership:syncIntersection():serverObjectModDate",
                                    serverObjectModDate + "deviceObjectModDate" + deviceObjectModDate);
                            if (Utils.isDateAfterLastSyncTime(deviceObjectModDate, serverObjectModDate)) {
                                propagateServerObjectToDevice(o, serverObject);
                            } else {
                                propagateDeviceObjectToServer(o);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (!Utils.isDateAfterLastSyncTime(lastSyncTime, deviceObjectModDate)) {
                        DBOHelper.delete(Table.Sessions.TABLE_NAME,
                                Table.ID, o.get(Table.ID).toString());
                        DBOHelper.delete(Table.SessionMeasurements.TABLE_NAME,
                                Table.SessionMeasurements.SESSION_ID, o.get(Table.SessionMeasurements.SESSION_ID).toString());
                    }
                }
            }
        }

        public void propagateDeviceObjectToServer(HashMap<String, Object> o) {

            HashMap<String, Object> sessionValue = new HashMap<String, Object>();
            sessionValue.put("Workout_Id", DBOHelper.getServerWorkoutId(o.get(Table.Workout.WORKOUT_ID).toString()));
            HashMap<String, Object> sessionStatus = new HashMap<String, Object>();
            HashMap<String, Object> session = new HashMap<String, Object>();
            session.put("Trainer_Id", Utils.getTrainerId(context));
            session.put("Title", o.get(Table.Sessions.TITLE));
            session.put("Venue", o.get(Table.Sessions.VENUE));
            session.put("Start_Date", o.get(Table.Sessions.START_DATE));
            session.put("End_Date", o.get(Table.Sessions.END_DATE));
            session.put("Start_Time", o.get(Table.Sessions.START_TIME));
            session.put("End_Time", o.get(Table.Sessions.END_TIME));
            session.put("Notes", o.get(Table.Sessions.NOTES));
            session.put("SessionType", o.get(Table.Sessions.SESSION_TYPE));
            sessionStatus.put("SessionType", o.get(Table.Sessions.SESSION_TYPE));
            session.put("Session_Id", o.get(Table.Sessions.SESSION_ID));
            sessionStatus.put("Session_Id", o.get(Table.Sessions.SESSION_ID));

            session.put("Duration", "0");
            if (o.get(Table.Sessions.SESSION_TYPE).toString().equalsIgnoreCase("0")) {
                String clientId = DBOHelper.getServerClientId(o.get(Table.Sessions.GROUP_ID).toString());
                session.put("Client_Id", clientId);
                session.put("Group_Id", "0");

                sessionStatus.put("Client_Id", clientId);
                sessionStatus.put("Group_Id", "0");
            } else {
                String groupId = DBOHelper.getServerGroupId(o.get(Table.Sessions.GROUP_ID).toString());
                session.put("Client_Id", "0");
                session.put("Group_Id", groupId);

                sessionStatus.put("Client_Id", groupId);
                sessionStatus.put("Group_Id", "0");
            }
            session.put("Package_Id", o.get(Table.Sessions.PACKAGE_ID));
            session.put("syncID", o.get(Table.SYNC_ID));
            sessionValue.put("SessionValue", session);

            WebService w = new WebService();
            String response = w.webInvoke(scheduleManagementService, "UpdateSession", sessionValue);
            if (response != null) {
                Log.d("response", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    jsonObject = jsonObject.getJSONObject("UpdateSessionResult");
                    int sessionId = Integer.valueOf(jsonObject.getString("Id"));
                    if (sessionId > 0) {
                        updateSessionStatus(sessionStatus, o.get(Table.Sessions.SESSION_STATUS).toString());
                        deleteSessionMeasurementOnServer(sessionId + "");
                        createSessionMeasurementOnServer(o.get(Table.Sessions.ID).toString(), sessionId + "");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        private void propagateServerObjectToDevice(HashMap<String, Object> o, JSONObject serverObject) {
            try {
                ContentValues contentValues = new ContentValues();
                contentValues.put(Table.Sessions.TITLE, serverObject.getString("Title"));
                contentValues.put(Table.Sessions.START_TIME, serverObject.getString("Start_Time"));
                contentValues.put(Table.Sessions.END_TIME, serverObject.getString("End_Time"));
                contentValues.put(Table.Sessions.START_DATE, serverObject.getString("Start_Date").replace("Z", ""));
                contentValues.put(Table.Sessions.END_DATE, serverObject.getString("End_Date").replace("Z", ""));
                contentValues.put(Table.Sessions.NOTES, serverObject.getString("Notes"));
                if (serverObject.getString("Group_Id").equalsIgnoreCase("0")) {
                    contentValues.put(Table.Sessions.GROUP_ID, DBOHelper.getDeviceClientId(serverObject.getString("Client_Id")));
                    contentValues.put(Table.Sessions.SESSION_TYPE, 0);
                    contentValues.put(Table.Sessions.SESSION_STATUS, getClientSessionStatus(serverObject.getString("Session_Id")));
                } else {
                    contentValues.put(Table.Sessions.GROUP_ID, DBOHelper.getDeviceGroupId(serverObject.getString("Group_Id")));
                    contentValues.put(Table.Sessions.SESSION_TYPE, 1);
                    contentValues.put(Table.Sessions.SESSION_STATUS, getGroupSessionStatus(serverObject.getString("Session_Id")));
                }
                contentValues.put(Table.Sessions.VENUE, serverObject.getString("Venue"));

                contentValues.put(Table.Sessions.PACKAGE_ID, serverObject.getString("Package_Id"));
                contentValues.put(Table.Sessions.SESSION_ID, serverObject.getString("Session_Id"));
                contentValues.put(Table.SYNC_ID, serverObject.getString("syncID"));
                long isUpdated = DBOHelper.update(Table.Sessions.TABLE_NAME, contentValues, o.get(Table.ID).toString());
                Log.d("update Sessions id is", "" + isUpdated);
                if (isUpdated > 0) {
                    JSONArray sessionMeasurements = getServerSessionMeasurementsBySessionId(serverObject.getString("Session_Id"), serverSessionMeasurementsSet);
                    DBOHelper.delete(Table.SessionMeasurements.TABLE_NAME, Table.SessionMeasurements.SESSION_ID, o.get(Table.ID).toString());
                    for (int i = 0; i < sessionMeasurements.length(); i++) {
                        JSONObject measurements = sessionMeasurements.getJSONObject(i);
                        ContentValues mContentValues = new ContentValues();
                        mContentValues.put(Table.SessionMeasurements.SESSION_MEASUREMENT_ID, measurements.getString("SessionMeasurement_Id"));
                        mContentValues.put(Table.SessionMeasurements.MEASUREMENT_ID, measurements.getString("Measurement"));
                        mContentValues.put(Table.SessionMeasurements.EXERCISE_ID, DBOHelper.getDeviceExerciseId(measurements.getString("Exercise_Id")));
                        mContentValues.put(Table.SessionMeasurements.WORKOUT_ID, DBOHelper.getDeviceWorkoutId(measurements.getString("Workout_Id")));
                        mContentValues.put(Table.SessionMeasurements.SESSION_ID, o.get(Table.ID).toString());
                        mContentValues.put(Table.SessionMeasurements.MEASURED_VALUE, measurements.getString("MeasuredValue"));
                        mContentValues.put(Table.SessionMeasurements.TARGET_VALUE, measurements.getString("TargetValue"));
                        mContentValues.put(Table.SessionMeasurements.SET_NO, measurements.getString("SetNumber"));
                        mContentValues.put(Table.SYNC_ID, measurements.getString("syncID"));
                        long sessionMeasurementId = DBOHelper.insert(context, Table.SessionMeasurements.TABLE_NAME, mContentValues);
                        Log.d("updated sessionMeasurement id is", "" + sessionMeasurementId);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void syncObjectOnlyOnServer() {
            for (int i = 0; i < serverSessionsSet.length(); i++) {
                JSONObject session;
                try {
                    session = serverSessionsSet.getJSONObject(i);
                    String createdDate = session.getString("CreatedDate");
                    String createdDateUTC = createdDate.replace("Z", "");
                    Log.d("Session:syncObjectOnlyOnServer:lastSyncTime", lastSyncTime);
                    Log.d("Session:syncObjectOnlyOnServer:o.get(Table.CREATED).toString())", createdDateUTC);
                    Log.d("Session:syncObjectOnlyOnServer:if-Condition", Utils.isDateAfterLastSyncTime(lastSyncTime, createdDateUTC) + "");
                    String syncId = session.getString("syncID");
                    if (Utils.isDateAfterLastSyncTime(lastSyncTime, createdDateUTC) && getDeviceObjectBySyncId(syncId, deviceSessionsSet) == null) {
                        createOnDevice(session);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        public void createOnDevice(JSONObject session) {
            try {
                ContentValues contentValues = new ContentValues();
                contentValues.put(Table.Sessions.TITLE, session.getString("Title"));
                contentValues.put(Table.Sessions.START_TIME, session.getString("Start_Time"));
                contentValues.put(Table.Sessions.END_TIME, session.getString("End_Time"));
                contentValues.put(Table.Sessions.START_DATE, session.getString("Start_Date").replace("Z", ""));
                contentValues.put(Table.Sessions.END_DATE, session.getString("End_Date").replace("Z", ""));
                contentValues.put(Table.Sessions.NOTES, session.getString("Notes"));
                if (session.getString("Group_Id").equalsIgnoreCase("0")) {
                    contentValues.put(Table.Sessions.GROUP_ID, DBOHelper.getDeviceClientId(session.getString("Client_Id")));
                    contentValues.put(Table.Sessions.SESSION_TYPE, 0);
                    contentValues.put(Table.Sessions.SESSION_STATUS, getClientSessionStatus(session.getString("Session_Id")));
                } else {
                    contentValues.put(Table.Sessions.GROUP_ID, DBOHelper.getDeviceGroupId(session.getString("Group_Id")));
                    contentValues.put(Table.Sessions.SESSION_TYPE, 1);
                    contentValues.put(Table.Sessions.SESSION_STATUS, getGroupSessionStatus(session.getString("Session_Id")));
                }
                contentValues.put(Table.Sessions.VENUE, session.getString("Venue"));
                contentValues.put(Table.Sessions.PACKAGE_ID, session.getString("Package_Id"));
                contentValues.put(Table.Sessions.SESSION_ID, session.getString("Session_Id"));
                contentValues.put(Table.SYNC_ID, session.getString("syncID"));
                long sessionId = DBOHelper.insert(context, Table.Sessions.TABLE_NAME, contentValues);
                Log.d("inserted Sessions id is", "" + sessionId);
                if (sessionId > 0) {
                    JSONArray sessionMeasurements = getServerSessionMeasurementsBySessionId(session.getString("Session_Id"), serverSessionMeasurementsSet);
                    for (int i = 0; i < sessionMeasurements.length(); i++) {
                        JSONObject measurements = sessionMeasurements.getJSONObject(i);
                        ContentValues mContentValues = new ContentValues();
                        mContentValues.put(Table.SessionMeasurements.SESSION_MEASUREMENT_ID, measurements.getString("SessionMeasurement_Id"));
                        mContentValues.put(Table.SessionMeasurements.MEASUREMENT_ID, measurements.getString("Measurement"));
                        mContentValues.put(Table.SessionMeasurements.EXERCISE_ID, DBOHelper.getDeviceExerciseId(measurements.getString("Exercise_Id")));
                        mContentValues.put(Table.SessionMeasurements.WORKOUT_ID, DBOHelper.getDeviceWorkoutId(measurements.getString("Workout_Id")));
                        mContentValues.put(Table.SessionMeasurements.SESSION_ID, sessionId);
                        mContentValues.put(Table.SessionMeasurements.MEASURED_VALUE, measurements.getString("MeasuredValue"));
                        mContentValues.put(Table.SessionMeasurements.TARGET_VALUE, measurements.getString("TargetValue"));
                        mContentValues.put(Table.SessionMeasurements.SET_NO, measurements.getString("SetNumber"));
                        mContentValues.put(Table.SYNC_ID, measurements.getString("syncID"));
                        long sessionMeasurementId = DBOHelper.insert(context, Table.SessionMeasurements.TABLE_NAME, mContentValues);
                        Log.d("inserted sessionMeasurement id is", "" + sessionMeasurementId);
                    }


                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void syncObjectOnlyOnDevice() {
            for (HashMap<String, Object> o : deviceSessionsSet) {
                Log.d("Membership:syncObjectOnlyOnDevice:lastSyncTime", lastSyncTime);
                Log.d("Membership:syncObjectOnlyOnDevice:o.get(Table.CREATED).toString())", o.get(Table.CREATED).toString());
                Log.d("Membership:syncObjectOnlyOnDevice:if-Condition", Utils.isDateAfterLastSyncTime(lastSyncTime, o.get(Table.CREATED).toString()) + "");
                String syncId = o.get(Table.SYNC_ID).toString();
                if (Utils.isDateAfterLastSyncTime(lastSyncTime, o.get(Table.CREATED).toString())
                        && getServerObjectBySyncId(syncId, serverSessionsSet) == null) {
                    createOnServer(o);
                }
            }
        }

        public void createOnServer(HashMap<String, Object> o) {
            HashMap<String, Object> sessionValue = new HashMap<String, Object>();
            sessionValue.put("Trainer_Id", Utils.getTrainerId(context));
            if (o.containsKey(Table.Workout.WORKOUT_ID) && o.get(Table.Workout.WORKOUT_ID) != null)
                sessionValue.put("Workout_Id", DBOHelper.getServerWorkoutId(o.get(Table.Workout.WORKOUT_ID).toString()));
            else
                sessionValue.put("Workout_Id", "0");
            HashMap<String, Object> session = new HashMap<String, Object>();
            session.put("Title", o.get(Table.Sessions.TITLE));
            session.put("Venue", o.get(Table.Sessions.VENUE));
            session.put("Start_Date", o.get(Table.Sessions.START_DATE));
            session.put("End_Date", o.get(Table.Sessions.END_DATE));
            session.put("Start_Time", o.get(Table.Sessions.START_TIME));
            session.put("End_Time", o.get(Table.Sessions.END_TIME));
            session.put("Notes", o.get(Table.Sessions.NOTES));
            session.put("SessionType", o.get(Table.Sessions.SESSION_TYPE));
            session.put("Trainer_Id", Utils.getTrainerId(context));
            session.put("Duration", "0");
            if (o.get(Table.Sessions.SESSION_TYPE).toString().equalsIgnoreCase("0")) {
                session.put("Client_Id", DBOHelper.getServerClientId(o.get(Table.Sessions.GROUP_ID).toString()));
                session.put("Group_Id", "0");
            } else {
                session.put("Client_Id", "0");
                session.put("Group_Id", DBOHelper.getServerGroupId(o.get(Table.Sessions.GROUP_ID).toString()));
            }
            session.put("Package_Id", o.get(Table.Sessions.PACKAGE_ID));
            session.put("syncID", o.get(Table.SYNC_ID));
            sessionValue.put("SessionValue", session);

            WebService w = new WebService();
            String response = w.webInvoke(scheduleManagementService, "AddSession", sessionValue);
            if (response != null) {
                Log.d("response", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    jsonObject = jsonObject.getJSONObject("AddSessionResult");
                    int sessionId = jsonObject.getInt("Result");
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(Table.Sessions.SESSION_ID, sessionId);
                    DBOHelper.update(Table.Sessions.TABLE_NAME, contentValues, o.get(Table.Sessions.ID).toString());
                    createSessionMeasurementOnServer(o.get(Table.Sessions.ID).toString(), sessionId + "");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }

        private void deleteSessionMeasurementOnServer(String sessionId) {
            JSONArray sessionMeasurements = getServerSessionMeasurementsBySessionId(sessionId, serverSessionMeasurementsSet);
            Log.d("delete:sessionMeasurements", sessionMeasurements.toString());
            for (int i = 0; i < sessionMeasurements.length(); i++) {
                try {
                    JSONObject aSessionMeasurement = sessionMeasurements.getJSONObject(i);
                    HashMap<String, String> mSessionMeasurement = new HashMap<String, String>();
                    mSessionMeasurement.put("SessionMeasurement_Id", aSessionMeasurement.get("SessionMeasurement_Id").toString());
                    WebService w = new WebService();
                    String response = w.webGet(scheduleManagementService, "DeleteSessionMeasurement", mSessionMeasurement);
                    if (response != null) {
                        Log.d("response", response);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        private void createSessionMeasurementOnServer(String deviceSessionId, String serverSessionId) {
            ArrayList<HashMap<String, Object>> sessionMeasurementList = getAllDeviceSessionMeasurementsBySessionId(deviceSessionId);
            for (HashMap<String, Object> mSessionMeasurement : sessionMeasurementList) {
                HashMap<String, Object> sessionMeasurementValue = new HashMap<String, Object>();
                HashMap<String, Object> sessionMeasurement = new HashMap<String, Object>();
                sessionMeasurement.put("SetNumber", mSessionMeasurement.get(Table.SessionMeasurements.SET_NO));
                sessionMeasurement.put("Measurement", mSessionMeasurement.get(Table.SessionMeasurements.MEASUREMENT_ID));
                sessionMeasurement.put("Session_Id", serverSessionId);
                sessionMeasurement.put("Workout_Id", DBOHelper.getServerWorkoutId(mSessionMeasurement.get(Table.SessionMeasurements.WORKOUT_ID).toString()));
                sessionMeasurement.put("Exercise_Id", DBOHelper.getServerExerciseId(mSessionMeasurement.get(Table.SessionMeasurements.EXERCISE_ID).toString()));
                sessionMeasurement.put("sortOrder", "0");
                sessionMeasurement.put("MeasuredValue", mSessionMeasurement.get(Table.SessionMeasurements.MEASURED_VALUE).toString().equalsIgnoreCase("") ? "0" :
                        mSessionMeasurement.get(Table.SessionMeasurements.MEASURED_VALUE).toString());
                sessionMeasurement.put("TargetValue", mSessionMeasurement.get(Table.SessionMeasurements.TARGET_VALUE).toString().equalsIgnoreCase("") ? "0" :
                        mSessionMeasurement.get(Table.SessionMeasurements.TARGET_VALUE));

                sessionMeasurement.put("syncID", mSessionMeasurement.get(Table.SYNC_ID));
                sessionMeasurementValue.put("SessionMeasurement", sessionMeasurement);

                WebService w = new WebService();
                //  String jsonTest=""
                String response = w.webInvoke(scheduleManagementService, "AddSessionMeasurement", sessionMeasurementValue);
                if (response != null) {
                    Log.d("response", response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        jsonObject = jsonObject.getJSONObject("AddSessionMeasurementResult");
                        int sessionMeasurementId = jsonObject.getInt("Id");
                        if (sessionMeasurementId > 0) {
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(Table.SessionMeasurements.SESSION_MEASUREMENT_ID, sessionMeasurementId);
                            DBOHelper.update(Table.SessionMeasurements.TABLE_NAME, contentValues, mSessionMeasurement.get(Table.SessionMeasurements.ID).toString());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private void updateSessionStatus(HashMap<String, Object> sessionStatus, String status) {
            HashMap<String, Object> sessionValue = new HashMap<String, Object>();
            sessionValue.put("SessionValue", sessionStatus);
            sessionValue.put("Session_Status", String.valueOf(Integer.valueOf(status) + 1));
            WebService w = new WebService();
            String response = w.webInvoke(scheduleManagementService, "UpdateSessionStatus", sessionValue);
            if (response != null) {
                Log.d("response", response);
            }
        }

        private int getClientSessionStatus(String sessionId) {
            HashMap<String, String> sessionStatus = new HashMap<String, String>();
            sessionStatus.put("Session_Id", sessionId);
            WebService w = new WebService();
            String response = w.webGet(scheduleManagementService, "GetSessionStatusForClient", sessionStatus);
            if (response != null) {
                Log.d("response", response);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    int status = jsonObject.getInt("GetSessionStatusForClientResult");
                    if (status > 0) {
                        return status - 1;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            return -1;
        }

        private int getGroupSessionStatus(String sessionId) {
            HashMap<String, String> sessionStatus = new HashMap<String, String>();
            sessionStatus.put("Session_Id", sessionId);
            WebService w = new WebService();
            String response = w.webGet(scheduleManagementService, "GetSessionStatusForGroup", sessionStatus);
            if (response != null) {
                Log.d("response", response);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    int status = jsonObject.getInt("GetSessionStatusForGroupResult");
                    if (status > 0) {
                        return status - 1;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return -1;
        }

        private JSONArray getAllServerMemberShips() {
            Map<String, String> param = new HashMap<String, String>();
            param.put("Trainer_Id", Utils.getTrainerId(context));
            WebService w = new WebService();
            String response = w.webGet(trainerWebServiceURL,
                    "GetPaymentDetailsByTrainer", param);
            JSONObject obj = null;
            try {
                if (response != null) {
                    Log.d("response is", response);
                    obj = new JSONObject(response);
                    return obj.getJSONArray("GetPaymentDetailsByTrainerResult");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        private JSONArray getAllServerSessionMeasurements() {
            Map<String, String> param = new HashMap<String, String>();
            param.put("Trainer_Id", Utils.getTrainerId(context));
            WebService w = new WebService();
            String response = w.webGet(scheduleManagementService,
                    "GetSessionMeasurementByTrainerId", param);
            JSONObject obj = null;
            try {
                if (response != null) {
                    Log.d("response is", response);
                    obj = new JSONObject(response);
                    return obj.getJSONArray("GetSessionMeasurementByTrainerIdResult");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }


        private ArrayList getAllDeviceMemberships() {
            ArrayList<HashMap<String, Object>> mapArrayList = new ArrayList<HashMap<String, Object>>();
            SQLiteDatabase sqlDB = null;
            try {
                sqlDB = DatabaseHelper.instance().getReadableDatabase();

                String query = "select  * "
                        + " from " +
                        Table.Membership.TABLE_NAME;

                Log.d("query is ", query);
                assert sqlDB != null;
                Cursor cursor = sqlDB
                        .rawQuery(query
                                , null);

                LinkedHashMap<String, Object> row;

                while (cursor.moveToNext()) {
                    row = new LinkedHashMap<String, Object>();
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        row.put(cursor.getColumnName(i), cursor.getString(i) != null ? cursor.getString(i) : "");
                    }
                    mapArrayList.add(row);
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
            }
            return mapArrayList;
        }

        private ArrayList<HashMap<String, Object>> getAllDeviceSession() {
            ArrayList sessionsList = new ArrayList<Map<String, Object>>();
            SQLiteDatabase sqlDB = null;
            try {
                sqlDB = DatabaseHelper.instance().getReadableDatabase();
                String query = "SELECT sessions.*,session_measurements.workout_id " +
                        "FROM sessions LEFT JOIN session_measurements " +
                        "ON session_measurements.session_id=sessions._id group by sessions._id;";
                //"select  * from " + Table.Sessions.TABLE_NAME;
                Log.d("query is ", query);
                Cursor cursor = sqlDB.rawQuery(query, null);
                LinkedHashMap<String, Object> row;
                while (cursor.moveToNext()) {
                    row = new LinkedHashMap<String, Object>();
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        row.put(cursor.getColumnName(i), cursor.getString(i) != null ? cursor.getString(i) : "");
                    }
                    sessionsList.add(row);
                }
                cursor.close();
            } catch (Exception e) {
                Log.d(this.getClass().getName(), e.toString());
            } finally {
            }
            Log.d("sessionList", new Gson().toJson(sessionsList));
            return sessionsList;
        }

        private ArrayList<HashMap<String, Object>> getAllDeviceSessionMeasurementsBySessionId(String sessionId) {
            ArrayList sessionMeasurementList = new ArrayList<Map<String, Object>>();
            SQLiteDatabase sqlDB = null;
            try {
                sqlDB = DatabaseHelper.instance().getReadableDatabase();
                String query = "select  * from " + Table.SessionMeasurements.TABLE_NAME + " where session_id = " + sessionId;
                Log.d("query is ", query);
                Cursor cursor = sqlDB.rawQuery(query, null);
                LinkedHashMap<String, Object> row;
                while (cursor.moveToNext()) {
                    row = new LinkedHashMap<String, Object>();
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        row.put(cursor.getColumnName(i), cursor.getString(i) != null ? cursor.getString(i) : "");
                    }
                    sessionMeasurementList.add(row);
                }
                cursor.close();
            } catch (Exception e) {
                Log.d(this.getClass().getName(), e.toString());
            } finally {
            }
            Log.d("sessionMeasurementList", new Gson().toJson(sessionMeasurementList));
            return sessionMeasurementList;
        }

        private JSONArray getServerSessionMeasurementsBySessionId(String sessionId, JSONArray objectSet) {
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < objectSet.length(); i++) {
                try {
                    JSONObject serverObject = objectSet.getJSONObject(i);
                    if (serverObject.getString("Session_Id").equalsIgnoreCase(sessionId)) {
                        jsonArray.put(serverObject);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return jsonArray;
        }
    }

    public static JSONObject getServerObjectBySyncId(String syncId, JSONArray objectSet) {
        for (int i = 0; i < objectSet.length(); i++) {
            try {
                JSONObject serverObject = objectSet.getJSONObject(i);
                if (serverObject.getString("syncID").equalsIgnoreCase(syncId))
                    return serverObject;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static HashMap<String, Object> getDeviceObjectBySyncId(String syncId, ArrayList<HashMap<String, Object>> objectSet) {
        for (HashMap<String, Object> serverObject : objectSet) {
            if (serverObject.get(Table.SYNC_ID).toString().equalsIgnoreCase(syncId))
                return serverObject;
        }
        return null;
    }

    public static HashMap<String, Object> getDeviceObjectById(String _id, ArrayList<HashMap<String, Object>> objectSet) {
        for (HashMap<String, Object> object : objectSet) {
            if (object.get(Table.ID).toString().equalsIgnoreCase(_id))
                return object;
        }
        return null;
    }
}
