package com.fitforbusiness.framework;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.fitforbusiness.nafc.R;

import java.util.ArrayList;
import java.util.HashMap;

public class ContactList extends ActionBarActivity implements AdapterView.OnItemClickListener {

    ArrayList<HashMap<String, Object>> mapArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        mapArrayList = new ContactProvider(this).getContacts();

        ContactAdapter adapter = new ContactAdapter(this,
                R.layout.custom_row, R.id.ivImageView, R.id.tvTextView, mapArrayList);
        ListView contacts = (ListView) findViewById(R.id.lvContactList);
        contacts.setAdapter(adapter);
        contacts.setOnItemClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.contact_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        HashMap map = mapArrayList.get(i);
        Intent intent = new Intent();
        intent.putExtra("map", map);
        setResult(Activity.RESULT_OK, intent);
        finish();


    }
}
