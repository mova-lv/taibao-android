package com.taibao.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.taibao.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class MockLocationWorker(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {

    companion object {
        const val NOTIFICATION_ID = 10086
        const val CHANNEL_ID = "虚拟定位"
        const val UNIQUE_WORK_NAME = "虚拟定位"
        const val KEY_LATITUDE = "key_latitude"
        const val KEY_LONGITUDE = "key_longitude"
        const val KEY_SEMATICDESCRIPTION = "key_sematicdescription"
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val mLocManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private var mCurLat: Double = 0.0
    private var mCurLng: Double = 0.0
    private var mCurAlt: Double = 30.0 // 更真实的海拔高度（地面约30米）
    private var mCurBea = 0F
    private var mSpeed = 0.5F/* 更真实的速度，模拟缓慢移动 0.5 m/s */
    override suspend fun doWork(): Result {
        addTestProviderNetwork()
        addTestProviderGPS()

        val latString = inputData.getString(KEY_LATITUDE)
            ?: return Result.failure()
        val longString = inputData.getString(KEY_LONGITUDE)
            ?: return Result.failure()

        mCurLat = latString.toDouble()
        mCurLng = longString.toDouble()

        // Mark the Worker as important
        val progress = "虚拟定位中..."
        // 创建通知渠道（只需一次）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }

        setForeground(createForegroundInfo(progress))

        return withContext(Dispatchers.IO) {
            return@withContext try {
                mockLocation()
                Result.success()
            } catch (exception: Exception) {
                exception.printStackTrace()
                Result.failure()
            }
        }
//		return Result.success()
    }


    private suspend fun mockLocation() {
        val sematicDescription = inputData.getString(KEY_SEMATICDESCRIPTION)
        var lastDescription: String? = null
        while (true) {
            delay(32)
            setLocationNetwork()
            setLocationGPS()
            // 通知文字有变化时才更新（减少系统通知触发）
            if (sematicDescription != lastDescription) {
                setForeground(createForegroundInfo(sematicDescription ?: ""))
                lastDescription = sematicDescription
            }
        }

    }

    // Creates an instance of ForegroundInfo which can be used to update the
    // ongoing notification.
    private fun createForegroundInfo(progress: String): ForegroundInfo {
        val title = "虚拟定位服务"
//		val cancel = applicationContext.getString(R.string.cancel_download)
        // This PendingIntent can be used to cancel the worker
        val intent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(id)


        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            //.setTicker(title)  // 移除 ticker 避免持续触发通知提醒
            .setContentText(progress)
            .setSmallIcon(R.drawable.ic_launcher_location)
            .setOngoing(true)
            // Add the cancel action to the notification which can
            // be used to cancel the worker
//			.addAction(android.R.drawable.ic_delete, cancel, intent)
            .build()
        return ForegroundInfo(
            NOTIFICATION_ID, notification,
            FOREGROUND_SERVICE_TYPE_LOCATION
        )
    }

    private fun setLocationNetwork() {
        try {
            val location = Location(LocationManager.NETWORK_PROVIDER).apply {
                accuracy = 50.0F // 更合理的网络定位精度（50米，而不是ACCURACY_COARSE的几百米）
                altitude = mCurAlt // 设置高度，在 WGS 84 参考坐标系中的米
                latitude = mCurLat // 纬度（度）
                longitude = mCurLng // 经度（度）
                bearing = mCurBea // 方向（度）
                time = System.currentTimeMillis() // 本地时间
                speed = mSpeed
                elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
            }

            mLocManager.setTestProviderLocation(LocationManager.NETWORK_PROVIDER, location)
        } catch (e: java.lang.Exception) {
        }
    }

    private fun setLocationGPS() {
        try {
            val bundle = Bundle()
            bundle.putInt("satellites", 12) // 更真实的卫星数量（12颗，而不是3颗）
            val loc = Location(LocationManager.GPS_PROVIDER).apply {
                accuracy = 5.0F // 更合理的GPS精度（5米，而不是ACCURACY_FINE的1米）
                altitude = mCurAlt // 设置高度，在 WGS 84 参考坐标系中的米
                bearing = if (mSpeed > 0.1F) mCurBea else 0F // 有速度时才设置方向，否则为0
                latitude = mCurLat // 纬度（度）
                longitude = mCurLng // 经度（度）
                time = System.currentTimeMillis() // 本地时间
                speed = mSpeed
                elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
                extras = bundle
            }

            mLocManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, loc)
        } catch (e: java.lang.Exception) {
        }
    }

    private fun addTestProviderNetwork() {
        try {
            // 先清理可能残留的 network test provider
            try {
                mLocManager.removeTestProvider(LocationManager.NETWORK_PROVIDER)
            } catch (_: IllegalArgumentException) {
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                mLocManager.addTestProvider(
                    LocationManager.NETWORK_PROVIDER, true, false,
                    true, true, true, true,
                    true, ProviderProperties.POWER_USAGE_LOW, ProviderProperties.ACCURACY_COARSE
                )
            } else {
                mLocManager.addTestProvider(
                    LocationManager.NETWORK_PROVIDER, true, false,
                    true, true, true, true,
                    true, Criteria.POWER_LOW, Criteria.ACCURACY_COARSE
                )
            }
            mLocManager.setTestProviderEnabled(LocationManager.NETWORK_PROVIDER, true)

        } catch (e: SecurityException) {
        }
    }

    private fun addTestProviderGPS() {
        try {
            // 先清理可能残留的 gps test provider
            try {
                mLocManager.removeTestProvider(LocationManager.GPS_PROVIDER)
            } catch (_: IllegalArgumentException) {
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                mLocManager.addTestProvider(
                    LocationManager.GPS_PROVIDER, false, true, false,
                    false, true, true, true, ProviderProperties.POWER_USAGE_HIGH,
                    ProviderProperties.ACCURACY_FINE
                )
            } else {
                mLocManager.addTestProvider(
                    LocationManager.GPS_PROVIDER, false, true, false,
                    false, true, true, true, Criteria.POWER_HIGH, Criteria.ACCURACY_FINE
                )
            }
            mLocManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true)

        } catch (e: Exception) {
        }
    }

    private fun removeTestProvider() {
        try {
            mLocManager.setTestProviderEnabled(LocationManager.NETWORK_PROVIDER, false)
            mLocManager.removeTestProvider(LocationManager.NETWORK_PROVIDER)
            mLocManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, false)
            mLocManager.removeTestProvider(LocationManager.GPS_PROVIDER)
        } catch (e: java.lang.Exception) {
        }
    }

    private fun createChannel() {

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        val name = "虚拟定位服务通知"
        val description = "虚拟定位前台服务"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        channel.description = description

        notificationManager.createNotificationChannel(channel)
    }

}