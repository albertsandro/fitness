package com.fitforbusiness.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "PTCentralPro.db";
    private static final int DATABASE_VERSION = 17;
    private static DatabaseHelper _instance;

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static void initialize(Context context) {
        _instance = new DatabaseHelper(context);
    }

    public static synchronized DatabaseHelper instance() {
        return _instance;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        try {
            database.execSQL(Table.TrainerProfileDetails.CREATE_TABLE);
            database.execSQL(Table.TrainerProfileAccreditation.CREATE_TABLE);
            database.execSQL(Table.AssessmentForms.CREATE_TABLE);
            database.execSQL(Table.AssessmentField.CREATE_TABLE);
            database.execSQL(Table.Client.CREATE_TABLE);
            database.execSQL(Table.Group.CREATE_TABLE);
            database.execSQL(Table.GroupClients.CREATE_TABLE);
            database.execSQL(Table.Measurement.CREATE_TABLE);
            database.execSQL(Table.Exercise.CREATE_TABLE);
            database.execSQL(Table.ExerciseMeasurements.CREATE_TABLE);
            database.execSQL(Table.Workout.CREATE_TABLE);
            database.execSQL(Table.WorkoutExercises.CREATE_TABLE);
            database.execSQL(Table.Sessions.CREATE_TABLE);
            database.execSQL(Table.SessionMeasurements.CREATE_TABLE);
            DBOHelper.createDefaultsMeasurement(database);
            DBOHelper.createParQForm(database);
            database.execSQL(Table.CompletedAssessmentForm.CREATE_TABLE);
            database.execSQL(Table.CompletedAssessmentFormField.CREATE_TABLE);
            database.execSQL(Table.Membership.CREATE_TABLE);
            database.execSQL(Table.StripePayment.CREATE_TABLE);
            database.execSQL(Table.GroupMembership.CREATE_TABLE);
            database.execSQL(Table.SyncLog.CREATE_TABLE);

            database.execSQL(String.format(Table.UPDATE_TRIGGER,
                    Table.TrainerProfileDetails.TABLE_NAME,
                    Table.TrainerProfileDetails.TABLE_NAME,
                    Table.TrainerProfileDetails.TABLE_NAME));

            database.execSQL(String.format(Table.UPDATE_TRIGGER,
                    Table.TrainerProfileAccreditation.TABLE_NAME,
                    Table.TrainerProfileAccreditation.TABLE_NAME,
                    Table.TrainerProfileAccreditation.TABLE_NAME));

            database.execSQL(String.format(Table.UPDATE_TRIGGER,
                    Table.AssessmentForms.TABLE_NAME,
                    Table.AssessmentForms.TABLE_NAME,
                    Table.AssessmentForms.TABLE_NAME));

            database.execSQL(String.format(Table.UPDATE_TRIGGER,
                    Table.AssessmentField.TABLE_NAME,
                    Table.AssessmentField.TABLE_NAME,
                    Table.AssessmentField.TABLE_NAME));

            database.execSQL(String.format(Table.UPDATE_TRIGGER,
                    Table.Client.TABLE_NAME, Table.Client.TABLE_NAME,
                    Table.Client.TABLE_NAME));


            database.execSQL(String.format(Table.UPDATE_TRIGGER,
                    Table.Group.TABLE_NAME, Table.Group.TABLE_NAME,
                    Table.Group.TABLE_NAME));

            database.execSQL(String.format(Table.UPDATE_TRIGGER,
                    Table.GroupClients.TABLE_NAME,
                    Table.GroupClients.TABLE_NAME,
                    Table.GroupClients.TABLE_NAME));

            database.execSQL(String.format(Table.UPDATE_TRIGGER,
                    Table.Measurement.TABLE_NAME,
                    Table.Measurement.TABLE_NAME,
                    Table.Measurement.TABLE_NAME));


            database.execSQL(String.format(Table.UPDATE_TRIGGER,
                    Table.Exercise.TABLE_NAME, Table.Exercise.TABLE_NAME,
                    Table.Exercise.TABLE_NAME));


            database.execSQL(String.format(Table.UPDATE_TRIGGER,
                    Table.Workout.TABLE_NAME, Table.Workout.TABLE_NAME,
                    Table.Workout.TABLE_NAME));

            database.execSQL(String.format(Table.UPDATE_TRIGGER,
                    Table.WorkoutExercises.TABLE_NAME,
                    Table.WorkoutExercises.TABLE_NAME,
                    Table.WorkoutExercises.TABLE_NAME));

            database.execSQL(String.format(Table.UPDATE_TRIGGER,
                    Table.Sessions.TABLE_NAME,
                    Table.Sessions.TABLE_NAME,
                    Table.Sessions.TABLE_NAME));

            database.execSQL(String.format(Table.UPDATE_TRIGGER,
                    Table.SessionMeasurements.TABLE_NAME,
                    Table.SessionMeasurements.TABLE_NAME,
                    Table.SessionMeasurements.TABLE_NAME));

            database.execSQL(String.format(Table.UPDATE_TRIGGER,
                    Table.CompletedAssessmentForm.TABLE_NAME,
                    Table.CompletedAssessmentForm.TABLE_NAME,
                    Table.CompletedAssessmentForm.TABLE_NAME));

            database.execSQL(String.format(Table.UPDATE_TRIGGER,
                    Table.CompletedAssessmentFormField.TABLE_NAME,
                    Table.CompletedAssessmentFormField.TABLE_NAME,
                    Table.CompletedAssessmentFormField.TABLE_NAME));

            database.execSQL(String.format(Table.UPDATE_TRIGGER,
                    Table.Membership.TABLE_NAME,
                    Table.Membership.TABLE_NAME,
                    Table.Membership.TABLE_NAME));

            database.execSQL(String.format(Table.UPDATE_TRIGGER,
                    Table.GroupMembership.TABLE_NAME,
                    Table.GroupMembership.TABLE_NAME,
                    Table.GroupMembership.TABLE_NAME));

            database.execSQL(String.format(Table.UPDATE_TRIGGER,
                    Table.SyncLog.TABLE_NAME,
                    Table.SyncLog.TABLE_NAME,
                    Table.SyncLog.TABLE_NAME));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion,
                          int newVersion) {
        Log.d("db", "sql upgrade function");
        if(oldVersion < newVersion)
        {
            Log.d("db", "sql upgrade create");
            try {
                database.execSQL(Table.StripePayment.CREATE_TABLE);
            }catch(Exception e){
                Log.d("db", "Payment already exists!");
            }
            try {
                String addNativeId = "ALTER TABLE " + Table.Sessions.TABLE_NAME +
                        " ADD COLUMN " + Table.Sessions.NATIVE_ID + " integer default 0;";

                String addIsNative = "ALTER TABLE " + Table.Sessions.TABLE_NAME +
                        " ADD COLUMN " + Table.Sessions.IS_NATIVE + " integer default 0;";

                database.execSQL(addIsNative);
                database.execSQL(addNativeId);
            }catch(Exception e){
                Log.d("db", "Session already exists!");
            }

            try {
                String addNativeId = "ALTER TABLE " + Table.Sessions.TABLE_NAME +
                        " ADD COLUMN " + Table.Sessions.RECURRENCE_RULE + " text;";

                database.execSQL(addNativeId);
            }catch(Exception e){
                Log.d("db", "Session already exists!");
            }

        }

    }

}
