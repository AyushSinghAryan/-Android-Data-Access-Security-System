package com.example.securefileaccess.helper;

import android.content.Context;
import android.content.SharedPreferences;

public class UserPref {

    public static String getId(Context con) {
        return sharedPreferences(con).getString("adminId", "");
    }

    public static boolean getLoginStatus(Context con) {
        return sharedPreferences(con).getBoolean("isLoggedIn", false);
    }

    public static void setValue(Context con, String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences(con).edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void setLoginStatus(Context con, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences(con).edit();
        editor.putBoolean("isLoggedIn", value);
        editor.apply();
    }

    public static SharedPreferences sharedPreferences(Context con) {
        return con.getSharedPreferences("SecureFileAccess", Context.MODE_PRIVATE);
    }
}