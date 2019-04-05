package cash.z.android.wallet.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.databinding.DataBindingUtil
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import cash.z.android.wallet.R
import cash.z.android.wallet.databinding.FragmentFirstrunBinding
import dagger.Module
import dagger.android.ContributesAndroidInjector

class FirstrunFragment : ProgressFragment(R.id.progress_firstrun), Transition.TransitionListener {

    private lateinit var binding: FragmentFirstrunBinding

    //
    // Lifecycle
    //

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setupSharedElementTransitions()
        return DataBindingUtil.inflate<FragmentFirstrunBinding>(
            inflater, R.layout.fragment_firstrun, container, false
        ).let {
            binding = it
            it.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        binding.buttonNext.setOnClickListener {
            mainActivity?.navController?.navigate(
                R.id.action_firstrun_fragment_to_sync_fragment,
                null,
                null,
                null
            )
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (view?.parent as? ViewGroup)?.doOnPreDraw {
            startPostponedEnterTransition()
        }
    }

    override fun onResume() {
        super.onResume()
        mainActivity?.setDrawerLocked(true)
        mainActivity?.setToolbarShown(false)
    }

    private fun setupSharedElementTransitions() {
        TransitionInflater.from(mainActivity).inflateTransition(R.transition.transition_zec_sent).apply {
            duration = 250L
            addListener(this@FirstrunFragment)
            this@FirstrunFragment.sharedElementEnterTransition = this
            this@FirstrunFragment.sharedElementReturnTransition = this
        }
    }

    override fun showProgress(progress: Int) {
        super.showProgress(progress)
        binding.textProgressFirstrun.text = getProgressText(progress)

    }

    override fun onProgressComplete() {
        super.onProgressComplete()
        binding.textProgressFirstrun.visibility = View.GONE
    }

    override fun onTransitionStart(transition: Transition) {
        binding.buttonNext.alpha = 0f
    }

    override fun onTransitionEnd(transition: Transition) {
        binding.buttonNext.animate().apply {
            duration = 300L
        }.alpha(1.0f)
        binding.textProgressFirstrun.animate().apply {
            duration = 300L
        }.alpha(1.0f)
    }

    override fun onTransitionResume(transition: Transition) {}
    override fun onTransitionPause(transition: Transition) {}
    override fun onTransitionCancel(transition: Transition) {}
}

@Module
abstract class FirstrunFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeFirstrunFragment(): FirstrunFragment
}