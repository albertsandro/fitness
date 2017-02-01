package com.fitforbusiness.nafc.membership;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TableRow;
import android.widget.TextView;

import com.fitforbusiness.database.DatabaseHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.framework.view.MySwitch;
import com.fitforbusiness.nafc.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ViewMemberShipActivity extends ActionBarActivity {

    private EditText groupClientHeader, description, noOfSession,
            costPerSession, noOfClients, packageTotal;
    private String client_or_group_id, membership_id;

    private int type;
    private ListView clientList;
    private TableRow hiddenRowOne, hiddenRowTwo, hiddenRowThree, clientDetails;
    private Button currency, interval, intervalPeriod, addClients;
    private MySwitch recurringSwitch;
    private TextView titlePackageTotal, titleCostPerSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member_ship);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setReferences();
        try {
            client_or_group_id = getIntent().getStringExtra(Utils.ARG_GROUP_OR_CLIENT_ID);
            type = getIntent().getIntExtra(Utils.ARG_GROUP_OR_CLIENT, -1);
            membership_id = getIntent().getStringExtra(Utils.ARG_MEMBERSHIP_ID);
            if (type == 1) {
                clientDetails.setVisibility(View.VISIBLE);
                setNumberOfClients(client_or_group_id);
                addClients.setVisibility(View.GONE);
                clientList.setVisibility(View.VISIBLE);
            } else {
                setClientName(client_or_group_id);
                clientDetails.setVisibility(View.GONE);
                addClients.setVisibility(View.GONE);
                clientList.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        setFieldEnable(false);
        loadDetails(membership_id);
    }

    private void setFieldEnable(boolean enable) {
        description.setEnabled(enable);
        noOfSession.setEnabled(enable);
        costPerSession.setEnabled(enable);
        noOfClients.setEnabled(enable);
        packageTotal.setEnabled(enable);
        recurringSwitch.setEnabled(enable);
        intervalPeriod.setEnabled(true);
        interval.setEnabled(true);
        currency.setEnabled(true);
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
            if (sqLiteDatabase != null) {
                sqLiteDatabase.close();
            }
            e.printStackTrace();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.add_member_ship, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.menuSave) {
            finish();
        } else if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadDetails(String _id) {
        SQLiteDatabase sqLiteDatabase = null;
        try {
            sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("select * from membership where deleted=0" +
                    " and _id= " + _id, null);
            if (cursor.moveToFirst()) {
                String currencyValue = cursor.getString(cursor.getColumnIndex(
                        Table.Membership.CURRENCY));
                description.setText(cursor.getString(cursor.getColumnIndex(Table.Membership.DESCRIPTION)));
                costPerSession.setText(cursor.getString(cursor.getColumnIndex(Table.Membership.COST_PER_SESSION)));
                int no_of_sessions = cursor.getInt(cursor.getColumnIndex(Table.Membership.PACKAGE_TOTAL) /
                        cursor.getInt(cursor.getColumnIndex(Table.Membership.PACKAGE_TOTAL)));
                noOfSession.setText(no_of_sessions + "");
                packageTotal.setText(Utils.getCurrencySymbol(currencyValue) + " "
                        + cursor.getString(cursor.getColumnIndex(Table.Membership.PACKAGE_TOTAL)));
                boolean recurringOptions = (cursor.getInt(
                        cursor.getColumnIndex(Table.Membership.RECURRING_PAYMENT)) == 1);
                setRecurringPaymentDetailsVisibility(recurringOptions);
                recurringSwitch.setChecked(recurringOptions);
                if (type == 1) {
                    loadListItems();
                }
                titleCostPerSession.setText(getString(R.string.title_package_total) + "(" + currencyValue + ")");
                titlePackageTotal.setText(getString(R.string.title_package_total) + "(" + currencyValue + ")");
                if (recurringOptions) {
                    intervalPeriod.setText(getResources()
                            .getStringArray(R.array.interval_period_title)
                            [cursor.getInt(cursor.getColumnIndex(
                            Table.Membership.INTERVAL_PERIOD))]);
                    interval.setText(cursor.getString(cursor.getColumnIndex(
                            Table.Membership.NO_OF_INTERVALS)));
                    currency.setText(cursor.getString(cursor.getColumnIndex(
                            Table.Membership.CURRENCY)));
                }
            }
            cursor.close();
        } catch (Exception e) {
            if (sqLiteDatabase != null) {
                sqLiteDatabase.close();
            }
            e.printStackTrace();
        } finally {
            if (sqLiteDatabase != null) {
                sqLiteDatabase.close();
            }
        }
    }

    private void setRecurringPaymentDetailsVisibility(boolean b) {
        if (b) {
            hiddenRowOne.setVisibility(View.VISIBLE);
            hiddenRowTwo.setVisibility(View.VISIBLE);
            hiddenRowThree.setVisibility(View.VISIBLE);
        } else {
            hiddenRowOne.setVisibility(View.GONE);
            hiddenRowTwo.setVisibility(View.GONE);
            hiddenRowThree.setVisibility(View.GONE);
        }
    }

    private void setReferences() {

        groupClientHeader = (EditText) findViewById(R.id.etClientOrGroup);
        description = (EditText) findViewById(R.id.etDescription);
        noOfSession = (EditText) findViewById(R.id.etNoOfSessions);
        costPerSession = (EditText) findViewById(R.id.etCostPerSession);
        noOfClients = (EditText) findViewById(R.id.etNoOfClients);
        packageTotal = (EditText) findViewById(R.id.etPackageTotal);

        recurringSwitch = (MySwitch) findViewById(R.id.tbRecurringPayment);
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

    private void loadListItems() {
        ArrayList<Map<String, Object>> mapArrayList = new ArrayList<Map<String, Object>>();
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            String query = "select gm.* ,c._id, c.first_name,"
                    + " c.last_name from group_membership gm,"
                    + " client c where  gm.group_id= " + client_or_group_id
                    + " and gm.membership_id=" + membership_id
                    + " and gm.client_id=c._id;";
            Log.d("Query is", query);
            Cursor cursor = sqlDB != null ? sqlDB
                    .rawQuery(query
                            , null) : null;

            Map<String, Object> row;
            while (cursor != null && cursor.moveToNext()) {
                row = new HashMap<String, Object>();
                row.put("_id", cursor.getString(cursor
                        .getColumnIndex(Table.GroupClients.ID)));
                row.put("group_id", cursor.getString(cursor
                        .getColumnIndex(Table.GroupClients.CLIENT_ID)));
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
}
