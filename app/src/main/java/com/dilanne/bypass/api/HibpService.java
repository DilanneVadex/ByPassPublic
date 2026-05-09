package com.dilanne.bypass.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface HibpService {
    @GET("range/{hashPrefix}")
    Call<String> getPasswordRange(@Path("hashPrefix") String hashPrefix);
}
