package com.example.studiomusic.SP_Controller;

import android.content.Context;
import android.content.SharedPreferences;

public class SPService {

    public static SharedPreferences TOKEN(Context context) {
        // accessToken: string
        // refreshToken: string
        return context.getSharedPreferences("token", Context.MODE_PRIVATE);
    }

    public static SharedPreferences USER(Context context) {
        // _id: string
        // name: string
        // email: string
        // picture: string
        return context.getSharedPreferences("user", Context.MODE_PRIVATE);
    }

}
