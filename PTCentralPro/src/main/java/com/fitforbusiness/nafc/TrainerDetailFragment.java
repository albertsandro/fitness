package com.fitforbusiness.nafc;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.fitforbusiness.Parse.Models.Trainer;
import com.fitforbusiness.Parse.Models.TrainerBusinessData;
import com.fitforbusiness.database.DBOHelper;
import com.fitforbusiness.database.DatabaseHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.framework.FFBFragment;
import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.webservice.Synchronise;
import com.parse.GetCallback;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Sanjeet on 5/27/14.
 */
public class TrainerDetailFragment extends FFBFragment implements View.OnClickListener {


    private static final String ARG_SECTION_NUMBER = "section_number";
    private EditText firstName, lastName, email, phone, emergencyContact, emergencyContactNo;
    private Button dateOfBirth;
    private RadioButton genderMale;
    private EditText companyName, companyId, taxId, insuranceId, insuranceProvider, ptLicenseNo;
    private Button ptRenewalDate, firstAddRenewalDate, cprCertRenewalDate, aedRenewalDate;
    private EditText website, experience, facebookId, twitterId;
    private ImageView trainerImage;
    private Button insuranceRenewalDate;
    private Button chooseFile;
    private String imageName = "trainer.JPG";
    private ActionBar actionBar;
    private RadioButton genderFemale;

    public TrainerDetailFragment() {

    }

    public static TrainerDetailFragment newInstance(int sectionNumber) {
        TrainerDetailFragment fragment = new TrainerDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_trainer_details, container, false);
        setReferences(rootView);

        trainerImage.setOnClickListener(this);
        dateOfBirth.setOnClickListener(this);
        insuranceRenewalDate.setOnClickListener(this);
        ptRenewalDate.setOnClickListener(this);
        firstAddRenewalDate.setOnClickListener(this);
        cprCertRenewalDate.setOnClickListener(this);
        aedRenewalDate.setOnClickListener(this);
        chooseFile.setOnClickListener(this);

//        loadData(Utils.getTrainerId(getActivity()));
        loadDataFromParse();
        setFieldsEditable(false);
        try {
            loadTrainerImage();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rootView;
    }

    private void loadTrainerImage() {
        try {
            trainerImage.setImageBitmap(BitmapFactory.decodeFile(Utils.PROFILE_THUMBNAIL_PATH + imageName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setReferences(View rootView) {

        trainerImage = (ImageView) rootView.findViewById(R.id.ivTrainer);
        chooseFile = (Button) rootView.findViewById(R.id.bChooseFile);
        chooseFile.setVisibility(View.GONE);
        firstName = (EditText) rootView.findViewById(R.id.etFirstName);
        lastName = (EditText) rootView.findViewById(R.id.etLastName);
        email = (EditText) rootView.findViewById(R.id.etEmailID);
        phone = (EditText) rootView.findViewById(R.id.etPhoneNo);
        emergencyContact = (EditText) rootView.findViewById(R.id.etEmergencyContactAddress);
        emergencyContactNo = (EditText) rootView.findViewById(R.id.etEmergencyContactNo);
        dateOfBirth = (Button) rootView.findViewById(R.id.bDOB);


        genderMale = (RadioButton) rootView.findViewById(R.id.radGenderMale);
        genderFemale = (RadioButton) rootView.findViewById(R.id.radGenderFemale);

        companyName = (EditText) rootView.findViewById(R.id.etCompanyName);
        companyId = (EditText) rootView.findViewById(R.id.etCompanyID);
        taxId = (EditText) rootView.findViewById(R.id.etTaxID);
        insuranceId = (EditText) rootView.findViewById(R.id.etInsuranceNumber);
        insuranceRenewalDate = (Button) rootView.findViewById(R.id.bInsuranceDate);
        insuranceProvider = (EditText) rootView.findViewById(R.id.etInsuranceProvider);


        ptLicenseNo = (EditText) rootView.findViewById(R.id.etPTLicenseNo);
        ptRenewalDate = (Button) rootView.findViewById(R.id.bPTRenewalDate);
        firstAddRenewalDate = (Button) rootView.findViewById(R.id.bFirstAidCertRenewalDate);
        cprCertRenewalDate = (Button) rootView.findViewById(R.id.bCPRCertRenewalDate);
        aedRenewalDate = (Button) rootView.findViewById(R.id.bAEDCertRenewalDate);

        website = (EditText) rootView.findViewById(R.id.etWebsite);
        experience = (EditText) rootView.findViewById(R.id.etExperience);
        facebookId = (EditText) rootView.findViewById(R.id.etFacebookID);

        twitterId = (EditText) rootView.findViewById(R.id.etTwitterID);


    }

    void showAlert(final Button mButton) {
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

        DatePickerDialog dateDlg = new DatePickerDialog(getActivity(),
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bDOB:
                showAlert(dateOfBirth);
                break;
            case R.id.bInsuranceDate:
                showAlert(insuranceRenewalDate);
                break;
            case R.id.bPTRenewalDate:
                showAlert(ptRenewalDate);
                break;
            case R.id.bFirstAidCertRenewalDate:
                showAlert(firstAddRenewalDate);
                break;
            case R.id.bCPRCertRenewalDate:
                showAlert(cprCertRenewalDate);
                break;
            case R.id.bAEDCertRenewalDate:
                showAlert(aedRenewalDate);
                break;
            case R.id.bChooseFile:

            case R.id.ivTrainer:
                showImageUploadOption();
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
                                startActivityForResult(Utils.captureImage("trainer.jpg",
                                        getActivity()), Utils.ACTION_CAPTURE_IMAGE);
                                break;
                        }
                    }
                }
        );
        builderSingle.show();
    }

//    private void loadData(String trainer_id) {
//
//        SQLiteDatabase sqlDB = DatabaseHelper.instance().getReadableDatabase();
//
//        try {
//            assert sqlDB != null;
//            Cursor cursor = sqlDB
//                    .rawQuery("select  * "
//                            + " from " + Table.TrainerProfileDetails.TABLE_NAME
//                            + " where trainer_id = " + trainer_id, null);
//            while (cursor.moveToNext()) {
//                firstName.setText(cursor.getString(cursor
//                        .getColumnIndex(Table.TrainerProfileDetails.FIRST_NAME)));
//                lastName.setText(cursor.getString(cursor
//                        .getColumnIndex(Table.TrainerProfileDetails.LAST_NAME)));
//                email.setText(cursor.getString(cursor
//                        .getColumnIndex(Table.TrainerProfileDetails.EMAIL_ID)));
//                phone.setText(cursor.getString(cursor
//                        .getColumnIndex(Table.TrainerProfileDetails.PHONE_NO)));
//                emergencyContact.setText(cursor.getString(cursor
//                        .getColumnIndex(Table.TrainerProfileDetails.EMERGENCY_CONTACT)));
//
//                emergencyContactNo.setText(cursor.getString(cursor
//                        .getColumnIndex(Table.TrainerProfileDetails.EMERGENCY_CONTACT_NO)));
//                dateOfBirth.setText(Utils.formatConversionSQLite(cursor.getString(cursor
//                        .getColumnIndex(Table.TrainerProfileDetails.DATE_OF_BIRTH))));
//
//                genderMale.setChecked(Integer.parseInt(cursor.getString(cursor
//                        .getColumnIndex(Table.TrainerProfileDetails.GENDER))) == Utils.MALE);
//
//                companyName.setText(cursor.getString(cursor
//                        .getColumnIndex(Table.TrainerProfileDetails.COMPANY_NAME)));
//                companyId.setText(cursor.getString(cursor
//                        .getColumnIndex(Table.TrainerProfileDetails.COMPANY_ID)));
//
//                taxId.setText(cursor.getString(cursor
//                        .getColumnIndex(Table.TrainerProfileDetails.TAX_ID)));
//
//                insuranceId.setText(cursor.getString(cursor
//                        .getColumnIndex(Table.TrainerProfileDetails.INSURANCE_MEMBERSHIP_NO)));
//                insuranceRenewalDate.setText(Utils.formatConversionSQLite(cursor.getString(cursor
//                        .getColumnIndex(Table.TrainerProfileDetails.INSURANCE_EXPIRY_DATE))));
//
//                insuranceProvider.setText(cursor.getString(cursor
//                        .getColumnIndex(Table.TrainerProfileDetails.INSURANCE_PROVIDER)));
//
//                ptLicenseNo.setText(cursor.getString(cursor
//                        .getColumnIndex(Table.TrainerProfileDetails.PT_LICENSE_NUMBER)));
//                ptRenewalDate.setText(Utils.formatConversionSQLite(cursor.getString(cursor
//                        .getColumnIndex(Table.TrainerProfileDetails.PT_LICENSE_RENEWAL_DATE))));
//
//                firstAddRenewalDate.setText(Utils.formatConversionSQLite(cursor.getString(cursor
//                        .getColumnIndex(Table.TrainerProfileDetails.FIRST_AID_CERT_RENEWAL_DATE))));
//
//
//                cprCertRenewalDate.setText(Utils.formatConversionSQLite(cursor.getString(cursor
//                        .getColumnIndex(Table.TrainerProfileDetails.CPR_CERT_RENEWAL_DATE))));
//                aedRenewalDate.setText(Utils.formatConversionSQLite(cursor.getString(cursor
//                        .getColumnIndex(Table.TrainerProfileDetails.AED_CERT_RENEWAL_DATE))));
//
//                website.setText(cursor.getString(cursor
//                        .getColumnIndex(Table.TrainerProfileDetails.WEBSITE)));
//                experience.setText(cursor.getString(cursor
//                        .getColumnIndex(Table.TrainerProfileDetails.EXPERIENCE)));
//                facebookId.setText(cursor.getString(cursor
//                        .getColumnIndex(Table.TrainerProfileDetails.FACEBOOK_ID)));
//                twitterId.setText(cursor.getString(cursor
//                        .getColumnIndex(Table.TrainerProfileDetails.TWITTER_ID)));
//            }
//            cursor.close();
//        } catch (Exception e) {
//            sqlDB.close();
//            Log.d("Exception in catch is ", e.toString());
//            e.printStackTrace();
//        } finally {
//            assert sqlDB != null;
//            sqlDB.close();
//
//        }
//    }

    private void loadDataFromParse() {
        Trainer.getCurrentUser().fetchIfNeededInBackground(new GetCallback<Trainer>() {

            @Override
            public void done(Trainer trainer, com.parse.ParseException e) {
                Log.d("loadingTrainer", (e==null) + " - " + (trainer != null));
                if (e == null && trainer != null) {
                    firstName.setText(trainer.getFirstName());
                    lastName.setText(trainer.getLastName());
                    email.setText(trainer.getEmail());
                    phone.setText(trainer.getPhone());
                    emergencyContact.setText(trainer.getEmergencyContact());

                    emergencyContactNo.setText(trainer.getEmergencyContactPhone());
                    dateOfBirth.setText(Utils.formatConversionDateOnly2(
                            trainer.getDateOfBirth()));

                    genderMale.setChecked(trainer.getGender());
                    genderFemale.setChecked(!trainer.getGender());

                    TrainerBusinessData data;
                    if (trainer.getTrainerBusinessData() != null) {
                        data = trainer.getTrainerBusinessData();
                        data.fetchIfNeededInBackground(new GetCallback<TrainerBusinessData>() {
                            @Override
                            public void done(TrainerBusinessData data, com.parse.ParseException e) {
                                if (e == null && data != null) {
                                    companyName.setText(data.getCompanyName());
                                    companyId.setText(data.getCompanyId());

                                    taxId.setText(data.getCompanyTaxId());

                                    insuranceId.setText(data.getInsuranceNumber());

                                    if (data.getInsuranceRenewalDate() != null)
                                        insuranceRenewalDate.setText(
                                            Utils.formatConversionDateOnly2(
                                                    data.getInsuranceRenewalDate()));

                                    insuranceProvider.setText(data.getInsuranceProvider());

                                    ptLicenseNo.setText(data.getPtLicenseNumber());

                                    if (data.getPtRenewalDate() != null)
                                    ptRenewalDate.setText(Utils.formatConversionDateOnly2(
                                            data.getPtRenewalDate()));

                                    if (data.getFirstAidRenewalDate() != null)
                                    firstAddRenewalDate.setText(Utils.formatConversionDateOnly2(
                                            data.getFirstAidRenewalDate()));

                                    if (data.getCprRenewalDate() != null)
                                    cprCertRenewalDate.setText(Utils.formatConversionDateOnly2(
                                            data.getCprRenewalDate()));

                                    if (data.getAedRenewalDate() != null)
                                    aedRenewalDate.setText(Utils.formatConversionDateOnly2(
                                            data.getAedRenewalDate()));

                                    website.setText(data.getWebsite());
                                    experience.setText(data.getExperience() + "");
                                    facebookId.setText(data.getFacebook());
                                    twitterId.setText(data.getTwitter());
                                }
                            }
                        });
                    }
                }
            }
        });

    }


//    private long updateData(String trainer_id) {
//
//        ContentValues values = new ContentValues();
//
//        values.put(Table.TrainerProfileDetails.TRAINER_ID, trainer_id);
//        values.put(Table.TrainerProfileDetails.FIRST_NAME, firstName.getText().toString());
//        values.put(Table.TrainerProfileDetails.LAST_NAME, lastName.getText().toString());
//        values.put(Table.TrainerProfileDetails.EMAIL_ID, email.getText().toString());
//        values.put(Table.TrainerProfileDetails.PHONE_NO, phone.getText().toString());
//        values.put(Table.TrainerProfileDetails.EMERGENCY_CONTACT, emergencyContact.getText().toString());
//        values.put(Table.TrainerProfileDetails.EMERGENCY_CONTACT_NO, emergencyContactNo.getText().toString());
//        values.put(Table.TrainerProfileDetails.DATE_OF_BIRTH, Utils.formatConversionLocale(
//                dateOfBirth.getText().toString()));
//
//        values.put(Table.TrainerProfileDetails.GENDER, genderMale.isChecked() ? Utils.MALE : Utils.FEMALE);
//        values.put(Table.TrainerProfileDetails.COMPANY_NAME, companyName.getText().toString());
//        values.put(Table.TrainerProfileDetails.COMPANY_ID, companyId.getText().toString());
//        values.put(Table.TrainerProfileDetails.TAX_ID, taxId.getText().toString());
//
//        values.put(Table.TrainerProfileDetails.WEBSITE, website.getText().toString());
//        values.put(Table.TrainerProfileDetails.EXPERIENCE, experience.getText().toString());
//
//        values.put(Table.TrainerProfileDetails.TWITTER_ID, twitterId.getText().toString());
//        values.put(Table.TrainerProfileDetails.FACEBOOK_ID, facebookId.getText().toString());
//
//        values.put(Table.TrainerProfileDetails.INSURANCE_MEMBERSHIP_NO, insuranceId.getText().toString());
//        values.put(Table.TrainerProfileDetails.INSURANCE_EXPIRY_DATE, Utils.formatConversionLocale(
//                insuranceRenewalDate.getText().toString()));
//        values.put(Table.TrainerProfileDetails.INSURANCE_PROVIDER, insuranceProvider.getText().toString());
//
//        values.put(Table.TrainerProfileDetails.PT_LICENSE_NUMBER, ptLicenseNo.getText().toString());
//        values.put(Table.TrainerProfileDetails.PT_LICENSE_RENEWAL_DATE, Utils.formatConversionLocale(
//                ptRenewalDate.getText().toString()));
//        values.put(Table.TrainerProfileDetails.FIRST_AID_CERT_RENEWAL_DATE, Utils.formatConversionLocale(
//                firstAddRenewalDate.getText().toString()));
//
//        values.put(Table.TrainerProfileDetails.CPR_CERT_RENEWAL_DATE, Utils.formatConversionLocale(
//                cprCertRenewalDate.getText().toString()));
//        values.put(Table.TrainerProfileDetails.AED_CERT_RENEWAL_DATE, Utils.formatConversionLocale(
//                aedRenewalDate.getText().toString()));
//        long id = DBOHelper.update(getActivity(), Table.TrainerProfileDetails.TABLE_NAME, values, trainer_id);
//        Log.d("updated row is", id + "");
//        return id;
//
//
//    }

    private void updateDataOnParse() {

        Trainer trainer = (Trainer) Trainer.getCurrentUser();

        trainer.setFirstName(firstName.getText().toString());
        trainer.setLastName(lastName.getText().toString());
        trainer.setEmail(email.getText().toString());
        trainer.setPhone(phone.getText().toString());
        trainer.setEmergencyContact(emergencyContact.getText().toString());
        trainer.setEmergencyContactPhone(emergencyContactNo.getText().toString());
        trainer.setDateOfBirth(Utils.dateConversionFromString2(
                dateOfBirth.getText().toString()));

        trainer.setGender(genderMale.isChecked());
        TrainerBusinessData data;
        if (trainer.getTrainerBusinessData() != null)
            data = trainer.getTrainerBusinessData();
        else
            data = new TrainerBusinessData();
        data.setCompanyName(companyName.getText().toString());
        data.setCompanyId(companyId.getText().toString());
        data.setCompanyTaxId(taxId.getText().toString());

        data.setWebsite(website.getText().toString());

        try {
            data.setExperience(Integer.parseInt(experience.getText().toString()));
        }catch (Exception ignored) {}

        data.setTwitter(twitterId.getText().toString());
        data.setFacebook(facebookId.getText().toString());

        data.setInsuranceNumber(insuranceId.getText().toString());

        Date d = Utils.dateConversionFromString2(
                insuranceRenewalDate.getText().toString());
        if (d != null)
            data.setInsuranceRenewalDate(d);

        data.setInsuranceProvider(insuranceProvider.getText().toString());
        data.setPtLicenseNumber(ptLicenseNo.getText().toString());

        d = Utils.dateConversionFromString2(
                ptRenewalDate.getText().toString());
        if (d != null)
            data.setPtRenewalDate(d);

        d = Utils.dateConversionFromString2(
                firstAddRenewalDate.getText().toString());
        if (d != null)
            data.setFirstAidRenewalDate(d);

        d = Utils.dateConversionFromString2(
                cprCertRenewalDate.getText().toString());
        if (d != null)
        data.setCprRenewalDate(d);

        d = Utils.dateConversionFromString2(
                aedRenewalDate.getText().toString());
        if (d != null)
        data.setAedRenewalDate(d);

        trainer.saveEventually();
        try {
            data.pin();
            trainer.setTrainerBusinessData(data);
            trainer.saveEventually();
        } catch (com.parse.ParseException e) {
            e.printStackTrace();
        }
        data.saveEventually();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Utils.ACTION_CAPTURE_IMAGE:

                if (resultCode == Activity.RESULT_OK) {
                    Utils.saveImage(Utils.LOCAL_RESOURCE_PATH + imageName, imageName);
                    trainerImage.setImageBitmap(BitmapFactory.decodeFile(
                            Utils.PROFILE_THUMBNAIL_PATH + imageName));
                }
                break;
            case Utils.ACTION_PICK_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri imageUri = data.getData();
                    Log.d("", "imageUri = " + Utils.getRealPathFromURI(getActivity(), data.getData()));
                    if (imageUri != null)
                        try {
                            Utils.saveImage(Utils.getRealPathFromURI(getActivity(), imageUri), imageName);
                            trainerImage.setImageBitmap(BitmapFactory.decodeFile(
                                    Utils.PROFILE_THUMBNAIL_PATH + imageName));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                }
                break;
        }

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
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
        ((TextView) actionBar.getCustomView().findViewById(R.id.tvTitle)).setText("Profile");
        actionBar.getCustomView().findViewById(R.id.tbHistorical).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.tbHistorical) {
                    ToggleButton toggleButton = (ToggleButton) view;

                    if (toggleButton.isChecked()) {
                        setFieldsEditable(true);
                        Log.d("Toggle button clicked", " setFieldsEditable(true);");
                    } else {

                        setFieldsEditable(false);
                        updateDataOnParse();
                        Log.d("Toggle button clicked", " setFieldsEditable(false);");
                    }
                }
            }
        });
        actionBar.getCustomView().findViewById(R.id.tvTitle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

    }

//    private void updateTrainerDetails() {
//        new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void... params) {
//                new Synchronise.Trainer(getActivity(), Utils.getLastSyncTime(getActivity()))
//                        .propagateDeviceObjectToServer();
//                return null;
//            }
//
//            @Override
//            protected void onPreExecute() {
//                super.onPreExecute();
//            }
//        }.execute();
//    }

    private void setFieldsEditable(boolean enabled) {
        chooseFile.setVisibility(enabled ? View.VISIBLE : View.GONE);
        chooseFile.setEnabled(enabled);
        trainerImage.setEnabled(enabled);
        firstName.setEnabled(enabled);
        lastName.setEnabled(enabled);
        email.setEnabled(enabled);
        phone.setEnabled(enabled);
        emergencyContact.setEnabled(enabled);
        emergencyContactNo.setEnabled(enabled);
        dateOfBirth.setEnabled(enabled);
        dateOfBirth.setTextColor(enabled ? Color.BLACK : Color.GRAY);
        genderFemale.setEnabled(enabled);
        genderMale.setEnabled(enabled);
        genderFemale.setTextColor(enabled ? Color.BLACK : Color.GRAY);
        genderMale.setTextColor(enabled ? Color.BLACK : Color.GRAY);

        companyName.setEnabled(enabled);
        companyId.setEnabled(enabled);
        taxId.setEnabled(enabled);
        insuranceId.setEnabled(enabled);
        insuranceRenewalDate.setEnabled(enabled);
        insuranceRenewalDate.setTextColor(enabled ? Color.BLACK : Color.GRAY);
        insuranceProvider.setEnabled(enabled);
        ptLicenseNo.setEnabled(enabled);
        ptRenewalDate.setEnabled(enabled);
        ptRenewalDate.setTextColor(enabled ? Color.BLACK : Color.GRAY);
        firstAddRenewalDate.setEnabled(enabled);
        firstAddRenewalDate.setTextColor(enabled ? Color.BLACK : Color.GRAY);
        cprCertRenewalDate.setEnabled(enabled);
        cprCertRenewalDate.setTextColor(enabled ? Color.BLACK : Color.GRAY);
        aedRenewalDate.setEnabled(enabled);
        aedRenewalDate.setTextColor(enabled ? Color.BLACK : Color.GRAY);
        website.setEnabled(enabled);
        experience.setEnabled(enabled);
        facebookId.setEnabled(enabled);
        twitterId.setEnabled(enabled);


        firstName.setFocusable(enabled);
        lastName.setFocusable(enabled);
        email.setFocusable(enabled);
        phone.setFocusable(enabled);
        emergencyContact.setFocusable(enabled);
        emergencyContactNo.setFocusable(enabled);
        companyName.setFocusable(enabled);
        companyId.setFocusable(enabled);
        taxId.setFocusable(enabled);
        insuranceId.setFocusable(enabled);
        insuranceRenewalDate.setFocusable(enabled);
        insuranceProvider.setFocusable(enabled);
        ptLicenseNo.setFocusable(enabled);
        ptRenewalDate.setFocusable(enabled);
        firstAddRenewalDate.setFocusable(enabled);
        cprCertRenewalDate.setFocusable(enabled);
        aedRenewalDate.setFocusable(enabled);
        website.setFocusable(enabled);
        experience.setFocusable(enabled);
        facebookId.setFocusable(enabled);
        twitterId.setFocusable(enabled);


        firstName.setClickable(enabled);
        lastName.setClickable(enabled);
        email.setClickable(enabled);
        phone.setClickable(enabled);
        emergencyContact.setClickable(enabled);
        emergencyContactNo.setClickable(enabled);
        companyName.setClickable(enabled);
        companyId.setClickable(enabled);
        taxId.setClickable(enabled);
        insuranceId.setClickable(enabled);
        insuranceRenewalDate.setClickable(enabled);
        insuranceProvider.setClickable(enabled);
        ptLicenseNo.setClickable(enabled);
        ptRenewalDate.setClickable(enabled);
        firstAddRenewalDate.setClickable(enabled);
        cprCertRenewalDate.setClickable(enabled);
        aedRenewalDate.setClickable(enabled);
        website.setClickable(enabled);
        experience.setClickable(enabled);
        facebookId.setClickable(enabled);
        twitterId.setEnabled(enabled);


        firstName.setFocusableInTouchMode(enabled);
        lastName.setFocusableInTouchMode(enabled);
        email.setFocusableInTouchMode(enabled);
        phone.setFocusableInTouchMode(enabled);
        emergencyContact.setFocusableInTouchMode(enabled);
        emergencyContactNo.setFocusableInTouchMode(enabled);
        companyName.setFocusableInTouchMode(enabled);
        companyId.setFocusableInTouchMode(enabled);
        taxId.setFocusableInTouchMode(enabled);
        insuranceId.setFocusableInTouchMode(enabled);
        insuranceRenewalDate.setFocusableInTouchMode(enabled);
        insuranceProvider.setFocusableInTouchMode(enabled);
        ptLicenseNo.setFocusableInTouchMode(enabled);
        ptRenewalDate.setFocusableInTouchMode(enabled);
        firstAddRenewalDate.setFocusableInTouchMode(enabled);
        cprCertRenewalDate.setFocusableInTouchMode(enabled);
        aedRenewalDate.setFocusableInTouchMode(enabled);
        website.setFocusableInTouchMode(enabled);
        experience.setFocusableInTouchMode(enabled);
        facebookId.setFocusableInTouchMode(enabled);
        twitterId.setFocusableInTouchMode(enabled);


        firstName.setText(firstName.getText().toString().equalsIgnoreCase("null") ? "" : firstName.getText().toString());
        email.setText(email.getText().toString().equalsIgnoreCase("null") ? "" : email.getText().toString());
        phone.setText(phone.getText().toString().equalsIgnoreCase("null") ? "" : phone.getText().toString());
        emergencyContactNo.setText(emergencyContactNo.getText().toString().equalsIgnoreCase("null") ? "" : emergencyContactNo.getText().toString());
        emergencyContact.setText(emergencyContact.getText().toString().equalsIgnoreCase("null") ? "" : emergencyContact.getText().toString());
        dateOfBirth.setText(dateOfBirth.getText().toString().equalsIgnoreCase("null") ? "" : dateOfBirth.getText().toString());
        companyName.setText(companyName.getText().toString().equalsIgnoreCase("null") ? "" : companyName.getText().toString());
        companyId.setText(companyId.getText().toString().equalsIgnoreCase("null") ? "" : companyId.getText().toString());
        taxId.setText(taxId.getText().toString().equalsIgnoreCase("null") ? "" : taxId.getText().toString());
        insuranceId.setText(insuranceId.getText().toString().equalsIgnoreCase("null") ? "" : insuranceId.getText().toString());
        insuranceRenewalDate.setText(insuranceRenewalDate.getText().toString().equalsIgnoreCase("null") ? "" : insuranceRenewalDate.getText().toString());
        insuranceProvider.setText(insuranceProvider.getText().toString().equalsIgnoreCase("null") ? "" : insuranceProvider.getText().toString());
        ptLicenseNo.setText(ptLicenseNo.getText().toString().equalsIgnoreCase("null") ? "" : ptLicenseNo.getText().toString());
        ptRenewalDate.setText(ptRenewalDate.getText().toString().equalsIgnoreCase("null") ? "" : ptRenewalDate.getText().toString());
        firstAddRenewalDate.setText(firstAddRenewalDate.getText().toString().equalsIgnoreCase("null") ? "" : firstAddRenewalDate.getText().toString());
        cprCertRenewalDate.setText(cprCertRenewalDate.getText().toString().equalsIgnoreCase("null") ? "" : cprCertRenewalDate.getText().toString());
        aedRenewalDate.setText(aedRenewalDate.getText().toString().equalsIgnoreCase("null") ? "" : aedRenewalDate.getText().toString());
        website.setText(website.getText().toString().equalsIgnoreCase("null") ? "" : website.getText().toString());
        experience.setText(experience.getText().toString().equalsIgnoreCase("null") ? "" : experience.getText().toString());
        facebookId.setText(facebookId.getText().toString().equalsIgnoreCase("null") ? "" : facebookId.getText().toString());
        twitterId.setText(twitterId.getText().toString().equalsIgnoreCase("null") ? "" : twitterId.getText().toString());
    }
}