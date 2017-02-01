package com.fitforbusiness.framework;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sanjeet on 20-Jun-14.
 */
public class ContactProvider {
    Context context;

    public ContactProvider(Context context) {
        this.context = context;
    }

    public ArrayList<HashMap<String, Object>> getContacts() {

        ArrayList<HashMap<String, Object>> contacts = new ArrayList<HashMap<String, Object>>();
        final String[] projection = new String[]{ContactsContract.RawContacts.CONTACT_ID,
                ContactsContract.RawContacts.DELETED, ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY};

        @SuppressWarnings("deprecation")
        final Cursor rawContacts = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, projection, null, null, ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY);

        final int contactIdColumnIndex = rawContacts.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID);
        final int deletedColumnIndex = rawContacts.getColumnIndex(ContactsContract.RawContacts.DELETED);
        final int displayNameColumnIndex = rawContacts.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY);
        if (rawContacts.moveToFirst()) {
            while (!rawContacts.isAfterLast()) {
                final int contactId = rawContacts.getInt(contactIdColumnIndex);
                final boolean deleted = (rawContacts.getInt(deletedColumnIndex) == 1);
                String displayName = rawContacts.getString(displayNameColumnIndex);
                if (!deleted) {
                    HashMap<String, Object> contactInfo = new HashMap<String, Object>() {
                        {
                            put("contactId", "");
                            put("name", "");
                           /* put("email", "");
                            put("address", "");
                            put("photo", "");
                            put("phone", "");*/
                        }
                    };
                    contactInfo.put("contactId", "" + contactId);
                    contactInfo.put("name", displayName);
                   contactInfo.put("photo", getPhoto(contactId) != null ? "a" : "");
                    // contactInfo.put("photo", getPhoto(contactId) != null ? getPhoto(contactId) : "");
                    /*contactInfo.put("email", getEmail(contactId));
                    contactInfo.put("photo", getPhoto(contactId) != null ? getPhoto(contactId) : "");
                    contactInfo.put("address", getAddress(contactId));
                    contactInfo.put("phone", getPhoneNumber(contactId));
                    contactInfo.put("isChecked", "false");*/
                    contacts.add(contactInfo);
                }
                rawContacts.moveToNext();
            }
        }

        rawContacts.close();

        return contacts;
    }

    public ArrayList<HashMap<String, Object>> getContact(int contactId) {
        ArrayList<HashMap<String, Object>> contacts = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> contactInfo = new HashMap<String, Object>() {
            {
                put("contactId", "");
                put("name", "");
                put("email", "");
                put("address", "");
                put("photo", "");
                put("phone", "");
                put("birthDay", "");
                put("structuredName", new String[]{"", "", ""});
            }
        };
        contactInfo.put("contactId", "" + contactId);
        contactInfo.put("name", getName(contactId));
        contactInfo.put("email", getEmail(contactId));
        contactInfo.put("photo", getPhoto(contactId) != null ? getPhoto(contactId) : "");
        contactInfo.put("address", getAddress(contactId));
        contactInfo.put("phone", getPhoneNumber(contactId));
        contactInfo.put("birthDay", getBirthDay(contactId));
        contactInfo.put("structuredName", getStructuredName(contactId));
        contacts.add(contactInfo);
        return contacts;
    }

    private String getName(int contactId) {
        String name = "";
        final String[] projection = new String[]{ContactsContract.Contacts.DISPLAY_NAME};

        final Cursor contact = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                projection, ContactsContract.Contacts._ID + "=?", new String[]{String.valueOf(contactId)}, null);

        if (contact.moveToFirst()) {
            name = contact.getString(contact.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            contact.close();
        }
        contact.close();
        return name;

    }

    private String getEmail(int contactId) {
        String emailStr = "";
        final String[] projection = new String[]{ContactsContract.CommonDataKinds.Email.DATA, // use
                // Email.ADDRESS
                // for API-Level
                // 11+
                ContactsContract.CommonDataKinds.Email.TYPE};

        final Cursor email = context.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                projection, ContactsContract.Data.CONTACT_ID + "=?", new String[]{String.valueOf(contactId)},
                null);

        if (email.moveToFirst()) {
            final int contactEmailColumnIndex = email.getColumnIndex(ContactsContract.
                    CommonDataKinds.Email.DATA);

            emailStr = email.getString(contactEmailColumnIndex);
           /* while (!email.isAfterLast()) {
                emailStr = emailStr + email.getString(contactEmailColumnIndex) + ";";
                email.moveToNext();
            }*/
        }
        email.close();
        return emailStr;

    }

    private String getPhotoId(int contactId) {
        String photoId = null;
        final String[] projection = new String[]{ContactsContract.Contacts.PHOTO_ID};

        final Cursor contact = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                projection, ContactsContract.Contacts._ID + "=?", new String[]{String.valueOf(contactId)},
                null);

        if (contact.moveToFirst()) {
            photoId = contact.getString(contact.getColumnIndex(ContactsContract.
                    Contacts.PHOTO_ID));
        }
        contact.close();

        return photoId;
    }

    public Bitmap getPhoto(int contactId) {
        Bitmap photo = null;
        final String[] projection = new String[]{ContactsContract.Contacts.PHOTO_ID};

        final Cursor contact = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                projection, ContactsContract.Contacts._ID + "=?", new String[]{String.valueOf(contactId)},
                null);

        if (contact.moveToFirst()) {
            final String photoId = contact.getString(contact.getColumnIndex(ContactsContract.
                    Contacts.PHOTO_ID));
            if (photoId != null) {
                photo = getBitmap(photoId);
            } else {
                photo = null;
            }
        }
        contact.close();

        return photo;
    }

    public Bitmap getBitmap(String photoId) {
        final Cursor photo = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Photo.PHOTO},
                ContactsContract.Contacts.Data._ID + "=?", new String[]{photoId}, null);

        final Bitmap photoBitmap;
        if (photo.moveToFirst()) {
            byte[] photoBlob = photo.getBlob(photo.getColumnIndex(
                    ContactsContract.CommonDataKinds.Photo.PHOTO));
            photoBitmap = BitmapFactory.decodeByteArray(photoBlob, 0, photoBlob.length);
        } else {
            photoBitmap = null;
        }
        photo.close();
        return photoBitmap;
    }

    private String getAddress(int contactId) {
        String postalData = "";
        String addrWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] addrWhereParams = new String[]{String.valueOf(contactId),
                ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE};

        Cursor addrCur = context.getContentResolver().query((ContactsContract.Data.CONTENT_URI), null, addrWhere, addrWhereParams, null);

        if (addrCur.moveToFirst()) {
            postalData = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS));
        }
        addrCur.close();
        return postalData;
    }

    private String getPhoneNumber(int contactId) {

        String phoneNumber = "";
        final String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.TYPE,};
        final Cursor phone = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, ContactsContract.Data.CONTACT_ID + "=?", new String[]{String.valueOf(contactId)}, null);

        if (phone.moveToFirst()) {
            final int contactNumberColumnIndex = phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA);


            phoneNumber = phone.getString(contactNumberColumnIndex);
            /*while (!phone.isAfterLast()) {
                phoneNumber = phoneNumber + phone.getString(contactNumberColumnIndex) + ";";
                phone.moveToNext();
            }*/

        }
        phone.close();
        return phoneNumber;
    }

    private String getBirthDay(int contactId) {

        String birthDay = "";
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Event.START_DATE,
        };

        String where = ContactsContract.Data.CONTACT_ID + "= ? AND "
                + ContactsContract.Data.MIMETYPE + " = ?   AND "
                + ContactsContract.CommonDataKinds.Event.TYPE + " = "
                + ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY;
        String[] selectionArgs = new String[]{
                String.valueOf(contactId), ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE};
        final Cursor cursor = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                projection, where, selectionArgs, null);

        if (cursor.moveToFirst()) {
            int contactBirthDayColumnIndex = cursor.getColumnIndex(ContactsContract.
                    CommonDataKinds.Event.START_DATE);
            birthDay = cursor.getString(contactBirthDayColumnIndex);
            Log.d("Birth Day is", birthDay);
        }
        cursor.close();
        return birthDay;

           /*Uri uri = ContactsContract.Data.CONTENT_URI;

           String[] projection = new String[] {
                   ContactsContract.Contacts.DISPLAY_NAME,
                   ContactsContract.CommonDataKinds.Event.CONTACT_ID,
                   ContactsContract.CommonDataKinds.Event.START_DATE
           };

           String where =
                   ContactsContract.Data.MIMETYPE + "= ? AND " +
                           ContactsContract.CommonDataKinds.Event.TYPE + "=" +
                           ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY;
           String[] selectionArgs = new String[] {
                   ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE
           };
           String sortOrder = null;
           return


// iterate through all Contact's Birthdays and print in log
       Cursor cursor = getContactsBirthdays();
       int bDayColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE);
       while (cursor.moveToNext()) {
           String bDay = cursor.getString(bDayColumn);
           Log.d(TAG, "Birthday: " + bDay);
       }*/
    }

    private Map<String, String> getStructuredName(int contactId) {

        String given = "";
        String family = "";
        String middle = "";
        String whereName = ContactsContract.Data.MIMETYPE + " = ? AND " + ContactsContract.Data.CONTACT_ID + " = ? ";
        String[] whereNameParams = new String[]{ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE, String.valueOf(contactId)};
        try {
            Cursor nameCur = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, whereName, whereNameParams, ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME);
            if (nameCur.moveToNext()) {

                given = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
                family = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
                middle = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME));

            }
            nameCur.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<String, String> map = new HashMap<String, String>();
        map.put("given", given != null ? given : "");
        map.put("family", family != null ? family : "");
        map.put("middle", middle != null ? family : "");

        return map;
    }
}
