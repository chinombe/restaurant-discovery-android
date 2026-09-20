package com.chinombe.restaurants.di

import android.content.Context
import androidx.room.Room
import com.chinombe.restaurants.data.local.AppDatabase
import com.chinombe.restaurants.data.remote.DemoControls
import com.chinombe.restaurants.data.remote.DemoSettings
import com.chinombe.restaurants.data.remote.MockRestaurantApi
import com.chinombe.restaurants.data.remote.RestaurantApi
import com.chinombe.restaurants.data.repository.OfflineFirstRestaurantRepository
import com.chinombe.restaurants.domain.repository.RestaurantRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "restaurants.db").build()

    @Provides fun controls(settings: DemoSettings): DemoControls = settings

    @Provides @DefaultDispatcher fun dispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides fun dao(database: AppDatabase) = database.restaurantDao()

    @Provides @Singleton fun api(api: MockRestaurantApi): RestaurantApi = api

    @Provides
    @Singleton
    fun repository(repository: OfflineFirstRestaurantRepository): RestaurantRepository = repository
}

@Qualifier @Retention(AnnotationRetention.BINARY) annotation class DefaultDispatcher
