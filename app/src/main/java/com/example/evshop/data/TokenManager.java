package com.example.evshop.data;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private final SharedPreferences prefs;
    public TokenManager(Context ctx){ prefs = ctx.getSharedPreferences("auth", Context.MODE_PRIVATE); }

    public void saveAccessToken(String t){ prefs.edit().putString("access_token", t).apply(); }
    public String getAccessToken(){ return prefs.getString("access_token", null); }

    public void saveRefreshToken(String t){ prefs.edit().putString("refresh_token", t).apply(); }
    public String getRefreshToken(){ return prefs.getString("refresh_token", null); }

    public void clear(){ prefs.edit().clear().apply(); }
}
