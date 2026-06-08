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

	fun startWorker() {
		isMockServStart.postValue(true)
		val blurRequest = OneTimeWorkRequestBuilder<MockLocationWorker>()
			.setInputData(createInputData())
			.build()
		workManager.enqueueUniqueWork(
			UNIQUE_WORK_NAME,
			ExistingWorkPolicy.REPLACE,
			blurRequest
		)
	}

	fun stopWorker() {
		isMockServStart.postValue(false)
		workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
	}

	private fun createInputData(): Data {
		val builder = Data.Builder()
		val bean = selectedLocationLivedata.value
		if (bean != null) {
			val latLng = MapUtils.bd2wgs(
				bean.longitude,
				bean.latitude
			)
			builder.putString(KEY_LONGITUDE, latLng[0].toString())
			builder.putString(KEY_LATITUDE, latLng[1].toString())
			builder.putString(KEY_SEMATICDESCRIPTION, bean.sematicDescription)
		}
		return builder.build()
	}

	fun getHistoryLocation() = mRepository.getAll()

	fun deleteHistory(bean: LocalLocationBean) = mRepository.delete(bean)


}
