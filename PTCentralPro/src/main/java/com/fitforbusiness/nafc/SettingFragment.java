package com.fitforbusiness.nafc;


import android.app.Activity;
import android.app.Notification;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;

import com.appboy.Appboy;
import com.google.gson.Gson;
import com.fitforbusiness.database.DatabaseHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.webservice.SyncService;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Sanjeet on 4/25/14.
 */
public class SettingFragment extends com.fitforbusiness.framework.PreferenceFragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    public SettingFragment() {
    }

    public static SettingFragment newInstance(int sectionNumber) {
        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public static SettingFragment newInstance(int sectionNumber, Bundle args) {
        SettingFragment fragment = new SettingFragment();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_data_sync);
        final ListPreference listPreference = (ListPreference) getPreferenceManager()
                .findPreference("currency");
        listPreference.setSummary(listPreference.getValue());
        listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                listPreference.setSummary(newValue.toString());
                return true;
            }
        });

        final ListPreference alertPreference = (ListPreference) getPreferenceManager()
                .findPreference("session_alert");
        alertPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                ArrayList<HashMap<String, Object>> sessions = getAllSession();
                Log.d("settings:sessionAlert", "total sessions = " + sessions.size());

                if (Integer.valueOf(newValue.toString()) < 0){
                    for (HashMap<String, Object> session : sessions){
                        Log.d("settings:sessionAlert", "canceling notifications for id = " +
                                session.get(Table.Sessions.ID));
                        Notify.cancelScheduledNotification(getActivity(),
                                Integer.valueOf((String) session.get(Table.Sessions.ID)));
                    }
                    return true;
                }

                for (HashMap<String, Object> session : sessions){
                    int status = Integer.valueOf((String) session.get(Table.Sessions.SESSION_STATUS));
                    Log.d("settings:sessionAlert", "Status = " + status);
                    if (status == 0){
                        Log.d("settings:sessionAlert", "setting up notifications for id = " +
                                session.get(Table.Sessions.ID));
                        try {
                            setUpNotification(session, Integer.valueOf((String)
                                    session.get(Table.Sessions.ID)));
                        }catch (NumberFormatException e){
                            Log.d("settings:sessionAlert", "Invalid id");
                        }
                    }
                }
                return true;
            }
        });

        final ListPreference listPreferenceSyncFrequency = (ListPreference) getPreferenceManager()
                .findPreference("sync_frequency");
        //listPreferenceSyncFrequency.setSummary(listPreference.getValue());
        listPreferenceSyncFrequency.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
            //    listPreferenceSyncFrequency.setSummary(newValue.toString());
                Intent intent = new Intent(getActivity(), SyncService.class);
                intent.putExtra("interval", Integer.valueOf(newValue.toString()) * 60 * 1000);
                getActivity().startService(intent);
                return true;
            }
        });

    }



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onResume() {
        super.onResume();
        Appboy.getInstance(getActivity()).requestSlideupRefresh();
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);

        if (pref instanceof ListPreference) {
            ListPreference listPref = (ListPreference) pref;
            pref.setSummary(listPref.getEntry());
        }
    }

//    _____________ Helper Functions _______________

    void setUpNotification(HashMap<String, Object> contentValues, int sessionId){
        try {
            Log.d("settings:sessionAlert", (String) contentValues.get(Table.Sessions.START_DATE));
            Date notificationDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
                    .parse((String) contentValues.get(Table.Sessions.START_DATE));
            Time time = Time.valueOf((String) contentValues.get(Table.Sessions.START_TIME));

            notificationDate.setHours(time.getHours());
            notificationDate.setMinutes(time.getMinutes());
            notificationDate.setSeconds(time.getSeconds());

            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());
            String listPreference = prefs.getString("session_alert", "30");
            int milliSecondsToAlert = Integer.valueOf(listPreference) * 60 * 1000;
            Log.d("settings:sessionAlert", "setting = " + milliSecondsToAlert);

            if (milliSecondsToAlert < 0) return;

            notificationDate.setTime(notificationDate.getTime() - milliSecondsToAlert);
            Log.d("settings:sessionAlert", "" + notificationDate);

            Date c = new Date(System.currentTimeMillis());

            long delay = notificationDate.getTime() - c.getTime();
            Log.d("settings:sessionAlert", "delay = " + delay);

            if (delay < -milliSecondsToAlert) return;


            String groupClientId = contentValues.get("group_id").toString();
            int sessionType = Integer.parseInt(contentValues.get("session_type").toString());

            String name = sessionType == Utils.FLAG_CLIENT ?
                    loadClientName(groupClientId): loadGroupName(groupClientId);
            String type = sessionType == Utils.FLAG_CLIENT ?
                    "Client" : "Group";

            Notification n = Notify.getNotification(getActivity(),
                    String.format(getString(R.string.text_notification), type, name));

            Notify.scheduleNotification(getActivity(), n,
                    delay, sessionId);
            Log.d("settings:sessionAlert", "Done!");
        } catch (ParseException e) {
            e.printStackTrace();
            Log.d("settings:sessionAlert", "error");
        }
    }

    //    _______________   Get all Sessions for alert ________

    private ArrayList<HashMap<String, Object>> getAllSession() {
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
            if (sqlDB != null) {
                sqlDB.close();
            }
        }
        Log.d("sessionList", new Gson().toJson(sessionsList));
        return sessionsList;
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
            if (sqlDB != null) {
                sqlDB.close();
            }
            e.printStackTrace();
        } finally {
            if (sqlDB != null) {
                sqlDB.close();
            }
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
            assert sqlDB != null;
            sqlDB.close();
            e.printStackTrace();
        } finally {
            assert sqlDB != null;
            sqlDB.close();
        }
        return firstName + " " + lastName;
    }
}