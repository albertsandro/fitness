package com.fitforbusiness.nafc.dashboard;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.fitforbusiness.Parse.Models.Client;
import com.fitforbusiness.Parse.Models.Group;
import com.fitforbusiness.Parse.Models.Session;
import com.fitforbusiness.Parse.Models.Status;
import com.fitforbusiness.Parse.Models.Trainer;
import com.fitforbusiness.database.DatabaseHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.framework.CustomAsyncTaskListAdapter;
import com.fitforbusiness.framework.RowData;
import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.nafc.R;
import com.fitforbusiness.nafc.calendar.CalendarMonthViewFragment;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseFile;
import com.parse.ParseQuery;

import org.dmfs.rfc5545.recur.RecurrenceRule;

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


public class DashBoardFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private String mParam1;
    private String mParam2;
    private String[] status;
    private ListView sessionList;
    private RowData rowData;
    private SimpleAdapter adapter;
    private TextView totalCecPoints, totalCecHours;
    ArrayList<HashMap<String, Object>> mapArrayList = new ArrayList<>();
    List<Client> mapclientList = new ArrayList<>();
    List<Group> mapgroupList = new ArrayList<>();
    List<Group> mapStatusList = new ArrayList<>();
    Calendar calendar;
    ListView notifications;
    byte[] file;


    public DashBoardFragment() {
        // Required empty public constructor
    }

    public static DashBoardFragment newInstance(int position) {
        DashBoardFragment fragment = new DashBoardFragment();
        Bundle args = new Bundle();

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
        // Inflate the layout for this fragment
        rowData = new RowData();
        status = getResources().getStringArray(R.array.status);
        View rootView = inflater.inflate(R.layout.fragment_dash_board, container, false);
        notifications = (ListView) rootView.findViewById(R.id.lvNotifications);

        sessionList = (ListView) rootView.findViewById(R.id.lvSessionList);


        totalCecPoints = (TextView) rootView.findViewById(R.id.tvTotalCecPoints);
        totalCecHours = (TextView) rootView.findViewById(R.id.tvTotalCecHours);

//         sessionList.addFooterView(footerView);
//        loadSessions("");
        loadParseSessions();
//        setTopExercises();
        setBusinessAlerts();
        loadQualificationPoint();
        loadQualificationHours();
        notifications.setAdapter(adapter);

        return rootView;
    }

    private void setTopExercises() {


    }

    @Override
    public void onResume() {
        super.onResume();
        loadSessions("");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onDetach() {
        super.onDetach();

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

    private void loadParseSessions() {
        calendar=Calendar.getInstance();
        final String startDateStr =convertDateToString(calendar.getTime());

        mapArrayList = new ArrayList<HashMap<String, Object>>();
        ParseQuery parseQuery = new ParseQuery(Session.class);
        parseQuery.fromLocalDatastore();
        parseQuery.whereEqualTo("trainer", Trainer.getCurrent());
        parseQuery.whereEqualTo("startDate", startDateStr);
        parseQuery.findInBackground(new FindCallback<Session>() {
            @Override
            public void done(List<Session> list, com.parse.ParseException e) {
                if (e == null && list != null) {
                    if (list.size() == 0) {
//                        ParseQuery parseQuery = new ParseQuery(Session.class);
//                        parseQuery.whereEqualTo("trainer", Trainer.getCurrent());
//                        parseQuery.whereEqualTo("startDate", startDateStr);
//                        parseQuery.findInBackground(new FindCallback<Session>() {
//                            @Override
//                            public void done(List<Session> list, com.parse.ParseException e) {
//                                if (e == null && list.size() != 0) {
//                                    loadIntoListView(list);
// //                                   Session.pinAllInBackground(list);
//                                }
//                            }
//                        });
                    } else {
                        loadIntoListView(list);
                    }
                }
            }
        });
    }

    private void loadIntoListView(List<Session> list) {
        if (list!=null && list.size()!=0) {
            LinkedHashMap<String, Object> row;
            if (list != null && list.size() != 0) {
                for (Session session : list) {
                    row = new LinkedHashMap<String, Object>();
                    row.put("secondLabel", session.getStartTime());
                    if (session.getSessionType().equals("1")) {
                        ParseQuery parseQuery = new ParseQuery(Group.class);
                        parseQuery.fromLocalDatastore();
                        parseQuery.whereEqualTo("objectId", session.getGroupClientId());
                        mapgroupList = new ArrayList<Group>();
                        try {
                            mapgroupList = parseQuery.find();
                        } catch (com.parse.ParseException e1) {
                        }
                        if (mapclientList == null) {
                            ParseQuery parseQuery1 = new ParseQuery(Group.class);
                            parseQuery1.whereEqualTo("objectId", session.getGroupClientId());
                            mapgroupList = new ArrayList<Group>();
                            try {
                                mapgroupList = parseQuery1.find();
                            } catch (com.parse.ParseException e1) {
                            }
                            for (Group group : mapgroupList) {
                                row.put("name", group.getName());
                                if (group.getImageFile() != null) {
                                    String fileObjectStr = group.getImageFile();
                                    byte[] fileObject = android.util.Base64.decode(fileObjectStr, 1);
                                    Bitmap bmp = BitmapFactory.decodeByteArray(fileObject, 0, fileObject.length);
                                    row.put("photo", bmp);
                                } else {
                                    row.put("photo", null);
                                }
                            }
                        } else {
                            for (Group group : mapgroupList) {
                                row.put("name", group.getName());
                                if (group.getImageFile() != null) {
                                    String fileObjectStr = group.getImageFile();
                                    byte[] fileObject = android.util.Base64.decode(fileObjectStr, 1);
                                    Bitmap bmp = BitmapFactory.decodeByteArray(fileObject, 0, fileObject.length);
                                    row.put("photo", bmp);
                                } else {
                                    row.put("photo", null);
                                }
                            }
                        }
                    } else {
                        ParseQuery parseQuery = new ParseQuery(Client.class);
                        parseQuery.fromLocalDatastore();
                        parseQuery.whereEqualTo("objectId", session.getGroupClientId());
                        mapclientList = new ArrayList<Client>();
                        try {
                            mapclientList = parseQuery.find();
                        } catch (com.parse.ParseException e1) {
                        }
                        if (mapclientList == null) {
                            ParseQuery parseQuery1 = new ParseQuery(Client.class);
                            parseQuery1.whereEqualTo("objectId", session.getGroupClientId());
                            mapclientList = new ArrayList<Client>();
                            try {
                                mapclientList = parseQuery1.find();
                            } catch (com.parse.ParseException e1) {
                            }
                            for (Client client : mapclientList) {
                                row.put("name", client.getFirstName());
                                if (client.getImageFile() != null) {
                                    String fileObjectStr = client.getImageFile();
                                    byte[] fileObject = android.util.Base64.decode(fileObjectStr, 1);
                                    Bitmap bmp = BitmapFactory.decodeByteArray(fileObject, 0, fileObject.length);
                                    row.put("photo", bmp);
                                } else {
                                    row.put("photo", null);
                                }
                            }
                        } else {
                            for (Client client : mapclientList) {
                                row.put("name", client.getFirstName());
                                if (client.getImageFile() != null) {
                                    String fileObjectStr = client.getImageFile();
                                    byte[] fileObject = android.util.Base64.decode(fileObjectStr, 1);
                                    Bitmap bmp = BitmapFactory.decodeByteArray(fileObject, 0, fileObject.length);
                                    row.put("photo", bmp);
                                } else {
                                    row.put("photo", null);
                                }
                            }
                        }
                    }
                    row.put("_id", session.getObjectId());
//            row.put("secondLabel", session.getSessionType());
                    ParseQuery parseQuery = new ParseQuery(Status.class);
                    parseQuery.fromLocalDatastore();
                    try {
                        mapStatusList = parseQuery.find();
                    } catch (com.parse.ParseException e) {
                    }
                    if (mapStatusList == null || mapStatusList.size() == 0) {
                        ParseQuery parseQuery1 = new ParseQuery(Status.class);
                        parseQuery1.fromLocalDatastore();
                        try {
                            mapStatusList = parseQuery.find();
                        } catch (com.parse.ParseException e) {
                        }
                        if (mapStatusList != null && mapStatusList.size() != 0) {
                            row.put("thirdLabel", mapStatusList.get((int) session.getStatus()).getName());
                        }
                    } else {
                        row.put("thirdLabel", mapStatusList.get((int) session.getStatus()).getName());
                    }

                    row.put("secondLabel", session.getStartTime());
                    mapArrayList.add(row);
                }
                if (mapArrayList != null && mapArrayList.size() != 0) {
                    CustomAsyncTaskListAdapter adapter = new CustomAsyncTaskListAdapter(getActivity(),
                            R.layout.calendar_day_view_session_row, R.id.ivRowImage, R.id.tvFirstName,
                            R.id.tvSecondLabel, R.id.tvThirdLabel, mapArrayList);
                    sessionList.setAdapter(adapter);
                }
            }
        }

    }
    private void loadSessions(String date) {

        Calendar calendar = Calendar.getInstance();
        date = String.format("%d-%02d-%02d", calendar.get(Calendar.YEAR),
                (calendar.get(Calendar.MONTH) + 1), calendar.get(Calendar.DAY_OF_MONTH));
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

        ArrayList<HashMap<String, Object>> mapArrayList = new ArrayList<>();
        SQLiteDatabase sqlDB;
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
                    + " and ( " + Table.Sessions.START_DATE + " =  datetime(\'" + date + "\') " +
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

                    row = new LinkedHashMap<String, Object>();

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
                    dates = CalendarMonthViewFragment.ruleOccurONDate(rule, stDate, edDate);

                    Log.e("RecurRule", "size = " + dates.size());

                    if (dates.size() > 0) {
                        row = new LinkedHashMap<String, Object>();

                        if (freq == RecurrenceRule.Freq.DAILY
                                || freq == RecurrenceRule.Freq.WEEKLY) {
                            if (d.after(dates.get(0).getTime())) continue;
                        }

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
                R.layout.calendar_day_view_session_row, R.id.ivRowImage, R.id.tvFirstName,
                R.id.tvSecondLabel, R.id.tvThirdLabel, mapArrayList);
        sessionList.setAdapter(adapter);
    }


    private void loadSessions() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
        String startDate;
        startDate = sdf.format(Calendar.getInstance().getTime());
        ArrayList<HashMap<String, Object>> mapSessionArray = new ArrayList<HashMap<String, Object>>();
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            String query = "select * " + " from " +
                    Table.Sessions.TABLE_NAME
                    + " where " + Table.DELETED
                    + " = 0 and " + Table.Sessions.START_DATE + " = \'" + startDate
                    + "\'";
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
                row.put("name", Utils.timeFormatAMPM(cursor.getString(cursor
                        .getColumnIndex(Table.Sessions.START_TIME))));
                rowData = getImageName(cursor.getString(cursor.getColumnIndex(Table.Sessions.GROUP_ID)), cursor.getInt(cursor
                        .getColumnIndex(Table.Sessions.SESSION_TYPE)));
                row.put("photo",
                        rowData.getImageName());
                row.put("secondLabel", rowData.getPersonName());
                row.put("thirdLabel", status[cursor.getInt(cursor
                        .getColumnIndex(Table.Sessions.SESSION_STATUS))]);
                mapSessionArray.add(row);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        CustomAsyncTaskListAdapter adapter = new CustomAsyncTaskListAdapter(getActivity(),
                R.layout.calendar_day_view_session_row, R.id.ivRowImage,
                R.id.tvFirstName, R.id.tvSecondLabel, R.id.tvThirdLabel, mapSessionArray);
        sessionList.setAdapter(adapter);
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
            e.printStackTrace();
        } finally {
        }

        return rowData;
    }

    private void setBusinessAlerts() {

        ArrayList<Map<String, String>> mapArrayList = new ArrayList<Map<String, String>>();
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();

            String query = "select julianday(pt_license_renewal_date)-julianday('now')  as pt_license, " +
                    " julianday(first_aid_cert_renewal_date)-julianday('now')  as first_aid, " +
                    " julianday(cpr_cert_renewal_date)-julianday('now')  as cpr_cert, " +
                    " julianday(aed_cert_renewal_date)-julianday('now')  as aed_cert from trainer";

            Log.d("query is ", query);
            Cursor cursor = sqlDB
                    .rawQuery(query
                            , null);

            LinkedHashMap<String, String> row;
            if (cursor.moveToFirst()) {
                if (cursor.getFloat(cursor
                        .getColumnIndex("pt_license")) < 90) {
                    row = new LinkedHashMap<String, String>();
                    if (cursor.getFloat(cursor
                            .getColumnIndex("pt_license")) < 0) {
                        row.put("title", "PT License is overdue.");
                    } else {
                        row.put("title", "PT License is due for renewal.");
                    }

                    row.put("no_of_days", (int) cursor.getFloat(cursor
                            .getColumnIndex("pt_license")) + " day");
                    mapArrayList.add(row);
                }
                if (cursor.getFloat(cursor
                        .getColumnIndex("first_aid")) < 90) {
                    row = new LinkedHashMap<String, String>();
                    if (cursor.getFloat(cursor
                            .getColumnIndex("first_aid")) < 0) {
                        row.put("title", "First Aid is overdue.");
                    } else {
                        row.put("title", "First Aid is due for renewal.");
                    }

                    row.put("no_of_days", (int) cursor.getFloat(cursor
                            .getColumnIndex("first_aid")) + " day");
                    mapArrayList.add(row);
                }
                if (cursor.getFloat(cursor
                        .getColumnIndex("cpr_cert")) < 90) {
                    row = new LinkedHashMap<String, String>();
                    if (cursor.getFloat(cursor
                            .getColumnIndex("cpr_cert")) < 0) {
                        row.put("title", "CPR Certificate is overdue.");
                    } else {
                        row.put("title", "CPR Certificate is due for renewal.");
                    }

                    row.put("no_of_days", (int) cursor.getFloat(cursor
                            .getColumnIndex("cpr_cert")) + " day");
                    mapArrayList.add(row);
                }
                if (cursor.getFloat(cursor
                        .getColumnIndex("aed_cert")) < 90) {
                    row = new LinkedHashMap<String, String>();
                    if (cursor.getFloat(cursor
                            .getColumnIndex("aed_cert")) < 0) {
                        row.put("title", "AED Certificate is overdue.");
                    } else {
                        row.put("title", "AED Certificate is due for renewal.");
                    }
                    row.put("no_of_days", (int) cursor.getFloat(cursor
                            .getColumnIndex("aed_cert")) + " day");
                    mapArrayList.add(row);
                }

            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        adapter = new SimpleAdapter(getActivity(), mapArrayList, R.layout.custom_list_row_assesssment,
                new String[]{"title", "no_of_days"}, new int[]{R.id.tvFormName, R.id.tvNoOfFields});

    }

    private void loadQualificationHours() {
        SQLiteDatabase sqLiteDatabase = null;
        try {
            sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("select sum(points) as total_hours from " +
                    "trainer_profile_accreditation where deleted =0 and is_point=1", null);

            if (cursor.moveToFirst()) {
                totalCecHours.setText("Training Hours : " +
                        (cursor.getString(cursor.getColumnIndex("total_hours")) != null ?
                                cursor.getString(cursor.getColumnIndex("total_hours")) : ""));
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    private void loadQualificationPoint() {
        SQLiteDatabase sqLiteDatabase = null;
        try {
            sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("select sum(points) as total_points from " +
                    "trainer_profile_accreditation where deleted =0 and is_point=0 " + " and "
                    + Table.TrainerProfileAccreditation.COMPLETED_DATE + " >= "
                    + "date(\'now\',\'-2 year\')", null);

            if (cursor.moveToFirst()) {
                totalCecPoints.setText("CEC Points : "
                        + (cursor.getString(cursor.getColumnIndex("total_points")) != null ?
                        cursor.getString(cursor.getColumnIndex("total_points")) : ""));
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }

    }
}
