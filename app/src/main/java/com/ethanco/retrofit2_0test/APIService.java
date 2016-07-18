package com.ethanco.retrofit2_0test;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

public interface APIService {


    @GET("idcard/index")
    Call<IdCardEntity> getIdCardDetails(@Query("cardno") String cardno, @Query("dtype") String dtype, @Query("key") String key);

    @GET("idcard/index")
    Observable<IdCardEntity> getIdCardDetailsWithRx(@Query("cardno") String cardno, @Query("dtype") String dtype, @Query("key") String key);

}
