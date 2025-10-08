package com.example.evshop.di;

import android.content.Context;

import com.example.evshop.data.ApiService;
import com.example.evshop.data.AuthInterceptor;
import com.example.evshop.data.TokenManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import jakarta.inject.Singleton;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    @Provides
    @Singleton
    public TokenManager provideTokenManager(@ApplicationContext Context ctx){
        return new TokenManager(ctx);
    }

    @Provides @Singleton
    public OkHttpClient provideOkHttp(TokenManager tm){
        HttpLoggingInterceptor log = new HttpLoggingInterceptor();
        log.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(tm))
                .addInterceptor(log)
                .build();
    }

    @Provides @Singleton
    public Gson provideGson(){ return new GsonBuilder().setLenient().create(); }

    @Provides @Singleton
    public Retrofit provideRetrofit(OkHttpClient client, Gson gson){
        return new Retrofit.Builder()
                .baseUrl("https://api.metrohcmc.xyz/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    @Provides @Singleton
    public ApiService provideApi(Retrofit retrofit){
        return retrofit.create(ApiService.class);
    }
}
