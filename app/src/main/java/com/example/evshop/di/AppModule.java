package com.example.evshop.di;

import com.example.evshop.data.Analytics;
import com.example.evshop.data.HomeRepository;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;


@Module
@InstallIn(SingletonComponent.class)
public class AppModule {
    @Provides @Singleton public HomeRepository provideHomeRepository() { return new HomeRepository(); }
    @Provides @Singleton public Analytics provideAnalytics() { return new Analytics(); }
}