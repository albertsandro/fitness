package com.fitforbusiness.nafc;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.fitforbusiness.Parse.Models.Exercise;
import com.fitforbusiness.Parse.Models.Measurements;
import com.fitforbusiness.Parse.Models.Trainer;
import com.fitforbusiness.Parse.Models.UnitMetrics;
import com.fitforbusiness.database.DBOHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.stripe.compat.AsyncTask;
import com.fitforbusiness.webservice.ProgramManagementWebService;
import com.fitforbusiness.webservice.WebService;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseObject;
import com.parse.SignUpCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Sanjeet on 5/26/14.
 */
public class RegisterActivity extends ActionBarActivity {
    ProgressDialog progressDialog;
    private EditText email, password, passwordAgain, firstName, lastName, phoneNumber;
    private RadioButton genderMale, genderFemale;
    private Button register, cancel, dob;
    private static String trainerWebServiceURL = Utils.BASE_URL + Utils.TRAINING_APP_SERVICE;
    private static String programManagementWebServiceURL = Utils.BASE_URL
            + Utils.PROGRAM_MANAGEMENT_SERVICE;
    private HashMap<Integer, Exercise> defaultExercises;
    private Measurements measurementsDistance, measurementsReps, measurementsWeight, measurementsTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_register);

        try {
            Utils.createLocalResourceDirectory(getApplicationContext());
            Utils.createThumbNailDirectory(getApplicationContext());
            Utils.createProfileImageDirectory(getApplicationContext());
        } catch (Exception ex) {
            Log.d("Directory not created", "");
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        email = (EditText) findViewById(R.id.etUserName);
        password = (EditText) findViewById(R.id.etPassword);
        passwordAgain = (EditText) findViewById(R.id.etPasswordAgain);
        password.setTypeface(Typeface.DEFAULT);
        password.setTransformationMethod(new PasswordTransformationMethod());
        passwordAgain.setTypeface(Typeface.DEFAULT);
        passwordAgain.setTransformationMethod(new PasswordTransformationMethod());
        firstName = (EditText) findViewById(R.id.etFirstName);
        lastName = (EditText) findViewById(R.id.etLastName);
        phoneNumber = (EditText) findViewById(R.id.etPhoneNo);
        dob = (Button) findViewById(R.id.bDateOfBirth);
        genderMale = (RadioButton) findViewById(R.id.radGenderMale);
        genderFemale = (RadioButton) findViewById(R.id.radGenderFemale);
        register = (Button) findViewById(R.id.btnCreate);
        cancel = (Button) findViewById(R.id.btnCancel);
        dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  Utils.showToast(RegisterActivity.this, "Error : Cannot RegisterActivity ,");
                showAlert(0);
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utils.isNetworkAvailable(RegisterActivity.this)) {
                    if (Utils.isValidEmail(email)) {
                        if (password.getText().toString().equals(passwordAgain.getText().toString())) {
                            if (Utils.validateFields(password)
                                    && Utils.validateFields(passwordAgain)
                                    && Utils.validateFields(firstName)
                                    && Utils.validateFields(lastName)
                                    && Utils.validateFields(phoneNumber)
                                    && (genderFemale.isChecked() || genderMale.isChecked())) {

                               /* webInterface = new WebInterface(RegisterActivity.this, getParams(), regUrl,
                                        "CreateTrainer", "CreateTrainerResult");
                                webInterface.execute();*/
//                                registerUser();
                                registerUserOnParse();
                            }
                        } else
                            passwordAgain.setError("Password Didn't match.!");
                    } else {
                        email.setError("Invalid Email Address");
                    }
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void registerUserOnParse () {
        // Check if user exists on AWS
        HashMap<String, Object> params = new HashMap<>();
        params.put("Email", email.getText().toString());
        params.put("Enviornment", Utils.ENV);

        ParseCloud.callFunctionInBackground("UserExistsOnAWS", params, new FunctionCallback<String>() {
            @Override
            public void done(String response, com.parse.ParseException e) {
                if (e == null)
                    try {
                        JSONObject result = new JSONObject(response);
                        boolean exists = result.getBoolean("UserExistsResult");
//                        If Not exists then try regitering on parse
                        if (!exists) {
                            final Trainer trainer = new Trainer();
                            trainer.setEmail(email.getText().toString());
                            trainer.setFirstName(firstName.getText().toString());
                            trainer.setLastName(lastName.getText().toString());
                            trainer.setPhone(phoneNumber.getText().toString());

                            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");

                            try {
                                trainer.setDateOfBirth(sdf.parse(dob.getText().toString()));
                            } catch (ParseException e1) {
                                e1.printStackTrace();
                            }
                            trainer.setParseUser(true);
                            trainer.setGender(genderMale.isChecked());
                            trainer.setUsername(email.getText().toString());
                            trainer.setPassword(password.getText().toString());
                            trainer.signUpInBackground(new SignUpCallback() {
                                @Override
                                public void done(com.parse.ParseException e) {
                                    if (e == null) {
                                        new Handler().post(new Runnable() {
                                            @Override
                                            public void run() {
                                                SetUpForParse();
                                            }
                                        });
                                        trainer.pinInBackground();
                                    } else {
                                        Toast.makeText(getBaseContext(), e.getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                            // Else Show message
                        } else {
                            Toast.makeText(getBaseContext(), getString(R.string.alreadyOnAws),
                                    Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                else {
                    Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void registerUser() {

        new AsyncTask<Map<String, Object>, Void, JSONObject>() {
            @Override
            protected void onPreExecute() {
                progressDialog = new ProgressDialog(RegisterActivity.this);
                progressDialog.setMessage("Please Wait...");
                progressDialog.setCancelable(false);
                progressDialog.show();
                super.onPreExecute();
            }

            @Override
            protected JSONObject doInBackground(Map<String, Object>... params) {
                JSONObject json = null;
                try {

                    WebService w = new WebService();
                    String response = w.webInvoke(trainerWebServiceURL, "CreateTrainer", getParams()
                    );
                    if (response != null) {
                        Log.d("CreateTrainerResult", response);
                        json = new JSONObject(response);
                    }
                } catch (Exception ex) {
                    System.out.println("Oops ! an error occurred while registering: " + ex.toString());
                }
                return json;
            }

            @Override
            protected void onPostExecute(JSONObject jsonObject) {
                super.onPostExecute(jsonObject);
                try {
                    progressDialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d("DefaultData", "Post create");
                if (jsonObject != null) {
                    Log.d("DefaultData", "Response not null!");
                    try {
                        jsonObject = jsonObject.getJSONObject("CreateTrainerResult");
                        int result = jsonObject.getInt("Result");
                        Log.d("DefaultData", "Result is = " + result);
                        if (result > 0) {
                            setUp(result + "");

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.execute();
    }

    private void SetUpForParse() {
        Toast.makeText(RegisterActivity.this, "Success!",
                Toast.LENGTH_SHORT).show();
        try {
            addDefaultUnits();
            addDefaultExercises();
        } catch (com.parse.ParseException e) {
            e.printStackTrace();
        }

        Utils.copyAssets(this, Utils.THUMBNAIL_PATH);
        Utils.copyAssets(this, Utils.PROFILE_THUMBNAIL_PATH);
        Utils.copyAssets(this, Utils.LOCAL_RESOURCE_PATH);

        startActivity(new Intent(RegisterActivity.this,
                MainActivity.class));
    }

    private void addDefaultUnits() throws com.parse.ParseException {
        UnitMetrics distance = new UnitMetrics();
        distance.setTrainer(Trainer.getCurrent());
        distance.setUnitAbbreviation("mi"); distance.setUnitName("Mile");

        UnitMetrics weight = new UnitMetrics();
        weight.setTrainer(Trainer.getCurrent());
        weight.setUnitAbbreviation("lb"); weight.setUnitName("Pound");

        UnitMetrics time = new UnitMetrics();
        time.setTrainer(Trainer.getCurrent());
        time.setUnitAbbreviation("m"); time.setUnitName("Minute");

        List metrics = new ArrayList();
        metrics.add(distance); metrics.add(weight); metrics.add(time);
        UnitMetrics.pinAll(metrics);
        UnitMetrics.saveAll(metrics);

        Log.e("saveTest", "units saved + " + time.getObjectId());

        measurementsDistance = new Measurements();
        measurementsDistance.setName("Distance");
        measurementsDistance.setTrainer(Trainer.getCurrent());
        //measurementsDistance.setUnitMetrics(distance);

        measurementsReps = new Measurements();
        measurementsReps.setName("Reps");
        measurementsReps.setTrainer(Trainer.getCurrent());

        measurementsWeight = new Measurements();
        measurementsWeight.setName("Weight");
      //  measurementsWeight.setUnitMetrics(weight);
        measurementsWeight.setTrainer(Trainer.getCurrent());

        measurementsTime = new Measurements();
        measurementsTime.setName("Time");
    //    measurementsTime.setUnitMetrics(time);
        measurementsTime.setTrainer(Trainer.getCurrent());

        List measurements = new ArrayList();
        measurements.add(measurementsDistance); measurements.add(measurementsWeight);
        measurements.add(measurementsTime); measurements.add(measurementsReps);
        Measurements.pinAll(measurements);
        Measurements.saveAll(measurements);

        Log.e("saveTest", "measurnments saved");
    }

    public void setUp(String trainerId) {

        try {
            addDefaultExercises();
        } catch (com.parse.ParseException e) {
            e.printStackTrace();
        }
        addDefaultWorkouts();
        Utils.copyAssets(this, Utils.THUMBNAIL_PATH);
        Utils.copyAssets(this, Utils.PROFILE_THUMBNAIL_PATH);
        Utils.copyAssets(this, Utils.LOCAL_RESOURCE_PATH);
        SharedPreferences settings = getSharedPreferences(Utils.TRAINER_PREFS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("hasLoggedIn", true);
        editor.putString("trainer_id", trainerId);
        editor.putBoolean("download_data", true);
        editor.putBoolean("default_setting_done", true);
        editor.commit();
        updateTrainerOnDevice(trainerId);
        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
    }



    private void updateTrainerOnDevice(String trainerId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Table.TrainerProfileDetails.TRAINER_ID, trainerId);
        contentValues.put(Table.TrainerProfileDetails.FIRST_NAME, firstName.getText().toString());
        contentValues.put(Table.TrainerProfileDetails.LAST_NAME, lastName.getText().toString());
        contentValues.put(Table.TrainerProfileDetails.EMAIL_ID, email.getText().toString());
        contentValues.put(Table.TrainerProfileDetails.PHONE_NO, phoneNumber.getText().toString());
        DBOHelper.insert(this, Table.TrainerProfileDetails.TABLE_NAME, contentValues);
        contentValues.put(Table.TrainerProfileDetails.GENDER, genderMale.isChecked() ? Utils.MALE : Utils.FEMALE);
        /* contentValues.put(Table.TrainerProfileDetails.FIRST_NAME,firstName.getText().toString());
        contentValues.put(Table.TrainerProfileDetails.FIRST_NAME,firstName.getText().toString());*/


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    private Map<String, Object> getParams() {
        Map<String, Object> params = new HashMap<String, Object>();
        Map<String, Object> details = new HashMap<String, Object>();
        details.put("EmailId", email.getText().toString());
        details.put("Password", password.getText().toString());
        //  params.put("passwordAgain", passwordAgain.getText().toString());
        details.put("FirstName", firstName.getText().toString());
        details.put("LastName", lastName.getText().toString());
        details.put("ContactNo", phoneNumber.getText().toString());
        details.put("DOB", dob.getText().toString());
        details.put("Gender", genderMale.isChecked() ? Utils.MALE : Utils.FEMALE);
        details.put("isTempPass", 0);
        params.put("Trainer", details);
        return params;
    }



    private void addDefaultsToServer() {
        final ProgramManagementWebService programManagementWebService =
                new ProgramManagementWebService(this, Utils.getLastSyncTime(this));
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                progressDialog = new ProgressDialog(RegisterActivity.this);
                progressDialog.setMessage("Please Wait...");
                progressDialog.setCancelable(false);
                progressDialog.show();
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... params) {
                programManagementWebService.addExercisesToServer();
                programManagementWebService.addWorkoutsToServer();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                try {
                    progressDialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                DBOHelper.clearExerciseAndWorkoutData();

            }
        }.execute();
    }

    void showAlert(int diaLogNo) {
        DatePickerDialog dateDlg = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        Calendar c = Calendar.getInstance();
                        c.set(year, monthOfYear, dayOfMonth);
                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
                        String formattedDate = sdf.format(c.getTime());
                        dob.setText(formattedDate);
                    }
                    /* Calendar
                            .getInstance().get(Calendar.HOUR_OF_DAY), Calendar
							.getInstance().get(Calendar.MINUTE), Calendar
							.getInstance().get(Calendar.SECOND))*/
                }, Calendar
                .getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar
                .getInstance().get(Calendar.DAY_OF_MONTH)
        );
        dateDlg.show();
    }

    private void addDefaultExercises() throws com.parse.ParseException {
        try {
            JSONObject jsonObject = new JSONObject(Utils.getJSONData(this, "default_exercises.json"));
            JSONArray jsonArray = jsonObject.getJSONArray("exercise");
            defaultExercises = new HashMap<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                Exercise parseExercise = new Exercise();
                JSONObject exercise = jsonArray.getJSONObject(i);

                defaultExercises.put(exercise.getInt(Table.Exercise.EXERCISE_ID), parseExercise);
                parseExercise.setName(exercise.getString(Table.Exercise.NAME));
                parseExercise.setMuscleGroup(exercise.getString(Table.Exercise.MUSCLE_GROUP));
                parseExercise.setTags(exercise.getString(Table.Exercise.DESCRIPTION));
                parseExercise.setTags(exercise.getString(Table.Exercise.TAG));
                parseExercise.setTrainer(Trainer.getCurrent());
//                exerciseContentValue.set exercise.getString(Table.Exercise.PHOTO_URL));
                JSONArray measurement = exercise.getJSONArray("measurement");
                for (int j = 0; j < measurement.length(); j++) {
                    JSONObject measurementObject = measurement.getJSONObject(j);
                    switch (measurementObject.getInt(Table.ExerciseMeasurements.MEASUREMENT_ID)) {
                        case 1:
                            parseExercise.addMeasurements(measurementsDistance);
                            break;
                        case 2:
                            parseExercise.addMeasurements(measurementsReps);
                            break;
                        case 3:
                            parseExercise.addMeasurements(measurementsTime);
                            break;
                        case 4:
                            parseExercise.addMeasurements(measurementsWeight);
                            break;
                    }
                }
                parseExercise.pin();
            }
            Log.e("saveTest", "Exercises pinned");
            Exercise.saveAll(new ArrayList<>(defaultExercises.values()));
            Log.e("saveTest", "Exercises saved");
        } catch (JSONException e) {
            Log.d("JSONException", e.toString());
            e.printStackTrace();
        }
    }

    private void addDefaultWorkouts() {
        try {
            JSONObject jsonObject = new JSONObject(Utils.getJSONData(this, "default_workouts.json"));
            JSONArray jsonArray = jsonObject.getJSONArray("workout");
            for (int i = 0; i < jsonArray.length(); i++) {
                ContentValues workoutContentValue = new ContentValues();
                JSONObject workout = jsonArray.getJSONObject(i);
                workoutContentValue.put(Table.Workout.NAME, workout.getString(Table.Workout.NAME));
                workoutContentValue.put(Table.Workout.DESCRIPTION, workout.getString(Table.Workout.DESCRIPTION));
                workoutContentValue.put(Table.Workout.PHOTO_URL, workout.getString(Table.Workout.PHOTO_URL));
                workoutContentValue.put(Table.SYNC_ID, UUID.randomUUID().toString());
                long rowId = DBOHelper.insert(this, Table.Workout.TABLE_NAME, workoutContentValue);
                if (rowId > 0) {
                    JSONArray exercise = workout.getJSONArray("exercise");
                    for (int j = 0; j < exercise.length(); j++) {
                        JSONObject exerciseObject = exercise.getJSONObject(j);
                        ContentValues exerciseContentValue = new ContentValues();
                        exerciseContentValue.put(Table.WorkoutExercises.WORKOUT_ID, rowId);
                        exerciseContentValue.put(Table.WorkoutExercises.EXERCISE_ID, DBOHelper.getDeviceExerciseId(exerciseObject.getString(Table.Exercise.EXERCISE_ID)));
                        DBOHelper.insert(this, Table.WorkoutExercises.TABLE_NAME, exerciseContentValue);
                    }
                }
            }
        } catch (JSONException e) {
            Log.d("JSONException", e.toString());
            e.printStackTrace();
        }
    }

}