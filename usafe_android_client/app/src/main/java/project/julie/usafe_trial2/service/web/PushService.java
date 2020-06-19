package project.julie.usafe_trial2.service.web;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface PushService {

    @POST("/follow")
    @FormUrlEncoded
    Call<String> sendPush(@Field("ids") List<String> ids, @Field("user") String user, @Field("latlngs") List<List<Double>> latLngList,
                          @Field("userID") long userID, @Field("lat") double lat, @Field("long") double lng);

    @POST("/notificationUserIsRunning")
    @FormUrlEncoded
    Call<String> notifyFollowersWhenUserIsRunning(@Field("followersID") List<String> followersID, @Field("user") String user,
                                                  @Field("userID") long userID);

    @POST("/notificationUserNotFollowingRoute")
    @FormUrlEncoded
    Call<String> notifyFollowersWhenUserNotFollowingRoute(@Field("followersID") List<String> followersID, @Field("user") String user,
                                                          @Field("userID") long userID);

    @POST("/notificationUserTerminatedRoute")
    @FormUrlEncoded
    Call<String> notificationUserTerminatedRoute(@Field("followersID") List<String> followersID, @Field("user") String user,
                                                          @Field("userID") long userID);
}
