package cash.z.android.wallet.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cash.z.android.qrecycler.QRecycler
import cash.z.android.wallet.R
import cash.z.android.wallet.ui.activity.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_receive.*
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ReceiveFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ReceiveFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class ReceiveFragment : BaseFragment() {

    @Inject
    lateinit var qrecycler: QRecycler

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_receive, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).setSupportActionBar(toolbar)
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        qrecycler.load("https://z.cash").into(receive_qr_code)
    }

}

@Module
abstract class ReceiveFragmentModule {
    @ContributesAndroidInjector
    abstract fun contributeReceiveFragment(): ReceiveFragment
}
