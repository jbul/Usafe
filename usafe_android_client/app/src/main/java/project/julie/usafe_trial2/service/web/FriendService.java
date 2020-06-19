package project.julie.usafe_trial2.service.web;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface FriendService {

    @GET("isFriendAdded/{userEmail}/{friendPhoneNo}")
    Call<Boolean> isFriendAdded(@Path("userEmail") String userEmail,
                                @Path("friendPhoneNo") String friendPhoneNo);

}
