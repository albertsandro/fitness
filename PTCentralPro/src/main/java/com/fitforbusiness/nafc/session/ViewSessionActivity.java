package com.fitforbusiness.nafc.session;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.internal.widget.ListPopupWindow;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.TimePicker;
import android.widget.Toast;

import com.fitforbusiness.Parse.Models.Session;
import com.fitforbusiness.Parse.Models.Workout;
import com.fitforbusiness.database.DBOHelper;
import com.fitforbusiness.database.DatabaseHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.framework.CustomAsyncTaskListAdapter;
import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.nafc.R;
import com.fitforbusiness.nafc.calendar.NativeSync;
import com.fitforbusiness.webservice.Synchronise;
import com.parse.FindCallback;
import com.parse.ParseQuery;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ViewSessionActivity extends ActionBarActivity implements View.OnClickListener {

    private EditText title, venue, notes;
    private Button repeat, status;
    private Button selectWorkout;
    private Button setGoals, startDate, endDate, startTime, endTime;
    private List<HashMap<String, Object>> mapWorkoutArray;
    private ListPopupWindow statusPopUp, repeatPopUp;
    private String sessionId = "";
    private SimpleAdapter workoutAdapter;
    private String workoutId;
    private int statusId = 0;
    private int repeatId = 0;
    private int isNative = 0;
    private long nativeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_session);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        title = (EditText) findViewById(R.id.etTitle);
        venue = (EditText) findViewById(R.id.etVenue);

        notes = (EditText) findViewById(R.id.etNotes);

        startDate = (Button) findViewById(R.id.bStartDate);
        endDate = (Button) findViewById(R.id.bEndDate);

        startTime = (Button) findViewById(R.id.bStartTime);
        endTime = (Button) findViewById(R.id.bEndTime);
        status = (Button) findViewById(R.id.spnStatus);
        repeat = (Button) findViewById(R.id.spnRepeat);
        selectWorkout = (Button) findViewById(R.id.spnWorkout);

        setWorkoutList();
        selectWorkout.setOnClickListener(this);
        setGoals = (Button) findViewById(R.id.bSetGoals);
        setGoals.setOnClickListener(this);

        startDate.setOnClickListener(this);
        endDate.setOnClickListener(this);
        startTime.setOnClickListener(this);
        endTime.setOnClickListener(this);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        DateFormat stf = new DateFormat();
        String formattedDate = stf.format("hh:mm:ss", Calendar.getInstance()).toString();

        startDate.setText(sdf.format(new Date()));
        endDate.setText(sdf.format(new Date()));
        startTime.setText(Utils.timeFormatAMPM(formattedDate));
        endTime.setText(Utils.timeFormatAMPM(formattedDate));

        try {
            sessionId = getIntent().getStringExtra("_id");
            loadParseSessionDetails(sessionId);

//            workoutId = getWorkoutId();
//            Log.d("workoutTest", workoutId);
//            String name = getWorkoutName(workoutId);
//            Log.d("workoutTest", name);
//            selectWorkout.setText(name);

        } catch (Exception e) {
            e.printStackTrace();
        }
//        setFieldsEditable(false);
        setRepeatPopUp();
        setStatusPopUp();
        status.setOnClickListener(this);
        repeat.setOnClickListener(this);
    }

    private void setRepeatPopUp() {
        repeatPopUp = new ListPopupWindow(this);
        repeatPopUp.setAnchorView(repeat);
        repeatPopUp.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.repeat)));
        repeatPopUp.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                repeatId = position;
                repeat.setText(getResources().getStringArray(R.array.repeat)[position]);
                repeatPopUp.dismiss();
            }
        });
    }

    private void setStatusPopUp() {
        statusPopUp = new ListPopupWindow(this);
        statusPopUp.setAnchorView(status);
        statusPopUp.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.status)));
        statusPopUp.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                statusId = position;
                status.setText(getResources().getStringArray(R.array.status)[position]);
                statusPopUp.dismiss();
            }
        });
    }

    private String getWorkoutName(String workoutId) {
        for (HashMap<String, Object> aMapWorkoutArray : mapWorkoutArray) {

            if (aMapWorkoutArray.get("_id").toString().equalsIgnoreCase(workoutId)) {
                return aMapWorkoutArray.get("name").toString();
            }
        }
        return null;
    }
    private void loadParseSessionDetails(String _id){
        ParseQuery parseQuery = new ParseQuery(Session.class);
        parseQuery.fromLocalDatastore();
        parseQuery.whereEqualTo("objectId",_id);
        parseQuery.findInBackground(new FindCallback<Session>() {
            @Override
            public void done(List<Session> list, com.parse.ParseException e) {
                if (e == null && list != null) {
                    if (list.size() == 0) {

                    } else {
                        loadIntoListView(list);
                    }
                }
            }
        });
    }

    private void loadIntoListView(List<Session> list) {
        for (Session session : list) {
            title.setText(session.getTitle());
            venue.setText(session.getVenue());
            startDate.setText(session.getStartDate());
            endDate.setText(session.getEndDate());
            startTime.setText(session.getStartTime().toString());
            endTime.setText(session.getEndTime().toString());
            statusId = (int)session.getStatus();
            String workoutId=session.getWorkout();
            selectWorkout.setText(Workout.createWithoutData(Workout.class, workoutId).getName());
           // isNative = cursor.getInt(cursor.getColumnIndex(Table.Sessions.IS_NATIVE));
            //nativeId = cursor.getLong(cursor.getColumnIndex(Table.Sessions.NATIVE_ID));
            status.setText((getResources().getStringArray(R.array.status))[statusId]);
//            if (cursor.getInt(cursor.getColumnIndex(Table.Sessions.SESSION_STATUS)) == 1) {
//                setGoals.setText("Summary");
//                // setGoals.setClickable(false);
//            }

            //cursor.getString(cursor.getColumnIndex(Table.Sessions.SESSION_TYPE));
            notes.setText(session.getNotes());
            //cursor.getString(cursor.getColumnIndex(Table.Sessions.PACKAGE_ID));
        }

    }
    private void loadSessionDetails(String _id) {


        SQLiteDatabase sqLiteDatabase = null;
        try {
            sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("select * from "
                    + Table.Sessions.TABLE_NAME + " where _id=" + _id, null);
            if (cursor.moveToFirst()) {

                title.setText(cursor.getString(cursor.getColumnIndex(Table.Sessions.TITLE)));
                venue.setText(cursor.getString(cursor.getColumnIndex(Table.Sessions.VENUE)));
                startDate.setText(Utils.formatConversionSQLite(cursor.getString(cursor.getColumnIndex(Table.Sessions.START_DATE))));
                endDate.setText(Utils.formatConversionSQLite(cursor.getString(cursor.getColumnIndex(Table.Sessions.END_DATE))));
                startTime.setText(Utils.timeFormatAMPM(cursor.getString(cursor.getColumnIndex(Table.Sessions.START_TIME))));
                endTime.setText(Utils.timeFormatAMPM(cursor.getString(cursor.getColumnIndex(Table.Sessions.END_TIME))));
                statusId = cursor.getInt(cursor.getColumnIndex(Table.Sessions.SESSION_STATUS));
                isNative = cursor.getInt(cursor.getColumnIndex(Table.Sessions.IS_NATIVE));
                nativeId = cursor.getLong(cursor.getColumnIndex(Table.Sessions.NATIVE_ID));
                status.setText((getResources().getStringArray(R.array.status))[statusId]);
                if (cursor.getInt(cursor.getColumnIndex(Table.Sessions.SESSION_STATUS)) == 1) {
                    setGoals.setText("Summary");
                    // setGoals.setClickable(false);
                }

                //cursor.getString(cursor.getColumnIndex(Table.Sessions.SESSION_TYPE));
                notes.setText(cursor.getString(cursor.getColumnIndex(Table.Sessions.NOTES)));
                //cursor.getString(cursor.getColumnIndex(Table.Sessions.PACKAGE_ID));

            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.delete_session, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.bUpdate) {
            return true;
        } else if (id == android.R.id.home) {
//            long isUpdated = updateSession();
//            if (isUpdated > 0) {
//                if (Utils.isNetworkAvailable(this)) {
//                    updateSessionOnServer(sessionId);
//                }

                this.finish();
           // }
        } else if (id == R.id.action_delete_session) {
            if (isNative == 0) {
                AlertDialog.Builder b = new AlertDialog.Builder(this);
                b.setMessage("This session will be deleted.");
                b.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        DBOHelper.delete(Table.Sessions.TABLE_NAME, Table.Sessions.ID, sessionId);
//                        NativeSync.deleteEventFromNative(getApplication(), nativeId);
                        deleteParseSession(sessionId);
                        Log.d("deleteSuccess", "success");

                        finish();
                    }
                });
                b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                b.show();
            } else {
                Toast.makeText(getApplication(), R.string.CantDelete, Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }
    public void deleteParseSession(String sessionId){
        ParseQuery parseQuery = new ParseQuery(Session.class);
        parseQuery.fromLocalDatastore();
        parseQuery.whereEqualTo("objectId",sessionId);
        parseQuery.findInBackground(new FindCallback<Session>() {
            @Override
            public void done(List<Session> list, com.parse.ParseException e) {
                if (e == null && list != null) {
                    if (list.size() == 0) {

                    } else {
                        for (Session session:list){
                            try {
                                session.unpin();
                            }catch (com.parse.ParseException e1){
                            }

                            try {
                                session.delete();
                            }catch (com.parse.ParseException e1){
                            }


                        }
                    }
                }
            }
        });
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.spnWorkout:
                showListPopUp(selectWorkout, workoutAdapter);
                break;
            case R.id.bSetGoals:
                if (((Button) view).getText().toString().equalsIgnoreCase("Summary")) {
                    startActivity(new Intent(this, GoalSummaryActivity.class).putExtra("_id", sessionId));
                } else {

                    startActivity(new Intent(this, SetGoalsActivity.class).putExtra("workout_id",
                            workoutId
                    ).putExtra("session_id", sessionId));
                }
                break;
            case R.id.bStartDate:
                showDatePicker(startDate);
                break;
            case R.id.bEndDate:
                showDatePicker(endDate);
                break;
            case R.id.bStartTime:
                showTimePicker(startTime);
                break;
            case R.id.bEndTime:
                showTimePicker(endTime);
                break;
            case R.id.spnStatus:
                statusPopUp.show();
                break;
            case R.id.spnRepeat:
                repeatPopUp.show();
                break;
        }
    }

    private void addSessionMeasurements(String session_id) {
        SQLiteDatabase sqLiteDatabase = null;
        sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select exercise_id,measurement_id" +
                " from exercise_measurements where exercise_id in" +
                "(select exercise_id from workout_exercises where workout_id=" + workoutId + ")", null);
        while (cursor.moveToNext()) {

            ContentValues contentValues = new ContentValues();
            contentValues.put(Table.SessionMeasurements.SESSION_ID, session_id);
            contentValues.put(Table.SessionMeasurements.WORKOUT_ID, workoutId);
            contentValues.put(Table.SessionMeasurements.EXERCISE_ID, cursor.getInt(0));
            contentValues.put(Table.SessionMeasurements.MEASUREMENT_ID, cursor.getInt(1));
            contentValues.put(Table.SessionMeasurements.SET_NO, 1);
            /*contentValues.put(Table.SessionMeasurements.MEASURED_VALUE, 1);
            contentValues.put(Table.SessionMeasurements.TARGET_VALUE, 2);*/
            Log.d("inserted measurement id", "" + DBOHelper.insert(this,
                    Table.SessionMeasurements.TABLE_NAME, contentValues));
        }


    }

    void showDatePicker(final Button mButton) {

        String defaultDate = mButton.getText().toString();
        Calendar cal = Calendar.getInstance();

        try {

            Date d = new SimpleDateFormat("dd MMM yyyy").parse(defaultDate);
            cal.setTime(d);
            Log.d("Date is", d + "");
        } catch (ParseException e) {
            Log.d("Error in date is", e.toString());
            e.printStackTrace();
        }

        DatePickerDialog dateDlg = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        Calendar c = Calendar.getInstance();
                        c.set(year, monthOfYear, dayOfMonth);

                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
                        String formattedDate = sdf.format(c.getTime());
                        mButton.setText(formattedDate);
                    }

                }, cal
                .get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
                .get(Calendar.DAY_OF_MONTH)
        );
        dateDlg.show();


    }

    void showTimePicker(final Button mButton) {

        String defaultDate = mButton.getText().toString();
        Calendar cal = Calendar.getInstance();

        TimePickerDialog dateDlg = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                c.set(Calendar.MINUTE, minute);
                DateFormat sdf = new DateFormat();
                String formattedDate = sdf.format("hh:mm a", c).toString();
                mButton.setText(formattedDate);
            }
        }, cal
                .get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false);
        dateDlg.show();


    }

    private long updateSession() {

        ContentValues contentValues = new ContentValues();
        contentValues.put(Table.Sessions.TITLE, title.getText().toString());
        contentValues.put(Table.Sessions.VENUE, venue.getText().toString());
        contentValues.put(Table.Sessions.START_DATE, Utils.formatConversionLocale(startDate.getText().toString()));
        contentValues.put(Table.Sessions.END_DATE, Utils.formatConversionLocale(endDate.getText().toString()));
        contentValues.put(Table.Sessions.START_TIME, Utils.timeFormat24(startTime.getText().toString()));
        contentValues.put(Table.Sessions.END_TIME, Utils.timeFormat24(endTime.getText().toString()));
        contentValues.put(Table.Sessions.SESSION_STATUS, statusId);
        // contentValues.put(Table.Sessions.SESSION_TYPE, "0");
        // contentValues.put(Table.Sessions.GROUP_ID, groupClientId);
        contentValues.put(Table.Sessions.NOTES, notes.getText().toString());
        contentValues.put(Table.Sessions.PACKAGE_ID, new Date().getTime());
        long rowId = DBOHelper.update(Table.Sessions.TABLE_NAME, contentValues, sessionId);
        if (rowId > 0) {
            Log.d("Updated   session", rowId + "");
        }
        return rowId;
    }

    private void showListPopUp(final Button selectWorkout, final SimpleAdapter simpleAdapter) {
        final ListPopupWindow popup = new ListPopupWindow(this);
        popup.setAnchorView(selectWorkout);
        popup.setAdapter(simpleAdapter);
        popup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map map = mapWorkoutArray.get(position);
                selectWorkout.setText(map.get("name").toString());
                workoutId = map.get("_id").toString();
                if (!mapWorkoutArray.get(position).get("_id").toString().equalsIgnoreCase(getWorkoutId())) {
                    DBOHelper.delete(Table.SessionMeasurements.TABLE_NAME,
                            Table.SessionMeasurements.SESSION_ID, sessionId);
                    addSessionMeasurements(sessionId);
                }
                popup.dismiss();
            }
        });
        popup.show();
    }

    private void setWorkoutList() {

        mapWorkoutArray = new ArrayList<HashMap<String, Object>>();
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();

            String query = "select  * "
                    + " from " +
                    Table.Workout.TABLE_NAME + " where " + Table.DELETED
                    + " = 0  order by " + Table.Workout.NAME + " asc";
            Log.d("query is ", query);
            assert sqlDB != null;
            Cursor cursor = sqlDB
                    .rawQuery(query
                            , null);

            HashMap<String, Object> row;
            while (cursor.moveToNext()) {


                row = new HashMap<>();
                row.put("_id", cursor.getString(cursor
                        .getColumnIndex(Table.Workout.ID)));

                row.put("name", cursor.getString(cursor
                        .getColumnIndex(Table.Workout.NAME)));


                mapWorkoutArray.add(row);
            }
            cursor.close();
        } catch (Exception e) {
            assert sqlDB != null;
            sqlDB.close();
            e.printStackTrace();
        } finally {
            assert sqlDB != null;
            sqlDB.close();
        }
        SimpleAdapter adapter = new SimpleAdapter(this, mapWorkoutArray,
                android.R.layout.simple_list_item_1, new String[]{"name"}, new int[]{android.R.id.text1});

        workoutAdapter = new SimpleAdapter(this, mapWorkoutArray,
                android.R.layout.simple_list_item_1, new String[]{"name"}, new int[]{android.R.id.text1});
    }

    private String getWorkoutId() {

        SQLiteDatabase sqLiteDatabase;
        try {
            sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("select workout_id from " +
                    Table.SessionMeasurements.TABLE_NAME + " where session_id = " + sessionId, null);
            if (cursor.moveToFirst())
                return cursor.getString(0);

            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    private int getSpinnerSelectedItem(String workoutId) {

        for (int i = 0; i < mapWorkoutArray.size(); i++) {
            HashMap map = mapWorkoutArray.get(i);
            if (map.get("_id").toString().equalsIgnoreCase(workoutId))
                return i;
        }
        return 0;
    }

    private void setFieldsEditable(boolean enable) {

        startDate.setEnabled(enable);
        endDate.setEnabled(enable);
        startTime.setEnabled(enable);
        endTime.setEnabled(enable);
        repeat.setEnabled(enable);
        title.setEnabled(enable);
        venue.setEnabled(enable);
        if (enable) {
            //addClient.setVisibility(View.VISIBLE);
        } else {
            //addClient.setVisibility(View.GONE);
        }

    }

    private void updateSessionOnServer(final String _id) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Synchronise.Session syncSession = new Synchronise.Session(ViewSessionActivity.this,
                        Utils.getLastClientSyncTime(ViewSessionActivity.this));
                syncSession.propagateDeviceObjectToServer(Synchronise.getDeviceObjectById(_id, syncSession.getDeviceSessionsSet()));
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Log.d("Updated Session", "Session created on the server");
            }
        }.execute();
    }

}
