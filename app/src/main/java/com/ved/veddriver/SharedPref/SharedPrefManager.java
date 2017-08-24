package com.ved.veddriver.SharedPref;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by NaRan on 8/23/17 at 16:35.
 */

public class SharedPrefManager {

    private static SharedPrefManager mInstance;
    private static Context mCtx;

    private static final String SHARED_PREF_NAME = "routeStats";
    public static final String KEY_AUTH_CODE = "auth_code";
    public static final String KEY_ROUTE_ID = "route_id";
    public static final String KEY_M_START_DATE = "startDateMorning";
    public static final String KEY_M_END_DATE = "endDateMorning";
    public static final String KEY_E_START_DATE = "startDateEvening";
    public static final String KEY_E_END_DATE = "endDateEvening";

    private SharedPrefManager(Context context) {
        mCtx = context;

    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SharedPrefManager(context);
        }
        return mInstance;
    }

    public boolean authCode(String auth_code, String route_id, String startDateMorning, String endDateMorning, String startDateEvening, String endDateEvening) {

        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_AUTH_CODE, auth_code);
        editor.putString(KEY_ROUTE_ID, route_id);
        editor.putString(KEY_M_START_DATE, startDateMorning);
        editor.putString(KEY_M_END_DATE, endDateMorning);
        editor.putString(KEY_E_START_DATE, startDateEvening);
        editor.putString(KEY_E_END_DATE, endDateEvening);
        editor.apply();
        return true;
    }

    public boolean isAuthCode() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        if (sharedPreferences.getString(KEY_AUTH_CODE, null) != null) {
            return true;
        }
        return false;
    }

    public boolean logout() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        return true;
    }


    public String getAuthCode() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_AUTH_CODE, null);
    }


}
