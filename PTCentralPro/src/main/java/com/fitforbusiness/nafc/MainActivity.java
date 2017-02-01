package com.fitforbusiness.nafc;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.Settings;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.fitforbusiness.Parse.Models.Trainer;
import com.fitforbusiness.database.DatabaseHelper;
import com.fitforbusiness.nafc.calendar.NativeSync;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.fitforbusiness.database.DBOHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.framework.FFBActivity;
import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.nafc.accreditation.AccreditationFragment;
import com.fitforbusiness.nafc.calendar.CalendarFragment;
import com.fitforbusiness.nafc.client.ClientFragment;
import com.fitforbusiness.nafc.dashboard.HomeFragment;
import com.fitforbusiness.nafc.exercise.ExerciseFragment;
import com.fitforbusiness.nafc.exercise.WorkoutFragment;
import com.fitforbusiness.nafc.group.GroupFragment;
import com.fitforbusiness.nafc.membership.PaymentTabsFragment;
import com.fitforbusiness.webservice.SyncService;
import com.mobileapptracker.MobileAppTracker;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TimeZone;

public class MainActivity extends FFBActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    int nMenuItemNumber = -1;
    private boolean mRefreshData;
    private String[] list = new String[]{"Home", "News Feed", "My Calendar", "Clients", "Groups",
             "Exercises", "Workouts", "Qualifications", "Payment Details", "Settings", "Feedback", "Logout", "Profile"};
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;
    private MobileAppTracker mobileAppTracker;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            Utils.createLocalResourceDirectory(getApplicationContext());
            Utils.createThumbNailDirectory(getApplicationContext());
            Utils.createProfileImageDirectory(getApplicationContext());
        } catch (Exception ex) {
            Log.d("Directory not created", "");
        }
       /* if (BuildConfig.DEBUG) {
            com.fitforbusiness.framework.cache.Utils.enableStrictMode();
        }*/
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences settings = getSharedPreferences(Utils.TRAINER_PREFS, 0);

        mobileAppTracker = TuneInitialize.initialize(this);

        try {
            String trainer_id = settings.getString("trainer_id", "-1");
            boolean download_data = settings.getBoolean("download_data", false);
            if (!trainer_id.equalsIgnoreCase("-1")) {
                Utils.setLastExerciseSyncTime(this);
                Utils.setLastWorkoutSyncTime(this);
                Utils.setLastQualificationSyncTime(this);
                Utils.setLastClientSyncTime(this);
                Utils.setLastGroupSyncTime(this);
                Utils.setLastAssessmentSyncTime(this);
                Utils.setLastSessionSyncTime(this);
            }
            if (download_data) {
                ContentValues contentValues = new ContentValues();
//                contentValues.put(Table.TrainerProfileDetails.TRAINER_ID, trainer_id);
//                DBOHelper.insert(this, Table.TrainerProfileDetails.TABLE_NAME, contentValues);
                //   new DataDownloadAsyncTask(this, trainer_id).execute();
                Intent intent = new Intent(this, SyncService.class);
                intent.putExtra("interval", 0);
                startService(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Collect Google Play Advertising ID; REQUIRED for attribution of Android apps distributed via Google Play
        new Thread(new Runnable() {
            @Override
            public void run() {
                // See sample code at http://developer.android.com/google/play-services/id.html
                try {
                    AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(getApplicationContext());
                    mobileAppTracker.setGoogleAdvertisingId(adInfo.getId(), adInfo.isLimitAdTrackingEnabled());
                } catch (Exception e) {
                    // Encountered an error getting Google Advertising Id, use ANDROID_ID
                    mobileAppTracker.setAndroidId(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
                }

                // Check if deferred deeplink can be opened
                // Uncomment this line if your MAT account has enabled deferred deeplinks
                //mobileAppTracker.checkForDeferredDeeplink(750);
            }
        }).start();

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        handleViewIntent();

        new Thread(new Runnable() {
            @Override
            public void run() {
                NativeSync.getAllNativeEvents(getApplication());
            }
        }).start();


//        Notify.scheduleNotification(this,
//                Notify.getNotification(this, "Testing"), 1000, 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Get source of open for app re-engagement
        mobileAppTracker.setReferralSources(this);
        // MAT will not function unless the measureSession call is included
        mobileAppTracker.measureSession();
    }

    void handleViewIntent(){
        Intent i = getIntent();
        if (i.getAction() == Intent.ACTION_VIEW){
            FragmentManager fragmentManager = getSupportFragmentManager();
            if (i.getData().getHost().equalsIgnoreCase("My_Calendar")){
                nMenuItemNumber = 3;
                fragmentManager.beginTransaction()
                        .replace(R.id.container, CalendarFragment.newInstance(nMenuItemNumber))
                        .commit();
            }if (i.getData().getHost().equalsIgnoreCase("Trainer-My_Details")){
                nMenuItemNumber = 12;
                fragmentManager.beginTransaction()
                        .replace(R.id.container, TrainerDetailFragment.newInstance(nMenuItemNumber))
                        .commit();
            }if (i.getData().getHost().equalsIgnoreCase("Clients-My_Clients")){
                nMenuItemNumber = 4;
                fragmentManager.beginTransaction()
                        .replace(R.id.container, ClientFragment.newInstance(nMenuItemNumber))
                        .commit();
            }if (i.getData().getHost().equalsIgnoreCase("Clients-My_Groups")){
                nMenuItemNumber = 5;
                fragmentManager.beginTransaction()
                        .replace(R.id.container, GroupFragment.newInstance(nMenuItemNumber))
                        .commit();
            }if (i.getData().getHost().equalsIgnoreCase("Trainer-Qualifications")){
                nMenuItemNumber = 6;
                fragmentManager.beginTransaction()
                        .replace(R.id.container, AccreditationFragment.newInstance(nMenuItemNumber))
                        .commit();
            }if (i.getData().getHost().equalsIgnoreCase("Workouts")){
                nMenuItemNumber = 8;
                fragmentManager.beginTransaction()
                        .replace(R.id.container, WorkoutFragment.newInstance(nMenuItemNumber))
                        .commit();
            }if (i.getData().getHost().equalsIgnoreCase("Exercises")){
                nMenuItemNumber = 7;
                fragmentManager.beginTransaction()
                        .replace(R.id.container, ExerciseFragment.newInstance(nMenuItemNumber))
                        .commit();
            }
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (position) {
            case 0:
                nMenuItemNumber = 0;
                fragmentManager.beginTransaction()
                        .replace(R.id.container, HomeFragment.newInstance(position))
                        .commit();
                break;
            case 1:
                nMenuItemNumber = 1;
                fragmentManager.beginTransaction()
                        .replace(R.id.container, FeedFragment.newInstance(position))
                        .commit();
                break;
            case 8:
                nMenuItemNumber = 2;
                fragmentManager.beginTransaction()
                        .replace(R.id.container, PaymentTabsFragment.newInstance(position))
                        .commit();
                break;
            case 2:
                nMenuItemNumber = 2;
                fragmentManager.beginTransaction()
                        .replace(R.id.container, CalendarFragment.newInstance(position))
                        .commit();
                break;

            case 3:
                nMenuItemNumber = 3;
                fragmentManager.beginTransaction()
                        .replace(R.id.container, ClientFragment.newInstance(position))
                        .commit();
                break;
            case 4:
                nMenuItemNumber = 4;
                fragmentManager.beginTransaction()
                        .replace(R.id.container, GroupFragment.newInstance(position))
                        .commit();
                break;
            case 7:
                nMenuItemNumber = 7;
                fragmentManager.beginTransaction()
                        .replace(R.id.container, AccreditationFragment.newInstance(position))
                        .commit();
                break;
            case 5:
                nMenuItemNumber = 5;
                fragmentManager.beginTransaction()
                        .replace(R.id.container, ExerciseFragment.newInstance(position))
                        .commit();
                break;
            case 6:
                nMenuItemNumber = 6;
                fragmentManager.beginTransaction()
                        .replace(R.id.container, WorkoutFragment.newInstance(position))
//                        .replace(R.id.container, DSLVFragment.newInstance(0,0))
                        .commit();

                break;
            case 9:
                restoreActionBar();
                nMenuItemNumber = 9;
                fragmentManager.beginTransaction()
                        .replace(R.id.container, SettingFragment.newInstance(position))
                        .commit();
                break;

            case 10:
                nMenuItemNumber = 10;
                fragmentManager.beginTransaction()
                        .replace(R.id.container, FeedbackFragment.newInstance(position))
                        .commit();
                break;
            case 11:
                nMenuItemNumber = 11;
                Log.d("Main:menu", "logout");
                SharedPreferences settings = getSharedPreferences(Utils.TRAINER_PREFS, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("hasLoggedIn", false);
                editor.putString("trainer_id", "-1");
                editor.putBoolean("download_data", false);
                Utils.clearLastSyncTime(this);
                DBOHelper.clearAllData();
                editor.commit();
                DBOHelper.clearAllData();
                Trainer.logOut();
                finish();
                break;
            case 12:
                nMenuItemNumber = 12;
                Log.d("Main:menu", "edit profile");
                fragmentManager.beginTransaction()
                        .replace(R.id.container, TrainerDetailFragment.newInstance(position))
                        .commit();
                break;
        }
    }

    public void onSectionAttached(int number) {
        mTitle = list[number];
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        try {
            actionBar.setCustomView(null);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(mTitle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        restoreActionBar();
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("Request Code is:", requestCode + ": Result Code is " + requestCode);
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent();
        i.setAction(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        startActivity(i);
    }


}
