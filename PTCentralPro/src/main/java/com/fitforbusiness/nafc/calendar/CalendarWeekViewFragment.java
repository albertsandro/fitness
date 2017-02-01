package com.fitforbusiness.nafc.calendar;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.fitforbusiness.Parse.Models.Client;
import com.fitforbusiness.Parse.Models.Group;
import com.fitforbusiness.Parse.Models.Session;
import com.fitforbusiness.Parse.Models.Trainer;
import com.fitforbusiness.database.DatabaseHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.framework.CalendarAdapter;
import com.fitforbusiness.framework.CustomAsyncTaskListAdapter;
import com.fitforbusiness.framework.RowData;
import com.fitforbusiness.framework.SwipeDetector;
import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.nafc.R;
import com.fitforbusiness.nafc.session.ViewSessionActivity;
import com.parse.FindCallback;
import com.parse.ParseQuery;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CalendarWeekViewFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final String ARG_SECTION_NUMBER = "section_number";
    public Handler handler;
    public ArrayList<String> items;
    public Runnable calendarUpdater = new Runnable() {

        @Override
        public void run() {
            adapter.setItems(items);
            adapter.notifyDataSetChanged();
        }
    };

    TextView detail;
    Calendar calendar;

    private CalendarAdapter adapter;
    private Button datePrev, dateNext;
    private TextView currentDate;
    private ListView eventList;
    private ArrayList<HashMap<String, Object>> mapArrayList;
    private RowData rowData;
    private String[] status;
    private SwipeDetector swipeDetector;

    public CalendarWeekViewFragment() {
        // Required empty public constructor
    }

    public static CalendarWeekViewFragment newInstance(int section) {
        CalendarWeekViewFragment fragment = new CalendarWeekViewFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, section);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //  mParam1 = getArguments().getString(ARG_PARAM1);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_calendar_week, container, false);

        status = getResources().getStringArray(R.array.status);
        eventList = (ListView) rootView.findViewById(R.id.lvEvents);
        rowData = new RowData();
        currentDate = (TextView) rootView.findViewById(R.id.tvCurrentDate);
        datePrev = (Button) rootView.findViewById(R.id.bDatePrev);
        dateNext = (Button) rootView.findViewById(R.id.bDateNext);
        datePrev.setOnClickListener(this);
        dateNext.setOnClickListener(this);

        calendar = Calendar.getInstance();
        Calendar c = Calendar.getInstance();
        c.setTime(calendar.getTime());
        c.add(Calendar.DAY_OF_MONTH, 7);
        String toDay = new SimpleDateFormat("dd MMMM").format(calendar.getTime());
        String nextDay = new SimpleDateFormat("dd MMMM").format(c.getTime());
        currentDate.setText(toDay + " - " + nextDay);
        loadParseSessions();
        eventList.setOnItemClickListener(this);
        swipeDetector = new SwipeDetector(getActivity());
        eventList.setOnTouchListener(swipeDetector);
        return rootView;
    }


    @Override
    public void onClick(View v) {
        Calendar c = Calendar.getInstance();
        switch (v.getId()) {
            case R.id.bDatePrev:
                calendar.add(Calendar.DAY_OF_MONTH, -7);
                c.setTime(calendar.getTime());
                loadParseSessions();
                break;
            case R.id.bDateNext:
                calendar.add(Calendar.DAY_OF_MONTH, 7);
                c.setTime(calendar.getTime());
                loadParseSessions();

                break;
        }
        c.add(Calendar.DAY_OF_MONTH, 7);
        String toDay = new SimpleDateFormat("dd MMMM,yyyy").format(calendar.getTime());
        String nextDay = new SimpleDateFormat("dd MMMM,yyyy").format(c.getTime());
        currentDate.setText(toDay + " - " + nextDay);

    }
    private void loadParseSessions() {

        mapArrayList = new ArrayList<HashMap<String, Object>>();
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

    private Date convertStringToDate(String strDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        //SimpleDateFormat dateFormat = new SimpleDateFormat("mm dd yyyy");
        Date convertedDate = new Date();
        try {
            convertedDate = dateFormat.parse(strDate);
        } catch (ParseException e) {
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
    private void loadIntoListView(List<Session> list) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(calendar.getTime());
        cal.add(Calendar.DAY_OF_MONTH, 7);
        String startDateStr =convertDateToString(calendar.getTime());
        String endDateStr = convertDateToString(cal.getTime());
        Date startDate=convertStringToDate(startDateStr);
        Date endDate=convertStringToDate(endDateStr);
        LinkedHashMap<String, Object> row;
        for (Session session : list) {
            Date date=convertStringToDate(session.getStartDate());
            //Date edate=convertStringToDate(session.getEndDate());
            if ((date.after(startDate) && (date.before(endDate)))||
                    (date.getMonth()==startDate.getMonth() && date.getDate()==startDate.getDate())
                    ||(date.getMonth()==endDate.getMonth() && date.getDate()==endDate.getDate())) {
                row = new LinkedHashMap<String, Object>();
                row.put("_id", session.getObjectId());
                row.put("name", session.getTitle());
                row.put("type", session.getSessionType());
                row.put("secondLabel", session.getNotes());
                row.put("thirdLabel", session.getStatus());

                String fileObjectStr;
                if (session.getSessionType().equals("1")) {
                    fileObjectStr= Group.createWithoutData(Group.class, session.getGroupClientId()).getImageFile();
                }else{
                    fileObjectStr= Client.createWithoutData(Client.class, session.getGroupClientId()).getImageFile();
                }
                Bitmap bmp;
                byte[] fileObject = android.util.Base64.decode(fileObjectStr, 1);
                bmp = BitmapFactory.decodeByteArray(fileObject, 0, fileObject.length);
                row.put("photo", bmp);

                mapArrayList.add(row);
            }
        }
        CustomAsyncTaskListAdapter adapter = new CustomAsyncTaskListAdapter(getActivity(),
                R.layout.calendar_day_view_session_row, R.id.ivRowImage, R.id.tvFirstName, R.id.tvSecondLabel, R.id.tvThirdLabel, mapArrayList);

        eventList.setAdapter(adapter);
    }
    private void loadSessions() {


        Calendar cal = Calendar.getInstance();
        cal.setTime(calendar.getTime());
        cal.add(Calendar.DAY_OF_MONTH, 7);
        String startDate = String.format("%d-%02d-%02d", calendar.get(Calendar.YEAR), (calendar.get(Calendar.MONTH) + 1), calendar.get(Calendar.DAY_OF_MONTH));
        String endDate = String.format("%d-%02d-%02d", cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH) + 1), cal.get(Calendar.DAY_OF_MONTH));


        mapArrayList = new ArrayList<HashMap<String, Object>>();
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();

            String query = "select * " + " from " +
                    Table.Sessions.TABLE_NAME
                    + " where " + Table.DELETED
                    + " = 0 and " + Table.Sessions.START_DATE + " >=  date(\'" + startDate + "\') and " + Table.Sessions.START_DATE + " <=  (\'" + endDate
                    + "\')";
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
                row.put("name", Utils.dateConversionForRow(cursor.getString(cursor
                        .getColumnIndex(Table.Sessions.START_DATE))) + " " + Utils.timeFormatAMPM(cursor.getString(cursor
                        .getColumnIndex(Table.Sessions.START_TIME))));
                long groupSessionId = cursor.getInt(cursor
                        .getColumnIndex(Table.Sessions.GROUP_ID));
                int session_type = cursor.getInt(cursor
                        .getColumnIndex(Table.Sessions.SESSION_TYPE));
                rowData = getImageName(groupSessionId + "",
                        session_type);
                row.put("rowId", groupSessionId);
                row.put("type", session_type);
                row.put("photo", rowData.getImageName());
                row.put("secondLabel", rowData.getPersonName());
                row.put("thirdLabel", status[cursor.getInt(cursor
                        .getColumnIndex(Table.Sessions.SESSION_STATUS))]);
                mapArrayList.add(row);
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
       /*new SessionCustomAdapter(getActivity(),
                R.layout.custom_training_row, R.id.bButton1, R.id.tvText1,
                R.id.tvText2, R.id.tvText3, mapArrayList);*/
        CustomAsyncTaskListAdapter adapter = new CustomAsyncTaskListAdapter(getActivity(),
                R.layout.calendar_day_view_session_row, R.id.ivRowImage, R.id.tvFirstName, R.id.tvSecondLabel, R.id.tvThirdLabel, mapArrayList);

        eventList.setAdapter(adapter);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
       /* ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));*/
    }

    private RowData getImageName(String _id, int isGroup) {
        String query = "";

        if (isGroup == 0) {
            query = "select  * "
                    + " from " +
                    Table.Client.TABLE_NAME +
                    " where " + Table.DELETED + " = 0 "
                    + " and " + Table.Client.ID + " = " + _id;
        } else {

            query = "select  * "
                    + " from " +
                    Table.Group.TABLE_NAME +
                    " where " + Table.DELETED + " = 0 "
                    + " and " + Table.Group.ID + " = " + _id;
        }
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();


            Log.d("query is ", query);
            assert sqlDB != null;
            Cursor cursor = sqlDB
                    .rawQuery(query
                            , null);


            if (cursor.moveToFirst()) {


                String imageName = (cursor.getString(cursor
                        .getColumnIndex(Table.Client.PHOTO_URL)));
                String personName;
                if (isGroup == 0) {
                    personName = cursor.getString(cursor
                            .getColumnIndex(Table.Client.FIRST_NAME))
                            + " "+ cursor.getString(cursor
                            .getColumnIndex(Table.Client.LAST_NAME));
                } else {
                    personName = cursor.getString(cursor
                            .getColumnIndex(Table.Group.NAME));
                }

                rowData.setImageName(imageName);
                rowData.setPersonName(personName);
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

        return rowData;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Map map = mapArrayList.get(position);

        if (swipeDetector.swipeDetected()) {
            if (swipeDetector.getAction() == SwipeDetector.Action.RL) {
                // showDeleteAlert(map.get("_id").toString());
                // Toast.makeText(getActivity(), "Action Right to left", Toast.LENGTH_LONG).show();
            } else {

            }
        } else {
            startActivity(new Intent(getActivity(),
                            ViewSessionActivity.class).putExtra("_id", map.get("_id").toString())
            );
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loadSessions();
    }
}


//public class CalendarWeekViewFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {
//
//    private static final String ARG_SECTION_NUMBER = "section_number";
//    public Handler handler;
//    public ArrayList<String> items;
//    public Runnable calendarUpdater = new Runnable() {
//
//        @Override
//        public void run() {
//            adapter.setItems(items);
//            adapter.notifyDataSetChanged();
//        }
//    };
//
//    TextView detail;
//    Calendar calendar;
//
//    private CalendarAdapter adapter;
//    private Button datePrev, dateNext;
//    private TextView currentDate;
//    private ListView eventList;
//    private ArrayList<HashMap<String, Object>> mapArrayList;
//    private RowData rowData;
//    private String[] status;
//    private SwipeDetector swipeDetector;
//
//    public CalendarWeekViewFragment() {
//        // Required empty public constructor
//    }
//
//    public static CalendarWeekViewFragment newInstance(int section) {
//        CalendarWeekViewFragment fragment = new CalendarWeekViewFragment();
//        Bundle args = new Bundle();
//        args.putInt(ARG_SECTION_NUMBER, section);
//
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            //  mParam1 = getArguments().getString(ARG_PARAM1);
//
//        }
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//
//        // Inflate the layout for this fragment
//        View rootView = inflater.inflate(R.layout.fragment_calendar_week, container, false);
//
//        status = getResources().getStringArray(R.array.status);
//        eventList = (ListView) rootView.findViewById(R.id.lvEvents);
//        rowData = new RowData();
//        currentDate = (TextView) rootView.findViewById(R.id.tvCurrentDate);
//        datePrev = (Button) rootView.findViewById(R.id.bDatePrev);
//        dateNext = (Button) rootView.findViewById(R.id.bDateNext);
//        datePrev.setOnClickListener(this);
//        dateNext.setOnClickListener(this);
//
//        calendar = Calendar.getInstance();
//        Calendar c = Calendar.getInstance();
//        c.setTime(calendar.getTime());
//        c.add(Calendar.DAY_OF_MONTH, 7);
//        String toDay = new SimpleDateFormat("dd MMMM yyyy").format(calendar.getTime());
//        String nextDay = new SimpleDateFormat("dd MMMM yyyy").format(c.getTime());
//        currentDate.setText(toDay + " - " + nextDay);
//        loadSessions();
//        eventList.setOnItemClickListener(this);
//        swipeDetector = new SwipeDetector(getActivity());
//        eventList.setOnTouchListener(swipeDetector);
//        return rootView;
//    }
//
//
//    @Override
//    public void onClick(View v) {
//        Calendar c = Calendar.getInstance();
//        switch (v.getId()) {
//            case R.id.bDatePrev:
//                calendar.add(Calendar.DAY_OF_MONTH, -7);
//                c.setTime(calendar.getTime());
//                loadSessions();
//                break;
//            case R.id.bDateNext:
//                calendar.add(Calendar.DAY_OF_MONTH, 7);
//                c.setTime(calendar.getTime());
//                loadSessions();
//
//                break;
//        }
//        c.add(Calendar.DAY_OF_MONTH, 7);
//        String toDay = new SimpleDateFormat("dd MMMM,yyyy").format(calendar.getTime());
//        String nextDay = new SimpleDateFormat("dd MMMM,yyyy").format(c.getTime());
//        currentDate.setText(toDay + " - " + nextDay);
//
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        loadSessions();
//    }
//
//    private void loadSessions() {
//
//
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(calendar.getTime());
//        cal.add(Calendar.DAY_OF_MONTH, 7);
//        String startDate = String.format("%d-%02d-%02d", calendar.get(Calendar.YEAR), (calendar.get(Calendar.MONTH) + 1), calendar.get(Calendar.DAY_OF_MONTH));
//        String endDate = String.format("%d-%02d-%02d", cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH) + 1), cal.get(Calendar.DAY_OF_MONTH));
//
//        Date stDate = null;
//        Date edDate = null;
//        try {
//            stDate = new SimpleDateFormat("yyyy-MM-dd").parse(startDate);
//            edDate = new SimpleDateFormat("yyyy-MM-dd").parse(endDate);
//            edDate.setTime(edDate.getTime() - 1000);
//        } catch (ParseException e) {
//            e.printStackTrace();
//            return;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return;
//        }
//
//        mapArrayList = new ArrayList<HashMap<String, Object>>();
//        SQLiteDatabase sqlDB = null;
//        try {
//            sqlDB = DatabaseHelper.instance().getReadableDatabase();
//
//            String query = "select * " + " from " +
//                    Table.Sessions.TABLE_NAME
//                    + " where " + Table.DELETED
//                    + " = 0 and ( ( " + Table.Sessions.START_DATE + " >=  date(\'" + startDate + "\') and " + Table.Sessions.START_DATE + " <=  (\'" + endDate
//                    + "\') ) OR  " + Table.Sessions.RECURRENCE_RULE + " IS NOT NULL " +
//            "       OR " + Table.Sessions.RECURRENCE_RULE + " != '' ) ";
//            Log.d("query is ", query);
//            assert sqlDB != null;
//            Cursor cursor = sqlDB
//                    .rawQuery(query
//                            , null);
//
//            Log.d("Calendar", "Week Count = " + cursor.getCount());
//
//            LinkedHashMap<String, Object> row;
//            while (cursor.moveToNext()) {
//                if (cursor.getString(cursor.getColumnIndex(
//                        Table.Sessions.RECURRENCE_RULE))== null ||
//                        cursor.getString(cursor.getColumnIndex(
//                                Table.Sessions.RECURRENCE_RULE)).equals("")) {
//
//                    row = new LinkedHashMap<String, Object>();
//                    row.put("_id", cursor.getString(cursor
//                            .getColumnIndex(Table.Sessions.ID)));
//                    row.put("name", Utils.dateConversionForRow(cursor.getString(cursor
//                            .getColumnIndex(Table.Sessions.START_DATE))) + " " + Utils.timeFormatAMPM(cursor.getString(cursor
//                            .getColumnIndex(Table.Sessions.START_TIME))));
//                    long groupSessionId = cursor.getInt(cursor
//                            .getColumnIndex(Table.Sessions.GROUP_ID));
//                    int session_type = cursor.getInt(cursor
//                            .getColumnIndex(Table.Sessions.SESSION_TYPE));
//                    rowData = getImageName(groupSessionId + "",
//                            session_type);
//                    row.put("rowId", groupSessionId);
//                    row.put("type", session_type);
//
//                    if (cursor.getInt(cursor
//                            .getColumnIndex(Table.Sessions.IS_NATIVE)) == 1) {
//
//                        row.put("secondLabel", cursor.getString(cursor
//                                .getColumnIndex(Table.Sessions.TITLE)));
//                        row.put("thirdLabel", getActivity().getString(
//                                R.string.lblNativeStatus));
//                        Log.d("Calendar", "native events");
//                    } else if (rowData.getImageName() != null && rowData.getImageName().length() > 0) {
//                        row.put("photo", rowData.getImageName());
//                        row.put("secondLabel", rowData.getPersonName());
//                        row.put("thirdLabel", status[cursor.getInt(cursor
//                                .getColumnIndex(Table.Sessions.SESSION_STATUS))]);
//
//                        Log.d("Calendar", "app events");
//                    }
//                    mapArrayList.add(row);
//                } else {
//                    String rule = cursor.getString(cursor.getColumnIndex(
//                            Table.Sessions.RECURRENCE_RULE));
//
//                    RecurrenceRule recRule = new RecurrenceRule(rule);
//                    RecurrenceRule.Freq freq = recRule.getFreq();
//
//                    Date d = new SimpleDateFormat("dd MMM yyyy").parse(Utils.formatConversionSQLite(cursor.getString(cursor
//                            .getColumnIndex(Table.Sessions.START_DATE))));
//
//                    switch (freq) {
//                        case MONTHLY:
//                            d.setMonth(stDate.getMonth());
//                        case YEARLY:
//                            d.setYear(stDate.getYear());
//                    }
//
//                    if (freq == RecurrenceRule.Freq.MONTHLY ||
//                            freq == RecurrenceRule.Freq.YEARLY) {
//                        if (!(d.after(stDate) && d.before(edDate))) continue;
//                    }
//
//                    ArrayList<Calendar> dates;
//                    dates = CalendarMonthViewFragment.ruleOccurONDate(rule, stDate, edDate);
//
//                    Log.e("RecurRule", "size = " + dates.size());
//
//                    if (dates.size() == 1) {
//
//                        if (freq == RecurrenceRule.Freq.DAILY
//                                || freq == RecurrenceRule.Freq.WEEKLY) {
//                            if (d.after(dates.get(0).getTime())) continue;
//                        }
//
//                        row = new LinkedHashMap<String, Object>();
//
//                        long groupSessionId = cursor.getInt(cursor
//                                .getColumnIndex(Table.Sessions.GROUP_ID));
//                        int session_type = cursor.getInt(cursor
//                                .getColumnIndex(Table.Sessions.SESSION_TYPE));
//                        rowData = getImageName(groupSessionId + "",
//                                session_type);
//                        row.put("_id", cursor.getString(cursor
//                                .getColumnIndex(Table.Sessions.ID)));
//                        row.put("name", Utils.formatConversionDateOnly(stDate) + " "
//                                + Utils.timeFormatAMPM(cursor.getString(cursor
//                                .getColumnIndex(Table.Sessions.START_TIME))));
//
//                        row.put("rowId", groupSessionId);
//                        row.put("type", session_type);
//
//                        if (cursor.getInt(cursor
//                                .getColumnIndex(Table.Sessions.IS_NATIVE)) == 1) {
//
//                            row.put("secondLabel", cursor.getString(cursor
//                                    .getColumnIndex(Table.Sessions.TITLE)));
//                            row.put("thirdLabel", getActivity().getString(
//                                    R.string.lblNativeStatus));
//                            Log.d("Calendar", "native events");
//                        } else if (rowData.getImageName() != null && rowData.getImageName().length() > 0) {
//                            row.put("photo", rowData.getImageName());
//                            row.put("secondLabel", rowData.getPersonName());
//                            row.put("thirdLabel", status[cursor.getInt(cursor
//                                    .getColumnIndex(Table.Sessions.SESSION_STATUS))]);
//
//                            Log.d("Calendar", "app events");
//                        }
//                        Log.d("Calendar", (String) row.get("secondLabel"));
//                        mapArrayList.add(row);
//                    } else if (dates.size() > 1) {
//                        for (Calendar c : dates)
//                            if (c.getTime().before(edDate)) {
//                                row = new LinkedHashMap<String, Object>();
//
//                                if (freq == RecurrenceRule.Freq.DAILY
//                                        || freq == RecurrenceRule.Freq.WEEKLY) {
//                                    if (d.after(c.getTime())) continue;
//                                }
//
//                                long groupSessionId = cursor.getInt(cursor
//                                        .getColumnIndex(Table.Sessions.GROUP_ID));
//                                int session_type = cursor.getInt(cursor
//                                        .getColumnIndex(Table.Sessions.SESSION_TYPE));
//                                rowData = getImageName(groupSessionId + "",
//                                        session_type);
//                                row.put("_id", cursor.getString(cursor
//                                        .getColumnIndex(Table.Sessions.ID)));
//                                row.put("name", Utils.formatConversionDateOnly(c.getTime()) + " "
//                                        + Utils.timeFormatAMPM(cursor.getString(cursor
//                                        .getColumnIndex(Table.Sessions.START_TIME))));
//
//                                row.put("rowId", groupSessionId);
//                                row.put("type", session_type);
//
//                                if (cursor.getInt(cursor
//                                        .getColumnIndex(Table.Sessions.IS_NATIVE)) == 1) {
//
//                                    row.put("secondLabel", cursor.getString(cursor
//                                            .getColumnIndex(Table.Sessions.TITLE)));
//                                    row.put("thirdLabel", getActivity().getString(
//                                            R.string.lblNativeStatus));
//                                    Log.d("Calendar", "native events");
//                                } else if (rowData.getImageName() != null && rowData.getImageName().length() > 0) {
//                                    row.put("photo", rowData.getImageName());
//                                    row.put("secondLabel", rowData.getPersonName());
//                                    row.put("thirdLabel", status[cursor.getInt(cursor
//                                            .getColumnIndex(Table.Sessions.SESSION_STATUS))]);
//
//                                    Log.d("Calendar", "app events");
//                                }
//                                Log.d("Calendar", (String) row.get("secondLabel"));
//                                mapArrayList.add(row);
//                            }
//                    }
//                }
//            }
//            cursor.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//        }
//       /*new SessionCustomAdapter(getActivity(),
//                R.layout.custom_training_row, R.id.bButton1, R.id.tvText1,
//                R.id.tvText2, R.id.tvText3, mapArrayList);*/
//        CustomAsyncTaskListAdapter adapter = new CustomAsyncTaskListAdapter(getActivity(),
//                R.layout.calendar_day_view_session_row, R.id.ivRowImage, R.id.tvFirstName, R.id.tvSecondLabel, R.id.tvThirdLabel, mapArrayList);
//
//        eventList.setAdapter(adapter);
//    }
//
//
//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//       /* ((MainActivity) activity).onSectionAttached(
//                getArguments().getInt(ARG_SECTION_NUMBER));*/
//    }
//
//    private RowData getImageName(String _id, int isGroup) {
//        String query = "";
//
//        if (isGroup == 0) {
//            query = "select  * "
//                    + " from " +
//                    Table.Client.TABLE_NAME +
//                    " where " + Table.DELETED + " = 0 "
//                    + " and " + Table.Client.ID + " = " + _id;
//        } else {
//
//            query = "select  * "
//                    + " from " +
//                    Table.Group.TABLE_NAME +
//                    " where " + Table.DELETED + " = 0 "
//                    + " and " + Table.Group.ID + " = " + _id;
//        }
//        SQLiteDatabase sqlDB = null;
//        try {
//            sqlDB = DatabaseHelper.instance().getReadableDatabase();
//
//
//            Log.d("query is ", query);
//            assert sqlDB != null;
//            Cursor cursor = sqlDB
//                    .rawQuery(query
//                            , null);
//
//
//            if (cursor.moveToFirst()) {
//
//
//                String imageName = (cursor.getString(cursor
//                        .getColumnIndex(Table.Client.PHOTO_URL)));
//                String personName;
//                if (isGroup == 0) {
//                    personName = cursor.getString(cursor
//                            .getColumnIndex(Table.Client.FIRST_NAME))
//                            + " "+ cursor.getString(cursor
//                            .getColumnIndex(Table.Client.LAST_NAME));
//                } else {
//                    personName = cursor.getString(cursor
//                            .getColumnIndex(Table.Group.NAME));
//                }
//
//                rowData.setImageName(imageName);
//                rowData.setPersonName(personName);
//            }
//            cursor.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//        }
//
//        return rowData;
//    }
//
//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//        Map map = mapArrayList.get(position);
//
//        if (swipeDetector.swipeDetected()) {
//            if (swipeDetector.getAction() == SwipeDetector.Action.RL) {
//                // showDeleteAlert(map.get("_id").toString());
//                // Toast.makeText(getActivity(), "Action Right to left", Toast.LENGTH_LONG).show();
//            } else {
//
//            }
//        } else {
//            startActivity(new Intent(getActivity(),
//                            ViewSessionActivity.class).putExtra("_id", map.get("_id").toString())
//            );
//        }
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        loadSessions();
//    }
//}
//
