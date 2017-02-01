package com.fitforbusiness.nafc.assessment;


import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.fitforbusiness.Parse.Models.AssessmentForm;
import com.fitforbusiness.Parse.Models.CompletedAssessmentForm;
import com.fitforbusiness.database.DBOHelper;
import com.fitforbusiness.database.DatabaseHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.framework.SwipeDetector;
import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.nafc.R;
import com.fitforbusiness.webservice.Synchronise;
import com.fitforbusiness.webservice.WebService;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 */
public class PreWorkoutAssessmentFragment extends Fragment implements AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String trainerWebServiceURL = Utils.BASE_URL + Utils.TRAINING_APP_SERVICE;
    ArrayList<Map<String, String>> mapArrayList = null;
    SimpleAdapter adapter;
    ListView forms;
    SwipeDetector swipeDetector;
    private int type;
    private SwipeRefreshLayout swipeLayout;

    public PreWorkoutAssessmentFragment() {
        // Required empty public constructor
    }

    public static PreWorkoutAssessmentFragment newInstance(int flag, String client_group_id) {
        PreWorkoutAssessmentFragment fragment = new PreWorkoutAssessmentFragment();
        Bundle args = new Bundle();
        args.putInt(Utils.ARG_GROUP_OR_CLIENT, flag);
        args.putString(Utils.ARG_GROUP_OR_CLIENT_ID, client_group_id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_pre_workout_assessment, container, false);
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setEnabled(false);
        swipeLayout.setColorScheme(R.color.blue_bright,
                R.color.green_light,
                R.color.orange_light,
                R.color.red_light);
        forms = (ListView) rootView.findViewById(R.id.lvPreWorkOutAssessmentDetails);
        forms.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                swipeLayout.setEnabled(firstVisibleItem == 0);
            }
        });

        type = getArguments().getInt(Utils.ARG_GROUP_OR_CLIENT);
//        loadData();
        swipeDetector = new SwipeDetector(getActivity());
        forms.setOnTouchListener(swipeDetector);
        forms.setOnItemClickListener(this);
        return rootView;
    }

    private void loadParseData() {
        mapArrayList = new ArrayList<Map<String, String>>();
        ParseQuery parseQuery = new ParseQuery(CompletedAssessmentForm.class);
//        parseQuery.whereEqualTo("",getArguments().getString(Utils.ARG_GROUP_OR_CLIENT_ID));
        parseQuery.fromLocalDatastore();
        parseQuery.findInBackground(new FindCallback<CompletedAssessmentForm>() {
            @Override
            public void done(List<CompletedAssessmentForm> list, ParseException e) {
                if (e == null && list != null) {
                    if (list.size() == 0) {
                        ParseQuery parseQuery = new ParseQuery(CompletedAssessmentForm.class);
                        parseQuery.findInBackground(new FindCallback<CompletedAssessmentForm>() {
                            @Override
                            public void done(List<CompletedAssessmentForm> list, ParseException e) {
                                if (e == null && list != null) {
                                    loadIntoAssessmentFormListView(list);
                                    //AssessmentForm.pinAllInBackground(list);
                                }
                            }
                        });
                    } else {
                        loadIntoAssessmentFormListView(list);
                    }
                }
            }
        });
    }

    private void loadIntoAssessmentFormListView(List<CompletedAssessmentForm> list) {
        LinkedHashMap<String, String> row;
        for (CompletedAssessmentForm completedAssessmentForm : list) {
            row = new LinkedHashMap<String, String>();
            row.put("_id", completedAssessmentForm.getObjectId());
            row.put("title", completedAssessmentForm.getName());
            row.put("date", ""+completedAssessmentForm.getDate());
            row.put("type",completedAssessmentForm.getFormType()+"");
//            row.put("type", assessmentForm.getType());
            mapArrayList.add(row);
        }
        adapter = new SimpleAdapter(getActivity(), mapArrayList, R.layout.custom_list_row_assesssment,
                new String[]{"title", "date"}, new int[]{R.id.tvFormName, R.id.tvNoOfFields});
        forms.setAdapter(adapter);
    }
    private void loadData() {
        mapArrayList = new ArrayList<Map<String, String>>();
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            /*String query = "select  * "
                    + " from " +
                    Table.CompletedAssessmentForm.TABLE_NAME + " order by _id desc ";*/
            String join_query = " select * from " + Table.CompletedAssessmentForm.TABLE_NAME
                    + " where " + Table.DELETED + " = 0 and "
                    + Table.CompletedAssessmentForm.GROUP_ID + " = " + getArguments().getString(Utils.ARG_GROUP_OR_CLIENT_ID)
                    + (type == 0 ? "" : " and completed_form_type=2");

            Log.d("query is ", join_query);
            Cursor cursor = sqlDB
                    .rawQuery(join_query
                            , null);

            LinkedHashMap<String, String> row;
            while (cursor.moveToNext()) {

                row = new LinkedHashMap<String, String>();
                row.put("_id", cursor.getString(cursor
                        .getColumnIndex(Table.CompletedAssessmentForm.ID)));
                row.put("title", cursor.getString(cursor
                        .getColumnIndex(Table.CompletedAssessmentForm.FORM_NAME)));
                row.put("date", Utils.dateConversionForSessionRow(cursor.getString(cursor
                        .getColumnIndex(Table.CREATED))));
                row.put("type", cursor.getInt(cursor
                        .getColumnIndex(Table.CompletedAssessmentForm.COMPLETED_FORM_TYPE)) + "");
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
        adapter = new SimpleAdapter(getActivity(), mapArrayList, R.layout.custom_list_row_assesssment,
                new String[]{"title", "date"}, new int[]{R.id.tvFormName, R.id.tvNoOfFields});
        forms.setAdapter(adapter);


    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Map map = mapArrayList.get(position);
        Log.v("form_id:", map.get("_id").toString());

//        Bundle bundle = new Bundle();
//        bundle.putBoolean("flagAdd", false);
//        bundle.putString("form_id",map.get("_id").toString());
//        bundle.putString("title",map.get("title").toString());
//        startActivityForResult(new Intent(getActivity(), AddAssessment.class).putExtra("bundle", bundle), 1234);

        if (swipeDetector.swipeDetected()) {
            if (swipeDetector.getAction() == SwipeDetector.Action.RL) {
                showDeleteAlert(map.get("_id").toString());
            }
        } else {
            if (map.get("type").toString().equalsIgnoreCase("1")) {
                startActivityForResult(new Intent(getActivity(), PARQAssessmentForm.class)
                        .putExtra("form_id", map.get("_id").toString()), 1231);
            } else {
                startActivityForResult(new Intent(getActivity(), AddPreWorkoutAssessment.class)
                        .putExtra("form_id", map.get("_id").toString()), 1231);
            }
        }
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
                        deleteAssessment(row_id);
                    }
                }
        );
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void deleteAssessment(String row_id) {
        CompletedAssessmentForm completedAssessmentForm=new CompletedAssessmentForm();
        completedAssessmentForm=CompletedAssessmentForm.createWithoutData(CompletedAssessmentForm.class, row_id);
        completedAssessmentForm.deleteInBackground();
//        ContentValues values = new ContentValues();
//        values.put(Table.DELETED, 1);
//        DBOHelper.update(Table.CompletedAssessmentForm.TABLE_NAME, values, row_id);
       loadParseData();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("onActivityResult", "onActivityResult");
    }

    private void syncForms() {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                swipeLayout.setEnabled(false);
            }

            @Override
            protected Void doInBackground(Void... params) {

                new Synchronise.Assessment(getActivity(),
                        Utils.getLastAssessmentSyncTime(getActivity())).sync();
                return null;
            }

            @Override
            protected void onPostExecute(Void o) {
                super.onPostExecute(o);
                loadData();
                Utils.setLastAssessmentSyncTime(getActivity());
                swipeLayout.setRefreshing(false);
                swipeLayout.setEnabled(true);
            }
        }.execute();
    }

    @Override
    public void onRefresh() {
//        swipeLayout.setEnabled(false);
//        if (Utils.isNetworkAvailable(getActivity())) {
//            swipeLayout.setEnabled(true);
//            syncForms();
//        }
        loadParseData();
    }
    @Override
    public void onResume(){
        super.onResume();
        loadParseData();
    }
    private void deleteOnServer(final String clientId) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("Client_Id", clientId);
                WebService mWebservice = new WebService();
                String mResponse = mWebservice.webGet(trainerWebServiceURL, "DeleteClient",
                        map);
                JSONObject mJson;
                try {
                    if (mResponse != null) {
                        mJson = new JSONObject(mResponse);
                        Log.d("DeleteClientResult", mJson.toString());
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
}
