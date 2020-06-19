package project.julie.usafe_trial2.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import project.julie.usafe_trial2.R;
import project.julie.usafe_trial2.entity.Contact;
import project.julie.usafe_trial2.service.web.FriendService;
import project.julie.usafe_trial2.service.web.UserService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.MyViewHolder> implements Filterable {

    private Context context;
    private ArrayList<Contact> contactsList;
    private ValueFilter valueFilter;
    private ArrayList<Contact> filteredResults;
    private SharedPreferences preferences;
    private String BASE_URL;

    private String friendPhoneNo;
    private String email;

    public ContactAdapter(Context ctx, ArrayList<Contact> list) {
        this.context = ctx;
        this.contactsList = list;
        this.filteredResults = new ArrayList<>(list);
        BASE_URL = "http://" + context.getResources().getString(R.string.home_IP) + ":8080/";
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final ContactAdapter contactAdapter = this;
        preferences = context.getSharedPreferences("user_details", 0);
        email = preferences.getString("user_email", null);
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_layout, parent, false);

        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                final TextView phoneNo = view.findViewById(R.id.phone_layout);
                friendPhoneNo = phoneNo.getText().toString();

                Gson gson = new GsonBuilder()
                        .setLenient()
                        .create();

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(ScalarsConverterFactory.create())
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                        .build();

                final UserService service = retrofit.create(UserService.class);

                service.isUserExist(friendPhoneNo).enqueue(new Callback<Boolean>() {
                    @Override
                    public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                        if (response.body() != null && response.body()) {
                            service.addFriend(friendPhoneNo, email).enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    Log.i("", "Got a response");
                                    Toast.makeText(contactAdapter.context, "Friend added successfully", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    Log.i("", "Got a response");
                                    Log.i("", t.getMessage());
                                    t.printStackTrace();
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<Boolean> call, Throwable t) {

                    }
                });
            }
        });


        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        final UserService service = retrofit.create(UserService.class);
        final FriendService friendService = retrofit.create(FriendService.class);


        filteredResults.stream()
                .forEach(c -> {
                    service.isUserExist(c.getPhoneNo()).enqueue(new Callback<Boolean>() {
                        @Override
                        public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                            if (c.getPhoneNo() != null) {
                                if (response.body()) {
                                    c.setUser(true);
                                    if (c.getFirstName().equalsIgnoreCase("Camille")) {
                                        Log.i("From API", "Camille is" + response.body());
                                    }
                                    friendService.isFriendAdded(email, c.getPhoneNo()).enqueue(new Callback<Boolean>() {
                                        @Override
                                        public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                                            if (response.body()) {
                                                c.setFriend(true);
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<Boolean> call, Throwable t) {
                                            Log.i("", "Got a failure response (isFriendAdded) ");
                                            Log.i("", t.getMessage());
                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<Boolean> call, Throwable t) {
                            Log.i("", "Got a failure response");
                            Log.i("", t.getMessage());
                            t.printStackTrace();
                        }

                    });
                });

        return new MyViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.name.setText(filteredResults.get(position).getFirstName());
        holder.phone.setText(filteredResults.get(position).getPhoneNo());

        if (filteredResults.get(position).isFriend()) {
            holder.invite.setVisibility(View.INVISIBLE);
            holder.inviteText.setVisibility(View.INVISIBLE);
        } else if (filteredResults.get(position).isUser()) {
            holder.invite.setVisibility(View.VISIBLE);
            holder.inviteText.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return filteredResults.size();
    }

    @Override
    public Filter getFilter() {
        if (valueFilter == null) {
            valueFilter = new ValueFilter();
        }
        return valueFilter;
    }

    private class ValueFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint != null && constraint.length() > 0) {
                ArrayList<Contact> filterList = new ArrayList<>();
                for (int i = 0; i < contactsList.size(); i++) {
                    if (contactsList.get(i).getEmail().toLowerCase().startsWith(constraint.toString().toLowerCase()) ||
                            contactsList.get(i).getFirstName().toLowerCase().startsWith(constraint.toString().toLowerCase()) ||
                            contactsList.get(i).getLastName().toLowerCase().startsWith(constraint.toString().toLowerCase()) ||
                            contactsList.get(i).getPhoneNo().toLowerCase().startsWith(constraint.toString().toLowerCase())) {
                        filterList.add(contactsList.get(i));
                    }
                }
                results.count = filterList.size();
                results.values = filterList;
            } else {
                results.count = contactsList.size();
                results.values = contactsList;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            filteredResults = (ArrayList<Contact>) results.values;
            notifyDataSetChanged();
        }

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView name, phone, inviteText;
        private ImageView invite;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name_layout);
            phone = itemView.findViewById(R.id.phone_layout);
            invite = itemView.findViewById(R.id.inviteImage);
            inviteText = itemView.findViewById(R.id.inviteTxt);
        }
    }


}
