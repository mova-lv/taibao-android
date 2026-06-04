package com.zcshou.gogogo

import android.content.Intent
import android.provider.Settings
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.zcshou.gogogo.databinding.ActivityMainNavBinding
import com.zcshou.utils.GoUtils
import dagger.hilt.android.AndroidEntryPoint
import tech.jour.template.base.mvvm.vm.EmptyViewModel
import tech.jour.template.base.utils.toast
import tech.jour.template.common.ui.BaseActivity

@AndroidEntryPoint
class NavMainActivity : BaseActivity<ActivityMainNavBinding, EmptyViewModel>() {

	override val mViewModel by viewModels<EmptyViewModel>()

	private lateinit var navController: NavController
	private lateinit var appBarConfiguration: AppBarConfiguration

	companion object {
		const val LAT_MSG_ID: String = "LAT_VALUE"
		const val LNG_MSG_ID: String = "LNG_VALUE"
		const val ALT_MSG_ID: String = "ALT_VALUE"
	}

	override fun initView() {
		setSupportActionBar(mBinding.toolbar)

		val host: NavHostFragment = supportFragmentManager
			.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
		navController = host.navController

		appBarConfiguration = AppBarConfiguration(
			setOf(
				R.id.navMainFragment,
//				R.id.nav_settings
			),
			mBinding.drawerLayout
		)
		setupActionBarWithNavController(navController, appBarConfiguration)
		mBinding.navView.setupWithNavController(navController)

		navController.addOnDestinationChangedListener { controller, destination, arguments ->
			when (destination.id) {
				R.id.nav_dev -> {
					try {
						val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
						startActivity(intent)
					} catch (e: Exception) {
						GoUtils.DisplayToast(
							this@NavMainActivity,
							resources.getString(R.string.app_error_dev)
						)
					}
				}
				else -> {}
			}
		}

	}

	override fun initObserve() {
	}

	override fun initRequestData() {
	}

	override fun onSupportNavigateUp(): Boolean {
		return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
	}
}