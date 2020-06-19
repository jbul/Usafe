package project.julie.usafe_trial2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import project.julie.usafe_trial2.entity.User;
import project.julie.usafe_trial2.service.web.UserService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class StartNavigationActivity extends AppCompatActivity {

    private SearchView searchView;
    private ListView listView, selectedFollowers;
    private ArrayList<User> list, followersList;
    private ArrayAdapter<User> adapter, followersAdapter;
    private LatLng latLngDestination;
    private String destinationAddress;
    private static String BASE_URL;
    private AutocompleteSupportFragment autocompleteFragmentTo;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_navigation);
        BASE_URL = "http://" + getResources().getString(R.string.home_IP) + ":8080/";

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }

        autocompleteFragmentTo = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_to);
        autocompleteFragmentTo.setHint("To");

        autocompleteFragmentTo.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS));
        autocompleteFragmentTo.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                latLngDestination = place.getLatLng();
                destinationAddress = place.getAddress();
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });


        searchView = findViewById(R.id.searchViewFollowers);
        listView = findViewById(R.id.listFollowers);
        selectedFollowers = findViewById(R.id.selectedFollowers);

        list = new ArrayList<>();
        followersList = new ArrayList<>();

        SharedPreferences sp = getSharedPreferences("user_details", MODE_PRIVATE);
        long userId = sp.getLong("user_id", 0);

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        UserService service = retrofit.create(UserService.class);
        final StartNavigationActivity activity = this;
        service.findAllFriends(userId).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                Log.i("", "Got a success response");
                Log.i("RESPONSE ", response.message());
                list.addAll(response.body());
                adapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, list);
                listView.setAdapter(adapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        User u = (User) parent.getAdapter().getItem(position);
                        if (!followersList.contains(u)) {
                            followersList.add(u);
                            followersAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, followersList);
                            selectedFollowers.setAdapter(followersAdapter);
                            selectedFollowers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    User u = (User) parent.getAdapter().getItem(position);
                                    followersList.remove(u);
                                    followersAdapter.notifyDataSetChanged();                                }
                            });
                        }
                    }
                });

                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {

                        if (list.contains(query)) {
                            adapter.getFilter().filter(query);
                        } else {
                            Toast.makeText(StartNavigationActivity.this, "No Match found", Toast.LENGTH_LONG).show();
                        }
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        adapter.getFilter().filter(s);
                        return false;

                    }
                });
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Log.i("", "Got a failure response");
                Log.i("", t.getMessage());
                t.printStackTrace();
            }
        });

    }

    public void go(View view) {
        if (latLngDestination == null) {
            Toast.makeText(getApplicationContext(), "Please select a destination", Toast.LENGTH_SHORT).show();
        } else {
            Intent i = new Intent(this, MapsActivity.class);
            i.putExtra("to", destinationAddress);
            i.putExtra("followers", followersList);
            startActivity(i);
        }
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
