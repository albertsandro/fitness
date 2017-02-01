package com.fitforbusiness.nafc.exercise;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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
import com.fitforbusiness.Parse.Models.Group;
import com.fitforbusiness.Parse.Models.Measurements;
import com.fitforbusiness.Parse.Models.Trainer;
import com.fitforbusiness.Parse.Models.Workout;
import com.fitforbusiness.database.DBOHelper;
import com.fitforbusiness.database.DatabaseHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.framework.CustomAsyncTaskListAdapter;
import com.fitforbusiness.framework.PickItemList;
import com.fitforbusiness.framework.SwipeDetector;
import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.nafc.R;
import com.fitforbusiness.webservice.Synchronise;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ViewWorkoutActivity extends ActionBarActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    String table_name="Exercise";
    private static final String QUERY_KEY = "selectQuery";
    private SwipeDetector swipeDetector;
    private String QUERY_STRING = "select * from exercise where deleted = 0";
    private EditText name, description;
    private ListView exerciseList;
    private ArrayList<Map<String, Object>> mapArrayList;
    private String imageName;
    private ImageView workoutImage;
    private String _id = null;
    byte[] file;
    private  Workout workout;
    private  Bitmap selectBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_workout);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        try {
            _id = getIntent().getStringExtra("_id");
        } catch (Exception e) {
            e.printStackTrace();
        }

        mapArrayList = new ArrayList<Map<String, Object>>();
        imageName = new Date().getTime() + ".JPG";
        workoutImage = (ImageView) findViewById(R.id.ivWorkout);
        name = (EditText) findViewById(R.id.etWorkoutName);

        description = (EditText) findViewById(R.id.etDescription);

        Button addExercise = (Button) findViewById(R.id.bAddExercise);
        Button chooseFile = (Button) findViewById(R.id.bChooseFile);
        exerciseList = (ListView) findViewById(R.id.lvExercise);
        LoadParseWorkout();
        addExercise.setOnClickListener(this);
        chooseFile.setOnClickListener(this);
        workoutImage.setOnClickListener(this);
        swipeDetector = new SwipeDetector(this);
        exerciseList.setOnTouchListener(swipeDetector);
        exerciseList.setOnItemClickListener(this);
    }
//    private void LoadParseWorkout(){
//        mapArrayList = new ArrayList<Map<String, Object>>();
//        ParseQuery parseQuery = new ParseQuery(Workout.class);
//        parseQuery.fromLocalDatastore();
//        parseQuery.findInBackground(new FindCallback<Workout>() {
//            @Override
//            public void done(List<Workout> list, ParseException e) {
//                if (e == null && list != null) {
//                    if (list.size() == 0) {
//                        ParseQuery parseQuery = new ParseQuery(Workout.class);
//                        parseQuery.findInBackground(new FindCallback<Workout>() {
//                            @Override
//                            public void done(List<Workout> list, ParseException e) {
//                                if (e == null && list != null) {
//                                    loadIntoWorkoutListView(list);
//                                    Workout.pinAllInBackground(list);
//                                }
//                            }
//                        });
//                    } else {
//                        loadIntoWorkoutListView(list);
//                    }
//                }
//            }
//        });
//    }

    private void LoadParseWorkout() {
        workout = Workout.createWithoutData(Workout.class, _id);
        workout.fetchFromLocalDatastoreInBackground(new GetCallback<Workout>() {
            @Override
            public void done(Workout workout, ParseException e) {
                if (e == null) {
                    name.setText(workout.getName());
                    description.setText(workout.getWorkoutDescription());
                    ViewWorkoutActivity.this.workout = workout;
                    if (workout.getImageFile()!=null) {
                        String fileObjectStr = workout.getString("imageFile");
                        byte[] fileObject = android.util.Base64.decode(fileObjectStr,1);
                        Bitmap bmp = BitmapFactory.decodeByteArray(fileObject,0,fileObject.length);
                        workoutImage.setImageBitmap(bmp);
                    }
                    loadExerciseListItems();
                }
            }
        });
    }
    private void loadWorkoutDetail() {

        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            assert sqlDB != null;
            Cursor localCursor = sqlDB.rawQuery("select  *  from " + Table.Workout.TABLE_NAME +
                    " where _id = " + _id, null);
            if (localCursor.moveToFirst()) {
                name.setText(localCursor.getString(localCursor.getColumnIndex(Table.Workout.NAME)));

                description.setText(localCursor.getString(localCursor.getColumnIndex(Table.Workout.DESCRIPTION)));

                imageName = localCursor.getString(localCursor.getColumnIndex(Table.Workout.PHOTO_URL));
                try {
                    workoutImage.setImageBitmap(BitmapFactory.decodeFile(Utils.PROFILE_THUMBNAIL_PATH
                            + imageName));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                loadListItems();
            }
            localCursor.close();
        } catch (Exception localException) {
            assert sqlDB != null;
            sqlDB.close();

        } finally {
            assert sqlDB != null;
            sqlDB.close();
        }
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
//                long isUpdated = updateWorkout();
//                if (isUpdated > 0) {
//                    HashMap<String, Object> workout;
//                    workout = DBOHelper.getWorkout(Long.valueOf(_id));
//                    String syncId = workout.get(Table.SYNC_ID).toString();
//                    updateWorkoutOnServer(syncId, workout);
//                }
                updateParseWorkout();
                finish();
            }
        } else if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

   /* @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setCustomView(R.layout.cust_actionbar);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setTitle("Qualifications");
        actionBar.getCustomView().findViewById(R.id.tbHistorical);

        return true;
    }*/

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ivExercise:
            case R.id.bChooseFile:
                showImageUploadOption();
                break;
            case R.id.bAddExercise:
                startActivityForResult(new Intent(this, PickItemList.class)
                        .putExtra(PickItemList.TABLE_KEY, table_name).putExtra("nameOnly", true)
                        .putExtra(Utils.TITLE, "Select A Exercise"), 123);
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
                                        ViewWorkoutActivity.this), Utils.ACTION_CAPTURE_IMAGE);
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
                        List<Exercise> list=new ArrayList<Exercise>();
                        for (Map<String, Object> map:mapArrayList){
                            ParseQuery parseQuery = new ParseQuery(Exercise.class);
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
                       // Log.d("map is", mapArrayList.toString());
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
                    workoutImage.setImageBitmap(BitmapFactory.decodeFile(
                            Utils.PROFILE_THUMBNAIL_PATH + imageName));
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
                        workoutImage.setImageBitmap(BitmapFactory.decodeFile(
                                Utils.PROFILE_THUMBNAIL_PATH + imageName));
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
        exerciseList.setAdapter(adapter);
    }

    private void updateParseWorkout(){
        workout.setName(name.getText().toString());
        workout.setWorkoutDescription(description.getText().toString());
        for (Map<String, Object> map : mapArrayList) {
//            Exercise obj = ParseObject.createWithoutData(
//                    Exercise.class, map.get("_id").toString());
            if (!workout.getExercises().contains((Exercise)map.get("obj"))) {
                workout.addExercises((Exercise)map.get("obj"));
            }
        }
        if (selectBitmap!=null){
            workout.setImageName(imageName);
            File file = new File(Utils.PROFILE_THUMBNAIL_PATH + imageName);
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                byte data[] = new byte[(int) file.length()];
                fis.read(data);
                String  encondedImage = android.util.Base64.encodeToString(data, Base64.DEFAULT);
                workout.setImageFile(encondedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            workout.save();
        }catch (ParseException e){
        }

        try {
            workout.pin();
        }catch (ParseException e){
        }
    }
    private long updateWorkout() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Table.Workout.NAME, name.getText().toString());
        contentValues.put(Table.Workout.DESCRIPTION, description.getText().toString());
        contentValues.put(Table.Workout.PHOTO_URL, imageName);
        try {
            if (workoutImage.getDrawable() != null) {
                Utils.saveImage(((BitmapDrawable) workoutImage
                        .getDrawable()).getBitmap(), Utils.LOCAL_RESOURCE_PATH + imageName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        long id = DBOHelper.update(Table.Workout.TABLE_NAME, contentValues, _id);
        SQLiteDatabase sqLiteDatabase = null;
        try {
            sqLiteDatabase = DatabaseHelper.instance().getWritableDatabase();
            assert sqLiteDatabase != null;
            sqLiteDatabase.execSQL("delete from " + Table.WorkoutExercises.TABLE_NAME
                    + " where " + Table.WorkoutExercises.WORKOUT_ID + " = " + _id);
        } catch (SQLException e) {
            assert sqLiteDatabase != null;
            sqLiteDatabase.close();
            e.printStackTrace();
        } finally {
            assert sqLiteDatabase != null;
            sqLiteDatabase.close();
        }
        if (id > 0) {
            for (Map<String, Object> aMapArrayList : mapArrayList) {
                Log.d("Exercise id :_id is", _id);
                ContentValues mContentValue = new ContentValues();
                mContentValue.put(Table.WorkoutExercises.WORKOUT_ID, _id);
                mContentValue.put(Table.WorkoutExercises.EXERCISE_ID, aMapArrayList.get("_id").toString());
                long row_id = DBOHelper.insert(this, Table.WorkoutExercises.TABLE_NAME, mContentValue);
            }
        }
        return id;
    }
    private void loadExerciseListItems() {
        Map<String, Object> row;
        if (workout.getExercises()==null || workout.getExercises().size()==0) {
        }else{
            for (Exercise exercise : workout.getExercises()) {
                row = new HashMap<String, Object>();
                row.put("_id", exercise.getObjectId());
                row.put("name", exercise.getName());
                row.put("obj",exercise);
                mapArrayList.add(row);
            }
            SimpleAdapter adapter = new SimpleAdapter(this, mapArrayList,
                    android.R.layout.simple_list_item_1, new String[]{"name"}, new int[]{android.R.id.text1});
            exerciseList.setAdapter(adapter);
        }
    }
    private void loadListItems() {
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            Cursor cursor = sqlDB != null ? sqlDB.rawQuery("select e._id, e.name " +
                    "  from workout_exercises  w, " +
                    " exercise e where  w.workout_id= " + _id +
                    " and w.exercise_id=e._id", null) : null;
            Map<String, Object> row;
            while (cursor != null && cursor.moveToNext()) {
                row = new HashMap<String, Object>();
                row.put("_id", cursor.getString(cursor
                        .getColumnIndex(Table.WorkoutExercises.ID)));
               /* row.put("exercise_id", cursor.getString(cursor
                        .getColumnIndex(Table.WorkoutExercises.EXERCISE_ID)));*/
                row.put("name", cursor.getString(cursor
                        .getColumnIndex(Table.Exercise.NAME)));
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
        SimpleAdapter adapter = new SimpleAdapter(this, mapArrayList,
                android.R.layout.simple_list_item_1, new String[]{"name"}, new int[]{android.R.id.text1});
        exerciseList.setAdapter(adapter);
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

    private void updateWorkoutOnServer(final String syncId, final HashMap<String, Object> o) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Synchronise.Workout syncWorkout = new Synchronise.Workout(ViewWorkoutActivity.this,
                        Utils.getLastSyncTime(ViewWorkoutActivity.this));
                syncWorkout.propagateDeviceObjectToServer(Synchronise.getServerObjectBySyncId(syncId,
                        syncWorkout.getAllServerWorkouts()), o);
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
