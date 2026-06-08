package com.taibao.app

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import androidx.activity.viewModels
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.permissionx.guolindev.PermissionX
import com.taibao.app.databinding.ActivityWelcomeBinding
import tech.jour.template.base.ktx.clickDelay
import tech.jour.template.base.ktx.d
import tech.jour.template.base.utils.toast
import tech.jour.template.common.ui.BaseActivity
import tech.jour.template.module.DViewModel

class WelcomeActivity : BaseActivity<ActivityWelcomeBinding, DViewModel>() {
    private lateinit var preferences: SharedPreferences
    private val KEY_CACHED_PASSWORD = "KEY_CACHED_PASSWORD"
    override val mViewModel by viewModels<DViewModel>()

    override fun initView() {
        PreferenceManager.setDefaultValues(this, R.xml.preferences_main, false)
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        mBinding.startButton.clickDelay {
            startMain()
        }
    }

    override fun initObserve() {
    }

    override fun initRequestData() {
        checkDefaultPermissions()
    }

    private fun startMain() {
        val expectedPassword = DeviceAuthUtil.generatePassword(this)
        "AndroidID: ${DeviceAuthUtil.getAndroidId(this)}, Password: $expectedPassword".d()

        // 检查本地缓存的密码是否仍有效
        val cachedPassword = preferences.getString(KEY_CACHED_PASSWORD, null)
        if (cachedPassword == expectedPassword) {
            navigateToMain()
            return
        }

        // 缓存无效或无缓存，弹出验证对话框
        val inputView = layoutInflater.inflate(R.layout.dialog_device_auth, null)
        val passwordInput =
            inputView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.passwordInput)

        MaterialAlertDialogBuilder(this)
            .setTitle("设备验证")
            .setMessage("请输入设备验证码以继续使用\n当前设备ID ${DeviceAuthUtil.getAndroidId(this)}")
            .setView(inputView)
            .setCancelable(false)
            .setPositiveButton("验证") { _, _ ->
                val input = passwordInput.text?.toString()?.trim() ?: ""
                if (DeviceAuthUtil.validatePassword(this@WelcomeActivity, input)) {
                    // 密码正确，缓存到本地，下次直接跳过验证
                    preferences.edit().putString(KEY_CACHED_PASSWORD, expectedPassword).apply()
                    navigateToMain()
                } else {
                    toast("验证码错误")
                }
            }
            .show()
    }

    private fun navigateToMain() {
        startActivity(
            Intent(this@WelcomeActivity, NavMainActivity::class.java)
        )
        this@WelcomeActivity.finish()
    }

    private fun checkDefaultPermissions() {
        val plist = listOf(
            PermissionX.permission.POST_NOTIFICATIONS,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
        PermissionX.init(this).permissions(plist).onExplainRequestReason { scope, deniedList ->
            val message = "需要您同意以下权限才能正常使用"
            scope.showRequestReasonDialog(deniedList, message, "允许", "拒绝")
        }.request { allGranted, grantedList, deniedList ->
            if (allGranted) {
                startMain()
            } else {
                toast("您拒绝了如下权限：$deniedList")
            }
        }
    }
}