package com.fitforbusiness.nafc.client;

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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.fitforbusiness.Parse.Models.Client;
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
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Sanjeet on 19-Jun-14.
 */
public class ClientFragment extends FFBFragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String clientManagementService = Utils.BASE_URL + Utils.CLIENT_MANAGEMENT_SERVICE;
    private static final String ARG_SECTION_NUMBER = "section_number";
    CustomAsyncTaskListAdapter adapter;
    private ListView clientList;
    private ArrayList<HashMap<String, Object>> mapClientArray;
    private SwipeRefreshLayout swipeLayout;
    Bitmap bmp;
    LinkedHashMap<String, Object> row;
    Client client_item;
    byte[] file;

    public ClientFragment() {
    }

    public static ClientFragment newInstance(int sectionNumber) {
        ClientFragment fragment = new ClientFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ParseObject.registerSubclass(Client.class);
        //loadClientDetail();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_clients, container, false);
        assert rootView != null;
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);

        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setEnabled(false);
        swipeLayout.setColorScheme(R.color.blue_bright,
                R.color.green_light,
                R.color.orange_light,
                R.color.red_light);
        clientList = (ListView) rootView.findViewById(R.id.lvClientList);
        clientList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                swipeLayout.setEnabled(firstVisibleItem == 0);
            }
        });

        loadParseClient();
        return rootView;
    }


    private void setListAdapter() {
        adapter = new CustomAsyncTaskListAdapter(getActivity(),
                R.layout.custom_client_list_row, R.id.ivRowImage, R.id.tvFirstName,
                R.id.tvSecondLabel, R.id.tvThirdLabel, mapClientArray);
        clientList.setAdapter(adapter);
    }

    private void loadClientDetail() {
        mapClientArray = new ArrayList<HashMap<String, Object>>();
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            String select_query = "select c._id,c.client_id, c.first_name,c.last_name, c.photo_url , " +
                    "(select count(group_id) from sessions where group_id=c._id and session_type=0 and deleted=0)" +
                    "as session_count," +
                    "(select datetime(date(start_date)||' '|| start_time) from sessions " +
                    "where group_id=c._id and datetime('now','localtime') <= datetime(date(start_date)||' '|| start_time) and session_type=0) as next_session " +
                    "from client c where c.deleted=0 order by c.first_name asc";

            Log.d("query is ", select_query);
            assert sqlDB != null;
            Cursor cursor = sqlDB
                    .rawQuery(select_query
                            , null);
            LinkedHashMap<String, Object> row;
            while (cursor.moveToNext()) {
                row = new LinkedHashMap<String, Object>();
                row.put("_id", cursor.getString(cursor
                        .getColumnIndex(Table.Client.ID)));
                row.put("client_id", cursor.getString(cursor
                        .getColumnIndex(Table.Client.CLIENT_ID)));
                row.put("name", cursor.getString(cursor
                        .getColumnIndex(Table.Client.FIRST_NAME)) + " " + cursor.getString(cursor
                        .getColumnIndex(Table.Client.LAST_NAME)));
                row.put("photo", cursor.getString(cursor
                        .getColumnIndex(Table.Client.PHOTO_URL)));
                row.put("secondLabel", cursor.getString(cursor
                        .getColumnIndex("session_count")));
                row.put("thirdLabel", cursor.getString(cursor
                        .getColumnIndex("next_session")) != null ? Utils.dateConversionForSessionRow(
                        cursor.getString(cursor.getColumnIndex("next_session"))) : "None");
                mapClientArray.add(row);
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
    }

    public void loadParseClient(){
        mapClientArray = new ArrayList<HashMap<String, Object>>();

        ParseQuery parseQuery = new ParseQuery(Client.class);
        parseQuery.fromLocalDatastore();
        //parseQuery.whereEqualTo("trainer", Trainer.getCurrent());
        parseQuery.findInBackground(new FindCallback<Client>() {
            @Override
            public void done(List<Client> list, ParseException e) {
                if (e == null && list != null) {
                    if (list.size() == 0) {
                        ParseQuery parseQuery = new ParseQuery(Client.class);
                        //parseQuery.whereEqualTo("trainer", Trainer.getCurrent());
                        parseQuery.findInBackground(new FindCallback<Client>() {
                            @Override
                            public void done(List<Client> list, ParseException e) {
                                if (e == null && list != null) {
                                    loadIntoClientListView(list);
                                   // Client.pinAllInBackground(list);
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
        if(list!=null && list.size()!=0) {
            for (int i = 0; i < list.size(); i++) {
                client_item = list.get(i);
                row = new LinkedHashMap<String, Object>();
                if (client_item.getImageFile() != null) {
                    String fileObjectStr = client_item.getString("imageFile");
                    byte[] fileObject = android.util.Base64.decode(fileObjectStr, 1);
                    bmp = BitmapFactory.decodeByteArray(fileObject, 0, fileObject.length);
                    row.put("photo", bmp);
                } else {
                    row.put("photo", null);
                }
                row.put("_id", client_item.getObjectId());
                row.put("name", client_item.getFirstName() + "  " + client_item.getLastName());
                row.put("secondLabel", "");
                row.put("thirdLabel", "");
                mapClientArray.add(row);

            }
            if (mapClientArray != null && mapClientArray.size() != 0) {
                CustomAsyncTaskListAdapter adapter = new CustomAsyncTaskListAdapter(getActivity(),
                        R.layout.exercise_list_row, R.id.ivRowImage, R.id.tvFirstName,
                        R.id.tvSecondLabel, R.id.tvThirdLabel, mapClientArray);
                clientList.setAdapter(adapter);
                swipeLayout.setRefreshing(false);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_client, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addClient:
                startActivityForResult(new Intent(getActivity(), AddClientActivity.class), Utils.CLIENTS);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            setHasOptionsMenu(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        setListAdapter();

        final SwipeDetector swipeDetector = new SwipeDetector(getActivity());
        clientList.setOnTouchListener(swipeDetector);
        clientList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Map map = mapClientArray.get(i);

                if (swipeDetector.swipeDetected()) {
                    if (swipeDetector.getAction() == SwipeDetector.Action.RL)
                        showDeleteAlert(map);
                } else {
                    if (map.get("_id") != null) {
                        startActivityForResult(new Intent(getActivity(),
                                        TempClientDetailActivity.class).putExtra("_id", map.get("_id")
                                        .toString()),
                                Utils.CLIENTS
                        );
                    }
                }
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));

    }

    private void showDeleteAlert(final Map map) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Fit For Business");
        builder.setMessage("Delete Client?")
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

    private void deleteClient(Map map) {
        ContentValues values = new ContentValues();
        values.put(Table.DELETED, 1);
        long isDeleted = DBOHelper.update(Table.Client.TABLE_NAME, values, map.get("_id").toString());
        if (isDeleted > 0 && Utils.isNetworkAvailable(getActivity())&&map.containsValue("client_id"))
            deleteOnServer(map.get("client_id").toString());
        loadClientDetail();
        setListAdapter();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            loadParseClient();
//        setListAdapter();
    }
    @Override
    public void onResume() {
        super.onResume();
//        loadParseClient();
    }
    @Override
    public void onRefresh() {
        loadParseClient();
//        swipeLayout.setEnabled(false);
       // loadParseClient();
//        if (Utils.isNetworkAvailable(getActivity())) {
//            swipeLayout.setEnabled(true);
//
//        }
    }


    private class LoadClientList extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] params) {
            loadClientDetail();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (getActivity() != null) {
                setListAdapter();
            }
        }
    }

    private void syncClients() {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                swipeLayout.setEnabled(false);
            }

            @Override
            protected Void doInBackground(Void... params) {
                new Synchronise.Client(getActivity(),
                        Utils.getLastClientSyncTime(getActivity())).sync();
                return null;
            }

            @Override
            protected void onPostExecute(Void o) {
                super.onPostExecute(o);
                loadClientDetail();
                setListAdapter();
                Utils.getLastClientSyncTime(getActivity());
                swipeLayout.setRefreshing(false);
                swipeLayout.setEnabled(true);
            }
        }.execute();
    }

    private void deleteOnServer(final String clientId) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("Client_Id", clientId);
                WebService mWebservice = new WebService();
                String mResponse = mWebservice.webGet(clientManagementService, "DeleteClient",
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
