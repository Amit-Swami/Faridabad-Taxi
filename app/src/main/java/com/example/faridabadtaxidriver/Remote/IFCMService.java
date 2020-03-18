package com.example.faridabadtaxidriver.Remote;

import com.example.faridabadtaxidriver.Model.DataMessage;
import com.example.faridabadtaxidriver.Model.FCMResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "content-Type:application/json",
            "Authorization:key=AAAAK5BEcR8:APA91bFZiQsFazv2JcYBOZu38JGGxrffRETEYLObMiPYcf_BsvrNAVYXC9Nnjj0ReN5121t-Wevr5XVBesNXmXnFApJyuuKKE3bBARqOn2Wupm56CkF3q7lxswQ-el9MjrVd4TFCq156"
    })
    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body DataMessage body);
}
