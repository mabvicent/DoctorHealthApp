package com.example.doctorhealthapp.Remote;

import com.example.doctorhealthapp.model.FCMResponse;
import com.example.doctorhealthapp.model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {


    @Headers({
            "Authorisation:key=<AAAA8GDTkxg:APA91bEk2B3FrnKXLYJL5vNsC9NAc2A4HSZ2wa6m2vROgPje8FgmS6gR194NDygveuPUN8IuWqsQS0-kvKW59u0_q2nUV7Mi8-ecLELQQdOq05lHze9y2wT_tQQQ4zhym-pNeUpRnA-a>",
            "Content-Type:application/json"


    })
    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body Sender body);
}

