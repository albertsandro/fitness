package com.fitforbusiness.nafc.group;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.fitforbusiness.Parse.Models.Client;
import com.fitforbusiness.Parse.Models.Exercise;
import com.fitforbusiness.Parse.Models.Group;
import com.fitforbusiness.Parse.Models.Trainer;
import com.fitforbusiness.database.DBOHelper;
import com.fitforbusiness.database.DatabaseHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.framework.PickItemList;
import com.fitforbusiness.framework.SwipeDetector;
import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.nafc.R;
import com.fitforbusiness.nafc.client.ClientDrawerFragment;
import com.fitforbusiness.webservice.Synchronise;
import com.parse.FindCallback;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


public class  GroupDrawerFragment extends Fragment implements View.OnClickListener {
    String table_name="Client";
    private static final String QUERY_KEY = "selectQuery";
    private String imageName;
    private ArrayList<Map<String, Object>> mapArrayList;
    private ImageView groupImage;
    private EditText groupName;
    private Button addClient;
    private ListView clientList;
    private Button chooseFile;
    private SwipeDetector swipeDetector;
    private String groupId;
    private Group group;
    byte[] file;
    Bitmap selectBitmap;
    public GroupDrawerFragment() {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.drawer_group_detail, container, false);
        assert rootView != null;
        groupImage = (ImageView) rootView.findViewById(R.id.ivGroup);
        groupName = (EditText) rootView.findViewById(R.id.etGroupName);
        addClient = (Button) rootView.findViewById(R.id.bAddClients);
        clientList = (ListView) rootView.findViewById(R.id.lvClientList);
        chooseFile = (Button) rootView.findViewById(R.id.bChooseFile);
        addClient.setOnClickListener(this);
        groupName.setOnClickListener(this);
        chooseFile.setOnClickListener(this);
        try {
            loadParseGroupDetails(getArguments().getString(Utils.ARG_GROUP_OR_CLIENT_ID));
           // loadListItems(getArguments().getString(Utils.ARG_GROUP_OR_CLIENT_ID));
        } catch (Exception e) {
            e.printStackTrace();
        }
        setFieldsEditable(false);

        swipeDetector = new SwipeDetector(getActivity());
        clientList.setOnTouchListener(swipeDetector);
        clientList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (swipeDetector.swipeDetected()) {
                    if (swipeDetector.getAction() == SwipeDetector.Action.RL) {

                        if (((ToggleButton) getActionBar().getCustomView().findViewById(R.id.tbHistorical)).isChecked()) {
                            mapArrayList.remove(position);
                            reloadData();
                        }
                    }
                }
            }
        });
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.cust_actionbar_client);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        ((TextView) actionBar.getCustomView().findViewById(R.id.tvTitle)).setText("My Group");
        actionBar.getCustomView().findViewById(R.id.tbHistorical).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.tbHistorical) {
                    ToggleButton toggleButton = (ToggleButton) view;
                    if (toggleButton.isChecked()) {
                        setFieldsEditable(true);
                        Log.d("Toggle button clicked", " setFieldsEditable(true);");
                    } else {
                        if (Utils.validateFields(groupName)) {
                            /*updateAccreditation(_id + "");

                        setResult(Utils.ACCREDITATION, new Intent());
                        finish();
                    }*/
//                            long isUpdated = updateGroup(getArguments().getString(Utils.ARG_GROUP_OR_CLIENT_ID));
//                            if (isUpdated > 0) {
//                                HashMap<String, Object> group;
//                                group = DBOHelper.getGroup(getArguments().getString(Utils.ARG_GROUP_OR_CLIENT_ID));
//                                if (Utils.isNetworkAvailable(getActivity())) {
//                                    updateQualificationOnServer(groupId, group);
//                                }
//                            }
                            updateParseGroupDetail();
                            setFieldsEditable(false);
                        }
                    }
                }
            }
        });
        actionBar.getCustomView().findViewById(R.id.tvTitle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
                ;
            }
        });
    }

    private void setFieldsEditable(boolean enable) {
        chooseFile.setVisibility(enable ? View.VISIBLE : View.GONE);
        chooseFile.setEnabled(enable);
        groupImage.setEnabled(enable);
        groupName.setEnabled(enable);

        chooseFile.setFocusable(enable);
        groupImage.setFocusable(enable);
        groupName.setFocusable(enable);


        chooseFile.setFocusableInTouchMode(enable);
        groupImage.setFocusableInTouchMode(enable);
        groupName.setFocusableInTouchMode(enable);

        chooseFile.setClickable(enable);
        groupImage.setClickable(enable);
        groupName.setClickable(enable);

        if (enable) {
            clientList.setOnTouchListener(swipeDetector);
            addClient.setVisibility(View.VISIBLE);
        } else {

            addClient.setVisibility(View.GONE);
        }
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bChooseFile:
            case R.id.ivGroup:
                showImageUploadOption();
                break;
            case R.id.bAddClients:
                String QUERY_STRING = "SELECT * FROM " + Table.Client.TABLE_NAME + " WHERE deleted=0";
                startActivityForResult(new Intent(getActivity(), PickItemList.class).putExtra(PickItemList.TABLE_KEY, table_name)
                        .putExtra(Utils.TITLE, "Select A Client"), 123);
                break;
        }
    }
    private void loadParseGroupDetails(String _id){
        ParseQuery parseQuery = new ParseQuery(Group.class);
        parseQuery.fromLocalDatastore();
        parseQuery.whereEqualTo("objectId", _id);
        parseQuery.findInBackground(new FindCallback<Group>() {
            @Override
            public void done(List<Group> list, com.parse.ParseException e) {
                if (e == null && list != null) {
                    if (list.size() == 0) {

                    } else {
                        loadIntoListView(list);
                    }
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
    private void loadIntoListView(List<Group> list) {
        for (Group group : list) {
            GroupDrawerFragment.this.group=group;
            groupName.setText(group.getName());
            imageName=group.getImageName();
            if (imageName==null){
                imageName = new Date().getTime() + ".JPG";
            }
            if (group.get("imageFile")!=null) {
                String fileObjectStr=group.getImageFile();
                byte[] fileObject = android.util.Base64.decode(fileObjectStr,1);
                Bitmap bmp = BitmapFactory.decodeByteArray(fileObject,0,fileObject.length);
                groupImage.setImageBitmap(bmp);
            }
            if (group.getClients()!=null && group.getClients().size()!=0) {
                loadListItems();
            }
        }
    }
    private void loadGroupDetail(String _id) {
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            String query = "select  * "
                    + " from " +
                    Table.Group.TABLE_NAME +
                    " where " + Table.DELETED + " = 0 and " + Table.ID + " =  " + _id;
            Log.d("query is ", query);
            Cursor cursor = sqlDB != null ? sqlDB
                    .rawQuery(query
                            , null) : null;
            if (cursor != null && cursor.moveToNext()) {
                groupId = cursor.getString(cursor
                        .getColumnIndex(Table.Group.GROUP_ID));
                groupName.setText(cursor.getString(cursor
                        .getColumnIndex(Table.Group.NAME)));
                imageName = cursor.getString(cursor
                        .getColumnIndex(Table.Group.PHOTO_URL));
               /* HashMap<String,Object> test= new HashMap<String, Object>();
                test.put("image",BitmapFactory.decodeFile(Utils.PROFILE_THUMBNAIL_PATH + imageName));
                Gson gson=new Gson();
                Log.d("JSON",gson.toJson(test));*/
                try {
                    groupImage.setImageBitmap(BitmapFactory.decodeFile(Utils.PROFILE_THUMBNAIL_PATH + imageName));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            assert cursor != null;
            cursor.close();
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
    }

    private void loadListItems(String group_id) {
        mapArrayList = new ArrayList<Map<String, Object>>();
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            String query = "select g.* ,c._id, c.first_name," +
                    " c.last_name from group_clients g," +
                    " client c where  g.group_id= " + group_id +
                    " and g.client_id=c._id;";
            Log.d("Query is", query);
            Cursor cursor = sqlDB != null ? sqlDB
                    .rawQuery(query
                            , null) : null;

            Map<String, Object> row;
            while (cursor != null && cursor.moveToNext()) {
                row = new HashMap<String, Object>();
                row.put("_id", cursor.getString(cursor
                        .getColumnIndex(Table.GroupClients.ID)));
                row.put("group_id", cursor.getString(cursor
                        .getColumnIndex(Table.GroupClients.CLIENT_ID)));
                row.put("name", cursor.getString(cursor
                        .getColumnIndex(Table.Client.FIRST_NAME))
                        + " " + cursor.getString(cursor
                        .getColumnIndex(Table.Client.LAST_NAME)));
                mapArrayList.add(row);
            }
            assert cursor != null;
            cursor.close();
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
        SimpleAdapter adapter = new SimpleAdapter(getActivity(), mapArrayList,
                android.R.layout.simple_list_item_1, new String[]{"name"}, new int[]{android.R.id.text1});
        clientList.setAdapter(adapter);
    }

    public void showImageUploadOption() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                getActivity());
        builderSingle.setTitle("Select Image");
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.select_dialog_item);
        arrayAdapter.add("Choose From Gallery");
        arrayAdapter.add("Capture From Camera");
        builderSingle.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }
        );
        builderSingle.setAdapter(arrayAdapter,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        switch (which) {
                            case 0:
                                startActivityForResult(Utils.pickImage(), Utils.ACTION_PICK_IMAGE);
                                break;
                            case 1:
                                startActivityForResult(Utils.captureImage(imageName, getActivity()), Utils.ACTION_CAPTURE_IMAGE);
                                break;
                        }
                    }
                }
        );
        builderSingle.show();
    }

    private void reloadData() {
        HashSet hs = new HashSet();
        hs.addAll(mapArrayList);
        mapArrayList.clear();
        mapArrayList.addAll(hs);

        SimpleAdapter adapter = new SimpleAdapter(getActivity(), mapArrayList,
                android.R.layout.simple_list_item_1, new String[]{"name"}, new int[]{android.R.id.text1});
        clientList.setAdapter(adapter);

    }
    private void updateParseGroupDetail() {
        group.setName(groupName.getText().toString());
        group.setTrainer(Trainer.getCurrent());
        for (Map<String, Object> map : mapArrayList) {
            Client obj = ParseObject.createWithoutData(
                    Client.class, map.get("_id").toString());
            if (group.getClients() == null ||
                    !group.getClients().contains(obj)) {
                group.addClient(obj);
            }
        }
        if (selectBitmap!=null){
            File file = new File(Utils.PROFILE_THUMBNAIL_PATH + imageName);
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                byte data[] = new byte[(int) file.length()];
                fis.read(data);
                String  encondedImage = android.util.Base64.encodeToString(data, Base64.DEFAULT);
                group.setName(imageName);
                group.setImageFile(encondedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            group.save();
        }catch (com.parse.ParseException e){
        }

        try {
            group.pin();
        }catch (com.parse.ParseException e){
        }
    }

    private void loadListItems() {
        Map<String, Object> row;
        mapArrayList=new ArrayList<Map<String, Object>>();
        for (Client client: group.getClients()) {
            row = new HashMap<String, Object>();
            row.put("_id", client.getObjectId());
            row.put("name", client.getFirstName()+client.getLastName());
            row.put("obj", client);
            mapArrayList.add(row);
        }
        SimpleAdapter adapter = new SimpleAdapter(getActivity(), mapArrayList,
                android.R.layout.simple_list_item_1, new String[]{"name"}, new int[]{android.R.id.text1});
        clientList.setAdapter(adapter);
    }
    private long updateGroup(String group_id) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Table.Group.NAME, groupName.getText().toString());
        contentValues.put(Table.Group.PHOTO_URL, imageName);
        long rowId = DBOHelper.update(Table.Group.TABLE_NAME, contentValues, group_id);
        if (rowId > 0) {
            SQLiteDatabase sqLiteDatabase = null;
            try {
                sqLiteDatabase = DatabaseHelper.instance().getWritableDatabase();
                assert sqLiteDatabase != null;
                sqLiteDatabase.execSQL("delete from " + Table.GroupClients.TABLE_NAME
                        + " where " + Table.GroupClients.GROUP_ID + " = " + group_id);
            } catch (SQLException e) {
                assert sqLiteDatabase != null;
                sqLiteDatabase.close();
                e.printStackTrace();
            } finally {
                assert sqLiteDatabase != null;
                sqLiteDatabase.close();
            }
            for (Map<String, Object> aMapArrayList : mapArrayList) {
                ContentValues mContentValue = new ContentValues();
                mContentValue.put(Table.GroupClients.GROUP_ID, group_id);
                mContentValue.put(Table.GroupClients.CLIENT_ID, aMapArrayList.get("_id").toString());
                long row_id = DBOHelper.insert(getActivity(), Table.GroupClients.TABLE_NAME, mContentValue);
            }
            return rowId;
        }
        return -1;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 123:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                       // mapArrayList.addAll((ArrayList<Map<String, Object>>) data.getSerializableExtra("data"));
                        mapArrayList=(ArrayList<Map<String, Object>>) data.getSerializableExtra("data");
                        List<Client> list=new ArrayList<Client>();
                        for (Map<String, Object> map:mapArrayList){
                            ParseQuery parseQuery = new ParseQuery(Client.class);
                            parseQuery.fromLocalDatastore();
                            parseQuery.whereEqualTo("objectId",map.get("_id"));
                            list=parseQuery.find();
                            if (list==null || list.size()==0){
                                parseQuery.whereEqualTo("objectId",map.get("_id"));
                                list=parseQuery.find();
                            }
                            map.put("obj",list.get(0));
                        }
                        reloadData();
                        Log.d("map is", mapArrayList.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case Utils.ACTION_CAPTURE_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    Utils.saveImage(Utils.LOCAL_RESOURCE_PATH + imageName, imageName);
                    selectBitmap=BitmapFactory.decodeFile(
                            Utils.PROFILE_THUMBNAIL_PATH + imageName);
                    groupImage.setImageBitmap(selectBitmap);
                }
                break;
            case Utils.ACTION_PICK_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri imageUri = data.getData();
                    Log.d("", "imageUri = " + Utils.getRealPathFromURI(getActivity(), data.getData()));
                    try {
                        Utils.saveImage(Utils.getRealPathFromURI(getActivity(), imageUri), imageName);
                        selectBitmap=BitmapFactory.decodeFile(
                                Utils.PROFILE_THUMBNAIL_PATH + imageName);
                        groupImage.setImageBitmap(selectBitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private void updateQualificationOnServer(final String groupId, final HashMap<String, Object> o) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Synchronise.Group syncGroup = new Synchronise.Group(getActivity(),
                        Utils.getLastGroupSyncTime(getActivity()));
                syncGroup.propagateDeviceObjectToServer(syncGroup.getServerObjectByWebRecordId(groupId,
                        syncGroup.getAllServerGroups()), o);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Log.d("Create Group", "Group created on the server");
            }
        }.execute();

    }
}


