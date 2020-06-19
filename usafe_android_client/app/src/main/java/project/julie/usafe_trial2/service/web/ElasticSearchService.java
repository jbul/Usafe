package project.julie.usafe_trial2.service.web;

import java.util.List;

import project.julie.usafe_trial2.entity.GardaStation;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ElasticSearchService {

    @FormUrlEncoded
    @POST("lightsOnPath")
    Call<Long> getLights(@Field("latlngs") List<List<Double>> latLngList);

    @FormUrlEncoded
    @POST("garda/getGardaStations")
    Call<List<GardaStation>> getGardaStations(@Field("latlngs") List<List<Double>> latLngList);

    @FormUrlEncoded
    @POST("path/getPathScore")
    Call<Double> getPathScore(@Field("gardaStations") Integer gardaStations, @Field("lights") Long lights, @Field("distance") Double distance,
                              @Field("gardaWeight") int gardaWeight, @Field("lightWeight") int lightWeight);
}


