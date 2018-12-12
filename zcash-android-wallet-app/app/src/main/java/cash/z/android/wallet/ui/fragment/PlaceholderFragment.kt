package cash.z.android.wallet.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cash.z.android.wallet.R
import cash.z.android.wallet.ui.activity.MainActivity


/**
 * Fragment for sending Zcash.
 *
 */
open class PlaceholderFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_placeholder, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).let { mainActivity ->
            mainActivity.setSupportActionBar(view.findViewById(R.id.toolbar))
            mainActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            mainActivity.supportActionBar?.setTitle(R.string.destination_title_placeholder)
        }
    }


}
