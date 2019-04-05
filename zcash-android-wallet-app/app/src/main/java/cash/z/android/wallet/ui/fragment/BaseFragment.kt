package cash.z.android.wallet.ui.fragment

import android.os.Bundle
import cash.z.android.wallet.ui.activity.MainActivity
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext

abstract class BaseFragment : DaggerFragment(), CoroutineScope {

    private lateinit var job: Job

    val mainActivity: MainActivity? get() = activity as MainActivity?

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        job = Job()
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

}
