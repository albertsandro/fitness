package com.fitforbusiness.nafc.exercise;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.fitforbusiness.Parse.Models.Group;
import com.fitforbusiness.Parse.Models.Trainer;
import com.fitforbusiness.Parse.Models.Workout;
import com.fitforbusiness.database.DBOHelper;
import com.fitforbusiness.database.DatabaseHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.framework.CustomAsyncTaskListAdapter;
import com.fitforbusiness.framework.FFBFragment;
import com.fitforbusiness.framework.SwipeDetector;
import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.nafc.MainActivity;
import com.fitforbusiness.nafc.R;
import com.fitforbusiness.webservice.Synchronise;
import com.fitforbusiness.webservice.WebService;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class WorkoutFragment extends FFBFragment implements SwipeRefreshLayout.OnRefreshListener {


    private static final String ARG_SECTION_NUMBER = "section_number";
    private ListView workoutList;
    private ArrayList<HashMap<String, Object>> mapWorkoutArray;
    private SwipeRefreshLayout swipeLayout;
    private static String programManagementWebServiceURL = Utils.BASE_URL + Utils.PROGRAM_MANAGEMENT_SERVICE;
    byte[] file;
    public WorkoutFragment() {
    }

    public static WorkoutFragment newInstance(int position) {
        WorkoutFragment fragment = new WorkoutFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, position);

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

        View rootView = inflater.inflate(R.layout.fragment_workout, container, false);
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setEnabled(false);
        swipeLayout.setColorScheme(R.color.blue_bright,
                R.color.green_light,
                R.color.orange_light,
                R.color.red_light);
        workoutList = (ListView) rootView.findViewById(R.id.lvWorkout);
        workoutList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                swipeLayout.setEnabled(firstVisibleItem == 0);
            }
        });

        loadParseWorkout();
//        loadWorkout();
        final SwipeDetector swipeDetector = new SwipeDetector(getActivity());
        workoutList.setOnTouchListener(swipeDetector);
        workoutList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Map map = mapWorkoutArray.get(i);

                if (swipeDetector.swipeDetected()) {
                    if (swipeDetector.getAction() == SwipeDetector.Action.RL) {
                        showDeleteAlert(map);
                    }
                } else {
                    if (map.get("_id") != null) {
                        startActivityForResult(new Intent(getActivity(),
                                        ViewWorkoutActivity.class).putExtra("_id", map.get("_id").toString()),
                                Utils.EXERCISES
                        );
                    }
                }
            }
        });
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            setHasOptionsMenu(true);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Auto-generated method stub
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_exercise, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.addExercise:
                startActivityForResult(new Intent(getActivity(), AddWorkoutActivity.class), 1234);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    private  void loadParseWorkout(){
        mapWorkoutArray = new ArrayList<HashMap<String, Object>>();
        ParseQuery parseQuery = new ParseQuery(Workout.class);
        parseQuery.fromLocalDatastore();
//        parseQuery.whereEqualTo("trainer", Trainer.getCurrent());
        parseQuery.findInBackground(new FindCallback<Workout>() {
            @Override
            public void done(List<Workout> list, ParseException e) {
                if (e == null && list != null) {
                    if (list.size() == 0) {
                        ParseQuery parseQuery = new ParseQuery(Workout.class);
//                        parseQuery.whereEqualTo("trainer", Trainer.getCurrent());
                        parseQuery.findInBackground(new FindCallback<Workout>() {
                            @Override
                            public void done(List<Workout> list, ParseException e) {
                                if (e == null && list != null) {
                                    loadIntoWorkListView(list);
 //                                   Workout.pinAllInBackground(list);
                                }
                            }
                        });
                    } else {
                        loadIntoWorkListView(list);
                    }
                }
            }
        });
    }

    private void loadIntoWorkListView(List<Workout> list) {
        if (list!=null && list.size()!=0) {
            LinkedHashMap<String, Object> row;
            for (Workout workout : list) {
                row = new LinkedHashMap<String, Object>();
                if (workout.getImageFile() != null) {
                    String fileObjectStr = workout.getImageFile();
                    byte[] fileObject = android.util.Base64.decode(fileObjectStr, 1);
                    Bitmap bmp = BitmapFactory.decodeByteArray(fileObject, 0, fileObject.length);
                    row.put("photo", bmp);
                } else {
                    row.put("photo", null);
                }
                row.put("_id", workout.getObjectId());
                row.put("name", workout.getName());
                row.put("workout_id", workout.getObjectId());
                row.put("thirdLabel", "");
                row.put("secondLable", workout.getWorkoutDescription());
                mapWorkoutArray.add(row);
            }
            if (mapWorkoutArray!=null && mapWorkoutArray.size()!=0) {
                CustomAsyncTaskListAdapter adapter = new CustomAsyncTaskListAdapter(getActivity(),
                        R.layout.exercise_list_row, R.id.ivRowImage,
                        R.id.tvFirstName, R.id.tvSecondLabel, R.id.tvThirdLabel, mapWorkoutArray);

                workoutList.setAdapter(adapter);
                swipeLayout.setRefreshing(false);
            }
        }
    }

    private void loadWorkout() {
        mapWorkoutArray = new ArrayList<HashMap<String, Object>>();
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();

            String query = "select  * "
                    + " , (select count(workout_id) from " +
                    "workout_exercises where workout_id=w._id) as exercises_count from " +
                    Table.Workout.TABLE_NAME + " w  where w." + Table.DELETED
                    + " = 0  order by w." + Table.Workout.NAME + " asc";
            Log.d("query is ", query);
            assert sqlDB != null;
            Cursor cursor = sqlDB
                    .rawQuery(query
                            , null);

            LinkedHashMap<String, Object> row;
            while (cursor.moveToNext()) {


                row = new LinkedHashMap<String, Object>();
                row.put("_id", cursor.getString(cursor
                        .getColumnIndex(Table.Workout.ID)));
                row.put("workout_id", cursor.getString(cursor
                        .getColumnIndex(Table.Workout.WORKOUT_ID)));
                row.put("name", cursor.getString(cursor
                        .getColumnIndex(Table.Workout.NAME)));
                row.put("photo", cursor.getString(cursor
                        .getColumnIndex(Table.Workout.PHOTO_URL)));

                row.put("secondLabel", cursor.getString(cursor
                        .getColumnIndex(Table.Workout.DESCRIPTION)));
                row.put("thirdLabel", cursor.getString(cursor
                        .getColumnIndex("exercises_count")));
                mapWorkoutArray.add(row);
            }
        } catch (Exception e) {
            assert sqlDB != null;
            sqlDB.close();
            e.printStackTrace();
        } finally {
            assert sqlDB != null;
            sqlDB.close();
        }
        CustomAsyncTaskListAdapter adapter = new CustomAsyncTaskListAdapter(getActivity(),
                R.layout.exercise_list_row, R.id.ivRowImage,
                R.id.tvFirstName, R.id.tvSecondLabel, R.id.tvThirdLabel, mapWorkoutArray);

        workoutList.setAdapter(adapter);
    }

    private void showDeleteAlert(final Map map) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Fit For Business");
        builder.setMessage("Delete Workout?")
                .setCancelable(true)
                .setPositiveButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        }
                ).setNegativeButton("Delete",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteWorkout(map);
                    }
                }
        );

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void deleteWorkout(Map map) {
        ContentValues values = new ContentValues();
        values.put(Table.DELETED, 1);
        long rowId = DBOHelper.update(Table.Workout.TABLE_NAME, values, map.get("_id").toString());
        if (rowId > 0 && Utils.isNetworkAvailable(getActivity()))
            deleteOnServer(map.get("workout_id").toString());
        loadWorkout();
    }

    private void deleteOnServer(final String workoutId) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("workoutId", workoutId);
                WebService mWebservice = new WebService();
                String mResponse = mWebservice.webGet(programManagementWebServiceURL, "DeleteWorkout",
                        map);
                JSONObject mJson;
                try {
                    if (mResponse != null) {
                        mJson = new JSONObject(mResponse);
                        Log.d("DeleteWorkoutResult", mJson.toString());
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loadParseWorkout();
    }

    @Override
    public void onRefresh() {
        swipeLayout.setEnabled(false);
        loadParseWorkout();
//        if (Utils.isNetworkAvailable(getActivity())) {
//            swipeLayout.setEnabled(true);
//            syncWorkout();
//        }
    }

    private void syncWorkout() {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                swipeLayout.setEnabled(false);
            }

            @Override
            protected Void doInBackground(Void... params) {

                new Synchronise.Workout(getActivity(),
                        Utils.getLastWorkoutSyncTime(getActivity())).sync();
                return null;
            }

            @Override
            protected void onPostExecute(Void o) {
                super.onPostExecute(o);
                loadWorkout();
                Utils.setLastWorkoutSyncTime(getActivity());
                swipeLayout.setRefreshing(false);
                swipeLayout.setEnabled(true);
            }
        }.execute();
    }
}
