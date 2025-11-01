package com.example.evshop.di;

import android.content.Context;

import com.example.evshop.data.ApiService;
import com.example.evshop.data.AuthInterceptor;
import com.example.evshop.data.TokenManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit; // Thêm import này
import javax.inject.Named;             // Thêm import này
import javax.inject.Singleton;        // Đảm bảo là 'javax.inject.Singleton'

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    private static final String BASE_URL = "https://api.electricvehiclesystem.click/";

    // --- PHẦN 1: Cung cấp các thành phần cơ bản (Giữ nguyên từ file cũ của bạn) ---

    @Singleton
    @Provides
    public TokenManager provideTokenManager(@ApplicationContext Context context) {
        return new TokenManager(context);
    }

    @Singleton
    @Provides
    public Gson provideGson() {
        return new GsonBuilder().setLenient().create();
    }


    // --- PHẦN 2: Cung cấp 2 phiên bản OkHttpClient ---

    /**
     * Cung cấp OkHttpClient CÓ TOKEN, đặt tên là "AuthClient".
     * Dùng cho API backend của bạn.
     */
    @Singleton
    @Provides
    @Named("AuthClient") // << Đặt tên
    public OkHttpClient provideAuthOkHttpClient(TokenManager tm) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(tm)) // Có AuthInterceptor để gắn Token
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Cung cấp OkHttpClient KHÔNG CÓ TOKEN, đặt tên là "PublicClient".
     * Dùng để upload file lên S3.
     */
    @Singleton
    @Provides
    @Named("PublicClient") // << Đặt tên
    public OkHttpClient providePublicOkHttpClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                // << KHÔNG có AuthInterceptor
                .addInterceptor(logging)
                .connectTimeout(60, TimeUnit.SECONDS) // Tăng thời gian chờ cho việc upload
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    // --- PHẦN 3: Cung cấp 2 phiên bản ApiService tương ứng ---

    /**
     * Cung cấp ApiService CÓ TOKEN, đặt tên là "AuthApiService".
     * Nó sử dụng "AuthClient" đã định nghĩa ở trên.
     */
    @Singleton
    @Provides
    @Named("AuthApiService") // << Đặt tên
    public ApiService provideAuthApiService(@Named("AuthClient") OkHttpClient okHttpClient, Gson gson) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(ApiService.class);
    }

    /**
     * Cung cấp ApiService KHÔNG CÓ TOKEN, đặt tên là "PublicApiService".
     * Nó sử dụng "PublicClient" đã định nghĩa ở trên.
     */
    @Singleton
    @Provides
    @Named("PublicApiService") // << Đặt tên
    public ApiService providePublicApiService(@Named("PublicClient") OkHttpClient okHttpClient, Gson gson) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL) // BaseUrl này không quá quan trọng vì S3 dùng url động
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(ApiService.class);
    }
}
