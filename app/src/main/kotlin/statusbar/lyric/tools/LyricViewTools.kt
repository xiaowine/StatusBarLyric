package statusbar.lyric.tools

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import statusbar.lyric.view.LyricSwitchView

object LyricViewTools {
    private var animaList: ArrayList<String> = arrayListOf("Top", "Bottom", "Start", "End", "Fade", "ScaleXY", "ScaleX", "ScaleY")
    val randomAnima: String get() = animaList.random()

    fun getAlphaAnimation(into: Boolean, duration: Long = 250): AnimationSet {
        val alphaAnimation = (if (into) AlphaAnimation(0f, 1F) else AlphaAnimation(1F, 0f)).apply {
            this.duration = duration
        }
        return AnimationSet(true).apply {
            addAnimation(alphaAnimation)
        }
    }

    fun switchViewInAnima(str: String?, interpolator: String?, time: Int?): Animation? {
        val t = time?.toLong() ?: 500L
        val translateAnimation: Animation? = when (str) {
            "Top" -> TranslateAnimation(0f, 0f, 100f, 0f)
            "Bottom" -> TranslateAnimation(0f, 0f, -100f, 0f)
            "Start" -> TranslateAnimation(100f, 0f, 0f, 0f)
            "End" -> TranslateAnimation(-100f, 0f, 0f, 0f)
            "Fade" -> null
            "ScaleXY" -> ScaleAnimation(0f, 1f, 0f, 1f)
            "ScaleX" -> ScaleAnimation(0f, 1f, 1f, 1f)
            "ScaleY" -> ScaleAnimation(1f, 1f, 0f, 1f)
            else -> return null
        }?.apply {
            duration = t
        }
        return getAlphaAnimation(true, t).apply {
            translateAnimation?.let { addAnimation(it) }
            switchInterpolator(interpolator)
        }
    }


    fun switchViewOutAnima(str: String?, time: Int?): Animation? {
        val t = time?.toLong() ?: 500L
        val translateAnimation: Animation? = when (str) {
            "Top" -> TranslateAnimation(0f, 0f, 0f, -100f)
            "Bottom" -> TranslateAnimation(0f, 0f, 0f, +100f)
            "Start" -> TranslateAnimation(0f, -100f, 0f, 0f)
            "End" -> TranslateAnimation(0f, 0f + 100f, 0f, 0f)
            "Fade" -> null
            "ScaleXY" -> ScaleAnimation(1f, 0f, 1f, 0f)
            "ScaleX" -> ScaleAnimation(1f, 0f, 1f, 1f)
            "ScaleY" -> ScaleAnimation(1f, 1f, 1f, 0f)
            else -> return null
        }?.apply {
            duration = t
        }
        return getAlphaAnimation(false, t).apply {
            translateAnimation?.let { addAnimation(it) }
        }
    }

    private fun Animation.switchInterpolator(str: String?) {
        interpolator = when (str) {
            "Linear" -> LinearInterpolator()
            "Accelerate" -> AccelerateInterpolator()
            "Decelerate" -> DecelerateInterpolator()
            "Accelerate&Decelerate" -> AccelerateDecelerateInterpolator()
            "Overshoot" -> OvershootInterpolator()
            "Bounce" -> BounceInterpolator()
            else -> LinearInterpolator()
        }
    }

    fun View.hideView(anim: Boolean = true) {
        if (visibility == View.GONE) return
        if (anim) {
            val alphaAnimation = getAlphaAnimation(false).apply {
                setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}
                    override fun onAnimationEnd(animation: Animation) {
                        visibility = View.GONE
                    }

                    override fun onAnimationRepeat(animation: Animation) {}
                })
            }
            startAnimation(alphaAnimation)
        } else {
            visibility = View.GONE
        }
    }

    fun View.showView() {
        if (visibility == View.VISIBLE) return
        val alphaAnimation = getAlphaAnimation(true).apply {
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    visibility = View.VISIBLE
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
        }
        startAnimation(alphaAnimation)
    }

    private fun ObjectAnimator.colorAnimator() {
        this.apply {
            setEvaluator(ArgbEvaluator())
            duration = 200L
        }.start()
    }

    @SuppressLint("Recycle", "AnimatorKeep")
    fun LyricSwitchView.textColorAnima(color: Int) {
        this.applyToAllViews {
            ObjectAnimator.ofInt(it, "textColor", it.currentTextColor, color).colorAnimator()
        }
    }

    @SuppressLint("Recycle")
    fun ImageView.iconColorAnima(startColor: Int, endColor: Int) {
        ObjectAnimator.ofInt(this, "colorFilter", startColor, endColor).colorAnimator()
    }

}