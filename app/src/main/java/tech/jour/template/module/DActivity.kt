package tech.jour.template.module

import androidx.activity.viewModels
import com.zcshou.gogogo.databinding.ActivityMainNavBinding
import dagger.hilt.android.AndroidEntryPoint
import tech.jour.template.base.mvvm.vm.EmptyViewModel
import tech.jour.template.common.ui.BaseActivity

@AndroidEntryPoint
class DActivity : BaseActivity<ActivityMainNavBinding, EmptyViewModel>() {

	override val mViewModel by viewModels<EmptyViewModel>()

	override fun initView() {

	}

	override fun initObserve() {
	}


	override fun initRequestData() {
	}

}