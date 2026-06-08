package com.taibao.app

import android.net.Uri
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.taibao.app.databinding.FragmentNavMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import tech.jour.template.base.ktx.clickDelay
import tech.jour.template.base.ktx.d
import tech.jour.template.base.utils.toast
import tech.jour.template.common.ui.BaseFragment
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class NavMainFragment : BaseFragment<FragmentNavMainBinding, MainViewModel>() {

    override val mViewModel: MainViewModel by activityViewModels()
    private var currentPhotoUri: Uri? = null

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        // content://com.taibao.app.fileProvider/files/camera/IMG_20260608_170319.jpg
        currentPhotoUri.d()
        if (success && currentPhotoUri != null) {
            picUriCallback(currentPhotoUri!!)
        }
    }

    val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            picUriCallback(uri)
        }
    }

    override fun initView() {
        mBinding.apply {
            navMapBtn.clickDelay {
                findNavController().navigate(R.id.navMapFragment)
            }
            cameraFab.setOnClickListener {
                val photoFile = createPhotoFile() ?: return@setOnClickListener
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileProvider",
                    photoFile
                )
                currentPhotoUri = uri
                takePictureLauncher.launch(uri)
            }

            galleryBtn.clickDelay {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        }
    }

    override fun initObserve() {
        mViewModel.selectedLocationLivedata.observe(this) {
            mBinding.apply {
                latitudeEt.setText(it.latitude.toString())
                longitudeEt.setText(it.longitude.toString())
            }
        }
        mViewModel.fakeImagePath.observe(this) {
            if (it.isEmpty()) {
                mBinding.fakePhoto.load(R.drawable.img_empty_holder)
            } else
                mBinding.fakePhoto.load(it)
        }
        mViewModel.isMockServStart.observe(this) {
            if (it) {
                toast("模拟位置启动")
                mBinding.apply {
//                    startMockLocation.text = "停止模拟"
//                    startMockLocation.clickDelay {
//                        mViewModel.stopWorker()
//                    }
                }

            } else {
                mBinding.apply {
//                    startMockLocation.text = "启动模拟"
//                    startMockLocation.clickDelay {
//                        if (!GoUtils.isAllowMockLocation(requireContext())) {
//                            GoUtils.showEnableMockLocationDialog(requireContext())
//                            return@clickDelay
//                        }
//                        if (GoUtils.isWifiEnabled(requireContext())) {
//                            GoUtils.showDisableWifiDialog(requireContext())
//                        }
//                        if (!GoUtils.isGpsOpened(requireContext())) {
//                            GoUtils.showEnableGpsDialog(requireContext())
//                            return@clickDelay
//                        }
//                        if (mBinding.locationProviceTv.text.isEmpty()) {
//                            toast("请选择正确位置")
//                            findNavController().navigate(R.id.mapFragment)
//                            return@clickDelay
//                        }
//                        mViewModel.startWorker()
//                    }
                }
            }
        }

    }

    override fun initRequestData() {
        updateFakeImagePath()
    }

    private fun updateFakeImagePath() {
        mViewModel.fakeImagePath.postValue(FakeCameraManager.getFakeImageFile(requireContext())?.absolutePath)
    }

    private fun createPhotoFile(): File? {
        val dir = File(requireContext().filesDir, "camera")
        dir.mkdirs()
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return File(dir, "IMG_${timestamp}.jpg")
    }

    private fun picUriCallback(uri: Uri) {
        lifecycleScope.launch {
            val localPath = FakeCameraManager.setFakeImageFromUri(requireContext(), uri)
            if (localPath != null) {
                mViewModel.fakeImagePath.postValue(localPath)
            }
        }
    }


}