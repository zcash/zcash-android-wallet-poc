package cash.z.android.wallet.ui.fragment

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.IdRes
import cash.z.android.wallet.ui.presenter.ProgressPresenter
import cash.z.android.wallet.ui.util.AnimatorCompleteListener
import kotlinx.coroutines.launch

abstract class ProgressFragment(@IdRes private val progressBarId: Int) : BaseFragment(),
    ProgressPresenter.ProgressView {

    private lateinit var progressPresenter: ProgressPresenter
    private lateinit var progressBar: ProgressBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBar = view.findViewById(progressBarId)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        progressPresenter = ProgressPresenter(this, mainActivity.synchronizer)
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
            progressBar.animate().alpha(0.0f).apply {
                duration = 250L
                setListener(AnimatorCompleteListener {
                    progressBar.visibility = View.GONE
                })
            }
        }
        progressBar.progress = progress
    }

    open fun onProgressComplete() {}
}
