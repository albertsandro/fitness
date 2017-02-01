package com.fitforbusiness.nafc.session;

import android.content.Intent;
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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fitforbusiness.database.DBOHelper;
import com.fitforbusiness.database.DatabaseHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.nafc.R;

import java.util.HashMap;

public class GoalSummaryActivity extends ActionBarActivity {

    private LinearLayout linearLayout;
    private String workout_id = "-1";
    private String session_id = "-1";
    private int HEADING_HEIGHT = 0;
    private int VIEW_HEIGHT = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_summary);
        VIEW_HEIGHT = (int) Utils.convertPixelToDensityIndependentPixels(this, 45);
        HEADING_HEIGHT = (int) Utils.convertPixelToDensityIndependentPixels(this, 30);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        try {

            session_id = getIntent().getStringExtra("_id");
            workout_id = getWorkoutId(session_id);
            Log.d("workout_id:", workout_id);
            Log.d("session_id:", session_id);

        } catch (Exception e) {
            e.printStackTrace();
        }
        linearLayout = (LinearLayout) findViewById(R.id.llGoalSummary);
        loadGoalView("");
    }

    private String getWorkoutId(String session_id) {

        String workout_id = "";
        SQLiteDatabase sqLiteDatabase = null;

        try {
            sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("select workout_id from session_measurements" +
                    " where session_id=" + session_id, null);

            if (cursor.moveToFirst()) {
                workout_id = cursor.getString(cursor.getColumnIndex("workout_id"));
                Log.d("workout_id is", workout_id + "");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return workout_id;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (DBOHelper.getSessionStatus(session_id) != 1)
            getMenuInflater().inflate(R.menu.goal_summary, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivityForResult(new Intent(this,
                    RecordWorkoutActivity.class).putExtra("session_id", session_id)
                    .putExtra("workout_id", workout_id), 12345);
        } else if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void drawExerciseHeading(final String exercise_id) {


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

            }
        } catch (Exception e) {
            e.printStackTrace();

        } finally {

        }
    }

    private void drawMeasurementHeading(String exercise_id) {

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
                textView.setText(heading + Utils.getUnit(this, heading));
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

    private void loadGoalView(String string) {

        linearLayout.removeAllViews();
        TextView tvRV;
        TextView tvEV;
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
                drawMeasurementHeading(exercise_id);
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
                            (int) Utils.convertPixelToDensityIndependentPixels(this, 30),
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


                        tvEV = new TextView(this);

                        tvEV.setText(mCursor.getString(mCursor.getColumnIndex(Table.SessionMeasurements.TARGET_VALUE)));
                        tvEV.setBackgroundResource(R.drawable.custom_btn_gray);

                        tvEV.setEllipsize(TextUtils.TruncateAt.END);
                        tvEV.setSingleLine(true);
                        LinearLayout.LayoutParams evLayoutParams = new LinearLayout.LayoutParams(
                                getEditTextWidth() / 2,
                                VIEW_HEIGHT);
                        evLayoutParams.setMargins(0, 10, 0, 0);
                        tvEV.setLayoutParams(evLayoutParams);
                        tvEV.setGravity(Gravity.CENTER);
                        tvEV.setTextColor(Color.WHITE);
                        mLinearLayout.addView(tvEV);

                        tvRV = new TextView(this);

                        tvRV.setText(mCursor.getString(mCursor.getColumnIndex(Table.SessionMeasurements.MEASURED_VALUE)));
                        tvRV.setBackgroundResource(R.drawable.custom_btn_green);
                        tvRV.setEllipsize(TextUtils.TruncateAt.END);
                        tvRV.setSingleLine(true);

                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                getEditTextWidth() / 2,
                                VIEW_HEIGHT);
                        layoutParams.setMargins(0, 10, 10, 0);
                        tvRV.setGravity(Gravity.CENTER);
                        tvRV.setLayoutParams(layoutParams);
                        mLinearLayout.addView(tvRV);


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
        return (measuredWidth - 100) / 4;
    }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loadGoalView("");
        supportInvalidateOptionsMenu();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (DBOHelper.getSessionStatus(session_id) == 1)
            menu.removeItem(R.id.action_settings);
        return super.onPrepareOptionsMenu(menu);

    }
}
