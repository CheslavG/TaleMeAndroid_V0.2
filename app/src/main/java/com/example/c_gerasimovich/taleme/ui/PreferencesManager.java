package com.example.c_gerasimovich.taleme.ui;

import android.content.SharedPreferences;
import android.net.Uri;

import com.example.c_gerasimovich.taleme.R;
import com.example.c_gerasimovich.taleme.TeleApp;

/**
 * Created by anray on 29.06.2016.
 */
public class PreferencesManager {

    private SharedPreferences mSharedPreferences;

    public static final String TALE1 = "THE TRUE BRIDE";
    public static final String TALE2 = "THE BEAR AND THE MOUSE";






    public PreferencesManager() {
        this.mSharedPreferences = TeleApp.getSharedPreferences();
        putTale(TALE1, TeleApp.getContext().getString(R.string.TALE1));
        putTale(TALE2, TeleApp.getContext().getString(R.string.TALE2));


    }



    public void saveUserAvatar(Uri uri) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
//        editor.putString(ConstantManager.USER_AVATAR_KEY, uri.toString());
        editor.apply();

    }







    public String getTaleTitle() {
        return mSharedPreferences.getString(TALE1, "null");

    }

    public void putTale(String key, String value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();

    }
}
