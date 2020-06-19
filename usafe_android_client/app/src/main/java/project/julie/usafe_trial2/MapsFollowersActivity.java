package project.julie.usafe_trial2;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import project.julie.usafe_trial2.service.web.PathService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MapsFollowersActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static String BASE_URL;

    Timer timer;
    TimerTask timerTask;
    //we are going to use a handler to be able to run in our TimerTask
    final Handler handler = new Handler();
    private Marker currentMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_followers);
        BASE_URL = "http://" + getResources().getString(R.string.home_IP) + ":8080/";
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
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

                        pathService.getUserLocation(userID).enqueue(new Callback<List<Double>>() {
                            @Override
                            public void onResponse(Call<List<Double>> call, Response<List<Double>> response) {
                                Log.i("", "Got a success response");
                                Log.i("RESPONSE ", response.message());
                                if(currentMarker != null){
                                    currentMarker.remove();
                                }
                                LatLng currentLoc = new LatLng(response.body().get(0), response.body().get(1));
                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.position(currentLoc);
                                currentMarker = mMap.addMarker(markerOptions);

                            }
                            @Override
                            public void onFailure(Call<List<Double>> call, Throwable t) {
                                Log.i("", "Got a failure response");
                                Log.i("", t.getMessage());
                                t.printStackTrace();
                            }
                        });
                    }
                });
            }
        };
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Intent intent = getIntent();
        long userID = Long.valueOf((String) intent.getExtras().get("userId"));

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
        final MapsFollowersActivity activity = this;
        pathService.getUserPath(userID).enqueue(new Callback<List<List<Double>>>() {
            @Override
            public void onResponse(Call<List<List<Double>>> call, Response<List<List<Double>>> response) {
                Log.i("", "Got a success response");
                Log.i("RESPONSE ", response.message());
                for(List<Double> d: response.body()){
                    Log.i("", "COORD " + d.get(0));
                    Log.i("", "COORD " + d.get(0));
                    displayUserPath(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<List<Double>>> call, Throwable t) {
                Log.i("", "Got a failure response");
                Log.i("", t.getMessage());
                t.printStackTrace();
            }
        });
        startTimer(userID);

    }

    private void displayUserPath(List<List<Double>> coordinates){
        List<LatLng> latLngs = new ArrayList<>();
        for (List<Double> list: coordinates){
            latLngs.add(new LatLng(list.get(0), list.get(1)));
        }

        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.BLUE);
        polylineOptions.width(10);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : latLngs) {
            MarkerOptions markerOptions = new MarkerOptions().position(latLng);
            builder.include(markerOptions.getPosition());
            polylineOptions.add(latLng);
            mMap.addPolyline(polylineOptions);
        }

        LatLngBounds bounds = builder.build();
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
        mMap.addMarker(new MarkerOptions().position(latLngs.get(0)).icon(generateBitmapDescriptorFromRes(this, R.drawable.ic_lens_black_24dp)));
        mMap.addMarker(new MarkerOptions().position(latLngs.get(latLngs.size()-1)));
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
