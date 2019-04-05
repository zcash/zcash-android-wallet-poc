package cash.z.android.wallet.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavOptions
import androidx.transition.TransitionInflater
import cash.z.android.wallet.R
import cash.z.android.wallet.databinding.FragmentSyncBinding
import cash.z.android.wallet.extention.alert
import cash.z.android.wallet.extention.showOk
import cash.z.android.wallet.ui.presenter.Presenter
import cash.z.android.wallet.ui.presenter.ProgressPresenter
import cash.z.wallet.sdk.data.Synchronizer
import cash.z.wallet.sdk.data.twig
import com.google.android.material.snackbar.Snackbar
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

class SyncFragment : ProgressFragment(R.id.progress_sync) {


    private lateinit var binding: FragmentSyncBinding

    //
    // Lifecycle
    //

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setupSharedElementTransitions()
        return DataBindingUtil.inflate<FragmentSyncBinding>(
            inflater, R.layout.fragment_sync, container, false
        ).let {
            binding = it
            it.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        binding.buttonNext.setOnClickListener {
            mainActivity?.navController?.navigate(R.id.action_sync_fragment_to_home_fragment,
                null,
                NavOptions.Builder().setPopUpTo(R.id.mobile_navigation, true).build(),
                null
            )
        }
        binding.progressSync.visibility = View.INVISIBLE
        binding.textProgressSync.visibility = View.INVISIBLE
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (view?.parent as? ViewGroup)?.doOnPreDraw {
            startPostponedEnterTransition()
        }
        synchronizer.onSynchronizerErrorListener = ::onSynchronizerError
    }

    override fun onResume() {
        super.onResume()
        mainActivity?.setDrawerLocked(true)
        mainActivity?.setToolbarShown(true)
    }

    private fun setupSharedElementTransitions() {
        TransitionInflater.from(mainActivity).inflateTransition(R.transition.transition_zec_sent).apply {
            duration = 250L
//            addListener(this@SyncFragment)
            this@SyncFragment.sharedElementEnterTransition = this
            this@SyncFragment.sharedElementReturnTransition = this
        }
    }

    override fun showProgress(progress: Int) {
        binding.textProgressSync.text = getProgressText(progress)
        binding.textProgressSync.visibility = View.VISIBLE
        super.showProgress(progress)
    }

    override fun onProgressComplete() {
        super.onProgressComplete()
        binding.textProgressSync.visibility = View.GONE
        with (binding.buttonNext) {
            isEnabled = true
            alpha = 0.3f
            animate().alpha(1.0f).duration = 300L
            text = "Start"
        }
    }

    fun onSynchronizerError(error: Throwable?): Boolean {
        context?.alert(
            message = "WARNING: A critical error has occurred and " +
                    "this app will not function properly until that is corrected!",
            positiveButtonResId = R.string.ignore,
            negativeButtonResId = R.string.details,
            negativeAction = { context?.alert("Synchronization error:\n\n$error") }
        )
        return false
    }

}

@Module
abstract class SyncFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeSyncFragment(): SyncFragment

}