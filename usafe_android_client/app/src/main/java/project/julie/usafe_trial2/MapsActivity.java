package project.julie.usafe_trial2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import project.julie.usafe_trial2.entity.User;
import project.julie.usafe_trial2.gps.GpsTracker;
import project.julie.usafe_trial2.tasks.GetDirectionsData;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<User> followers;
    private static final int REQUEST_PERMISSION_LOCATION = 255;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;
    GpsTracker gpsTracker;
    private double latitude, longitude;
    private Context context;

    private SharedPreferences sharedPreferences;
    private long userID;
    private static String BASE_URL;
    private Marker currentMarker;
    private boolean isGettingPath = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);
            return;
        }

        BASE_URL = "http://" + getResources().getString(R.string.home_IP) + ":8080/";
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        fetchLocation();

        SharedPreferences sp = getSharedPreferences("user_details", MODE_PRIVATE);
        String userLogged = sp.getString("user_phone", null);
        userID = sp.getLong("user_id", 0);

        Intent i = getIntent();
        followers = (ArrayList<User>) i.getSerializableExtra("followers");

        FirebaseMessaging.getInstance().subscribeToTopic(userLogged)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Subscribed";
                        if (!task.isSuccessful()) {
                            msg = "Could not subscribe";
                        }
                        Log.d("", msg);
                    }
                });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {


        mMap = googleMap;
        getLocation();
    }

    public void startNavigation(View view) {
        Intent start = new Intent(this, StartNavigationActivity.class);
        startActivity(start);
    }

    public void addContact(MenuItem item) {
        Intent addContact = new Intent(this, AddContactActivity.class);
        startActivity(addContact);
    }

    private void getDirections(String destinationAddress) {
        Object[] dataTransfer = new Object[4];
        // TODO Extract in variable
        String key = "MAP_QUEST_KEY";

        GetDirectionsData getDirectionsData = new GetDirectionsData(getApplicationContext(), followers);
        dataTransfer[0] = mMap; //google map object
        dataTransfer[1] = "https://www.mapquestapi.com/directions/v2/alternateroutes?key=" + key + "&from=" + latitude + "," + longitude + "&to=" + destinationAddress + "&maxRoutes=4&timeOverage=100&routeType=pedestrian";
        Log.i("MapQuest", dataTransfer[1].toString());
        getDirectionsData.execute(dataTransfer);
    }

    private void getLocation(){
        gpsTracker = GpsTracker.getInstance(getApplicationContext());

        if (gpsTracker != null) {
            currentLocation = gpsTracker.getLocation();
            latitude = currentLocation.getLatitude();
            longitude = currentLocation.getLongitude();
        }

        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("I am here!").icon(generateBitmapDescriptorFromRes(getApplicationContext(), R.drawable.ic_person_pin_circle_black_24dp));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        mMap.addMarker(markerOptions);

        sharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("id", userID);
        editor.apply();

        if (!isGettingPath && getIntent().getExtras() != null && getIntent().getExtras().containsKey("to")) {

            getDirections(String.valueOf(getIntent().getExtras().getString("to")));
            isGettingPath = true;
        }
    }

    private void fetchLocation() {

        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    assert supportMapFragment != null;
                    supportMapFragment.getMapAsync(MapsActivity.this);

                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case 255: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(MapsActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                        //getLocation();
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    public void logout(MenuItem item) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }



    public void accessProfile(MenuItem item) {
        Intent i = new Intent(this, ProfileActivity.class);
        startActivity(i);
    }


    public static BitmapDescriptor generateBitmapDescriptorFromRes(
            Context context, int resId) {
        Drawable drawable = ContextCompat.getDrawable(context, resId);
        drawable.setBounds(
                0,
                0,
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
