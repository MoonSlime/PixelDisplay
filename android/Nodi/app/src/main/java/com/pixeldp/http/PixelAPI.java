package com.pixeldp.http;


import com.pixeldp.model.EyeListModel;
import com.pixeldp.model.EyeModel;
import com.pixeldp.model.ResponseModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface PixelAPI {
    @POST("login_nodi")
    Call<ResponseModel> login(@Query("accountName") String accountName);

    @POST("logout_nodi")
    Call<ResponseModel> logout();

    @POST("update_nodi")
    Call<ResponseModel> update(@Query("age") int age, @Query("sex") int sex, @Query("eyeModel") String eyeModel);

    @POST("optimize_nodi")
    Call<ResponseModel> optimize();

    @GET("getEyeInfo_nodi")
    Call<EyeListModel> getEyeInfo();

    @GET("getLastEyeInfo_nodi")
    Call<EyeModel> getLastEyeInfo();

    @GET("deleteEyeInfo_nodi")
    Call<ResponseModel> deleteEyeInfo(@Query("id") int id);
}