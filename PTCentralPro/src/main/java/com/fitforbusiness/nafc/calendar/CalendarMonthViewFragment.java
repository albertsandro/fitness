package com.fitforbusiness.nafc.calendar;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
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

import org.apache.http.impl.cookie.DateUtils;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recur.RecurrenceRuleIterator;

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
import java.util.TimeZone;


public class CalendarMonthViewFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {
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
    public ArrayList<String> tempItems;
    ContentValues contentValues;
    ContentValues tempContentValues;
    GridView gridview;
    Calendar calendar;
    Calendar calendarCopy=Calendar.getInstance();
    private CalendarAdapter adapter;
    private TextView currentDate;
    private ListView eventList;
    private ArrayList<HashMap<String, Object>> mapArrayList = new ArrayList<>();
    private RowData rowData;
    private String[] status;
    private SwipeDetector swipeDetector;
    private String formattedDate;
    private TextView date;
    private String date_str;
    private List<Session> sqlList;
    public CalendarMonthViewFragment() {
        // Required empty public constructor
    }

    public static CalendarMonthViewFragment newInstance(int section) {
        CalendarMonthViewFragment fragment = new CalendarMonthViewFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, section);
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
        rowData = new RowData();
        contentValues = new ContentValues();
        tempContentValues = new ContentValues();
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_calendar_month_view, container, false);
        gridview = (GridView) rootView.findViewById(R.id.gridview);
        eventList = (ListView) rootView.findViewById(R.id.lvEvents);
        currentDate = (TextView) rootView.findViewById(R.id.tvCurrentDate);
        Button datePrev = (Button) rootView.findViewById(R.id.bDatePrev);
        Button dateNext = (Button) rootView.findViewById(R.id.bDateNext);
        datePrev.setOnClickListener(this);
        dateNext.setOnClickListener(this);
        items = new ArrayList<>();
        tempItems = new ArrayList<>();
        status = getResources().getStringArray(R.array.status);
        calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        formattedDate = sdf.format(calendar.getTime());

        currentDate.setText(new SimpleDateFormat("dd MMM yyyy").format(calendar.getTime()));
        setCalenderData(getActivity(), calendar);
        markParseCalendarDates();
        date = (TextView) rootView.findViewById(R.id.date);
        if (date instanceof TextView && !date.getText().equals("")) {
            loadParseSessions((contentValues.get(date.getText().toString()).toString()));
        }
        swipeDetector = new SwipeDetector(getActivity());
        eventList.setOnTouchListener(swipeDetector);
        eventList.setOnItemClickListener(this);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadParseSessions(formattedDate);
        markParseCalendarDates();

    }

    private void setCalenderData(Context context, final Calendar month) {
        adapter = new CalendarAdapter(context, month);
        gridview.setAdapter(adapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                TextView date = (TextView) arg1.findViewById(R.id.date);
                ImageView mark = (ImageView) arg1.findViewById(R.id.date_icon);
                if (date instanceof TextView && !date.getText().equals("")) {

                    String day = date.getText().toString();
                    if (day.length() == 1) {
                        day = "0" + day;
                    }
                    currentDate.setText(new SimpleDateFormat("dd MMM yyyy").format(month.getTime()));
                    if (mark.getVisibility() == View.VISIBLE) {
                        try {
                            loadParseSessions((contentValues.get(date.getText().toString()).toString()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        //    Log.d("Mark date is ", contentValues.get(day).toString());
                    } else {
                        loadParseSessions("");
                    }
                   /* detail.setText(" ");
                     today = (month.get(Calendar.MONTH) + 1) + "/"
                            + date.getText().toString() + "/"
                            + month.get(Calendar.YEAR);*/
                    Log.d("date is ", date.getText().toString());
                }
            }
        });
        handler = new Handler();
        handler.post(calendarUpdater);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bDatePrev:
                mapArrayList.clear();
                calendar.add(Calendar.MONTH, -1);
                currentDate.setText(new SimpleDateFormat("MMMMMM,yyyy").format(calendar.getTime()));
                setCalenderData(getActivity(), calendar);
                markParseCalendarDates();
                break;
            case R.id.bDateNext:
                mapArrayList.clear();
                calendar.add(Calendar.MONTH, 1);
                currentDate.setText(new SimpleDateFormat("MMMMMM,yyyy").format(calendar.getTime()));
                setCalenderData(getActivity(), calendar);
                markParseCalendarDates();
                break;
        }
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

    public void markParseCalendarDates() {
        items.clear();
        calendarCopy.clear();
        calendarCopy.setTime(calendar.getTime());
        Log.v("markParseCalendarDates","starting");
        //mapArrayList = new ArrayList<HashMap<String, Object>>();
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
                            public void done(List<Session> list_, com.parse.ParseException e) {
                                if (e == null && list_ != null) {
                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(calendarCopy.getTime());
                                    cal.add(Calendar.DAY_OF_MONTH, 0);
                                    Date startDate=cal.getTime();
                                    Calendar cal1=Calendar.getInstance();
                                    cal1.setTime(calendarCopy.getTime());
                                    cal1.add(Calendar.DAY_OF_MONTH,calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                                    Date endDate=cal1.getTime();
                                    for (Session session : list_) {
                                        Date date=convertStringToDate(session.getStartDate());
                                        if ((date.after(startDate) && (date.before(endDate)))||
                                                (date.getMonth()==startDate.getMonth() && date.getDate()==startDate.getDate())
                                                ||(date.getMonth()==endDate.getMonth() && date.getDate()==endDate.getDate())) {
                                            try {
                                                Date d = convertStringToDate(session.getStartDate());
                                                cal.setTime(d);
                                                Log.d("item", cal.get(Calendar.DAY_OF_MONTH) + "");
                                                items.add(cal.get(Calendar.DAY_OF_MONTH) + "");
                                                contentValues.put(cal.get(Calendar.DAY_OF_MONTH) + "",
                                                        (session.getString("startDate")));
                                            } catch (Exception d) {
                                                d.printStackTrace();
                                            } finally {
                                            }
                                        }
                                        Session.pinAllInBackground(list_);
                                        setCalenderData(getActivity(), calendarCopy);
                                    }
                                }
                            }
                        });
                    } else {
                        Log.v("markParseCalendarDates","starting1");
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(calendarCopy.getTime());
                        cal.add(Calendar.DAY_OF_MONTH, 0);
                        Date startDate=cal.getTime();
//                        Calendar cal1=Calendar.getInstance();
//                        cal1.setTime(calendar.getTime());
                        cal.add(Calendar.DAY_OF_MONTH,calendarCopy.getActualMaximum(Calendar.DAY_OF_MONTH)-1);
                        Date endDate=cal.getTime();
                        for (Session session : list) {
                            Log.v("markParseCalendarDates","starting2");
                            Date date=convertStringToDate(session.getStartDate());
                            if ((date.after(startDate) && (date.before(endDate)))||
                                    (date.getMonth()==startDate.getMonth() && date.getDate()==startDate.getDate())
                                    ||(date.getMonth()==endDate.getMonth() && date.getDate()==endDate.getDate())) {
                                try {
                                    Log.v("markParseCalendarDates","starting3");
                                    Date d = convertStringToDate(session.getStartDate());
                                    cal.setTime(d);
                                    Log.d("item", cal.get(Calendar.DAY_OF_MONTH) + "");
                                    items.add(cal.get(Calendar.DAY_OF_MONTH) + "");
                                    contentValues.put(cal.get(Calendar.DAY_OF_MONTH) + "",
                                            (session.getString("startDate")));
                                } catch (Exception d) {
                                    d.printStackTrace();
                                } finally {
                                }
                            }
                            Log.v("markParseCalendarDates","starting4");
                            setCalenderData(getActivity(), calendarCopy);
                        }
                    }
                }
            }
        });
    }

    private void markCalendarDates() {
        items.clear();
        String startDate = String.format("%d-%02d-%02d", calendar.get(Calendar.YEAR), (calendar.get(Calendar.MONTH) + 1), 1);
        String endDate = String.format("%d-%02d-%02d", calendar.get(Calendar.YEAR), (calendar.get(Calendar.MONTH) + 1), calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date stDate = null;
        Date edDate = null;
        try {
            stDate = new SimpleDateFormat("yyyy-MM-dd").parse(startDate);
            stDate.setTime(stDate.getTime() - 1000);
            edDate = new SimpleDateFormat("yyyy-MM-dd").parse(endDate);
            edDate.setTime(edDate.getTime() + (24 * 60 * 60 * 1000));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();

            String query = "select * " + " from " +
                    Table.Sessions.TABLE_NAME
                    + " where " + Table.DELETED
                    + " = 0 and ( ( " + Table.Sessions.START_DATE + " >=  datetime(\'"
                    + startDate + "\') and " + Table.Sessions.START_DATE + " <=  datetime(\'" + endDate
                    + "\') ) OR  " + Table.Sessions.RECURRENCE_RULE + " IS NOT NULL " +
                    "       OR " + Table.Sessions.RECURRENCE_RULE + " != '' ) " +
                    " group by " + Table.Sessions.START_DATE;

            Log.d("query is ", query);
            assert sqlDB != null;
            Cursor cursor = sqlDB.rawQuery(query, null);
            Log.e("RecurRule", "count = " + cursor.getCount());
            while (cursor.moveToNext()) {
                if (cursor.getString(cursor.getColumnIndex(
                        Table.Sessions.RECURRENCE_RULE))== null ||
                        cursor.getString(cursor.getColumnIndex(
                        Table.Sessions.RECURRENCE_RULE)).equals("")) {

                    Calendar cal = Calendar.getInstance();
                    Date d = new SimpleDateFormat("dd MMM yyyy").parse(Utils.formatConversionSQLite(cursor.getString(cursor
                            .getColumnIndex(Table.Sessions.START_DATE))));
                    cal.setTime(d);
                    Log.d("item", cal.get(Calendar.DAY_OF_MONTH) + "");
                    items.add(cal.get(Calendar.DAY_OF_MONTH) + "");
                    contentValues.put(cal.get(Calendar.DAY_OF_MONTH) + "", cursor.getString(cursor
                            .getColumnIndex(Table.Sessions.START_DATE)));
                } else {
                    String rule = cursor.getString(cursor.getColumnIndex(
                            Table.Sessions.RECURRENCE_RULE));

                    Date d = new SimpleDateFormat("dd MMM yyyy").parse(Utils.formatConversionSQLite(cursor.getString(cursor
                            .getColumnIndex(Table.Sessions.START_DATE))));

                    ArrayList<Calendar> dates;
                    if (d.after(stDate))
                        dates = ruleOccurONDate(rule, d, edDate);
                    else
                        dates = ruleOccurONDate(rule, stDate, edDate);

                    Log.e("RecurRule", "size = " + dates.size());

                    if (dates.size() == 1) {
                        Calendar cal = Calendar.getInstance();

                        d.setYear(stDate.getYear());
                        if (d.after(stDate) && d.before(edDate)) {
                            cal.setTime(d);
                            Log.d("item", cal.get(Calendar.DAY_OF_MONTH) + "");
                            items.add(cal.get(Calendar.DAY_OF_MONTH) + "");
                            contentValues.put(cal.get(Calendar.DAY_OF_MONTH) + "", cursor.getString(cursor
                                    .getColumnIndex(Table.Sessions.START_DATE)));
                        }
                    } else if (dates.size() > 1) {
                        for (Calendar c : dates)
                            if (c.getTime().after(stDate) && c.getTime().before(edDate)) {
                                Log.d("item", c.get(Calendar.DAY_OF_MONTH) + " - " + c.getTime()
                                        + " - " + Utils.formatConversionLocale(c.getTime()));
                                items.add(c.get(Calendar.DAY_OF_MONTH) + "");

                                contentValues.put(c.get(Calendar.DAY_OF_MONTH) + "",
                                        Utils.formatConversionLocale(c.getTime()));
                            }
                    }
                }
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private void loadParseSessions(String date) {
        Log.v("reload","starting");
       if (date!="") {
           date_str = date;
           Date stDate = null;
           Date edDate = null;
           try {
               stDate = new SimpleDateFormat("dd MMM yyyy").parse(date);
               stDate.setTime(stDate.getTime() - 1000);
               edDate = new SimpleDateFormat("dd MMM yyyy").parse(date);
               edDate.setTime(edDate.getTime() + (24 * 60 * 60 * 1000));
               edDate.setTime(edDate.getTime() - 1000);
           } catch (Exception e) {
               e.printStackTrace();
               return;
           }
           mapArrayList = new ArrayList<HashMap<String, Object>>();
           ParseQuery parseQuery = new ParseQuery(Session.class);
           parseQuery.fromLocalDatastore();
           parseQuery.whereEqualTo("trainer", Trainer.getCurrent());
           parseQuery.whereEqualTo("startDate",date);
           parseQuery.findInBackground(new FindCallback<Session>() {
               @Override
               public void done(List<Session> list, com.parse.ParseException e) {
                   if (e == null && list != null) {
                       if (list.size() == 0) {
                           ParseQuery parseQuery = new ParseQuery(Session.class);
                           parseQuery.whereEqualTo("trainer", Trainer.getCurrent());
                           parseQuery.whereEqualTo("startDate",date_str);
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
        else{
           mapArrayList.clear();
           CustomAsyncTaskListAdapter adapter = new CustomAsyncTaskListAdapter(getActivity(),
                   R.layout.calendar_day_view_session_row, R.id.ivRowImage, R.id.tvFirstName, R.id.tvSecondLabel, R.id.tvThirdLabel, mapArrayList);
           eventList.setAdapter(adapter);
       }
    }
    
    private void loadIntoListView(List<Session> list) {
        if (list!=null && list.size()!=0) {
            LinkedHashMap<String, Object> row;
            for (Session session : list) {
                row = new LinkedHashMap<String, Object>();
                row.put("_id", session.getObjectId());
                row.put("firstLabel", session.getObjectId());
                row.put("secondLabel", session.getTitle());
                row.put("thirdLabel", Utils.dateConversionForRow(session.getString("startDate")) + " " + Utils.timeFormatAMPM(session.getString("endDate")));

                String fileObjectStr;
                if ((int)session.getSessionType()==1) {
                     fileObjectStr=Group.createWithoutData(Group.class, session.getGroupClientId()).getImageFile();
                }else{
                    fileObjectStr=Client.createWithoutData(Client.class, session.getGroupClientId()).getImageFile();
                }
                Bitmap bmp;
                byte[] fileObject = android.util.Base64.decode(fileObjectStr, 1);
                bmp = BitmapFactory.decodeByteArray(fileObject, 0, fileObject.length);
                row.put("photo", bmp);

                mapArrayList.add(row);
            }

            CustomAsyncTaskListAdapter adapter = new CustomAsyncTaskListAdapter(getActivity(),
                    R.layout.calendar_day_view_session_row, R.id.ivRowImage, R.id.tvFirstName, R.id.tvSecondLabel, R.id.tvThirdLabel, mapArrayList);
            eventList.setAdapter(adapter);
        }else{
            mapArrayList.clear();
            CustomAsyncTaskListAdapter adapter = new CustomAsyncTaskListAdapter(getActivity(),
                    R.layout.calendar_day_view_session_row, R.id.ivRowImage, R.id.tvFirstName, R.id.tvSecondLabel, R.id.tvThirdLabel, mapArrayList);
            eventList.setAdapter(adapter);
        }
    }
    private void loadSessions(String date) {
        Date stDate = null;
        Date edDate = null;
        try {
            stDate = new SimpleDateFormat("yyyy-MM-dd").parse(date);
            stDate.setTime(stDate.getTime() - 1000);
            edDate = new SimpleDateFormat("yyyy-MM-dd").parse(date);
            edDate.setTime(edDate.getTime() + (24 * 60 * 60 * 1000));
            edDate.setTime(edDate.getTime() - 1000);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        mapArrayList = new ArrayList<>();
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();

            String query = "select  " + Table.Sessions.ID + " , "
                    + Table.Sessions.START_DATE + " , "
                    + Table.Sessions.START_TIME + " , "
                    + Table.Sessions.SESSION_STATUS + " , "
                    + Table.Sessions.GROUP_ID + " , "
                    + Table.Sessions.IS_NATIVE + " , "
                    + Table.Sessions.RECURRENCE_RULE + " , "
                    + Table.Sessions.TITLE + " , "
                    + Table.Sessions.SESSION_TYPE + "  "
                    + " from " +
                    Table.Sessions.TABLE_NAME +
                    " where " + Table.DELETED + " = 0 "
                    + " and ( " + Table.Sessions.START_DATE + " =  \'" + date + "\'" +
                    " OR  " + Table.Sessions.RECURRENCE_RULE + " IS NOT NULL " +
                    "       OR " + Table.Sessions.RECURRENCE_RULE + " != '' ) " ;

            Log.d("query is ", query);
            assert sqlDB != null;
            Cursor cursor = sqlDB
                    .rawQuery(query
                            , null);

            Log.d("Calendar", "Month Count = " + cursor.getCount());

            LinkedHashMap<String, Object> row;
            while (cursor.moveToNext()) {

                if (cursor.getString(cursor.getColumnIndex(
                        Table.Sessions.RECURRENCE_RULE))== null ||
                        cursor.getString(cursor.getColumnIndex(
                                Table.Sessions.RECURRENCE_RULE)).equals("")) {

                    row = new LinkedHashMap<>();

                    long groupSessionId = cursor.getInt(cursor
                            .getColumnIndex(Table.Sessions.GROUP_ID));
                    int session_type = cursor.getInt(cursor
                            .getColumnIndex(Table.Sessions.SESSION_TYPE));
                    rowData = getImageName(groupSessionId + "",
                            session_type);

                    row.put("_id", cursor.getString(cursor
                            .getColumnIndex(Table.Sessions.ID)));
                    row.put("name", Utils.dateConversionForRow(cursor.getString(cursor
                            .getColumnIndex(Table.Sessions.START_DATE))) + " "
                            + Utils.timeFormatAMPM(cursor.getString(cursor
                            .getColumnIndex(Table.Sessions.START_TIME))));


                    row.put("rowId", groupSessionId);
                    row.put("type", session_type);

                    if (cursor.getInt(cursor
                            .getColumnIndex(Table.Sessions.IS_NATIVE)) == 1) {

                        row.put("secondLabel", cursor.getString(cursor
                                .getColumnIndex(Table.Sessions.TITLE)));
                        row.put("thirdLabel", getActivity().getString(
                                R.string.lblNativeStatus));
                        Log.d("Calendar", "native events");
                    } else if (rowData.getImageName() != null && rowData.getImageName().length() > 0) {
                        row.put("photo", rowData.getImageName());
                        row.put("secondLabel", rowData.getPersonName());
                        row.put("thirdLabel", status[cursor.getInt(cursor
                                .getColumnIndex(Table.Sessions.SESSION_STATUS))]);

                        Log.d("Calendar", "app events");
                    }
                    Log.d("Calendar", (String) row.get("secondLabel"));
                    mapArrayList.add(row);
                } else {
                    String rule = cursor.getString(cursor.getColumnIndex(
                            Table.Sessions.RECURRENCE_RULE));

                    RecurrenceRule recRule = new RecurrenceRule(rule);
                    RecurrenceRule.Freq freq = recRule.getFreq();

                    Date d = new SimpleDateFormat("dd MMM yyyy").parse(Utils.formatConversionSQLite(cursor.getString(cursor
                            .getColumnIndex(Table.Sessions.START_DATE))));

                    switch (freq) {
                        case MONTHLY:
                            d.setMonth(stDate.getMonth());
                        case YEARLY:
                            d.setYear(stDate.getYear());
                    }

                    if (freq == RecurrenceRule.Freq.MONTHLY ||
                            freq == RecurrenceRule.Freq.YEARLY) {
                        if (!(d.after(stDate) && d.before(edDate))) continue;
                    }

                    ArrayList<Calendar> dates;
                    dates = ruleOccurONDate(rule, stDate, edDate);

                    Log.e("RecurRule", "size = " + dates.size());

                    if (dates.size() > 0) {

                        if (freq == RecurrenceRule.Freq.DAILY
                                || freq == RecurrenceRule.Freq.WEEKLY) {
                            if (d.after(dates.get(0).getTime())) continue;
                        }

                        row = new LinkedHashMap<>();

                        long groupSessionId = cursor.getInt(cursor
                                .getColumnIndex(Table.Sessions.GROUP_ID));
                        int session_type = cursor.getInt(cursor
                                .getColumnIndex(Table.Sessions.SESSION_TYPE));
                        rowData = getImageName(groupSessionId + "",
                                session_type);
                        row.put("_id", cursor.getString(cursor
                                .getColumnIndex(Table.Sessions.ID)));
                        row.put("name", Utils.formatConversionDateOnly(stDate) + " "
                                + Utils.timeFormatAMPM(cursor.getString(cursor
                                .getColumnIndex(Table.Sessions.START_TIME))));

                        row.put("rowId", groupSessionId);
                        row.put("type", session_type);

                        if (cursor.getInt(cursor
                                .getColumnIndex(Table.Sessions.IS_NATIVE)) == 1) {

                            row.put("secondLabel", cursor.getString(cursor
                                    .getColumnIndex(Table.Sessions.TITLE)));
                            row.put("thirdLabel", getActivity().getString(
                                    R.string.lblNativeStatus));
                            Log.d("Calendar", "native events");
                        } else if (rowData.getImageName() != null && rowData.getImageName().length() > 0) {
                            row.put("photo", rowData.getImageName());
                            row.put("secondLabel", rowData.getPersonName());
                            row.put("thirdLabel", status[cursor.getInt(cursor
                                    .getColumnIndex(Table.Sessions.SESSION_STATUS))]);

                            Log.d("Calendar", "app events");
                        }
                        Log.d("Calendar", (String) row.get("secondLabel"));
                        mapArrayList.add(row);
                    }
                }
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        CustomAsyncTaskListAdapter adapter = new CustomAsyncTaskListAdapter(getActivity(),
                R.layout.calendar_day_view_session_row, R.id.ivRowImage, R.id.tvFirstName, R.id.tvSecondLabel, R.id.tvThirdLabel, mapArrayList);
        eventList.setAdapter(adapter);
    }

    private RowData getImageName(String _id, int isGroup) {
        String query;

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
        SQLiteDatabase sqlDB;
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
            e.printStackTrace();
        }

        return rowData;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            setHasOptionsMenu(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // inflater.inflate(R.menu.menu_acc, menu);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
       /* ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));*/
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
        } else
        {
            Log.d("startActivityForResult", "go");
            startActivityForResult(new Intent(getActivity(),
                            ViewSessionActivity.class).putExtra("_id", map.get("_id").toString())
            ,123);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==123) {
            Log.d("onActivityResult", "before");
            loadParseSessions(formattedDate);
            markParseCalendarDates();
            Log.d("onActivityResult", "reFresh");
        }
    }

    public static ArrayList<Calendar> ruleOccurONDate (String strRule, Date startD, Date endD) {
        Log.e("RecurRule", "Start: Rule = " + strRule);
        RecurrenceRule rule;
        ArrayList<Calendar> calendars = new ArrayList<>();
        try {
            rule = new RecurrenceRule(strRule);
            RecurrenceRule.Freq freq = rule.getFreq();

            int freqInDays = -1;
            switch (freq) {
                case DAILY:
                    freqInDays = 1;
                    break;
                case WEEKLY:
                    freqInDays = 7;
                    break;
            }


            RecurrenceRuleIterator it = rule.iterator(startD.getTime(), TimeZone.getDefault());

            int maxInstances = 31; // limit instances for rules that recur forever

            while (it.hasNext() && maxInstances-- > 0) {

                long nextInstance = it.nextMillis();
                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date(nextInstance));

                Calendar peekCal = null;
                if (it.hasNext()) {
                    long peekInstance = it.peekMillis();
                    peekCal = Calendar.getInstance();
                    peekCal.setTime(new Date(peekInstance));
                }

                if (new Date(nextInstance).after(endD)) break;

                if (it.hasNext())
                    if (freqInDays != -1 &&
                        peekCal.get(Calendar.DAY_OF_MONTH)
                            - cal.get(Calendar.DAY_OF_MONTH) != freqInDays) continue;

                Log.e("RecurRule", "Date = " + Utils.formatConversionLocale(cal.getTime()));
                calendars.add(cal);

            }
        } catch (InvalidRecurrenceRuleException e) {
            e.printStackTrace();
            Log.e("RecurRule", "error");
        }
        return calendars;
    }

    @Override
    public void onRefresh() {
        loadParseSessions(formattedDate);
        markParseCalendarDates();
    }
}
