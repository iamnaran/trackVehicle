package com.ved.veddriver;


import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

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
import com.ved.veddriver.Service.LocationUpdateService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class DriverActivity extends AppCompatActivity implements View.OnClickListener, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    protected Button button, btnStopService, buttonCal;
    ImageView indicatorImage;
    public static final String GCM_REPEAT_TAG = "ONE_TIME";
    private GcmNetworkManager gcmNetworkManager;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 200;
    public GoogleApiClient googleApiClient;
    public LocationRequest locationRequest;
    private final int FASTEST_INTERVAL = 2 * 1000;
    private final int TIME_INTERVAL = 2 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        gcmNetworkManager = GcmNetworkManager.getInstance(this);

        button = (Button) findViewById(R.id.button_driver);
        buttonCal = (Button) findViewById(R.id.button_calculate);
        btnStopService = (Button) findViewById(R.id.button_Stop);
        indicatorImage = (ImageView) findViewById(R.id.service_indicator);

        button.setOnClickListener(this);
        buttonCal.setOnClickListener(this);
        btnStopService.setOnClickListener(this);

        getRouteData();

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
                if (hasPermission()) {
                    PeriodicTask.Builder taskBuilder = new PeriodicTask.Builder();
                    PeriodicTask periodicTask = taskBuilder
                            .setService(LocationUpdateService.class)
                            .setTag(GCM_REPEAT_TAG)
                            .setPeriod(10L)
                            .setPersisted(true)
                            .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED).build();
                    gcmNetworkManager.schedule(periodicTask);
                    buildNotification();

                    Animation myFade = AnimationUtils.loadAnimation(this, R.anim.blink);

                    indicatorImage.startAnimation(myFade);


                } else {
                    requestPermission();

                }
                break;

            case R.id.button_Stop:

                gcmNetworkManager.cancelTask(GCM_REPEAT_TAG, LocationUpdateService.class);
                gcmNetworkManager.cancelAllTasks(LocationUpdateService.class);
                Toast.makeText(this, "Stopped", Toast.LENGTH_SHORT).show();
                break;

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

                Log.e("json: ", ""+response );
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

        Log.e("START DATE", "buildNotification: "+getSecond(startDateMorning));
        Log.e("END DATE", "buildNotification: "+getSecond(endDateMorning));
        Log.e("START DATE", "buildNotification: "+getSecond(endDateEvening));
        Log.e("END DATE", "buildNotification: "+getSecond(startDateEvening));

        NotificationUtils.scheduleNotification(getApplicationContext(), " Good Evening ", "Please Enable GPS & 3G ", getSecond(startDateMorning));
        NotificationUtils.scheduleNotification(getApplicationContext(), " Good Evening ", "Please Enable GPS & 3G ", getSecond(endDateMorning));
        NotificationUtils.scheduleNotification(getApplicationContext(), " Good Evening ", "Please Enable GPS & 3G ", getSecond(startDateEvening));
        NotificationUtils.scheduleNotification(getApplicationContext(), " Good Evening ", "Please Enable GPS & 3G ", getSecond(endDateEvening));

    }


    public static long getSecond(String dateArg) {

        long diffSeconds = 0;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
        try {
            Date date = simpleDateFormat.parse(dateArg);
            Date currentTime = Calendar.getInstance().getTime();


            diffSeconds = date.getTime() - currentTime.getTime() ;


        } catch (ParseException e) {
            e.printStackTrace();
        }
        return diffSeconds;
    }


}
