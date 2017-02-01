package com.fitforbusiness.nafc.session;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fitforbusiness.database.DBOHelper;
import com.fitforbusiness.database.DatabaseHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.nafc.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Sanjeet on 09-Jul-14.
 */
public class SetGoalsActivity extends ActionBarActivity {

    LinearLayout linearLayout;
    List<HashMap<String, Object>> allEds;
    private String workout_id;
    private String session_id;
    private int VIEW_HEIGHT = 0;
    private int HEADING_HEIGHT = 0;
    private int addSetParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_goals);
        VIEW_HEIGHT = (int) Utils.convertPixelToDensityIndependentPixels(this, 45);
        HEADING_HEIGHT = (int) Utils.convertPixelToDensityIndependentPixels(this, 30);
        linearLayout = (LinearLayout) findViewById(R.id.llSetGoal);
        addSetParams = (int) Utils.convertPixelToDensityIndependentPixels(this, 40);
        try {
            workout_id = getIntent().getStringExtra("workout_id");
            session_id = getIntent().getStringExtra("session_id");

            Log.d("workout_id:", workout_id);
            Log.d("session_id:", session_id);

        } catch (Exception e) {
            e.printStackTrace();
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadGoalView("");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == android.R.id.home) {
            updateMeasurements();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadGoalView(String string) {
        allEds = new ArrayList<HashMap<String, Object>>();
        linearLayout.removeAllViews();
        EditText ed;

        TextView tv;
        try {
            SQLiteDatabase sqlDB = DatabaseHelper.instance().getReadableDatabase();
            String query = "select exercise_id from session_measurements where " +
                    "workout_id= " + workout_id + " and session_id= " + session_id
                    + " group by exercise_id";
            Log.d("query", query);
            Cursor cursor = sqlDB.rawQuery(query, null);
            for (; cursor.moveToNext(); ) {

                String exercise_id = "" + cursor.getInt(cursor.getColumnIndex(Table.SessionMeasurements.EXERCISE_ID));
                drawExerciseHeading(exercise_id);
                parseDrawMeasurementHeading(exercise_id);
                for (int i = 1; i <= getMaxSetNumber(exercise_id); i++) {
                    SQLiteDatabase sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
                    String measurementQuery = "select sm.*,e.name from session_measurements sm , exercise e" +
                            " where sm.exercise_id  = " + exercise_id + " and sm.workout_id= "
                            + workout_id + " and sm.session_id= " + session_id + " and sm.set_no= " + i + " and e._id=sm.exercise_id ";
                    Log.d("measurementQuery", measurementQuery);
                    Cursor mCursor = sqLiteDatabase.rawQuery(measurementQuery, null);


                    LinearLayout mLinearLayout = new LinearLayout(this);
                    mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    mLinearLayout.setLayoutParams(params);

                    TextView setLabel = new TextView(this);
                    LinearLayout.LayoutParams sLayoutParams = new LinearLayout.LayoutParams(
                            HEADING_HEIGHT,
                            VIEW_HEIGHT);
                    sLayoutParams.setMargins(0, 10, 0, 0);
                    setLabel.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.ITALIC));
                    setLabel.setEllipsize(TextUtils.TruncateAt.END);
                    setLabel.setText(i + "");
                    setLabel.setGravity(Gravity.CENTER);
                    setLabel.setLayoutParams(sLayoutParams);
                    mLinearLayout.addView(setLabel);
                    HashMap<String, Object> map;
                    while (mCursor.moveToNext()) {
                        map = new HashMap<String, Object>();

                        ed = new EditText(this);

                        ed.setText(mCursor.getString(mCursor.getColumnIndex(Table.SessionMeasurements.TARGET_VALUE)));
                        ed.setBackgroundResource(R.drawable.text_field);

                        ed.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                getEditTextWidth(),
                                VIEW_HEIGHT);
                        layoutParams.setMargins(0, 10, 10, 0);
                        ed.setLayoutParams(layoutParams);
                        mLinearLayout.addView(ed);
                        map.put("_id", mCursor.getString(mCursor.getColumnIndex(Table.SessionMeasurements.ID)));
                        map.put("edtText", ed);
                        allEds.add(map);

                    }
                    mCursor.close();
                    linearLayout.addView(mLinearLayout);
                }
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }

    }


//    private void loadGoalView(String string) {
//        allEds = new ArrayList<HashMap<String, Object>>();
//        linearLayout.removeAllViews();
//        EditText ed;
//
//        TextView tv;
//        try {
//            SQLiteDatabase sqlDB = DatabaseHelper.instance().getReadableDatabase();
//            String query = "select exercise_id from session_measurements where " +
//                    "workout_id= " + workout_id + " and session_id= " + session_id
//                    + " group by exercise_id";
//            Log.d("query", query);
//            Cursor cursor = sqlDB.rawQuery(query, null);
//            for (; cursor.moveToNext(); ) {
//
//                String exercise_id = "" + cursor.getInt(cursor.getColumnIndex(Table.SessionMeasurements.EXERCISE_ID));
//                drawExerciseHeading(exercise_id);
//                drawMeasurementHeading(exercise_id);
//                for (int i = 1; i <= getMaxSetNumber(exercise_id); i++) {
//                    SQLiteDatabase sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
//                    String measurementQuery = "select sm.*,e.name from session_measurements sm , exercise e" +
//                            " where sm.exercise_id  = " + exercise_id + " and sm.workout_id= "
//                            + workout_id + " and sm.session_id= " + session_id + " and sm.set_no= " + i + " and e._id=sm.exercise_id ";
//                    Log.d("measurementQuery", measurementQuery);
//                    Cursor mCursor = sqLiteDatabase.rawQuery(measurementQuery, null);
//
//
//                    LinearLayout mLinearLayout = new LinearLayout(this);
//                    mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
//                    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
//                            LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                    mLinearLayout.setLayoutParams(params);
//
//                    TextView setLabel = new TextView(this);
//                    LinearLayout.LayoutParams sLayoutParams = new LinearLayout.LayoutParams(
//                            HEADING_HEIGHT,
//                            VIEW_HEIGHT);
//                    sLayoutParams.setMargins(0, 10, 0, 0);
//                    setLabel.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.ITALIC));
//                    setLabel.setEllipsize(TextUtils.TruncateAt.END);
//                    setLabel.setText(i + "");
//                    setLabel.setGravity(Gravity.CENTER);
//                    setLabel.setLayoutParams(sLayoutParams);
//                    mLinearLayout.addView(setLabel);
//                    HashMap<String, Object> map;
//                    while (mCursor.moveToNext()) {
//                        map = new HashMap<String, Object>();
//
//                        ed = new EditText(this);
//
//                        ed.setText(mCursor.getString(mCursor.getColumnIndex(Table.SessionMeasurements.TARGET_VALUE)));
//                        ed.setBackgroundResource(R.drawable.text_field);
//
//                        ed.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
//                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
//                                getEditTextWidth(),
//                                VIEW_HEIGHT);
//                        layoutParams.setMargins(0, 10, 10, 0);
//                        ed.setLayoutParams(layoutParams);
//                        mLinearLayout.addView(ed);
//                        map.put("_id", mCursor.getString(mCursor.getColumnIndex(Table.SessionMeasurements.ID)));
//                        map.put("edtText", ed);
//                        allEds.add(map);
//
//                    }
//                    mCursor.close();
//                    linearLayout.addView(mLinearLayout);
//                }
//            }
//            cursor.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//
//        }
//
//    }

    private void loadParseGoalView(String string) {
        allEds = new ArrayList<HashMap<String, Object>>();
        linearLayout.removeAllViews();
        EditText ed;

        TextView tv;
        try {
            SQLiteDatabase sqlDB = DatabaseHelper.instance().getReadableDatabase();
            String query = "select exercise_id from session_measurements where " +
                    "workout_id= " + workout_id + " and session_id= " + session_id
                    + " group by exercise_id";
            Log.d("query", query);
            Cursor cursor = sqlDB.rawQuery(query, null);
            for (; cursor.moveToNext(); ) {

                String exercise_id = "" + cursor.getInt(cursor.getColumnIndex(Table.SessionMeasurements.EXERCISE_ID));
                drawExerciseHeading(exercise_id);
                parseDrawMeasurementHeading(exercise_id);
                for (int i = 1; i <= getMaxSetNumber(exercise_id); i++) {
                    SQLiteDatabase sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
                    String measurementQuery = "select sm.*,e.name from session_measurements sm , exercise e" +
                            " where sm.exercise_id  = " + exercise_id + " and sm.workout_id= "
                            + workout_id + " and sm.session_id= " + session_id + " and sm.set_no= " + i + " and e._id=sm.exercise_id ";
                    Log.d("measurementQuery", measurementQuery);
                    Cursor mCursor = sqLiteDatabase.rawQuery(measurementQuery, null);


                    LinearLayout mLinearLayout = new LinearLayout(this);
                    mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    mLinearLayout.setLayoutParams(params);

                    TextView setLabel = new TextView(this);
                    LinearLayout.LayoutParams sLayoutParams = new LinearLayout.LayoutParams(
                            HEADING_HEIGHT,
                            VIEW_HEIGHT);
                    sLayoutParams.setMargins(0, 10, 0, 0);
                    setLabel.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.ITALIC));
                    setLabel.setEllipsize(TextUtils.TruncateAt.END);
                    setLabel.setText(i + "");
                    setLabel.setGravity(Gravity.CENTER);
                    setLabel.setLayoutParams(sLayoutParams);
                    mLinearLayout.addView(setLabel);
                    HashMap<String, Object> map;
                    while (mCursor.moveToNext()) {
                        map = new HashMap<String, Object>();

                        ed = new EditText(this);

                        ed.setText(mCursor.getString(mCursor.getColumnIndex(Table.SessionMeasurements.TARGET_VALUE)));
                        ed.setBackgroundResource(R.drawable.text_field);

                        ed.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                getEditTextWidth(),
                                VIEW_HEIGHT);
                        layoutParams.setMargins(0, 10, 10, 0);
                        ed.setLayoutParams(layoutParams);
                        mLinearLayout.addView(ed);
                        map.put("_id", mCursor.getString(mCursor.getColumnIndex(Table.SessionMeasurements.ID)));
                        map.put("edtText", ed);
                        allEds.add(map);

                    }
                    mCursor.close();
                    linearLayout.addView(mLinearLayout);
                }
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }

    }

    private void drawExerciseHeading(final String exercise_id) {

        Button addSet = new Button(this);
        LinearLayout.LayoutParams addSetLayoutParams = new LinearLayout.LayoutParams(addSetParams,
                addSetParams);
       /* addSetLayoutParams.setMargins(0, 10, 0, 0);*/
        // addSet.setPadding(5, 5, 5, 5);
       /* addSet.setTextSize(20);*/
        addSet.setLayoutParams(addSetLayoutParams);
        addSet.setBackgroundResource(R.drawable.button_add);
        addSet.setTextColor(Color.WHITE);
        addSet.setGravity(Gravity.CENTER);
        //  addSet.setText("+Set");

        addSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parseAddNewSet(exercise_id);
            }
        });

        Button removeSet = new Button(this);
        LinearLayout.LayoutParams removeSetLayoutParams = new LinearLayout.LayoutParams(
                addSetParams,
                addSetParams);
       /* removeSetLayoutParams.setMargins(5, 10, 0, 0);*/
        removeSet.setLayoutParams(removeSetLayoutParams);
        //removeSet.setPadding(5, 5, 5, 5);
//        removeSet.setTextSize(20);
        removeSet.setBackgroundResource(R.drawable.button_remove);
        removeSet.setTextColor(Color.WHITE);
        removeSet.setGravity(Gravity.CENTER);
        // removeSet.setText("-Set");
        removeSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getMaxSetNumber(exercise_id) > 1) {
                    removeNewSet(exercise_id);
                } else {
                    Toast.makeText(SetGoalsActivity.this, "One set is required to create session!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        LinearLayout mLinearLayout = new LinearLayout(this);
        mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mLinearLayout.setLayoutParams(params);
        mLinearLayout.addView(addSet);
        mLinearLayout.addView(removeSet);
        SQLiteDatabase sqLiteDatabase = null;
        try {
            sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("select sm.*,e.name from session_measurements sm , " +
                    "exercise e  where sm.exercise_id  = " + exercise_id + " and sm.workout_id= "
                    + workout_id + " and sm.session_id= " + session_id + " and e._id=sm.exercise_id", null);

            if (cursor.moveToFirst()) {
                TextView tv;
                tv = new TextView(this);
                tv.setTextColor(Color.WHITE);
                tv.setTextSize(20);
                tv.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                tv.setBackgroundResource(R.drawable.custom_btn_orange);
                tv.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
                tv.setPadding(5, 5, 5, 5);
                LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        VIEW_HEIGHT);
                mLayoutParams.setMargins(0, 10, 0, 0);
                if (cursor.moveToFirst()) {
                    tv.setText(cursor.getString(cursor.getColumnIndex(Table.Exercise.NAME)));
                }
                tv.setLayoutParams(mLayoutParams);
                linearLayout.addView(tv);
                linearLayout.addView(mLinearLayout);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();

        } finally {

        }
    }

    private void removeNewSet(String exercise_id) {
        SQLiteDatabase sqLiteDatabase = DatabaseHelper.instance().getWritableDatabase();
        sqLiteDatabase.execSQL("delete from session_measurements where " +
                "session_id=" + session_id
                + " and workout_id=" + workout_id
                + " and exercise_id = " + exercise_id
                + " and set_no= " + getMaxSetNumber(exercise_id));
        updateMeasurements();
        loadGoalView("");
    }

    private void parseAddNewSet(String exercise_id) {
        int set = getMaxSetNumber(exercise_id) + 1;
        SQLiteDatabase sqLiteDatabase = null;
        sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select sm.*,e.name from session_measurements sm , exercise e" +
                " where sm.exercise_id  = " + exercise_id + " and sm.workout_id= "
                + workout_id + " and sm.session_id= " + session_id + " and sm.set_no= " + getMaxSetNumber(exercise_id) + " and e._id=sm.exercise_id ", null);
        while (cursor.moveToNext()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Table.SessionMeasurements.SESSION_ID, session_id);
            contentValues.put(Table.SessionMeasurements.WORKOUT_ID, workout_id);
            contentValues.put(Table.SessionMeasurements.EXERCISE_ID, exercise_id);
            contentValues.put(Table.SessionMeasurements.MEASUREMENT_ID, cursor.getInt(5));
            contentValues.put(Table.SessionMeasurements.SET_NO, set);
            /*contentValues.put(Table.SessionMeasurements.MEASURED_VALUE, 1);
            contentValues.put(Table.SessionMeasurements.TARGET_VALUE, 2);*/
            Log.d("inserted measurement id", "" + DBOHelper.insert(this,
                    Table.SessionMeasurements.TABLE_NAME, contentValues));
        }
        cursor.close();
        updateMeasurements();
        loadGoalView("");
    }

//    private void addNewSet(String exercise_id) {
//        int set = getMaxSetNumber(exercise_id) + 1;
//        SQLiteDatabase sqLiteDatabase = null;
//        sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
//        Cursor cursor = sqLiteDatabase.rawQuery("select sm.*,e.name from session_measurements sm , exercise e" +
//                " where sm.exercise_id  = " + exercise_id + " and sm.workout_id= "
//                + workout_id + " and sm.session_id= " + session_id + " and sm.set_no= " + getMaxSetNumber(exercise_id) + " and e._id=sm.exercise_id ", null);
//        while (cursor.moveToNext()) {
//            ContentValues contentValues = new ContentValues();
//            contentValues.put(Table.SessionMeasurements.SESSION_ID, session_id);
//            contentValues.put(Table.SessionMeasurements.WORKOUT_ID, workout_id);
//            contentValues.put(Table.SessionMeasurements.EXERCISE_ID, exercise_id);
//            contentValues.put(Table.SessionMeasurements.MEASUREMENT_ID, cursor.getInt(5));
//            contentValues.put(Table.SessionMeasurements.SET_NO, set);
//            /*contentValues.put(Table.SessionMeasurements.MEASURED_VALUE, 1);
//            contentValues.put(Table.SessionMeasurements.TARGET_VALUE, 2);*/
//            Log.d("inserted measurement id", "" + DBOHelper.insert(this,
//                    Table.SessionMeasurements.TABLE_NAME, contentValues));
//        }
//        cursor.close();
//        updateMeasurements();
//        loadGoalView("");
//    }

    private void parseDrawMeasurementHeading(String exercise_id) {
        LinearLayout mLinearLayout = new LinearLayout(this);
        mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) Utils.convertPixelToDensityIndependentPixels(this, 40));
        mLinearLayout.setLayoutParams(params);
        mLinearLayout.setPadding(0, 0, 0, 0);

        TextView setHeading = new TextView(this);
        setHeading.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams sLayoutParams = new LinearLayout.LayoutParams(
                (int) Utils.convertPixelToDensityIndependentPixels(this, 30),
                HEADING_HEIGHT);
        setHeading.setGravity(Gravity.CENTER);
        setHeading.setBackgroundResource(R.drawable.custom_btn_orange);
        setHeading.setText("Set");
        sLayoutParams.setMargins(0, 10, 0, 0);
        setHeading.setLayoutParams(sLayoutParams);
        setHeading.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        mLinearLayout.addView(setHeading);
        SQLiteDatabase sqLiteDatabase = null;
        try {
            sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("select m.name from measurement m, " +
                    "exercise_measurements em where em.measurement_id=m._id and" +
                    " em.exercise_id=" + exercise_id, null);
            while (cursor.moveToNext()) {
                TextView textView = new TextView(this);
                textView.setBackgroundResource(R.drawable.custom_btn_green);
                textView.setTextColor(Color.WHITE);
                textView.setGravity(Gravity.CENTER);
                String heading = cursor.getString(cursor.getColumnIndex(Table.Measurement.NAME));
                textView.setText(heading + Utils.getUnit(this,heading));
                textView.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        getEditTextWidth(),
                        HEADING_HEIGHT);
                layoutParams.setMargins(0, 10, 10, 0);
                textView.setLayoutParams(layoutParams);
                mLinearLayout.addView(textView);
            }
            linearLayout.addView(mLinearLayout);
            cursor.close();
        } catch (Exception e) {

            e.printStackTrace();
        } finally {

        }

    }

//    private void drawMeasurementHeading(String exercise_id) {
//        LinearLayout mLinearLayout = new LinearLayout(this);
//        mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
//        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                (int) Utils.convertPixelToDensityIndependentPixels(this, 40));
//        mLinearLayout.setLayoutParams(params);
//        mLinearLayout.setPadding(0, 0, 0, 0);
//
//        TextView setHeading = new TextView(this);
//        setHeading.setTextColor(Color.WHITE);
//        LinearLayout.LayoutParams sLayoutParams = new LinearLayout.LayoutParams(
//                (int) Utils.convertPixelToDensityIndependentPixels(this, 30),
//                HEADING_HEIGHT);
//        setHeading.setGravity(Gravity.CENTER);
//        setHeading.setBackgroundResource(R.drawable.custom_btn_orange);
//        setHeading.setText("Set");
//        sLayoutParams.setMargins(0, 10, 0, 0);
//        setHeading.setLayoutParams(sLayoutParams);
//        setHeading.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
//        mLinearLayout.addView(setHeading);
//        SQLiteDatabase sqLiteDatabase = null;
//        try {
//            sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
//            Cursor cursor = sqLiteDatabase.rawQuery("select m.name from measurement m, " +
//                    "exercise_measurements em where em.measurement_id=m._id and" +
//                    " em.exercise_id=" + exercise_id, null);
//            while (cursor.moveToNext()) {
//                TextView textView = new TextView(this);
//                textView.setBackgroundResource(R.drawable.custom_btn_green);
//                textView.setTextColor(Color.WHITE);
//                textView.setGravity(Gravity.CENTER);
//                String heading = cursor.getString(cursor.getColumnIndex(Table.Measurement.NAME));
//                textView.setText(heading + Utils.getUnit(this,heading));
//                textView.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
//                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
//                        getEditTextWidth(),
//                        HEADING_HEIGHT);
//                layoutParams.setMargins(0, 10, 10, 0);
//                textView.setLayoutParams(layoutParams);
//                mLinearLayout.addView(textView);
//            }
//            linearLayout.addView(mLinearLayout);
//            cursor.close();
//        } catch (Exception e) {
//
//            e.printStackTrace();
//        } finally {
//
//        }
//
//    }
    private int getMaxSetNumber(String exercise_id) {
        int set_no = 1;
        SQLiteDatabase sqLiteDatabase = null;
        try {
            sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("select max(sm.set_no) as max_set from session_measurements sm , " +
                    "exercise e where sm.exercise_id  = " + exercise_id + " and sm.workout_id=" + workout_id + " and " +
                    "sm.session_id=" + session_id + " and e._id=sm.exercise_id", null);

            if (cursor.moveToFirst()) {
                set_no = cursor.getInt(cursor.getColumnIndex("max_set"));
                Log.d("nax set is", set_no + "");
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return set_no;
    }

    private int getEditTextWidth() {

        int measuredWidth = 0;
        int measuredHeight = 0;
        Point size = new Point();
        WindowManager w = getWindowManager();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            w.getDefaultDisplay().getSize(size);

            measuredWidth = size.x;
            measuredHeight = size.y;
        } else {
            Display d = w.getDefaultDisplay();
            measuredWidth = d.getWidth();
            measuredHeight = d.getHeight();
        }
        return (measuredWidth - 150) / 4;
    }

    private void updateMeasurements() {

        for (HashMap<String, Object> allEd : allEds) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Table.SessionMeasurements.TARGET_VALUE, ((EditText) allEd.get("edtText")).getText().toString());
            DBOHelper.update(Table.SessionMeasurements.TABLE_NAME, contentValues, allEd.get("_id").toString());
        }

    }
}
