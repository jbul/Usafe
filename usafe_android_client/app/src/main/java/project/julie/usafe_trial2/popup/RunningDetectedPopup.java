package project.julie.usafe_trial2.popup;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import project.julie.usafe_trial2.R;
import project.julie.usafe_trial2.service.web.PushService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RunningDetectedPopup {

    private static String BASE_URL;
    private boolean isRunning = true;

    //PopupWindow display method
    public void showPopupWindow(final View view) {


        //Create a View object yourself through inflater
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window, null);

        //Specify the length and width through constants
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;

        //Make Inactive Items Outside Of PopupWindow
        boolean focusable = true;

        //Create a window with our parameters
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        //Set the location of the window on the screen
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);


        Button buttonEdit = popupView.findViewById(R.id.messageButton);
        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //As an example, display the message
                Toast.makeText(view.getContext(), "Ok you're not running", Toast.LENGTH_SHORT).show();
                Log.d("", "onClick: NOT RUNNING");
                popupWindow.dismiss();
                isRunning = false;

            }
        });

        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (isRunning) {

                    popupWindow.dismiss();
                    SharedPreferences prefs = view.getContext().getSharedPreferences("getDirectionsPrefs", Context.MODE_PRIVATE);
                    String userFname = prefs.getString("user_fname", null);
                    long userID = prefs.getLong("user_id", 0);
                    Gson g = new Gson();
                    String json = prefs.getString("followers", "");
                    Type type = new TypeToken<List<String>>() {
                    }.getType();
                    List<String> followersID = g.fromJson(json, type);


                    popupWindow.dismiss();
                    Toast.makeText(view.getContext(), "WARNING YOUR FOLLOWERS", Toast.LENGTH_SHORT).show();
                    Log.d("", "run: WARNING YOUR FOLLOWERS");
                    BASE_URL = "http://" + view.getResources().getString(R.string.home_IP) + ":8080/";

                    Gson gson = new GsonBuilder()
                            .setLenient()
                            .create();

                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .addConverterFactory(ScalarsConverterFactory.create())
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                            .build();

                    PushService service = retrofit.create(PushService.class);
                    service.notifyFollowersWhenUserIsRunning(followersID, userFname, userID).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            Log.i("", String.valueOf(response.code()));
                            isRunning = true;
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            t.printStackTrace();
                        }
                    });

                }
           }
        }, 20000);


        //Handler for clicking on the inactive zone of the window
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //Close the window when clicked
                return true;
            }
        });
    }

}

