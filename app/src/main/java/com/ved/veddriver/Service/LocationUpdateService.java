package com.ved.veddriver.Service;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.ved.veddriver.Constants.Constants;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * Created by NaRan on 8/10/17.
 * Location update in 10 sec service
 */

public class LocationUpdateService extends GcmTaskService {

    public static String latitude="";
    public static String longitude="";


    @Override
    public void onCreate() {
        super.onCreate();

        Toast.makeText(getApplicationContext(), " Location Update Service ", Toast.LENGTH_SHORT).show();

    }

    @Override
    public int onRunTask(TaskParams taskParams) {

        if (!TextUtils.isEmpty(latitude) && !TextUtils.isEmpty(longitude)){
            sendToServer(latitude,longitude);
        }
        else {
            Log.e(TAG, " onRunTask Error ");
        }



        return GcmNetworkManager.RESULT_FAILURE;
    }

    private void sendToServer(String lat, String log) {

        final String destLat = lat;
        final String destLog = log;

        final int id = 1;

        Log.e(TAG, "sendToServer: " + destLat);
        Log.e(TAG, "sendToServer: " + destLog);


        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.BASE_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


            }
        }) {
            @Override
            protected Map<String, String> getParams() {

                Map<String, String> params = new HashMap<String, String>();
                params.put(Constants.KEY_ID, String.valueOf(id));
                params.put(Constants.KEY_LAT, destLat);
                params.put(Constants.KEY_LOG, destLog);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);

    }


}
