package com.pixeldp.http;

import com.pixeldp.model.EyeModel;
import com.pixeldp.model.ResponseModel;
import com.pixeldp.model.UserModel;
import com.pixeldp.model.ValueModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface PixelAPI {
    @POST("prototype_join")
    Call<UserModel> join(@Query("userModel") String userModel);

    @POST("prototype_login")
    Call<UserModel> login(@Query("userModel") String userModel);

    @GET("prototype_getEyeInfo")
    Call<EyeModel> getEyeInfo();

    @GET("prototype_getRaspberryIp")
    Call<ValueModel> getRaspberryIp();

    @POST("prototype_saveIntensityProfile")
    Call<ResponseModel> saveIntensityProfile(@Query("intensityProfile") String intensityProfile);

    @POST("prototype_updateEyeInfo")
    Call<ResponseModel> updateEyeInfo(@Query("eyeModel") String eyeModel);

    @POST("prototype_insertEyeInfo")
    Call<ResponseModel> insertEyeInfo(@Query("eyeModel") String eyeModel);
}