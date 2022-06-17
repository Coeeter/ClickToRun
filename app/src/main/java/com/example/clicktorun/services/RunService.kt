package com.example.clicktorun.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.example.clicktorun.R
import com.example.clicktorun.utils.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

typealias Line = MutableList<LatLng>

@AndroidEntryPoint
class RunService : LifecycleService() {
    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var currentNotificationBuilder: NotificationCompat.Builder

    private var isFirstTimeTracking = true
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            if (isTracking.value!!) {
                addLocation(locationResult.lastLocation)
            }
        }
    }
    private var startTime: Long = 0
    private var pausedTime: Long = 0
    private var timeTakenInMilliseconds: Long = 0

    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val runPath = MutableLiveData<MutableList<Line>>()
        val distanceRanInMetres = MutableLiveData<Int>()
        val timeTaken = MutableLiveData<Long>()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_RUN_SERVICE -> {
                createService()
                if (isFirstTimeTracking) {
                    startTime = System.currentTimeMillis()
                    isFirstTimeTracking = false
                }
                isTracking.value = true
            }
            ACTION_PAUSE_RUN_SERVICE -> {
                isTracking.value = false
                startPauseTimer()
            }
            ACTION_CANCEL_RUN_SERVICE -> {
                createBaseValues()
                stopForeground(true)
                stopSelf()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        createBaseValues()
        currentNotificationBuilder = notificationBuilder
        isTracking.observe(this) {
            if (isFirstTimeTracking) return@observe
            updateNotification()
            if (it) return@observe startTrackingLocation()
            stopTrackingLocation()
        }
        runPath.observe(this) {
            distanceRanInMetres.value = it.getDistance()
        }
    }

    private fun createBaseValues() {
        isTracking.value = false
        runPath.value = mutableListOf()
        distanceRanInMetres.value = 0
        timeTaken.value = 0
    }

    @SuppressLint("MissingPermission")
    private fun startTrackingLocation() {
        runPath.value?.apply {
            add(mutableListOf())
            runPath.value = this
        }
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 1000L
            fastestInterval = 200L
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        startTimer()
    }

    private fun stopTrackingLocation() =
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)

    private fun startTimer() {
        val handler = Handler(mainLooper)
        val runnable = object : Runnable {
            override fun run() {
                if (isTracking.value == false) return
                timeTakenInMilliseconds = System.currentTimeMillis() - pausedTime - startTime
                timeTaken.value = timeTakenInMilliseconds
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)
    }

    private fun startPauseTimer() {
        val handler = Handler(mainLooper)
        val runnable = object : Runnable {
            override fun run() {
                if (isTracking.value == true) return
                pausedTime = System.currentTimeMillis() - timeTakenInMilliseconds - startTime
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)
    }

    private fun addLocation(location: Location) {
        runPath.value!!.apply {
            last().add(location.run {
                LatLng(latitude, longitude)
            })
            if (isTracking.value == false) return
            runPath.value = this
        }
    }

    private fun createService() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    setSound(null, null)
                }
            )
        startForeground(
            NOTIFICATION_ID,
            notificationBuilder.build()
        )
        timeTaken.observe(this) {
            currentNotificationBuilder.setContentText(it.toTimeString())
            notificationManager.notify(NOTIFICATION_ID, currentNotificationBuilder.build())
        }
    }

    private fun updateNotification() {
        val pendingIntent = PendingIntent.getService(
            this,
            if (isTracking.value == true) 0 else 1,
            Intent(this, RunService::class.java).apply {
                action = if (isTracking.value == true) ACTION_PAUSE_RUN_SERVICE
                else ACTION_START_RUN_SERVICE
            },
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val text = if (isTracking.value == true) "Pause"
        else "play"
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(currentNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }
        currentNotificationBuilder = notificationBuilder
            .addAction(R.drawable.ic_play, text, pendingIntent)
        notificationManager.notify(
            NOTIFICATION_ID,
            currentNotificationBuilder.build()
        )
    }
}