package com.fitforbusiness.nafc.exercise;

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
import com.fitforbusiness.Parse.Models.Exercise;
import com.fitforbusiness.Parse.Models.Measurements;
import com.fitforbusiness.Parse.Models.Trainer;
import com.fitforbusiness.database.DBOHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.framework.PickItemList;
import com.fitforbusiness.framework.SwipeDetector;
import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.nafc.R;
import com.fitforbusiness.webservice.Synchronise;
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

public class AddExerciseActivity extends ActionBarActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final String table_name = "Measurements";
    SwipeDetector swipeDetector;
    private EditText name, muscleGroup, description, tag;
    private ListView measurementList;
    private ArrayList<Map<String, Object>> mapArrayList;
    private String imageName;
    private ImageView exerciseImage;
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
        setContentView(R.layout.activity_add_exercise);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
        addMeasurement.setOnClickListener(this);
        chooseFile.setOnClickListener(this);
        exerciseImage.setOnClickListener(this);
        swipeDetector = new SwipeDetector(this);
        measurementList.setOnTouchListener(swipeDetector);
        measurementList.setOnItemClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        getMenuInflater().inflate(R.menu.add_exercise, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.bSave) {
            if (Utils.validateFields(name)) {
                saveExercise();
//                if (exerciseId > 0) {
//                    if (Utils.isNetworkAvailable(this)) {
//                        createExerciseOnServer(DBOHelper.getExercise(exerciseId + ""));
//                    }
//                    Appboy.getInstance(this).logCustomEvent(Utils.CUSTOM_EVENT_KEY_CREATE_EXERCISE);

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
                        .putExtra(Utils.TITLE, "Select A Measurement"), 123);
                break;
        }
    }

    public void showImageUploadOption() {

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                this);
      //  builderSingle.setIcon(R.drawable.icon);
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
                                        AddExerciseActivity.this), Utils.ACTION_CAPTURE_IMAGE);
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
                    selectBitmap=BitmapFactory.decodeFile(Utils.PROFILE_THUMBNAIL_PATH + imageName);
                    exerciseImage.setImageBitmap(BitmapFactory.decodeFile(Utils.PROFILE_THUMBNAIL_PATH + imageName));
                }
                break;
            case Utils.ACTION_PICK_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri imageUri = data.getData();
                    Log.d("", "imageUri = " + Utils.getRealPathFromURI(this, data.getData()));
                    try {
                        Utils.saveImage(Utils.getRealPathFromURI(this, imageUri), imageName);
                        selectBitmap=BitmapFactory.decodeFile(Utils.PROFILE_THUMBNAIL_PATH + imageName);
                        exerciseImage.setImageBitmap(BitmapFactory.decodeFile(Utils.PROFILE_THUMBNAIL_PATH + imageName));
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

    private void saveExercise() {
        final Exercise exercise = new Exercise();
        exercise.setName(name.getText().toString());
        exercise.setMuscleGroup(muscleGroup.getText().toString());
        exercise.setTags(description.getText().toString());
        exercise.setTags(tag.getText().toString());
        exercise.setTrainer(Trainer.getCurrent());
//        contentValues.put(Table.Exercise.PHOTO_URL, imageName);
        for (Map<String, Object> map : mapArrayList) {
            Measurements obj = ParseObject.createWithoutData(
                    Measurements.class, map.get("_id").toString());
            if (exercise.getMeasurements() == null ||
                    !exercise.getMeasurements().contains(obj)) {
                exercise.addMeasurements(obj);
            }
        }
        if (selectBitmap!=null) {
            exercise.setImageName(imageName);

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
            exercise.setImageFile(encondedImage);

        }

        exercise.pinInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (parseFile != null) {
                    exercise.setExerciseImage(parseFile);
                }
                exercise.saveInBackground();
                finish();
            }
        });
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

    private void createExerciseOnServer(final HashMap<String, Object> exercise) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                new Synchronise.Exercise(AddExerciseActivity.this,
                        Utils.getLastSyncTime(AddExerciseActivity.this)).createExerciseOnServer(exercise);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Log.d("Create Workout", "Workout created on the server");
            }
        }.execute();

    }
}
