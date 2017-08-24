package com.ved.veddriver.Activity;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.ved.veddriver.Constants.Constants;
import com.ved.veddriver.Model.VedaGuruErrorListener;
import com.ved.veddriver.Notification.NotificationUtils;
import com.ved.veddriver.R;
import com.ved.veddriver.Service.LocationUpdateService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class DriverActivity extends AppCompatActivity implements View.OnClickListener, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, SensorEventListener {

    protected Button button, buttonCal;
    ImageView indicatorImage , gpsIndicator, networkIndicator;
    public static final String GCM_REPEAT_TAG = "ONE_TIME";
    private GcmNetworkManager gcmNetworkManager;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 200;
    public GoogleApiClient googleApiClient;
    public LocationRequest locationRequest;
    private final int FASTEST_INTERVAL = 2 * 1000;
    private final int TIME_INTERVAL = 2 * 1000;

    private SwipeRefreshLayout swipe;
    private CoordinatorLayout coordinatorLayout;
    private ImageView imageViewCompass;
    private float currentDegree = 0f;
    private SensorManager mSensorManager;

    LocationManager locationManager;
    ConnectivityManager connectivityManager;
    boolean gps_status = false;
    boolean network_status = false;
    int status = 0;

    private boolean isService = true;
    private CardView cardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        gcmNetworkManager = GcmNetworkManager.getInstance(this);

        buttonCal = (Button) findViewById(R.id.button_calculate);
        swipe = (SwipeRefreshLayout) findViewById(R.id.swipe);
        indicatorImage = (ImageView) findViewById(R.id.service_indicator);
        imageViewCompass = (ImageView) findViewById(R.id.imageCompass);
        gpsIndicator = (ImageView) findViewById(R.id.gps_status);
        networkIndicator = (ImageView) findViewById(R.id.network_status);
        cardView = (CardView) findViewById(R.id.cardView);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        buttonCal.setOnClickListener(this);
        indicatorImage.setVisibility(View.INVISIBLE);

        getRouteData();

        imageViewCompass = findViewById(R.id.imageCompass);


        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        locationRequest = new LocationRequest();
        locationRequest.setInterval(TIME_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        googleApiClient.connect();

        gpsIndicator.setVisibility(View.INVISIBLE);
        networkIndicator.setVisibility(View.INVISIBLE);
        checkGps();
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                checkGps();
                swipe.setRefreshing(false);

            }
        });




    }


    private boolean hasPermission() {

        int res = 0;
        String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_CALENDAR};

        for (String params : permissions) {
            res = checkCallingOrSelfPermission(params);
            if (!(res == PackageManager.PERMISSION_GRANTED)) {
                return false;
            }
        }

        return true;

    }

    private void requestPermission() {
        String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_CALENDAR};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            requestPermissions(permissions, MY_PERMISSIONS_REQUEST_LOCATION);


        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean allowed = true;

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION:

                for (int res : grantResults) {

                    allowed = allowed && (res == PackageManager.PERMISSION_GRANTED);

                }
                break;

            default:

                allowed = false;
                break;

        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {


    }

    @Override
    public void onLocationChanged(Location location) {

        LocationUpdateService.latitude = String.valueOf(location.getLatitude());
        LocationUpdateService.longitude = String.valueOf(location.getLongitude());

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {


    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.button_calculate:

                if (isService){

                    if (hasPermission()) {

                        checkGps();

                        if (status == 1){
                            indicatorImage.setVisibility(View.VISIBLE);
                            buttonCal.setText("STOP SERVICE");
                            PeriodicTask.Builder taskBuilder = new PeriodicTask.Builder();
                            PeriodicTask periodicTask = taskBuilder
                                    .setService(LocationUpdateService.class)
                                    .setTag(GCM_REPEAT_TAG)
                                    .setPeriod(10L)
                                    .setPersisted(true)
                                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED).build();
                            gcmNetworkManager.schedule(periodicTask);

                            buildNotification();
                            animation(Color.CYAN);
                            isService = false;
                            snackBarFunc("Service running");

                        }else {
                            AlertDialog alertDialog = new android.app.AlertDialog.Builder(this).create();
                            alertDialog.setTitle(" Please check connections !");
                            alertDialog.setMessage(" Enable your mobile GPS and Wi-Fi/3G \n networks to start this service. \n Thank you ! ");
                            alertDialog.setButton("Ok",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            //dismiss the dialog
                                        }
                                    });
                            alertDialog.show();

                        }

                    } else {
                        requestPermission();
                    }

                }else {

                    status = 0;
                    indicatorImage.setVisibility(View.INVISIBLE);
                    buttonCal.setText("START SERVICE");
                    isService = true;
                    gcmNetworkManager.cancelTask(GCM_REPEAT_TAG, LocationUpdateService.class);
                    gcmNetworkManager.cancelAllTasks(LocationUpdateService.class);
                    indicatorImage.clearAnimation();
                    snackBarFunc("Service Stopped");
                    break;
                }
        }

    }


    public void getRouteData() {


        StringRequest stringRequest = new StringRequest(Request.Method.GET, Constants.REQUEST_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                SharedPreferences.Editor editor = getSharedPreferences("PREF_ROUTE", 0).edit();
                editor.putString("ROUTE_RESPONSE", response);
                editor.commit();


            }
        }, new VedaGuruErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                super.onErrorResponse(error);
            }
        });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);


    }


    public void buildNotification() {

        SharedPreferences prefs = getSharedPreferences("PREF_ROUTE", 0);
        String response = prefs.getString("ROUTE_RESPONSE", "[]");


        String authCode = "";
        String startDateMorning = "";
        String endDateMorning = "";
        String startDateEvening = "";
        String endDateEvening = "";


        try {
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {

                Log.e("json: ", "" + response);
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                authCode = jsonObject.getString("auth_code");
                startDateMorning = jsonObject.getString("startDateMorning");
                endDateMorning = jsonObject.getString("endDateMorning");
                startDateEvening = jsonObject.getString("startDateEvening");
                endDateEvening = jsonObject.getString("endDateEvening");
                
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        long minSecSDM = getSecond(startDateMorning);
        long minSecEDM = getSecond(endDateMorning);
        long minSecSDE = getSecond(startDateEvening);
        long minSecEDE = getSecond(endDateEvening);

        if (minSecSDM > 0 || minSecEDM >0 || minSecSDE >0 || minSecEDE >0){

            NotificationUtils.scheduleNotification(getApplicationContext(), " Good Morning ", "Please Enable GPS & 3G ", getSecond(startDateMorning));
            NotificationUtils.scheduleNotification(getApplicationContext(), " Good Morning ", "Please Enable GPS & 3G ", getSecond(endDateMorning));
            NotificationUtils.scheduleNotification(getApplicationContext(), " Good Evening ", "Please Enable GPS & 3G ", getSecond(startDateEvening));
            NotificationUtils.scheduleNotification(getApplicationContext(), " Good Evening ", "Please Enable GPS & 3G ", getSecond(endDateEvening));

        }else {
            Log.e("SECOND","past date");
        }

    }


    public static long getSecond(String dateArg) {

        long diffSeconds = 0;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
        try {
            Date date = simpleDateFormat.parse(dateArg);
            Date currentTime = Calendar.getInstance().getTime();

            diffSeconds = date.getTime() - currentTime.getTime();


        } catch (ParseException e) {
            e.printStackTrace();
        }
        return diffSeconds;
    }


    public void animation(int color) {

        indicatorImage.setColorFilter(color);
        Animation myFade = AnimationUtils.loadAnimation(this, R.anim.blink);
        indicatorImage.startAnimation(myFade);

    }


    public void checkGps(){

        Animation animShake = AnimationUtils.loadAnimation(this, R.anim.shake);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        if (activeNetwork == null) {
//                    networkStatusText.setText("OFF");
            networkIndicator.setVisibility(View.VISIBLE);
            networkIndicator.startAnimation(animShake);
            network_status = false;

        }
        if (activeNetwork != null && activeNetwork.isConnected()) {

//                    networkStatusText.setText("ON");

            networkIndicator.setVisibility(View.INVISIBLE);
            network_status = true;
        }
        {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

//                        gpsStatusText.setText("ON");

                gpsIndicator.setVisibility(View.INVISIBLE);

                gps_status = true;

            } else {

//                        gpsStatusText.setText("OFF");

                gpsIndicator.setVisibility(View.VISIBLE);
                gpsIndicator.startAnimation(animShake);
                network_status = false;


            }
        }
        if (gps_status && network_status) {
            status = 1;
        }

    }


    public void snackBarFunc(String title){

        Snackbar snackbar = Snackbar.make(coordinatorLayout, title, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);

    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        float degree = Math.round(sensorEvent.values[0]);
        RotateAnimation ra = new RotateAnimation(currentDegree, -degree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setDuration(210);
        ra.setFillAfter(true);
        imageViewCompass.startAnimation(ra);
        currentDegree = -degree;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
