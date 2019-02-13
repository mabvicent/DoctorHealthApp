package com.example.doctorhealthapp;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.example.doctorhealthapp.Common.Common;
import com.example.doctorhealthapp.Remote.IGoogleAPI;
import com.example.doctorhealthapp.model.Token;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Welcome extends FragmentActivity implements OnMapReadyCallback,
                      GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
         LocationListener{

    private GoogleMap mMap;

    ///Play services

    private static final int MY_PERMISSION_REQUEST_CODE =7000;
    private static final int PLAY_SERVICE_RES_REQUEST =7001;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;


    private static int UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL =3000;
    private static int DISPLACEMENT =10;
    DatabaseReference docoter;
    GeoFire geoFire;
    Marker mCurrent;

    MaterialAnimatedSwitch loccation_switch;
    SupportMapFragment mapFragment;
    private FusedLocationProviderClient mFusedLocationClient;

    // car animation
    private List<LatLng> polyLineList;
    private Marker CarMarker;
    float v;
    private double lat,lng;
    private Handler handler;
    private LatLng startPostion,endPosition,currentPosition;
    private int index,next;
    //private Button btnGo;
    private PlaceAutocompleteFragment place;
    private String destination;
    private PolylineOptions polylineOptions,blackPolylineOption;
    private Polyline polyline,blackPolyline,greyPolyline;
    private IGoogleAPI mService;
    private PolylineOptions PolylineOptions,blackPolylineOptions;


    Runnable drawPathRunnable = new Runnable() {
        @Override
        public void run() {
            if(index<polyLineList.size()-1)
            {
                index++;
                next = index+1;

            }
            if(index<polyLineList.size()-1){
                startPostion = polyLineList.get(index);
                endPosition = polyLineList.get(next);


            }
            final ValueAnimator valueAnimator = ValueAnimator.ofFloat(0,1);
            valueAnimator.setDuration(3000);
            valueAnimator.setInterpolator(new LinearInterpolator());

            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {

                    v = valueAnimator.getAnimatedFraction();
                    lng = v*endPosition.longitude+(1-v)*startPostion.longitude;
                    lat =v*endPosition.latitude+(1-v)*startPostion.latitude;
                    LatLng newPos = new LatLng(lat,lng);

                    CarMarker.setPosition(newPos);

                    CarMarker.setAnchor(0.5f, 0.5f);

                    CarMarker.setRotation(getBearing(startPostion,newPos));

                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                        new CameraPosition.Builder()
                                .target(newPos)
                            .zoom(0.5f)
                            .build()

                    ));



                }
            });

            valueAnimator.start();
            handler.postDelayed(this,3000);



        }
    };

    private float getBearing(LatLng startPostion, LatLng endPosition) {

        double  lat = Math.abs(startPostion.latitude - endPosition.latitude);
        double  lng = Math.abs(startPostion.longitude - endPosition.longitude);

        if(startPostion.latitude < endPosition.latitude && startPostion.longitude < endPosition.longitude)
            return (float)(Math.toDegrees(Math.atan(lng/lat)));
        else  if(startPostion.latitude >= endPosition.latitude && startPostion.longitude < endPosition.longitude)
            return (float)((90-Math.toDegrees(Math.atan(lng/lat)))+90);
        else  if(startPostion.latitude >= endPosition.latitude && startPostion.longitude >= endPosition.longitude)
            return (float)(Math.toDegrees(Math.atan(lng/lat))+180);
        else  if(startPostion.latitude < endPosition.latitude && startPostion.longitude >= endPosition.longitude)
            return (float)((90-Math.toDegrees(Math.atan(lng/lat)))+270);

        return -1;

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //init view

        loccation_switch = findViewById(R.id.location_switch);
        loccation_switch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean isOnline) {

                if(isOnline){
                    startLocationUpdate();
                    displayLocation();

                    Snackbar.make(mapFragment.getView(),"You are online",Snackbar.LENGTH_SHORT).show();
                }
                else {
                    stopLocationUpdate();
                    mCurrent.remove();

                    mMap.clear();
                    handler.removeCallbacks(drawPathRunnable);
                    Snackbar.make(mapFragment.getView(),"You are offline",Snackbar.LENGTH_SHORT).show();
                }

            }


        });

        polyLineList = new ArrayList<>();



         place = (PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

         place.setOnPlaceSelectedListener(new PlaceSelectionListener() {
             @Override
             public void onPlaceSelected(Place place) {


                 if(loccation_switch.isChecked()){

                     destination = place.getAddress().toString();
                     destination = destination.replace("","+");
                     getDirection();

                 }
                 else {
                     Toast.makeText(Welcome.this,"Please change your status online",Toast.LENGTH_SHORT).show();
                 }

             }

             @Override
             public void onError(Status status) {
                 Toast.makeText(Welcome.this,""+status.toString(),Toast.LENGTH_SHORT).show();

             }
         });



        docoter = FirebaseDatabase.getInstance().getReference(Common.doctor_tb1);
        geoFire = new GeoFire(docoter);
        setupLOcation();
        mService = Common.getGoogleAPI();

        updateFirebaseToken();



    }

    private void updateFirebaseToken() {

        FirebaseDatabase db = FirebaseDatabase.getInstance();

        DatabaseReference tokens = db.getReference(Common.token_tb1);

        Token token = new Token(FirebaseInstanceId.getInstance().getToken());

            tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(token) ;

    }



    private void getDirection() {

        currentPosition = new LatLng(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude());
        String requestApi = null;

        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+currentPosition.latitude+","+currentPosition.longitude+"&"+
                    "destination="+destination+"&"+
                    "key="+getResources().getString(R.string.google_direction_api);
            Log.d("DOCTOR APP",requestApi);
            mService.gePath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().toString());

                                JSONArray jsonArray = jsonObject.getJSONArray("routes");
                                for(int i=0; i<jsonArray.length();i++){

                                    JSONObject route = jsonArray.getJSONObject(i);
                                    JSONObject poly = route.getJSONObject("overview_polyline");
                                    String polyline = poly.getString("points");
                                    polyLineList = decodePoly(polyline);



                                }

                                //Adjust bound

                                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                for(LatLng latLng:polyLineList)
                                    builder.include(latLng);
                                //Some error here
                                LatLngBounds bounds = builder.build();
                                CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,2);
                                mMap.animateCamera(mCameraUpdate);

                                PolylineOptions = new PolylineOptions();
                                polylineOptions.color(Color.GRAY);
                                polylineOptions.width(5);
                                polylineOptions.startCap(new SquareCap());
                                polylineOptions.endCap(new SquareCap());
                                polylineOptions.jointType(JointType.ROUND);
                                polylineOptions.addAll(polyLineList);
                                greyPolyline = mMap.addPolyline(polylineOptions);

                                blackPolylineOptions = new PolylineOptions();
                                blackPolylineOptions.color(Color.BLACK);
                                blackPolylineOptions.width(5);
                                blackPolylineOptions.startCap(new SquareCap());
                                blackPolylineOptions.endCap(new SquareCap());
                                blackPolylineOptions.jointType(JointType.ROUND);
                                blackPolylineOptions.addAll(polyLineList);
                                blackPolyline = mMap.addPolyline(blackPolylineOptions);

                                mMap.addMarker(new MarkerOptions()
                                        .position(polyLineList.get(polyLineList.size()-1))
                                         .title("Patient Location"));

                                //Animation

                                ValueAnimator polyLineAnimator = ValueAnimator.ofInt(0,100);
                                polyLineAnimator.setDuration(2000);
                                polyLineAnimator.setInterpolator(new LinearInterpolator());

                                polyLineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator animation) {


                                        List<LatLng> points = greyPolyline.getPoints();

                                        int percentValue  = (int)animation.getAnimatedValue();
                                        int size  = points.size();
                                        int newPoints = (int)(size * (percentValue/100.0f));

                                        List<LatLng> p = points.subList(0,newPoints);
                                        blackPolyline.setPoints(p);


                                    }
                                });
                                polyLineAnimator.start();

                                CarMarker = mMap.addMarker(new MarkerOptions().position(currentPosition)
                                   .flat(true)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_off)));
                                handler = new Handler();
                                index =-1;
                                next = 1;
                                handler.postDelayed(drawPathRunnable,3000);





                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(Welcome.this,""+t.getMessage(),Toast.LENGTH_SHORT).show();

                        }
                    });

        }catch (Exception e){
            e.printStackTrace();

        }



    }

    private List decodePoly(String encoded) {

        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
    //PRESS CNTR + O


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){

            case MY_PERMISSION_REQUEST_CODE:
                if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED)
                {
                    if(checkPlayServices()){
                        buildGooogleApiClient();
                        createLocationRequest();
                        if(loccation_switch.isChecked())

                            displayLocation();
                    }

                }

        }


    }

    private void setupLOcation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ) {


            //Request for runtime  permission
            ActivityCompat.requestPermissions(this, new String[]{
                   Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION

            },MY_PERMISSION_REQUEST_CODE);
        }
        else {
            if(checkPlayServices()){
                buildGooogleApiClient();
                createLocationRequest();
                if(loccation_switch.isChecked())

                displayLocation();
            }
        }



    }

    private void createLocationRequest() {


        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);


    }

    private void buildGooogleApiClient() {


        mGoogleApiClient = new GoogleApiClient.Builder(this)

                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS){
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICE_RES_REQUEST).show();
            else {
                Toast.makeText(this, "This devise is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;

        }
        return true;
    }


    private void stopLocationUpdate() {

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ){
            return;

        }

        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);






    }

    private void displayLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ){
            return;

        }
       Common. mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(Common.mLastLocation != null){

            if(loccation_switch.isChecked()){

                final double latitude = Common.mLastLocation.getLatitude();
                final  double longitude = Common.mLastLocation.getLongitude();

                //up date firebase



                geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                        new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {

                         //Add markerMainActivity
                        if(mCurrent!= null)
                        mCurrent.remove();//remove already markerMainActivity

                        mCurrent = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(latitude,longitude))
                                //.icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_location_off))
                                .title("Your current location"));

//                        mCurrent = mMap.addMarker(new MarkerOptions()MainActivity
//
//                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.walk))
//                        .position(new LatLng(latitude,longitude)).title("You"));
                        //move camera to this position

                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude),15.0f));




                    }
                });

            }

        }
        else {
            Log.d("ERROR","Can not get your location");
        }






    }

    private void rotateMarker(final Marker mCurrent, final float i, GoogleMap mMap) {

        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final float startRotation = mCurrent.getRotation();
        final long duration = 1500;

        final Interpolator interpolator = new LinearInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {

                long elapsed = SystemClock.uptimeMillis() -start;
                float t = interpolator.getInterpolation((float)elapsed/duration);
                float rot = t*i+(1-t)*startRotation;
                mCurrent.setRotation(-rot >180?rot/2:rot);

                if(t<1.0){
                    handler.postDelayed(this,16);




                }


            }
        });




    }
    private  BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void startLocationUpdate() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                            this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ){
            return;

        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);


    }

    @Override
    public void onLocationChanged(Location location) {

        Common.mLastLocation = location;
        displayLocation();

    }


    public void onStatusChanged(String provider, int status, Bundle extras) {

    }


    public void onProviderEnabled(String provider) {

    }


    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {


        displayLocation();
        startLocationUpdate();

    }

    @Override
    public void onConnectionSuspended(int i) {

        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
