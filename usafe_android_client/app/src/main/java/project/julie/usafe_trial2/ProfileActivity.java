package project.julie.usafe_trial2;

import androidx.appcompat.app.AppCompatActivity;
import project.julie.usafe_trial2.constants.SharedPreferencesConstants;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

public class ProfileActivity extends AppCompatActivity {

    private NumberPicker pickerGarda;
    private NumberPicker pickerLights;
    private int valueGardaPicker = 50, valueLightPicker = 50;
    private static final int maxValue = 100;
    private SharedPreferences sharedPreferences;
    private TextView contactNumber;
    private TextView contactName;
    private String number = "";
    private String contact = "";
    private int PICK_CONTACT = 1;
    private EditText inputKeyword;
    private TextView txtKeyword;
    private String emergencyKeyword = "";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        inputKeyword = findViewById(R.id.emergencyKeyword);
        txtKeyword = findViewById(R.id.keyword);

        contactNumber = findViewById(R.id.contactnumber);
        contactName = findViewById(R.id.contactName);


        pickerGarda = findViewById(R.id.numberPicker_garda);
        pickerLights = findViewById(R.id.numberPicker_lights);


        pickerGarda.setMinValue(0);
        pickerLights.setMinValue(0);
        pickerGarda.setMaxValue(100);
        pickerLights.setMaxValue(100);

        sharedPreferences = getSharedPreferences("preferences", MODE_PRIVATE);
        int lights = sharedPreferences.getInt("lights_prefs", SharedPreferencesConstants.DEFAULT_LIGHTS_VALUE);
        int garda = sharedPreferences.getInt("garda_prefs", SharedPreferencesConstants.DEFAULT_GARDA_VALUE);
        String num = sharedPreferences.getString("phoneNo", SharedPreferencesConstants.EMERGENCY_NUMBER);
        String name = sharedPreferences.getString("contactName", SharedPreferencesConstants.EMERGENCY_CONTACT_NAME);
        emergencyKeyword = sharedPreferences.getString("keyword", SharedPreferencesConstants.EMERGENCY_KEYWORD);
        txtKeyword.setText("Keyword: '" + emergencyKeyword + "'");



        pickerGarda.setValue(garda);
        pickerLights.setValue(lights);

        number = num;
        contact = name;
        contactNumber.setText(number);
        contactName.setText(contact);




        pickerGarda.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                valueGardaPicker = pickerGarda.getValue();
                pickerLights.setValue(maxValue - valueGardaPicker);
                valueLightPicker = maxValue - valueGardaPicker;
            }
        });

        pickerLights.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                valueLightPicker = pickerLights.getValue();
                pickerGarda.setValue(maxValue - valueLightPicker);
                valueGardaPicker = maxValue - valueLightPicker;

            }
        });

        Button buttonPickContact = (Button) findViewById(R.id.pickcontact);
        buttonPickContact.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, PICK_CONTACT);
            }

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_CONTACT) {
            if (resultCode == RESULT_OK) {
                Uri contactData = data.getData();
                Cursor cursor = getContentResolver().query(contactData, null, null, null, null);
                cursor.moveToFirst();

                String hasPhone = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                String contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                if (hasPhone.equals("1")) {
                    Cursor phones = getContentResolver().query
                            (ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                            + " = " + contactId, null, null);
                    while (phones.moveToNext()) {
                        number = phones.getString(phones.getColumnIndex
                                (ContactsContract.CommonDataKinds.Phone.NUMBER)).replaceAll("[-() ]", "");
                        contact = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    }
                    phones.close();
                    contactNumber.setText(number);
                    contactName.setText(contact);
                } else {
                    Toast.makeText(getApplicationContext(), "This contact has no phone number", Toast.LENGTH_LONG).show();
                }
                cursor.close();

            }
        }
    }


    public void savePreferences(View view) {
        if (!inputKeyword.getText().toString().isEmpty()) {
            emergencyKeyword = inputKeyword.getText().toString();
            txtKeyword.setText("Keyword: '" + emergencyKeyword + "'");
        }

        Toast.makeText(this, "Preferences saved successfully", Toast.LENGTH_SHORT).show();
        sharedPreferences = getSharedPreferences("preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("lights_prefs", valueLightPicker);
        editor.putInt("garda_prefs", valueGardaPicker);
        editor.putString("phoneNo", number);
        editor.putString("contactName", contact);
        editor.putString("keyword", emergencyKeyword);
        editor.commit();
    }

    public void addContact(MenuItem item) {
        Intent addContact = new Intent(this, AddContactActivity.class);
        startActivity(addContact);
    }

    public void logout(MenuItem item) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void accessProfile(MenuItem item) {
        Intent i = new Intent(this, ProfileActivity.class);
        startActivity(i);
    }

}
