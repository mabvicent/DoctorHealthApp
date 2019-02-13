package com.example.doctorhealthapp.Common;

import android.location.Location;

import com.example.doctorhealthapp.Remote.FCMClient;
import com.example.doctorhealthapp.Remote.IFCMService;
import com.example.doctorhealthapp.Remote.IGoogleAPI;
import com.example.doctorhealthapp.Remote.RetrofitClient;

public class Common {

    public static  Location mLastLocation = null;

    public static String currentToken = "";
    public static final String doctor_tb1 ="Doctors";
    public static final String user_doctor_tb1 ="DoctorInformation";
    public static final String user_patient_tb1 ="PatientsInformation";
    public static final String pickup_request_tb1 ="pickupRequest";
    public static final String token_tb1 ="Tokens";


    public static final String  baseURL = "https://maps.googleapis.com";
    public static final String  fcmURL = "https://fcm.googleapis.com/";

    public static IGoogleAPI getGoogleAPI(){

        return RetrofitClient.getClint(baseURL).create(IGoogleAPI.class);
    }
    public static IFCMService getFCMService(){

        return FCMClient.getClint(fcmURL).create(IFCMService.class);
    }


}
