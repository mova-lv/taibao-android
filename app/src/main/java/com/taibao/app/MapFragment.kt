package com.taibao.app

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener
import com.baidu.mapapi.map.BaiduMapOptions
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.MapPoi
import com.baidu.mapapi.map.MapStatus
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MarkerOptions
import com.baidu.mapapi.map.MyLocationData
import com.baidu.mapapi.map.SupportMapFragment
import com.baidu.mapapi.model.LatLng
import com.blankj.utilcode.util.TimeUtils
import com.google.android.material.chip.Chip
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.AttachPopupView
import com.lxj.xpopup.core.BottomPopupView
import com.lxj.xpopup.enums.PopupPosition
import com.taibao.app.databinding.FragmentMapBinding
import com.taibao.app.databinding.ItemLocationHistorySearchBinding
import com.taibao.app.databinding.PopMapSearchBinding
import com.taibao.app.databinding.PopMapTypeBinding
import com.zcshou.utils.myLocation
import com.zcshou.utils.requestLocation
import com.zcshou.utils.sugSearch
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tech.jour.template.base.ktx.clickDelay
import tech.jour.template.common.model.db.SearchLocationBean
import tech.jour.template.common.ui.BaseFragment

@AndroidEntryPoint
class MapFragment : BaseFragment<FragmentMapBinding, MapViewModel>() {

	override val mViewModel by viewModels<MapViewModel>()

	private var myLatLng = LatLng(myLocation.latitude, myLocation.longitude)

	private var fakeLatLng = myLatLng

	private val supportMapFragment = SupportMapFragment.newInstance(
		BaiduMapOptions()
			.mapStatus(
				MapStatus.Builder().target(myLatLng)
					.zoom(16F)//地图缩放级别 4~21，室内图支持到22
					.build()
			)
			.compassEnabled(false) // 设置是否允许指南针，默认允许。
			.zoomControlsEnabled(false)
	)
	private val mBaiduMap by lazy { supportMapFragment.baiduMap }

	override fun initView() {
		childFragmentManager.beginTransaction()
			.add(R.id.mapFragment, supportMapFragment, "map_fragment")
			.commit()
		mBinding.apply {
			fabStart.clickDelay {
				mViewModel.insertLocation(fakeLatLng)
			}

			fabMapType.setOnClickListener {
				XPopup.Builder(requireContext())
					.isDestroyOnDismiss(false) //对于只使用一次的弹窗，推荐设置这个
					.popupPosition(PopupPosition.Bottom) //手动指定位置，有可能被遮盖
					.hasShadowBg(false) // 去掉半透明背景
					.atView(fabMapType)
					.asCustom(PopMapType(requireContext()))
					.show()
			}
			fabSearch.setOnClickListener {
				XPopup.Builder(requireContext())
					.isDestroyOnDismiss(false) //对于只使用一次的弹窗，推荐设置这个
					.autoOpenSoftInput(false)
					.hasShadowBg(true)
					.asCustom(PopSearchLocation(requireContext()))
					.show()
			}
			fabLocation.clickDelay {
				locateMap(myLatLng)
			}

			lifecycleScope.launch {
				delay(1000)
				supportMapFragment.baiduMap.setOnMapTouchListener {
					fabStart.shrink()
				}
				supportMapFragment.baiduMap.setOnMapClickListener(object : OnMapClickListener {
					override fun onMapClick(point: LatLng) {
						markMap(point)
					}

					override fun onMapPoiClick(poi: MapPoi) {
						markMap(poi.position)
					}
				})
			}
		}
	}

	override fun initObserve() {
		mViewModel.myLocationLivedata.observe(this) {
			myLatLng = LatLng(it.latitude, it.longitude)
			fakeLatLng = myLatLng

			TimeUtils.getNowMills()
			if (it.latitude > 0) {
				locateMap(myLatLng)
				mBaiduMap.isMyLocationEnabled = true
				val locData = MyLocationData.Builder()
					.accuracy(it.radius) // 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(it.direction)
					.latitude(it.latitude)
					.longitude(it.longitude)
					.build()
				mBaiduMap.setMyLocationData(locData)
			}
		}

//		mViewModel.selectedAddress.observe(this) {
//			Snackbar.make(mBinding.fabStart, it, Snackbar.LENGTH_LONG)
//				.setAction("Action", null).show()
//		}

		mViewModel.insertSuccess.observe(this) {
			if (it) findNavController().navigateUp()
		}

	}


	override fun initRequestData() {
		requireContext().requestLocation {
			mViewModel.updateMyLocation(it)
		}
	}

	private fun locateMap(latLng: LatLng) {
		supportMapFragment.baiduMap.animateMapStatus(
			MapStatusUpdateFactory.newLatLngZoom(
				latLng,
				17.4F
			)
		)
	}

	//标定选择的位置
	private fun markMap(point: LatLng) {
		fakeLatLng = point
		mBinding.fabStart.extend()

		mBaiduMap.clear()
		mBaiduMap.addOverlay(
			MarkerOptions().position(point)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_piner))
		)
	}


	inner class PopMapType(context: Context) : AttachPopupView(context) {
		override fun getImplLayoutId(): Int {
			return R.layout.pop_map_type
		}

		override fun onCreate() {
			super.onCreate()
			val binding = PopMapTypeBinding.bind(this.popupImplView)
			val mBaiduMap = supportMapFragment.baiduMap
			binding.apply {
				when (mBaiduMap.mapType) {
					BaiduMap.MAP_TYPE_NORMAL -> {
						normalIv.isSelected = true
					}

					else -> {
						satelliteIv.isSelected = true
					}
				}
				normalIv.clickDelay {
					normalIv.isSelected = true
					satelliteIv.isSelected = false
					mBaiduMap.mapType = BaiduMap.MAP_TYPE_NORMAL
					dismiss()
				}
				satelliteIv.clickDelay {
					normalIv.isSelected = false
					satelliteIv.isSelected = true
					mBaiduMap.mapType = BaiduMap.MAP_TYPE_SATELLITE
					dismiss()
				}
			}
		}
	}

	inner class PopSearchLocation(context: Context) : BottomPopupView(context) {
		override fun getImplLayoutId(): Int {
			return R.layout.pop_map_search
		}

		override fun onCreate() {
			super.onCreate()
			val binding = PopMapSearchBinding.bind(this.popupImplView)
			binding.apply {
//				searchView.setStatusBarSpacerEnabled(false)

				mViewModel.getSearchHistoryList().forEach { historyString ->
					val chip = Chip(context).apply {
						text = historyString
						setOnClickListener {
							searchBar.setText(historyString)
							searchView.hide()
							doSearch(historyString)
						}
					}
					chipGroup.addView(chip)
				}
				searchView
					.getEditText()
					.setOnEditorActionListener { v, actionId, event ->
						val searchString = searchView.text.toString()
						searchBar.setText(searchString)
						searchView.hide()
						doSearch(searchString)
						false
					}
			}
		}

		private fun doSearch(searchString: String) {
			val binding = PopMapSearchBinding.bind(this.popupImplView)

			showLoading()
			mViewModel.addSearchHistoryList(searchString)
			sugSearch(searchString) {
				dismissLoading()
				if (it?.allSuggestions != null) {
					val resultList = mutableListOf<SearchLocationBean>()
					it.allSuggestions.filter {
						it.pt != null
					}.forEach {
						resultList.add(
							SearchLocationBean(
								it.address,
								it.key,
								it.pt.latitude,
								it.pt.longitude,
							)
						)
					}
					binding.recyclerView.adapter = SearchLocationAdapter(resultList)
				}
			}
		}
	}


	inner class SearchLocationAdapter(private val data: List<SearchLocationBean>) :
		RecyclerView.Adapter<SearchLocationAdapter.ViewHolder>() {
		override fun onCreateViewHolder(
			parent: ViewGroup,
			viewType: Int
		): ViewHolder {
			return ViewHolder(
				ItemLocationHistorySearchBinding.inflate(
					LayoutInflater.from(parent.context),
					parent,
					false
				)
			)
		}

		override fun onBindViewHolder(
			holder: ViewHolder,
			position: Int
		) {
			holder.bind(data[position])
		}

		override fun getItemCount(): Int {
			return data.size
		}

		inner class ViewHolder(private val binding: ItemLocationHistorySearchBinding) :
			RecyclerView.ViewHolder(binding.root) {
			fun bind(bean: SearchLocationBean) {
				binding.apply {
					addressTv.text = bean.address
					tagTv.text = bean.sematicDescription
					root.clickDelay {
						val lat = LatLng(bean.latitude, bean.longitude)
						locateMap(lat)
						markMap(lat)
					}
				}
			}
		}
	}

}
