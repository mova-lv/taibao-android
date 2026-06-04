package com.taibao.app

import androidx.fragment.app.viewModels
import com.taibao.app.databinding.ActivityMainNavBinding
import com.taibao.app.databinding.FragmentNavDevBinding
import dagger.hilt.android.AndroidEntryPoint
import tech.jour.template.base.mvvm.vm.EmptyViewModel
import tech.jour.template.common.ui.BaseFragment

@AndroidEntryPoint
class NavDevFragment : BaseFragment<FragmentNavDevBinding, EmptyViewModel>() {

	override val mViewModel: EmptyViewModel by viewModels()

	override fun initView() {
	}

	override fun initObserve() {
	}

	override fun initRequestData() {
	}


}
