package project.julie.usafe_trial2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.MaterialDialog.Builder;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import project.julie.usafe_trial2.constants.Constants;
import project.julie.usafe_trial2.constants.SharedPreferencesConstants;
import project.julie.usafe_trial2.entity.GardaStation;
import project.julie.usafe_trial2.entity.ParcelablePathChunk;
import project.julie.usafe_trial2.gps.GpsTracker;
import project.julie.usafe_trial2.popup.RunningDetectedPopup;
import project.julie.usafe_trial2.service.ActivityRecognitionService;
import project.julie.usafe_trial2.service.SpeechRecognitionService;
import project.julie.usafe_trial2.service.web.PathService;
import project.julie.usafe_trial2.service.web.PushService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.location.Location;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.PolyUtil;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class JourneyActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private static String BASE_URL;
    private PathService pathService;
    private GpsTracker gpsTracker;
    private Location currentLocation;
    private double latitude;
    private double longitude;
    private List<LatLng> latLngs;
    BottomNavigationView bottomNavigationView;
    private long userID;
    private Marker currentMarker;
    private boolean isSent;
    private Toolbar mToolbar;

    Timer timer;
    TimerTask timerTask;
    final Handler handler = new Handler();
    private MediaRecorder recorder;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private CameraDevice mCamera;
    CameraManager mCameraManager;

    public GoogleApiClient mApiClient;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        BASE_URL = "http://" + getResources().getString(R.string.home_IP) + ":8080/";
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        Intent i = getIntent();
        userID = i.getLongExtra("user_id", 0);

        enableAutoStart();
        startService(new Intent(JourneyActivity.this, SpeechRecognitionService.class));

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return false;
            }
        });

        this.mCameraManager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);
    }

    public void startTimer(long userID) {
        //set a new Timer
        timer = new Timer();
        //initialize the TimerTask's job
        initializeTimerTask(userID);
        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(timerTask, 5000, 10000); //
    }

    public void stoptimertask(View v) {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void initializeTimerTask(final long userID) {
        timerTask = new TimerTask() {
            public void run() {

                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {
                        Location location = gpsTracker.getLocation();

                        if (currentMarker != null) {
                            currentMarker.remove();
                        }

                        Log.i("CurrentLoc", String.valueOf(location));

                        if (location != null) {
                            LatLng currentLoc = new LatLng(location.getLatitude(), location.getLongitude());
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(currentLoc).icon(generateBitmapDescriptorFromRes(getApplicationContext(), R.drawable.ic_person_pin_circle_black_24dp));
                            currentMarker = mMap.addMarker(markerOptions);

                            isCurrentLocationOnPath(currentLoc, latLngs);

                        }

                    }
                });
            }
        };
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        gpsTracker = GpsTracker.getInstance(getApplicationContext());

        if (gpsTracker != null) {
            currentLocation = gpsTracker.getLocation();
            latitude = currentLocation.getLatitude();
            longitude = currentLocation.getLongitude();
        }


        ArrayList<GardaStation> stations = new ArrayList<>();

        if (getIntent().getExtras().containsKey("gardaStations")) {
            stations.addAll(getIntent().getExtras().getParcelableArrayList("gardaStations"));
        }

        if (!getIntent().getExtras().containsKey("path")) {
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();

            PathService pathService = retrofit.create(PathService.class);
            pathService.getUserPath(userID).enqueue(new Callback<List<List<Double>>>() {
                @Override
                public void onResponse(Call<List<List<Double>>> call, Response<List<List<Double>>> response) {
                    for (List<Double> d : response.body()) {

                        displayUserPath(response.body(), stations);
                    }
                }

                @Override
                public void onFailure(Call<List<List<Double>>> call, Throwable t) {
                    Log.i("", "Got a failure response");
                    Log.i("", t.getMessage());
                    t.printStackTrace();
                }
            });
        } else {
            List<List<Double>> coordinates = new ArrayList<>();
            ArrayList<ParcelablePathChunk> chunks = getIntent().getExtras().getParcelableArrayList("path");

            for (ParcelablePathChunk chunk : chunks) {
                coordinates.add(chunk.getChunk());
            }

            displayUserPath(coordinates, stations);
            startTimer(userID);
        }

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mApiClient.connect();


        // Register for the particular broadcast based on ACTION string
        IntentFilter filter = new IntentFilter(ActivityRecognitionService.ACTION);
        IntentFilter speechFilter = new IntentFilter(SpeechRecognitionService.class.getName());

        LocalBroadcastManager.getInstance(this).registerReceiver(testReceiver, filter);
        LocalBroadcastManager.getInstance(this).registerReceiver(testReceiver, speechFilter);
    }

    private void displayUserPath(List<List<Double>> coordinates, List<GardaStation> stations) {
        latLngs = new ArrayList<>();
        for (List<Double> list : coordinates) {
            latLngs.add(new LatLng(list.get(0), list.get(1)));
        }

        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.parseColor("#800080"));
        polylineOptions.width(10);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : latLngs) {
            MarkerOptions markerOptions = new MarkerOptions().position(latLng);
            builder.include(markerOptions.getPosition());
            polylineOptions.add(latLng);
            mMap.addPolyline(polylineOptions);
        }


        LatLngBounds bounds = builder.build();
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
        mMap.addMarker(new MarkerOptions().position(latLngs.get(0)).icon(generateBitmapDescriptorFromRes(this, R.drawable.ic_lens_black_24dp)));
        mMap.addMarker(new MarkerOptions().position(latLngs.get(latLngs.size() - 1)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));

        for (GardaStation station : stations) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(station.getGeoPoint().getLat(), station.getGeoPoint().getLon())).title("Garda station " + station.getStation()).icon(generateBitmapDescriptorFromRes(this, R.drawable.ic_police)));
        }
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

    public void stopJourney(MenuItem item) {

        SharedPreferences prefs = getApplicationContext().getSharedPreferences("getDirectionsPrefs", Context.MODE_PRIVATE);
        String userFname = prefs.getString("user_fname", null);
        Gson g = new Gson();
        String json = prefs.getString("followers", "");
        Type type = new TypeToken<List<String>>() {
        }.getType();
        List<String> followersID = g.fromJson(json, type);

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        PushService pushService = retrofit.create(PushService.class);
        pushService.notificationUserTerminatedRoute(followersID, userFname, userID).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.i("", "Journey finished");
                Log.i("RESPONSE ", response.message());
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.i("", "Got a failure response");
                Log.i("", t.getMessage());
                t.printStackTrace();
            }
        });

        PathService pathService = retrofit.create(PathService.class);
        pathService.stopLocation(userID).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.i("", "Location stopped");
                Log.i("RESPONSE ", response.message());
                Toast.makeText(getApplicationContext(), "Journey stopped", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.i("", "Got a failure response");
                Log.i("", t.getMessage());
                t.printStackTrace();
            }
        });


        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        startActivity(intent);
    }

    private BroadcastReceiver testReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        if (intent.getExtras().containsKey("running")) {
            String resultValue = intent.getStringExtra("running");
            if (resultValue != null) {
                RunningDetectedPopup puc = new RunningDetectedPopup();
                puc.showPopupWindow(getLayoutInflater().inflate(R.layout.popup_window, null));
            }
        } else if (intent.getExtras().containsKey("needsHelp")) {
            callBackupNumber();
        }
        }
    };

    public void callBackupNumber() {
        Intent intent = new Intent(Intent.ACTION_CALL);
        SharedPreferences sp = getSharedPreferences("preferences", MODE_PRIVATE);
        String phoneNo = sp.getString("phoneNo", SharedPreferencesConstants.EMERGENCY_NUMBER);
        intent.setData(Uri.parse("tel: " + phoneNo));

        if (checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            this.startActivity(intent);
        }
    }


    public boolean isCurrentLocationOnPath(LatLng currentLocation, List<LatLng> polyline) {
        if (PolyUtil.isLocationOnPath(currentLocation, polyline, false, 10.0)) { //tolerance 10 metres
            Log.d("", "isCurrentLocationOnPath: True");
            isSent = false;
            return true;
        }
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("getDirectionsPrefs", Context.MODE_PRIVATE);
        String userFname = prefs.getString("user_fname", null);
        Gson g = new Gson();
        String json = prefs.getString("followers", "");
        Type type = new TypeToken<List<String>>() {
        }.getType();
        List<String> followersID = g.fromJson(json, type);

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        if (!isSent) {
            PushService service = retrofit.create(PushService.class);
            service.notifyFollowersWhenUserNotFollowingRoute(followersID, userFname, userID).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    Log.i("", String.valueOf(response.code()));
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    t.printStackTrace();
                }
            });
        }
        isSent = true;
        return false;

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Intent intent = new Intent(this, ActivityRecognitionService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        ActivityRecognition.getClient(this).requestActivityUpdates(5, pendingIntent);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

 

    public void sosButtonPressed(View view) {
        Intent i = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(i, 1);
    }

    private void enableAutoStart() {
        for (Intent intent : Constants.AUTO_START_INTENTS) {
            if (getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                new Builder(this).title("Enable AutoStart")
                        .content(R.string.ask_permission)
                        .positiveText("ALLOW")
                        .onPositive((dialog, which) -> {
                            try {
                                for (Intent intent1 : Constants.AUTO_START_INTENTS)
                                    if (getPackageManager().resolveActivity(intent1, PackageManager.MATCH_DEFAULT_ONLY)
                                            != null) {
                                        startActivity(intent1);
                                        break;
                                    }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        })
                        .show();
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1){

            Uri uri = data.getData();
            File dataDirectory = Environment.getDataDirectory();

            File usafeDirectory = new File(dataDirectory, "/usafe/");

            if (!usafeDirectory.exists()) {
                usafeDirectory.mkdirs();
            }

            File output = new File(usafeDirectory, String.valueOf(System.currentTimeMillis()) + ".mp4");
            try {
                if (output.createNewFile()) {
                    try (OutputStream os = new FileOutputStream(output)) {

                        IOUtils.copy(getContentResolver().openInputStream(uri), os);
                        Log.i("", "File created: " + output.getPath());
                    } catch (Exception e) {
                        Log.e("", e.getMessage());
                    }
                }
                Log.i("", Environment.getDataDirectory().getAbsolutePath());
            } catch (IOException e) {
                Log.e("", "File: " + output.getPath() + " was not created: " + e.getMessage());
                e.printStackTrace();
            }
        }


    }

}

