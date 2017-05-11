package com.example.c_gerasimovich.taleme;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by anray on 28.06.2016.
 */
public class TeleApp extends Application {

    private static final String TAG = "DEV: ";
    private static Context sContext;
    public static SharedPreferences sSharedPreferences;


    @Override
    public void onCreate() {
        super.onCreate();
        sSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sContext = this;

      /*  AndroidAudioConverter.load(this, new ILoadCallback() {
            @Override
            public void onSuccess() {
                // Great!
                Log.d(TAG, "AndroidAudioConverter success");
            }
            @Override
            public void onFailure(Exception error) {
                // FFmpeg is not supported by device
            }
        });*/




    }

    public static SharedPreferences getSharedPreferences() {

        return sSharedPreferences;
    }

    public static Context getContext() {
        return sContext;
    }

}
