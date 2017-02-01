package com.fitforbusiness.nafc.membership;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.internal.widget.ListPopupWindow;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.appboy.Appboy;
import com.fitforbusiness.Parse.Models.Client;
import com.fitforbusiness.Parse.Models.Group;
import com.fitforbusiness.Parse.Models.Membership;
import com.fitforbusiness.Parse.Models.Trainer;
import com.fitforbusiness.database.DBOHelper;
import com.fitforbusiness.database.DatabaseHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.framework.PickItemList;
import com.fitforbusiness.framework.SwipeDetector;
import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.framework.view.MySwitch;
import com.fitforbusiness.nafc.R;
import com.fitforbusiness.nafc.TuneInitialize;
import com.fitforbusiness.webservice.TrainerWebService;
import com.mobileapptracker.MobileAppTracker;
import com.parse.ParseException;
import com.parse.ParseObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


public class AddMemberShipActivity extends ActionBarActivity implements TextWatcher, View.OnClickListener, AdapterView.OnItemClickListener {

    private static final String QUERY_KEY = "selectQuery";
    private static String QUERY_STRING = "";
    private TableRow hiddenRowOne, hiddenRowTwo, hiddenRowThree, clientDetails;
    private EditText groupClientHeader, description, noOfSession,
            costPerSession, noOfClients, packageTotal;
    private String _id;
    private int type;
    private ArrayList<Map<String, Object>> mapArrayList;
    private ListView clientList;
    private SwipeDetector swipeDetector;
    private ListPopupWindow currencyPopUp, intervalPopUp, intervalPeriodPopUp;
    private int currencyId;
    private Button currency, interval, intervalPeriod, addClients;
    private MySwitch recurringPaymentOption;
    private int intervalId;
    private int select_single_client = 133, select_group = 143;
    private int intervalPeriodId;
    private TextView titlePackageTotal, titleCostPerSession;
    private MobileAppTracker mobileAppTracker;
    private Boolean flag=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member_ship);
        setReferences();
        mobileAppTracker = TuneInitialize.initialize(this);
        currency.setOnClickListener(this);
        interval.setOnClickListener(this);
        intervalPeriod.setOnClickListener(this);
        clientList.setOnItemClickListener(this);
        addClients.setOnClickListener(this);

        switchSetUp();

        noOfSession.addTextChangedListener(this);
        costPerSession.addTextChangedListener(this);
        mapArrayList = new ArrayList<Map<String, Object>>();
        try {
            if(getIntent().getExtras().containsKey(Utils.ARG_GROUP_OR_CLIENT_ID)){}
            _id = getIntent().getStringExtra(Utils.ARG_GROUP_OR_CLIENT_ID);
            type = getIntent().getIntExtra(Utils.ARG_GROUP_OR_CLIENT, -1);
            contextualStart();
        } catch (Exception e) {
            // if used from deeplink
            deepLinkStart();
            e.printStackTrace();
        }
        swipeDetector = new SwipeDetector(this);
        clientList.setOnTouchListener(swipeDetector);


        setCurrencyPopUp();
        // setIntervalPopUp();
        setIntervalPeriodPopUp();
        if (!isCurrencyDefaultSelected()) {
            showSetDefaultCurrencyDialog();
        }else {

        }
    }

    private void showSetDefaultCurrencyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.title_ptcentral_pro))
                .setMessage(getString(R.string.dialog_message_default_currency_not_set))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        builder.setCancelable(false);
        builder.show();

    }

    private void showSendEmailToClientDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.title_ptcentral_pro))
                .setMessage(getString(R.string.dialog_message_send_email_to_clients))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (flag==true) {
                            if (type == 0) {
                                createPayment(_id);
                            } else {
                                for (Map map : mapArrayList) {
                                    createPayment(map.get("_id").toString());
                                }
                            }
                            Appboy.getInstance(AddMemberShipActivity.this)
                                    .logCustomEvent(Utils.CUSTOM_EVENT_KEY_CREATE_PAYMENT);
                            finish();
                        }
                    }
                });
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_member_ship, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuSave) {
            saveParseMembership();
            if (Utils.validateFields(noOfSession)
                    && Utils.validateFields(costPerSession)
                    ) {
                if ((mapArrayList.size() > 0) || (type == 0)) {
                    if (recurringPaymentOption.isChecked()) {
                        if (Utils.validateFields(intervalPeriod)
                                && Utils.validateFields(interval)
                                && Utils.validateFields(currency)) {
                            showSendEmailToClientDialog();
                        }
                    } else {
                        if (saveDetail() > 0) {
                            if (type == 0) {
                                createPayment(_id);
                            } else {
                                for (Map map : mapArrayList) {
                                    createPayment(map.get("_id").toString());
                                }
                            }
                            Appboy.getInstance(this).logCustomEvent(Utils.CUSTOM_EVENT_KEY_CREATE_PAYMENT);
                            finish();
                        }
                    }
                } else {
                    Toast.makeText(this, "Add at least one client!", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveParseMembership() {
        Membership membership=new Membership();
        membership.setDescription(description.getText().toString());
        membership.setNumberOfSessions(noOfSession.getText().toString());
        membership.setCostPerSession(costPerSession.getText().toString());
        membership.setTotalPackage(packageTotal.getText().toString());
        membership.setGroupId(_id);
        membership.setMembershipType("" + type);
        if (recurringPaymentOption.isChecked()) {
            membership.setRecurringPayment(1);
            membership.setIntervalPeriod(""+intervalPeriodId);
            membership.setNumberOfInterval(interval.getText().toString());
            membership.setCurrency(currency.getText().toString());
        }
        membership.setTrainer(Trainer.getCurrent());
        try {
            membership.save();
        }catch (ParseException e){
        }
        try {
            membership.pin();
        }catch (ParseException e){
        }
        flag=true;
    }

    private long saveDetail() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Table.Membership.DESCRIPTION, description.getText().toString());
        contentValues.put(Table.Membership.SESSION, noOfSession.getText().toString());
        contentValues.put(Table.Membership.COST_PER_SESSION, costPerSession.getText().toString());
        contentValues.put(Table.Membership.PACKAGE_TOTAL, packageTotal.getText().toString());
        contentValues.put(Table.Membership.GROUP_ID, _id);
        contentValues.put(Table.Membership.MEMBERSHIP_TYPE, type);
        if (recurringPaymentOption.isChecked()) {
            contentValues.put(Table.Membership.RECURRING_PAYMENT, 1);
            contentValues.put(Table.Membership.INTERVAL_PERIOD, intervalPeriodId);
            contentValues.put(Table.Membership.NO_OF_INTERVALS, interval.getText().toString());
            contentValues.put(Table.Membership.CURRENCY, currency.getText().toString());
        }
        long membership_id = DBOHelper.insert(this, Table.Membership.TABLE_NAME, contentValues);
        if (membership_id > 0 && type > 0) {
            for (Map<String, Object> aMapArrayList : mapArrayList) {
                ContentValues mContentValue = new ContentValues();
                mContentValue.put(Table.GroupMembership.MEMBERSHIP_ID, membership_id);
                mContentValue.put(Table.GroupMembership.GROUP_ID, _id);
                mContentValue.put(Table.GroupMembership.CLIENT_ID, aMapArrayList.get("_id").toString());
                long row_id = DBOHelper.insert(this, Table.GroupMembership.TABLE_NAME, mContentValue);
            }

        }
        return membership_id;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        try {
            packageTotal.setText(Utils.getCurrencySymbol(currency.getText().toString()) + " " + String.valueOf(
                    Integer.parseInt(noOfSession.getText().toString().length() == 0 ?
                            "0" : noOfSession.getText().toString())
                            * Integer.parseInt(costPerSession.getText().toString().length() == 0 ?
                            "0" : costPerSession.getText().toString())
            ));

        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bAddClients:
                startActivityForResult(new Intent(this, PickItemList.class)
                        .putExtra(QUERY_KEY, QUERY_STRING)
                        .putExtra(Utils.TITLE, "Clients"), 123);
                break;
            case R.id.bCurrency:
                currencyPopUp.show();
                break;
            case R.id.bInterval:
                if (intervalPeriod.getText().toString().length() > 0) {
                    setIntervalPopUp();
                    intervalPopUp.show();
                } else
                    Toast.makeText(this, "Please select Interval Period first", Toast.LENGTH_LONG).show();
                break;
            case R.id.bIntervalPeriod:
                intervalPeriodPopUp.show();
                break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123) {
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
        } if (requestCode == select_single_client) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    Map<String, Object> client = ((ArrayList<Map<String, Object>>)
                            data.getSerializableExtra("data")).get(0);
                    type = 0;
                    _id = (String) client.get("_id");
                    contextualStart();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } if (requestCode == select_group) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    Map<String, Object> group = ((ArrayList<Map<String, Object>>)
                            data.getSerializableExtra("data")).get(0);
                    type = 1;
                    _id = (String) group.get("_id");
                    contextualStart();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void reloadData() {
        HashSet hs = new HashSet();
        hs.addAll(mapArrayList);
        mapArrayList.clear();
        mapArrayList.addAll(hs);
        SimpleAdapter adapter = new SimpleAdapter(this, mapArrayList,
                android.R.layout.simple_list_item_1, new String[]{"name"}, new int[]{android.R.id.text1});
        clientList.setAdapter(adapter);

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

    private void setReferences() {

        groupClientHeader = (EditText) findViewById(R.id.etClientOrGroup);
        description = (EditText) findViewById(R.id.etDescription);
        noOfSession = (EditText) findViewById(R.id.etNoOfSessions);
        costPerSession = (EditText) findViewById(R.id.etCostPerSession);
        noOfClients = (EditText) findViewById(R.id.etNoOfClients);
        packageTotal = (EditText) findViewById(R.id.etPackageTotal);

        currency = (Button) findViewById(R.id.bCurrency);
        interval = (Button) findViewById(R.id.bInterval);
        intervalPeriod = (Button) findViewById(R.id.bIntervalPeriod);
        addClients = (Button) findViewById(R.id.bAddClients);

        clientList = (ListView) findViewById(R.id.lvClientList);

        clientDetails = (TableRow) findViewById(R.id.tlNoOfClients);
        hiddenRowOne = (TableRow) findViewById(R.id.trOne);
        hiddenRowTwo = (TableRow) findViewById(R.id.trTwo);
        hiddenRowThree = (TableRow) findViewById(R.id.trThree);

        titlePackageTotal = (TextView) findViewById(R.id.tvTitlePackageTotal);
        titleCostPerSession = (TextView) findViewById(R.id.tvTitleCostPerSession);

    }

    private void switchSetUp() {
        recurringPaymentOption = (MySwitch) findViewById(R.id.tbRecurringPayment);
        recurringPaymentOption.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    hiddenRowOne.setVisibility(View.VISIBLE);
                    hiddenRowTwo.setVisibility(View.VISIBLE);
                    hiddenRowThree.setVisibility(View.VISIBLE);
                } else {
                    hiddenRowOne.setVisibility(View.GONE);
                    hiddenRowTwo.setVisibility(View.GONE);
                    hiddenRowThree.setVisibility(View.GONE);
                }
            }
        });
        recurringPaymentOption.performClick();

    }

    void contextualStart(){
        if (type == 1) {
            clientDetails.setVisibility(View.VISIBLE);
            setNumberOfClients(_id);
            QUERY_STRING = "select g.* ,c._id, c.first_name," +
                    " c.last_name from group_clients g," +
                    " client c where  g.group_id= " + _id +
                    " and g.client_id=c._id;";
            loadListItems(_id);
        } else {
            setClientName(_id);
            clientDetails.setVisibility(View.GONE);
            addClients.setVisibility(View.GONE);
            clientList.setVisibility(View.GONE);
        }
    }

    void deepLinkStart(){
        groupClientHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectClientPopUp();
            }
        });
        MySwitch swType = (MySwitch) findViewById(R.id.switch_type);
        findViewById(R.id.row1).setVisibility(View.VISIBLE);
        swType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    groupClientHeader.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            selectClientPopUp();
                        }
                    });
                } else {
                    groupClientHeader.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            selectGroupPopUp();
                        }
                    });
                }
            }
        });
    }

    void selectClientPopUp(){
        String q = "select c._id,c.client_id, c.first_name,c.last_name, c.photo_url , " +
                "(select count(group_id) from sessions where group_id=c._id and session_type=0 and deleted=0)" +
                "as session_count," +
                "(select datetime(date(start_date)||' '|| start_time) from sessions " +
                "where group_id=c._id and datetime('now','localtime') <= datetime(date(start_date)||' '|| start_time) and session_type=0) as next_session " +
                "from client c where c.deleted=0 order by c.first_name asc";
        startActivityForResult(new Intent(this, PickItemList.class)
                .putExtra(QUERY_KEY, q)
                .putExtra(Utils.SELECTION_MODE, Utils.SINGLE_SELECT)
                .putExtra(Utils.TITLE, "Clients"), select_single_client);
    }void selectGroupPopUp(){
        String q = "select g.group_id,g._id, g.name, g.photo_url , " +
                "(select count(group_id) from sessions where group_id=g._id and session_type=1 and deleted=0) " +
                "as session_count," +
                "(select datetime(date(start_date)||' '|| start_time) from sessions " +
                "where group_id=g._id and datetime('now','localtime') <= datetime(date(start_date)||' '|| start_time) and session_type=1) as next_session " +
                "from groups g where g.deleted=0 order by g.name asc";
        startActivityForResult(new Intent(this, PickItemList.class)
                .putExtra(QUERY_KEY, q)
                .putExtra(Utils.SELECTION_MODE, Utils.SINGLE_SELECT)
                .putExtra("nameOnly", true)
                .putExtra(Utils.TITLE, "Groups"), select_group);
    }

    private void setCurrencyPopUp() {
        currencyPopUp = new ListPopupWindow(this);
        currencyPopUp.setAnchorView(currency);
        currencyPopUp.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.pref_currency_titles)));
        currencyPopUp.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currencyId = position;
                String titleCurrency = getResources().getStringArray(R.array.pref_currency_values)
                        [position];
                currency.setText(titleCurrency);
                titleCostPerSession.setText(getString(R.string.title_cost_per_session) + "(" + titleCurrency + ")");
                titlePackageTotal.setText(getString(R.string.title_package_total) + "(" + titleCurrency + ")");
                currencyPopUp.dismiss();
            }
        });
    }

    private void setIntervalPopUp() {
        intervalPopUp = new ListPopupWindow(this);
        intervalPopUp.setAnchorView(interval);
        intervalPopUp.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                getResources().getStringArray(intervalPeriodId == 0 ? R.array.interval_week : R.array.interval_month)));
        intervalPopUp.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                intervalId = position;
                interval.setText(getResources().getStringArray(intervalPeriodId == 0 ? R.array.interval_week : R.array.interval_month)[position]);
                intervalPopUp.dismiss();
            }
        });
    }

    private void setIntervalPeriodPopUp() {
        intervalPeriodPopUp = new ListPopupWindow(this);
        intervalPeriodPopUp.setAnchorView(intervalPeriod);
        intervalPeriodPopUp.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.interval_period_title)));
        intervalPeriodPopUp.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                intervalPeriodId = position;
                intervalPeriod.setText(getResources().getStringArray(R.array.interval_period_title)[position]);
                intervalPeriodPopUp.dismiss();
            }
        });
    }

    private void setNumberOfClients(String id) {
        SQLiteDatabase sqLiteDatabase = null;
        try {
            sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("select count(gc.group_id) " +
                    "as no_of_clients,g.name from group_clients gc,groups g where" +
                    " gc.deleted=0 and g._id=gc.group_id and gc.group_id= " + id, null);

            if (cursor.moveToFirst()) {
                noOfClients.setText(cursor.getString(cursor.getColumnIndex("no_of_clients")));
                groupClientHeader.setText(cursor.getString(cursor.getColumnIndex("name")));
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            if (sqLiteDatabase != null) {
                sqLiteDatabase.close();
            }
        } finally {
            if (sqLiteDatabase != null) {
                sqLiteDatabase.close();
            }
        }
    }

    private void setClientName(String id) {
        SQLiteDatabase sqLiteDatabase = null;
        try {
            sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("select " + Table.Client.FIRST_NAME + ","
                    + Table.Client.LAST_NAME + " from " + Table.Client.TABLE_NAME
                    + " where deleted=0 and " + Table.Client.ID + " = " + id, null);
            if (cursor.moveToFirst()) {
                groupClientHeader.setText(cursor.getString(cursor.getColumnIndex(Table.Client.FIRST_NAME)) +
                        " " + cursor.getString(cursor.getColumnIndex(Table.Client.LAST_NAME)));
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            if (sqLiteDatabase != null) {
                sqLiteDatabase.close();
            }
        } finally {
            if (sqLiteDatabase != null) {
                sqLiteDatabase.close();
            }
        }
    }

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

    private boolean isCurrencyDefaultSelected() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());

        String ListPreference = prefs.getString("currency", getString(R.string.default_currency_value));
        if (ListPreference.equalsIgnoreCase(getString(R.string.default_currency_value))) {
            // Toast.makeText(this, "Please set default currency in the setting", Toast.LENGTH_LONG).show();
            return false;
        } else {
            currency.setText(ListPreference);
            titleCostPerSession.setText(getString(R.string.title_cost_per_session) + "(" + ListPreference + ")");
            titlePackageTotal.setText(getString(R.string.title_package_total) + "(" + ListPreference + ")");
            return true;
        }

    }

    private void loadListItems(String group_id) {
        mapArrayList = new ArrayList<Map<String, Object>>();
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            String query = "select g.* ,c._id, c.first_name," +
                    " c.last_name from group_clients g," +
                    " client c where  g.group_id= " + group_id +
                    " and g.client_id=c._id;";
            Log.d("Query is", query);
            Cursor cursor = sqlDB != null ? sqlDB
                    .rawQuery(query
                            , null) : null;

            Map<String, Object> row;
            while (cursor != null && cursor.moveToNext()) {
                row = new HashMap<String, Object>();
                row.put("_id", cursor.getString(cursor
                        .getColumnIndex(Table.GroupClients.ID)));
               /* row.put("group_id", cursor.getString(cursor
                        .getColumnIndex(Table.GroupClients.CLIENT_ID)));*/
                row.put("name", cursor.getString(cursor
                        .getColumnIndex(Table.Client.FIRST_NAME))
                        + " " + cursor.getString(cursor
                        .getColumnIndex(Table.Client.LAST_NAME)));
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
        clientList.setAdapter(adapter);
    }

    private Map<String, Object> createPaymentParams(String clientId) {
        ArrayList<Map<String, Object>> orderItemArray = new ArrayList<Map<String, Object>>();
        Map<String, Object> item = new HashMap<String, Object>();
        item.put("Description", description.getText().toString());
        item.put("Name", description.getText().toString());
        item.put("Price", costPerSession.getText().toString());
        item.put("Quantity", noOfSession.getText().toString());
        orderItemArray.add(item);

        Map<String, Object> payment = new HashMap<String, Object>();
        payment.put("Trainer_Id", Utils.getTrainerId(this));
        if (type == 0) {
            payment.put("Client_Id", _id);
            payment.put("Session_Type", "I");
        } else {
            payment.put("Group_Id", _id);
            payment.put("Session_Type", "G");
            payment.put("Client_Id", clientId);
        }
        payment.put("Recurring", recurringPaymentOption.isChecked());
        if (recurringPaymentOption.isChecked()) {
            payment.put("Interval", intervalPeriodId == 1 ? "W" : "M");
            payment.put("IntervalPeriod", intervalId + 1);
        }
            payment.put("Currency", getResources().getStringArray(
                    R.array.pref_currency_values)[currencyId]);

        payment.put("SyncId", new Date().getTime() + "");
        payment.put("OrderItems", orderItemArray);


        Map<String, Object> paymentRequest = new HashMap<String, Object>();
        paymentRequest.put("paymentrequest", payment);
        return paymentRequest;
    }

    private void createPayment(final String clientId) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                Log.d("CreatePaymentRequest", "async");
                TrainerWebService.createPayment(createPaymentParams(clientId), "CreatePaymentRequestResult");
                return null;
            }
        }.execute();
    }

    public void hideSoftKey(boolean enable) {

        description.setEnabled(enable);
        description.setFocusable(enable);
        description.setFocusableInTouchMode(enable);
        description.setClickable(enable);

        noOfSession.setEnabled(enable);
        noOfSession.setFocusable(enable);
        noOfSession.setFocusableInTouchMode(enable);
        noOfSession.setClickable(enable);

        costPerSession.setEnabled(enable);
        costPerSession.setFocusable(enable);
        costPerSession.setFocusableInTouchMode(enable);
        costPerSession.setClickable(enable);

       /* noOfClients.setEnabled(enable);
        noOfClients.setFocusable(enable);
        noOfClients.setFocusableInTouchMode(enable);
        noOfClients.setClickable(enable);
        InputMethodManager inputManager = (InputMethodManager) this
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        //check if no view has focus:
        View view = this.getCurrentFocus();
        if (view == null)
            return;
        inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);*/
    }

    @Override
    protected void onResume() {
        super.onPostResume();
        mobileAppTracker.measureSession();
        hideSoftKey(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }
}
