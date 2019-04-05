package cash.z.android.wallet.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import cash.z.android.wallet.R
import cash.z.android.wallet.databinding.IncludeHomeHeaderBinding

class HomeHeaderEmptyFragment : Fragment() {
    private lateinit var binding: IncludeHomeHeaderBinding

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?): View? {
        return DataBindingUtil
            .inflate<IncludeHomeHeaderBinding>(inflater, R.layout.include_home_header, parent, false).let {
                binding = it
                it.root
            }
    }
}