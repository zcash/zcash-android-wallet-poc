package cash.z.android.wallet.ui.activity

import android.os.Bundle
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

abstract class BaseActivity : DaggerAppCompatActivity(), CoroutineScope {
    private lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() =  job + Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        job = Job()
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}