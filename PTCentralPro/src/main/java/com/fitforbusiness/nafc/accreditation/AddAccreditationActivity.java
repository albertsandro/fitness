package com.fitforbusiness.nafc.accreditation;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
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

import com.appboy.Appboy;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.fitforbusiness.Parse.Models.Accreditation;
import com.fitforbusiness.Parse.Models.Group;
import com.fitforbusiness.Parse.Models.Trainer;
import com.fitforbusiness.database.DBOHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.framework.view.MySwitch;
import com.fitforbusiness.oauth.dropbox.DownloadFile;
import com.fitforbusiness.oauth.dropbox.DropBoxFileList;
import com.fitforbusiness.oauth.dropbox.DropboxSyncFragment;
import com.fitforbusiness.nafc.R;
import com.fitforbusiness.webservice.Synchronise;
import com.parse.ParseException;
import com.parse.SaveCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Sanjeet on 5/29/14.
 */
public class AddAccreditationActivity extends ActionBarActivity implements View.OnClickListener {

    final static private String APP_KEY = "4pqqfp0k0ruso0y";
    final static private String APP_SECRET = "37bs5lqcx9nr29a";
    final static private String ACCOUNT_PREFS_NAME = "prefs";
    final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    private EditText courseName, courseNumber, pointHours, institute;
    private ToggleButton isPoint;
    private Button completedDate, viewFile, unlinkFile, likedFile;
    private DropboxAPI<?> mApi;
    private String filePath = "";
    private String filName = "";
    private AutoCompleteTextView rtoList;
    private MySwitch mySwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acrreditation);
        AndroidAuthSession session = buildSession();
        mApi = new DropboxAPI<AndroidAuthSession>(session);
        completedDate = (Button) findViewById(R.id.bCompleteDate);
        /*courseName,courseNumber,pointHours,institute;*/
        courseName = (EditText) findViewById(R.id.etCourseName);
        courseNumber = (EditText) findViewById(R.id.etCourseNumber);
        pointHours = (EditText) findViewById(R.id.etPoint);
        institute = (EditText) findViewById(R.id.etRTO);

        rtoList = (AutoCompleteTextView) findViewById(R.id.acRTO);
        loadAutoCompleteList();
        rtoList.setThreshold(1);

        isPoint = (ToggleButton) findViewById(R.id.tbIsHour);
        mySwitch = (MySwitch) findViewById(R.id.swIsHour);
        mySwitch.setChecked(true);
        viewFile = (Button) findViewById(R.id.bView);
        unlinkFile = (Button) findViewById(R.id.bUnlike);

        likedFile = (Button) findViewById(R.id.bChooseFromDropBox);

        viewFile.setOnClickListener(this);
        unlinkFile.setOnClickListener(this);

        likedFile.setOnClickListener(this);

        completedDate.setOnClickListener(this);

    }

    private void loadAutoCompleteList() {

        new AsyncTask<Void, Void, ArrayList<String>>() {

            @Override
            protected ArrayList<String> doInBackground(Void... params) {
                return Utils.getRTO(AddAccreditationActivity.this);
            }

            @Override
            protected void onPostExecute(ArrayList<String> mRTOList) {
                super.onPostExecute(mRTOList);
                rtoList.setAdapter(new ArrayAdapter<String>(AddAccreditationActivity.this,
                        android.R.layout.simple_spinner_dropdown_item, mRTOList));
                rtoList.setThreshold(1);
            }
        }.execute();

    }

    void showAlert() {
        DatePickerDialog dateDlg = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        Calendar c = Calendar.getInstance();
                        c.set(year, monthOfYear, dayOfMonth);
                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
                        String formattedDate = sdf.format(c.getTime());
                        completedDate.setText(formattedDate);/*String.format("%4d-%02d-%02d", year,
                                monthOfYear + 1, dayOfMonth));*/
                    }
                }, Calendar
                .getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar
                .getInstance().get(Calendar.DAY_OF_MONTH)
        );
        dateDlg.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bCompleteDate:
                showAlert();
                break;
            case R.id.bChooseFromDropBox:
                AndroidAuthSession session = buildSession();

                if (new DropboxAPI<AndroidAuthSession>(session).getSession().isLinked())
                    startActivityForResult(new Intent(this, DropBoxFileList.class), 123);
                else {
                    showPromptDialog();
                }

                break;
            case R.id.bView:
                new DownloadFile(this, mApi, filePath).execute();
                break;
            case R.id.bUnlike:
                likedFile.setText("Choose from dropbox");
                break;
            case R.id.bSave:
                saveParseAccreditation();
                finish();
                break;
            case R.id.bCancel:
                finish();
                break;
        }
    }

    private void showPromptDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Fit For Business");
        builder.setMessage("Not linked to dropbox.");
        builder.setPositiveButton("Link", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(AddAccreditationActivity.this, DropboxSyncFragment.class));
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    private void saveParseAccreditation(){
        Accreditation accreditation=new Accreditation();
        accreditation.setCecCourseName(courseName.getText().toString());
        accreditation.setTrainer(Trainer.getCurrent());
        accreditation.setCourseNumber(courseNumber.getText().toString());
        accreditation.setPoints(pointHours.getText().toString());
        accreditation.setIsHours(mySwitch.isChecked());
        accreditation.setRegisteredTrainingOrgainsation(rtoList.getText().toString());
        accreditation.setCompletedDate(Utils.formatConversionLocale(
                completedDate.getText().toString()));

        try {
            accreditation.save();
        }catch (ParseException e){
        }
        try {
            accreditation.pin();
        }catch (ParseException e){
        }
    }

    private long saveAccreditation() {

        ContentValues values = new ContentValues();
        values.put(Table.TrainerProfileAccreditation.COURSE_NAME, courseName.getText().toString());
        values.put(Table.TrainerProfileAccreditation.COURSE_NO, courseNumber.getText().toString());
        values.put(Table.TrainerProfileAccreditation.POINTS_HOURS, pointHours.getText().toString());
        values.put(Table.TrainerProfileAccreditation.IS_POINT, mySwitch.isChecked() ? 0 : 1);
        values.put(Table.TrainerProfileAccreditation.REGISTERED_TRAINING_ORGANIZATION, rtoList.getText().toString());
        values.put(Table.TrainerProfileAccreditation.COMPLETED_DATE, Utils.formatConversionLocale(
                completedDate.getText().toString()));
        values.put(Table.SYNC_ID, UUID.randomUUID().toString());
        values.put(Table.TrainerProfileAccreditation.LINKED_FILE, filePath);

        long id = DBOHelper.insert(this, Table.TrainerProfileAccreditation.TABLE_NAME, values);
        if (id > 0) {
            Utils.showToast(this, "Accreditation Added !");
            return id;
        } else {
            Utils.showToast(this, "Couldn't Add !");
        }
        Log.d("accreditation  inserted  is", id + "");
        return -1;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && data != null) {
            likedFile.setText(data.getStringExtra("name"));
            filePath = data.getStringExtra("path");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_group, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            try {
                if (Utils.validateFields(courseName) && Utils.validateFields(rtoList) && Utils.validateFields(completedDate)) {
                     saveParseAccreditation();
//                    if (accreditationId > 0) {
//                        if (Utils.isNetworkAvailable(this)) {
//                            createQualificationOnServer(DBOHelper.getQualification(accreditationId + ""));
//                        }
//                        Appboy.getInstance(this).logCustomEvent(Utils.CUSTOM_EVENT_KEY_CREATE_QUALIFICATION);
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

    private void createQualificationOnServer(final HashMap<String, Object> qualification) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                new Synchronise.Qualification(AddAccreditationActivity.this,
                        Utils.getLastQualificationSyncTime(AddAccreditationActivity.this)).createOnServer(qualification);
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
