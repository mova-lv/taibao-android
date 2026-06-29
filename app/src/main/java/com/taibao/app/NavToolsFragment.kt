package com.taibao.app

import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.taibao.app.databinding.FragmentNavToolsBinding
import com.taibao.app.utils.MapUtils
import dagger.hilt.android.AndroidEntryPoint
import tech.jour.template.base.ktx.clickDelay
import tech.jour.template.base.utils.MMKVUtils
import tech.jour.template.common.ui.BaseFragment

@AndroidEntryPoint
class NavToolsFragment : BaseFragment<FragmentNavToolsBinding, MainViewModel>() {

    override val mViewModel: MainViewModel by activityViewModels()

    private companion object {
        const val KEY_CACHE_LNG = "nav_tools_cache_lng"
        const val KEY_CACHE_LAT = "nav_tools_cache_lat"
    }

    override fun initView() {
        mBinding.apply {
            resultBtn.clickDelay {
                val lngStr = longitudeEt.text?.toString()?.trim() ?: ""
                val latStr = latitudeEt.text?.toString()?.trim() ?: ""

                if (lngStr.isEmpty() || latStr.isEmpty()) {
                    Toast.makeText(requireContext(), "请输入完整的经纬度", Toast.LENGTH_SHORT).show()
                    return@clickDelay
                }

                val lng = lngStr.toDoubleOrNull()
                val lat = latStr.toDoubleOrNull()

                if (lng == null || lat == null) {
                    Toast.makeText(requireContext(), "经纬度格式不正确", Toast.LENGTH_SHORT).show()
                    return@clickDelay
                }

                // 缓存本次输入的经纬度
                MMKVUtils.putDouble(KEY_CACHE_LNG, lng)
                MMKVUtils.putDouble(KEY_CACHE_LAT, lat)

                val gcjResult = MapUtils.gcj02towgs84(lng, lat)
                resultGcjLngTv.text = "WGS 经度: %.6f".format(gcjResult[0])
                resultGcjLatTv.text = "WGS 纬度: %.6f".format(gcjResult[1])
            }
        }
    }

    override fun initObserve() {
    }

    override fun initRequestData() {
        // 恢复上次输入的经纬度
        val cachedLng = MMKVUtils.getDouble(KEY_CACHE_LNG, 0.0) ?: 0.0
        val cachedLat = MMKVUtils.getDouble(KEY_CACHE_LAT, 0.0) ?: 0.0
        if (cachedLng != 0.0 || cachedLat != 0.0) {
            mBinding.longitudeEt.setText(cachedLng.toString())
            mBinding.latitudeEt.setText(cachedLat.toString())
        }
    }

}