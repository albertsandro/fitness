package com.fitforbusiness.nafc.session;

import android.content.ClipData;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fitforbusiness.database.DBOHelper;
import com.fitforbusiness.database.DatabaseHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.nafc.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RecordWorkoutActivity extends ActionBarActivity implements View.OnDragListener{

    List<HashMap<String, Object>> allEds;
    private LinearLayout linearLayout;
    private String workout_id;
    private String session_id;
    private ArrayList<String> exercise_ids;
    private int VIEW_HEIGHT = 0, HEADING_HEIGHT = 0;
    private boolean isDragCommited;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_workout);
        VIEW_HEIGHT = (int) Utils.convertPixelToDensityIndependentPixels(this, 45);
        HEADING_HEIGHT = (int) Utils.convertPixelToDensityIndependentPixels(this, 30);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        try {

            workout_id = getIntent().getStringExtra("workout_id");
            session_id = getIntent().getStringExtra("session_id");

            Log.d("workout_id:", workout_id);
            Log.d("session_id:", session_id);

        } catch (Exception e) {
            e.printStackTrace();
        }
        linearLayout = (LinearLayout) findViewById(R.id.llRecordWorkout);

        findViewById(R.id.ourParent).setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                Log.e("DragParent", "Drag action = " + event.getAction());
                if (event.getAction() == DragEvent.ACTION_DRAG_ENDED ||
                        event.getAction() == DragEvent.ACTION_DROP){
                    LinearLayout pv = (LinearLayout) current_drag_view.getParent();

                    pv.removeViewAt(drag_from);
                    pv.addView(current_drag_view, pv.getChildCount());

                    drag_from = pv.indexOfChild(current_drag_view);
                    final_to = drag_from;
                    commitDrag();
                    return  true;
                }
                return false;
            }
        });
        loadGoalView();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.record_workout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Log.e("DRAG", "Option menu selected!");
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            setWorkoutRecorded(session_id);
        } else if (id == android.R.id.home) {
            updateMeasurements();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setWorkoutRecorded(String session_id) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Table.Sessions.SESSION_STATUS, 1);
        long result = DBOHelper.update(Table.Sessions.TABLE_NAME, contentValues, session_id);
        if (result > 0) {
            updateMeasurements();
            finish();
            Log.d("Session Recorded", result + "");
        }

    }

    View current_drag_view = null;
    int drag_from, original_from, final_to;
    void setupDrag(View v) {

        final String IMAGEVIEW_TAG = "icon bitmap";
        v.setOnDragListener(this);
        v.setTag(IMAGEVIEW_TAG);
        v.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                ClipData.Item item = new ClipData.Item(Uri.parse((String) v.getTag()));
                ClipData dragData = new ClipData((String) v.getTag(), new String[]{"text/plain"}, item);
                Log.e("drag", "Drag started");
                View.DragShadowBuilder myShadow = new View.DragShadowBuilder(v);
                isDragCommited = false;
                LinearLayout pv = (LinearLayout) v.getParent();
                drag_from = original_from = pv.indexOfChild(v);
                v.setVisibility(View.GONE);
                current_drag_view = v;
                v.startDrag(dragData,  // the data to be dragged
                        myShadow,  // the drag shadow builder
                        null,      // no need to use local data
                        0          // flags (not currently used, set to 0)
                );
                return true;
            }
        });
    }

    private void loadGoalView() {
        allEds = new ArrayList<>();
        exercise_ids = new ArrayList<>();
        linearLayout.removeAllViews();
        EditText ed;

        try {
            SQLiteDatabase sqlDB = DatabaseHelper.instance().getReadableDatabase();
            String query = "select exercise_id, order_value from session_measurements where " +
                    "workout_id= " + workout_id + " and session_id= " + session_id
                    + " group by exercise_id ORDER BY order_value ASC";
            Log.d("query", query);
            Cursor cursor = sqlDB.rawQuery(query, null);
            for (; cursor.moveToNext(); ) {

                String exercise_id = "" + cursor.getInt(cursor.getColumnIndex(Table.SessionMeasurements.EXERCISE_ID));
                Log.e("ORDER", "index = " + cursor.getInt(cursor.getColumnIndex(Table.SessionMeasurements.ORDER_VALUE))
                        + " id =  " + cursor.getInt(cursor.getColumnIndex(Table.SessionMeasurements.EXERCISE_ID)));
                exercise_ids.add(exercise_id);
                LinearLayout mVlayout = new LinearLayout(this);
                mVlayout.setOrientation(LinearLayout.VERTICAL);
                ViewGroup.LayoutParams params1 = new ViewGroup.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                mVlayout.setLayoutParams(params1);

                mVlayout.addView(drawExerciseHeading(exercise_id));
                mVlayout.addView(drawMeasurementHeading(exercise_id));
                for (int i = 1; i <= getMaxSetNumber(exercise_id); i++) {
                    SQLiteDatabase sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
                    String measurementQuery = "select sm.*,e.name from session_measurements sm , exercise e" +
                            " where sm.exercise_id  = " + exercise_id + " and sm.workout_id= "
                            + workout_id + " and sm.session_id= " + session_id + " and sm.set_no= " + i + " and e._id=sm.exercise_id ";
                    Log.d("measurementQuery", measurementQuery);
                    Cursor mCursor = sqLiteDatabase.rawQuery(measurementQuery, null);


                    LinearLayout mLinearLayout = new LinearLayout(this);
                    mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                    mLinearLayout.setOnDragListener(childDragListener);
                    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    mLinearLayout.setLayoutParams(params);

                    TextView setLabel = new TextView(this);
                    setLabel.setOnDragListener(childDragListener);
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
                        map = new HashMap<>();

                        ed = new EditText(this);
                        ed.setOnDragListener(childDragListener);
                        ed.setText(mCursor.getString(mCursor.getColumnIndex(Table.SessionMeasurements.MEASURED_VALUE)));
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
                    mVlayout.addView(mLinearLayout);
                    mCursor.close();
                }
                setupDrag(mVlayout);
                linearLayout.addView(mVlayout);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private View drawMeasurementHeading(String exercise_id) {

        LinearLayout mLinearLayout = new LinearLayout(this);
        mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) Utils.convertPixelToDensityIndependentPixels(this, 40));
        mLinearLayout.setLayoutParams(params);
        mLinearLayout.setPadding(0, 0, 0, 0);
        mLinearLayout.setOnDragListener(childDragListener);

        TextView setHeading = new TextView(this);
        setHeading.setOnDragListener(childDragListener);
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
        SQLiteDatabase sqLiteDatabase;
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
                textView.setOnDragListener(childDragListener);
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
            cursor.close();
        } catch (Exception e) {

            e.printStackTrace();
        }
        return mLinearLayout;
    }

    private View drawExerciseHeading(final String exercise_id) {
        SQLiteDatabase sqLiteDatabase;
        TextView tv;
        tv = new TextView(this);
        tv.setOnDragListener(childDragListener);
        try {
            sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("select sm.*,e.name from session_measurements sm , " +
                    "exercise e  where sm.exercise_id  = " + exercise_id + " and sm.workout_id= "
                    + workout_id + " and sm.session_id= " + session_id + " and e._id=sm.exercise_id", null);
            if (cursor.moveToFirst()) {
                tv.setTextColor(Color.WHITE);
                tv.setTextSize(20);
                tv.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                tv.setBackgroundResource(R.drawable.custom_btn_orange);
                tv.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable
                        (R.drawable.ic_action_action_reorder), null);
                
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
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();

        }
        return tv;
    }
    private int getEditTextWidth() {

        int measuredWidth;
        Point size = new Point();
        WindowManager w = getWindowManager();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            w.getDefaultDisplay().getSize(size);

            measuredWidth = size.x;
        } else {
            Display d = w.getDefaultDisplay();
            measuredWidth = d.getWidth();
        }
        return (measuredWidth - 100) / 4;
    }
    private int getMaxSetNumber(String exercise_id) {
        int set_no = 1;
        SQLiteDatabase sqLiteDatabase;
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
        }
        return set_no;
    }

    private void updateMeasurements() {

        for (int i = 0; i < allEds.size(); i++) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Table.SessionMeasurements.MEASURED_VALUE, ((EditText) allEds.get(i).get("edtText")).getText().toString());
            DBOHelper.update(Table.SessionMeasurements.TABLE_NAME, contentValues, allEds.get(i).get("_id").toString());
        }
    }


    void removePadding(LinearLayout v){
        for (int i = 0; i < v.getChildCount(); i++){
            v.getChildAt(i).setPadding(0,0,0,0);
        }
    }
    View.OnDragListener childDragListener = new View.OnDragListener() {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            if (event.getAction() == DragEvent.ACTION_DRAG_ENDED ||
                    event.getAction() == DragEvent.ACTION_DROP){
                LinearLayout pv = (LinearLayout) current_drag_view.getParent();
                removePadding(pv);
                final_to = pv.indexOfChild(current_drag_view);
                commitDrag();
                return  true;
            }
            return false;
        }
    };

    @Override
    public boolean onDrag(View v, DragEvent event) {
        if (event.getAction() == DragEvent.ACTION_DRAG_STARTED){
            return true;
        }if (event.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
            if (event.getY() < v.getY() + (v.getHeight()/2)){
                LinearLayout pv = (LinearLayout) v.getParent();

                pv.removeViewAt(drag_from);
                pv.addView(current_drag_view, pv.indexOfChild(v));

                drag_from = pv.indexOfChild(current_drag_view);

                v.setPadding(0,200,0,0);
            }else if (event.getY() > v.getY() + (v.getHeight()/2)){
                LinearLayout pv = (LinearLayout) v.getParent();

                pv.removeViewAt(drag_from);
                pv.addView(current_drag_view, pv.indexOfChild(v) + 1);

                drag_from = pv.indexOfChild(current_drag_view);

                v.setPadding(0,0,0,200);
            }
            return  true;
        }else if (event.getAction() == DragEvent.ACTION_DRAG_LOCATION){
            if (event.getY() < v.getY() + (v.getHeight()/2)){
                LinearLayout pv = (LinearLayout) v.getParent();

                pv.removeViewAt(drag_from);
                pv.addView(current_drag_view, pv.indexOfChild(v));

                drag_from = pv.indexOfChild(current_drag_view);

                v.setPadding(0,200,0,0);
            }else if (event.getY() > v.getY() + (v.getHeight()/2)){
                LinearLayout pv = (LinearLayout) v.getParent();

                pv.removeViewAt(drag_from);
                pv.addView(current_drag_view, pv.indexOfChild(v) + 1);

                drag_from = pv.indexOfChild(current_drag_view);

                v.setPadding(0,0,0,200);
            }
            return  true;
        }else if (event.getAction() == DragEvent.ACTION_DRAG_EXITED){
            v.setPadding(0,0,0,0);
            return true;
        }else if (event.getAction() == DragEvent.ACTION_DROP ||
                event.getAction() == DragEvent.ACTION_DRAG_ENDED){
            Log.e("drag", "Drop happened");
            LinearLayout pv = (LinearLayout) v.getParent();
            final_to = pv.indexOfChild(current_drag_view);
            commitDrag();
            v.setPadding(0,0,0,0);
            return true;
        }

        return true;
    }

    void commitDrag(){
        current_drag_view.post(new Runnable(){
            @Override
            public void run() {
                current_drag_view.setVisibility(View.VISIBLE);
            }
        });
        if (!isDragCommited)
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (isDragCommited)
                    return;
                isDragCommited = true;
                String id = exercise_ids.remove(original_from);
                exercise_ids.add(final_to, id);
                Log.e("ORDER", "From " + original_from + " To " + final_to);
                for (int i = 0; i < exercise_ids.size(); i++) {
                    Log.e("ORDER", "index = " + i + " id =  " + exercise_ids.get(i));
                    String q = "UPDATE " + Table.SessionMeasurements.TABLE_NAME +
                            " SET " + Table.SessionMeasurements.ORDER_VALUE + " = " + i +
                            " WHERE workout_id = " + workout_id + " and session_id = "
                            + session_id + " and exercise_id = " + exercise_ids.get(i) + ";";
                    SQLiteDatabase sqlDB = DatabaseHelper.instance().getReadableDatabase();
                    sqlDB.execSQL(q);
                }
            }
        }).start();

    }
}
