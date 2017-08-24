package com.ved.veddriver.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ved.veddriver.Constants.Constants;
import com.ved.veddriver.Constants.RequestHandler;
import com.ved.veddriver.R;
import com.ved.veddriver.SharedPref.SharedPrefManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText authCode;
    private Button buttonCheckAuth;
    String checkAuthCode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (SharedPrefManager.getInstance(this).isAuthCode()) {

            startActivity(new Intent(LoginActivity.this, DriverActivity.class));
            finish();

        }


        authCode = (EditText) findViewById(R.id.auth_code);
        buttonCheckAuth = (Button) findViewById(R.id.button_go);
        buttonCheckAuth.setOnClickListener(this);

    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.button_go:

                isAuthCodeLoggedIn();

                break;

        }

    }

    private void isAuthCodeLoggedIn() {


        checkAuthCode = authCode.getText().toString().trim();

        if (checkAuthCode.isEmpty()) {

            Toast.makeText(this, " Auth field is empty", Toast.LENGTH_SHORT).show();

        } else {

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.AUTH_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (!jsonObject.getBoolean("error_status")) {

//                            String auth_code, String route_id, String startDateMorning, String endDateMorning, String startDateEvening, String endDateEvening

                            SharedPrefManager.getInstance(getApplicationContext())
                                    .authCode(
                                            jsonObject.getString("auth_code"),
                                            jsonObject.getString("route_id"),
                                            jsonObject.getString("startDateMorning"),
                                            jsonObject.getString("endDateMorning"),
                                            jsonObject.getString("startDateEvening"),
                                            jsonObject.getString("endDateEvening"));

                            Log.e("TAG", " Logged in"+response);


                            startActivity(new Intent(LoginActivity.this, DriverActivity.class));
                            finish();
                            Toast.makeText(LoginActivity.this, "Success", Toast.LENGTH_SHORT).show();

                        }else {
                            Toast.makeText(LoginActivity.this, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Log.e("HELLO", " error "+error);

                }
            }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("auth_code", checkAuthCode);
                    return params;
                }


            };

            RequestHandler.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);


        }


    }

}
