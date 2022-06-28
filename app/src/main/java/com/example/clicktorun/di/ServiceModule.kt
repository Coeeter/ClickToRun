package com.example.clicktorun.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.clicktorun.R
import com.example.clicktorun.ui.activities.MainActivity
import com.example.clicktorun.utils.ACTION_NAVIGATE_TO_TRACKING
import com.example.clicktorun.utils.NOTIFICATION_CHANNEL_ID
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Singleton
    @Provides
    fun providesFusedLocationProviderClient(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient = LocationServices
        .getFusedLocationProviderClient(context)

    @Provides
    fun providesPendingIntent(
        @ApplicationContext context: Context
    ): PendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java).apply {
            action = ACTION_NAVIGATE_TO_TRACKING
        },
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    @Provides
    fun providesNotificationBuilder(
        @ApplicationContext context: Context,
        pendingIntent: PendingIntent
    ): NotificationCompat.Builder = NotificationCompat
        .Builder(context, NOTIFICATION_CHANNEL_ID)
        .setContentTitle("Tracking your run!")
        .setContentText("00:00:00")
        .setSmallIcon(R.drawable.ic_shoes)
        .setAutoCancel(false)
        .setOngoing(true)
        .setContentIntent(pendingIntent)
}