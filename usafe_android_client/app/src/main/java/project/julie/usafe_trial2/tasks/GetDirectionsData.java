package project.julie.usafe_trial2.tasks;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;


import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.maps.android.ui.IconGenerator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.core.content.ContextCompat;
import project.julie.usafe_trial2.JourneyActivity;
import project.julie.usafe_trial2.R;
import project.julie.usafe_trial2.constants.SharedPreferencesConstants;
import project.julie.usafe_trial2.entity.GardaStation;
import project.julie.usafe_trial2.entity.ParcelablePathChunk;
import project.julie.usafe_trial2.entity.PathBundle;
import project.julie.usafe_trial2.entity.Score;
import project.julie.usafe_trial2.entity.User;
import project.julie.usafe_trial2.service.web.ElasticSearchService;
import project.julie.usafe_trial2.service.web.PushService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GetDirectionsData extends AsyncTask<Object, String, String> {

    private Context context;
    GoogleMap mMap;
    String url;

    HttpURLConnection httpURLConnection = null;
    String data = "";
    InputStream inputStream = null;

    private double lights;

    long userID;
    private List<User> followers;

    private ArrayList<GardaStation> stations;

    public GetDirectionsData(Context context, List<User> followers) {
        this.context = context;
        this.followers = followers;
        this.stations = new ArrayList<>();
    }

    private Map<String, Integer> colors = new HashMap<>();

    @Override
    protected String doInBackground(Object... params) {
        mMap = (GoogleMap) params[0];
        url = (String) params[1];

        colors.put("0", Color.parseColor("#800080"));
        colors.put("1", Color.parseColor("#cc00ff"));
        colors.put("2", Color.parseColor("#c2c2c2"));
        colors.put("3", Color.parseColor("#ffb0f4"));

        try {
            URL myUrl = new URL(url);
            httpURLConnection = (HttpURLConnection) myUrl.openConnection();
            httpURLConnection.connect();

            inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            bufferedReader.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    @Override
    protected void onPostExecute(final String s) {
        JSONObject jsonObject = null;
        List<PathBundle> pathBundles = new ArrayList<>();

        try {
            int pathIndex = 0;
            jsonObject = new JSONObject(s);

            PathBundle pb = new PathBundle();
            pb.setIdx(pathIndex);
            pb.setLatLngs(extractLatLngs(jsonObject.getJSONObject("route")));
            pathBundles.add(pb);

            pathIndex++;

            if (jsonObject.getJSONObject("route").has("alternateRoutes")) {
                final JSONArray alternateArray = jsonObject.getJSONObject("route").getJSONArray("alternateRoutes");//.getJSONObject("route").getJSONObject("shape").getJSONArray("shapePoints");
                for (int i = 0; i < alternateArray.length(); i++) {
                    pb = new PathBundle();
                    pb.setIdx(pathIndex);
                    pb.setLatLngs(extractLatLngs(alternateArray.getJSONObject(i).getJSONObject("route")));
                    pathBundles.add(pb);
                    pathIndex++;
                }
            }

            for (PathBundle pathBundle : pathBundles) {

                final Score score = new Score();
                pathBundle.setScore(score);
                LatLng previous = null;
                float[] distance;
                for (LatLng latLng : pathBundle.getLatLngs()) {
                    pathBundle.getDoubleLatLngs().add(Arrays.asList(latLng.latitude, latLng.longitude));

                    if (previous != null) {
                        distance = new float[4];

                        Location.distanceBetween(
                                previous.latitude,
                                previous.longitude,
                                latLng.latitude,
                                latLng.longitude,
                                distance);

                        score.setPathLength(score.getPathLength() + distance[0]);
                    }

                    previous = latLng;
                }
            }


            for (final PathBundle pathBundle : pathBundles) {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("http://" + context.getResources().getString(R.string.home_IP) + ":8080/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                final ElasticSearchService service = retrofit.create(ElasticSearchService.class);

                service.getGardaStations(pathBundle.getDoubleLatLngs()).enqueue(new Callback<List<GardaStation>>() {
                    @Override
                    public void onResponse(Call<List<GardaStation>> call, Response<List<GardaStation>> response) {

                        stations.addAll(response.body());

                        List<GardaStation> pathGardaList = new ArrayList<>(response.body());

                        pathBundle.getScore().setGardaStationsCount(pathGardaList.size());

                        service.getLights(pathBundle.getDoubleLatLngs()).enqueue(new Callback<Long>() {
                            @Override
                            public void onResponse(Call<Long> call, Response<Long> response) {

                                SharedPreferences sharedPreferences = context.getSharedPreferences("preferences", context.MODE_PRIVATE);
                                int lightsWeight = sharedPreferences.getInt("lights_prefs", SharedPreferencesConstants.DEFAULT_LIGHTS_VALUE);
                                int gardaWeight = sharedPreferences.getInt("garda_prefs", SharedPreferencesConstants.DEFAULT_GARDA_VALUE);

                                lights = response.body().doubleValue();
                                pathBundle.getScore().setLightsCount(response.body());


                                service.getPathScore(pathBundle.getScore().getGardaStationsCount(), response.body(), pathBundle.getScore().getPathLength(), gardaWeight, lightsWeight).enqueue(new Callback<Double>() {
                                    @Override
                                    public void onResponse(Call<Double> call, Response<Double> response) {
                                        displayPath(s, response.body(), pathBundle, stations);
                                    }

                                    @Override
                                    public void onFailure(Call<Double> call, Throwable t) {
                                        Log.i("", t.getMessage());
                                        t.printStackTrace();
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Call<Long> call, Throwable t) {
                                Log.i("", t.getMessage());
                                t.printStackTrace();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<List<GardaStation>> call, Throwable t) {
                        Log.i("", "Something happened...");
                        t.printStackTrace();
                    }
                });

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void displayPath(String s, Double d, PathBundle pathBundle, List<GardaStation> stations) {

        SharedPreferences preferences = context.getSharedPreferences("user_info", Context.MODE_PRIVATE);
        userID = preferences.getLong("id", 0);

        SharedPreferences pref = context.getSharedPreferences("currentLocation", Context.MODE_PRIVATE);
        final double lat = Double.valueOf(pref.getFloat("lat", 0));
        final double lng = Double.valueOf(pref.getFloat("long", 0));

        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(colors.get(String.valueOf(pathBundle.getIdx())));
        polylineOptions.width(10);
        polylineOptions.clickable(true);

        SharedPreferences sp = context.getSharedPreferences("user_details", Context.MODE_PRIVATE);
        final String userFirstName = sp.getString("user_firstName", null);


        GetDirectionsData ctx = this;

        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                Log.i("", "Marker title: ");
                List<String> ids = new ArrayList<>();
                for (User follower : followers) {
                    ids.add(follower.getPhoneNo());
                }

                SharedPreferences prefs = context.getSharedPreferences("getDirectionsPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                Gson gson = new Gson();
                String followersID = gson.toJson(ids);
                editor.putString("user_fname", userFirstName);
                editor.putLong("user_id", userID);
                editor.putString("followers", followersID);
                editor.commit();

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("http://" + context.getResources().getString(R.string.home_IP) + ":8080/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();


                final PushService service = retrofit.create(PushService.class);
                service.sendPush(ids, userFirstName, castLatLngToDouble(polyline.getPoints()), userID, lat, lng).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        Log.i("", String.valueOf(response.code()));
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        t.printStackTrace();
                    }
                });

                Intent intent = new Intent(context, JourneyActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("user_id", userID);

                ArrayList<ParcelablePathChunk> parcelablePath = new ArrayList<>();
                for (List<Double> pathElement : castLatLngToDouble(polyline.getPoints())) {
                    parcelablePath.add(new ParcelablePathChunk(pathElement));
                }

                intent.putParcelableArrayListExtra("path", parcelablePath);
                intent.putParcelableArrayListExtra("gardaStations", new ArrayList<>(ctx.stations));
                context.startActivity(intent);
            }
        });

        LatLngBounds.Builder builder = new LatLngBounds.Builder();


        for (LatLng latLng : pathBundle.getLatLngs()) {
            MarkerOptions markerOptions = new MarkerOptions().position(latLng);
            builder.include(markerOptions.getPosition());
            polylineOptions.add(latLng);
        }

        mMap.addPolyline(polylineOptions);

        for (GardaStation station : this.stations) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(station.getGeoPoint().getLat(), station.getGeoPoint().getLon())).title("Garda station " + station.getStation()).icon(generateBitmapDescriptorFromRes(context, R.drawable.ic_police)));
        }
        Log.i("Path", "Ending path for: " + pathBundle.getScore());

        mMap.addMarker(new MarkerOptions().position(pathBundle.getLatLngs().get(0)).icon(generateBitmapDescriptorFromRes(context, R.drawable.ic_lens_black_24dp)));
        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                .position(pathBundle.getLatLngs().get(pathBundle.getLatLngs().size() - 1)));


        LatLng latLng = pathBundle.getLatLngs().get(pathBundle.getLatLngs().size() / 2);

        //LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        builder.include(pathBundle.getLatLngs().get(0));
        builder.include(pathBundle.getLatLngs().get(pathBundle.getLatLngs().size() - 1));
        LatLngBounds bounds = builder.build();

        int padding = 200; // padding around start and end marker
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);

        TextView text = new TextView(context);
        text.setText(String.format("Safety score: %.2f", d));
        text.setPaintFlags(Paint.FAKE_BOLD_TEXT_FLAG);
        text.setTextColor(polylineOptions.getColor());
        text.setPadding(5, 5, 5, 5);
        IconGenerator generator = new IconGenerator(context);
        generator.setContentView(text);
        Bitmap icon = generator.makeIcon();

        MarkerOptions tp = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(icon));

        mMap.addMarker(tp);


    }

    private List<LatLng> extractLatLngs(JSONObject obj) throws JSONException {
        List<LatLng> result = new ArrayList<>();
        JSONArray array = obj.getJSONObject("shape").getJSONArray("shapePoints");

        for (int i = 0; i < array.length(); i += 2) {
            LatLng latLng = new LatLng(array.getDouble(i), array.getDouble(i + 1));
            result.add(latLng);
        }

        return result;
    }

    private List<List<Double>> castLatLngToDouble(List<LatLng> latLngs) {
        List<List<Double>> result = new ArrayList<>();
        for (LatLng l : latLngs) {
            result.add(Arrays.asList(l.latitude, l.longitude));
        }
        return result;
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
