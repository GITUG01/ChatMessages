package com.klochkov.messages

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class DialogMessageView(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs) {

    var startX = 0F

    private var onDownX = 0F
    private var onDownY = 0F

    private val defaultX = 0f
    private var bacX = 0f
    private var dX = 0f

    private var isSelectModeActive = false
    private var isMessageSelected = false
    private var needMoveMessageInSelectMode = false

    private var leftFirstStepInAnimation: Int? = null
    private var leftFirstStepOutAnimation: Int? = null

    private var movementListener: MovementListener? = null

    private var leftAnimationImageView: ImageView? = null
    private var rightAnimationImageView: ImageView? = null

    private var radioBtn: View? = null

    private var isLeftAnimationAnimating = false

    var container: FrameLayout

    private var onLeftActionCallback: (() -> Unit)? = null

    var isDraggable = false

    init {
        container = FrameLayout(context)
        val layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        container.layoutParams = layoutParams

        addContainer()

        addContainerTouchListener()
    }

    private fun addContainer() {
        addView(container)
    }

    fun setNeedMoveMessageInSelectMode(needMove: Boolean) {
        needMoveMessageInSelectMode = needMove
    }

    fun setLeftFirstStepAnimation(animationIn: Int, animationOut: Int) {
        leftFirstStepInAnimation = animationIn
        leftFirstStepOutAnimation = animationOut

        inflateAnimationsViews()
    }

    fun setOnLeftActionCallback(action: () -> Unit) {
        onLeftActionCallback = action
    }

    fun setMovementListener(movementListener: MovementListener) {
        this.movementListener = movementListener
    }

    fun setSelectMode(isActive: Boolean, isSelected: Boolean) {
        isSelectModeActive = isActive
        isMessageSelected = isSelected

        checkSelectedModeState()
    }

    fun setIsMessageSelected(isSelected: Boolean) {
        isMessageSelected = isSelected

        checkSelectedModeState()
    }

    fun isMessageSelected() = isMessageSelected

    private fun checkSelectedModeState() {
        if (isSelectModeActive) {
            inflateRadioBtn()

            if (needMoveMessageInSelectMode) {
                container.x = 130F
            } else {
                container.x = 0F
            }

            radioBtn?.visibility = View.VISIBLE
            if (isMessageSelected) {
                radioBtn?.setBackgroundResource(R.drawable.fill_radiobtn)
            } else {
                radioBtn?.setBackgroundResource(R.drawable.empty_radiobtn)
            }
        } else {
            radioBtn?.visibility = View.INVISIBLE
            radioBtn?.alpha = 0F
            container.x = 0F
        }
    }

    private fun inflateAnimationsViews() {
        if (leftFirstStepInAnimation != null && leftAnimationImageView == null) {
            leftAnimationImageView = ImageView(context)

            val layoutParams = LayoutParams(268.dpToPx(), 43.dpToPx())

            layoutParams.gravity = Gravity.CENTER_VERTICAL

            leftAnimationImageView!!.elevation = 0F
            leftAnimationImageView!!.setBackgroundResource(R.drawable.appear_reply_02)

            leftAnimationImageView!!.layoutParams = layoutParams

            addView(leftAnimationImageView)

            leftAnimationImageView!!.visibility = View.INVISIBLE
        }

        if (leftFirstStepOutAnimation != null && rightAnimationImageView == null) {
            rightAnimationImageView = ImageView(context)

            val layoutParams = LayoutParams(268.dpToPx(), 43.dpToPx())
            layoutParams.gravity = Gravity.END

            rightAnimationImageView!!.elevation = 0F

            rightAnimationImageView!!.layoutParams = layoutParams

            addView(rightAnimationImageView)

            rightAnimationImageView!!.visibility = View.INVISIBLE
        }
    }

    private fun inflateRadioBtn() {
        if (radioBtn == null) {
            radioBtn = View(context)

            val layoutParams = LayoutParams(24.dpToPx(), 24.dpToPx())
            layoutParams.marginStart = 16.dpToPx()
            layoutParams.gravity = Gravity.CENTER_VERTICAL

            radioBtn!!.layoutParams = layoutParams

            radioBtn!!.setBackgroundResource(R.drawable.empty_radiobtn)

            addView(radioBtn)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addContainerTouchListener() {
        container.setOnTouchListener { _, ev ->
            if (!isDraggable) return@setOnTouchListener false

            if (isSelectModeActive) {
                return@setOnTouchListener false
            }

            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = ev.rawX

                    onDownX = ev.rawX
                    onDownY = ev.rawY
                    runLongClickJob()
                    movementListener?.onTouch()
                    dX = container.x - ev.rawX

                }

                MotionEvent.ACTION_MOVE -> {
                    if (ev.rawX !in (onDownX - 5F)..(onDownX + 5F) && ev.rawY !in (onDownY - 5F)..(onDownY + 5F)) {
                        breakLongClickJob()
                    }

                    if (container.x !in -30f..30f) {
                        movementListener?.onMove()
                    }

                    if (onDownX - ev.rawX < 0) {
                        if (container.x >= measuredWidth * 0.1) {
                            if (!isLeftAnimationAnimating) {
                                leftFirstStepInAnimation()
                            }
                            bacX = defaultX
                        }

                        if (container.x < measuredWidth * 0.1) {
                            if (isLeftAnimationAnimating) {
                                leftFirstStepOutAnimation()
                            }
                            bacX = defaultX
                        }

                        container.animate()
                            .x(ev.rawX + dX)
                            .setDuration(0)
                            .start()
                    }
                }

                MotionEvent.ACTION_UP -> {
                    breakLongClickJob()

                    container.animate()
                        .x(bacX)
                        .setDuration(200)
                        .start()
                    bacX = defaultX
                    movementListener?.upFinger()

                    if (isLeftAnimationAnimating) {
                        onLeftActionCallback?.invoke()

                        leftFirstStepOutAnimation()
                    }

                    if (onDownX == ev.rawX && onDownY == ev.rawY) {
                        callOnClick()
                    }
                }

                MotionEvent.ACTION_CANCEL -> {
                    breakLongClickJob()
                    container.animate()
                        .x(defaultX)
                        .setDuration(200)
                        .start()
                    bacX = defaultX
                }
            }

            true
        }
    }

    private fun leftFirstStepInAnimation() {
        if (leftFirstStepInAnimation != null) {
            isLeftAnimationAnimating = true

            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            leftAnimationImageView!!.visibility = VISIBLE

            val resources = context.resources
            val animationDrawable = ResourcesCompat.getDrawable(
                resources,
                leftFirstStepInAnimation!!,
                context.theme
            ) as AnimationDrawable
            leftAnimationImageView!!.background = animationDrawable
            animationDrawable.start()
        }
    }

    private fun leftFirstStepOutAnimation() {
        if (leftFirstStepOutAnimation != null) {
            isLeftAnimationAnimating = false

            val resources = context.resources
            val animationDrawable = ResourcesCompat.getDrawable(
                resources,
                leftFirstStepOutAnimation!!,
                context.theme
            ) as AnimationDrawable

            leftAnimationImageView?.background = animationDrawable

//            animationDrawable.callback =
//                object : AnimationDrawableCallback(animationDrawable, leftAnimationImageView) {
//                    override fun onAnimationComplete() {
//                        leftAnimationImageView?.visibility = View.INVISIBLE
//                    }
//                }

            animationDrawable.start()
        }
    }

    private var longClickJob: Job? = null
    private fun runLongClickJob() {
        longClickJob?.cancel()
        longClickJob = CoroutineScope(Dispatchers.Main).launch {
            delay(800)
            performLongClick()
        }
    }

    private fun breakLongClickJob() {
        longClickJob?.cancel()
        longClickJob = null
    }

    private fun Int.dpToPx(): Int {
        val metrics = context.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), metrics)
            .toInt()
    }

    interface MovementListener {
        fun onTouch()
        fun onMove()
        fun upFinger()
    }

    enum class CheckStatus(val iconRes: Int) {
        CHECK_STATUS_SENT(R.drawable.clockwise),
        CHECK_STATUS_RECEIVED(R.drawable.one_check),
        CHECK_STATUS_READ(R.drawable.checks);
    }
}