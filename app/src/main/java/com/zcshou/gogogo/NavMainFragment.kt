package com.zcshou.gogogo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.zcshou.gogogo.databinding.FragmentNavMainBinding
import com.zcshou.gogogo.databinding.ItemLocationHistoryBinding
import com.zcshou.utils.GoUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tech.jour.template.base.ktx.clickDelay
import tech.jour.template.base.ktx.gone
import tech.jour.template.base.ktx.visible
import tech.jour.template.base.utils.toast
import tech.jour.template.common.model.db.LocalLocationBean
import tech.jour.template.common.ui.BaseFragment

@AndroidEntryPoint
class NavMainFragment : BaseFragment<FragmentNavMainBinding, MainViewModel>() {

	override val mViewModel: MainViewModel by viewModels()

	private var cardLocation: LocalLocationBean = LocalLocationBean()

	override fun initView() {
		mBinding.apply {
			fab.clickDelay {
//			findNavController().navigate(R.id.mapActivity)
				findNavController().navigate(R.id.mapFragment)
			}
		}

	}

	override fun initObserve() {
		mViewModel.selectedLocationLivedata.observe(this) {
			mBinding.apply {
				cardLocation = it
				locationDetailTv.text = cardLocation.sematicDescription
				locationProviceTv.text = cardLocation.address
				latitudeTv.text = "经纬度:${cardLocation.latitude}·${cardLocation.longitude}"
			}
		}
		mViewModel.isMockServStart.observe(this) {
			if (it) {
				toast("模拟位置已启动")
				mBinding.apply {
					startMockLocation.text = "停止模拟"
					startMockLocation.clickDelay {
						mViewModel.stopWorker()
					}
				}

			} else {
				mBinding.apply {
					startMockLocation.text = "启动模拟"
					startMockLocation.clickDelay {
						if (!GoUtils.isAllowMockLocation(requireContext())) {
							GoUtils.showEnableMockLocationDialog(requireContext())
							return@clickDelay
						}
						if (GoUtils.isWifiEnabled(requireContext())) {
							GoUtils.showDisableWifiDialog(requireContext())
						}
						if (!GoUtils.isGpsOpened(requireContext())) {
							GoUtils.showEnableGpsDialog(requireContext())
							return@clickDelay
						}
						if (mBinding.locationProviceTv.text.isEmpty()) {
							toast("请选择正确位置")
							findNavController().navigate(R.id.mapFragment)
							return@clickDelay
						}
						mViewModel.startWorker()
					}
				}
			}
		}
	}

	override fun initRequestData() {
		lifecycleScope.launch {
			mViewModel.getHistoryLocation().collectLatest { localLocationBeans ->
				val list = localLocationBeans.reversed().filter { it.address.isNotEmpty() }
				mBinding.apply {
					if (list.isNotEmpty()) {
						mViewModel.selectedLocationLivedata.postValue(list.first())
						recyclerView.adapter = HistoryLocationAdapter(list)
						emptyView.gone()
					} else {
						emptyView.visible()
					}
				}

			}
		}
	}


	inner class HistoryLocationAdapter(private val data: List<LocalLocationBean>) :
		RecyclerView.Adapter<HistoryLocationAdapter.ViewHolder>() {
		override fun onCreateViewHolder(
			parent: ViewGroup,
			viewType: Int
		): ViewHolder {
			return ViewHolder(
				ItemLocationHistoryBinding.inflate(
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

		inner class ViewHolder(private val binding: ItemLocationHistoryBinding) :
			RecyclerView.ViewHolder(binding.root) {
			fun bind(bean: LocalLocationBean) {
				binding.apply {
					LocationText.text = bean.sematicDescription
					latLngText.text = bean.address
					root.clickDelay {
						mViewModel.selectedLocationLivedata.postValue(bean)
					}
					root.setOnLongClickListener {
						mViewModel.deleteHistory(bean)
						true
					}
				}
			}
		}
	}


}