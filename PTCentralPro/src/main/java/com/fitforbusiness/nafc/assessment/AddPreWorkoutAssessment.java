package com.fitforbusiness.nafc.assessment;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fitforbusiness.Parse.Models.AssessmentFormType;
import com.fitforbusiness.Parse.Models.CompletedAssessmentForm;
import com.fitforbusiness.Parse.Models.CompletedAssessmentFormField;
import com.fitforbusiness.database.DBOHelper;
import com.fitforbusiness.database.DatabaseHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.nafc.R;
import com.fitforbusiness.webservice.Synchronise;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Sanjeet on 18-Jul-14.
 */
public class AddPreWorkoutAssessment extends ActionBarActivity {

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
                localContentValues.put("answer", ((EditText) localMap.get("value")).getText().toString());
                DBOHelper.update(Table.CompletedAssessmentFormField.TABLE_NAME,
                        localContentValues, localMap.get("_id").toString());
                localContentValues.clear();
            }
        }

        protected void onCreate (Bundle paramBundle) {
            super.onCreate(paramBundle);
            setContentView(R.layout.activity_add_pre_workout_assessment);
            VIEW_HEIGHT = (int) Utils.convertPixelToDensityIndependentPixels(this, 45);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
            LinearLayout.LayoutParams mLinearLayoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            mLinearLayoutParams.setMargins(0, 10, 0, 0);

            LinearLayout.LayoutParams mLabelParams = new LinearLayout.LayoutParams(
                    0, VIEW_HEIGHT, 0.3f);
            mLabelParams.setMargins(0, 0, 10, 0);

            LinearLayout.LayoutParams mValueParams = new LinearLayout.LayoutParams(
                    0, VIEW_HEIGHT, 0.7f);
            mValueParams.setMargins(0, 0, 0, 0);

            HashMap<String, Object> map;
            List<CompletedAssessmentFormField> list=getCompletedAssessmentFormFields();
            for (CompletedAssessmentFormField completedAssessmentFormField : list){
                map = new HashMap<String, Object>();
                LinearLayout mLinearLayout = new LinearLayout(this);
                mLinearLayout.setLayoutParams(mLinearLayoutParams);
                mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                mLinearLayout.setWeightSum(1);

                TextView label = new TextView(this);
                label.setLayoutParams(mLabelParams);
                label.setTextSize(17);
                label.setSingleLine(true);
                label.setEllipsize(TextUtils.TruncateAt.END);
                label.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
                label.setText(completedAssessmentFormField.getTitle());
                mLinearLayout.addView(label);

                EditText value = new EditText(this);
                value.setLayoutParams(mValueParams);
                value.setSingleLine(true);
                value.setEllipsize(TextUtils.TruncateAt.END);
                value.setHint(completedAssessmentFormField.getTitle());
                value.setText(completedAssessmentFormField.getAnswer());
                value.setBackgroundResource(R.drawable.text_field);
                mLinearLayout.addView(value);

                linearLayout.addView(mLinearLayout);

                map.put("_id", completedAssessmentFormField.getObjectId());
                map.put("value", value);

                mapArrayList.add(map);
            }
        }

        public boolean onCreateOptionsMenu(Menu paramMenu) {
            return true;
        }

        public boolean onOptionsItemSelected(MenuItem paramMenuItem) {
            if (paramMenuItem.getItemId() == android.R.id.home) {
                updateForm(form_id);
                finish();
            }
            return super.onOptionsItemSelected(paramMenuItem);
        }

        private void updateForm(String formId) {
            CompletedAssessmentForm completedAssessmentForm=
                CompletedAssessmentForm.createWithoutData(CompletedAssessmentForm.class, formId);

            AssessmentFormType assessmentFormType=new AssessmentFormType();
            assessmentFormType.setFormTypeName("2");
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
                Synchronise.Assessment syncAssessment = new Synchronise.Assessment(AddPreWorkoutAssessment.this,
                        Utils.getLastAssessmentSyncTime(AddPreWorkoutAssessment.this));
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
