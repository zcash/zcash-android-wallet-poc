package cash.z.android.wallet.ui.fragment

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.IdRes
import cash.z.android.wallet.ui.presenter.ProgressPresenter
import cash.z.wallet.sdk.data.Synchronizer
import kotlinx.coroutines.launch
import javax.inject.Inject

abstract class ProgressFragment(
    @IdRes private val progressBarId: Int
) : BaseFragment(),
    ProgressPresenter.ProgressView {

    @Inject
    protected lateinit var synchronizer: Synchronizer

    protected lateinit var progressPresenter: ProgressPresenter
    private lateinit var progressBar: ProgressBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBar = view.findViewById(progressBarId)
//        progressBar.visibility = View.INVISIBLE
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        progressPresenter = ProgressPresenter(this, synchronizer)
    }

    override fun onResume() {
        super.onResume()
        launch {
            progressPresenter.start()
        }
    }

    override fun onPause() {
        super.onPause()
        progressPresenter.stop()
    }

    override fun showProgress(progress: Int) {
        if (progress >= 100) {
            onProgressComplete()
//            progressBar.animate().(0.0f).apply {
//                duration = 250L
//                setListener(AnimatorCompleteListener {
                    progressBar.visibility = View.GONE
//                })
//            }
        } else if (progress > 0 && progressBar.visibility != View.VISIBLE) {
            progressBar.visibility = View.VISIBLE
        }
        progressBar.progress = progress
    }


    // TODO: replace this quick and dirty logic with something permanent
    open fun getProgressText(progress: Int): String {
        if (mainActivity == null) return ""
        // cycle twice
        val factor = 100 / (mainActivity!!.loadMessages.size * 2)
        val index = (progress/factor).rem(mainActivity!!.loadMessages.size)
        var message = "$progress% ${mainActivity?.nextLoadMessage(index)}"
        if (progress > 98) message = "Done!"
        if (progress >= 50) message = message.replace("Zooko", "Zooko AGAIN", true).replace("Learning to spell", "Double-checking the spelling of").replace("the kool", "MORE kool", true).replace("Making the sausage", "Getting a little hangry by now!", true)
        return message
    }

    open fun onProgressComplete() {}
}
