package com.taibao.app.map

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.core.SearchResult
import com.baidu.mapapi.search.geocode.GeoCodeResult
import com.baidu.mapapi.search.geocode.GeoCoder
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult
import com.taibao.app.utils.LocationBean
import com.taibao.app.utils.myLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import tech.jour.template.base.mvvm.vm.BaseViewModel
import tech.jour.template.base.utils.MMKVUtils
import tech.jour.template.common.constant.SpKey.SEARCH_HISTORY_KEY
import tech.jour.template.common.model.db.LocalLocationBean
import javax.inject.Inject


@HiltViewModel
class MapViewModel @Inject constructor(private val mRepository: MapRepository) : BaseViewModel() {

	val myLocationLivedata = MutableLiveData(myLocation)

	val insertSuccess = MutableLiveData(false)

	private val mCoder = GeoCoder.newInstance().apply {
		setOnGetGeoCodeResultListener(object : OnGetGeoCoderResultListener {
			override fun onGetGeoCodeResult(p0: GeoCodeResult?) {
			}

			override fun onGetReverseGeoCodeResult(reverseGeoCodeResult: ReverseGeoCodeResult?) {
				if (reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
					return
				} else {
					val fakeLatLng = reverseGeoCodeResult.location
					val bean = LocalLocationBean(
						latitude = fakeLatLng.latitude,
						longitude = fakeLatLng.longitude,
						address = reverseGeoCodeResult.address,
						sematicDescription = reverseGeoCodeResult.sematicDescription
					)
					viewModelScope.launch {
						mRepository.insert(bean)
						insertSuccess.postValue(true)
					}
				}
			}

		})

	}

	fun updateMyLocation(bean: LocationBean) {
		myLocation = bean
		myLocationLivedata.postValue(bean)
	}

	fun insertLocation(fakeLatLng: LatLng) {
		getLocationByGeo(fakeLatLng)
	}

	private fun getLocationByGeo(point: LatLng) {
		//执行 OnGetGeoCoderResultListener 回调
		mCoder.reverseGeoCode(
			ReverseGeoCodeOption()
				.location(point) // 设置是否返回新数据 默认值0不返回，1返回
				.newVersion(1) // POI召回半径，允许设置区间为0-1000米，超过1000米按1000米召回。默认值为1000
				.radius(500)
		)
	}

	fun getSearchHistoryList() = MMKVUtils.getList<String>(SEARCH_HISTORY_KEY)

	fun addSearchHistoryList(searchViewText: String) {
		val searchHistory = mutableListOf<String>()
		searchHistory.addAll(MMKVUtils.getList<String>(SEARCH_HISTORY_KEY))
		searchHistory.add(searchViewText)
		MMKVUtils.putObj(SEARCH_HISTORY_KEY, searchHistory.distinct())
	}

}
