package com.fitforbusiness.nafc.assessment;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.appboy.Appboy;
import com.fitforbusiness.Parse.Models.AssessmentField;
import com.fitforbusiness.Parse.Models.AssessmentForm;
import com.fitforbusiness.Parse.Models.AssessmentFormField;
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
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AssessmentFormList extends ActionBarActivity implements AdapterView.OnItemClickListener {

    private ArrayList<Map<String, Object>> mapArrayList;
    private String _id = "-1";
    private int type = -1;
    private String selectedFormId = "-1";
    private String completedFormId = "-1";
    private String completed_form_type;

    private CompletedAssessmentForm selectForm;

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
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_assessment_form_list);

        try {
            _id = this.getIntent().getStringExtra(Utils.ARG_GROUP_OR_CLIENT_ID);
            type = this.getIntent().getIntExtra(Utils.ARG_GROUP_OR_CLIENT, -1);

        } catch (Exception e) {
            e.printStackTrace();
        }
        ListView listView = (ListView) findViewById(R.id.lvAssessments);
        loadListItems();
        SimpleAdapter adapter = new SimpleAdapter(this, mapArrayList,
                android.R.layout.simple_list_item_1, new String[]{"name"}, new int[]{android.R.id.text1});
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.assessment_form_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    public List<AssessmentForm> getCompletedAssessmentForms(){
        List<AssessmentForm> list=new ArrayList<AssessmentForm>();
        ParseQuery parseQuery = new ParseQuery(AssessmentForm.class);
        parseQuery.fromLocalDatastore();
        try{
            list=parseQuery.find();
        }catch (ParseException ex){
        }
        return list;
    }

    private void loadListItems() {
        mapArrayList = new ArrayList<Map<String, Object>>();
        Map<String, Object> row;
        row = new HashMap<String, Object>();
        row.put("_id","111111");
        row.put("name","ParQ Assessment Form");
        row.put("formType","1");
        mapArrayList.add(row);
        List<AssessmentForm> list=getCompletedAssessmentForms();
        for (AssessmentForm assessmentForm : list){
            row = new HashMap<String, Object>();
            row.put("_id", assessmentForm.getObjectId());
            row.put("name", assessmentForm.getName());
            row.put("formType", assessmentForm.getFormType());
            mapArrayList.add(row);
        }
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        selectedFormId = mapArrayList.get(i).get("_id").toString();
         completed_form_type = mapArrayList.get(i).get("_id").toString();
        copyFormData(selectedFormId);
//        if (formId> 0) {
//            Appboy.getInstance(this).logCustomEvent(Utils.CUSTOM_EVENT_KEY_CREATE_ASSESSMENT);
//            createAssessmentOnServer(formId + "");
//            finish();
//        }
    }

    public AssessmentFormType getFormType(int type){
        List<AssessmentFormType> list=new ArrayList<AssessmentFormType>();
        ParseQuery parseQuery = new ParseQuery(AssessmentFormType.class);
        parseQuery.fromLocalDatastore();
        parseQuery.whereEqualTo("formTypeName",type+"");
        try{
            list=parseQuery.find();
        }catch (ParseException ex){
        }
        return list.get(0);
    }

    private void copyFormData(String formId) {
        if (!formId.equals("111111")) {
            AssessmentForm assessmentForm = AssessmentForm.
                    createWithoutData(AssessmentForm.class, formId);
            CompletedAssessmentForm completedAssessmentForm = new CompletedAssessmentForm();
            completedAssessmentForm.setName(assessmentForm.getName());
            //completedAssessmentForm.setFormType(getFormType(type));
            completedAssessmentForm.setFormType(assessmentForm.getFormType());
            saveFormField(formId, completedAssessmentForm);
        }else {
            CompletedAssessmentForm completedAssessmentForm = new CompletedAssessmentForm();
            completedAssessmentForm.setName("ParQ Assessment Form");
            AssessmentFormType assessmentFormType=AssessmentFormType.createWithoutData(AssessmentFormType.class,"SD4fIn2Z3w");
            completedAssessmentForm.setFormType(assessmentFormType);
            saveFormField(formId, completedAssessmentForm);
        }
//        if (completed_form_id > 0) {
//            completedFormId = completed_form_id + "";
//            createCompletedFormFields(completedFormId);
//        }
//        return completed_form_id;
    }

    private  int j=0;
    private List<CompletedAssessmentFormField> list3;
    public void saveFormField(String formId, CompletedAssessmentForm completedAssessmentForm) {
        list3=new ArrayList<CompletedAssessmentFormField>();
        selectForm=completedAssessmentForm;
        Log.v("saveFormField:", "i am here");
        List<AssessmentFormField> list=new ArrayList<AssessmentFormField>();
        ParseQuery parseQuery = new ParseQuery(AssessmentFormField.class);
        parseQuery.fromLocalDatastore();
        if (!formId.equals("111111")) {
            parseQuery.whereEqualTo("form", AssessmentForm.createWithoutData(AssessmentForm.class, formId));
            try {
                list = parseQuery.find();
            } catch (ParseException ex) {
            }
            Log.v("geted list:", "i am here");
            for (j = 0; j < list.size(); j++) {
                CompletedAssessmentFormField completedAssessmentFormField = new CompletedAssessmentFormField();
                completedAssessmentFormField = new CompletedAssessmentFormField();
                completedAssessmentFormField.setTitle(list.get(j).getTitle());
                completedAssessmentFormField.setType(list.get(j).getType());
                completedAssessmentFormField.setSortOrder(list.get(j).getSortOrder());
                completedAssessmentFormField.setForm(selectForm);
                Log.v("name:", completedAssessmentFormField.getTitle());
                list3.add(completedAssessmentFormField);
                CompletedAssessmentFormField.saveAllInBackground(list3, new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            CompletedAssessmentFormField.pinAllInBackground(list3, new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        Log.v("kkk1:", "completedAssessmentformfields save success");
                                        for (int i = 0; i < list3.size(); i++) {
                                            selectForm.setAssessmentFormField(list3.get(i));
                                            try {
                                                selectForm.save();
                                                selectForm.pin();
                                            } catch (ParseException ex) {
                                            }

                                            completedFormId = selectForm.getObjectId();
                                            Log.v("index1:", "" + j);

                                            startActivity(new Intent(AssessmentFormList.this, AddPreWorkoutAssessment.class)
                                                    .putExtra(Utils.ARG_GROUP_OR_CLIENT, type)
                                                    .putExtra(Utils.ARG_GROUP_OR_CLIENT_ID, _id)
                                                    .putExtra("form_id", completedFormId));

                                            finish();
                                        }
                                    } else {
                                        Log.v("ParseException:", e.toString());
                                    }
                                }
                            });
                        } else {
                            Log.v("ParseException:", e.toString());
                        }
                    }
                });
            }
        }else {
            startActivity(new Intent(AssessmentFormList.this, PARQAssessmentForm.class)
                    .putExtra(Utils.ARG_GROUP_OR_CLIENT, type)
                    .putExtra(Utils.ARG_GROUP_OR_CLIENT_ID, _id)
                    .putExtra("form_id", completedFormId));

            finish();
        }
    }
    private void createCompletedFormFields(String completed_form_id) {
        ContentValues contentValues;
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            String query = "select  * from " + Table.AssessmentField.TABLE_NAME +
                    " where " + Table.DELETED + " =  0 and " + Table.AssessmentField.FORM_ID
                    + " = " + selectedFormId;
            Log.d("query is ", query);
            Cursor cursor = sqlDB.rawQuery(query, null);
            while (cursor.moveToNext()) {
                contentValues = new ContentValues();
                contentValues.put(Table.CompletedAssessmentFormField.TITLE,
                        cursor.getString(cursor.getColumnIndex(Table.AssessmentField.TITLE)));
                contentValues.put(Table.CompletedAssessmentFormField.SORT_ORDER,
                        cursor.getString(cursor.getColumnIndex(Table.AssessmentField.SORT_ORDER)));
                contentValues.put(Table.CompletedAssessmentFormField.TYPE,
                        cursor.getString(cursor.getColumnIndex(Table.AssessmentField.TYPE)));
                contentValues.put(Table.CompletedAssessmentFormField.FORM_ID,
                        completed_form_id);
                contentValues.put(Table.SYNC_ID, UUID.randomUUID().toString());
                long completed_field_id = DBOHelper.insert(this,
                        Table.CompletedAssessmentFormField.TABLE_NAME, contentValues);
                if (completed_field_id > 0)
                    Log.d("completed_field_id", completed_field_id + "");
            }
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


        if (completed_form_type.equals("1")) {
            startActivity(new Intent(this, PARQAssessmentForm.class)
                    .putExtra(Utils.ARG_GROUP_OR_CLIENT, type)
                    .putExtra(Utils.ARG_GROUP_OR_CLIENT_ID, _id)
                    .putExtra("form_id", completedFormId));
        } else {
            startActivity(new Intent(this, AddPreWorkoutAssessment.class)
                    .putExtra(Utils.ARG_GROUP_OR_CLIENT, type)
                    .putExtra(Utils.ARG_GROUP_OR_CLIENT_ID, _id)
                    .putExtra("form_id", completedFormId));
        }
    }

    private void createAssessmentOnServer(final String formId) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Synchronise.Assessment syncAssessment = new Synchronise.Assessment(AssessmentFormList.this,
                        Utils.getLastAssessmentSyncTime(AssessmentFormList.this));
                syncAssessment.createOnServer(Synchronise.getDeviceObjectById(formId, syncAssessment.getDeviceAssessmentFormsSet()));
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Log.d("Create Assessment", "Assessment created on the server");
            }
        }.execute();

    }
}
