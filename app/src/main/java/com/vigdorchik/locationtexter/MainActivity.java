package com.vigdorchik.locationtexter;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;

import java.util.HashSet;

public class MainActivity extends Activity {

    static final int PICK_CONTACT_REQUEST = 1;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        NumberPicker np = (NumberPicker) findViewById(R.id.numberPicker);
        np.setMinValue(1);
        np.setMaxValue(60);
        np.setWrapSelectorWheel(true);
        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                sharedPref.edit().putInt(getString(R.string.preference_interval_key), newVal).apply();
            }
        });
        matchPreferences();
        Switch sw = (Switch) findViewById(R.id.toggle_service);
        if (isServiceRunning(TextService.class)) {
            sw.setChecked(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_CONTACT_REQUEST) {
            if (resultCode == RESULT_OK) {
                Uri contactUri = data.getData();

                String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.Contacts.DISPLAY_NAME};

                Cursor cursorNumber = getContentResolver()
                        .query(contactUri, projection, null, null, null);
                cursorNumber.moveToFirst();

                String number = cursorNumber.getString(cursorNumber.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String name = cursorNumber.getString(cursorNumber.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                cursorNumber.close();

                addContact(name, number);
                matchPreferences();
            }
        }
    }

    public void addContact(String name, String number) {
        HashSet<String> contacts = (HashSet<String>) sharedPref.getStringSet(getString(R.string.preference_contacts_key), new HashSet<String>());
        HashSet<String> new_contacts = new HashSet<>();
        for (String s : contacts) {
            new_contacts.add(s);
        }
        new_contacts.add(encodeContact(name, number));
        sharedPref.edit().putStringSet(getString(R.string.preference_contacts_key), new_contacts).apply();
    }

    public void matchPreferences() {
        NumberPicker np = (NumberPicker) findViewById(R.id.numberPicker);
        np.setValue(sharedPref.getInt(getString(R.string.preference_interval_key), 20));
        HashSet<String> contacts = (HashSet<String>) sharedPref.getStringSet(getString(R.string.preference_contacts_key), new HashSet<String>());

        TextView contact_list = (TextView) findViewById(R.id.contactsList);
        contact_list.setText("");
        for (String s : contacts) {
            Contact c = decodeContact(s);
            contact_list.append(c.getName() + ", ");
        }
    }

    public static String encodeContact(String name, String number) {
        return name + ";" + number;
    }

    public static Contact decodeContact(String str) {
        String[] strs = str.split(";");
        return new Contact(strs[0], strs[1]);
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void pickContact(View view) {
        Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
        pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
    }

    public void toggleService(View view) {
        Switch sw = (Switch) view;
        Intent intent = new Intent(this, TextService.class);
        if (sw.isChecked()) {
            startService(intent);
        } else {
            stopService(intent);
        }
    }

    public void clearContacts(View view) {
        sharedPref.edit().putStringSet(getString(R.string.preference_contacts_key), new HashSet<String>()).apply();
        matchPreferences();
    }
}
