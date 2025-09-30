// data/RetrofitClient.java
package com.example.evshop.data;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit;
    private static ApiService api;

    private static final String BASE_URL = "https://f9ca9d5ccc29.ngrok-free.app"; // TODO: đổi cho đúng

    public static ApiService getApi(Context ctx){
        if (api == null){
            TokenManager tm = new TokenManager(ctx.getApplicationContext());
            HttpLoggingInterceptor log = new HttpLoggingInterceptor();
            log.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(tm))
                    .addInterceptor(log)
                    .build();
            Gson gson = new GsonBuilder().setLenient().create();
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
            api = retrofit.create(ApiService.class);
        }
        return api;
    }
}
