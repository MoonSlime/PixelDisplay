package com.pixeldp.http;

import com.pixeldp.model.EyeListModel;
import com.pixeldp.model.ResponseModel;
import com.pixeldp.model.EyeModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface PixelAPI {
    @POST("login")
    Call<ResponseModel> login(@Query("accountName") String accountName);

    @POST("logout")
    Call<ResponseModel> logout();

    @POST("update")
    Call<ResponseModel> update(@Query("age") int age, @Query("sex") int sex, @Query("eyeModel") String eyeModel);

    @POST("optimize")
    Call<ResponseModel> optimize();

    @GET("getEyeInfo")
    Call<EyeListModel> getEyeInfo();

    @GET("getLastEyeInfo")
    Call<EyeModel> getLastEyeInfo();

    @GET("deleteEyeInfo")
    Call<ResponseModel> deleteEyeInfo(@Query("id") int id);
}