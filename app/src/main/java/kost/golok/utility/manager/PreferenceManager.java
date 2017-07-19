package kost.golok.utility.manager;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    public static final String PASSWORD = "yetececiteureup";

    private static PreferenceManager instance;

    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPref;

    private PreferenceManager(Context context){
        sharedPref = context.getSharedPreferences("absen", 0);
        editor = sharedPref.edit();
    }

    public static PreferenceManager getInstance(Context context){
        if(instance == null) instance = new PreferenceManager(context);

        return instance;
    }

    public void set(String key, String value){
        editor.putString(key, value);
        editor.commit();
    }

    public String get(String key){
        return sharedPref.getString(key, null);
    }

}
