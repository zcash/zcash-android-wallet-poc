package cash.z.android.wallet.ui.fragment

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import cash.z.android.wallet.R
import cash.z.android.wallet.ui.activity.MainActivity
import cash.z.wallet.sdk.jni.JniConverter
import com.leinardi.android.speeddial.SpeedDialActionItem
import kotlinx.android.synthetic.main.fragment_home.*
import com.leinardi.android.speeddial.SpeedDialView
import android.widget.Toast


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [HomeFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null

    var converter: JniConverter = JniConverter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).setSupportActionBar(toolbar)
        (activity as MainActivity).setupNavigation()
        (activity as MainActivity).supportActionBar?.setTitle(R.string.destination_title_home)

        val seed = byteArrayOf(0x77, 0x78, 0x79)
        val result = converter.getAddress(seed)
        text_wallet_message.text = "Your address:\n$result"
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initFab()
    }

    private fun initFab() {
        val theme = activity?.theme
        val speedDial = sd_fab
        speedDial.addActionItem(
            SpeedDialActionItem.Builder(R.id.fab_request, R.drawable.ic_receipt_24dp)
                .setFabBackgroundColor(ResourcesCompat.getColor(resources, R.color.icon_request, theme))
                .setFabImageTintColor(ResourcesCompat.getColor(resources, R.color.zcashWhite, theme))
                .setLabel(getString(R.string.destination_menu_label_request))
                .setLabelClickable(true)
                .create()
        )
        speedDial.addActionItem(
            SpeedDialActionItem.Builder(R.id.fab_receive, R.drawable.ic_qrcode_24dp)
                .setFabBackgroundColor(ResourcesCompat.getColor(resources, R.color.icon_receive, theme))
                .setFabImageTintColor(ResourcesCompat.getColor(resources, R.color.zcashWhite, theme))
                .setLabel(getString(R.string.destination_menu_label_receive))
                .setLabelClickable(true)
                .create()
        )
        speedDial.addActionItem(
            SpeedDialActionItem.Builder(R.id.fab_send, R.drawable.ic_menu_send)
                .setFabBackgroundColor(ResourcesCompat.getColor(resources, R.color.icon_send, theme))
                .setFabImageTintColor(ResourcesCompat.getColor(resources, R.color.zcashWhite, theme))
                .setLabel(getString(R.string.destination_menu_label_send))
                .setLabelClickable(true)
                .create()
        )

        val nav = (activity as MainActivity).navController
        speedDial.setOnActionSelectedListener { item ->
            when (item.id) {
                R.id.fab_send -> {
                    nav.navigate(R.id.nav_send_fragment)
                }
                R.id.fab_receive -> {
                    nav.navigate(R.id.nav_receive_fragment)
                }
                R.id.fab_request -> {
                    nav.navigate(R.id.nav_request_fragment)
                }
                else -> {
                    // TODO: do we need an else
                }
            }
            false
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
