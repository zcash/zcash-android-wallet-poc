package cash.z.android.wallet.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavOptions
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.transition.TransitionInflater
import cash.z.android.wallet.BuildConfig
import cash.z.android.wallet.R
import cash.z.android.wallet.databinding.FragmentWelcomeBinding
import dagger.Module
import dagger.android.ContributesAndroidInjector
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


class WelcomeFragment : ProgressFragment(R.id.progress_welcome) {

    private lateinit var binding: FragmentWelcomeBinding

    //
    // Lifecycle
    //

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setupSharedElementTransitions()
        return DataBindingUtil.inflate<FragmentWelcomeBinding>(
            inflater, R.layout.fragment_welcome, container, false
        ).let {
            binding = it
            it.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val network = if (resources.getBoolean(R.bool.is_testnet)) "Testnet 2.0.1" else "Mainnet 2.0.1"
        var buildInfo = "PoC v${BuildConfig.VERSION_NAME} $network\nZcash Company - For demo purposes only"
        binding.textWelcomeBuildInfo.text = buildInfo
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view!!.postDelayed({
            launch {
                onNext()
            }
        }, 5000L)

//        this.setExitSharedElementCallback(object : SharedElementCallback() {
//            override fun onCaptureSharedElementSnapshot(
//                sharedElement: View,
//                viewToGlobalMatrix: Matrix,
//                screenBounds: RectF
//            ): Parcelable? {
//                val width = Math.round(screenBounds.width())
//                val height = Math.round(screenBounds.height())
//                var bitmap: Bitmap? = null
//                if (width > 0 && height > 0) {
//                    val matrix = Matrix()
//                    matrix.set(viewToGlobalMatrix)
//                    matrix.postTranslate(screenBounds.left, screenBounds.top)
//                    bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//                    val canvas = Canvas(bitmap)
//                    canvas.concat(matrix)
//                    sharedElement.draw(canvas)
//                }
//                return bitmap
//            }
//        })
    }

    override fun onResume() {
        super.onResume()
        mainActivity?.setDrawerLocked(true)
        mainActivity?.setToolbarShown(false)
    }

    private fun setupSharedElementTransitions() {
        TransitionInflater.from(mainActivity).inflateTransition(R.transition.transition_zec_sent).apply {
            duration = 2500L
            this@WelcomeFragment.sharedElementReturnTransition = this
        }
    }
    private suspend fun onNext() = coroutineScope {
        if (mainActivity != null) {
            val isFirstRun = mainActivity!!.synchronizer.isFirstRun()
            val destination =
                if (isFirstRun) R.id.action_welcome_fragment_to_firstrun_fragment
                else R.id.action_welcome_fragment_to_sync_fragment

            //        var extras = with(binding) {
            //            listOf(progressWelcome, textProgressWelcome)
            //                .map { it to it.transitionName }
            //                .let { FragmentNavigatorExtras(*it.toTypedArray()) }
            //        }
            val extras = FragmentNavigatorExtras(
                binding.progressWelcome to binding.progressWelcome.transitionName
            )

            mainActivity?.navController?.navigate(
                destination,
                null,
                NavOptions.Builder().setPopUpTo(R.id.mobile_navigation, true).build(),
                extras
            )
        }
    }

}

@Module
abstract class WelcomeFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeWelcomeFragment(): WelcomeFragment

}