package com.fitforbusiness.nafc.assessment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.fitforbusiness.Parse.Models.AssessmentField;
import com.fitforbusiness.Parse.Models.AssessmentForm;
import com.fitforbusiness.Parse.Models.Group;
import com.fitforbusiness.database.DBOHelper;
import com.fitforbusiness.database.DatabaseHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.framework.SwipeDetector;
import com.fitforbusiness.nafc.R;
import com.fitforbusiness.nafc.TuneInitialize;
import com.mobileapptracker.MobileAppTracker;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class AssessmentFormActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {


    ArrayList<Map<String, String>> mapArrayList = null;
    SimpleAdapter adapter;
    ListView forms;
    SwipeDetector swipeDetector;
    private MobileAppTracker mobileAppTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment_form);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mobileAppTracker = TuneInitialize.initialize(this);

        forms = (ListView) findViewById(R.id.lvAssessmentForms);

//        loadData();
        loadParseData();
        swipeDetector = new SwipeDetector(this);
        forms.setOnTouchListener(swipeDetector);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.assessment_form, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {

            Bundle bundle = new Bundle();
            bundle.putBoolean("flagAdd", true);

            startActivityForResult(new Intent(this, AddAssessment.class).putExtra("bundle", bundle), 1234);
        } else if (id == android.R.id.home) {
            finish();

        }
        return super.onOptionsItemSelected(item);
    }
    private void loadParseData() {
        mapArrayList = new ArrayList<Map<String, String>>();
        ParseQuery parseQuery = new ParseQuery(AssessmentForm.class);
        parseQuery.fromLocalDatastore();
        parseQuery.findInBackground(new FindCallback<AssessmentForm>() {
            @Override
            public void done(List<AssessmentForm> list, ParseException e) {
                if (e == null && list != null) {
                    if (list.size() == 0) {
                        ParseQuery parseQuery = new ParseQuery(AssessmentForm.class);
                        parseQuery.findInBackground(new FindCallback<AssessmentForm>() {
                            @Override
                            public void done(List<AssessmentForm> list, ParseException e) {
                                if (e == null && list != null) {
                                    loadIntoAssessmentFieldListView(list);
                                    AssessmentForm.pinAllInBackground(list);
                                }
                            }
                        });
                    } else {
                        loadIntoAssessmentFieldListView(list);
                    }
                }
            }
        });
    }

    private void loadIntoAssessmentFieldListView(List<AssessmentForm> list) {
        LinkedHashMap<String, String> row;
        for (AssessmentForm assessmentForm : list) {
            row = new LinkedHashMap<String, String>();
            if (!assessmentForm.getName().equals("ParQ Assessment Form")) {
                row.put("_id", assessmentForm.getObjectId());
                row.put("title", assessmentForm.getName());
//            row.put("type", assessmentForm.getType());
                mapArrayList.add(row);
            }
        }
        adapter = new SimpleAdapter(this, mapArrayList, R.layout.custom_list_row_assesssment,
                new String[]{"title", "no_of_field"}, new int[]{R.id.tvFormName, R.id.tvNoOfFields});
        forms.setAdapter(adapter);
        forms.setOnItemClickListener(this);
    }

    private void loadData() {
        mapArrayList = new ArrayList<Map<String, String>>();
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();

            String join_query = " select f.*, count(fi._id) as no_of_field from assessment_forms " +
                    "f inner join assessment_field fi on f._id=fi.form_id where form_type=2 " +
                    "group by fi.form_id";
            Log.d("query is ", join_query);
            Cursor cursor = sqlDB
                    .rawQuery(join_query
                            , null);

            LinkedHashMap<String, String> row;
            while (cursor.moveToNext()) {

                row = new LinkedHashMap<String, String>();
                row.put("_id", cursor.getString(cursor
                        .getColumnIndex(Table.AssessmentForms.ID)));
                row.put("title", cursor.getString(cursor
                        .getColumnIndex(Table.AssessmentForms.FORM_NAME)));
                row.put("no_of_field", cursor.getString(cursor
                        .getColumnIndex("no_of_field")));

                mapArrayList.add(row);
            }
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
        adapter = new SimpleAdapter(this, mapArrayList, R.layout.custom_list_row_assesssment,
                new String[]{"title", "no_of_field"}, new int[]{R.id.tvFormName, R.id.tvNoOfFields});
        forms.setAdapter(adapter);
        forms.setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        Map<String, String> map = mapArrayList.get(i);
        Bundle bundle = new Bundle();
        bundle.putBoolean("flagAdd", false);
        bundle.putString("form_id", map.get("_id"));
        bundle.putString("title", map.get("title"));
        if (swipeDetector.swipeDetected()) {
            if (swipeDetector.getAction() == SwipeDetector.Action.RL) {
                showDeleteAlert(map.get("_id"));
                // Toast.makeText(getActivity(), "Action Right to left", Toast.LENGTH_LONG).show();
            } else {

            }
        } else {
            startActivityForResult(new Intent(this, AddAssessment.class).putExtra("bundle", bundle), 1234);
        }

    }

    private void showDeleteAlert(final String _id) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Fit For Business");
      //  builder.setIcon(R.drawable.icon);
        builder.setMessage("Delete Form?")
                .setCancelable(true)
                .setPositiveButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        }
                ).setNegativeButton("Delete",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        DBOHelper.deleteFieldsByFormId(_id);
                        DBOHelper.deleteForm(_id);
                        loadParseData();
                    }
                }
        );

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loadParseData();
        // forms.invalidateViews();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mobileAppTracker.measureSession();
    }
}
