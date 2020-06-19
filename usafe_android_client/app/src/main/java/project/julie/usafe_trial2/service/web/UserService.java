package project.julie.usafe_trial2.service.web;

import java.util.List;

import okhttp3.MultipartBody;
import project.julie.usafe_trial2.entity.User;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface UserService {


    @FormUrlEncoded
    @POST("loginUser")
    Call<User> loginUser(@Field("email") String email,
                         @Field("password") String password);

    @FormUrlEncoded
    @POST("saveUser")
    Call<String> saveUser(@Field("email") String email,
                          @Field("phoneNo") String phoneNo,
                          @Field("firstName") String firstName,
                          @Field("lastName") String lastName,
                          @Field("password") String password);

    @GET("isUserExist/{phoneNo}")
    Call<Boolean> isUserExist(@Path("phoneNo") String phoneNo);

    @FormUrlEncoded
    @POST("addFriend")
    Call<String> addFriend(@Field("phoneNo") String phoneNo,
                           @Field("email") String email);


    @FormUrlEncoded
    @POST("findAllFriends")
    Call<List<User>> findAllFriends(@Field("userID") long userID);

    @Multipart
    @POST("/upload_video")
    Call<Void> uploadVideo(@Part MultipartBody.Part file);

}
