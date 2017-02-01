package com.fitforbusiness.nafc.session;


import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.fitforbusiness.Parse.Models.Session;
import com.fitforbusiness.database.DBOHelper;
import com.fitforbusiness.database.DatabaseHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.framework.SessionCustomAdapter;
import com.fitforbusiness.framework.SwipeDetector;
import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.nafc.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class PersonalTrainingFragment extends Fragment {


    private ListView sessionList;
    private ArrayList<HashMap<String, Object>> mapSessionArray;
    private SwipeRefreshLayout swipeLayout;

    public PersonalTrainingFragment() {
        // Required empty public constructor
    }

    public static PersonalTrainingFragment newInstance(int flag, String client_group_id) {
        PersonalTrainingFragment fragment = new PersonalTrainingFragment();
        Bundle args = new Bundle();
        args.putInt(Utils.ARG_GROUP_OR_CLIENT, flag);
        args.putString(Utils.ARG_GROUP_OR_CLIENT_ID, client_group_id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_client_detail, container, false);
        assert rootView != null;
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        swipeLayout.setEnabled(false);
        sessionList = (ListView) rootView.findViewById(R.id.lvSessionList);
        Button heading = (Button) rootView.findViewById(R.id.bHeading);
        heading.setText("Personal Training Activity");
//        loadSessions();
        loadParseSession();
        final SwipeDetector swipeDetector = new SwipeDetector(getActivity());
        sessionList.setOnTouchListener(swipeDetector);
        sessionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Map map = mapSessionArray.get(i);

                if (swipeDetector.swipeDetected()) {
                    if (swipeDetector.getAction() == SwipeDetector.Action.RL) {
                        showDeleteAlert(map.get("_id").toString());
                        // Toast.makeText(getActivity(), "Action Right to left", Toast.LENGTH_LONG).show();
                    } else {

                    }
                } else {
                    startActivityForResult(new Intent(getActivity(),
                                    ViewSessionActivity.class).putExtra("_id", map.get("_id").toString()),
                            Utils.CLIENTS
                    );
                }
            }
        });

        return rootView;
    }

    private void showDeleteAlert(final String row_id) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Fit For Business");
        builder.setMessage("Delete Session?")
                .setCancelable(true)
                .setPositiveButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        }
                ).setNegativeButton("Delete",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        deleteSession(row_id);
                    }
                }
        );

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void deleteSession(String row_id) {

        ContentValues values = new ContentValues();
        values.put(Table.DELETED, 1);
        DBOHelper.update(Table.Sessions.TABLE_NAME, values, row_id);
        loadSessions();

    }
    private Date convertStringToDate(String strDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        //SimpleDateFormat dateFormat = new SimpleDateFormat("mm dd yyyy");
        Date convertedDate = new Date();
        try {
            convertedDate = dateFormat.parse(strDate);
        } catch (java.text.ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return convertedDate;
    }

    private String convertDateToString(Date date) {
        DateFormat df = new SimpleDateFormat("dd MMM yyyy");
        String reportDate = df.format(date);
        return reportDate;
    }

    private void loadParseSession() {
        Calendar calendar=Calendar.getInstance();
        final Date today=calendar.getTime();
        final String today_str=convertDateToString(today);
        mapSessionArray = new ArrayList<HashMap<String, Object>>();
        ParseQuery parseQuery = new ParseQuery(Session.class);
        parseQuery.whereEqualTo("groupClientID",getArguments().getString(Utils.ARG_GROUP_OR_CLIENT_ID));
        parseQuery.whereLessThan("startDate", today_str);
        parseQuery.fromLocalDatastore();
        parseQuery.findInBackground(new FindCallback<Session>() {
            @Override
            public void done(List<Session> list, ParseException e) {
//                Log.e("test", (e == null) + " - " + (list == null));
                if (e == null && list != null) {
                    if (list.size() == 0) {
                        ParseQuery parseQuery = new ParseQuery(Session.class);
                        parseQuery.whereEqualTo("groupClientID",getArguments().getString(Utils.ARG_GROUP_OR_CLIENT_ID));
                        parseQuery.whereLessThan("startDate", today_str);
                        parseQuery.findInBackground(new FindCallback<Session>() {
                            @Override
                            public void done(List<Session> list, ParseException e) {
                                if (e == null && list != null) {
                                    loadIntoListView(list);
                                    Session.pinAllInBackground(list);
                                }
                            }
                        });
                    } else {
                        loadIntoListView(list);
                    }
                }
            }
        });


    }


    private void loadIntoListView(List<Session> list) {
        LinkedHashMap<String, Object> row;
        for (Session session : list) {
            row = new LinkedHashMap<String, Object>();
            row.put("_id", session.getObjectId());
            row.put("firstLabel", session.getObjectId());
            row.put("secondLabel", session.getTitle());
            row.put("thirdLabel",Utils.dateConversionForRow(session.getString("startDate")) + " " + Utils.timeFormatAMPM(session.getString("endDate")) );


            mapSessionArray.add(row);

            SessionCustomAdapter adapter = new SessionCustomAdapter(getActivity(),
                    R.layout.custom_button_list_row, R.id.bButton1, R.id.tvText1,
                    R.id.tvText2, R.id.tvText3, mapSessionArray);
            sessionList.setAdapter(adapter);
            swipeLayout.setRefreshing(false);
        }
    }
    private void loadSessions() {
        mapSessionArray = new ArrayList<HashMap<String, Object>>();
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();

            String query = "select  * "
                    + " from " +
                    Table.Sessions.TABLE_NAME +
                    " where " + Table.DELETED + " = 0 and "
                    + Table.Sessions.SESSION_TYPE + " = " + getArguments().getInt(Utils.ARG_GROUP_OR_CLIENT)
                    + " and  " + Table.Sessions.GROUP_ID + " = " + getArguments().getString(Utils.ARG_GROUP_OR_CLIENT_ID)
                    + " and " + Table.Sessions.START_DATE + " < date(\'now\')"
                    + " order by " + Table.Sessions.TITLE + " asc";

            Log.d("query is ", query);
            assert sqlDB != null;
            Cursor cursor = sqlDB
                    .rawQuery(query
                            , null);

            LinkedHashMap<String, Object> row;
            while (cursor.moveToNext()) {


                row = new LinkedHashMap<String, Object>();
                row.put("_id", cursor.getString(cursor
                        .getColumnIndex(Table.Sessions.ID)));
                row.put("firstLabel", cursor.getString(cursor
                        .getColumnIndex(Table.Sessions.TITLE)));
                row.put("secondLabel", cursor.getString(cursor
                        .getColumnIndex(Table.Sessions.VENUE)));
                row.put("thirdLabel", Utils.dateConversionForRow(cursor.getString(cursor
                        .getColumnIndex(Table.Sessions.START_DATE)))+" "+Utils.timeFormatAMPM(cursor.getString(cursor
                        .getColumnIndex(Table.Sessions.START_TIME))));


                mapSessionArray.add(row);
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
        SessionCustomAdapter adapter = new SessionCustomAdapter(getActivity(),
                R.layout.training_activity_row, R.id.bButton1, R.id.tvText1,
                R.id.tvText2, R.id.tvText3, mapSessionArray);

        sessionList.setAdapter(adapter);
    }
}
