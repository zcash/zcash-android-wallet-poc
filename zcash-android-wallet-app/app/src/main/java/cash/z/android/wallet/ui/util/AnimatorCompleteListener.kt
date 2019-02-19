package cash.z.android.wallet.ui.util

import android.animation.Animator

class AnimatorCompleteListener(val block: (Animator) -> Unit) : Animator.AnimatorListener {
    override fun onAnimationRepeat(animation: Animator?) {
    }

    override fun onAnimationEnd(animation: Animator) {
        block(animation)
    }

    override fun onAnimationStart(animation: Animator?) {
    }

    override fun onAnimationCancel(animation: Animator?) {
    }
}