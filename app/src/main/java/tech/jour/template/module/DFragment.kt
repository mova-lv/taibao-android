package tech.jour.template.module

import androidx.fragment.app.viewModels
import com.taibao.app.databinding.ActivityMainNavBinding
import dagger.hilt.android.AndroidEntryPoint
import tech.jour.template.base.mvvm.vm.EmptyViewModel
import tech.jour.template.common.ui.BaseFragment

@AndroidEntryPoint
class DFragment : BaseFragment<ActivityMainNavBinding, EmptyViewModel>() {

	override val mViewModel: EmptyViewModel by viewModels()

	override fun initView() {
	}

	override fun initObserve() {
	}

	override fun initRequestData() {
	}


}
