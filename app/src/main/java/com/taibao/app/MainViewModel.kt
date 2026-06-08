package com.taibao.app

import androidx.lifecycle.MutableLiveData
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.taibao.app.map.MapRepository
import com.taibao.app.service.MockLocationWorker
import com.taibao.app.service.MockLocationWorker.Companion.KEY_LATITUDE
import com.taibao.app.service.MockLocationWorker.Companion.KEY_LONGITUDE
import com.taibao.app.service.MockLocationWorker.Companion.KEY_SEMATICDESCRIPTION
import com.taibao.app.service.MockLocationWorker.Companion.UNIQUE_WORK_NAME
import com.taibao.app.utils.MapUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import tech.jour.template.base.mvvm.vm.BaseViewModel
import tech.jour.template.common.model.db.LocalLocationBean
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val mRepository: MapRepository) : BaseViewModel() {
    val selectedLocationLivedata = MutableLiveData<LocalLocationBean>()
    val isMockServStart = MutableLiveData(false)
    private val workManager = WorkManager.getInstance()

    val fakeImagePath = MutableLiveData("")

    /**
     * 从 selectedLocationLivedata 取值启动模拟定位
     */
    fun startWorker() {
        val bean = selectedLocationLivedata.value ?: return
        val latLng = MapUtils.bd2wgs(bean.longitude, bean.latitude)
        startWorker(
            longitude = latLng[0],
            latitude = latLng[1],
            sematicDescription = bean.sematicDescription
        )
    }

    /**
     * 直接传入坐标和描述启动模拟定位
     */
    fun startWorker(longitude: Double, latitude: Double, sematicDescription: String) {
        isMockServStart.postValue(true)
        val data = Data.Builder()
            .putString(KEY_LONGITUDE, longitude.toString())
            .putString(KEY_LATITUDE, latitude.toString())
            .putString(KEY_SEMATICDESCRIPTION, sematicDescription)
            .build()
        val request = OneTimeWorkRequestBuilder<MockLocationWorker>()
            .setInputData(data)
            .build()
        workManager.enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun stopWorker() {
        isMockServStart.postValue(false)
        workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    fun getHistoryLocation() = mRepository.getAll()

    fun deleteHistory(bean: LocalLocationBean) = mRepository.delete(bean)
}