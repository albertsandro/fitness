package com.fitforbusiness.nafc.session;

import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import com.appboy.Appboy;
import com.fitforbusiness.Parse.Models.Client;
import com.fitforbusiness.Parse.Models.Exercise;
import com.fitforbusiness.Parse.Models.Group;
import com.fitforbusiness.Parse.Models.Measurements;
import com.fitforbusiness.Parse.Models.Session;
import com.fitforbusiness.Parse.Models.SessionExercise;
import com.fitforbusiness.Parse.Models.SessionMeasurements;
import com.fitforbusiness.Parse.Models.SessionStatus;
import com.fitforbusiness.Parse.Models.SessionWorkout;
import com.fitforbusiness.Parse.Models.Status;
import com.fitforbusiness.Parse.Models.Trainer;
import com.fitforbusiness.Parse.Models.Workout;
import com.fitforbusiness.database.DBOHelper;
import com.fitforbusiness.database.DatabaseHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.framework.CustomAsyncTaskListAdapter;
import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.nafc.Notify;
import com.fitforbusiness.nafc.R;
import com.fitforbusiness.nafc.calendar.NativeSync;
import com.fitforbusiness.webservice.Synchronise;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;

import java.io.ByteArrayOutputStream;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class AddSessionActivity extends ActionBarActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private EditText title, venue, notes;
    private Button repeat, statusButton;
    private Button selectWorkout;
    private Button startDate;
    private Button endDate;
    private Button startTime;
    private Button endTime;
    private List<HashMap<String, Object>> mapWorkoutArray;
    private List<HashMap<String, Object>> mapStatusArray;
    private String[] statusNames;
    private ListPopupWindow statusPopUp, repeatPopUp;
    private String sessionId = "";
    private int sessionType = -1;
    private String groupClientId = "-1";
    private String workoutId = "-1";
    private SimpleAdapter workoutAdapter;
    private SimpleAdapter statusAdapter;
    private int statusId = 0;
    private int repeatId = 0;
    private Bitmap selectBitmap;
    private String imageName;

    @Override
    public void onStart() {
        super.onStart();
        Appboy.getInstance(this).openSession(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        Appboy.getInstance(this).closeSession(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_session);
        try {
            sessionType = getIntent().getIntExtra(Utils.ARG_GROUP_OR_CLIENT, -1);
            groupClientId = getIntent().getStringExtra(Utils.ARG_GROUP_OR_CLIENT_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        title = (EditText) findViewById(R.id.etTitle);
        venue = (EditText) findViewById(R.id.etVenue);
        notes = (EditText) findViewById(R.id.etNotes);
        startDate = (Button) findViewById(R.id.bStartDate);
        endDate = (Button) findViewById(R.id.bEndDate);
        startTime = (Button) findViewById(R.id.bStartTime);
        endTime = (Button) findViewById(R.id.bEndTime);
        statusButton = (Button) findViewById(R.id.spnStatus);
        repeat = (Button) findViewById(R.id.spnRepeat);
        selectWorkout = (Button) findViewById(R.id.spnWorkout);
        setParseWorkoutList();
        Button setGoals = (Button) findViewById(R.id.bSetGoals);
        setGoals.setOnClickListener(this);

        startDate.setOnClickListener(this);
        endDate.setOnClickListener(this);
        startTime.setOnClickListener(this);
        endTime.setOnClickListener(this);
        selectWorkout.setOnClickListener(this);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        SimpleDateFormat stf = new SimpleDateFormat("HH:mm:ss");
        String formattedTime = stf.format(new Date());

        startDate.setText(sdf.format(new Date()));
        endDate.setText(sdf.format(new Date()));
        startTime.setText(Utils.timeFormatAMPM(formattedTime));
        endTime.setText(Utils.timeFormatAMPM(formattedTime));

      //  sessionId = "";

//        if (mapWorkoutArray.size() > 0) {
//            addDefaultSessionMeasurements(sessionId);
//        } else {
//            Log.d("addDefaultMeasurements", "");
//        }
        setStatusPopUp();
        setRepeatPopUp();
        statusButton.setOnClickListener(this);
        repeat.setOnClickListener(this);
        repeat.setText(getResources().getStringArray(R.array.repeat)[repeatId]);
        statusButton.setText(getResources().getStringArray(R.array.status)[statusId]);
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

    private void setStatusPopUp(){
        mapStatusArray = new ArrayList<HashMap<String, Object>>();
        ParseQuery parseQuery = new ParseQuery(Status.class);
        parseQuery.fromLocalDatastore();
        parseQuery.findInBackground(new FindCallback<Status>() {
            @Override
            public void done(List<Status> list, com.parse.ParseException e) {
                if (e == null && list != null) {
                    if (list.size() == 0) {
                        ParseQuery parseQuery = new ParseQuery(Status.class);
                        parseQuery.findInBackground(new FindCallback<Status>() {
                            @Override
                            public void done(List<Status> list, com.parse.ParseException e) {
                                if (e == null && list != null) {
                                    loadIntoStatusListView(list);
                                    Status.pinAllInBackground(list);
                                }
                            }
                        });
                    } else {
                        loadIntoStatusListView(list);
                    }
                }
            }
        });

    }
    int j=0;
    private void loadIntoStatusListView(List<Status> list) {
        LinkedHashMap<String, Object> row;
        statusNames=new String[list.size()];
        j=0;
        for (Status status : list) {

            row = new LinkedHashMap<String, Object>();
            row.put("_id", status.getObjectId());
            row.put("name", status.getName());
            mapStatusArray.add(row);
            statusNames[j]=status.getName();
            j++;
        }
        statusPopUp = new ListPopupWindow(this);
        statusPopUp.setAnchorView(statusButton);
        statusPopUp.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                statusNames));
        statusPopUp.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                statusId = position;
                statusButton.setText(statusNames[position]);
                statusPopUp.dismiss();
            }
        });

    }
//    private void setStatusPopUp() {
//        statusPopUp = new ListPopupWindow(this);
//        statusPopUp.setAnchorView(statusButton);
//        statusPopUp.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
//                getResources().getStringArray(R.array.status)));
//        statusPopUp.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                statusId = position;
//                statusButton.setText(getResources().getStringArray(R.array.status)[position]);
//                statusPopUp.dismiss();
//            }
//        });
//    }
    private void setParseWorkoutList(){
        mapWorkoutArray = new ArrayList<HashMap<String, Object>>();

        ParseQuery parseQuery = new ParseQuery(Workout.class);
        parseQuery.fromLocalDatastore();
        parseQuery.findInBackground(new FindCallback<Workout>() {
            @Override
            public void done(List<Workout> list, com.parse.ParseException e) {
                if (e == null && list != null) {
                    if (list.size() == 0) {
                        ParseQuery parseQuery = new ParseQuery(Workout.class);
                        parseQuery.findInBackground(new FindCallback<Workout>() {
                            @Override
                            public void done(List<Workout> list, com.parse.ParseException e) {
                                if (e == null && list != null) {
                                    loadIntoClientListView(list);
                                }
                            }
                        });
                    } else {
                        loadIntoClientListView(list);
                    }
                }
            }
        });

    }

    private void loadIntoClientListView(List<Workout> list) {
        LinkedHashMap<String, Object> row;
        for (Workout workout : list) {
            row = new LinkedHashMap<String, Object>();
            row.put("_id", workout.getObjectId());
            row.put("name", workout.getName());
            mapWorkoutArray.add(row);

            workoutAdapter = new SimpleAdapter(this, mapWorkoutArray,
                    android.R.layout.simple_list_item_1, new String[]{"name"}, new int[]{android.R.id.text1});

        }

    }

    private void setWorkoutList() {

        mapWorkoutArray = new ArrayList<>();

        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();

            String query = "select _id, name "
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
                row.put("_id", cursor.getString(0));

                row.put("name", cursor.getString(1));


                mapWorkoutArray.add(row);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        workoutAdapter = new SimpleAdapter(this, mapWorkoutArray,
                android.R.layout.simple_list_item_1, new String[]{"name"}, new int[]{android.R.id.text1});


//        setGoals
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
//                DBOHelper.delete(Table.SessionMeasurements.TABLE_NAME,
//                        Table.SessionMeasurements.SESSION_ID, sessionId);
//                addDefaultSessionMeasurements(sessionId);
                popup.dismiss();
            }
        });
        popup.show();

    }

    private long addDefaultSession() {

        ContentValues contentValues = new ContentValues();
        contentValues.put(Table.Sessions.TITLE, title.getText().toString());
        contentValues.put(Table.Sessions.VENUE, venue.getText().toString());
        contentValues.put(Table.Sessions.START_DATE, Utils.formatConversionLocale(startDate.getText().toString()));
        contentValues.put(Table.Sessions.END_DATE, Utils.formatConversionLocale(endDate.getText().toString()));
        contentValues.put(Table.Sessions.START_TIME, Utils.timeFormat24(startTime.getText().toString()));
        contentValues.put(Table.Sessions.END_TIME, Utils.timeFormat24(endTime.getText().toString()));
        contentValues.put(Table.Sessions.SESSION_TYPE, sessionType);
        contentValues.put(Table.Sessions.SESSION_STATUS, statusId);
        contentValues.put(Table.Sessions.GROUP_ID, groupClientId);
        contentValues.put(Table.Sessions.NOTES, notes.getText().toString());
        contentValues.put(Table.Sessions.PACKAGE_ID, new Date().getTime() + "");
        contentValues.put(Table.SYNC_ID, UUID.randomUUID().toString());
        long rowId = DBOHelper.insert(this, Table.Sessions.TABLE_NAME, contentValues);
        if (rowId > 0) {
            Log.d("Inserted in session", rowId + "");
        }
        return rowId;
    }
    private Date convertStringToDate(String strDate){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        Date convertedDate = new Date();
        try {
            convertedDate = dateFormat.parse(strDate);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return  convertedDate;
    }

    private void  addParseDefaultSession() {
        Session session=new Session();
        session.setTitle(title.getText().toString());
        session.setVenue(venue.getText().toString());
        Log.d("startDate", Utils.formatConversionLocale(startDate.getText().toString()));
        session.setStartDate(Utils.formatConversionLocale(startDate.getText().toString()));
        session.setEndDate(Utils.formatConversionLocale(endDate.getText().toString()));
        session.setStartTime(Utils.timeFormat24(startTime.getText().toString()));
        session.setEndTime(Utils.timeFormat24(endTime.getText().toString()));
        session.setSessionType(sessionType);
        session.setTrainer(Trainer.getCurrent());
        session.setRecurrenceRule(repeat.getText().toString());
        session.setGroupClientID(groupClientId);
        session.setStatus(statusId);

        SessionStatus sessionStatus=new SessionStatus();
        sessionStatus.setStatus(mapStatusArray.get(statusId).get("name").toString());

        try{
            sessionStatus.save();
            sessionStatus.pin();
        }catch (com.parse.ParseException ex){
        }

        session.setSessionStatus(sessionStatus);

        Log.v("sessionStatusSaving:", "kkk1");

        session.setNotes(notes.getText().toString());
        session.setPackageID(new Date().getTime() + "");

        if (workoutId!=null && !workoutId.equals("")) {
            session.setWorkout(workoutId);
            if (getWorkout(workoutId) != null) {
//                addParseSessionWorkout(session, getWorkout(workoutId));
                final Workout workout=getWorkout(workoutId);
                final SessionWorkout sessionWorkout=new SessionWorkout();
                sessionWorkout.setSession(session);
                sessionWorkout.setWorkout(workout);
                try{
                    sessionWorkout.save();
                    sessionWorkout.pin();
                }catch (com.parse.ParseException ex){
                }

                session.setSessionWorkout(sessionWorkout);

//                new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    if (workout.getExercises()!= null && workout.getExercises().size()!=0) {
////                      ParseRelation<SessionExercise> relation=sessionWorkout.getRelation("sessionExercises");
//                        for (Exercise exercise : workout.getExercises()) {
//                            SessionExercise sessionExercise=addParseSessionExercise(sessionWorkout, exercise);
////                       relation.add(sessionExercise);
//                            Log.v("sessionStatusSaving:", "kkk2.2");
//                        }
//                    }
//                }
//                }).start();
            }

        }
        Log.v("sessionStatusSaving:", "kkk2");
        if (sessionType==1) {
//            ParseRelation<Session> relation=listGroup.get(0).getRelation("sessions");
//            relation.add(session);
//            listGroup.get(0).saveInBackground();
            Group group=Group.createWithoutData(Group.class,groupClientId);
            group.setSession(session);
            try{
                group.save();
                group.pin();
            }catch (com.parse.ParseException ex){
            }
            session.setGroup(group);
            Log.v("sessionStatusSaving:", "kkk3");
        }else{
            Client client=Client.createWithoutData(Client.class,groupClientId);
            Log.v("client.getName","kkk"+client.getFirstName());
            client.setSession(session);
            try{
                client.save();
                client.pin();
            }catch (com.parse.ParseException ex){
            }
            session.setClient(client);
            Log.v("sessionStatusSaving:", "kkk4");
        }

        Log.v("sessionStatusSaving:", "kkk5.5");

        try {
            session.save();
            session.pin();
            Log.v("sessionStatusSaving:", "kkk5");
        }catch (com.parse.ParseException e){
        }

        Log.v("sessionStatusSaving:","kkk6");
    }

    private static String workoutID_;
    private Workout getWorkout(String workoutId){
        workoutID_=workoutId;
        List<Workout> list=new ArrayList<Workout>();
        ParseQuery parseQuery = new ParseQuery(Workout.class);
        parseQuery.fromLocalDatastore();
        parseQuery.whereEqualTo("objectId", workoutId);
        try {
            list=parseQuery.find();
        }catch (com.parse.ParseException e){
        }
        if (list!=null && list.size()!=0){
            parseQuery.fromLocalDatastore();
            parseQuery.whereEqualTo("objectId", workoutId);
            try {
                list=parseQuery.find();
            }catch (com.parse.ParseException e){
            }
        }
        return list.get(0);
    }
    private void addParseSessionWorkout(Session session,Workout workout){
        SessionWorkout sessionWorkout=new SessionWorkout();
        sessionWorkout.setSession(session);
        sessionWorkout.setWorkout(workout);
        SessionWorkout returnValue=sessionWorkout;
        if (workout.getExercises()!=null && workout.getExercises().size()!=0) {
//            ParseRelation<SessionExercise> relation=sessionWorkout.getRelation("sessionExercises");
            for (Exercise exercise : workout.getExercises()) {
                SessionExercise sessionExercise=addParseSessionExercise(sessionWorkout, exercise);
//                relation.add(sessionExercise);
            }
        }
        sessionWorkout.saveInBackground();
        sessionWorkout.pinInBackground();
    }

    private SessionExercise addParseSessionExercise(SessionWorkout sessionWorkout, Exercise exercise){
        SessionExercise sessionExercise=new SessionExercise();
        sessionExercise.setExercise(exercise);
        sessionExercise.setSessionWorkout(sessionWorkout);
        SessionExercise returnvalue=sessionExercise;
//        sessionExercise.saveInBackground();
//        sessionExercise.pinInBackground();
        try {
            sessionExercise.save();
            sessionExercise.pin();
        }catch (com.parse.ParseException ex){
        }
          ParseRelation<SessionMeasurements> relation=sessionExercise.getRelation("sessionMeasurements");
            for (Measurements measurement : exercise.getMeasurements()) {
             SessionMeasurements sessionMeasurement=this.addParseSessionMeasurements(sessionExercise, measurement);
           // relation.add(sessionMeasurement);
            }

        Log.v("sessionStatusSaving:", "kkk2.1");
        return returnvalue;
    }

    private SessionMeasurements addParseSessionMeasurements(SessionExercise sessionExercise, Measurements measurement){
        SessionMeasurements sessionMeasurement=new SessionMeasurements();
        sessionMeasurement.setMeasurements(measurement);
        sessionMeasurement.setSessionExercise(sessionExercise);
        try{
            sessionMeasurement.save();
            sessionMeasurement.pin();
        }catch (com.parse.ParseException ex){
        }
//        sessionMeasurement.saveInBackground();
//        sessionMeasurement.pinInBackground();
       return sessionMeasurement;
    }

//    private void addDefaultParseSessionMeasurements(Workout workout){
//        ArrayList<Exercise> exercises=new ArrayList<Exercise>();
//        exercises=workout.getExercises();
//        ArrayList<Measurements> measurementses=new ArrayList<Measurements>();
//        for (Exercise exercise:exercises) {
//            measurementses = exercise.getMeasurements();
//
//        }
//    }


    private void addDefaultSessionMeasurements(String session_id) {
        SQLiteDatabase sqLiteDatabase;
        sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select exercise_id,measurement_id" +
                " from exercise_measurements where exercise_id in" +
                "(select exercise_id from workout_exercises where workout_id=" + workoutId + ")", null);

//        Log.d("workoutTest", "query  = " + "select exercise_id,measurement_id" +
//                " from exercise_measurements where exercise_id in" +
//                "(select exercise_id from workout_exercises where workout_id=" + workoutId + ")");
//
//        Cursor c = sqLiteDatabase.rawQuery("select exercise_id from workout_exercises where workout_id=" + workoutId, null);
//        Log.d("workoutTest", c.getCount() + "");
//        if (c.getCount() > 0) {
//            c.moveToFirst();
//            do {
//                Log.d("workoutTest", c.getInt(0) + "");
//            } while (c.moveToNext());
//        } else {
//            Log.d("workoutTest", "No workout found id = " + workoutId);
//        }
//        c.close();

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                ContentValues contentValues = new ContentValues();
                Log.d("workoutTest", "Added one with id = " + workoutId);
                contentValues.put(Table.SessionMeasurements.SESSION_ID, session_id);
                contentValues.put(Table.SessionMeasurements.WORKOUT_ID, workoutId);
                contentValues.put(Table.SessionMeasurements.EXERCISE_ID, cursor.getInt(0));
                contentValues.put(Table.SessionMeasurements.MEASUREMENT_ID, cursor.getInt(1));
                contentValues.put(Table.SessionMeasurements.SET_NO, 1);
                contentValues.put(Table.SYNC_ID, UUID.randomUUID().toString());
                Log.d("inserted measurement id", "" + DBOHelper.insert(this,
                        Table.SessionMeasurements.TABLE_NAME, contentValues));
            } while (cursor.moveToNext());
        } else {
            Log.d("workoutTest", "No workout found id = " + workoutId);
        }
        cursor.close();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_session, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.bSave) {
            if (Utils.validateFields(title) &&
                    Utils.validateFields(startDate) &&
                    Utils.validateFields(endDate) &&
                    Utils.validateFields(startTime) && Utils.validateFields(endTime)) {

//                long isUpdated = updateSession();
                   addParseDefaultSession();
//                if (isUpdated > 0 && repeatId == 0) {
                    if (repeatId == 0){
//                    if (Utils.isNetworkAvailable(this)) {
//                        createSessionOnServer(sessionId);
//                    }
                    Appboy.getInstance(this).logCustomEvent(Utils.CUSTOM_EVENT_KEY_CREATE_SESSION);
                }
                switch (repeatId) {
                    case 0:
                        break;
                    case 1:
                        createRepeatingSession(Calendar.DATE);
                        break;
                    case 2:
                        createRepeatingSession(Calendar.WEEK_OF_MONTH);
                        break;
                    case 3:
                        createRepeatingSession(Calendar.MONTH);
                        break;
                }

//                if (repeatId != 0) {
//                    DBOHelper.delete(Table.Sessions.TABLE_NAME, Table.Sessions.ID, sessionId);
//                }
//
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        ArrayList<HashMap<String, Object>> maps = NativeSync.getAllEvents();
//
//                        for (HashMap<String, Object> map : maps)
//                            NativeSync.addEventToNative(
//                                    getApplicationContext(), map);
//                    }
//                }).start();
                Log.v("sessionStatusSaving:", "kkk7");
                finish();
            }

        } else if (id == android.R.id.home) {
//            DBOHelper.delete(Table.SessionMeasurements.TABLE_NAME,
//                    Table.SessionMeasurements.SESSION_ID, sessionId);
//            DBOHelper.delete(Table.Sessions.TABLE_NAME, Table.Sessions.ID, sessionId);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.spnWorkout:
                showListPopUp(selectWorkout, workoutAdapter);
                break;
            case R.id.bSetGoals:
                if (mapWorkoutArray.size() > 0) {
                    startActivity(new Intent(this, SetGoalsActivity.class).putExtra("workout_id",
                            workoutId
                    ).putExtra("session_id", sessionId));
                } else {
                    Toast.makeText(this, "No workout Selected!", Toast.LENGTH_SHORT).show();
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
            case R.id.spnRepeat:
                repeatPopUp.show();
                break;
            case R.id.spnStatus:
                statusPopUp.show();
                break;
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
                        if (mButton == startDate)
                            endDate.setText(formattedDate);
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
                if (mButton == startTime) {
                    c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) + 60);
                    formattedDate = sdf.format("hh:mm a", c).toString();
                    endTime.setText(formattedDate);
                }
            }
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false);
        dateDlg.show();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        DBOHelper.delete(Table.SessionMeasurements.TABLE_NAME,
                Table.SessionMeasurements.SESSION_ID, sessionId);
      //  addDefaultSessionMeasurements(sessionId);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private long updateSession() {

        ContentValues contentValues = new ContentValues();
        contentValues.put(Table.Sessions.TITLE, title.getText().toString());
        contentValues.put(Table.Sessions.VENUE, venue.getText().toString());
        contentValues.put(Table.Sessions.START_DATE, Utils.formatConversionLocale(startDate.getText().toString()));
        contentValues.put(Table.Sessions.END_DATE, Utils.formatConversionLocale(endDate.getText().toString()));
        contentValues.put(Table.Sessions.START_TIME, Utils.timeFormat24(startTime.getText().toString()));
        contentValues.put(Table.Sessions.END_TIME, Utils.timeFormat24(endTime.getText().toString()));
        contentValues.put(Table.Sessions.SESSION_TYPE, sessionType);
        contentValues.put(Table.Sessions.SESSION_STATUS, statusId);
        contentValues.put(Table.Sessions.PACKAGE_ID, workoutId);
        contentValues.put(Table.Sessions.GROUP_ID, groupClientId);
        contentValues.put(Table.Sessions.NOTES, notes.getText().toString());
        long rowId = DBOHelper.update(Table.Sessions.TABLE_NAME, contentValues, sessionId);
        if (rowId > 0) {
            Log.d("Updated   session", rowId + "");
            setUpNotification(contentValues, Integer.parseInt(sessionId));
        }
        return rowId;
    }

    void setUpNotification(ContentValues contentValues, int sessionId){
        try {
            Log.d("AddSession:sessionAlert", (String) contentValues.get(Table.Sessions.START_DATE));
            Date notificationDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
                    .parse((String) contentValues.get(Table.Sessions.START_DATE));
            Time time = Time.valueOf((String) contentValues.get(Table.Sessions.START_TIME));

            notificationDate.setHours(time.getHours());
            notificationDate.setMinutes(time.getMinutes());
            notificationDate.setSeconds(time.getSeconds());

            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(getBaseContext());
            String listPreference = prefs.getString("session_alert", "30");
            int milliSecondsToAlert = Integer.valueOf(listPreference) * 60 * 1000;
            Log.d("AddSession:sessionAlert", "setting = " + milliSecondsToAlert);

            if (milliSecondsToAlert < 0) return;

            notificationDate.setTime(notificationDate.getTime() - milliSecondsToAlert);
            Log.d("AddSession:sessionAlert", "" + notificationDate);

            Date c = new Date(System.currentTimeMillis());

            long delay = notificationDate.getTime() - c.getTime();
            Log.d("AddSession:sessionAlert", "delay = " + delay);

            if (delay < -milliSecondsToAlert) return;

            String name = sessionType == Utils.FLAG_CLIENT ?
                    loadClientName(groupClientId): loadGroupName(groupClientId);
            String type = sessionType == Utils.FLAG_CLIENT ?
                    "Client" : "Group";

            Notification n = Notify.getNotification(AddSessionActivity.this,
                    String.format(getString(R.string.text_notification), type, name));

            Notify.scheduleNotification(AddSessionActivity.this, n,
                    delay, sessionId);
            Log.d("AddSession:sessionAlert", "Done!");
        } catch (ParseException e) {
            e.printStackTrace();
            Log.d("AddSession:sessionAlert", "error");
        }
    }

    private long createRepeatingSession(int frequency) {
        Date sDate = null;
        Date eDate = null;
        try {
            sDate = new SimpleDateFormat("dd MMM yyyy").parse(startDate.getText().toString());
            eDate = new SimpleDateFormat("dd MMM yyyy").parse(endDate.getText().toString());

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sDate);
            calendar.add(frequency, 1);
            sDate = calendar.getTime();
            while (eDate.after(sDate) || eDate.equals(sDate)) {
                addParseSession(sDate);
//                 addSession(sDate);
//                if (id > 0) {
//                    addSessionMeasurements(id + "");
//                    if (Utils.isNetworkAvailable(this)) {
//                        createSessionOnServer(id + "");
//                    }
                    calendar.add(frequency, 1);
                    sDate = calendar.getTime();
//                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 1;
    }

    private void addParseSession(Date sDate){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(sDate);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        String formattedDate = sdf.format(calendar.getTime());

        Session session=new Session();
        session.setTitle(title.getText().toString());
        session.setVenue(venue.getText().toString());
        Log.d("startDate", Utils.formatConversionLocale(formattedDate.toString()));
        session.setStartDate(Utils.formatConversionLocale(formattedDate.toString()));
        session.setEndDate(Utils.formatConversionLocale(formattedDate.toString()));
        session.setStartTime(Utils.timeFormat24(startTime.getText().toString()));
        session.setEndTime(Utils.timeFormat24(endTime.getText().toString()));
        session.setSessionType(sessionType);
        session.setTrainer(Trainer.getCurrent());
        session.setRecurrenceRule(repeat.getText().toString());
        session.setGroupClientID(groupClientId);
        session.setStatus(statusId);
        session.setNotes(notes.getText().toString());
        session.setPackageID(new Date().getTime()+"");
        session.setWorkout(workoutId);
        session.pinInBackground();
        session.saveEventually();
    }
    private long addSession(Date sDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(sDate);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        String formattedDate = sdf.format(calendar.getTime());
        SQLiteDatabase sqLiteDatabase = null;
        try {
            sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("select * from "
                    + Table.Sessions.TABLE_NAME + " where _id=" + sessionId, null);
            if (cursor.moveToFirst()) {

                ContentValues contentValues = new ContentValues();
                contentValues.put(Table.Sessions.TITLE,
                        cursor.getString(cursor.getColumnIndex(Table.Sessions.TITLE)));
                contentValues.put(Table.Sessions.VENUE,
                        cursor.getString(cursor.getColumnIndex(Table.Sessions.VENUE)));
                contentValues.put(Table.Sessions.START_DATE,
                        Utils.formatConversionLocale(formattedDate.toString()));
                contentValues.put(Table.Sessions.END_DATE,
                        Utils.formatConversionLocale(formattedDate.toString()));
                contentValues.put(Table.Sessions.START_TIME,
                        cursor.getString(cursor.getColumnIndex(Table.Sessions.START_TIME)));
                contentValues.put(Table.Sessions.END_TIME,
                        cursor.getString(cursor.getColumnIndex(Table.Sessions.END_TIME)));
                contentValues.put(Table.Sessions.SESSION_TYPE,
                        cursor.getString(cursor.getColumnIndex(Table.Sessions.SESSION_TYPE)));
                contentValues.put(Table.Sessions.SESSION_STATUS,
                        cursor.getInt(cursor.getColumnIndex(Table.Sessions.SESSION_STATUS)));
                contentValues.put(Table.Sessions.GROUP_ID,
                        cursor.getString(cursor.getColumnIndex(Table.Sessions.GROUP_ID)));
                contentValues.put(Table.Sessions.NOTES,
                        cursor.getString(cursor.getColumnIndex(Table.Sessions.NOTES)));
                contentValues.put(Table.Sessions.PACKAGE_ID,
                        cursor.getString(cursor.getColumnIndex(Table.Sessions.PACKAGE_ID)));
                contentValues.put(Table.SYNC_ID,
                        UUID.randomUUID().toString());
                long rowId = DBOHelper.insert(this, Table.Sessions.TABLE_NAME, contentValues);
                if (rowId > 0) {
                    Log.d("Inserted in session", rowId + "");
                    setUpNotification(contentValues, (int) rowId);
                }
                return rowId;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return 1;
    }


    private void addSessionMeasurements(String session_id) {
        SQLiteDatabase sqLiteDatabase = null;
        sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select * from " + Table.SessionMeasurements.TABLE_NAME +
                " where session_id = " + sessionId, null);
        while (cursor.moveToNext()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Table.SessionMeasurements.SESSION_ID, session_id);
            contentValues.put(Table.SessionMeasurements.WORKOUT_ID, workoutId);
            contentValues.put(Table.SessionMeasurements.EXERCISE_ID, cursor.getString(cursor.getColumnIndex(Table.SessionMeasurements.EXERCISE_ID)));
            contentValues.put(Table.SessionMeasurements.MEASUREMENT_ID, cursor.getString(cursor.getColumnIndex(Table.SessionMeasurements.MEASUREMENT_ID)));
            contentValues.put(Table.SessionMeasurements.SET_NO, cursor.getString(cursor.getColumnIndex(Table.SessionMeasurements.SET_NO)));
            contentValues.put(Table.SessionMeasurements.MEASURED_VALUE, cursor.getString(cursor.getColumnIndex(Table.SessionMeasurements.MEASURED_VALUE)));
            contentValues.put(Table.SessionMeasurements.TARGET_VALUE, cursor.getString(cursor.getColumnIndex(Table.SessionMeasurements.TARGET_VALUE)));
            contentValues.put(Table.SYNC_ID, UUID.randomUUID().toString());
            Log.d("inserted measurement id", "" + DBOHelper.insert(this,
                    Table.SessionMeasurements.TABLE_NAME, contentValues));
        }
    }

    private void createSessionOnServer(final String _id) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Synchronise.Session syncSession = new Synchronise.Session(AddSessionActivity.this,
                        Utils.getLastClientSyncTime(AddSessionActivity.this));
                syncSession.createOnServer(Synchronise.getDeviceObjectById(_id, syncSession.getDeviceSessionsSet()));
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Log.d("Create Session", "Session created on the server");
            }
        }.execute();
    }

//    ___________  Helper functions __________
private String loadGroupName(String _id) {
    SQLiteDatabase sqlDB = null;
    String groupName = "";
    try {
        sqlDB = DatabaseHelper.instance().getReadableDatabase();
        String query = "select  * "
                + " from " +
                Table.Group.TABLE_NAME +
                " where " + Table.DELETED + " = 0 and " + Table.ID + " =  " + _id;
        Log.d("query is ", query);
        Cursor cursor = sqlDB != null ? sqlDB
                .rawQuery(query
                        , null) : null;
        if (cursor != null && cursor.moveToNext()) {
            groupName = cursor.getString(cursor
                    .getColumnIndex(Table.Group.NAME));
        }
        assert cursor != null;
        cursor.close();
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
    }

    return groupName;
}
    private String loadClientName(String client_id) {
        SQLiteDatabase sqlDB = null;
        String firstName = "", lastName = "";
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            String query = "select  * "
                    + " from " +
                    Table.Client.TABLE_NAME +
                    " where " + Table.DELETED + " = 0 and " + Table.ID + " =  " + client_id;
            Log.d("query is ", query);
            Cursor cursor = sqlDB
                    .rawQuery(query
                            , null);
            if (cursor.moveToNext()) {
                firstName = cursor.getString(cursor
                        .getColumnIndex(Table.Client.FIRST_NAME));
                lastName = cursor.getString(cursor
                        .getColumnIndex(Table.Client.LAST_NAME));
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return firstName + " " + lastName;
    }
}
