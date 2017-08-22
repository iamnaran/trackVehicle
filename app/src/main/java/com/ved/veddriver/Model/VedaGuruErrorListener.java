package com.ved.veddriver.Model;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

/**
 * Created by NaRan on 8/21/17 at 13:25.
 */

public class VedaGuruErrorListener implements Response.ErrorListener {
    @Override
    public void onErrorResponse(VolleyError error) {
        if(error instanceof TimeoutError || error instanceof NoConnectionError){
            Log.e("connection","No connection detected...");
            return;
        }
        if(error instanceof ServerError){
            Log.e("connection","Error in serevr");
            return;
        }
        if(error instanceof ParseError){
            Log.e("connection","Error in parsing...");
            return;
        }
        if(error instanceof AuthFailureError){
            Log.e("connection","Failure in authoriztion");
            return;
        }
    }
}
