package com.app.studiomusic.SP_Controller;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

public class SPService {

    public static SharedPreferences TOKEN(Context context) {
        // accessToken: string
        // refreshToken: string
        return context.getSharedPreferences("token", Context.MODE_PRIVATE);
    };

    public static SharedPreferences USER(Context context) {
        // _id: string
        // name: string
        // email: string
        // picture: string
        return context.getSharedPreferences("user", Context.MODE_PRIVATE);
    };

    public static SharedPreferences SEARCH_HISTORY(Context context) {
        // array of strings
        return context.getSharedPreferences("search_history", Context.MODE_PRIVATE);
    };

    public static String GET_UNIQUE_DEVICE_ID(Context context) {
        SharedPreferences sp = context.getSharedPreferences("device_id", Context.MODE_PRIVATE);
        if (sp.contains("device_id")) return sp.getString("device_id", "");
        String id = UUID.randomUUID().toString();
        sp.edit().putString("device_id", id).apply();
        return id;
    };

    public static void CLEAR_UNIQUE_DEVICE_ID(Context context) {
        SharedPreferences sp = context.getSharedPreferences("device_id", Context.MODE_PRIVATE);
        sp.edit().clear().apply();
    };

}
