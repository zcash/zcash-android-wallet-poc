package cash.z.android.wallet.ui.util

import android.graphics.Rect
import android.view.View
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import cash.z.android.wallet.R

class AlternatingRowColorDecoration(@DrawableRes val evenBackground: Int = R.color.zcashBlueGray, @DrawableRes val oddBackground: Int = R.color.zcashWhite) :
    RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        val adapterPosition = parent.getChildAdapterPosition(view)
        val rowBackground = if (adapterPosition.rem(2) == 0) evenBackground else oddBackground
        view.setBackgroundResource(rowBackground)
    }
}
