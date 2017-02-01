package com.fitforbusiness.nafc.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.appboy.Appboy;
import com.fitforbusiness.Parse.Models.Client;
import com.fitforbusiness.Parse.Models.Exercise;
import com.fitforbusiness.Parse.Models.Measurements;
import com.fitforbusiness.Parse.Models.Trainer;
import com.fitforbusiness.contact.ContactsListActivity;
import com.fitforbusiness.database.DBOHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.framework.ContactProvider;
import com.fitforbusiness.framework.FFBActivity;
import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.nafc.R;
import com.fitforbusiness.webservice.Synchronise;
import com.parse.Parse;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class AddClientActivity extends FFBActivity implements View.OnClickListener {

    private static final int PICK_CONTACT = 1010;
    EditText firstName, lastName, email, phone, emergencyContact,
            emergencyContactNo, medicalInfo, allergyInfo;
    Button dateOfBirth, chooseImage;
    RadioButton genderMale;
    RadioButton genderFemale;
    ImageView clientImage;
    String imageName;
    private Bitmap selectBitmap;
    private ParseFile parseFile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_client);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageName = new Date().getTime() + ".JPG";
        clientImage = (ImageView) findViewById(R.id.ivClient);
        chooseImage = (Button) findViewById(R.id.bChooseFile);
        firstName = (EditText) findViewById(R.id.etFirstName);
        lastName = (EditText) findViewById(R.id.etLastName);
        email = (EditText) findViewById(R.id.etEmailID);
        phone = (EditText) findViewById(R.id.etPhoneNo);
        emergencyContact = (EditText) findViewById(R.id.etEmergencyContactAddress);
        emergencyContactNo = (EditText) findViewById(R.id.etEmergencyContactNo);
        dateOfBirth = (Button) findViewById(R.id.bDOB);
        // dateOfBirth.setText(Utils.getToday());
        medicalInfo = (EditText) findViewById(R.id.etMedicalInfo);
        allergyInfo = (EditText) findViewById(R.id.etAllergyInfo);

        genderMale = (RadioButton) findViewById(R.id.radGenderMale);
        genderFemale = (RadioButton) findViewById(R.id.radGenderFemale);
        dateOfBirth.setOnClickListener(this);
        clientImage.setOnClickListener(this);
        chooseImage.setOnClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_client, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_contacts:
                Intent intent = new Intent(this, ContactsListActivity.class);
                startActivityForResult(intent, PICK_CONTACT);
                break;
            case R.id.bSave:
                if (Utils.validateFields(firstName) && Utils.validateFields(lastName) &&
                        Utils.validateFields(phone) && Utils.isValidEmail(email) &&
                        Utils.validateFields(dateOfBirth)) {
                    if (genderFemale.isChecked() || genderMale.isChecked()) {
                        saveParseClient();
                        //ServerHttpCommumicationPart
//                        long clientId = saveClient();                    //Client Save part.
//                        if (clientId > 0) {
//                            Appboy.getInstance(AddClientActivity.this).logCustomEvent(Utils.CUSTOM_EVENT_KEY_CREATE_CLIENT);
//                            Toast.makeText(this, "Client Added !", Toast.LENGTH_SHORT).show();
//                            createClientOnServer(DBOHelper.getClient(clientId + ""));
//                            finishThisActivity();
//                        } else {
//                            Toast.makeText(this, "Client not Added !", Toast.LENGTH_SHORT).show();
//                        }

                    } else {
                        genderFemale.setError("Required!");
                    }
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        switch (reqCode) {
            case PICK_CONTACT:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        setClientDetail(Integer.valueOf(data.getStringExtra("contactId")));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
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
                    Log.d("", "imageUri = " + Utils.getRealPathFromURI(this, data.getData()));
                    if (imageUri != null)
                        try {
                            Utils.saveImage(Utils.getRealPathFromURI(this, imageUri), imageName);
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

    private void setClientDetail(Integer contactId) {
        Map map = (Map<String, Object>) new ContactProvider(this).getContact(contactId).get(0);
        Log.d("Map is", map.toString());
        Map names = (Map<String, String>) map.get("structuredName");
        firstName.setText(names.get("given").toString());
        lastName.setText(names.get("family").toString());
        phone.setText(map.get("phone").toString());
        email.setText(map.get("email").toString());
        emergencyContact.setText(map.get("address").toString());
        dateOfBirth.setText(Utils.formatConversion(map.get("birthDay").toString()));
        Bitmap bitmap = (Bitmap) map.get("photo");
        Utils.saveImage(bitmap, imageName);
        clientImage.setImageBitmap(BitmapFactory.decodeFile(
                Utils.PROFILE_THUMBNAIL_PATH + imageName));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bDOB:
                datePickerDialog(dateOfBirth);
                break;
            case R.id.bChooseFile:
            case R.id.ivClient:
                showImageUploadOption();
                break;
            case R.id.bSave:
                if (Utils.validateFields(firstName) && Utils.validateFields(lastName) &&
                        Utils.validateFields(phone) && Utils.isValidEmail(email
                ) &&
                        Utils.validateFields(dateOfBirth)) {
                    if (saveClient() > 0) {
                        Toast.makeText(this, "Client Added !", Toast.LENGTH_SHORT).show();
                        finishThisActivity();
                    } else {
                        Toast.makeText(this, "Client not Added !", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.bCancel:
                finish();
                break;
        }
    }

    private void finishThisActivity() {
        Intent intent = new Intent();
        setResult(Utils.CLIENTS, intent);
        finish();
    }

    private long saveClient() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Table.Client.FIRST_NAME, firstName.getText().toString());
        contentValues.put(Table.Client.LAST_NAME, lastName.getText().toString());
        contentValues.put(Table.Client.EMAIL, email.getText().toString());
        contentValues.put(Table.Client.CONTACT_NO, phone.getText().toString());
        contentValues.put(Table.Client.EMERGENCY_CONTACT_ADDRESS, emergencyContact.getText().toString());
        contentValues.put(Table.Client.EMERGENCY_CONTACT_NUMBER, emergencyContactNo.getText().toString());
        contentValues.put(Table.Client.DOB, Utils.formatConversionUTC(dateOfBirth.getText().toString()));
        contentValues.put(Table.Client.GENDER, genderMale.isChecked() ? 1 : 0);
        contentValues.put(Table.Client.MEDICAL_NOTES, medicalInfo.getText().toString());
        contentValues.put(Table.Client.ALLERGIES, allergyInfo.getText().toString());
        contentValues.put(Table.Client.PHOTO_URL, imageName);
        contentValues.put(Table.SYNC_ID, UUID.randomUUID().toString());
        return DBOHelper.insert(this, Table.Client.TABLE_NAME, contentValues);
    }

    private void saveParseClient() {
       // ParseObject.registerSubclass(Client.class);
        final Client client = new Client();

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
        if (selectBitmap!=null) {
            client.setImageName(imageName);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            byte[] data;

            Bitmap bitmap = BitmapFactory.decodeFile(Utils.PROFILE_THUMBNAIL_PATH + imageName, options);
            ByteArrayOutputStream blob = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, blob);
            data = blob.toByteArray();

            Log.v("AddGroup","data1 length = "+data.length);

            while (data.length > 15000) {
                options.inSampleSize = options.inSampleSize * 2;
                bitmap = BitmapFactory.decodeFile(Utils.PROFILE_THUMBNAIL_PATH + imageName, options);
                blob = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, blob);
                data = blob.toByteArray();

                Log.v("AddGroup","data1 length = "+data.length);
            }

            Log.v("AddGroup","data1 length = "+data.length);
            parseFile = new ParseFile(imageName, data);
            parseFile.saveInBackground();

            String  encondedImage = android.util.Base64.encodeToString(data, Base64.DEFAULT);
            client.setImageFile(encondedImage);

        }

        client.pinInBackground(new SaveCallback() {
            @Override
            public void done(com.parse.ParseException e) {
                if (parseFile != null) {
                    client.setClientImage(parseFile);
                }
                client.saveInBackground();
                finish();
            }
        });
    }

    void datePickerDialog(final Button mButton) {
        String defaultDate = mButton.getText().toString();
        Calendar cal = Calendar.getInstance();
        try {

            Date d = new SimpleDateFormat("dd MMM yyyy").parse(defaultDate);
            cal.setTime(d);
            Log.d("Date is", d + "");
        } catch (ParseException e) {
            Log.d("Error in date is", e.toString());
            e.printStackTrace();
        }
        DatePickerDialog dateDlg = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        Calendar c = Calendar.getInstance();
                        c.set(year, monthOfYear, dayOfMonth);

                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
                        String formattedDate = sdf.format(c.getTime());
                        mButton.setText(formattedDate);
                    }
                }, cal
                .get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
                .get(Calendar.DAY_OF_MONTH)
        );
        dateDlg.show();
    }

    public void showImageUploadOption() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                this);
        builderSingle.setTitle("Select Image");
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
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
                                startActivityForResult(Utils.captureImage(imageName,
                                        AddClientActivity.this), Utils.ACTION_CAPTURE_IMAGE);
                                break;
                        }
                    }
                }
        );
        builderSingle.show();
    }

    private void createClientOnServer(final HashMap<String, Object> client) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                new Synchronise.Client(AddClientActivity.this,
                        Utils.getLastClientSyncTime(AddClientActivity.this)).createOnServer(client);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Log.d("Create Client", "Client created on the server");
            }
        }.execute();

    }

    private Date convertStringToDate(String date_str){
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Date convertedDate = new Date();
        try {
            convertedDate = dateFormat.parse(date_str);
        } catch (ParseException e) {
            // TODO Auto-generated catch block8
            e.printStackTrace();
        }

        return convertedDate;
    }
}
