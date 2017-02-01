package com.fitforbusiness.framework;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.fitforbusiness.Parse.Models.Exercise;
import com.fitforbusiness.Parse.Models.Trainer;
import com.fitforbusiness.database.DatabaseHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.nafc.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PickItemList extends ActionBarActivity implements AdapterView.OnItemClickListener {

    public static final String TABLE_KEY = "table_name";
    public static final String WHERE_KEY = "where_clause";
    public static final String CURRENT_USER = "current_user";
    /*    private String NO_ITEM="List is emp"*/
    private ArrayList<Map<String, Object>> mapArrayList, clientArrayList;
    private String table_name = "";
    private int selection_mode;
    private ListView listView;

    private boolean hasFirstNameOnly = false;
    private String where_clause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_item_list);

        clientArrayList = new ArrayList<Map<String, Object>>();
        try {
            hasFirstNameOnly = this.getIntent().getBooleanExtra("nameOnly", false);
            setTitle(this.getIntent().getStringExtra(Utils.TITLE));
            selection_mode = this.getIntent().getIntExtra(Utils.SELECTION_MODE, Utils.MULTI_SELECT);
           /* if (hasFirstNameOnly)
                setTitle("Select A Measurement");*/
            table_name = this.getIntent().getStringExtra(TABLE_KEY);
            where_clause = this.getIntent().getStringExtra(WHERE_KEY);
        } catch (Exception e) {
            e.printStackTrace();
        }
        listView = (ListView) findViewById(R.id.listView);
        loadListItems();

        listView.setOnItemClickListener(this);
    }

    @SuppressLint("NewApi")
    @Override
    protected void onResume()
    {
        super.onResume();

        if (Build.VERSION.SDK_INT < 16)
        {
            // Hide the status bar
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

            // Hide the action bar
            try {
                getSupportActionBar().hide();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else
        {
            // Hide the status bar
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
            // Hide the action bar
            try {
                getActionBar().hide();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void loadListItems() {
        ParseQuery query = new ParseQuery(table_name);
        if (where_clause != null && where_clause.equals(CURRENT_USER)) {
            query.whereEqualTo("trainer", Trainer.getCurrent());
        }

        query.fromLocalDatastore();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null && list != null) {
                    if (list.size() == 0 ) {
                        ParseQuery parseQuery = new ParseQuery(table_name);
                        if (where_clause != null && where_clause.equals(CURRENT_USER)) {
                            parseQuery.whereEqualTo("trainer", Trainer.getCurrent());
                        }

                        parseQuery.findInBackground(new FindCallback<ParseObject>() {

                            @Override
                            public void done(List<ParseObject> list, ParseException e) {
                                if (e == null && list != null) {
                                    loadIntoList(list);
                                    ParseObject.pinAllInBackground(list);
                                }
                            }
                        });
                    } else {
                        loadIntoList(list);
                    }
                }
            }
        });
    }

    private void loadIntoList(List<ParseObject> list) {
        mapArrayList = new ArrayList<>();
        Map<String, Object> row;
        for (ParseObject obj : list) {

            row = new HashMap<String, Object>();
            row.put("_id", obj.getObjectId());

            if (hasFirstNameOnly) {
                row.put("name", obj.get("name"));
            } else {
                row.put("name", obj.get("firstName") +
                        " " + obj.get("lastName"));
            }
            mapArrayList.add(row);
        }
        SimpleAdapter adapter = new SimpleAdapter(PickItemList.this, mapArrayList,
                android.R.layout.simple_list_item_1, new String[]{"name"}, new int[]{android.R.id.text1});
        listView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        clientArrayList.add(mapArrayList.get(i));
        mapArrayList.remove(i);
        listView.invalidateViews();
        if (selection_mode == Utils.SINGLE_SELECT)
            finish();
    }

    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra("data", clientArrayList);
        setResult(RESULT_OK, intent);
        super.finish();
    }
}

