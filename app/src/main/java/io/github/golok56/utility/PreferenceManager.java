package io.github.golok56.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import io.github.golok56.absenpelatih.MainActivity;

/**
 * This class will manage everything preference wise.
 *
 * @author Satria Adi Putra
 */
public class PreferenceManager {

    private static final String PREF_NAME = "pass_preference";

    private static final String PASSWORD = "PASSWORD";
    private static final String DEFAULT_PASSWORD = "yetececiteureup";

    private SharedPreferences mPref;

    private static PreferenceManager sInstance;

    private PreferenceManager(Context context) {
        mPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static PreferenceManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PreferenceManager(context);
        }

        return sInstance;
    }

    public boolean checkPassword(String password) {
        String passInPref = mPref.getString(PASSWORD, DEFAULT_PASSWORD);
        return password.equals(passInPref);
    }

    public void changePassword(String password) {
        mPref.edit().putString(PASSWORD, password).apply();
    }

}
