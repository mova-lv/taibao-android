package com.taibao.app

import androidx.fragment.app.activityViewModels
import com.taibao.app.databinding.FragmentNavToolsBinding
import dagger.hilt.android.AndroidEntryPoint
import tech.jour.template.common.ui.BaseFragment

@AndroidEntryPoint
class NavToolsFragment : BaseFragment<FragmentNavToolsBinding, MainViewModel>() {

    override val mViewModel: MainViewModel by activityViewModels()

    override fun initView() {
        mBinding.apply {
        }
    }

    override fun initObserve() {
    }

    override fun initRequestData() {
    }

}