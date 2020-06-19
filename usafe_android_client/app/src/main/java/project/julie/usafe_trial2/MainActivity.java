package project.julie.usafe_trial2;

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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private static String BASE_URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        preferences = getSharedPreferences("user_details", MODE_PRIVATE);
        setContentView(R.layout.activity_main2);
        BASE_URL = "http://" + getResources().getString(R.string.home_IP) + ":8080/";
    }

    public void login(View view) {

        TextView email = findViewById(R.id.editTextEmail);
        final TextView password = findViewById(R.id.editTextPassword);

        if(email.getText().toString().isEmpty() || password.getText().toString().isEmpty()){
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
        }

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
        final MainActivity main = this;
        service.loginUser(email.getText().toString(), password.getText().toString()).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                Log.i("", "Got a response");
                Log.i("", response.message());
                Log.i("", String.valueOf(response.code()));

                if (response.body() != null) {
                    preferences = getSharedPreferences("user_details", MODE_PRIVATE);

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("user_email", response.body().getEmail());
                    editor.putString("user_password", response.body().getPassword());
                    editor.putLong("user_id", response.body().getUserId());
                    editor.putString("user_phone", response.body().getPhoneNo());
                    editor.putString("user_firstName", response.body().getFirstName()) ;
                    editor.apply();

                    Toast.makeText(main, "Logged in successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(main, MapsActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(main, "Failed to log in", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.i("", "Got a response");
                Log.i("", t.getMessage());
                Toast.makeText(main, "Failed to log in", Toast.LENGTH_SHORT).show();


            }
        });
    }

    public void Register(View view) {
        Intent register = new Intent(this, RegistrationActivity.class);
        startActivity(register);
    }
}
