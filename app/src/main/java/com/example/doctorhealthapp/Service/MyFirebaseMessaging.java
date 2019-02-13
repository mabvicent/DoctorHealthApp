package com.example.doctorhealthapp.Service;

import android.content.Intent;

import com.example.doctorhealthapp.PatientCall;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

public class MyFirebaseMessaging  extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {


        LatLng customer_location = new Gson().fromJson(remoteMessage.getNotification().getBody(),LatLng.class);

        Intent customer = new Intent(getBaseContext(), PatientCall.class);
        customer.putExtra("lat",customer_location.latitude);
        customer.putExtra("log",customer_location.longitude);
        startActivity(customer);






    }
}
