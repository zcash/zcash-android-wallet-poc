package cash.z.android.wallet.ui.util

import android.animation.Animator
import cash.z.android.wallet.extention.Toaster
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable

/**
 * Utility to help with looping a lottie animation over a particular range. It will start the animation and play it up
 * to the end of the range and then set it to loop over the range and, once stopped, it will proceed from the current
 * frame to the end of the animation. Visually: BEGIN...LOOP...LOOP...LOOP...END
 */
class LottieLooper(private val lottie: LottieAnimationView, private val loopRange: IntRange, private val lastFrame: Int = Int.MAX_VALUE) :
    Animator.AnimatorListener {

    var isPlaying = false

    fun start() {
        if (isPlaying) return
        with(lottie) {
            setMinAndMaxFrame(1, loopRange.last)
            progress = 0f
            repeatCount = 0
            addAnimatorListener(this@LottieLooper)
            playAnimation()
        }
        isPlaying = true
    }

    fun stop() {
        with(lottie) {
            setMinAndMaxFrame(lottie.frame, lastFrame)
            repeatCount = 0
            // we don't want to just cancel the animation. We want it to finish it's final frames but the moment it is
            // done, we need it to freeze on that final frame and then die
            addAnimatorListener(LottieAssassin())
        }
        isPlaying = false
    }

    override fun onAnimationRepeat(animation: Animator?) {
    }

    override fun onAnimationEnd(animation: Animator?) {
        with(lottie) {
            removeAllAnimatorListeners()
            setMinAndMaxFrame(loopRange.first, loopRange.last)
            repeatCount = LottieDrawable.INFINITE
            playAnimation()
        }
    }

    override fun onAnimationCancel(animation: Animator?) {
    }

    override fun onAnimationStart(animation: Animator?) {
    }

    /** I have one job: kill lottie */
    inner class LottieAssassin : Animator.AnimatorListener {
        override fun onAnimationRepeat(animation: Animator?) {
            finishingMove()
        }

        override fun onAnimationEnd(animation: Animator?) {
            finishingMove()
        }

        override fun onAnimationCancel(animation: Animator?) {
            finishingMove()
        }

        override fun onAnimationStart(animation: Animator?) {
            finishingMove()
        }

        /** Agressively force it to freeze on the lastframe */
        private fun finishingMove() {
            lottie.pauseAnimation()
            lottie.setMinAndMaxFrame(lastFrame, lastFrame)
            lottie.progress = 1.0f
            // wait around a bit to see if my listeners detect any movement, then quietly make my getaway
            lottie.postDelayed({
                lottie.removeAnimatorListener(this)
                lottie.setMinAndMaxFrame(1, lastFrame)
            }, 500L)
        }
    }
}