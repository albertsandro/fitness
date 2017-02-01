package com.fitforbusiness.nafc.assessment;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.fitforbusiness.Parse.Models.AssessmentFormType;
import com.fitforbusiness.Parse.Models.CompletedAssessmentForm;
import com.fitforbusiness.Parse.Models.CompletedAssessmentFormField;
import com.fitforbusiness.database.DBOHelper;
import com.fitforbusiness.database.DatabaseHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.nafc.R;
import com.fitforbusiness.webservice.Synchronise;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PARQAssessmentForm extends ActionBarActivity {

    public int VIEW_HEIGHT = 0;
    String _id = "-1";
    LinearLayout linearLayout;
    ArrayList<HashMap<String, Object>> mapArrayList;
    int type = -1;
    private String form_id = null;

    private void save(String paramString) {
        for (Object aMapArrayList : this.mapArrayList) {
            Map localMap = (Map) aMapArrayList;
            ContentValues localContentValues = new ContentValues();
            localContentValues.put("answer", ((ToggleButton) localMap.get("value")).isChecked() ? "Yes" : "No");
            DBOHelper.update(Table.CompletedAssessmentFormField.TABLE_NAME,
                    localContentValues, localMap.get("_id").toString());
            Log.d("localMap ",localMap.toString());
            localContentValues.clear();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pre_workout_assessment);
        VIEW_HEIGHT = (int) Utils.convertPixelToDensityIndependentPixels(this, 45);
        mapArrayList = new ArrayList<HashMap<String, Object>>();
        this.linearLayout = ((LinearLayout) findViewById(R.id.linearLayout));
        try {
            this.type = getIntent().getIntExtra("arg_group_or_client", -1);
            this._id = getIntent().getStringExtra("arg_group_or_client_id");
            this.form_id = getIntent().getStringExtra("form_id");
            if (this.form_id != null) {
                loadForm(this.form_id);
            }
        } catch (Exception localException) {
            localException.printStackTrace();
        }

    }

    public List<CompletedAssessmentFormField> getCompletedAssessmentFormFields(){
        List<CompletedAssessmentFormField> list=new ArrayList<CompletedAssessmentFormField>();
        CompletedAssessmentForm completedAssessmentForm=CompletedAssessmentForm.
                createWithoutData(CompletedAssessmentForm.class, form_id);
        ParseQuery parseQuery = new ParseQuery(CompletedAssessmentFormField.class);
        parseQuery.whereEqualTo("form",completedAssessmentForm);
        parseQuery.fromLocalDatastore();
        try{
            list=parseQuery.find();
        }catch (ParseException ex){
        }
        return list;
    }

    private void loadForm(String form_id) {
//        LinearLayout.LayoutParams mLinearLayoutParams = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT, 2 * VIEW_HEIGHT);
//        mLinearLayoutParams.setMargins(0, 0, 0, 0);
//
//        LinearLayout.LayoutParams mLabelParams = new LinearLayout.LayoutParams(
//                0, ViewGroup.LayoutParams.WRAP_CONTENT, 01.0f);
//        mLabelParams.setMargins(0, 0, 0, 0);
//        mLabelParams.gravity = Gravity.LEFT;
//
//        LinearLayout.LayoutParams mValueParams = new LinearLayout.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        mValueParams.setMargins(0, 0, 0, 0);
//        mValueParams.gravity = Gravity.CENTER_VERTICAL;
//
//        List<CompletedAssessmentFormField> list=getCompletedAssessmentFormFields();
//        HashMap<String, Object> map;
//        for (CompletedAssessmentFormField completedAssessmentFormField : list) {
//            map = new HashMap<String, Object>();
//            LinearLayout mLinearLayout = new LinearLayout(this);
//            mLinearLayout.setLayoutParams(mLinearLayoutParams);
//            mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
//            mLinearLayout.setWeightSum(1);
//
//            TextView label = new TextView(this);
//            label.setLayoutParams(mLabelParams);
//            label.setTextSize(17);
//            /*label.setSingleLine(true);
//            label.setEllipsize(TextUtils.TruncateAt.END);*/
//            // label.setGravity(Gravity.LEFT);
//            label.setText(completedAssessmentFormField.getTitle());
//
//            mLinearLayout.addView(label);
//
//            ToggleButton value = new ToggleButton(this);
//            value.setText("NO");
//            value.setTextOn("YES");
//            value.setTextOff("NO");
//
//            if (completedAssessmentFormField.getAnswer() != null) {
//                value.setChecked(completedAssessmentFormField.getAnswer().equals("Yes"));
//            } else {
//                value.setChecked(false);
//            }
//
//            value.setLayoutParams(mValueParams);
//            // value.setVisibility(View.GONE);
//            // value.setGravity(Gravity.CENTER_VERTICAL);
//           /* value.setT
//           value.setSingleLine(true);
//            value.setEllipsize(TextUtils.TruncateAt.END);
//            value.setHint(cursor.getString(cursor.getColumnIndex(Table.CompletedAssessmentFormField.TITLE)));
//            value.setText(cursor.getString(cursor.getColumnIndex(Table.CompletedAssessmentFormField.ANSWER)));
//            value.setBackgroundResource(R.drawable.text_field);*/
//            mLinearLayout.addView(value);
//
//            linearLayout.addView(mLinearLayout);
//            map.put("_id", completedAssessmentFormField.getObjectId());
//            map.put("value", value);
//
//            mapArrayList.add(map);
//        }
        LinearLayout.LayoutParams mLinearLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 2 * VIEW_HEIGHT);
        mLinearLayoutParams.setMargins(0, 0, 0, 0);

        LinearLayout.LayoutParams mLabelParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 01.0f);
        mLabelParams.setMargins(0, 0, 0, 0);
        mLabelParams.gravity = Gravity.LEFT;

        LinearLayout.LayoutParams mValueParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mValueParams.setMargins(0, 0, 0, 0);
        mValueParams.gravity = Gravity.CENTER_VERTICAL;

        Log.v("form_id: ", "0");
        HashMap<String, Object> map;
        String selectQuery = "select * from " + Table.CompletedAssessmentFormField.TABLE_NAME
                + " where " + Table.CompletedAssessmentFormField.FORM_ID + " = " + "1"
                + " and " + Table.DELETED + " =0 order by "
                + Table.CompletedAssessmentFormField.SORT_ORDER + " asc ";
        SQLiteDatabase sqLiteDatabase;
        sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();

        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);
        while (cursor.moveToNext()) {
            map = new HashMap<String, Object>();
            LinearLayout mLinearLayout = new LinearLayout(this);
            mLinearLayout.setLayoutParams(mLinearLayoutParams);
            mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            mLinearLayout.setWeightSum(1);

            TextView label = new TextView(this);
            label.setLayoutParams(mLabelParams);
            label.setTextSize(17);
            /*label.setSingleLine(true);
            label.setEllipsize(TextUtils.TruncateAt.END);*/
            // label.setGravity(Gravity.LEFT);
            label.setText(cursor.getString(cursor.getColumnIndex(Table.CompletedAssessmentFormField.TITLE)));

            mLinearLayout.addView(label);

            ToggleButton value = new ToggleButton(this);
            value.setText("NO");
            value.setTextOn("YES");
            value.setTextOff("NO");

            if (cursor.getString(cursor.getColumnIndex(Table.CompletedAssessmentFormField.ANSWER)) != null) {
                value.setChecked(cursor.getString(cursor.getColumnIndex(Table.CompletedAssessmentFormField.ANSWER)).equals("Yes"));
            } else {
                value.setChecked(false);
            }

            value.setLayoutParams(mValueParams);
            // value.setVisibility(View.GONE);
            // value.setGravity(Gravity.CENTER_VERTICAL);
           /* value.setT
           value.setSingleLine(true);
            value.setEllipsize(TextUtils.TruncateAt.END);
            value.setHint(cursor.getString(cursor.getColumnIndex(Table.CompletedAssessmentFormField.TITLE)));
            value.setText(cursor.getString(cursor.getColumnIndex(Table.CompletedAssessmentFormField.ANSWER)));
            value.setBackgroundResource(R.drawable.text_field);*/
            mLinearLayout.addView(value);

            linearLayout.addView(mLinearLayout);
            map.put("_id", cursor.getString(cursor.getColumnIndex(Table.CompletedAssessmentFormField.ID)));
            map.put("value", value);

            mapArrayList.add(map);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.parqassessment_form, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            updateFormName(form_id);
//            save(this.form_id);
//            updateAssessmentOnServer(form_id);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateFormName(String formId) {
        CompletedAssessmentForm completedAssessmentForm=
                CompletedAssessmentForm.createWithoutData(CompletedAssessmentForm.class, formId);

        AssessmentFormType assessmentFormType=new AssessmentFormType();
        assessmentFormType.setFormTypeName("1");
        try{
            assessmentFormType.save();
            assessmentFormType.pin();
        }catch (ParseException ex){
        }
        completedAssessmentForm.setFormType(assessmentFormType);

        for (int i=0; i<mapArrayList.size();i++){
            CompletedAssessmentFormField completedAssessmentFormField=new CompletedAssessmentFormField();
            completedAssessmentFormField.setSortOrder(i);
            completedAssessmentFormField.setAnswer(mapArrayList.get(i).get("value").toString());
            completedAssessmentFormField.setForm(completedAssessmentForm);
            completedAssessmentFormField.saveInBackground();
            completedAssessmentFormField.pinInBackground();
        }
    }
    private void updateAssessmentOnServer(final String formId) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Synchronise.Assessment syncAssessment = new Synchronise.Assessment(PARQAssessmentForm.this,
                        Utils.getLastAssessmentSyncTime(PARQAssessmentForm.this));
                syncAssessment.propagateDeviceObjectToServer(Synchronise.getDeviceObjectById(formId, syncAssessment.getDeviceAssessmentFormsSet()));
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Log.d("Update Assessment", "Assessment updated on the server");
            }
        }.execute();

    }
}
