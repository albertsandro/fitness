package com.fitforbusiness.nafc.accreditation;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.fitforbusiness.Parse.Models.Accreditation;
import com.fitforbusiness.Parse.Models.Group;
import com.fitforbusiness.Parse.Models.Trainer;
import com.fitforbusiness.database.DBOHelper;
import com.fitforbusiness.database.DatabaseHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.framework.view.MySwitch;
import com.fitforbusiness.oauth.dropbox.DownloadFile;
import com.fitforbusiness.oauth.dropbox.DropBoxFileList;
import com.fitforbusiness.oauth.dropbox.DropboxSyncFragment;
import com.fitforbusiness.nafc.R;
import com.fitforbusiness.webservice.Synchronise;
import com.parse.FindCallback;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ViewAccreditationActivity extends ActionBarActivity
        implements View.OnClickListener {

    Accreditation accreditation=new Accreditation();
    List<Accreditation> list=new ArrayList<Accreditation>();
    final static private String APP_KEY = "4pqqfp0k0ruso0y";
    final static private String APP_SECRET = "37bs5lqcx9nr29a";
    final static private String ACCOUNT_PREFS_NAME = "prefs";
    final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    Button completedDate;
    EditText courseName;
    EditText courseNumber;
    EditText institute;
    ToggleButton isPoint;
    Button likedFile;
    EditText pointHours;
    Button unlinkFile;
    Button viewFile;
    String filePath = null;
    private DropboxAPI<?> mApi;
    private String mPath;
    private AutoCompleteTextView rtoList;
    private MySwitch mySwitch;
    private String _id;

    @Override
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);

        try {
            _id = getIntent().getBundleExtra("bundle").getString("_id");
        } catch (Exception e) {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_acrreditation);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        AndroidAuthSession session = buildSession();
        mApi = new DropboxAPI<AndroidAuthSession>(session);
        completedDate = ((Button) findViewById(R.id.bCompleteDate));
        courseName = ((EditText) findViewById(R.id.etCourseName));
        courseNumber = ((EditText) findViewById(R.id.etCourseNumber));
        pointHours = ((EditText) findViewById(R.id.etPoint));
        institute = ((EditText) findViewById(R.id.etRTO));
        rtoList = (AutoCompleteTextView) findViewById(R.id.acRTO);
        bindRTOList();
        isPoint = ((ToggleButton) findViewById(R.id.tbIsHour));
        mySwitch = ((MySwitch) findViewById(R.id.swIsHour));
        viewFile = ((Button) findViewById(R.id.bView));
        unlinkFile = ((Button) findViewById(R.id.bUnlike));

        likedFile = ((Button) findViewById(R.id.bChooseFromDropBox));
        viewFile.setOnClickListener(this);
        unlinkFile.setOnClickListener(this);

        likedFile.setOnClickListener(this);
        completedDate.setOnClickListener(this);

        try {

            String id = getIntent().getBundleExtra("bundle").getString("_id");

            loadParseAccredetation(id);
            if (!getIntent().getBundleExtra("bundle").getBoolean("editable"))
                setFieldsDisable();
            Log.d("LoadAccreditation", id);

        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    private void bindRTOList() {

        new AsyncTask<Void, Void, ArrayList<String>>() {

            @Override
            protected ArrayList<String> doInBackground(Void... params) {
                return Utils.getRTO(ViewAccreditationActivity.this);
            }

            @Override
            protected void onPostExecute(ArrayList<String> mRTOList) {
                super.onPostExecute(mRTOList);
                rtoList.setAdapter(new ArrayAdapter<String>(ViewAccreditationActivity.this,
                        android.R.layout.simple_spinner_dropdown_item, mRTOList));
                rtoList.setThreshold(1);
            }
        }.execute();
    }

    private void loadParseAccredetation(final String objectId) {
        list=new ArrayList<Accreditation>();
        ParseQuery parseQuery = new ParseQuery(Accreditation.class);
        parseQuery.fromLocalDatastore();
        parseQuery.whereEqualTo("trainer", Trainer.getCurrent());
        parseQuery.whereEqualTo("objectId", objectId);
        parseQuery.findInBackground(new FindCallback<Accreditation>() {
            @Override
            public void done(List<Accreditation> list, com.parse.ParseException e) {
                if (e == null && list != null) {
                    if (list.size() == 0) {
                        ParseQuery parseQuery = new ParseQuery(Accreditation.class);
                        parseQuery.whereEqualTo("trainer", Trainer.getCurrent());
                        parseQuery.findInBackground(new FindCallback<Accreditation>() {
                            @Override
                            public void done(List<Accreditation> list, com.parse.ParseException e) {
                                if (e == null && list != null) {
                                    Group.pinAllInBackground(list);
                                    loadIntoAccreditationListView(list);
                                }
                            }
                        });
                    } else {
                        loadIntoAccreditationListView(list);
                    }
                }
            }
        });
    }

    private void loadIntoAccreditationListView(List<Accreditation> list) {
        for (Accreditation accreditation:list){
            this.accreditation=accreditation;
            courseName.setText(accreditation.getCecCourseName());
            courseNumber.setText(accreditation.getCourseNumber());
            pointHours.setText(accreditation.getPoints());
            mySwitch.setChecked(accreditation.getIsHours());
            rtoList.setText(accreditation.getRegisteredTrainingOrganisation());
            completedDate.setText(accreditation.getCompletedDate());
//            filePath = localCursor.getString(localCursor.getColumnIndex(
//                    Table.TrainerProfileAccreditation.LINKED_FILE));
//            likedFile.setText(filePath != null ? filePath.substring(filePath.lastIndexOf("/") + 1) : "Choose from dropbox");
//            if (likedFile.getText().toString().equalsIgnoreCase("Choose from dropbox")) {
//                viewFile.setVisibility(View.GONE);
        }
    }


    private void loadAccreditation(String paramString) {
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            assert sqlDB != null;
            Cursor localCursor = sqlDB.rawQuery("select  *  from trainer_profile_accreditation " +
                    "where _id = " + paramString, null);
            if (localCursor.moveToFirst()) {
                courseName.setText(localCursor.getString(localCursor.getColumnIndex("course_name")));
                courseNumber.setText(localCursor.getString(localCursor.getColumnIndex("course_no")));
                pointHours.setText(localCursor.getString(localCursor.getColumnIndex("points")));
                mySwitch.setChecked(localCursor.getInt(localCursor.getColumnIndex(
                        Table.TrainerProfileAccreditation.IS_POINT)) == 0);
                rtoList.setText(localCursor.getString(localCursor.getColumnIndex("registered_training_organization")));
                completedDate.setText(Utils.formatConversionSQLite(localCursor.getString(localCursor.getColumnIndex("completed_date"))));
                filePath = localCursor.getString(localCursor.getColumnIndex(
                        Table.TrainerProfileAccreditation.LINKED_FILE));
                likedFile.setText(filePath != null ? filePath.substring(filePath.lastIndexOf("/") + 1) : "Choose from dropbox");
                if (likedFile.getText().toString().equalsIgnoreCase("Choose from dropbox")) {
                    viewFile.setVisibility(View.GONE);
                }
            }
            localCursor.close();
        } catch (Exception localException) {
            if (sqlDB != null) {
                sqlDB.close();
            }

        } finally {
            if (sqlDB != null) {
                sqlDB.close();
            }
        }
    }

    private void updateParseAccreditation(){
//        Accreditation accreditation=new Accreditation();
        accreditation.setCecCourseName(courseName.getText().toString());
     //   accreditation.setTrainer(Trainer.getCurrent());
        accreditation.setCourseNumber(courseNumber.getText().toString());
        accreditation.setPoints(pointHours.getText().toString());
        accreditation.setIsHours(mySwitch.isChecked());
        accreditation.setRegisteredTrainingOrgainsation(rtoList.getText().toString());
        accreditation.setCompletedDate(Utils.formatConversionLocale(
                completedDate.getText().toString()));

        try {
            accreditation.save();
        }catch (com.parse.ParseException e){
        }
        try {
            accreditation.pin();
        }catch (com.parse.ParseException e){
        }
    }
    private long updateAccreditation(String paramString) {
        ContentValues localContentValues = new ContentValues();
        localContentValues.put("course_name", courseName.getText().toString());
        localContentValues.put("course_no", courseNumber.getText().toString());
        localContentValues.put("points", pointHours.getText().toString());
        localContentValues.put("is_point", mySwitch.isChecked() ? 0 : 1);
        localContentValues.put("registered_training_organization", rtoList.getText().toString());
        localContentValues.put("completed_date", Utils.formatConversionLocale(completedDate.getText().toString()));
        localContentValues.put(Table.TrainerProfileAccreditation.LINKED_FILE, filePath);
        long l;
        l = DBOHelper.updateAccreditation(localContentValues, paramString);
        return l;
    }

    private void showPromptDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Fit For Business");
        builder.setMessage("Not linked to dropbox.");
        builder.setPositiveButton("Link", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(ViewAccreditationActivity.this, DropboxSyncFragment.class));
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }

    public void onClick(View paramView) {
        switch (paramView.getId()) {

            case R.id.bCompleteDate:
                showAlert(completedDate);
                break;
            case R.id.bChooseFromDropBox:
                AndroidAuthSession session = buildSession();

                if (new DropboxAPI<AndroidAuthSession>(session).getSession().isLinked())
                    startActivityForResult(new Intent(this, DropBoxFileList.class), 123);
                else {
                    showPromptDialog();
                }

                break;
            case R.id.bUnlike:
                likedFile.setText("Choose from dropbox");
                break;
            case R.id.bView:

                new DownloadFile(this, mApi, filePath).execute();
                break;

            case R.id.bSave:
                try {
                    if (Utils.validateFields(courseName) && Utils.validateFields(rtoList) && Utils.validateFields(completedDate)) {
//                        updateAccreditation(_id);
                        updateParseAccreditation();
                        finish();
                    }
                } catch (Exception localException) {
                    localException.printStackTrace();
                }
                break;
            case R.id.bCancel:
                finish();
        }
    }

    void showAlert(final Button mButton) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && data != null) {

            if (data.getStringExtra("path") != null) {
                filePath = data.getStringExtra("path");
                likedFile.setText(filePath.substring(filePath.lastIndexOf("/") + 1));
                if (likedFile.getText().toString().equalsIgnoreCase("Choose from dropbox")) {
                    viewFile.setVisibility(View.GONE);
                } else {
                    viewFile.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);

        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
        loadAuth(session);
        return session;
    }

    private void loadAuth(AndroidAuthSession session) {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key == null || secret == null || key.length() == 0 || secret.length() == 0) return;

        if (key.equals("oauth2:")) {
            // If the key is set to "oauth2:", then we can assume the token is for OAuth 2.
            session.setOAuth2AccessToken(secret);
        } else {
            // Still support using old OAuth 1 tokens.
            session.setAccessTokenPair(new AccessTokenPair(key, secret));
        }
    }

    private void setFieldsDisable() {

        courseName.setClickable(false);
        courseName.setFocusable(false);

        courseNumber.setClickable(false);
        courseNumber.setFocusable(false);


        pointHours.setClickable(false);
        pointHours.setFocusable(false);


        institute.setClickable(false);
        institute.setFocusable(false);

        mySwitch.setClickable(false);
        completedDate.setClickable(false);
        likedFile.setClickable(false);
        unlinkFile.setClickable(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_exercise, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.bUpdate) {
            try {
                if (Utils.validateFields(courseName) && Utils.validateFields(rtoList) && Utils.validateFields(completedDate)) {
//                    long isUpdated = updateAccreditation(_id + "");
//                    if (isUpdated > 0) {
//                        HashMap<String, Object> qualification;
//                        qualification = DBOHelper.getQualification(_id);
//                        String accreditationId = DBOHelper.getAccreditationWebIdFromRecordId(_id);
//                        if (Utils.isNetworkAvailable(this)) {
//                            updateQualificationOnServer(accreditationId, qualification);
//                        }
//                        setResult(Utils.ACCREDITATION, new Intent());
                        updateParseAccreditation();
                        finish();
//                    }
                }
            } catch (Exception localException) {
                localException.printStackTrace();
            }
        } else if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateQualificationOnServer(final String accreditationId, final HashMap<String, Object> o) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Synchronise.Qualification syncQualification = new Synchronise.Qualification(ViewAccreditationActivity.this,
                        Utils.getLastQualificationSyncTime(ViewAccreditationActivity.this));
                syncQualification.propagateDeviceObjectToServer(syncQualification.getServerObjectByWebRecordId(accreditationId,
                        syncQualification.getAllServerQualifications()), o);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Log.d("Create Workout", "Workout created on the server");
            }
        }.execute();

    }
}

