package com.zcshou.service

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
import androidx.annotation.RequiresApi
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
	private var mCurAlt: Double = 1.0
	private var mCurBea = 0F
	private var mSpeed = 0.0F/* 默认的速度，单位 m/s */
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
		setForeground(createForegroundInfo(progress))
		return withContext(Dispatchers.IO) {
			return@withContext try {
				mockLocation()
				Result.success()
			} catch (exception: Exception) {
				Result.failure()
			}
		}
//		return Result.success()
	}


	private suspend fun mockLocation() {
		val sematicDescription = inputData.getString(KEY_SEMATICDESCRIPTION)
		while (true) {
			delay(32)
			setLocationNetwork()
			setLocationGPS()
			setForeground(createForegroundInfo(sematicDescription ?: ""))
		}

	}

	// Creates an instance of ForegroundInfo which can be used to update the
	// ongoing notification.
	private fun createForegroundInfo(progress: String): ForegroundInfo {
		val title = "虚拟定位服务"
//		val cancel = applicationContext.getString(R.string.cancel_download)
		// This PendingIntent can be used to cancel the worker
		val intent = WorkManager.getInstance(applicationContext)
			.createCancelPendingIntent(getId())

		// Create a Notification channel if necessary
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			createChannel()
		}

		val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
			.setContentTitle(title)
			.setTicker(title)
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
				accuracy = Criteria.ACCURACY_COARSE.toFloat() // 设定此位置的估计水平精度，以米为单位。
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
			bundle.putInt("satellites", 3)
			val loc = Location(LocationManager.GPS_PROVIDER).apply {
				accuracy = Criteria.ACCURACY_FINE.toFloat() // 设定此位置的估计水平精度，以米为单位。
				altitude = mCurAlt // 设置高度，在 WGS 84 参考坐标系中的米
				bearing = mCurBea // 方向（度）
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
