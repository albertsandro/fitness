package com.fitforbusiness.nafc.group;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.appboy.Appboy;
import com.fitforbusiness.Parse.Models.Client;
import com.fitforbusiness.Parse.Models.Group;
import com.fitforbusiness.Parse.Models.Measurements;
import com.fitforbusiness.Parse.Models.Trainer;
import com.fitforbusiness.database.DBOHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.framework.PickItemList;
import com.fitforbusiness.framework.SwipeDetector;
import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.nafc.R;
import com.fitforbusiness.webservice.Synchronise;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;


public class AddGroupActivity extends ActionBarActivity implements View.OnClickListener {

    private static final String table_name = "Client";
    private static final String QUERY_KEY = "selectQuery";
    private static final String QUERY_STRING = "SELECT * FROM client WHERE deleted=0";
    private EditText groupName;
    private ListView clientList;
    private ArrayList<Map<String, Object>> mapArrayList;
    private ImageView groupImage;
    private String imageName;
    private SwipeDetector swipeDetector;
    private Bitmap selectBitmap;
    private ParseFile parseFile;
    @Override
    public void onStart() {
        super.onStart();
        Appboy.getInstance(this).openSession(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        Appboy.getInstance(this).closeSession(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        imageName = new Date().getTime() + ".JPG";
        mapArrayList = new ArrayList<Map<String, Object>>();

        groupName = (EditText) findViewById(R.id.etGroupName);
        clientList = (ListView) findViewById(R.id.lvClientList);
        groupImage = (ImageView) findViewById(R.id.ivGroup);
        Button chooseFile = (Button) findViewById(R.id.bChooseFile);
        Button add = (Button) findViewById(R.id.bAddGroup);
        add.setOnClickListener(this);
        chooseFile.setOnClickListener(this);
        groupImage.setOnClickListener(this);
        swipeDetector = new SwipeDetector(this);
        clientList.setOnTouchListener(swipeDetector);
        clientList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (swipeDetector.swipeDetected()) {
                    if (swipeDetector.getAction() == SwipeDetector.Action.RL) {
                        mapArrayList.remove(position);
                        reloadData();
                    }
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_group, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            if (Utils.validateFields(groupName)) {
//                long groupId = saveGroup();
//                if (groupId > 0) {
//                    Appboy.getInstance(this).logCustomEvent(Utils.CUSTOM_EVENT_KEY_CREATE_GROUP);
//                    if (Utils.isNetworkAvailable(this)) {
//                        createGroupOnServer(DBOHelper.getGroup(groupId + ""));
//                    }
                   saveParseGroup();


            }
        } else if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private long saveGroup() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Table.Group.NAME, groupName.getText().toString());
        contentValues.put(Table.Group.PHOTO_URL, imageName);
        contentValues.put(Table.SYNC_ID, UUID.randomUUID().toString());
        long id = DBOHelper.insert(this, Table.Group.TABLE_NAME, contentValues);
        if (id > 0) {
            for (Map<String, Object> aMapArrayList : mapArrayList) {
                ContentValues mContentValue = new ContentValues();
                mContentValue.put(Table.GroupClients.GROUP_ID, id);
                mContentValue.put(Table.GroupClients.CLIENT_ID, aMapArrayList.get("_id").toString());
                long row_id = DBOHelper.insert(this, Table.GroupClients.TABLE_NAME, mContentValue);
            }
        }
        return id;
    }

    private void saveParseGroup() {
        final  Group group=new Group();
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
        if (selectBitmap!=null) {
            group.setImageName(imageName);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            byte[] data;

            Bitmap bitmap = BitmapFactory.decodeFile(Utils.PROFILE_THUMBNAIL_PATH + imageName, options);
            ByteArrayOutputStream blob = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, blob);
            data = blob.toByteArray();

            Log.v("AddGroup","data1 length = "+data.length);

            while (data.length > 15000) {
                options.inSampleSize = options.inSampleSize * 2;
                bitmap = BitmapFactory.decodeFile(Utils.PROFILE_THUMBNAIL_PATH + imageName, options);
                blob = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, blob);
                data = blob.toByteArray();

                Log.v("AddGroup","data1 length = "+data.length);
            }

            Log.v("AddGroup","data1 length = "+data.length);
            parseFile = new ParseFile(imageName, data);
            parseFile.saveInBackground();

            String  encondedImage = android.util.Base64.encodeToString(data, Base64.DEFAULT);
            group.setImageFile(encondedImage);

        }

        group.pinInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (parseFile != null) {
                    group.setGroupImage(parseFile);
                }
                group.saveInBackground();
                finish();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bAddGroup:
                startActivityForResult(new Intent(this, PickItemList.class)
                        .putExtra(PickItemList.TABLE_KEY, table_name)
                        .putExtra(Utils.TITLE, "Select A Client"), 123);
                break;
            case R.id.bChooseFile:
            case R.id.ivGroup:
                showImageUploadOption();
                break;
        }
    }

    private void reloadData() {
        HashSet hs = new HashSet();
        hs.addAll(mapArrayList);
        mapArrayList.clear();
        mapArrayList.addAll(hs);
        SimpleAdapter adapter = new SimpleAdapter(this, mapArrayList,
                android.R.layout.simple_list_item_1, new String[]{"name"}, new int[]{android.R.id.text1});
        clientList.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 123:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        mapArrayList.addAll((ArrayList<Map<String, Object>>)
                                data.getSerializableExtra("data"));
                        reloadData();
                        Log.d("map is", mapArrayList.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case Utils.ACTION_PICK_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri imageUri = data.getData();
                    Log.d("", "imageUri = " + Utils.getRealPathFromURI(this, data.getData()));
                    try {
                        Utils.saveImage(Utils.getRealPathFromURI(this, imageUri), imageName);
                        selectBitmap=BitmapFactory.decodeFile(
                                Utils.PROFILE_THUMBNAIL_PATH + imageName);
                        groupImage.setImageBitmap(selectBitmap);
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
        }

    }

    public void showImageUploadOption() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setTitle("Select Image");
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this, android.R.layout.select_dialog_item);
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
                                startActivityForResult(Utils.captureImage(imageName, AddGroupActivity.this), Utils.ACTION_CAPTURE_IMAGE);
                                break;
                        }
                    }
                }
        );
        builderSingle.show();
    }

    private void createGroupOnServer(final HashMap<String, Object> group) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                new Synchronise.Group(AddGroupActivity.this,
                        Utils.getLastGroupSyncTime(AddGroupActivity.this)).createGroupOnServer(group);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Log.d("Create Group:", "Group created on the server");
            }
        }.execute();

    }

}


