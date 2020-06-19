package project.julie.usafe_trial2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import project.julie.usafe_trial2.service.web.UserService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RegistrationActivity extends AppCompatActivity {

    private String BASE_URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration2);
        getSupportActionBar().hide();
        BASE_URL = "http://" + getApplicationContext().getResources().getString(R.string.home_IP) + ":8080/";
    }

    public void login(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void signUp(View view) {

        TextView email = findViewById(R.id.signup_input_email);
        TextView firstname = findViewById(R.id.signup_input_firstName);
        TextView lastname = findViewById(R.id.signup_input_lastName);
        TextView password = findViewById(R.id.signup_input_password);
        TextView phone = findViewById(R.id.signup_input_phone);
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        String phonePattern = "^[+]?[0-9]{9,12}$";

        if (firstname.getText().toString().isEmpty() || lastname.getText().toString().isEmpty() || email.getText().toString().isEmpty()
                || password.getText().toString().isEmpty() || password.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show();

        } else if (password.length() < 8) {
            Toast.makeText(this, "Password must contains minimum 8 characters.", Toast.LENGTH_SHORT).show();

        } else if (!email.getText().toString().trim().matches(emailPattern)) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();

        } else if (!phone.getText().toString().trim().matches(phonePattern)) {
            Toast.makeText(this, "Invalid phone number format", Toast.LENGTH_SHORT).show();
        } else {

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
            final RegistrationActivity ra = this;
            service.saveUser(email.getText().toString(), phone.getText().toString(),
                    firstname.getText().toString(), lastname.getText().toString(),
                    password.getText().toString()).enqueue(new Callback<String>() {

                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    Log.i("", "Got a response");
                    Log.i("", response.message());
                    Toast.makeText(ra, "Account created succesfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ra, MainActivity.class);
                    startActivity(intent);
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.i("", t.getMessage());
                    Toast.makeText(ra, "Failed to register", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


}
