package com.taibao.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.taibao.app.databinding.FragmentNavMapBinding
import com.taibao.app.databinding.ItemLocationHistoryBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tech.jour.template.base.ktx.clickDelay
import tech.jour.template.base.ktx.gone
import tech.jour.template.base.ktx.visible
import tech.jour.template.common.model.db.LocalLocationBean
import tech.jour.template.common.ui.BaseFragment

@AndroidEntryPoint
class NavMapFragment : BaseFragment<FragmentNavMapBinding, MainViewModel>() {

    override val mViewModel: MainViewModel by activityViewModels()

    private var cardLocation: LocalLocationBean = LocalLocationBean()


    override fun initView() {
        mBinding.apply {
            fab.clickDelay {
                findNavController().navigate(R.id.mapFragment)
            }
            startMockLocation.clickDelay {
                findNavController().navigateUp()
            }
        }

    }

    override fun initObserve() {
        mViewModel.selectedLocationLivedata.observe(this) {
            mBinding.apply {
                cardLocation = it
                locationDetailTv.text = cardLocation.sematicDescription
                locationProviceTv.text = cardLocation.address
                latitudeTv.text = "经纬度: ${cardLocation.latitude}  ·  ${cardLocation.longitude}"
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