package com.zcshou.gogogo

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.CheckBox
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.permissionx.guolindev.PermissionX
import com.zcshou.gogogo.databinding.ActivityWelcomeBinding
import tech.jour.template.base.ktx.clickDelay
import tech.jour.template.base.utils.toast
import tech.jour.template.common.ui.BaseActivity
import tech.jour.template.module.DViewModel

class WelcomeActivity : BaseActivity<ActivityWelcomeBinding, DViewModel>() {
	private lateinit var checkBox: CheckBox
	private var mAgreement: Boolean = false
	private var mPrivacy: Boolean = false
	private lateinit var preferences: SharedPreferences

	companion object {
		private const val KEY_ACCEPT_AGREEMENT = "KEY_ACCEPT_AGREEMENT"
		private const val KEY_ACCEPT_PRIVACY = "KEY_ACCEPT_PRIVACY"
	}

	override val mViewModel by viewModels<DViewModel>()

	override fun initView() {
		// 生成默认参数的值（一定要尽可能早的调用，因为后续有些界面可能需要使用参数）
		PreferenceManager.setDefaultValues(this, R.xml.preferences_main, false)
		preferences = getSharedPreferences(KEY_ACCEPT_AGREEMENT, MODE_PRIVATE)
		mBinding.startButton.clickDelay {
			startMain()
//			startMainActivity()
		}
		checkBox = mBinding.checkAgreement
		mPrivacy = preferences.getBoolean(KEY_ACCEPT_PRIVACY, false)
		mAgreement = preferences.getBoolean(KEY_ACCEPT_AGREEMENT, false)

	}

	override fun initObserve() {
	}

	override fun initRequestData() {
		checkAgreementAndPrivacy()
	}


	private fun startMain() {
		startActivity(
			Intent(
				this@WelcomeActivity, NavMainActivity::class.java
			)
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

	private fun doAcceptation() {
		checkBox.isChecked = mAgreement && mPrivacy
		preferences.edit() {
			putBoolean(KEY_ACCEPT_AGREEMENT, mAgreement)
			putBoolean(KEY_ACCEPT_PRIVACY, mPrivacy)
		}
	}

	private fun showAgreementDialog() {
		val ssb = SpannableStringBuilder()
		ssb.append(resources.getString(R.string.app_agreement_content))
		MaterialAlertDialogBuilder(this).setTitle("用户协议").setMessage(ssb)
			.setNegativeButton("取消", null)
			.setPositiveButton("同意并继续") { dialog, which ->
				mAgreement = true
				doAcceptation()
			}.show()
	}

	private fun showPrivacyDialog() {
		val ssb = SpannableStringBuilder()
		ssb.append(resources.getString(R.string.app_privacy_content))
		MaterialAlertDialogBuilder(this).setTitle("隐私政策").setMessage(ssb)
			.setNegativeButton("取消", null)
			.setPositiveButton("同意并继续") { dialog, which ->
				mPrivacy = true
				doAcceptation()
			}.show()

	}

	@SuppressLint("ClickableViewAccessibility")
	private fun checkAgreementAndPrivacy() {
		// 拦截 CheckBox 的点击事件
		checkBox.setOnTouchListener(OnTouchListener { v: View?, event: MotionEvent ->
			if (v is TextView) {
				val text = v
				val method = text.movementMethod
				if (method != null && text.text is Spannable && event.action == MotionEvent.ACTION_UP) {
					if (method.onTouchEvent(text, text.text as Spannable, event)) {
						event.action = MotionEvent.ACTION_CANCEL
					}
				}
			}
			false
		})
		checkBox.setOnCheckedChangeListener { btn, checked ->
			if (checked) {
				checkDefaultPermissions()
			}
		}
		checkBox.text = getSpannableStringBuilder(getString(R.string.app_agreement_privacy))
		checkBox.movementMethod = LinkMovementMethod.getInstance()

		if (mPrivacy && mAgreement) {
			checkBox.setChecked(true)
//			checkDefaultPermissions()
//			startMain()
		} else {
			checkBox.setChecked(false)
		}
	}

	private fun getSpannableStringBuilder(str: String): SpannableStringBuilder {
		val builder = SpannableStringBuilder(str)
		val agreement_start = str.indexOf("《")
		val agreement_end = str.indexOf("》") + 1
		val privacy_start = str.indexOf("《", agreement_end)
		val privacy_end = str.indexOf("》", agreement_end) + 1

		setClickSpan(builder, agreement_start, agreement_end, ::showAgreementDialog)
		setClickSpan(builder, privacy_start, privacy_end, ::showPrivacyDialog)
		return builder
	}

	private fun setClickSpan(
		builder: SpannableStringBuilder, startIndex: Int, endIndex: Int, function: () -> Unit
	) {
		val clickSpanAgreement: ClickableSpan = object : ClickableSpan() {
			override fun onClick(widget: View) {
				function()
			}

			override fun updateDrawState(ds: TextPaint) {
				ds.color = resources.getColor(
					R.color.colorPrimary, this@WelcomeActivity.theme
				)
				ds.isUnderlineText = false
			}
		}
		builder.setSpan(
			clickSpanAgreement, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
		)
	}

}
