package com.fitforbusiness.nafc.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.ToggleButton;

import com.fitforbusiness.Parse.Models.Client;
import com.fitforbusiness.Parse.Models.Session;
import com.fitforbusiness.Parse.Models.Trainer;
import com.fitforbusiness.database.DBOHelper;
import com.fitforbusiness.database.DatabaseHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.nafc.R;
import com.fitforbusiness.webservice.Synchronise;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class ClientDrawerFragment extends Fragment implements View.OnClickListener {

    Button dateOfBirth;
    RadioButton genderMale;
    RadioButton genderFemale;
    ImageView clientImage;
    ActionBar actionBar;
    private EditText firstName, lastName, email, phone, emergencyContact,
            emergencyContactNo, allergyInfo, medicalInfo;
    private String imageName;
    private Button chooseFile;
    private Bitmap selectBitmap;
    private Client client;
    byte[] file;
    public ClientDrawerFragment() {
    }

    public static ClientDrawerFragment newInstance(Bundle bundle) {
        ClientDrawerFragment fragment = new ClientDrawerFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.drawer_client_detail, container, false);
        chooseFile = (Button) rootView.findViewById(R.id.bChooseFile);
        clientImage = (ImageView) rootView.findViewById(R.id.ivClient);
        firstName = (EditText) rootView.findViewById(R.id.etFirstName);
        lastName = (EditText) rootView.findViewById(R.id.etLastName);
        email = (EditText) rootView.findViewById(R.id.etEmailID);
        phone = (EditText) rootView.findViewById(R.id.etPhoneNo);
        emergencyContact = (EditText) rootView.findViewById(R.id.etEmergencyContactAddress);
        emergencyContactNo = (EditText) rootView.findViewById(R.id.etEmergencyContactNo);
        allergyInfo = (EditText) rootView.findViewById(R.id.etAllergyInfo);
        medicalInfo = (EditText) rootView.findViewById(R.id.etMedicalInfo);
        dateOfBirth = (Button) rootView.findViewById(R.id.bDOB);
        genderMale = (RadioButton) rootView.findViewById(R.id.radGenderMale);
        genderFemale = (RadioButton) rootView.findViewById(R.id.radGenderFemale);
        clientImage.setOnClickListener(this);
        dateOfBirth.setOnClickListener(this);
        chooseFile.setOnClickListener(this);
        setFieldsEditable(false);
        try {
            loadParseClientDetails(getArguments().getString(Utils.ARG_GROUP_OR_CLIENT_ID));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rootView;
    }
    private void loadParseClientDetails(String _id){
        ParseQuery parseQuery = new ParseQuery(Client.class);
        parseQuery.fromLocalDatastore();
        parseQuery.whereEqualTo("objectId",_id);
        parseQuery.findInBackground(new FindCallback<Client>() {
            @Override
            public void done(List<Client> list, com.parse.ParseException e) {
                if (e == null && list != null) {
                    if (list.size() == 0) {

                    } else {
                        loadIntoListView(list);
                    }
                }
            }
        });
    }
    private Date convertStringToDate(String strDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        //SimpleDateFormat dateFormat = new SimpleDateFormat("mm dd yyyy");
        Date convertedDate = new Date();
        try {
            convertedDate = dateFormat.parse(strDate);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return convertedDate;
    }

    private String convertDateToString(Date date) {
        DateFormat df = new SimpleDateFormat("dd MMM yyyy");
        String reportDate = df.format(date);
        return reportDate;
    }
    private void loadIntoListView(List<Client> list) {
        for (Client client : list) {
            ClientDrawerFragment.this.client=client;
            firstName.setText(client.getFirstName());
            lastName.setText(client.getLastName());
            email.setText(client.getEmail());
            phone.setText(client.getEmail());
            emergencyContact.setText(client.getEmergencyContact());
            emergencyContactNo.setText(client.getEmergencyContactNumber());
            dateOfBirth.setText(Utils.formatConversion(convertDateToString(client.getDateOfBirth())));
            genderMale.setChecked(client.getGender());
            medicalInfo.setText(client.getMedicalInformation());
            allergyInfo.setText(client.getAllergyInformation());
            imageName=client.getImageName();
            if (imageName==null){
                imageName = new Date().getTime() + ".JPG";
            }
            if (client.get("imageFile")!=null) {
                String fileObjectStr = client.getString("imageFile");
                byte[] fileObject = android.util.Base64.decode(fileObjectStr, 1);
                Bitmap bmp = BitmapFactory.decodeByteArray(fileObject,0,fileObject.length);
                clientImage.setImageBitmap(bmp);
            }
        }
    }
    private void loadClientDetail(String client_id) {
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            String query = "select  * "
                    + " from " +
                    Table.Client.TABLE_NAME +
                    " where " + Table.DELETED + " = 0 and " + Table.ID + " =  " + client_id;
            Log.d("query is ", query);
            Cursor cursor = sqlDB
                    .rawQuery(query
                            , null);
            if (cursor.moveToNext()) {
                firstName.setText(cursor.getString(cursor
                        .getColumnIndex(Table.Client.FIRST_NAME)));
                lastName.setText(cursor.getString(cursor
                        .getColumnIndex(Table.Client.LAST_NAME)));
                email.setText(cursor.getString(cursor
                        .getColumnIndex(Table.Client.EMAIL)));
                phone.setText(cursor.getString(cursor
                        .getColumnIndex(Table.Client.CONTACT_NO)));
                emergencyContact.setText(cursor.getString(cursor
                        .getColumnIndex(Table.Client.EMERGENCY_CONTACT_ADDRESS)));
                emergencyContactNo.setText(cursor.getString(cursor
                        .getColumnIndex(Table.Client.EMERGENCY_CONTACT_NUMBER)));
                dateOfBirth.setText(Utils.formatConversion(cursor.getString(cursor
                        .getColumnIndex(Table.Client.DOB))));
                genderMale.setChecked(cursor.getInt(cursor
                        .getColumnIndex(Table.Client.GENDER)) == Utils.MALE);
                medicalInfo.setText(cursor.getString(cursor
                        .getColumnIndex(Table.Client.MEDICAL_NOTES)));
                allergyInfo.setText(cursor.getString(cursor
                        .getColumnIndex(Table.Client.ALLERGIES)));
                imageName = cursor.getString(cursor
                        .getColumnIndex(Table.Client.PHOTO_URL));
                try {
                    clientImage.setImageBitmap(BitmapFactory.decodeFile(Utils.PROFILE_THUMBNAIL_PATH + imageName));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            cursor.close();
        } catch (Exception e) {
            assert sqlDB != null;
            sqlDB.close();
            e.printStackTrace();
        } finally {
            assert sqlDB != null;
            sqlDB.close();
        }

    }


    private void setFieldsEditable(boolean enabled) {

        chooseFile.setVisibility(enabled ? View.VISIBLE : View.GONE);
        chooseFile.setEnabled(enabled);
        clientImage.setEnabled(enabled);
        firstName.setEnabled(enabled);
        lastName.setEnabled(enabled);
        email.setEnabled(enabled);
        phone.setEnabled(enabled);
        emergencyContact.setEnabled(enabled);
        emergencyContactNo.setEnabled(enabled);
        allergyInfo.setEnabled(enabled);
        medicalInfo.setEnabled(enabled);
        dateOfBirth.setEnabled(enabled);
        dateOfBirth.setTextColor(enabled ? Color.BLACK : Color.GRAY);
        genderFemale.setEnabled(enabled);
        genderMale.setEnabled(enabled);
        genderFemale.setTextColor(enabled ? Color.BLACK : Color.GRAY);
        genderMale.setTextColor(enabled ? Color.BLACK : Color.GRAY);


        firstName.setFocusable(enabled);
        lastName.setFocusable(enabled);
        email.setFocusable(enabled);
        phone.setFocusable(enabled);
        emergencyContact.setFocusable(enabled);
        emergencyContactNo.setFocusable(enabled);
        allergyInfo.setFocusable(enabled);
        medicalInfo.setFocusable(enabled);

        firstName.setClickable(enabled);
        lastName.setClickable(enabled);
        email.setClickable(enabled);
        phone.setClickable(enabled);
        emergencyContact.setClickable(enabled);
        emergencyContactNo.setClickable(enabled);
        allergyInfo.setClickable(enabled);
        medicalInfo.setClickable(enabled);

        firstName.setFocusableInTouchMode(enabled);
        lastName.setFocusableInTouchMode(enabled);
        email.setFocusableInTouchMode(enabled);
        phone.setFocusableInTouchMode(enabled);
        emergencyContact.setFocusableInTouchMode(enabled);
        emergencyContactNo.setFocusableInTouchMode(enabled);
        allergyInfo.setFocusableInTouchMode(enabled);
        medicalInfo.setFocusableInTouchMode(enabled);


    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        /*try {
            mCallbacks = (ClientDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.global, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.setCustomView(R.layout.cust_actionbar_client);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.getCustomView().findViewById(R.id.tbHistorical).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.tbHistorical) {
                    ToggleButton toggleButton = (ToggleButton) view;

                    if (toggleButton.isChecked()) {
                        setFieldsEditable(true);
                        Log.d("Toggle button clicked", " setFieldsEditable(true);");
                    } else {
//                        updateParseClientDetail();
                        long rowId = updateClientDetail(getArguments().getString(Utils.ARG_GROUP_OR_CLIENT_ID));
//                        if (rowId > 0) {
//                            HashMap<String, Object> client;
//                            client = DBOHelper.getClient(getArguments().getString(Utils.ARG_GROUP_OR_CLIENT_ID));
//                            String syncId = client.get(Table.SYNC_ID).toString();
//                            if (Utils.isNetworkAvailable(getActivity())) {
//                                updateClientOnServer(syncId, client);
//                            }
//                        }
                        setFieldsEditable(false);
                        Log.d("Toggle button clicked", " setFieldsEditable(false);");

                    }
                }
            }
        });
        actionBar.getCustomView().findViewById(R.id.tvTitle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });
    }
    private void updateParseClientDetail() {
        client.setFirstName(firstName.getText().toString());
        client.setLastName(lastName.getText().toString());
        client.setEmail(email.getText().toString());
        client.setContactPhone(phone.getText().toString());
        client.setEmergencyContact(emergencyContact.getText().toString());
        client.setEmergencyContactNumber(emergencyContactNo.getText().toString());
        client.setDateOfBirth(convertStringToDate(Utils.formatConversionUTC(dateOfBirth.getText().toString())));
        client.setGender(genderMale.isChecked());
        client.setMedicalInformation(medicalInfo.getText().toString());
        client.setAllergyInformation(allergyInfo.getText().toString());
        client.setTrainer(Trainer.getCurrent());
        if (selectBitmap!=null){
            client.setImageName(imageName);
            File file = new File(Utils.PROFILE_THUMBNAIL_PATH + imageName);
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                byte data[] = new byte[(int) file.length()];
                fis.read(data);
                String  encondedImage = android.util.Base64.encodeToString(data, Base64.DEFAULT);

                client.setImageFile(encondedImage);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            client.save();
        }catch (com.parse.ParseException e){
        }

        try {
            client.pin();
        }catch (com.parse.ParseException e){
        }

    }
    private long updateClientDetail(String client_id) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Table.Client.FIRST_NAME, firstName.getText().toString());
        contentValues.put(Table.Client.LAST_NAME, lastName.getText().toString());
        contentValues.put(Table.Client.EMAIL, email.getText().toString());
        contentValues.put(Table.Client.CONTACT_NO, phone.getText().toString());
        contentValues.put(Table.Client.EMERGENCY_CONTACT_ADDRESS, emergencyContact.getText().toString());
        contentValues.put(Table.Client.EMERGENCY_CONTACT_NUMBER, emergencyContactNo.getText().toString());
        contentValues.put(Table.Client.DOB, Utils.formatConversionUTC(dateOfBirth.getText().toString()));
        contentValues.put(Table.Client.GENDER, genderMale.isChecked() ? Utils.MALE : Utils.FEMALE);
        contentValues.put(Table.Client.MEDICAL_NOTES, medicalInfo.getText().toString());
        contentValues.put(Table.Client.ALLERGIES, allergyInfo.getText().toString());
        contentValues.put(Table.Client.PHOTO_URL, imageName);
        return DBOHelper.update(Table.Client.TABLE_NAME, contentValues, client_id);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bChooseFile:
            case R.id.ivClient:
                showImageUploadOption();
                break;
            case R.id.bDOB:
                Utils.datePickerDialog(getActivity(), dateOfBirth);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Utils.ACTION_CAPTURE_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    Utils.saveImage(Utils.LOCAL_RESOURCE_PATH + imageName, imageName);
                    selectBitmap=BitmapFactory.decodeFile(
                            Utils.PROFILE_THUMBNAIL_PATH + imageName);
                    clientImage.setImageBitmap(selectBitmap);
                }

                break;
            case Utils.ACTION_PICK_IMAGE:

                if (resultCode == Activity.RESULT_OK) {
                    Uri imageUri = data.getData();
                    try {
                        Utils.saveImage(Utils.getRealPathFromURI(getActivity(), imageUri), imageName);
                        selectBitmap=BitmapFactory.decodeFile(
                                Utils.PROFILE_THUMBNAIL_PATH + imageName);
                        clientImage.setImageBitmap(selectBitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    public void showImageUploadOption() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                getActivity());
        builderSingle.setTitle("Select Image");
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.select_dialog_item);
        arrayAdapter.add("Choose From Gallery");
        arrayAdapter.add("Capture From Camera");
        builderSingle.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }
        );
        builderSingle.setAdapter(arrayAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        switch (which) {
                            case 0:
                                startActivityForResult(Utils.pickImage(), Utils.ACTION_PICK_IMAGE);
                                break;
                            case 1:
                                startActivityForResult(Utils.captureImage(imageName, getActivity()), Utils.ACTION_CAPTURE_IMAGE);
                                break;
                        }
                    }
                }
        );
        builderSingle.show();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            setHasOptionsMenu(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateClientOnServer(final String syncId, final HashMap<String, Object> o) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Synchronise.Client syncClient = new Synchronise.Client(getActivity(),
                        Utils.getLastSyncTime(getActivity()));
                syncClient.propagateDeviceObjectToServer(Synchronise.getServerObjectBySyncId(syncId,
                        syncClient.getAllServerClients()), o);

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Log.d("Update Client", "Update update on the server");
            }
        }.execute();

    }



}
