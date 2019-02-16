package cash.z.android.wallet.ui.fragment

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import cash.z.android.wallet.R
import cash.z.android.wallet.databinding.FragmentScanBinding
import cash.z.android.wallet.ui.activity.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Fragment for scanning addresss, hopefully.
 */
class ScanFragment : BaseFragment() {

    lateinit var binding: FragmentScanBinding

//    private var cameraSource: CameraSource? = null

    private val requiredPermissions: Array<String?>
        get() {
            return try {
                val info = mainActivity.packageManager
                    .getPackageInfo(mainActivity.packageName, PackageManager.GET_PERMISSIONS)
                val ps = info.requestedPermissions
                if (ps != null && ps.isNotEmpty()) {
                    ps
                } else {
                    arrayOfNulls(0)
                }
            } catch (e: Exception) {
                arrayOfNulls(0)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return DataBindingUtil.inflate<FragmentScanBinding>(
            inflater, R.layout.fragment_scan, container, false
        ).let {
            binding = it
            it.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).let { mainActivity ->
            mainActivity.setSupportActionBar(view.findViewById(R.id.toolbar))
            mainActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            mainActivity.supportActionBar?.setTitle(R.string.destination_title_send)
        }
//        binding.previewCameraSource.doOnLayout {
//            if (allPermissionsGranted()) {
//                createCameraSource(it.width, it.height)
//            } else {
//                getRuntimePermissions()
//            }
//        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if(!allPermissionsGranted()) getRuntimePermissions()


//        sendPresenter = SendPresenter(this, mainActivity.synchronizer)
    }

    override fun onResume() {
        super.onResume()
        if(allPermissionsGranted()) onStartCamera()
//        launch {
//            sendPresenter.start()
//        }
//        startCameraSource()
    }

    override fun onPause() {
        binding.cameraView.stop()
        super.onPause()
//        sendPresenter.stop()
//        binding.previewCameraSource?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
//        cameraSource?.release()
    }

    /* Camera */
//    private fun createCameraSource(width: Int, height: Int) {
//        Toaster.short("w: $width  h: $height")
//        // If there's no existing cameraSource, create one.
//        if (cameraSource == null) {
//            cameraSource = CameraSource(mainActivity, binding.graphicOverlay)
//        }
//
//        try {
//            cameraSource?.setMachineLearningFrameProcessor(BarcodeScanningProcessor())
//        } catch (e: FirebaseMLException) {
//            Log.e("temporaryBehavior", "can not create camera source")
//        }
//    }

    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
//    private fun startCameraSource() {
//        cameraSource?.let {
//            try {
//                binding.previewCameraSource?.start(cameraSource!!, binding.graphicOverlay)
//            } catch (e: IOException) {
//                Log.e("temporaryBehavior", "Unable to start camera source.", e)
//                cameraSource?.release()
//                cameraSource = null
//            }
//        }
//    }

    /* Permissions */

    private fun allPermissionsGranted(): Boolean {
        for (permission in requiredPermissions) {
            if (!isPermissionGranted(mainActivity, permission!!)) {
                return false
            }
        }
        return true
    }

    private fun getRuntimePermissions() {
        val allNeededPermissions = arrayListOf<String>()
        for (permission in requiredPermissions) {
            if (!isPermissionGranted(mainActivity, permission!!)) {
                allNeededPermissions.add(permission)
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            requestPermissions(allNeededPermissions.toTypedArray(), CAMERA_PERMISSION_REQUEST)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (allPermissionsGranted()) {
            onStartCamera()
        }
    }

    private fun onStartCamera() {
        with(binding.cameraView) {
            postDelayed({
                start()
            }, 1500L)
        }
    }

    companion object {
        // TODO: continue doing permissions here in a more specific, less general way
        private const val CAMERA_PERMISSION_REQUEST = 1001

        private fun isPermissionGranted(context: Context, permission: String): Boolean {
            return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
}


@Module
abstract class ScanFragmentModule {
    @ContributesAndroidInjector
    abstract fun contributeScanFragment(): ScanFragment
}
