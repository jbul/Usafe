package project.julie.usafe_trial2.service.web;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface PathService {

    @FormUrlEncoded
    @POST("path/saveCurrentLocation")
    Call<Void> saveCurrentLocation(@Field("userID") long userID,
                         @Field("lat") double lat, @Field("long") double lng);

    @FormUrlEncoded
    @POST("path/getUserPath")
    Call<List<List<Double>>> getUserPath(@Field("userID")long userID);

    @FormUrlEncoded
    @POST("path/getUserLocation")
    Call<List<Double>> getUserLocation(@Field("userID")long userID);

    @FormUrlEncoded
    @POST("path/stopJourney")
    Call<Void> stopLocation(@Field("userID")long userID);
}
