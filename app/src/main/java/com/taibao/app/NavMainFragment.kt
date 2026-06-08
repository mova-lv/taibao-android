package com.taibao.app

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.ImageUtils
import com.taibao.app.databinding.FragmentNavMainBinding
import com.taibao.app.databinding.ItemLocationHistoryBinding
import com.zcshou.utils.GoUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tech.jour.template.base.ktx.clickDelay
import tech.jour.template.base.ktx.gone
import tech.jour.template.base.ktx.visible
import tech.jour.template.base.utils.toast
import tech.jour.template.common.model.db.LocalLocationBean
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
        if (success && currentPhotoUri != null) {
            mBinding.fakePhoto.load(currentPhotoUri)

//            val bitmap = ImageUtils.getBitmap(currentPhotoUri!!.path)
//            if (bitmap != null) {
//                val bytes = ImageUtils.compressByQuality(bitmap, 1024 * 1024L)
//                if (bytes != null) {
//                    val tempfile = createPhotoFile()
//                    FileIOUtils.writeFileFromBytesByStream(tempfile, bytes)
//                    val fileSize = FileUtils.getFileLength(tempfile?.path)
//                    toast("照片已保存 (${fileSize / 1024}KB)")
//                    mBinding.fakePhoto.load(currentPhotoUri)
//                }
//            }
        }
    }

    override fun initView() {
        mBinding.apply {
            navMapBtn.clickDelay {
//			findNavController().navigate(R.id.mapActivity)
                findNavController().navigate(R.id.navMapFragment)
            }
        }

        mBinding.cameraFab.setOnClickListener {
            val photoFile = createPhotoFile() ?: return@setOnClickListener
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileProvider",
                photoFile
            )
            currentPhotoUri = uri
            takePictureLauncher.launch(uri)
        }
    }

    private fun createPhotoFile(): File? {
        val dir = File(requireContext().filesDir, "camera")
        dir.mkdirs()
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return File(dir, "IMG_${timestamp}.jpg")
    }

    override fun initObserve() {
        mViewModel.selectedLocationLivedata.observe(this) {
            mBinding.apply {
                latitudeEt.setText(it.latitude.toString())
                longitudeEt.setText(it.longitude.toString())
            }
        }
        mViewModel.isMockServStart.observe(this) {
            if (it) {
                toast("模拟位置已启动")
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
    }

}