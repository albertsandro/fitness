package com.fitforbusiness.nafc.exercise;

import android.app.Activity;
import android.app.AlertDialog;
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

import com.fitforbusiness.Parse.Models.Exercise;
import com.fitforbusiness.Parse.Models.Measurements;
import com.fitforbusiness.framework.PickItemList;
import com.fitforbusiness.framework.SwipeDetector;
import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.nafc.R;
import com.fitforbusiness.webservice.Synchronise;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ViewExerciseActivity extends ActionBarActivity implements View.OnClickListener, AdapterView.OnItemClickListener {


    private static final String QUERY_KEY = "selectQuery";
    private SwipeDetector swipeDetector;
    private String table_name = "";
    private EditText name, muscleGroup, description, tag;
    private ListView measurementList;
    private ArrayList<Map<String, Object>> mapArrayList;
    private String imageName;
    private ImageView exerciseImage;
    private String _id = null;
    private static String programManagementWebServiceURL = Utils.BASE_URL + Utils.PROGRAM_MANAGEMENT_SERVICE;
    private Exercise exercise;
    private Bitmap selectBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_exercise);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        try {
            _id = getIntent().getStringExtra("_id");
            table_name = "Measurements";

        } catch (Exception e) {
            e.printStackTrace();
        }

        mapArrayList = new ArrayList<Map<String, Object>>();
        imageName = new Date().getTime() + ".JPG";
        exerciseImage = (ImageView) findViewById(R.id.ivExercise);
        name = (EditText) findViewById(R.id.etExerciseName);
        muscleGroup = (EditText) findViewById(R.id.etMuscleGroupName);
        description = (EditText) findViewById(R.id.etDescription);
        tag = (EditText) findViewById(R.id.etTag);
        Button addMeasurement = (Button) findViewById(R.id.bAddMeasurement);
        Button chooseFile = (Button) findViewById(R.id.bChooseFile);
        measurementList = (ListView) findViewById(R.id.lvMeasurement);
        loadExerciseDetail();
        addMeasurement.setOnClickListener(this);
        chooseFile.setOnClickListener(this);
        exerciseImage.setOnClickListener(this);
        swipeDetector = new SwipeDetector(this);
        measurementList.setOnTouchListener(swipeDetector);
        measurementList.setOnItemClickListener(this);
    }

    private void loadExerciseDetail() {
        exercise = Exercise.createWithoutData(Exercise.class, _id);
        exercise.fetchFromLocalDatastoreInBackground(new GetCallback<Exercise>() {
            @Override
            public void done(Exercise exercise, ParseException e) {
                if (e == null) {
                    name.setText(exercise.getName());
                    muscleGroup.setText(exercise.getMuscleGroup());
                    description.setText(exercise.getTags());
                    tag.setText(exercise.getTags());
                    ViewExerciseActivity.this.exercise = exercise;
                    if (exercise.getImageFile()!=null) {
                        String fileObjectStr = exercise.getString("imageFile");
                        byte[] fileObject = android.util.Base64.decode(fileObjectStr,1);
                        Bitmap bmp = BitmapFactory.decodeByteArray(fileObject,0,fileObject.length);
                        exerciseImage.setImageBitmap(bmp);
                    }
                    loadListItems();
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_exercise, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.bUpdate) {
            if (Utils.validateFields(name)) {
                updateExercise();
                finish();
            }
        } else if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ivExercise:
            case R.id.bChooseFile:
                showImageUploadOption();
                break;
            case R.id.bAddMeasurement:
                startActivityForResult(new Intent(this, PickItemList.class)
                        .putExtra(PickItemList.TABLE_KEY, table_name).putExtra("nameOnly", true)
                        .putExtra(PickItemList.WHERE_KEY, PickItemList.CURRENT_USER)
                        .putExtra(Utils.TITLE, "Select A Measurement"), 123);
                break;
        }
    }

    public void showImageUploadOption() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                this);
        builderSingle.setTitle("Select Image");
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
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
                                startActivityForResult(Utils.captureImage(imageName,
                                        ViewExerciseActivity.this), Utils.ACTION_CAPTURE_IMAGE);
                                break;
                        }
                    }
                }
        );
        builderSingle.show();
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {

            case 123:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        mapArrayList.addAll((ArrayList<Map<String, Object>>)
                                data.getSerializableExtra("data"));
                        List<Measurements> list=new ArrayList<Measurements>();
                        for (Map<String, Object> map:mapArrayList){
                            ParseQuery parseQuery = new ParseQuery(Measurements.class);
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
                    exerciseImage.setImageBitmap(selectBitmap);
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
                        exerciseImage.setImageBitmap(selectBitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }

    }

    private void reloadData() {

        HashSet<Map<String, Object>> hs = new HashSet<Map<String, Object>>();
        hs.addAll(mapArrayList);
        mapArrayList.clear();
        mapArrayList.addAll(hs);
        SimpleAdapter adapter = new SimpleAdapter(this, mapArrayList,
                android.R.layout.simple_list_item_1, new String[]{"name"}, new int[]{android.R.id.text1});
        measurementList.setAdapter(adapter);

    }

    private void updateExercise() {
        exercise.setName(name.getText().toString());
        exercise.setMuscleGroup(muscleGroup.getText().toString());
        exercise.setTags(tag.getText().toString());
        for (Map<String, Object> map : mapArrayList) {
//            Measurements obj = ParseObject.createWithoutData(
//                    Measurements.class, map.get("_id").toString());
            if (!exercise.getMeasurements().contains((Measurements)map.get("obj"))) {
                exercise.addMeasurements((Measurements)map.get("obj"));
            }
        }
        if (selectBitmap!=null) {
            File file = new File(Utils.PROFILE_THUMBNAIL_PATH + imageName);
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                byte data[] = new byte[(int) file.length()];
                fis.read(data);
                String  encondedImage = android.util.Base64.encodeToString(data, Base64.DEFAULT);
                exercise.setImageName(imageName);
                exercise.setImageFile(encondedImage);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            exercise.save();
        }catch (ParseException e){
        }

        try {
            exercise.pin();
        }catch (ParseException e){
        }
    }

    private void loadListItems() {
        Map<String, Object> row;
        for (Measurements measurements : exercise.getMeasurements()) {
            row = new HashMap<String, Object>();
            row.put("_id", measurements.getObjectId());
            row.put("name", measurements.getName());
            row.put("obj", measurements);
            mapArrayList.add(row);
        }
        SimpleAdapter adapter = new SimpleAdapter(this, mapArrayList,
                android.R.layout.simple_list_item_1, new String[]{"name"}, new int[]{android.R.id.text1});
        measurementList.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (swipeDetector.swipeDetected()) {
            if (swipeDetector.getAction() == SwipeDetector.Action.RL) {
                mapArrayList.remove(position);
                reloadData();
            }
        }
    }

    private void updateExerciseOnServer(final String syncId, final HashMap<String, Object> o) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Synchronise.Exercise syncExercise = new Synchronise.Exercise(ViewExerciseActivity.this,
                        Utils.getLastSyncTime(ViewExerciseActivity.this));
                syncExercise.propagateDeviceObjectToServer(Synchronise.getServerObjectBySyncId(syncId,
                        syncExercise.getAllServerExercises()), o);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Log.d("Update Exercise", "Exercise update on the server");
            }
        }.execute();

    }

}
