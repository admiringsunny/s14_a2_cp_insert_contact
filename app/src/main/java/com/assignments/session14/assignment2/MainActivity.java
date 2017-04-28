package com.assignments.session14.assignment2;

import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.io.File;
import java.io.FileOutputStream;
/*
* MainActivity -> Display Contacts List
* click option menu '+' -> Intent to AddContactActivity
* AddContactActivity
*   -> Enter name and Mobile -> click button 'Add Contact'
*   ==> Toast 'Contact Added' and goto MainActivity
* MainActivity ->  Display Added Contact
* */
public class MainActivity extends AppCompatActivity {

    MatrixCursor matrixCursor;
    SimpleCursorAdapter adapter;

    ListView listContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
    }

    private void initializeViews() {
        // initialize matrixCursor -> make it ready -> to store contacts from CP (Content Provider)
        matrixCursor = new MatrixCursor(new String[]{"_id", "name", "photo", "details"});

        // setup list_view and adapter
        adapter = new SimpleCursorAdapter(getBaseContext(),
                R.layout.layout_contact_custom_list,
                null,
                new String[]{"name", "photo", "details"},
                new int[]{R.id.tv_name, R.id.iv_photo, R.id.tv_details}, 0);

        listContacts = (ListView) findViewById(R.id.list_contacts);
        listContacts.setAdapter(adapter);

        // Create obj to AsyncTask class to retrieve contacts/data (Note: First create LoaderAsyncTask class)
        ContactsListLoaderAsync contactsListLoaderAsync = new ContactsListLoaderAsync();

        // execute Async Task class
        contactsListLoaderAsync.execute();

        // call ActionBar to display '+' sign to add contact
        getSupportActionBar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(getApplicationContext(), AddContactActivity.class);
        startActivityForResult(intent, 10);
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (AddContactActivity.isChanged)
            initializeViews();
    }

    public class ContactsListLoaderAsync extends AsyncTask<Void, Void, Cursor> {

        @Override
        protected Cursor doInBackground(Void... params) {
            // get Contacts content Uri
            Uri contactsUri = ContactsContract.Contacts.CONTENT_URI;

            // query using Contacts content Uri, to retrieve all contacts and initialize Cursor
            Cursor contactsCursor = getContentResolver().query(contactsUri, null, null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC ");

            if (contactsCursor != null && contactsCursor.moveToFirst()) {
                do {
                    long contactId = contactsCursor.getLong(contactsCursor.getColumnIndex("_ID"));

                    // get Contact Data Uri
                    Uri dataUri = ContactsContract.Data.CONTENT_URI;

                    // query using Contact Data Uri to retrieve a Contact's all details (mobile, email etc)
                    Cursor dataCursor = getContentResolver().query(dataUri, null, ContactsContract.Data.CONTACT_ID + "=" + contactId, null, null);


                    // declare variables for all requirements from a Contact

                    // Names values
                    String name = "";
                    String nickName = "";

                    // Phone numbers
                    String homePhone = "";
                    String mobilePhone = "";
                    String workPhone = "";

                    // Photo
                    String photoPath = "" + com.assignments.session14.assignment2.R.drawable.blank;
                    byte[] photoByte = null;

                    // emails
                    String homeEmail = "";
                    String workEmail = "";

                    // Organization details
                    String companyName = "";
                    String title = "";

                    if (dataCursor != null && dataCursor.moveToFirst()) {

                        // initialize all Requirements contact values

                        // name
                        name = dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));

                        do {

                            // nickName
                            if (dataCursor.getString(dataCursor.getColumnIndex("mimetype")).equals(ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE))
                                nickName = dataCursor.getString(dataCursor.getColumnIndex("data1"));

                            // phone numbers
                            if (dataCursor.getString(dataCursor.getColumnIndex("mimetype")).equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
                                switch (dataCursor.getInt(dataCursor.getColumnIndex("data2"))) {
                                    case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                                        homePhone = dataCursor.getString(dataCursor.getColumnIndex("data1"));
                                        break;
                                    case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE :
                                        mobilePhone = dataCursor.getString(dataCursor.getColumnIndex("data1"));
                                        break;
                                    case ContactsContract.CommonDataKinds.Phone.TYPE_WORK :
                                        workPhone = dataCursor.getString(dataCursor.getColumnIndex("data1"));
                                        break;
                                }
                            }

                            // Photo
                            if (dataCursor.getString(dataCursor.getColumnIndex("mimetype")).equals(ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)) {
                                photoByte = dataCursor.getBlob(dataCursor.getColumnIndex("data15"));

                                if (photoByte != null) {
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(photoByte, 0, photoByte.length);

                                    // cache directory
                                    File cacheDir = getBaseContext().getCacheDir();
                                    File tempFile = new File(cacheDir.getPath(), "/wpta_" + contactId + ".png");

                                    // File Output Stream to tempFile
                                    try {
                                        FileOutputStream outputStream = new FileOutputStream(tempFile);

                                        // compress bitmap to temp as png
                                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

                                        // flush and close the FOS
                                        outputStream.flush();
                                        outputStream.close();

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            // EMails
                            if(dataCursor.getString(dataCursor.getColumnIndex("mimetype")).equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE ) ) {
                                switch(dataCursor.getInt(dataCursor.getColumnIndex("data2"))){
                                    case ContactsContract.CommonDataKinds.Email.TYPE_HOME :
                                        homeEmail = dataCursor.getString(dataCursor.getColumnIndex("data1"));
                                        break;
                                    case ContactsContract.CommonDataKinds.Email.TYPE_WORK :
                                        workEmail = dataCursor.getString(dataCursor.getColumnIndex("data1"));
                                        break;
                                }
                            }

                            // Organization details
                            if(dataCursor.getString(dataCursor.getColumnIndex("mimetype")).equals(ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)){
                                companyName = dataCursor.getString(dataCursor.getColumnIndex("data1"));
                                title = dataCursor.getString(dataCursor.getColumnIndex("data4"));
                            }
                        } while (dataCursor.moveToNext());

                        String details = "";

                        // Concatenating various information to single string
                        if(homePhone != null && !homePhone.equals("") )
                            details = "HomePhone : " + homePhone + "\n";
                        if(mobilePhone != null && !mobilePhone.equals("") )
                            details += "MobilePhone : " + mobilePhone + "\n";
                        if(workPhone != null && !workPhone.equals("") )
                            details += "WorkPhone : " + workPhone + "\n";
                        if(nickName != null && !nickName.equals("") )
                            details += "NickName : " + nickName + "\n";
                        if(homeEmail != null && !homeEmail.equals("") )
                            details += "HomeEmail : " + homeEmail + "\n";
                        if(workEmail != null && !workEmail.equals("") )
                            details += "WorkEmail : " + workEmail + "\n";
                        if(companyName != null && !companyName.equals("") )
                            details += "CompanyName : " + companyName + "\n";
                        if(title != null && !title.equals("") )
                            details += "Title : " + title + "\n";

                        // add all info to cursor row
                        matrixCursor.addRow(new Object[]{Long.toString(contactId), name, photoPath, details});

                    }

                } while (contactsCursor.moveToNext());
            }
            return matrixCursor;
        }

        // on Post Execute
        @Override
        protected void onPostExecute(Cursor cursor) {
            adapter.swapCursor(cursor);
        }
    }

}
