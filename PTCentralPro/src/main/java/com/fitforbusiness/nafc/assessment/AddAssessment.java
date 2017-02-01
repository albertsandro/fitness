package com.fitforbusiness.nafc.assessment;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.fitforbusiness.Parse.Models.AssessmentField;
import com.fitforbusiness.Parse.Models.AssessmentForm;
import com.fitforbusiness.Parse.Models.AssessmentFormField;
import com.fitforbusiness.Parse.Models.AssessmentFormType;
import com.fitforbusiness.Parse.Models.Group;
import com.fitforbusiness.Parse.Models.Trainer;
import com.fitforbusiness.database.DBOHelper;
import com.fitforbusiness.database.DatabaseHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.framework.CustomAsyncTaskListAdapter;
import com.fitforbusiness.nafc.R;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;


public class AddAssessment extends ActionBarActivity implements AdapterView.OnItemClickListener, View.OnClickListener {
    ListView fields;
    ArrayList<Map<String, String>> mapArrayList;
    SimpleAdapter adapter;
    String form_id;
    String save_form_id;
    EditText form_name;
    String obj_id;
    AssessmentForm selectForm;
    AssessmentFormField assessmentFormField;
    AssessmentFormField delete_formfiled;
    Boolean formFieldflag=false;
    AssessmentFormField[] assessmentFormFields;
    Boolean flagAdd;
    Boolean flagSelectForm;

    PopupWindow popupWindow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_assessment);
        formFieldflag=false;
        form_id="";
        Log.v("flagAdd:",""+getIntent().getBundleExtra("bundle").getBoolean("flagAdd"));
        form_name = (EditText) findViewById(R.id.etFormName);
        flagAdd=getIntent().getBundleExtra("bundle").getBoolean("flagAdd");
        flagSelectForm=getIntent().getBundleExtra("bundle").getBoolean("flagAdd");
        try {
            if (!flagAdd) {
                formFieldflag=true;
                form_id =this.getIntent().getBundleExtra("bundle").getString("form_id");
                Log.v("form_id:","kkk1:"+form_id);
                form_name.setText(this.getIntent().getBundleExtra("bundle").getString("title"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        View footerView = ((LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.footer_layout, null, true);
        // Button addNew = (Button) footerView.findViewById(R.id.bAddNewFields);

        /*View headerView = ((LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.header_layout, null, false);*/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        fields = (ListView) findViewById(R.id.lvAssessmentField);

        fields.addFooterView(footerView);
        // fields.addHeaderView(headerView);


        if (form_id==null || form_id.equals("")) {
            createForm();
//            loadParseData();
        }else{
            loadParseData();
        }
        fields.setOnItemClickListener(this);
    }

    private void createForm() {

        if (form_id==null || form_id.equals("")) {
            selectForm = new AssessmentForm();
            selectForm.setName("NewForm");
            selectForm.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        selectForm.pinInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    save_form_id = selectForm.getObjectId();
                                    loadParseData();

                                } else {
                                    Log.v("exception:", e.toString());
                                }
                            }
                        });
                    } else {
                        e.toString();
                    }
                }
            });
        }
    }
//    private void loadParseData() {
//        mapArrayList = new ArrayList<Map<String, String>>();
//        if (!formFieldflag) {
//            List<AssessmentField> list = new ArrayList<AssessmentField>();
//            ParseQuery parseQuery = new ParseQuery(AssessmentField.class);
//            parseQuery.fromLocalDatastore();
//            parseQuery.orderByAscending("sortOrder");
//            try {
//                list = parseQuery.find();
//            } catch (ParseException ex) {
//            }
//            if (list.size() == 0) {
//                ParseQuery parseQuery1 = new ParseQuery(AssessmentField.class);
//                parseQuery.orderByAscending("sortOrder");
//                try {
//                    list = parseQuery1.find();
//                } catch (ParseException ex) {
//                }
//                loadIntoAssessmentFieldListView(list);
//                AssessmentField.pinAllInBackground(list);
//            } else {
//                loadIntoAssessmentFieldListView(list);
//            }
//        }else{
//            List<AssessmentFormField> list = new ArrayList<AssessmentFormField>();
//            ParseQuery parseQuery = new ParseQuery(AssessmentFormField.class);
//            parseQuery.fromLocalDatastore();
//            parseQuery.whereEqualTo("form", selectForm);
//            parseQuery.orderByAscending("sortOrder");
//            try {
//                list = parseQuery.find();
//            } catch (ParseException ex) {
//            }
//            if (list.size() == 0) {
//                ParseQuery parseQuery1 = new ParseQuery(AssessmentFormField.class);
//                if (form_id==null) {
//                    parseQuery.whereEqualTo("form", AssessmentForm.createWithoutData(AssessmentForm.class, save_form_id));
//                }else{
//                    parseQuery.whereEqualTo("form", AssessmentForm.createWithoutData(AssessmentForm.class, form_id));
//                }
//                parseQuery.orderByAscending("sortOrder");
//                try {
//                    list = parseQuery1.find();
//                } catch (ParseException ex) {
//                }
//                loadIntoAssessmentFormFieldListView(list);
//                AssessmentFormField.pinAllInBackground(list);
//            } else {
//                loadIntoAssessmentFormFieldListView(list);
//            }
//        }
//
//    }

private void loadParseData() {
    mapArrayList = new ArrayList<Map<String, String>>();
    if (!formFieldflag) {
        List<AssessmentField> list = new ArrayList<AssessmentField>();
        ParseQuery parseQuery = new ParseQuery(AssessmentField.class);
        parseQuery.fromLocalDatastore();
       // parseQuery.orderByAscending("sortOrder");
        parseQuery.findInBackground(new FindCallback<AssessmentField>() {
            @Override
            public void done(List<AssessmentField> list, ParseException e) {
                if (list.size()==0 || list==null){
                    Log.v("kkk1:","list.size:"+list.size());
                    ParseQuery parseQuery = new ParseQuery(AssessmentField.class);
                    //parseQuery.orderByAscending("sortOrder");
                    parseQuery.findInBackground(new FindCallback<AssessmentField>() {
                        @Override
                        public void done(List<AssessmentField> list, ParseException e) {
                            if (e==null) {
                                loadIntoAssessmentFieldListView(list);
                                AssessmentField.pinAllInBackground(list);
                                saveFormField();
                            }
                        }
                    });
                }else{
                    loadIntoAssessmentFieldListView(list);
                    saveFormField();
                }
            }
        });
    }else{
        List<AssessmentFormField> list = new ArrayList<AssessmentFormField>();
        if (!flagSelectForm) {
            selectForm = AssessmentForm.createWithoutData(AssessmentForm.class, form_id);
        }
        ParseQuery parseQuery = new ParseQuery(AssessmentFormField.class);
        parseQuery.fromLocalDatastore();
        parseQuery.whereEqualTo("form", selectForm);
        parseQuery.orderByAscending("sortOrder");
        parseQuery.findInBackground(new FindCallback<AssessmentFormField>() {
            @Override
            public void done(List<AssessmentFormField> list, ParseException e) {
                if (list.size() == 0 || list == null){
                    ParseQuery parseQuery = new ParseQuery(AssessmentFormField.class);
                    parseQuery.whereEqualTo("form", selectForm);
                    parseQuery.orderByAscending("sortOrder");
                    parseQuery.findInBackground(new FindCallback<AssessmentFormField>() {
                        @Override
                        public void done(List<AssessmentFormField> list, ParseException e) {
                            if (e==null) {
                                loadIntoAssessmentFormFieldListView(list);
                            }
                        }
                    });
                }else {
                    loadIntoAssessmentFormFieldListView(list);
                }
            }
        });
    }
}
    private void loadIntoAssessmentFieldListView(List<AssessmentField> list) {
        LinkedHashMap<String, String> row;
       // int i=0;
        for (AssessmentField assessmentField : list) {
           // i++;
            row = new LinkedHashMap<String, String>();
            row.put("title", assessmentField.getTitle());
            row.put("type", assessmentField.getType());
            //row.put("sort_order",""+i);
            mapArrayList.add(row);
        }
        adapter = new SimpleAdapter(this, mapArrayList, R.layout.custom_assessment_field_row,
                new String[]{"title", "title"}, new int[]{R.id.tvFieldLabel, R.id.bFieldButton});
        fields.setAdapter(adapter);
    }

    private void loadIntoAssessmentFormFieldListView(final List<AssessmentFormField> list) {
        LinkedHashMap<String, String> row;
        List<AssessmentFormField> list2=new ArrayList<AssessmentFormField>();
        int j=0;
        for (AssessmentFormField assessmentFormField : list) {
            final int  i=j;
            row = new LinkedHashMap<String, String>();
            row.put("_id", assessmentFormField.getObjectId());
            row.put("title", assessmentFormField.getTitle());
            row.put("type", assessmentFormField.getType());
            assessmentFormField.setSortOrder(i);
            ParseQuery<AssessmentFormField> query = ParseQuery.getQuery("AssessmentFormField");
            query.fromLocalDatastore();
            query.getInBackground(assessmentFormField.getObjectId(), new GetCallback<AssessmentFormField>() {
                @Override
                public void done(AssessmentFormField assessmentFormField, ParseException e) {
                    if (e==null){
                        assessmentFormField.saveInBackground();
                        assessmentFormField.pinInBackground(assessmentFormField.getObjectId(),new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (i==list.size()-1){
                                    adapter = new SimpleAdapter(AddAssessment.this, mapArrayList, R.layout.custom_assessment_field_row,
                                            new String[]{"title", "title"}, new int[]{R.id.tvFieldLabel, R.id.bFieldButton});
                                    fields.setAdapter(adapter);
                                    if (flagAdd) {
                                        popupWindow.dismiss();
                                        fields.invalidateViews();
                                    }else {
                                        flagAdd=true;
                                    }
                                }
                            }
                        });
                    }
                }
            });
            //list2.add(assessmentFormField);
            mapArrayList.add(row);
            j++;
        }
    }

    private void loadData() {
        mapArrayList = new ArrayList<Map<String, String>>();
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            String query = "select  * "
                    + " from " +
                    Table.AssessmentField.TABLE_NAME +
                    " where  deleted=0 and " + Table.AssessmentForms.FORM_ID + " = " + form_id
                    + "  order by " + Table.AssessmentField.SORT_ORDER + " asc  ";
            Log.d("query is ", query);
            Cursor cursor = sqlDB
                    .rawQuery(query
                            , null);

            LinkedHashMap<String, String> row;
            while (cursor.moveToNext()) {

                row = new LinkedHashMap<String, String>();
                row.put("_id", cursor.getString(cursor
                        .getColumnIndex(Table.AssessmentField.ID)));
                row.put("title", cursor.getString(cursor
                        .getColumnIndex(Table.AssessmentField.TITLE)));
                row.put("sort_order", cursor.getString(cursor
                        .getColumnIndex(Table.AssessmentField.SORT_ORDER)));
                //  row.put();
                row.put("type", cursor.getString(cursor
                        .getColumnIndex(Table.AssessmentField.TYPE)));
                mapArrayList.add(row);
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

        adapter = new SimpleAdapter(this, mapArrayList, R.layout.custom_assessment_field_row,
                new String[]{"title", "title"}, new int[]{R.id.tvFieldLabel, R.id.bFieldButton});
        fields.setAdapter(adapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.add_assessment, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            up_down_flag=false;
            AssessmentForm assessmentForm=new AssessmentForm();
            AssessmentFormField assessmentFormField=new AssessmentFormField();
            assessmentFormField.setSortOrder(mapArrayList.size()+1);
            assessmentFormField.setType("type");
            assessmentFormField.setTitle("New Field");
            if (form_id==null) {
                assessmentFormField.setForm(AssessmentForm.createWithoutData(AssessmentForm.class, save_form_id));
                assessmentForm=AssessmentForm.createWithoutData(AssessmentForm.class, save_form_id);
            }else{
                assessmentFormField.setForm(AssessmentForm.createWithoutData(AssessmentForm.class,form_id));
                assessmentForm=AssessmentForm.createWithoutData(AssessmentForm.class, form_id);
            }
            try {
                assessmentFormField.save();
                assessmentFormField.pin();
                assessmentForm.setAssessmentFormField(assessmentFormField);

            }catch (ParseException ex){
            }
            loadParseData();
            fields.invalidateViews();
            if ( mapArrayList.size()!=0) {
                fields.smoothScrollToPosition(mapArrayList.size() - 1);
            }

        } else if (id == android.R.id.home) {
          // saveOnBackPress();
            saveOnParseBackPress();
        }
        return super.onOptionsItemSelected(item);
    }

    public void deleteParseFormField(String field_id){

    }
    public Boolean up_down_flag=true;
    @Override
    public void onItemClick(final AdapterView<?> adapterView, View view, final int i, long l) {
        //Toast.makeText(this, "selected item is " + i, Toast.LENGTH_SHORT).show();
         up_down_flag=true;
        if (i < mapArrayList.size()) {
            LayoutInflater layoutInflater = (LayoutInflater) getBaseContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View popupView = layoutInflater.inflate(R.layout.popup, null);
             popupWindow = new PopupWindow(popupView,
                    (int) (getWindowWidth() * 0.75),
                    WindowManager.LayoutParams.WRAP_CONTENT, true);
            Map<String, String> map = mapArrayList.get(i);
            final EditText title = (EditText) popupView.findViewById(R.id.etTitle);
            final ToggleButton type=(ToggleButton) popupView.findViewById(R.id.tbIsNumber);
            title.setHint(map.get("title"));
            if (!map.get("title").equalsIgnoreCase("New Field")) {
                title.setText(map.get("title"));
        } else {
        }
            final Button delete = (Button) popupView.findViewById(R.id.bDelete);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    up_down_flag = true;
                    Map<String, String> map = mapArrayList.get(i);
                    Log.v("kkk:", "map.get(_id):" + map.get("_id"));
//                    ParseQuery<AssessmentFormField> query = ParseQuery.getQuery("AssessmentFormField");
//                    query.fromLocalDatastore();
//                    query.getInBackground(map.get("_id"), new GetCallback<AssessmentFormField>() {
//                        public void done(AssessmentFormField parseObject, ParseException e) {
//                            if (e == null) {
//                                Log.v("kkk:", "create delete_formfield success");
//                                Log.v("kkk:", "assessmentFormField.ID:" + parseObject.getTitle());
//                                parseObject.deleteInBackground();
//                                parseObject.unpinInBackground(parseObject.getObjectId(),new DeleteCallback() {
//                                    @Override
//                                    public void done(ParseException e) {
//                                        if (e == null) {
//                                            formFieldflag=true;
//                                            Log.v("kkk:", "delete_formfield unpin success");
//                                            selectForm.removeAssessmentFormField(parseObject);
//                                            Log.v("kkk", "from form removed formfield success");
//                                            loadParseData();
////                                            popupWindow.dismiss();
////                                            fields.invalidateViews();
//                                        } else {
//                                            Log.v("exception:", "kkk2" + e.toString());
//                                        }
//                                    }
//                                });
//                            } else {
//                            }
//                        }
//                    });
                    AssessmentFormField assessmentFormField_=AssessmentFormField.
                            createWithoutData(AssessmentFormField.class,map.get("_id"));
                    assessmentFormField_.fetchFromLocalDatastoreInBackground(new GetCallback<AssessmentFormField>() {
                        @Override
                        public void done(final AssessmentFormField parseObject, ParseException e) {
                            if (e == null) {
                                Log.v("kkk:", "create delete_formfield success");
                                try{
                                    parseObject.delete();
                                    Log.v("kkk:", "delete success");
                                    parseObject.unpin(parseObject.getObjectId());
                                    Log.v("kkk:", "unpin success");
                                }catch (ParseException ex){
                                }
                                formFieldflag=true;
                                loadParseData();
//                                Log.v("kkk:", "delete_formfield unpin success");
//                                selectForm.removeAssessmentFormField(parseObject);
//                                Log.v("kkk", "from form removed formfield success");
//                                loadParseData();
//
//                                            popupWindow.dismiss();
//                                            fields.invalidateViews();
//                                parseObject.unpinInBackground(parseObject.getObjectId(),new DeleteCallback() {
//                                    @Override
//                                    public void done(ParseException e) {
//                                        if (e == null) {
//                                            formFieldflag=true;
//                                            Log.v("kkk:", "delete_formfield unpin success");
//                                          //  selectForm.removeAssessmentFormField(parseObject);
//                                            Log.v("kkk", "from form removed formfield success");
//
//                                            loadParseData();
////                                            popupWindow.dismiss();
////                                            fields.invalidateViews();
//                                        } else {
//                                            Log.v("exception:", "kkk2" + e.toString());
//                                        }
//                                    }
//                                });
                            } else {
                                Log.v("exception:", "kkk12" + e.toString());
                            }
                        }
                    });
                }
            });

            Button moveUp = (Button) popupView.findViewById(R.id.bMoveUp);
            moveUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (i > 0) {
                        up_down_flag = true;
                        formFieldflag = true;
                        Map<String, String> mapCurrent = mapArrayList.get(i);
                        Map<String, String> mapPreceding = mapArrayList.get(i - 1);

                        //save to buffers
                        AssessmentFormField assessmentFormField_pre = AssessmentFormField
                                .createWithoutData(AssessmentFormField.class, mapPreceding.get("_id").toString());
                        Number buffer_mapPreceding_sortorder = assessmentFormField_pre.getSortOrder();
                        AssessmentFormField assessmentFormField_current = AssessmentFormField
                                .createWithoutData(AssessmentFormField.class, mapCurrent.get("_id").toString());
                        Number buffer_mapCurrent_sortorder = assessmentFormField_current.getSortOrder();

                        //swapping
                        assessmentFormField_pre.setSortOrder(buffer_mapCurrent_sortorder);
                        assessmentFormField_current.setSortOrder(buffer_mapPreceding_sortorder);

                        assessmentFormField_current.saveInBackground();
                        assessmentFormField_current.pinInBackground();

                        assessmentFormField_pre.saveInBackground();
                        assessmentFormField_pre.pinInBackground(assessmentFormField_pre.getObjectId(), new SaveCallback() {
                            @Override
                                public void done(ParseException e) {
                                    loadParseData();
                            }
                        });

//                            popupWindow.dismiss();
//                            fields.invalidateViews();

                    } else {
                        Toast.makeText(AddAssessment.this, "Item is already on top", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            Button moveDown = (Button) popupView.findViewById(R.id.bMoveDown);
            moveDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    up_down_flag = true;
                    formFieldflag = true;
                    if (i < mapArrayList.size() - 1) {
                        Map<String, String> mapCurrent = mapArrayList.get(i);
                        Map<String, String> mapPreceding = mapArrayList.get(i + 1);

                        //save to buffers
                        AssessmentFormField assessmentFormField_pre = AssessmentFormField
                                .createWithoutData(AssessmentFormField.class, mapPreceding.get("_id").toString());
                        Number buffer_mapPreceding_sortorder = assessmentFormField_pre.getSortOrder();
                        AssessmentFormField assessmentFormField_current = AssessmentFormField
                                .createWithoutData(AssessmentFormField.class, mapCurrent.get("_id").toString());
                        Number buffer_mapCurrent_sortorder = assessmentFormField_current.getSortOrder();

                        //swapping
                        assessmentFormField_pre.setSortOrder(buffer_mapCurrent_sortorder);
                        assessmentFormField_current.setSortOrder(buffer_mapPreceding_sortorder);

                        assessmentFormField_current.saveInBackground();
                        assessmentFormField_current.pinInBackground();

                        assessmentFormField_pre.saveInBackground();
                        assessmentFormField_pre.pinInBackground(assessmentFormField_pre.getObjectId(), new SaveCallback() {
                            @Override
                                public void done(ParseException e) {
                                    loadParseData();
                            }
                        });

//                            popupWindow.dismiss();
//                            fields.invalidateViews();

                    } else {
                        Toast.makeText(AddAssessment.this, "Item is already on bottom", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            // ListView popList = (ListView) popupView.findViewById(R.id.lvPopup);

            popupWindow.setBackgroundDrawable(new

                            BitmapDrawable()

            );
            popupWindow.setOutsideTouchable(true);
            popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener()

            {
                @Override
                public void onDismiss() {
                    if (up_down_flag == false) {
                        Map<String, String> map = mapArrayList.get(i);
                        assessmentFormField = AssessmentFormField.createWithoutData(AssessmentFormField.class, map.get("_id"));
                        assessmentFormField.setTitle(title.getText().toString());
                        assessmentFormField.setType((String) type.getText());
                            try {
                                assessmentFormField.saveInBackground();
                                assessmentFormField.pin();
                            } catch (ParseException ex) {
                            }
                        }
                        Log.v("kkk:", "delete success");

                    }
                });
            popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        } else {

            /*DBOHelper.createDefaultFormField(form_id + "", i + "");

            Map<String, String> item;

            item = new HashMap<String, String>();
            item.put("title", "New Field");

            mapArrayList.add(item);
            loadData();
            fields.invalidateViews();*/

        }
        fields.smoothScrollToPosition(i);
    }

    @Override
    public void onClick(View view) {


    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        showDeleteAlert();


    }
    public void saveOnParseBackPress() {
        String formID;
        AssessmentForm assessmentForm;
        ParseQuery<AssessmentForm> query = ParseQuery.getQuery("AssessmentForm");
        if (form_id == null || form_id.equals("")) {
            formID = save_form_id;
            Log.v("kkksave_form_id:", formID);
        } else {
            formID = form_id;
            Log.v("kkkform_id:", formID);
        }
        Log.v("kkkformID:", formID);
        query.fromLocalDatastore();
        query.getInBackground(formID, new GetCallback<AssessmentForm>() {
            @Override
            public void done(AssessmentForm assessmentForm, ParseException e) {
                assessmentForm.setName(form_name.getText().toString());
                AssessmentFormType assessmentFormType=AssessmentFormType.createWithoutData(AssessmentFormType.class,"SD4fIn2Z3w");
                assessmentForm.setFormType(assessmentFormType);
                assessmentForm.saveInBackground();
                Log.v("kkk:", "savingAssessmentForm" + assessmentForm.getName());
                assessmentForm.pinInBackground(assessmentForm.getObjectId(),new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Log.v("kkk1:", "savingAssessmentForm");
                            finish();
                        } else {
                            Log.v("exception:", e.toString());
                        }
                    }
                });
            }
        });
    }

    private List<AssessmentFormField> list2;
    public void saveFormField( ) {
        list2=new ArrayList<AssessmentFormField>();
        for (int j=0;j<mapArrayList.size();j++){
            Log.v("index:", "" + j);
            AssessmentFormField assessmentFormField=new AssessmentFormField();
            assessmentFormField.setTitle(mapArrayList.get(j).get("title").toString());
            assessmentFormField.setType(mapArrayList.get(j).get("type".toString()));
            assessmentFormField.setSortOrder(j);
            Log.v("kkk:","selectForm:"+selectForm.getObjectId());
            assessmentFormField.setForm(selectForm);
            Log.v("name:", assessmentFormField.getTitle());
            list2.add(assessmentFormField);
        }
        AssessmentFormField.saveAllInBackground(list2, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    AssessmentFormField.pinAllInBackground(list2, new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Log.v("kkk1:", "assessmentformfields save success");
                                for (int i = 0; i < list2.size(); i++) {
                                    mapArrayList.get(i).put("_id", list2.get(i).getObjectId());
                                    selectForm.setAssessmentFormField(list2.get(i));
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
        Log.v("kkk2:", "maparraylist changing objectId");
        //formFieldflag=true;
    }

    public void saveOnBackPress() {
        if (form_name.getText().toString().length() > 0) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Table.AssessmentForms.FORM_NAME,
                    form_name.getText().toString());
            DBOHelper.updateAssessmentForm(contentValues, form_id + "");
            Intent intent = new Intent();
            setResult(12345, intent);
            finish();
        } else {
            form_name.setError("Name cannot be blank !");
        }
    }

    private void showDeleteAlert() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("PTCentralPro");
        builder.setMessage("Save  Changes?")
                .setCancelable(true)
                .setNegativeButton("Save",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (form_name.getText().toString().length() > 0) {
                                    ContentValues contentValues = new ContentValues();
                                    contentValues.put(Table.AssessmentForms.FORM_NAME,
                                            form_name.getText().toString());

                                    DBOHelper.updateAssessmentForm(contentValues, form_id + "");

                                    Intent intent = new Intent();
                                    setResult(12345, intent);
                                    finish();
                                } else {
                                    form_name.setError("Name cannot be blank !");
                                    Intent intent = new Intent();
                                    setResult(12345, intent);
                                    finish();
                                }
                            }
                        }
                )
                .setPositiveButton("Discard",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
//                                deleteForm(form_id);
                            }
                        }
                );
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void deleteForm(long form_id) {

        DBOHelper.delete(Table.AssessmentField.TABLE_NAME,
                Table.AssessmentField.FIELD_ID, form_id + "");
        DBOHelper.delete(this, Table.AssessmentForms.TABLE_NAME,
                form_id + "");
    }

    private int getWindowWidth() {

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
        return measuredWidth;
    }

}
