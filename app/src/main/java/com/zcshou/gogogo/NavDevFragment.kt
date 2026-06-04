package com.zcshou.gogogo

import androidx.fragment.app.viewModels
import com.zcshou.gogogo.databinding.ActivityMainNavBinding
import com.zcshou.gogogo.databinding.FragmentNavDevBinding
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