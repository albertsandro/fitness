package com.fitforbusiness.nafc.accreditation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.fitforbusiness.Parse.Models.Accreditation;
import com.fitforbusiness.Parse.Models.Group;
import com.fitforbusiness.Parse.Models.Session;
import com.fitforbusiness.Parse.Models.Trainer;
import com.fitforbusiness.database.DBOHelper;
import com.fitforbusiness.database.DatabaseHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.framework.CustomAsyncTaskListAdapter;
import com.fitforbusiness.framework.FFBFragment;
import com.fitforbusiness.framework.SwipeDetector;
import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.framework.view.MySwitch;
import com.fitforbusiness.nafc.MainActivity;
import com.fitforbusiness.nafc.R;
import com.fitforbusiness.webservice.Synchronise;
import com.fitforbusiness.webservice.WebService;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Sanjeet on 5/28/14.
 */
public class AccreditationFragment extends FFBFragment implements SwipeRefreshLayout.OnRefreshListener {

    List<Accreditation> filterList=new ArrayList<Accreditation>();
    private static final String trainerWebServiceURL = Utils.BASE_URL + Utils.TRAINING_APP_SERVICE;
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String SORT_CURRENT_ACCREDITATION = " and "
            + Table.TrainerProfileAccreditation.COMPLETED_DATE + " >= "
            + "date(\'now\',\'-2 year\')";
    private static final String SORT_HISTORICAL_ACCREDITATION = " and "
            + Table.TrainerProfileAccreditation.COMPLETED_DATE + " <= "
            + "date(\'now\',\'-2 year\') ";
    final static private String SORT_ORDER_POINT = " and is_point = 0 order by points desc ";
    final static private String SORT_ORDER_HOURS = "and is_point = 1 order by points desc ";

    private TextView header;
    private ActionBar actionBar;
    private ListView listView;
    private List<Map<String, Object>> accreditationList;
    private SimpleAdapter adapter;
    private boolean is_point = true;
    private boolean is_current = true;
    private SwipeRefreshLayout swipeLayout;

    public AccreditationFragment() {
    }

    public static AccreditationFragment newInstance(int sectionNumber) {
        AccreditationFragment fragment = new AccreditationFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //   loadAccreditation(SORT_CURRENT_ACCREDITATION + SORT_ORDER_POINT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_accreditation, container, false);
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setEnabled(false);
        swipeLayout.setColorScheme(R.color.blue_bright,
                R.color.green_light,
                R.color.orange_light,
                R.color.red_light);
        listView = (ListView) rootView.findViewById(R.id.lvAccreditation);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                swipeLayout.setEnabled(firstVisibleItem == 0);
            }
        });
        header = (TextView) rootView.findViewById(R.id.tvHeader);
        is_current = true;
        is_point = false;

        final SwipeDetector swipeDetector = new SwipeDetector(getActivity());
        listView.setOnTouchListener(swipeDetector);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int listIndex, long l) {
                Map<String, Object> map = accreditationList.get(listIndex);
                Bundle bundle = new Bundle();
                bundle.putString("_id", map.get("_id").toString());
                if (swipeDetector.swipeDetected()) {
                    if (swipeDetector.getAction() == SwipeDetector.Action.RL) {
                        showDeleteAlert(map);
                    }
                } else {
                    bundle.putBoolean("editable", true);
                    startActivityForResult(new Intent(getActivity(),
                            ViewAccreditationActivity.class).putExtra("bundle", bundle), 0);
                }


            }
        });
        //   setAdapter();

        loadParseAccreditation(is_current);
//
//        header.setText("Qualifications (Points)");
//        onRefresh();
        return rootView;
    }

    private void deleteAccreditation(Map map) {

        ContentValues values = new ContentValues();
        values.put(Table.DELETED, 1);
        long rowId = DBOHelper.updateAccreditation(values, map.get("_id").toString());
        if (rowId > 0 && Utils.isNetworkAvailable(getActivity())) {
            deleteOnServer(map.get("accreditation_id").toString());
        }
        refreshQualificationList();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    private void loadParseAccreditation(final Boolean current) {
        accreditationList = new ArrayList<Map<String, Object>>();
        filterList = new ArrayList<Accreditation>();
        ParseQuery parseQuery = new ParseQuery(Accreditation.class);
        parseQuery.fromLocalDatastore();
        parseQuery.whereEqualTo("trainer", Trainer.getCurrent());
        parseQuery.findInBackground(new FindCallback<Accreditation>() {
            @Override
            public void done(List<Accreditation> list, ParseException e) {
                if (e == null && list != null) {
                    if (list.size() == 0) {
                        ParseQuery parseQuery = new ParseQuery(Accreditation.class);
                        parseQuery.whereEqualTo("trainer", Trainer.getCurrent());
                        parseQuery.findInBackground(new FindCallback<Accreditation>() {
                            @Override
                            public void done(List<Accreditation> list, ParseException e) {
                                if (e == null && list != null) {
                                    Calendar cal = Calendar.getInstance();
                                    cal.add(Calendar.YEAR, -2);
                                    Date limitDate = cal.getTime();
                                    for (Accreditation accreditation:list) {
                                        if (current) {
                                            Date date = convertStringToDate(accreditation.getCompletedDate());
                                            if (date.after(limitDate)) {
                                                try {
                                                    filterList.add(accreditation);
                                                } catch (Exception d) {
                                                    d.printStackTrace();
                                                } finally {
                                                }
                                            }
                                        } else {
                                            Date date = convertStringToDate(accreditation.getCompletedDate());
                                            if (date.before(limitDate)) {
                                                try {
                                                    filterList.add(accreditation);
                                                } catch (Exception d) {
                                                    d.printStackTrace();
                                                } finally {
                                                }
                                            }
                                        }

                                    }
                                    loadIntoAccreditationListView(filterList);
                                }
                            }

                        });
                    } else {
                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.YEAR, -2);
                        Date limitDate = cal.getTime();
                        for (Accreditation accreditation : list) {
                            if (current) {
                                Date date = convertStringToDate(accreditation.getCompletedDate());
                                if (date.after(limitDate)) {
                                    try {
                                        filterList.add(accreditation);
                                    } catch (Exception d) {
                                        d.printStackTrace();
                                    } finally {
                                    }
                                }
                            } else {
                                Date date = convertStringToDate(accreditation.getCompletedDate());
                                if (date.before(limitDate)) {
                                    try {
                                        filterList.add(accreditation);
                                    } catch (Exception d) {
                                        d.printStackTrace();
                                    } finally {
                                    }
                                }
                            }

                        }
                        loadIntoAccreditationListView(filterList);
                    }
                }
            }
        });
    }

    private void loadIntoAccreditationListView(List<Accreditation> list) {
        if (list!=null && list.size()!=0) {
            HashMap<String, Object> row;
            for (Accreditation accreditation: list) {
                row = new HashMap<String, Object>();
                row.put("_id", accreditation.getObjectId());
                row.put("accreditation_id", accreditation.getObjectId());
                row.put("course", accreditation.getCecCourseName());
                row.put("date", accreditation.getCompletedDate());
                row.put("inst",accreditation.getRegisteredTrainingOrganisation());
                if (accreditation.getIsHours()==true) {
                    String point =accreditation.getPoints();
                    row.put("point", Html.fromHtml("<p style=\"color:red\">" + point + "</p>"));
                }else row.put("point", null);
                accreditationList.add(row);
            }
        }
        setAdapter();
    }

    private void loadAccreditation(String sortOrder) {
        accreditationList = new ArrayList<Map<String, Object>>();
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();

            String query = "select  * "
                    + " from " +
                    Table.TrainerProfileAccreditation.TABLE_NAME +
                    " where " + Table.DELETED + " = 0 " + sortOrder;
            Log.d("query is ", query);
            Cursor cursor = sqlDB
                    .rawQuery(query
                            , null);

            LinkedHashMap<String, Object> row;
            while (cursor.moveToNext()) {
               /* profileName.setText(cursor.getString(cursor
                        .getColumnIndex(Table.TrainerProfileDetails.FIRST_NAME)) + "" + cursor.getString(cursor
                        .getColumnIndex(Table.TrainerProfileDetails.LAST_NAME)));*/

                row = new LinkedHashMap<String, Object>();
                row.put("_id", cursor.getString(cursor
                        .getColumnIndex(Table.TrainerProfileAccreditation.ID)));
                String accreditationId = cursor.getString(cursor
                        .getColumnIndex(Table.TrainerProfileAccreditation.ACCREDITATION_ID));
                row.put("accreditation_id", accreditationId != null ? accreditationId : "");
                row.put("inst", cursor.getString(cursor
                        .getColumnIndex(Table.TrainerProfileAccreditation.REGISTERED_TRAINING_ORGANIZATION)));
                String point = cursor.getString(cursor
                        .getColumnIndex(Table.TrainerProfileAccreditation.POINTS_HOURS)) + (cursor.getInt(cursor
                        .getColumnIndex(Table.TrainerProfileAccreditation.IS_POINT)) == 0 ? " pts" : " hrs");

                String color = (cursor.getInt(cursor
                        .getColumnIndex(Table.TrainerProfileAccreditation.IS_POINT)) == 0
                        ? "color:#4c4c4c" : " color:#ff00ff");
                row.put("point", Html.fromHtml("<p style=\"color:red\">" + point + "</p>"));

                //  row.put();Html.fromHtml("<b><u>"+string+"<b><u>")
                //Html.fromHtml("")
                row.put("course", cursor.getString(cursor
                        .getColumnIndex(Table.TrainerProfileAccreditation.COURSE_NAME)));
                row.put("date", Utils.formatConversionSQLite(cursor.getString(cursor
                        .getColumnIndex(Table.TrainerProfileAccreditation.COMPLETED_DATE))));
                accreditationList.add(row);
            }
            cursor.close();

        } catch (Exception e) {
            assert sqlDB != null;
            e.printStackTrace();
        } finally {
        }

    }

    private void showDeleteAlert(final Map map) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Fit For Business");

        builder.setMessage("Delete Qualification?")
                .setCancelable(true)
                .setPositiveButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        }
                ).setNegativeButton("Delete",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        deleteAccreditation(map);
                    }
                }
        );

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            setHasOptionsMenu(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            ((MySwitch) actionBar.getCustomView().findViewById(R.id.tbSwitch)).setChecked(true);
            ((MySwitch) actionBar.getCustomView().findViewById(R.id.tbSwitch)).performClick();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Auto-generated method stub
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_acc, menu);

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        try {
            actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        } catch (Exception e) {
            e.printStackTrace();
        }
        actionBar.setCustomView(R.layout.cust_actionbar);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        ((TextView) actionBar.getCustomView().findViewById(R.id.tvTitle)).setText("Qualifications");
        ((MySwitch) actionBar.getCustomView().findViewById(R.id.tbSwitch))
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (buttonView.getId() == R.id.tbSwitch) {
                            refreshQualificationList();
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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection

        switch (item.getItemId()) {
            case R.id.addAcc:
                startActivityForResult(new Intent(getActivity(), AddAccreditationActivity.class), Utils.ACCREDITATION);
                break;
            case R.id.sortAcc:
                if (is_point) {
                    is_point = false;

                    if (!is_current) {
                       loadParseAccreditation(!is_current);
                       // setAdapter();
                        header.setText("Qualifications (Points)");
                    } else {
                        loadParseAccreditation(is_current);
                      //  loadAccreditation(SORT_HISTORICAL_ACCREDITATION + SORT_ORDER_POINT);
                        //setAdapter();
                        header.setText("Historical Qualifications (Points)");
                    }
                } else {
                    is_point = true;

                    if (!is_current) {
                        loadParseAccreditation(!is_current);
//                        loadAccreditation(SORT_CURRENT_ACCREDITATION + SORT_ORDER_HOURS);
                       // setAdapter();
                        header.setText("Qualifications (Hours)");
                    } else {
                        loadParseAccreditation(is_current);
//                        loadAccreditation(SORT_HISTORICAL_ACCREDITATION + SORT_ORDER_HOURS);
                       // setAdapter();
                        header.setText("Historical Qualifications (Hours)");
                    }
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loadParseAccreditation(!is_current);

    }

    @Override
    public void onRefresh() {
//        if (Utils.isNetworkAvailable(getActivity())) {
//            syncQualifications();
//        }
//        loadParseAccreditation(is_current);
    }

    private void setAdapter() {
        if (getActivity() != null) {
            adapter = new SimpleAdapter(getActivity(), accreditationList, R.layout.custom_list_row,
                    new String[]{"inst", "point", "course", "date"}, new int[]{R.id.tvInstitute,
                    R.id.tvPoints, R.id.tvCEC, R.id.tvDate}
            ) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    //return super.getView(position, convertView, parent);
                    ViewHolder holder = null;
                    Log.d("ConvertView", String.valueOf(position));
                    if (convertView == null) {
                        LayoutInflater layoutInflater = (LayoutInflater) getActivity()
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        convertView = layoutInflater.inflate(R.layout.custom_list_row, null);
                        holder = new ViewHolder();
                        assert convertView != null;
                        holder.textView1 = (TextView) convertView
                                .findViewById(R.id.tvInstitute);
                        holder.textView2 = (TextView) convertView
                                .findViewById(R.id.tvPoints);
                        holder.textView3 = (TextView) convertView
                                .findViewById(R.id.tvCEC);
                        holder.textView4 = (TextView) convertView
                                .findViewById(R.id.tvDate);
                        convertView.setTag(holder);

                    } else {
                        holder = (ViewHolder) convertView.getTag();
                    }

                    Map<String, Object> map = accreditationList.get(position);

                    holder.textView1.setText(map.get("inst").toString());
                    if (map.get("point")!=null) {
                        if (map.get("point").toString().contains("pts")) {
                            holder.textView2.setTextColor(getResources().getColor(R.color.orange));
                        } else {
                            holder.textView2.setTextColor(getResources().getColor(R.color.actionbar_color));
                        }

                        holder.textView2.setText((Spanned) map.get("point"));
                    }
                    holder.textView3.setText(map.get("course").toString());
                    holder.textView4.setText(map.get("date").toString());

                    return convertView;
                }

                class ViewHolder {
                    TextView textView1;
                    TextView textView2;
                    TextView textView3;
                    TextView textView4;
                }

            };

            if (listView != null) {
                listView.setAdapter(adapter);
            }
        }
    }

    private void syncQualifications() {
        new android.os.AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                swipeLayout.setEnabled(false);
            }

            @Override
            protected Void doInBackground(Void... params) {
                Synchronise.Qualification qualification
                        = new Synchronise.Qualification(getActivity(),
                        Utils.getLastSyncTime(getActivity()));
                Log.d("Synchronise.Qualification qualification", "");
                qualification.sync();
                return null;
            }

            @Override
            protected void onPostExecute(Void o) {
                super.onPostExecute(o);
                Utils.setLastSyncTime(getActivity());
//                loadAccreditation(SORT_CURRENT_ACCREDITATION + SORT_ORDER_POINT);
//                setAdapter();
                swipeLayout.setRefreshing(false);
                swipeLayout.setEnabled(true);
            }
        }.execute();
    }

    private void deleteOnServer(final String accreditationId) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("Trainer_Id", Utils.getTrainerId(getActivity()));
                map.put("Id", accreditationId);
                WebService mWebservice = new WebService();
                String mResponse = mWebservice.webGet(trainerWebServiceURL, "DeleteAccreditation",
                        map);
                JSONObject mJson;
                try {
                    if (mResponse != null) {
                        Log.d("DeleteAccreditation", mResponse.toString());
                        mJson = new JSONObject(mResponse);
                        Log.d("DeleteAccreditation", mJson.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
            }
        }.execute();
    }

    void refreshQualificationList() {
        boolean isChecked = ((MySwitch) actionBar.getCustomView().findViewById(R.id.tbSwitch)).isChecked();
        if (isChecked) {
            is_current = isChecked;
            //loadAccreditation(SORT_HISTORICAL_ACCREDITATION + SORT_ORDER_POINT);
            header.setText("Historical Qualifications (Points)");
        } else {
            is_current = isChecked;
           // loadAccreditation(SORT_CURRENT_ACCREDITATION + SORT_ORDER_POINT);
            header.setText("Qualifications (Points)");
        }
        try {
            setAdapter();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


