package com.zcshou.utils

import android.content.Context
import com.baidu.location.Address
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.busline.BusLineResult
import com.baidu.mapapi.search.busline.BusLineSearch
import com.baidu.mapapi.search.busline.BusLineSearchOption
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener
import com.baidu.mapapi.search.poi.PoiCitySearchOption
import com.baidu.mapapi.search.poi.PoiDetailResult
import com.baidu.mapapi.search.poi.PoiDetailSearchOption
import com.baidu.mapapi.search.poi.PoiDetailSearchResult
import com.baidu.mapapi.search.poi.PoiIndoorResult
import com.baidu.mapapi.search.poi.PoiNearbySearchOption
import com.baidu.mapapi.search.poi.PoiResult
import com.baidu.mapapi.search.poi.PoiSearch
import com.baidu.mapapi.search.sug.SuggestionResult
import com.baidu.mapapi.search.sug.SuggestionSearch
import com.baidu.mapapi.search.sug.SuggestionSearchOption


//临时存储我的位置,作为全局缓存使用
var myLocation = LocationBean()

fun Context.requestLocation(callback: (LocationBean) -> Unit) {

	//定位初始化
	LocationClient(this).apply {
		locOption = LocationClientOption().apply {
			setCoorType("bd09ll")
			isOnceLocation = true
			setIsNeedAddress(true)
			setIsNeedLocationDescribe(true)
			setIsNeedLocationPoiList(true)
			locationMode = LocationClientOption.LocationMode.Hight_Accuracy
		}
		registerLocationListener(object : BDAbstractLocationListener() {
			override fun onReceiveLocation(bdLocation: BDLocation?) {
				if (bdLocation != null) {
					callback(bdLocation.parseBean())
				}
			}
		})
		start()
	}

}


fun String.toLatLng(): LatLng {
	val latLngString = this.split(",")
	return LatLng(latLngString.last().toDouble(), latLngString.first().toDouble())
}

fun poiSearchDetail(uid: String, callback: (PoiDetailSearchResult?) -> Unit) {
	PoiSearch.newInstance().apply {
		setOnGetPoiSearchResultListener(object : OnGetPoiSearchResultListener {
			override fun onGetPoiResult(poiResult: PoiResult?) {
			}

			override fun onGetPoiDetailResult(p0: PoiDetailResult?) {
			}

			override fun onGetPoiDetailResult(p0: PoiDetailSearchResult?) {
				callback(p0)
			}

			override fun onGetPoiIndoorResult(p0: PoiIndoorResult?) {
			}
		})
		searchPoiDetail(PoiDetailSearchOption().poiUids(uid))
	}
}

fun poiSearchBus(city: String, address: String, callback: (PoiResult?) -> Unit) {
	PoiSearch.newInstance().apply {
		setOnGetPoiSearchResultListener(object : OnGetPoiSearchResultListener {
			override fun onGetPoiResult(poiResult: PoiResult?) {
				callback(poiResult)
			}

			override fun onGetPoiDetailResult(p0: PoiDetailResult?) {
			}

			override fun onGetPoiDetailResult(poiResult: PoiDetailSearchResult?) {
			}

			override fun onGetPoiIndoorResult(p0: PoiIndoorResult?) {
			}
		})
		searchInCity(
			PoiCitySearchOption()
				.city(city)
				.keyword(address)
				.scope(2)
		)
	}
}

fun searchBusLine(city: String, busLineId: String, callback: (BusLineResult?) -> Unit) {
	BusLineSearch.newInstance().apply {
		setOnGetBusLineSearchResultListener {
			callback(it)
		}
		searchBusLine(
			BusLineSearchOption()
				.city(city)
				.uid(busLineId)
		)
	}
}

fun poiSearchNearBy(
	lat: LatLng,
	keyword: String,
	radius: Int = 1000,
	callback: (PoiResult?) -> Unit
) {
	PoiSearch.newInstance().apply {
		setOnGetPoiSearchResultListener(object : OnGetPoiSearchResultListener {
			override fun onGetPoiResult(poiResult: PoiResult?) {
				callback(poiResult)
			}

			override fun onGetPoiDetailResult(p0: PoiDetailResult?) {
			}

			override fun onGetPoiDetailResult(poiResult: PoiDetailSearchResult?) {
			}

			override fun onGetPoiIndoorResult(p0: PoiIndoorResult?) {
			}
		})
		searchNearby(
			PoiNearbySearchOption()
				.location(lat)
				.radius(radius)
				.keyword(keyword)
				.pageNum(0)
		)
	}
}

fun sugSearch(
	keyword: String, callback: (SuggestionResult?) -> Unit
) {
	SuggestionSearch.newInstance().apply {
		setOnGetSuggestionResultListener {
			callback(it)
			destroy()
		}
		requestSuggestion(
			SuggestionSearchOption()
				.city(myLocation.city)
				.keyword(keyword)
		)
	}
}

fun BDLocation.parseBean(): LocationBean {
	return LocationBean(
		this.latitude,
		this.longitude,
		this.province,
		this.city,
		this.district,
		this.cityCode,
		this.adCode,
		this.address,
		this.country,
		this.street,
		this.locationDescribe,
		this.radius,
		this.direction,
	)
}

data class LocationBean(
	val latitude: Double = 0.0,
	val longitude: Double = 0.0,
	val province: String? = "",
	val city: String = "",
	val district: String = "",
	val cityCode: String = "",
	val adCode: String = "",
	val address: Address? = null,
	val country: String = "",
	val street: String = "",
	val locationDescribe: String? = "",
	val radius: Float = 0F,
	val direction: Float = 0F,
)