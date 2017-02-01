package com.fitforbusiness.framework;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.fitforbusiness.Parse.Models.Client;
import com.fitforbusiness.Parse.Models.Group;
import com.fitforbusiness.Parse.Models.Trainer;
import com.fitforbusiness.database.DatabaseHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.nafc.R;
import com.fitforbusiness.nafc.session.AddSessionActivity;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class ItemPickListWithImage extends ActionBarActivity implements AdapterView.OnItemClickListener {


    ;
    private ArrayList<HashMap<String, Object>> mapArrayList;
    private ListView listView;
    private boolean isClient = false;
    private ArrayList<HashMap<String, Object>> mapClientArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_pick_list_with_image);

        try {

            isClient = this.getIntent().getBooleanExtra("isClient", false);
            if (!isClient)
                setTitle("Select Group");

        } catch (Exception e) {
            e.printStackTrace();
        }
        listView = (ListView) findViewById(R.id.lvImageListView);
        loadParseListItems();

        listView.setOnItemClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.item_pick_list_with_image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        HashMap map = mapArrayList.get(position);

        if (isClient) {
            startActivity(new Intent(this, AddSessionActivity.class)
                    .putExtra(Utils.ARG_GROUP_OR_CLIENT, Utils.FLAG_CLIENT)
                    .putExtra(Utils.ARG_GROUP_OR_CLIENT_ID, map.get("_id").toString()));
            finish();
        } else {
            startActivity(new Intent(this, AddSessionActivity.class)
                    .putExtra(Utils.ARG_GROUP_OR_CLIENT, Utils.FLAG_GROUP)
                    .putExtra(Utils.ARG_GROUP_OR_CLIENT_ID, map.get("_id").toString()));
            finish();
        }
    }

    private void loadParseClient(){
        ParseQuery parseQuery = new ParseQuery(Client.class);
        parseQuery.fromLocalDatastore();
        parseQuery.whereEqualTo("trainer", Trainer.getCurrent());
        parseQuery.findInBackground(new FindCallback<Client>() {
            @Override
            public void done(List<Client> list, ParseException e) {
                if (e == null && list != null) {
                    if (list.size() == 0) {
                        ParseQuery parseQuery = new ParseQuery(Client.class);
                        parseQuery.whereEqualTo("trainer", Trainer.getCurrent());
                        parseQuery.findInBackground(new FindCallback<Client>() {
                            @Override
                            public void done(List<Client> list, ParseException e) {
                                if (e == null && list != null) {
                                    loadIntoClientListView(list);
                                    Client.pinAllInBackground(list);
                                }
                            }
                        });
                    } else {
                        loadIntoClientListView(list);
                    }
                }
            }
        });

    }

    private void loadIntoClientListView(List<Client> list) {

        LinkedHashMap<String, Object> row;
        for (Client client_item : list) {
            row = new LinkedHashMap<String, Object>();
            row.put("_id", client_item.getObjectId());
            row.put("name", client_item.getFirstName());
            mapArrayList.add(row);
            CustomAsyncTaskListAdapter adapter = new CustomAsyncTaskListAdapter(this,
                    R.layout.custom_client_list_row, R.id.ivRowImage, R.id.tvFirstName, mapArrayList);
            listView.setAdapter(adapter);

        }
    }

    private  void loadParseGroup(){
        ParseQuery parseQuery = new ParseQuery(Group.class);
        parseQuery.fromLocalDatastore();
        parseQuery.whereEqualTo("trainer", Trainer.getCurrent());
        parseQuery.findInBackground(new FindCallback<Group>() {
            @Override
            public void done(List<Group> list, ParseException e) {
                if (e == null && list != null) {
                    if (list.size() == 0) {
                        ParseQuery parseQuery = new ParseQuery(Group.class);
                        parseQuery.whereEqualTo("trainer", Trainer.getCurrent());
                        parseQuery.findInBackground(new FindCallback<Group>() {
                            @Override
                            public void done(List<Group> list, ParseException e) {
                                if (e == null && list != null) {
                                    loadIntoGroupListView(list);
                                    Group.pinAllInBackground(list);
                                }
                            }
                        });
                    } else {
                        loadIntoGroupListView(list);
                    }
                }
            }
        });
    }

    private void loadIntoGroupListView(List<Group> list) {
        LinkedHashMap<String, Object> row;
        for (Group group : list) {
            row = new LinkedHashMap<String, Object>();
            row.put("_id", group.getObjectId());
            row.put("name", group.getName());
            mapArrayList.add(row);
            CustomAsyncTaskListAdapter adapter = new CustomAsyncTaskListAdapter(this,
                    R.layout.custom_client_list_row, R.id.ivRowImage, R.id.tvFirstName, mapArrayList);
            listView.setAdapter(adapter);
        }
    }

    private void loadParseListItems() {
        mapArrayList = new ArrayList<HashMap<String, Object>>();
        if (isClient) {
            loadParseClient();
        } else {
            loadParseGroup();
        }

    }
    private void loadListItems() {
        mapArrayList = new ArrayList<HashMap<String, Object>>();
        SQLiteDatabase sqlDB = null;
        String query = "";

        if (isClient) {
            query = "select  * "
                    + " from " +
                    Table.Client.TABLE_NAME +
                    " where " + Table.DELETED + " = 0 "
            ;
        } else {

            query = "select  * "
                    + " from " +
                    Table.Group.TABLE_NAME +
                    " where " + Table.DELETED + " = 0 "
            ;
        }
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            Cursor cursor = sqlDB != null ? sqlDB
                    .rawQuery(query
                            , null) : null;

            HashMap<String, Object> row;
            while (cursor != null && cursor.moveToNext()) {
                row = new HashMap<String, Object>();
                row.put("_id", cursor.getString(cursor
                        .getColumnIndex(Table.Client.ID)));


                row.put("name", cursor.getString(cursor
                        .getColumnIndex(isClient ? Table.Client.FIRST_NAME : Table.Group.NAME)) + " " + (isClient ? cursor.getString(cursor
                        .getColumnIndex(Table.Client.LAST_NAME)) : ""));
                row.put("photo",
                        cursor.getString(cursor
                                .getColumnIndex(isClient ? Table.Client.PHOTO_URL : Table.Group.PHOTO_URL)));
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

            CustomAsyncTaskListAdapter adapter = new CustomAsyncTaskListAdapter(this,
                    R.layout.custom_client_list_row, R.id.ivRowImage, R.id.tvFirstName, mapArrayList);
            listView.setAdapter(adapter);
        }
    }
}
