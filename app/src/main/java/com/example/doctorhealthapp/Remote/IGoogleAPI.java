package com.example.doctorhealthapp.Remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface IGoogleAPI {
    @GET
    Call<String> gePath(@Url String url);


}
