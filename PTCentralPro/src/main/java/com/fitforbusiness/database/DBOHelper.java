package com.fitforbusiness.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.fitforbusiness.Parse.Models.Accreditation;
import com.fitforbusiness.Parse.Models.AssessmentField;
import com.fitforbusiness.Parse.Models.AssessmentForm;
import com.fitforbusiness.Parse.Models.AssessmentFormField;
import com.fitforbusiness.Parse.Models.AssessmentFormType;
import com.fitforbusiness.Parse.Models.Client;
import com.fitforbusiness.Parse.Models.Exercise;
import com.fitforbusiness.Parse.Models.Group;
import com.fitforbusiness.Parse.Models.Session;
import com.fitforbusiness.Parse.Models.Status;
import com.fitforbusiness.Parse.Models.Trainer;
import com.fitforbusiness.Parse.Models.Workout;
import com.fitforbusiness.framework.Utils;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Charge;
import com.stripe.model.Customer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Sanjeet on 6/2/14.
 */
public class DBOHelper {

    public static long insert(Context context, String tableName, ContentValues contentValues) {
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getWritableDatabase();
            
            long row_id = sqlDB.insert(tableName, null, contentValues);
            if (row_id > 0) {
                return row_id;
            } else {
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            
            //  

        } finally {
            
            //  
        }
        
        return -1;
    }

    public static long update(Context context, String tableName, ContentValues contentValues, String update_id) {
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getWritableDatabase();
            long row_id = sqlDB.update(tableName, contentValues,
                    Table.TrainerProfileDetails.TRAINER_ID + "=" + update_id, null);
            if (row_id > 0) {
                return row_id;
            } else {
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            
            

        } finally {
            
            
        }
        
        return -1;
    }

    public static long update(String tableName, ContentValues contentValues, String update_id) {
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getWritableDatabase();
            long row_id = sqlDB.update(tableName, contentValues,
                    Table.ID + "=" + update_id, null);
            if (row_id > 0) {
                return row_id;
            } else {
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (sqlDB != null) {
                
            }

        } finally {
            if (sqlDB != null) {
                
            }
        }
        if (sqlDB != null) {
            
        }
        return -1;
    }
    public static long updateWorkOutExercise(String tableName, ContentValues contentValues, String update_id) {
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getWritableDatabase();
            long row_id = sqlDB.update(tableName, contentValues,
                    Table.WorkoutExercises.WORKOUT_ID + "=" + update_id, null);
            if (row_id > 0) {
                return row_id;
            } else {
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (sqlDB != null) {
                
            }

        } finally {
            if (sqlDB != null) {
                
            }
        }
        if (sqlDB != null) {
            
        }
        return -1;
    }
    public static long update(String tableName, ContentValues contentValues, String update_column, String update_id) {
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getWritableDatabase();
            long row_id = sqlDB.update(tableName, contentValues,
                    update_column + "=\'" + update_id + "\'", null);
            if (row_id > 0) {
                return row_id;
            } else {
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (sqlDB != null) {
                
            }

        } finally {
            if (sqlDB != null) {
                
            }
        }
        if (sqlDB != null) {
            
        }
        return -1;
    }

    public static long update(String tableName, ContentValues contentValues, String update_column, long update_id) {
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getWritableDatabase();
            long row_id = sqlDB.update(tableName, contentValues,
                    update_column + "=" + update_id + "", null);
            if (row_id > 0) {
                return row_id;
            } else {
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (sqlDB != null) {
                
            }

        } finally {
            if (sqlDB != null) {
                
            }
        }
        if (sqlDB != null) {
            
        }
        return -1;
    }

    public static long delete(Context context, String tableName, String delete_id) {
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getWritableDatabase();
            long row_id = sqlDB.delete(tableName,
                    Table.TrainerProfileDetails.TRAINER_ID + " = " + delete_id, null);
            if (row_id > 0) {
                return row_id;
            } else {
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
        }
        return -1;
    }

    public static long delete(String tableName, String columnIdName, String ColumnIdValue) {
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getWritableDatabase();
            long row_id = sqlDB.delete(tableName,
                    columnIdName + " = " + ColumnIdValue, null);
            if (row_id > 0) {
                return row_id;
            } else {
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return -1;
    }

    public static long updateAccreditation(ContentValues contentValues, String update_id) {
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getWritableDatabase();
            long row_id = sqlDB.update(Table.TrainerProfileAccreditation.TABLE_NAME, contentValues,
                    Table.TrainerProfileAccreditation.ID + "=" + update_id, null);
            if (row_id > 0) {
                return row_id;
            } else {
                return -1;
            }

        } catch (Exception e) {
            e.printStackTrace();
            

        } finally {
            
        }
        
        return -1;
    }

    public static long delete(Context context, String tableName) {
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getWritableDatabase();
            long row_id = sqlDB.delete(tableName,
                    null, null);
            if (row_id > 0) {
                return row_id;
            } else {
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return -1;
    }

    public static long createDefaultFormFields(String form_id) {

        String[] items = {"Height", "Weight", "BP (Systolic)",
                "BP (Diastolic)", "Resting HR", "Passive HR",
                "Neck", "Shoulders", "Chest", "Waist", "Hips",
                "Thighs", "Calves", "Biceps", "Forearms"};

        SQLiteDatabase sqlDB = null;

        try {
            sqlDB = DatabaseHelper.instance().getWritableDatabase();
            for (int i = 0; i < items.length; i++) {

                ContentValues contentValues = new ContentValues();
                contentValues.put(Table.AssessmentField.TITLE, items[i]);
                contentValues.put(Table.AssessmentField.TYPE, "0");
                contentValues.put(Table.AssessmentField.SORT_ORDER, String.valueOf(i));
                contentValues.put(Table.AssessmentField.FORM_ID, form_id);
                
                long row = sqlDB.insert(Table.AssessmentField.TABLE_NAME, null, contentValues);
                Log.d("Inserted field is", row + "");

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }


        return -1;
    }

    public static long createParQForm(SQLiteDatabase database) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Table.AssessmentForms.FORM_NAME, "ParQ Assessment Form");
        contentValues.put(Table.AssessmentForms.FORM_TYPE, 1);
        assert database != null;
        long form_id = database.insert(Table.AssessmentForms.TABLE_NAME, null, contentValues);
        String[] items = {"Has your doctor ever said that you have a heart condition and that you should only do physical activity recommended by a doctor?",
                "Do you feel pain in your chest when you do physical activity?",
                "In the past month, have you had any chest pain when you were not doing physical activity?",
                "Do you lose your balance because of dizziness or do you ever lose your consciousness?",
                "Do you have a bone or joint problem that could be made worse by a change in your physical activity?",
                "Is your doctor currently prescribing drugs for your blood pressure or heart condition?",
                "Do you know any other reason why you should not do physical activity?"};

        if (form_id > 0) {

            try {

                for (int i = 0; i < items.length; i++) {
                    ContentValues mContentForm = new ContentValues();
                    mContentForm.put(Table.AssessmentField.TITLE, items[i]);
                    mContentForm.put(Table.AssessmentField.TYPE, "0");
                    mContentForm.put(Table.AssessmentField.SORT_ORDER, String.valueOf(i));
                    mContentForm.put(Table.AssessmentField.FORM_ID, form_id);
                    assert database != null;
                    long row = database.insert(Table.AssessmentField.TABLE_NAME, null, mContentForm);
                    Log.d("Inserted field ParQ is", row + "");
                }
            } catch (Exception e) {

            } finally {
            }
        }

        return form_id;
    }

    public static long createDefaultFormField(String form_id, String sort_order) {


        SQLiteDatabase sqlDB = null;

        try {
            sqlDB = DatabaseHelper.instance().getWritableDatabase();


            ContentValues contentValues = new ContentValues();
            contentValues.put(Table.AssessmentField.TITLE, "New Field");
            contentValues.put(Table.AssessmentField.TYPE, "0");
            contentValues.put(Table.AssessmentField.SORT_ORDER, sort_order);
            contentValues.put(Table.AssessmentField.FORM_ID, form_id);
            
            long row = sqlDB.insert(Table.AssessmentField.TABLE_NAME, null, contentValues);
            Log.d("Inserted field is", row + "");


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }


        return 1;
    }


    public static long deleteParseFormField(String field_id) {

        return 1;
    }
    public static long deleteFormField(String field_id) {


        SQLiteDatabase sqlDB = null;

        try {
            sqlDB = DatabaseHelper.instance().getWritableDatabase();


            
            long row = sqlDB.delete(Table.AssessmentField.TABLE_NAME,
                    Table.AssessmentField.ID + " = " + field_id, null);
            Log.d("Inserted field is", row + "");


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }


        return 1;
    }

    public static long deleteFieldsByFormId(String form_id) {


        SQLiteDatabase sqlDB = null;

        try {
            sqlDB = DatabaseHelper.instance().getWritableDatabase();


            
            long row = sqlDB.delete(Table.AssessmentField.TABLE_NAME,
                    Table.AssessmentField.FORM_ID + " = " + form_id, null);
            Log.d("Inserted field is", row + "");


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }


        return 1;
    }

    public static long deleteForm(String _id) {


        SQLiteDatabase sqlDB = null;

        try {
            sqlDB = DatabaseHelper.instance().getWritableDatabase();


            
            long row = sqlDB.delete(Table.AssessmentField.TABLE_NAME,
                    Table.AssessmentForms.ID + " = " + _id, null);
            Log.d("Inserted field is", row + "");


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }


        return -1;
    }

    public static long sortFormField(String id, String sort_order) {


        ContentValues contentValues = new ContentValues();
        contentValues.put(Table.AssessmentField.SORT_ORDER, sort_order);
        SQLiteDatabase sqlDB = null;

        try {
            sqlDB = DatabaseHelper.instance().getWritableDatabase();


            
            long row = sqlDB.update(Table.AssessmentField.TABLE_NAME, contentValues,
                    Table.AssessmentField.ID + " = " + id, null);
            Log.d("Inserted field is", row + "");


        } catch (Exception e) {
            e.printStackTrace();
            
            
        } finally {
            
            
        }


        return 1;
    }

    public static long updateFormFieldTitle(String id, String title) {


        ContentValues contentValues = new ContentValues();
        contentValues.put(Table.AssessmentField.TITLE, title);
        SQLiteDatabase sqlDB = null;

        try {
            sqlDB = DatabaseHelper.instance().getWritableDatabase();


            
            long row = sqlDB.update(Table.AssessmentField.TABLE_NAME, contentValues,
                    Table.AssessmentField.ID + " = " + id, null);
            Log.d("Inserted field is", row + "");


        } catch (Exception e) {
            e.printStackTrace();
            
            
        } finally {
            
            
        }


        return 1;
    }

    public static long insertAssessmentForm(ContentValues contentValues) {


        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getWritableDatabase();
            
            long row_id = sqlDB.insert(Table.AssessmentForms.TABLE_NAME, null, contentValues);
            if (row_id > 0) {
                return row_id;
            } else {
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            
            

        } finally {
            
            
        }
        
        return -1;
    }


    public static long updateAssessmentForm(ContentValues contentValues, String id) {


        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getWritableDatabase();
            
            long row_id = sqlDB.update(Table.AssessmentForms.TABLE_NAME, contentValues,
                    Table.AssessmentForms.ID + " = " + id, null);
            if (row_id > 0) {
                return row_id;
            } else {
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            
            

        } finally {
            
            
        }
        
        return -1;
    }

    public static long createDefaultsMeasurement(SQLiteDatabase sqLiteDatabase) {
        String[] items = {"Distance", "Reps", "Time",
                "Weight"};
        try {

            for (String item : items) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(Table.Measurement.NAME, item);
                assert sqLiteDatabase != null;
                long row = sqLiteDatabase.insert(Table.Measurement.TABLE_NAME, null, contentValues);
                Log.d("Inserted field is", row + "");
            }
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
        }
        return -1;
    }

    public static String topWorkout() {

        String topWorkoutTitle = "";
        String query = "select * from workout where _id=" +
                "(select workout_id from " +
                "(select max(num_work),workout_id from " +
                "( select count(workout_id) as num_work , workout_id from session_measurements " +
                //  "(select * from session_measurements group by session_id) " +
                "group by workout_id)))";

        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            
            Cursor cursor = sqlDB.rawQuery(query, null);
            if (cursor.moveToFirst())
                topWorkoutTitle = cursor.getString(cursor.getColumnIndex(Table.Workout.NAME));
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            
            
        } finally {
            
            
        }
        

        return topWorkoutTitle;
    }
    private static int totalWorkoutsCount=0;
    public static int totalParseWorkouts(){
        ArrayList<HashMap<String, Object>> mapWorkoutArray = new ArrayList<HashMap<String, Object>>();
        ParseQuery parseQuery = new ParseQuery(Workout.class);
        parseQuery.fromLocalDatastore();
//        parseQuery.whereEqualTo("trainer", Trainer.getCurrent());
        parseQuery.findInBackground(new FindCallback<Workout>() {
            @Override
            public void done(List<Workout> list, ParseException e) {
                if (e == null && list != null) {
                    if (list.size() == 0) {
                        ParseQuery parseQuery = new ParseQuery(Workout.class);
//                        parseQuery.whereEqualTo("trainer", Trainer.getCurrent());
                        parseQuery.findInBackground(new FindCallback<Workout>() {
                            @Override
                            public void done(List<Workout> list, ParseException e) {
                                if (e == null && list != null) {
                                    totalWorkoutsCount=list.size();
                                    Workout.pinAllInBackground(list);
                                }
                            }
                        });
                    } else {
                        totalWorkoutsCount=list.size();
                    }
                }
            }
        });
        return totalWorkoutsCount;
    }
    public static int totalWorkouts() {
        int totalWorkoutCount = 0;
        String query = "select count(*) as num_workout from " +
                Table.Workout.TABLE_NAME + " where " + Table.DELETED + " = 0 ";
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            
            Cursor cursor = sqlDB.rawQuery(query, null);
            if (cursor.moveToFirst())
                totalWorkoutCount = cursor.getInt(cursor.getColumnIndex("num_workout"));
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            
            
        } finally {
            
            
        }
        
        return totalWorkoutCount;
    }
    private static int totalExercisesCount=0;
    public static int totalParseExercises(){
        ArrayList<HashMap<String, Object>> mapExerciseArray = new ArrayList<HashMap<String, Object>>();

        ParseQuery parseQuery = new ParseQuery(Exercise.class);
        parseQuery.fromLocalDatastore();
        //parseQuery.whereEqualTo("trainer", Trainer.getCurrent());
        parseQuery.include("measurements");
        parseQuery.findInBackground(new FindCallback<Exercise>() {
            @Override
            public void done(List<Exercise> list, ParseException e) {
//                Log.e("test", (e == null) + " - " + (list == null));
                if (e == null && list != null) {
                    if (list.size() == 0 ) {
                        ParseQuery parseQuery = new ParseQuery(Exercise.class);
                       // parseQuery.whereEqualTo("trainer", Trainer.getCurrent());
                        parseQuery.include("measurements");
                        parseQuery.findInBackground(new FindCallback<Exercise>() {

                            @Override
                            public void done(List<Exercise> list, ParseException e) {
                                if (e == null && list != null) {
                                    totalExercisesCount=list.size();
                                    Exercise.pinAllInBackground(list);
                                }
                            }
                        });
                    } else {
                        totalExercisesCount=list.size();
                    }
                }
            }
        });
        return totalExercisesCount;
    }
    public static int totalExercises() {
        int totalExerciseCount = 0;
        String query = "select count(*) as num_workout from " +
                Table.Exercise.TABLE_NAME + " where " + Table.DELETED + " = 0 ";
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            
            Cursor cursor = sqlDB.rawQuery(query, null);
            if (cursor.moveToFirst())
                totalExerciseCount = cursor.getInt(cursor.getColumnIndex("num_workout"));
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            
            
        } finally {
            
            
        }
        
        return totalExerciseCount;
    }

    public static String topExercise() {

        String topExerciseTitle = "";
        String query = "select * from exercise where _id=" +
                "(select exercise_id from " +
                "(select max(num_exe),exercise_id from " +
                "( select count(exercise_id) as num_exe , exercise_id from session_measurements " +
                //  "(select * from session_measurements group by session_id) " +
                "group by exercise_id)))";

        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            
            Cursor cursor = sqlDB.rawQuery(query, null);
            if (cursor.moveToFirst())
                topExerciseTitle = cursor.getString(cursor.getColumnIndex(Table.Workout.NAME));
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            
            
        } finally {
            
            
        }
        

        return topExerciseTitle != null ? topExerciseTitle : "";
    }

    public static String membershipPackageTotal(String filterQuery) {

        String packageTotal = "";
        String query = "select sum(package_total) as " + Table.Membership.PACKAGE_TOTAL
                + " from membership where created>= datetime(\'now\',\'" + filterQuery + "\') and deleted=0";

        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            
            Cursor cursor = sqlDB.rawQuery(query, null);
            if (cursor.moveToFirst())
                packageTotal = cursor.getString(cursor.getColumnIndex(Table.Membership.PACKAGE_TOTAL));
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            
            
        } finally {
            
            
        }
        

        return packageTotal != null ? packageTotal : "";
    }


    public static String scheduledSessionTotal(String filterQuery) {

        String sessionTotal = "";
        String query = "select count(_id) as num_session "
                + " from sessions where start_date>= datetime(\'now\',\'"
                + filterQuery + "\') and session_status = 0 and  deleted=0";

        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            
            Cursor cursor = sqlDB.rawQuery(query, null);
            if (cursor.moveToFirst())
                sessionTotal = cursor.getString(cursor.getColumnIndex("num_session"));
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            
            
        } finally {
            
            
        }
        

        return sessionTotal != null ? sessionTotal : "";
    }

    public static String newClientsTotal(String filterQuery) {

        String clientTotal = "";
        String query = "select count(_id) as num_client "
                + " from client where updated>= datetime(\'now\',\'"
                + filterQuery + "\') and  deleted=0";

        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            
            Cursor cursor = sqlDB.rawQuery(query, null);
            if (cursor.moveToFirst())
                clientTotal = cursor.getString(cursor.getColumnIndex("num_client"));
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            
            
        } finally {
            
            
        }
        

        return clientTotal != null ? clientTotal : "";
    }
    private static  int totalclientscount=0;
    public static int totalParseClients(){
        ArrayList<HashMap<String, Object>> mapClientArray = new ArrayList<HashMap<String, Object>>();
        ParseQuery parseQuery = new ParseQuery(Client.class);
        parseQuery.fromLocalDatastore();
        //parseQuery.whereEqualTo("trainer", Trainer.getCurrent());
        parseQuery.findInBackground(new FindCallback<Client>() {
            @Override
            public void done(List<Client> list, ParseException e) {
                if (e == null && list != null) {
                    if (list.size() == 0) {
                        ParseQuery parseQuery = new ParseQuery(Client.class);
                        //parseQuery.whereEqualTo("trainer", Trainer.getCurrent());
                        parseQuery.findInBackground(new FindCallback<Client>() {
                            @Override
                            public void done(List<Client> list, ParseException e) {
                                if (e == null && list != null) {
                                    totalclientscount=list.size();
                                    Client.pinAllInBackground(list);
                                }
                            }
                        });
                    } else {
                        totalclientscount=list.size();
                    }
                }
            }
        });
      return totalclientscount;
    }
    public static int totalClients() {
        int totalClientsCount = 0;
        String query = "select count(*) as num_client from " +
                Table.Client.TABLE_NAME + " where " + Table.DELETED + " = 0 ";
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            
            Cursor cursor = sqlDB.rawQuery(query, null);
            if (cursor.moveToFirst())
                totalClientsCount = cursor.getInt(cursor.getColumnIndex("num_client"));
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            
            
        } finally {
            
            
        }
        
        return totalClientsCount;
    }
    private static int totalGroupsCount=0;
    public static int totalParseGroups(){
        ArrayList<HashMap<String, Object>> mapGroupArray = new ArrayList<HashMap<String, Object>>();

        ParseQuery parseQuery = new ParseQuery(Group.class);
        parseQuery.fromLocalDatastore();
       // parseQuery.whereEqualTo("trainer", Trainer.getCurrent());
        parseQuery.findInBackground(new FindCallback<Group>() {
            @Override
            public void done(List<Group> list, ParseException e) {
                if (e == null && list != null) {
                    if (list.size() == 0) {
                        ParseQuery parseQuery = new ParseQuery(Group.class);
                        //parseQuery.whereEqualTo("trainer", Trainer.getCurrent());
                        parseQuery.findInBackground(new FindCallback<Group>() {
                            @Override
                            public void done(List<Group> list, ParseException e) {
                                if (e == null && list != null) {
                                    totalGroupsCount=list.size();
                                    Group.pinAllInBackground(list);
                                }
                            }
                        });
                    } else {
                        totalGroupsCount=list.size();
                    }
                }
            }
        });
        return totalGroupsCount;
    }
    public static int totalGroups() {
        int totalGroupsCount = 0;
        String query = "select count(*) as num_group from " +
                Table.Group.TABLE_NAME + " where " + Table.DELETED + " = 0 ";
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            
            Cursor cursor = sqlDB.rawQuery(query, null);
            if (cursor.moveToFirst())
                totalGroupsCount = cursor.getInt(cursor.getColumnIndex("num_group"));
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            
            
        } finally {
            
            
        }
        
        return totalGroupsCount;
    }
   public static void loadSessions(){
       ParseQuery parseQuery = new ParseQuery(Session.class);
       parseQuery.fromLocalDatastore();
       parseQuery.whereEqualTo("trainer", Trainer.getCurrent());
       parseQuery.findInBackground(new FindCallback<Session>() {
           @Override
           public void done(List<Session> list, com.parse.ParseException e) {
               if (e == null && list != null) {
                   if (list.size() == 0) {
                       ParseQuery parseQuery = new ParseQuery(Session.class);
                       parseQuery.whereEqualTo("trainer", Trainer.getCurrent());
                       parseQuery.findInBackground(new FindCallback<Session>() {
                           @Override
                           public void done(List<Session> list, com.parse.ParseException e) {
                               if (e == null && list.size() != 0) {
                                   Session.pinAllInBackground(list);
                               }
                           }
                       });
                   } else {
                   }
               }
           }
       });
   }

    public static int totalAccreditationCaount;
    public static int totalParseQualificatioins(){
        ParseQuery parseQuery = new ParseQuery(Accreditation.class);
        parseQuery.fromLocalDatastore();
        parseQuery.whereEqualTo("trainer", Trainer.getCurrent());
        parseQuery.findInBackground(new FindCallback<Accreditation>() {
            @Override
            public void done(List<Accreditation> list, ParseException e) {
                if (e == null && list != null) {
                    if (list.size() == 0) {
                        ParseQuery parseQuery = new ParseQuery(Accreditation.class);
                        parseQuery.whereEqualTo("trainer", Trainer.getCurrent());
                        parseQuery.findInBackground(new FindCallback<Accreditation>() {
                            @Override
                            public void done(List<Accreditation> list, ParseException e) {
                                if (e == null && list != null) {
                                    Accreditation.pinAllInBackground(list);
                                    totalAccreditationCaount=list.size();
                                }
                            }
                        });
                    } else {
                        totalAccreditationCaount=list.size();
                    }
                }
            }
        });
        return totalAccreditationCaount;
    }

    public static String obj_id="";
    public static void loadAssessmentFormField() {
        ParseQuery parseQuery = new ParseQuery(AssessmentFormField.class);
        parseQuery.fromLocalDatastore();
        //parseQuery.whereEqualTo("form", AssessmentForm.createWithoutData(AssessmentForm.class, form_id));
        parseQuery.findInBackground(new FindCallback<AssessmentFormField>() {
            @Override
            public void done(List<AssessmentFormField> list, ParseException e) {
                if (e == null && list != null) {
                    if (list.size() == 0) {
                        ParseQuery parseQuery = new ParseQuery(AssessmentFormField.class);
                        //parseQuery.whereEqualTo("form", AssessmentForm.createWithoutData(AssessmentForm.class, form_id));
                        parseQuery.findInBackground(new FindCallback<AssessmentFormField>() {
                            @Override
                            public void done(List<AssessmentFormField> list, ParseException e) {
                                if (e == null && list!= null) {
                                    AssessmentFormField.pinAllInBackground(list);
                                }
                            }
                        });
                    } else {
                    }
                }
            }
        });
    }

    public static void loadStatus(){
        ParseQuery parseQuery = new ParseQuery(Status.class);
        parseQuery.fromLocalDatastore();
        parseQuery.findInBackground(new FindCallback<Status>() {
            @Override
            public void done(List<Status> list, ParseException e) {
                if (e == null && list != null) {
                    if (list.size() == 0) {
                        ParseQuery parseQuery = new ParseQuery(Status.class);
                        parseQuery.findInBackground(new FindCallback<Status>() {
                            @Override
                            public void done(List<Status> list_, ParseException e) {
                                if (e == null && list_ != null) {
                                    Status.pinAllInBackground(list_);
                                }
                            }
                        });
                    } else {
                    }
                }
            }
        });
    }

    public static void loadAssessmentField() {
        ParseQuery parseQuery = new ParseQuery(AssessmentField.class);
        parseQuery.fromLocalDatastore();
        parseQuery.findInBackground(new FindCallback<AssessmentField>() {
            @Override
            public void done(List<AssessmentField> list, ParseException e) {
                if (e == null && list != null) {
                    if (list.size() == 0) {
                        ParseQuery parseQuery = new ParseQuery(AssessmentField.class);
                        parseQuery.findInBackground(new FindCallback<AssessmentField>() {
                            @Override
                            public void done(List<AssessmentField> list_, ParseException e) {
                                if (e == null && list_ != null) {
                                    AssessmentField.pinAllInBackground(list_);
                                }
                            }
                        });
                    } else {
                    }
                }
            }
        });
    }

    public static void loadAssessmentFormType() {
        ParseQuery parseQuery = new ParseQuery(AssessmentFormType.class);
        parseQuery.fromLocalDatastore();
        parseQuery.findInBackground(new FindCallback<AssessmentFormType>() {
            @Override
            public void done(List<AssessmentFormType> list, ParseException e) {
                if (e == null && list != null) {
                    if (list.size() == 0) {
                        ParseQuery parseQuery = new ParseQuery(AssessmentFormType.class);
                        parseQuery.findInBackground(new FindCallback<AssessmentFormType>() {
                            @Override
                            public void done(List<AssessmentFormType> list_, ParseException e) {
                                if (e == null && list_ != null) {
                                    AssessmentFormType.pinAllInBackground(list_);
                                }
                            }
                        });
                    } else {
                    }
                }
            }
        });
    }

    public static void loadAssessmentForm() {
        ParseQuery parseQuery = new ParseQuery(AssessmentForm.class);
        parseQuery.fromLocalDatastore();
        parseQuery.findInBackground(new FindCallback<AssessmentForm>() {
            @Override
            public void done(List<AssessmentForm> list, ParseException e) {
                if (e == null && list != null) {
                    if (list.size() == 0) {
                        ParseQuery parseQuery = new ParseQuery(AssessmentForm.class);
                        parseQuery.findInBackground(new FindCallback<AssessmentForm>() {
                            @Override
                            public void done(List<AssessmentForm> list_, ParseException e) {
                                if (e == null && list_ != null) {
                                    AssessmentForm.pinAllInBackground(list_);
                                }
                            }
                        });
                    } else {
                    }
                }
            }
        });
    }
    public static int totalQualifications() {
        int totalQualificationsCount = 0;
        String query = "select count(*) as num_qualification from " +
                Table.TrainerProfileAccreditation.TABLE_NAME + " where " + Table.DELETED + " = 0 ";
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            
            Cursor cursor = sqlDB.rawQuery(query, null);
            if (cursor.moveToFirst())
                totalQualificationsCount = cursor.getInt(cursor.getColumnIndex("num_qualification"));
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            
            
        } finally {
            
            
        }
        
        return totalQualificationsCount;
    }
    public static int totalHours() {
        int totalQualificationsCount = 0;
        String query = "select sum(" + Table.TrainerProfileAccreditation.POINTS_HOURS + ") as num_points from " +
                Table.TrainerProfileAccreditation.TABLE_NAME + " where " + Table.DELETED + " = 0 AND " +
                Table.TrainerProfileAccreditation.IS_POINT + " = 1";
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            
            Cursor cursor = sqlDB.rawQuery(query, null);
            if (cursor.moveToFirst())
                totalQualificationsCount = cursor.getInt(cursor.getColumnIndex("num_points"));
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            
            
        } finally {
            
            
        }
        
        return totalQualificationsCount;
    }public static int totalPoints() {
        int totalQualificationsCount = 0;
        String query = "select sum(" + Table.TrainerProfileAccreditation.POINTS_HOURS + ") as num_points from " +
                Table.TrainerProfileAccreditation.TABLE_NAME + " where " + Table.DELETED + " = 0 AND " +
                Table.TrainerProfileAccreditation.IS_POINT + " = 0";
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            
            Cursor cursor = sqlDB.rawQuery(query, null);
            if (cursor.moveToFirst())
                totalQualificationsCount = cursor.getInt(cursor.getColumnIndex("num_points"));
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            
            
        } finally {
            
            
        }
        
        return totalQualificationsCount;
    }

    public static int getSessionStatus(String sessionId) {
        int status = -1;
        String query = "select " + Table.Sessions.SESSION_STATUS + " from " +
                Table.Sessions.TABLE_NAME + " where " + Table.Sessions.ID + " = " + sessionId;
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            
            Cursor cursor = sqlDB.rawQuery(query, null);
            if (cursor.moveToFirst())
                status = cursor.getInt(cursor.getColumnIndex(Table.Sessions.SESSION_STATUS));
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            
            
        } finally {
            
            
        }
        
        return status;
    }

    public static void clearAllData() {

        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getWritableDatabase();
            sqlDB.delete(Table.Exercise.TABLE_NAME, null, null);
            sqlDB.delete(Table.ExerciseMeasurements.TABLE_NAME, null, null);
            sqlDB.delete(Table.Workout.TABLE_NAME, null, null);
            sqlDB.delete(Table.WorkoutExercises.TABLE_NAME, null, null);
            sqlDB.delete(Table.TrainerProfileAccreditation.TABLE_NAME, null, null);
            sqlDB.delete(Table.Client.TABLE_NAME, null, null);
            sqlDB.delete(Table.Group.TABLE_NAME, null, null);
            sqlDB.delete(Table.GroupClients.TABLE_NAME, null, null);
            sqlDB.delete(Table.TrainerProfileDetails.TABLE_NAME, null, null);
            sqlDB.delete(Table.CompletedAssessmentForm.TABLE_NAME, null, null);
            sqlDB.delete(Table.CompletedAssessmentFormField.TABLE_NAME, null, null);
            sqlDB.delete(Table.Sessions.TABLE_NAME, null, null);
            sqlDB.delete(Table.SessionMeasurements.TABLE_NAME, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
       /* Utils.deleteRecursive(new File(Utils.PROFILE_THUMBNAIL_PATH));
        Utils.deleteRecursive(new File(Utils.THUMBNAIL_PATH));*/
        Utils.deleteRecursive(new File(Utils.LOCAL_RESOURCE_PATH));
    }

    public static void clearExerciseAndWorkoutData() {

        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getWritableDatabase();
            sqlDB.delete(Table.Exercise.TABLE_NAME, null, null);
            sqlDB.delete(Table.ExerciseMeasurements.TABLE_NAME, null, null);
            sqlDB.delete(Table.Workout.TABLE_NAME, null, null);
            sqlDB.delete(Table.WorkoutExercises.TABLE_NAME, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    public static String getDeviceExerciseId(String server_exercise_id) {
        SQLiteDatabase sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
        try {
            Cursor cursor = sqLiteDatabase.rawQuery("select _id from exercise where exercise_id=" + server_exercise_id, null);
            if (cursor.moveToFirst()) {
                String _id = cursor.getString(0);
                if (_id != null)
                    return _id;
            }
            cursor.close();
        } catch (Exception e) {
            
            e.printStackTrace();
        } finally {
            
        }
        return "";
    }


    public static HashMap<String, Object> getWorkout(long created_id) {
        HashMap<String, Object> workout = new HashMap<String, Object>();
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            String query = "select  * from " +
                    Table.Workout.TABLE_NAME + " where _id = " + created_id;
            Log.d("query is ", query);
            Cursor cursor = sqlDB.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    workout.put(cursor.getColumnName(i), cursor.getString(i) != null ? cursor.getString(i) : "");
                }
                workout.put("exercise", getWorkoutExercises(cursor.getString(0)));
            }
            cursor.close();
        } catch (Exception e) {
            Log.d("", e.toString());
        } finally {
            if (sqlDB != null) {
                
            }
        }
        Log.d("workoutList", workout.toString());
        return workout;
    }

    public static HashMap<String, Object> getGroup(String created_id) {
        HashMap<String, Object> workout = new HashMap<String, Object>();
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            String query = "select  * from " +
                    Table.Group.TABLE_NAME + " where _id = " + created_id;
            Log.d("query is ", query);
            Cursor cursor = sqlDB.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    workout.put(cursor.getColumnName(i), cursor.getString(i) != null ? cursor.getString(i) : "");
                }
                workout.put("clients", getGroupClient(cursor.getString(0)));
            }
            cursor.close();
        } catch (Exception e) {
            Log.d("", e.toString());
        } finally {
            if (sqlDB != null) {
                
            }
        }
        Log.d("groupList", workout.toString());
        return workout;
    }

    public static ArrayList<HashMap<String, Object>> getGroupClient(String id) {
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

    public static ArrayList<HashMap<String, Object>> getWorkoutExercises(String workout_id) {
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

    public static HashMap<String, Object> getExercise(String exerciseId) {
        HashMap<String, Object> exercise = new LinkedHashMap<String, Object>();
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            String query = "select  * from " +
                    Table.Exercise.TABLE_NAME + " where " + Table.Exercise.ID + " = " + exerciseId;
            Log.d("query is ", query);
            Cursor cursor = sqlDB.rawQuery(query, null);

            if (cursor.moveToFirst()) {

                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    exercise.put(cursor.getColumnName(i), cursor.getString(i) != null ? cursor.getString(i) : "");
                }
                exercise.put("measurement", getMeasurements(cursor.getString(0)));
            }
            cursor.close();
        } catch (Exception e) {
            Log.d("exercises exception", exercise.toString());
        } finally {
            if (sqlDB != null) {
                
            }
        }
        Log.d("exercisesList", exercise.toString());
        return exercise;
    }

    private static ArrayList<HashMap<String, Object>> getMeasurements(String exercise_id) {
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
            Log.d("measurement exception", e.toString());
        } finally {
            if (sqlDB != null) {
                
            }
        }
        Log.d("measurementsList", measurementsList.toString());
        return measurementsList;
    }

    public static HashMap<String, Object> getQualification(String accreditationId) {
        HashMap<String, Object> qualification = new LinkedHashMap<String, Object>();
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            String query = "select  * from " +
                    Table.TrainerProfileAccreditation.TABLE_NAME + " where " + Table.ID + " = " + accreditationId;
            Log.d("query is ", query);
            Cursor cursor = sqlDB.rawQuery(query, null);

            if (cursor.moveToFirst()) {

                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    qualification.put(cursor.getColumnName(i), cursor.getString(i) != null ? cursor.getString(i) : "");
                }
            }
            cursor.close();
        } catch (Exception e) {
            Log.d("qualification exception", qualification.toString());
        } finally {
            if (sqlDB != null) {
                
            }
        }
        Log.d("qualification", qualification.toString());
        return qualification;
    }

    public static String getAccreditationWebIdFromRecordId(String accreditationId) {
        SQLiteDatabase sqlDB = null;
        String webAccreditationId = "";
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            String query = "select " + Table.TrainerProfileAccreditation.ACCREDITATION_ID + " from " +
                    Table.TrainerProfileAccreditation.TABLE_NAME + " where " + Table.ID + " = " + accreditationId;
            Log.d("query is ", query);
            Cursor cursor = sqlDB.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                webAccreditationId = cursor.getString(cursor.getColumnIndex(Table.TrainerProfileAccreditation.ACCREDITATION_ID));
            }

        } catch (Exception e) {
            e.printStackTrace();
            

        } finally {
            
        }
        
        return webAccreditationId;
    }

    public static ArrayList<HashMap<String, Object>> getAssessmentFormFields(String formId) {
        ArrayList assessmentFieldsList = new ArrayList<Map<String, Object>>();
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            String query = "select * from " + Table.CompletedAssessmentFormField.TABLE_NAME
                    + " where " + Table.CompletedAssessmentFormField.FORM_ID + " = " + formId;
            Log.d("query is ", query);
            Cursor cursor = sqlDB.rawQuery(query, null);
            LinkedHashMap<String, Object> row;
            while (cursor.moveToNext()) {
                row = new LinkedHashMap<String, Object>();
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    row.put(cursor.getColumnName(i), cursor.getString(i) != null ? cursor.getString(i) : "");
                }
                assessmentFieldsList.add(row);
            }
            cursor.close();
        } catch (Exception e) {
            Log.d("assessmentFields ", e.toString());
        } finally {
            if (sqlDB != null) {
                
            }
        }
        Log.d("assessmentFieldsList", assessmentFieldsList.toString());
        return assessmentFieldsList;
    }

    public static HashMap<String, Object> getClient(String clientId) {
        HashMap<String, Object> client = new LinkedHashMap<String, Object>();
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            String query = "select  * from " +
                    Table.Client.TABLE_NAME + " where " + Table.ID + " = " + clientId;
            Log.d("query is ", query);
            Cursor cursor = sqlDB.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    client.put(cursor.getColumnName(i), cursor.getString(i) != null ? cursor.getString(i) : "");
                }
            }
            cursor.close();
        } catch (Exception e) {
            Log.d("client exception", client.toString());
        } finally {
            if (sqlDB != null) {
                
            }
        }
        Log.d("client", client.toString());
        return client;
    }

    public static String getDeviceGroupId(String serverGroupId) {
        SQLiteDatabase sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
        try {
            Cursor cursor = sqLiteDatabase.rawQuery("select _id from " + Table.Group.TABLE_NAME + " where group_id=" + serverGroupId, null);
            if (cursor.moveToFirst()) {
                String _id = cursor.getString(0);
                if (_id != null)
                    return _id;
            }
            cursor.close();
        } catch (Exception e) {
            
            e.printStackTrace();
        } finally {
            
        }
        return "";
    }

    public static String getDeviceClientId(String serverClientId) {
        SQLiteDatabase sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
        try {
            Cursor cursor = sqLiteDatabase.rawQuery("select _id from " + Table.Client.TABLE_NAME + " where client_id=" + serverClientId, null);
            if (cursor.moveToFirst()) {
                String _id = cursor.getString(0);
                if (_id != null)
                    return _id;
            }
            cursor.close();
        } catch (Exception e) {
            
            e.printStackTrace();
        } finally {
            
        }
        return "";
    }

    public static String getDeviceWorkoutId(String serverWorkoutId) {
        SQLiteDatabase sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
        try {
            Cursor cursor = sqLiteDatabase.rawQuery("select _id from " + Table.Workout.TABLE_NAME + " where workout_id=" + serverWorkoutId, null);
            if (cursor.moveToFirst()) {
                String _id = cursor.getString(0);
                if (_id != null)
                    return _id;
            }
            cursor.close();
        } catch (Exception e) {
            
            e.printStackTrace();
        } finally {
            
        }
        return "";
    }

    public static String getServerGroupId(String deviceGroupId) {
        SQLiteDatabase sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
        try {
            Cursor cursor = sqLiteDatabase.rawQuery("select group_id from " + Table.Group.TABLE_NAME + " where _id=" + deviceGroupId, null);
            if (cursor.moveToFirst()) {
                String _id = cursor.getString(0);
                if (_id != null)
                    return _id;
            }
            cursor.close();
        } catch (Exception e) {
            
            e.printStackTrace();
        } finally {
            
        }
        return "";
    }

    public static String getServerClientId(String deviceClientId) {
        SQLiteDatabase sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
        try {
            Cursor cursor = sqLiteDatabase.rawQuery("select client_id from " + Table.Client.TABLE_NAME + "  where _id = " + deviceClientId, null);
            if (cursor.moveToFirst()) {
                String _id = cursor.getString(0);
                if (_id != null)
                    return _id;
            }
            cursor.close();
        } catch (Exception e) {
            
            e.printStackTrace();
        } finally {
            
        }
        return "";
    }

    public static String getServerWorkoutId(String deviceWorkoutId) {
        SQLiteDatabase sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
        try {
            Cursor cursor = sqLiteDatabase.rawQuery("select workout_id from " + Table.Workout.TABLE_NAME + "  where _id = " + deviceWorkoutId, null);
            if (cursor.moveToFirst()) {
                String _id = cursor.getString(0);
                if (_id != null)
                    return _id;
            }
            cursor.close();
        } catch (Exception e) {
            
            e.printStackTrace();
        } finally {
            
        }
        return "0";
    }

    public static String getServerExerciseId(String deviceExerciseId) {
        SQLiteDatabase sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
        try {
            Cursor cursor = sqLiteDatabase.rawQuery("select exercise_id from " + Table.Exercise.TABLE_NAME + "  where _id = " + deviceExerciseId, null);
            if (cursor.moveToFirst()) {
                String _id = cursor.getString(0);
                if (_id != null)
                    return _id;
            }
            cursor.close();
        } catch (Exception e) {
            
            e.printStackTrace();
        } finally {
            
        }
        return "";
    }
    public static long getLastCreatedPayment() {
        SQLiteDatabase sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
        long last = 0;
        try {
            Cursor cursor = sqLiteDatabase.rawQuery("select " + Table.StripePayment.CREATED +
                    " from " + Table.StripePayment.TABLE_NAME, null);
            List<Long> data = new ArrayList<>();
            while (cursor.moveToNext()){
                data.add(cursor.getLong(0));
            }
            Collections.sort(data);
            last = data.get(data.size() - 1);
            cursor.close();
        } catch (Exception e) {
            
            e.printStackTrace();
        } finally {
            
        }
        return last;
    }

    public static boolean isStripePaymentsEmpty() {
        SQLiteDatabase sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
        int count = 0;
        try {
            Cursor cursor = sqLiteDatabase.rawQuery("select " + Table.StripePayment.ID +
                    " from " + Table.StripePayment.TABLE_NAME, null);
            count = cursor.getCount();
            cursor.close();
        } catch (Exception e) {
            
            e.printStackTrace();
        } finally {
            
        }
        return count == 0;
    }

    public static long insertToStripePayment(Charge charge) {
        SQLiteDatabase sqlDB = null;
        ContentValues contentValues = new ContentValues();
        contentValues.put(Table.StripePayment.PAYMENT_ID, charge.getId());
        contentValues.put(Table.StripePayment.CREATED, charge.getCreated());
        contentValues.put(Table.StripePayment.CURRENCY, charge.getCurrency());
        contentValues.put(Table.StripePayment.CUSTOMER_ID, charge.getCustomer());
        contentValues.put(Table.StripePayment.PACKAGE_TOTAL, charge.getAmount());
        contentValues.put(Table.StripePayment.DESCRIPTION, charge.getDescription());
        try {
            contentValues.put(Table.StripePayment.CUSTOMER_MAIL, Customer.retrieve(
                    charge.getCustomer()).getEmail());
        } catch (AuthenticationException e) {
            e.printStackTrace();
            Log.d("details", "customer authentication error");
        } catch (InvalidRequestException e) {
            e.printStackTrace();
            Log.d("details", "customer Invalid request error");
        } catch (APIConnectionException e) {
            e.printStackTrace();
            Log.d("details", "customer api connect error");
        } catch (CardException e) {
            e.printStackTrace();
            Log.d("details", "customer card exception error");
        } catch (APIException e) {
            e.printStackTrace();
            Log.d("details", "customer APi error");
        }
        contentValues.put(Table.StripePayment.CLIENT_ID,
                charge.getMetadata().get("client_id"));
        contentValues.put(Table.StripePayment.GROUP_ID,
                charge.getMetadata().get("group_id"));
        contentValues.put(Table.StripePayment.QUANTITY,
                charge.getMetadata().get("quantity"));
        try {
            sqlDB = DatabaseHelper.instance().getWritableDatabase();
            

            long row_id = sqlDB.insert(Table.StripePayment.TABLE_NAME, null, contentValues);
            if (row_id > 0) {
                return row_id;
            } else {
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            
            //  

        } finally {
            
            //  
        }
        
        return -1;
    }

    public static List<ContentValues> getAllPayments(int anInt) {
        SQLiteDatabase sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
        List<ContentValues> data = null;
        try {
            String q;
            if (anInt == 1) {
                q = "select * from " + Table.StripePayment.TABLE_NAME + " where NOT " + Table.StripePayment.GROUP_ID + " = 0";
            } else {
                q = "select * from " + Table.StripePayment.TABLE_NAME + " where  " + Table.StripePayment.GROUP_ID + " = 0";
            }
            Cursor cursor = sqLiteDatabase.rawQuery(q, null);
            data = new ArrayList<>();
            while (cursor.moveToNext()) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(Table.StripePayment.CUSTOMER_MAIL,
                        cursor.getString(cursor.getColumnIndex(Table.StripePayment.CUSTOMER_MAIL)));
                contentValues.put(Table.StripePayment.DESCRIPTION,
                        cursor.getString(cursor.getColumnIndex(Table.StripePayment.DESCRIPTION)));
                contentValues.put(Table.StripePayment.PACKAGE_TOTAL,
                        cursor.getString(cursor.getColumnIndex(Table.StripePayment.PACKAGE_TOTAL)));
                contentValues.put(Table.StripePayment.CREATED,
                        cursor.getString(cursor.getColumnIndex(Table.StripePayment.CREATED)));

                data.add(contentValues);
            }
            cursor.close();
        } catch (Exception e) {
            
            e.printStackTrace();
        } finally {
            
        }
        return data;
    }
    public static boolean paymentExists(String id) {
        SQLiteDatabase sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
        int count = 0;
        try {
            String q;
            q = "select * from " + Table.StripePayment.TABLE_NAME + " where " +
                    Table.StripePayment.PAYMENT_ID + " Like '" + id + "'";
            Cursor cursor = sqLiteDatabase.rawQuery(q, null);
            count = cursor.getCount();

            cursor.close();
        } catch (Exception e) {
            
            e.printStackTrace();
        } finally {
            
        }
        return count != 0;
    }

    public static boolean eventExists (long nativeId) {
        SQLiteDatabase sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
        int count = 0;
        try {
            String q;
            q = "select * from " + Table.Sessions.TABLE_NAME + " where " +
                    Table.Sessions.NATIVE_ID + " = " + nativeId + "";
            Cursor cursor = sqLiteDatabase.rawQuery(q, null);
            count = cursor.getCount();

            cursor.close();
        } catch (Exception e) {
            
            e.printStackTrace();
        } finally {
            
        }
        return count != 0;
    }
}
