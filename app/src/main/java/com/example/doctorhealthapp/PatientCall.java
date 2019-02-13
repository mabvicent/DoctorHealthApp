package com.example.doctorhealthapp;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.example.doctorhealthapp.Common.Common;
import com.example.doctorhealthapp.Remote.IGoogleAPI;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientCall extends AppCompatActivity {

    TextView Time, Address, Distance;
    MediaPlayer mediaPlayer;
    IGoogleAPI mService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_call);

         mService = Common.getGoogleAPI();

        Time = findViewById(R.id.textTime);
        Address = findViewById(R.id.textAddress);
        Distance = findViewById(R.id.textDistance);
        mediaPlayer = MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        if(getIntent()!= null){

            double lat = getIntent().getDoubleExtra("lat",-1.0);
            double lng = getIntent().getDoubleExtra("lng",-1.0);

            getDirection(lat,lng);

        }
    }

    private void getDirection(double lat,double lng) {


        String requestApi = null;

        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+ Common.mLastLocation.getLatitude()+","+Common.mLastLocation.getLongitude()+"&"+
                    "destination="+lat+","+lng+"&"+
                    "key="+getResources().getString(R.string.google_direction_api);
            Log.d("DOCTOR APP",requestApi);
            mService.gePath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().toString());
                                JSONArray routes = jsonObject.getJSONArray("routes");
                                JSONObject object = routes.getJSONObject(0);

                                JSONArray legs=  object.getJSONArray("legs");

                                JSONObject legsObject1 = legs.getJSONObject(0);
                                JSONObject distance  = legsObject1.getJSONObject("distance");
                                Distance.setText(distance.getString("text"));

                                JSONObject time  = legsObject1.getJSONObject("time");
                                Time.setText(time.getString("text"));

                                String address = legsObject1.getString("address");
                                Address.setText(address);





                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(PatientCall.this,""+t.getMessage(),Toast.LENGTH_SHORT).show();

                        }
                    });

        }catch (Exception e){
            e.printStackTrace();

        }



    }

    @Override
    protected void onStop() {
        mediaPlayer.release();

        super.onStop();

    }

    @Override
    protected void onPause() {
        mediaPlayer.release();
        super.onPause();
    }

    @Override
    protected void onPostResume() {


        super.onPostResume();
        mediaPlayer.start();
    }
}
