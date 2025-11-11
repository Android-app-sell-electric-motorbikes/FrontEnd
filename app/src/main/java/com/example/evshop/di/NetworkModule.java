package com.example.evshop.di;

import android.content.Context;

import com.example.evshop.data.ApiService;
import com.example.evshop.data.auth.AuthInterceptor;
import com.example.evshop.data.network.PaymentApiService;
import com.example.evshop.data.TokenManager;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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

    @Provides
    @Singleton
    public TokenManager provideTokenManager(@ApplicationContext Context context) {
        return new TokenManager(context);
    }

    @Provides
    @Singleton
    public X509TrustManager provideTrustManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[]{};
            }
        };
    }

    @Provides
    @Singleton
    public SSLSocketFactory provideSslSocketFactory(X509TrustManager trustManager) {
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{trustManager}, new java.security.SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Provides
    @Singleton
    @Named("AuthClient")
    public OkHttpClient provideAuthOkHttpClient(SSLSocketFactory sslSocketFactory, X509TrustManager trustManager, TokenManager tokenManager) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustManager)
                .hostnameVerifier((hostname, session) -> true)
                .addInterceptor(new AuthInterceptor(tokenManager))
                .addInterceptor(logging)
                .build();
    }

    @Provides
    @Singleton
    @Named("PublicClient")
    public OkHttpClient providePublicOkHttpClient(SSLSocketFactory sslSocketFactory, X509TrustManager trustManager) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustManager)
                .hostnameVerifier((hostname, session) -> true)
                .addInterceptor(logging)
                .build();
    }

    // ** CUNG CẤP ApiService VỚI AuthClient **
    @Provides
    @Singleton
    public ApiService provideApiService(@Named("AuthClient") OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService.class);
    }

    // ** CUNG CẤP PaymentApiService VỚI PublicClient **
    @Provides
    @Singleton
    public PaymentApiService providePaymentApiService(@Named("PublicClient") OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PaymentApiService.class);
    }
}
