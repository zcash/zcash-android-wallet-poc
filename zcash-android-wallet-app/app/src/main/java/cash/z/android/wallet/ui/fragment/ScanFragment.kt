package cash.z.android.wallet.ui.fragment

import android.animation.Animator
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.Image
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import cash.z.android.cameraview.CameraView
import cash.z.android.wallet.R
import cash.z.android.wallet.databinding.FragmentScanBinding
import cash.z.android.wallet.extention.Toaster
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import dagger.Module
import dagger.android.ContributesAndroidInjector


/**
 * Fragment for scanning addresss, hopefully.
 */
class ScanFragment : BaseFragment() {

    lateinit var binding: FragmentScanBinding
    var barcodeCallback: BarcodeCallback? = null

    interface BarcodeCallback {
        fun onBarcodeScanned(value: String)
    }

    private val revealCamera = Runnable {
        binding.overlayBarcodeScan.apply {
            val cX = measuredWidth / 2
            val cY = measuredHeight / 2
            ViewAnimationUtils.createCircularReveal(this, cX, cY, 0.0f, cX.toFloat()).start()
            postDelayed({
                val v:View = this
                v.animate().alpha(0.0f).apply { duration = 2400L }.setListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {
                    }

                    override fun onAnimationStart(animation: Animator?) {
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        binding.overlayBarcodeScan.visibility = View.GONE
                    }
                    override fun onAnimationCancel(animation: Animator?) {
                        binding.overlayBarcodeScan.visibility = View.GONE
                    }
                })
            },500L)
        }
    }

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
        binding.overlayBarcodeScan.post(revealCamera)
        System.err.println("camoorah : onResume ScanFragment")
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
            view!!.postDelayed({
                onStartCamera()
            },2000L) // TODO: remove this temp hack to sidestep crash when permissions were not available
        }
    }

    private fun onStartCamera() {
        with(binding.cameraView) {
            // workaround race conditions with google play services downloading the binaries for Firebase Vision APIs
            postDelayed({
                firebaseCallback = PoCallback()
                start()
            }, 1000L)
        }
    }

    inner class PoCallback : CameraView.FirebaseCallback {
        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
            .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
            .build()
        val barcodeDetector = FirebaseVision.getInstance().getVisionBarcodeDetector(options)
        var cameraId = getBackCameraId()

        private fun getBackCameraId(): String {
            val manager = mainActivity.getSystemService(Context.CAMERA_SERVICE) as CameraManager

            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)
                val cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING)!!
                if (cOrientation == CameraCharacteristics.LENS_FACING_BACK) return cameraId
            }
            throw IllegalArgumentException("no rear-facing camera found!")
        }

        override fun onImageAvailable(image: Image) {
            try {
                System.err.println("camoorah : onImageAvailable: $image  width: ${image.width}  height: ${image.height}")
                var firebaseImage =
                    FirebaseVisionImage.fromMediaImage(image, getRotationCompensation(cameraId, mainActivity))
                barcodeDetector
                    .detectInImage(firebaseImage)
                    .addOnSuccessListener { results ->
                        if (results.isNotEmpty()) {
                            val barcode = results[0]
                            val value = barcode.rawValue
                            onScanSuccess(value!!)
                            // TODO: highlight the barcode
                            var bounds = barcode.boundingBox
                            var corners = barcode.cornerPoints
                            binding.cameraView.setBarcode(barcode)
                        }
                    }
            } catch (t: Throwable) {
                System.err.println("camoorah : error while processing onImageAvailable: $t\n\tcaused by: ${t.cause}")
            }
        }
    }

    private var pendingSuccess = false
    private fun onScanSuccess(value: String) {
        binding.cameraView.stop()
        if (!pendingSuccess) {
            pendingSuccess = true
            binding.cameraView.post {
                barcodeCallback?.onBarcodeScanned(value)
            }
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
