package com.fitforbusiness.nafc.exercise;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.widget.Toast;

import com.fitforbusiness.Parse.Models.Exercise;
import com.fitforbusiness.Parse.Models.Trainer;
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
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class ExerciseFragment extends FFBFragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static String programManagementWebServiceURL = Utils.BASE_URL + Utils.PROGRAM_MANAGEMENT_SERVICE;
    private ListView exerciseList;
    private ArrayList<HashMap<String, Object>> mapExerciseArray;
    private SwipeRefreshLayout swipeLayout;
    private List<Exercise> exercises;
     byte[] file;
    public ExerciseFragment() {
    }

    public static ExerciseFragment newInstance(int position) {
        ExerciseFragment fragment = new ExerciseFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_exercise, container, false);
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setEnabled(false);
        swipeLayout.setColorScheme(R.color.blue_bright,
                R.color.green_light,
                R.color.orange_light,
                R.color.red_light);
        exerciseList = (ListView) rootView.findViewById(R.id.lvExercise);
        exerciseList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                swipeLayout.setEnabled(firstVisibleItem == 0);
            }
        });

        loadExercises();
        final SwipeDetector swipeDetector = new SwipeDetector(getActivity());
        exerciseList.setOnTouchListener(swipeDetector);
        exerciseList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final HashMap map = mapExerciseArray.get(i);
                if (swipeDetector.swipeDetected()) {
                    if (swipeDetector.getAction() == SwipeDetector.Action.RL) {
                        showDeleteAlert(map);
                    }
                } else {
                    if ((map.get("_id") != null)) {
                        startActivityForResult(new Intent(getActivity(),
                                        ViewExerciseActivity.class).putExtra("_id", map.get("_id").toString()),
                                Utils.CLIENTS
                        );
                   }
//                        else {
//                        Toast.makeText(getActivity(), "Saving Exercise", Toast.LENGTH_LONG).show();
//                        exercises.get(i).saveEventually(new SaveCallback() {
//                            @Override
//                            public void done(ParseException e) {
//                                if (e == null) {
//                                    startActivityForResult(new Intent(getActivity(),
//                                                    ViewExerciseActivity.class).putExtra("_id",
//                                                    map.get("_id").toString()), Utils.CLIENTS
//                                    );
//                                }
//                            }
//                        });
//                    }
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
                startActivityForResult(new Intent(getActivity(), AddExerciseActivity.class), 1234);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadExercises() {
        mapExerciseArray = new ArrayList<HashMap<String, Object>>();

        ParseQuery parseQuery = new ParseQuery(Exercise.class);
        parseQuery.fromLocalDatastore();
        //parseQuery.whereEqualTo("trainer", Trainer.getCurrent());
        parseQuery.include("measurements");
        parseQuery.findInBackground(new FindCallback<Exercise>() {
            @Override
            public void done(List<Exercise> list, ParseException e) {
//                Log.e("test", (e == null) + " - " + (list == null));
                if (e == null && list != null) {
                    if (list.size() == 0 ) {
                        ParseQuery parseQuery = new ParseQuery(Exercise.class);
                       // parseQuery.whereEqualTo("trainer", Trainer.getCurrent());
                        parseQuery.include("measurements");
                        parseQuery.findInBackground(new FindCallback<Exercise>() {

                            @Override
                            public void done(List<Exercise> list, ParseException e) {
                                if (e == null && list != null) {
                                    loadIntoListView(list);
//                                    Exercise.pinAllInBackground(list);
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

    private void loadIntoListView(List<Exercise> list) {
        if (list!=null && list.size()!=0) {
            exercises = list;
            LinkedHashMap<String, Object> row;
            for (Exercise exercise : list) {
                row = new LinkedHashMap<String, Object>();
                if (exercise.getImageFile() != null) {
                    String fileObjectStr = exercise.getString("imageFile");
                    byte[] fileObject = android.util.Base64.decode(fileObjectStr, 1);
                    Bitmap bmp = BitmapFactory.decodeByteArray(fileObject, 0, fileObject.length);
                    row.put("photo", bmp);
                } else {
                    row.put("photo", null);
                }
                row.put("_id", exercise.getObjectId());
                row.put("exercise_id", exercise.getObjectId());
                row.put("name", exercise.getName());
                row.put("secondLabel", exercise.getTags());
                row.put("thirdLabel", exercise.getMeasurements().size());

                mapExerciseArray.add(row);
            }
            if (mapExerciseArray!=null && mapExerciseArray.size()!=0) {
                CustomAsyncTaskListAdapter adapter = new CustomAsyncTaskListAdapter(getActivity(),
                        R.layout.exercise_list_row, R.id.ivRowImage, R.id.tvFirstName,
                        R.id.tvSecondLabel, R.id.tvThirdLabel, mapExerciseArray);

                exerciseList.setAdapter(adapter);
                swipeLayout.setRefreshing(false);
            }
        }
    }

    private void showDeleteAlert(final HashMap<String, String> map) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Fit For Business");
        builder.setMessage("Delete Exercise?")
                .setCancelable(true)
                .setPositiveButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        }
                ).setNegativeButton("Delete",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        deleteClient(map);
                    }
                }
        );

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void deleteClient(HashMap<String, String> map) {
        final Exercise exercise = Exercise.createWithoutData(Exercise.class, map.get("_id"));
        final ProgressDialog pd = new ProgressDialog(getActivity().getApplication());
        pd.setMessage("Deleting...");
        exercise.deleteInBackground(new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                pd.dismiss();
                if (e == null) {
                    exercise.unpinInBackground();
                    loadExercises();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loadExercises();
    }

    @Override
    public void onRefresh() {
        loadExercises();
        //new LoadExerciseList().execute();
//        syncExercise();
    }

    private void syncExercise() {
       /* final ProgramManagementWebService programManagementWebService
                = new ProgramManagementWebService(getActivity(),
                Utils.getLastSyncTime(getActivity()));*/
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                swipeLayout.setEnabled(false);
            }

            @Override
            protected Void doInBackground(Void... params) {
                if (Utils.isNetworkAvailable(getActivity())) {
                   /* Log.d("programManagementWebService", "addExercisesToServer()");
                    // programManagementWebService.addExercisesToServer();*/
                    Synchronise.Exercise exercise =
                            new Synchronise.Exercise(getActivity(),
                                    Utils.getLastExerciseSyncTime(getActivity()));
                    exercise.sync();

                }
                return null;
            }

            @Override
            protected void onPostExecute(Void o) {
                super.onPostExecute(o);
                if (getActivity() != null) {

                    Utils.setLastExerciseSyncTime(getActivity());
                    Log.d("ExerciseSync", "Refresh Sync complete!");
                    loadExercises();
                    swipeLayout.setRefreshing(false);
                    swipeLayout.setEnabled(true);
                }
            }
        }.execute();
    }

    private void deleteOnServer(final String exerciseId) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("exerciseId", exerciseId);
                WebService mWebservice = new WebService();
                String mResponse = mWebservice.webGet(programManagementWebServiceURL, "DeleteExercise",
                        map);
                JSONObject mJson;
                try {
                    if (mResponse != null) {
                        mJson = new JSONObject(mResponse);
                        Log.d("DeleteExerciseResult", mJson.toString());
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
