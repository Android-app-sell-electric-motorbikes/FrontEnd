package com.example.evshop.di;

import com.example.evshop.data.ApiService;
import com.example.evshop.data.network.PaymentApiService;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    // ** SỬA LẠI BASE URL CHO ĐÚNG **
    @Provides
    @Singleton
    public Retrofit provideRetrofit() {
        return new Retrofit.Builder()
                .baseUrl("https://api.electricvehiclesystem.click/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    @Named("AuthApiService")
    public ApiService provideAuthApiService(Retrofit retrofit) {
        return retrofit.create(ApiService.class);
    }

    @Provides
    @Singleton
    @Named("PublicApiService")
    public ApiService providePublicApiService(Retrofit retrofit) {
        return retrofit.create(ApiService.class);
    }

    @Provides
    @Singleton
    public PaymentApiService providePaymentApiService(Retrofit retrofit) {
        return retrofit.create(PaymentApiService.class);
    }
}
