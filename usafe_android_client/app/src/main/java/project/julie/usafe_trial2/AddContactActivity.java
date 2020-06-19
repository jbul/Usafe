package project.julie.usafe_trial2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import project.julie.usafe_trial2.adapter.ContactAdapter;
import project.julie.usafe_trial2.entity.Contact;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.ArrayList;

public class AddContactActivity extends AppCompatActivity {

    private ContactAdapter adapter;

    private SharedPreferences preferences;
    SearchView searchView;
    ArrayList<Contact> list;
    Cursor cursor;
    private String name, phoneNumber, email;
    public static final int RequestPermissionCode = 1;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences("user_details", MODE_PRIVATE);
        setContentView(R.layout.activity_add_contact);

        searchView = findViewById(R.id.searchView);


        RecyclerView listView = findViewById(R.id.recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        enableRuntimePermission();
        Log.i("", "Logged user: " + preferences.contains("user_email"));


        list = new ArrayList<>();
        getContactsIntoArrayList();
        adapter = new ContactAdapter(this, list);

        listView.setLayoutManager(linearLayoutManager);
        listView.setHasFixedSize(true);

        listView.setVisibility(View.VISIBLE);
        DividerItemDecoration itemDecor = new DividerItemDecoration(listView.getContext(), linearLayoutManager.getOrientation());
        listView.addItemDecoration(itemDecor);
        listView.setAdapter(adapter);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                if (list.contains(query)) {
                    adapter.getFilter().filter(query);
                } else {
                    Toast.makeText(AddContactActivity.this, "No Match found", Toast.LENGTH_LONG).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    public void enableRuntimePermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(
                AddContactActivity.this,
                Manifest.permission.READ_CONTACTS)) {
            Toast.makeText(AddContactActivity.this, "CONTACTS permission allows us to Access CONTACTS app", Toast.LENGTH_LONG).show();

        } else {
            ActivityCompat.requestPermissions(AddContactActivity.this, new String[]{
                    Manifest.permission.READ_CONTACTS}, RequestPermissionCode);

        }
    }

    public void getContactsIntoArrayList() {

        cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC");

        while (cursor.moveToNext()) {
            email = "";
            name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

            Cursor cur1 = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[]{id}, null);
            while (cur1.moveToNext()) {
                email = cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
            }
            Cursor cur2 = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
            while (cur2.moveToNext()) {
                phoneNumber = cur2.getString(cur2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            }


            cur1.close();
            list.add(new Contact(name, "", "", email, phoneNumber));
        }

        cursor.close();

    }

    public void addContact(MenuItem e) {
        Intent addContact = new Intent(this, AddContactActivity.class);
        startActivity(addContact);
    }

    public void logout(MenuItem e) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void accessProfile(MenuItem item){
        Intent i = new Intent(this, ProfileActivity.class);
        startActivity(i);
    }

}