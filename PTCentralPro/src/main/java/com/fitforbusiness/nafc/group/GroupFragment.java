package com.fitforbusiness.nafc.group;

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
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.fitforbusiness.Parse.Models.Client;
import com.fitforbusiness.Parse.Models.Group;
import com.fitforbusiness.Parse.Models.Measurements;
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
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class GroupFragment extends FFBFragment implements AbsListView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    private static final String clientGroupManagementService = Utils.BASE_URL + Utils.CLIENT_GROUP_MANAGEMENT_SERVICE;
    private static final String ARG_SECTION_NUMBER = "section_number";
    SwipeDetector swipeDetector;
    private AbsListView mListView;
    private ArrayList<HashMap<String, Object>> mapArrayList;
    private CustomAsyncTaskListAdapter adapter;
    private SwipeRefreshLayout swipeLayout;
    private ListView groupList;
    byte[] file;
    private ArrayList<HashMap<String, Object>> mapGroupArray;
    public GroupFragment() {
    }

    public static GroupFragment newInstance(int section) {
        GroupFragment fragment = new GroupFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, section);
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
        View view = inflater.inflate(R.layout.fragment_item, container, false);
        assert view != null;
        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setEnabled(false);
        swipeLayout.setColorScheme(R.color.blue_bright,
                R.color.green_light,
                R.color.orange_light,
                R.color.red_light);
        groupList = (ListView) view.findViewById(android.R.id.list);
        groupList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                swipeLayout.setEnabled(firstVisibleItem == 0);
            }
        });
       loadParseGroup();
//        loadGroupDetail();
//        setListAdapter();
        swipeDetector = new SwipeDetector(getActivity());
        groupList.setOnTouchListener(swipeDetector);
        groupList.setOnItemClickListener(this);
        return view;
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
                startActivityForResult(new Intent(getActivity(), AddGroupActivity.class), Utils.GROUPS);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Map map = mapGroupArray.get(position);
        if (swipeDetector.swipeDetected()) {
            if (swipeDetector.getAction() == SwipeDetector.Action.RL) {
                //showDeleteAlert(map);
            }
        } else {
            if (map.get("_id") != null) {
                startActivityForResult(new Intent(getActivity(), TempGroupDetailActivity.class)
                        .putExtra("_id", map.get("_id").toString()), Utils.GROUPS);
            }
        }
    }

    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyText instanceof TextView) {
            assert ((TextView) emptyView) != null;
            ((TextView) emptyView).setText(emptyText);
        }
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

    private  void loadParseGroup(){
        mapGroupArray = new ArrayList<HashMap<String, Object>>();

        ParseQuery parseQuery = new ParseQuery(Group.class);
        parseQuery.fromLocalDatastore();
        //parseQuery.whereEqualTo("trainer", Trainer.getCurrent());
        parseQuery.findInBackground(new FindCallback<Group>() {
            @Override
            public void done(List<Group> list, ParseException e) {
                if (e == null && list != null) {
                    if (list.size() == 0) {
                        ParseQuery parseQuery = new ParseQuery(Group.class);
                        //parseQuery.whereEqualTo("trainer", Trainer.getCurrent());
                        parseQuery.findInBackground(new FindCallback<Group>() {
                            @Override
                            public void done(List<Group> list, ParseException e) {
                                if (e == null && list != null) {
                                    loadIntoGroupListView(list);
                                    //Group.pinAllInBackground(list);
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
        if (list!=null && list.size()!=0) {
            LinkedHashMap<String, Object> row;
            for (Group group : list) {
                row = new LinkedHashMap<String, Object>();
                if (group.getImageFile() != null) {
                    String fileObjectStr = group.getImageFile();
                    byte[] fileObject = android.util.Base64.decode(fileObjectStr, 1);
                    Bitmap bmp = BitmapFactory.decodeByteArray(fileObject, 0, fileObject.length);
                    row.put("photo", bmp);
                } else {
                    row.put("photo", null);
                }
                row.put("_id", group.getObjectId());
                row.put("name", group.getName());
                row.put("secondLabel", "");
                row.put("thirdLabel", "");
                mapGroupArray.add(row);
            }
            if (mapGroupArray!=null && mapGroupArray.size()!=0) {
                adapter = new CustomAsyncTaskListAdapter(getActivity(),
                        R.layout.custom_client_list_row, R.id.ivRowImage, R.id.tvFirstName,
                        R.id.tvSecondLabel, R.id.tvThirdLabel, mapGroupArray);

                groupList.setAdapter(adapter);
                swipeLayout.setRefreshing(false);
            }
        }
    }

    private void loadGroupDetail() {
        mapArrayList = new ArrayList<HashMap<String, Object>>();
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();

            String select_query = "select g.group_id,g._id, g.name, g.photo_url , " +
                    "(select count(group_id) from sessions where group_id=g._id and session_type=1 and deleted=0) " +
                    "as session_count," +
                    "(select datetime(date(start_date)||' '|| start_time) from sessions " +
                    "where group_id=g._id and datetime('now','localtime') <= datetime(date(start_date)||' '|| start_time) and session_type=1) as next_session " +
                    "from groups g where g.deleted=0 order by g.name asc";

            String query = "select  * "
                    + " from " +
                    Table.Group.TABLE_NAME +
                    " where " + Table.DELETED + " = 0  order by " + Table.Group.NAME + " asc";
            Log.d("query is ", select_query);
            assert sqlDB != null;
            Cursor cursor = sqlDB
                    .rawQuery(select_query
                            , null);
            LinkedHashMap<String, Object> row;
            while (cursor.moveToNext()) {
                row = new LinkedHashMap<String, Object>();
                row.put("_id", cursor.getString(cursor
                        .getColumnIndex(Table.Group.ID)));
                row.put("group_id", cursor.getString(cursor
                        .getColumnIndex(Table.Group.GROUP_ID)));
                row.put("name", cursor.getString(cursor
                        .getColumnIndex(Table.Group.NAME)));
                row.put("photo", cursor.getString(cursor
                        .getColumnIndex(Table.Client.PHOTO_URL)));
                row.put("secondLabel", cursor.getString(cursor
                        .getColumnIndex("session_count")));

                row.put("thirdLabel", cursor.getString(cursor
                        .getColumnIndex("next_session")) != null ? Utils.dateConversionForSessionRow(
                        cursor.getString(cursor.getColumnIndex("next_session"))) : "None");
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
    }

    private void setListAdapter() {
        adapter = new CustomAsyncTaskListAdapter(getActivity(),
                R.layout.custom_client_list_row, R.id.ivRowImage, R.id.tvFirstName,
                R.id.tvSecondLabel, R.id.tvThirdLabel, mapArrayList);
        ((AdapterView<ListAdapter>) groupList).setAdapter(adapter);
    }

    private void showDeleteAlert(final Map map) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Fit For Business");
        builder.setMessage("Delete Group?")
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
        long isUpdated = DBOHelper.update(Table.Group.TABLE_NAME, values, map.get("_id").toString());
        if (isUpdated > 0 && Utils.isNetworkAvailable(getActivity())) {
            deleteOnServer(map.get("group_id").toString());
        }
        loadGroupDetail();
        setListAdapter();
    }
//   @Override
//   public void onResume(){
//       super.onResume();
//       loadParseGroup();
//   }
    @Override
    public void onRefresh() {
//        swipeLayout.setEnabled(false);
//        if (Utils.isNetworkAvailable(getActivity())) {
//            swipeLayout.setEnabled(true);
//            syncGroups();
//        }
        loadParseGroup();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loadParseGroup();
    }

    private void syncGroups() {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                swipeLayout.setEnabled(false);
            }

            @Override
            protected Void doInBackground(Void... params) {

                new Synchronise.Group(getActivity(),
                        Utils.getLastGroupSyncTime(getActivity())).sync();
                return null;
            }

            @Override
            protected void onPostExecute(Void o) {
                super.onPostExecute(o);
                loadGroupDetail();
                setListAdapter();
                Utils.setLastGroupSyncTime(getActivity());
                swipeLayout.setRefreshing(false);
                swipeLayout.setEnabled(true);
            }
        }.execute();
    }

    private void deleteOnServer(final String groupId) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("Group_Id", groupId);
                Log.d("DeleteGroup map", map.toString());
                WebService mWebservice = new WebService();
                String mResponse = mWebservice.webGet(clientGroupManagementService, "DeleteGroup",
                        map);
                JSONObject mJson;

                try {
                    if (mResponse != null) {
                        mJson = new JSONObject(mResponse);
                        Log.d("DeleteGroupResult", mJson.toString());
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
