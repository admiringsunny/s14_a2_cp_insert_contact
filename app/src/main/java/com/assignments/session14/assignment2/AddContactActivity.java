package com.assignments.session14.assignment2;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class AddContactActivity extends Activity{

    static boolean isChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_contact);

        Button button = (Button) findViewById(R.id.bt_add_contact);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!((EditText) findViewById(R.id.et_name)).getText().toString().equals("")
                        &&
                        !((EditText) findViewById(R.id.et_mobile)).getText().toString().equals("")) {
                    String  etName = ((EditText) findViewById(R.id.et_name)).getText().toString();
                    String etMobile = ((EditText) findViewById(R.id.et_mobile)).getText().toString();
                    WritePhoneContact(etName, etMobile, getApplicationContext());
                    isChanged = true;
                    finish();
                }
                else
                    Toast.makeText(getApplicationContext(), "Name and Mobile cannot be left blank.", Toast.LENGTH_SHORT).show();

            }
        });

    }

    public void WritePhoneContact(String name, String mobile, Context context) {

        ArrayList<ContentProviderOperation> cpo = new ArrayList<>();
        int contactIndex = cpo.size();

        // insert Raw contact
        cpo.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());

        // insert Display name
        cpo.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, contactIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                .build());

        // insert Mobile number
        cpo.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, contactIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, mobile)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build());

        try{
            // Execute / applyBatch
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, cpo);
            Toast.makeText(getApplicationContext(), "Contact Added", Toast.LENGTH_SHORT).show();
        }catch (RemoteException e) {
            e.printStackTrace();
        }catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

}
