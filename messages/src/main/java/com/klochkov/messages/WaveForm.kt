package com.klochkov.messages

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.LinearInterpolator
import com.masoudss.lib.utils.Utils
import com.masoudss.lib.utils.WaveGravity
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt

open class WaveForm @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var mCanvasWidth = 0
    private var mCanvasHeight = 0
    private val mWavePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mWaveRect = RectF()
    private val mMarkerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mMarkerRect = RectF()
    private val mProgressCanvas = Canvas()
    private var mMaxValue = Utils.dp(context, 2).toInt()
    private var mTouchDownX = 0F
    private var mProgress = 0f
    private var mScaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var mAlreadyMoved = false
    private lateinit var progressBitmap: Bitmap
    private lateinit var progressShader: Shader

    var animator: ValueAnimator? = null

    var onProgressChanged: WaveFormOnProgressChangeListener? = null

    private var sample: IntArray = intArrayOf(
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1
    )
        set(value) {
            field = value
            setMaxValue()
            invalidate()
        }

    var progress: Float = 0F
        set(value) {
            field = value
            invalidate()
            onProgressChanged?.onProgressChanged(progress, false)
        }

    private var maxProgress: Float = 100F
        set(value) {
            field = value
            invalidate()
        }

    var waveBackgroundColor: Int = Color.LTGRAY
        set(value) {
            field = value
            invalidate()
        }

    var waveProgressColor: Int = Color.WHITE
        set(value) {
            field = value
            invalidate()
        }

    private var waveGap: Float = Utils.dp(context, 2)
        set(value) {
            field = value
            invalidate()
        }

    private var wavePaddingTop: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    private var wavePaddingBottom: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    private var wavePaddingLeft: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    private var wavePaddingRight: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    private var waveWidth: Float = Utils.dp(context, 5)
        set(value) {
            field = value
            invalidate()
        }

    private var waveMinHeight: Float = waveWidth
        set(value) {
            field = value
            invalidate()
        }

    private var waveCornerRadius: Float = Utils.dp(context, 2)
        set(value) {
            field = value
            invalidate()
        }

    private var waveGravity: WaveGravity = WaveGravity.BOTTOM
        set(value) {
            field = value
            invalidate()
        }

    private var marker: HashMap<Float, String>? = null
        set(value) {
            field = value
            invalidate()
        }

    private var markerWidth: Float = Utils.dp(context, 1)
        set(value) {
            field = value
            invalidate()
        }

    private var markerColor: Int = Color.GREEN
        set(value) {
            field = value
            invalidate()
        }

    private var markerTextColor: Int = Color.RED
        set(value) {
            field = value
            invalidate()
        }

    private var markerTextSize: Float = Utils.dp(context, 12)
        set(value) {
            field = value
            invalidate()
        }

    private var markerTextPadding: Float = Utils.dp(context, 2)
        set(value) {
            field = value
            invalidate()
        }

    private var visibleProgress: Float = 0F
        set(value) {
            field = value
            invalidate()
        }

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.WaveForm)
        waveWidth = ta.getDimension(R.styleable.WaveForm_wave_width, waveWidth)
        waveGap = ta.getDimension(R.styleable.WaveForm_wave_gap, waveGap)
        wavePaddingTop = ta.getDimension(R.styleable.WaveForm_wave_padding_top, 0F).toInt()
        wavePaddingBottom =
            ta.getDimension(R.styleable.WaveForm_wave_padding_Bottom, 0F).toInt()
        wavePaddingLeft = ta.getDimension(R.styleable.WaveForm_wave_padding_left, 0F).toInt()
        wavePaddingRight =
            ta.getDimension(R.styleable.WaveForm_wave_padding_right, 0F).toInt()
        waveCornerRadius =
            ta.getDimension(R.styleable.WaveForm_wave_corner_radius, waveCornerRadius)
        waveMinHeight = ta.getDimension(R.styleable.WaveForm_wave_min_height, waveMinHeight)
        progress = ta.getFloat(R.styleable.WaveForm_wave_progress, progress)
        maxProgress = ta.getFloat(R.styleable.WaveForm_wave_max_progress, maxProgress)
        visibleProgress =
            ta.getFloat(R.styleable.WaveForm_wave_visible_progress, visibleProgress)
        ta.recycle()
    }

    private fun setMaxValue() {
        mMaxValue = sample.maxOrNull() ?: 0
    }

    fun setSampleFrom(samples: IntArray) {
        if (samples.none { it != 0 }) return
        if (samples.isEmpty()) return

        this.sample = samples

        val totalWaveWidth = waveGap + waveWidth
        val barsToDraw = (getAvailableWidth() / totalWaveWidth).toInt()
        if (barsToDraw == 0) return
        val forStep = sample.size / barsToDraw
        if (forStep <= 1) {
            return
        }
        sample.let { sample ->
            val newSample: MutableList<Int> = mutableListOf()
            for (i in 0..sample.size step forStep) {
                if (i != sample.size) {
                    val number = sample[i]
                    if (sample.indexOf(number) == 0 || sample.indexOf(number) == sample.size - 1) {
                        newSample.add(number)
                    } else {
                        newSample.add(max(number, sample[sample.indexOf(number) + 1]))
                    }
                }
            }
            this.sample = newSample.toIntArray()

        }
//        this.sample = samples.map { it * 2 }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasWidth = w
        mCanvasHeight = h
        progressBitmap =
            Bitmap.createBitmap(getAvailableWidth(), getAvailableHeight(), Bitmap.Config.ARGB_8888)
        progressShader = BitmapShader(progressBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        sample.let { waveSample ->
            if (waveSample.isEmpty())
                return

            canvas.clipRect(
                paddingLeft,
                paddingTop,
                mCanvasWidth - paddingRight,
                mCanvasHeight - paddingBottom
            )
            val totalWaveWidth = waveGap + waveWidth
            var step = waveSample.size / (getAvailableWidth() / totalWaveWidth)

            var previousWaveRight = paddingLeft.toFloat() + wavePaddingLeft
            var sampleItemPosition: Int

            val barsToDraw = (getAvailableWidth() / totalWaveWidth).toInt()
            val start: Int
            val progressXPosition: Float
            if (visibleProgress > 0) {
                // If visibleProgress is > 0, the bars move instead of the blue colored part
                step *= visibleProgress / maxProgress
                val barsForProgress = barsToDraw + 1
                // intFactor is required as depending on whether an equal number of bars must be drawn, the start will switch differently
                val intFactor = (((barsForProgress + 1) % 2))
                // Calculate fixed start change depending
                previousWaveRight += (getAvailableWidth() * 0.5F) % totalWaveWidth
                previousWaveRight += intFactor * 0.5F * totalWaveWidth - totalWaveWidth
                // Calculate start change depending on progress, so that it moves smoothly
                previousWaveRight -= ((progress + intFactor * visibleProgress / barsForProgress * 0.5f) % (visibleProgress / barsForProgress)) / (visibleProgress / barsForProgress) * totalWaveWidth
                start =
                    (progress * barsForProgress / visibleProgress - (barsForProgress / 2F)).roundToInt() - 1
                progressXPosition = getAvailableWidth() * 0.5F
            } else {
                start = 0
                progressXPosition = getAvailableWidth() * progress / maxProgress
            }

            // draw waves
            for (i in start until barsToDraw + start + 3) {
                sampleItemPosition = floor(i * step).roundToInt()
                var waveHeight =
                    if (sampleItemPosition >= 0 && sampleItemPosition < waveSample.size)
                        (getAvailableHeight() - wavePaddingTop - wavePaddingBottom) * (waveSample[sampleItemPosition].toFloat() / mMaxValue)
                    else 0F

                if (waveHeight < waveMinHeight) waveHeight = waveMinHeight

                val top: Float = when (waveGravity) {
                    WaveGravity.TOP -> paddingTop.toFloat() + wavePaddingTop
                    WaveGravity.CENTER -> (paddingTop + wavePaddingTop + getAvailableHeight()) / 2F - waveHeight / 2F
                    WaveGravity.BOTTOM -> mCanvasHeight - paddingBottom - wavePaddingBottom - waveHeight
                }

                mWaveRect.set(
                    previousWaveRight,
                    top,
                    previousWaveRight + waveWidth,
                    top + waveHeight
                )
                when {
                    // if progress is currently in waveRect, color have to be split up
                    mWaveRect.contains(progressXPosition, mWaveRect.centerY()) -> {
                        mProgressCanvas.setBitmap(progressBitmap)
                        mWavePaint.color = waveProgressColor
                        mProgressCanvas.drawRect(
                            0F,
                            0F,
                            progressXPosition,
                            mWaveRect.bottom,
                            mWavePaint
                        )
                        mWavePaint.color = waveBackgroundColor
                        mProgressCanvas.drawRect(
                            progressXPosition,
                            0F,
                            getAvailableWidth().toFloat(),
                            mWaveRect.bottom,
                            mWavePaint
                        )
                        mWavePaint.shader = progressShader
                    }

                    mWaveRect.right <= progressXPosition -> {
                        mWavePaint.color = waveProgressColor
                        mWavePaint.shader = null
                    }

                    else -> {
                        mWavePaint.color = waveBackgroundColor
                        mWavePaint.shader = null
                    }
                }
                canvas.drawRoundRect(mWaveRect, waveCornerRadius, waveCornerRadius, mWavePaint)
                previousWaveRight = mWaveRect.right + waveGap
            }

            // TODO: implement for visibleProgress > 0
            //draw markers
            if (visibleProgress <= 0) marker?.forEach {
                // out of progress range
                if (it.key < 0 || it.key > maxProgress) return

                val markerXPosition = getAvailableWidth() * (it.key / maxProgress)
                mMarkerRect.set(
                    markerXPosition - (markerWidth / 2),
                    0f,
                    markerXPosition + (markerWidth / 2),
                    getAvailableHeight().toFloat()
                )
                mMarkerPaint.color = markerColor
                canvas.drawRect(mMarkerRect, mMarkerPaint)

                val markerTextDistance = markerTextPadding
                val markerTextXPosition = -markerXPosition - (markerWidth / 2) - markerTextDistance

                mMarkerPaint.textSize = markerTextSize
                mMarkerPaint.color = markerTextColor
                canvas.rotate(90f)
                canvas.drawText(it.value, markerTextDistance, markerTextXPosition, mMarkerPaint)
                canvas.rotate(-90f)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!isEnabled)
            return false
        if (visibleProgress > 0) {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    mTouchDownX = event.x
                    mProgress = progress
                    mAlreadyMoved = false
                }

                MotionEvent.ACTION_MOVE -> {
                    if (abs(event.x - mTouchDownX) > mScaledTouchSlop || mAlreadyMoved) {
                        updateProgress(event)
                        mAlreadyMoved = true
                    }
                }

                MotionEvent.ACTION_UP -> {
                    performClick()
                }
            }
        } else {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (isParentScrolling())
                        mTouchDownX = event.x
                    else
                        updateProgress(event)
                }

                MotionEvent.ACTION_MOVE -> {
                    updateProgress(event)
                }

                MotionEvent.ACTION_UP -> {
                    if (abs(event.x - mTouchDownX) > mScaledTouchSlop)
                        updateProgress(event)
                    performClick()
                }
            }
        }
        return true
    }

    private fun isParentScrolling(): Boolean {
        var parent = parent as View
        val root = rootView
        while (true) {
            when {
                parent.canScrollHorizontally(1) -> return true
                parent.canScrollHorizontally(-1) -> return true
                parent.canScrollVertically(1) -> return true
                parent.canScrollVertically(-1) -> return true
            }
            if (parent == root)
                return false
            parent = parent.parent as View
        }
    }

    private fun updateProgress(event: MotionEvent) {
        progress = getProgress(event)
        onProgressChanged?.onProgressChanged(progress, true)
    }

    private fun getProgress(event: MotionEvent): Float {
        return maxProgress * event.x / getAvailableWidth()
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    fun getAvailableWidth(): Int {
        var width = mCanvasWidth - paddingLeft - paddingRight
        if (width <= 0) width = 1
        return width
    }

    private fun getAvailableHeight(): Int {
        var height = mCanvasHeight - paddingTop - paddingBottom
        if (height <= 0) height = 1
        return height
    }

    fun startProgress(voiceDuration: Long, onComplete: () -> Unit) {
        if (progress == 100F) progress = 0F
        val duration = voiceDuration * ((100F - progress) / 100F)
        if (duration >= 0) {
            animator = ValueAnimator.ofFloat(progress, 100F)
            animator!!.duration = duration.toLong()
            animator!!.interpolator = LinearInterpolator()
            animator!!.addUpdateListener { valueAnimator ->
                val value = valueAnimator.animatedValue as Float
                this.progress = value
                if (value == 100F) {
                    onComplete.invoke()
                }
            }
            animator?.start()
        }
    }

    fun pauseProgress() {
        animator?.pause()
    }
}

interface WaveFormOnProgressChangeListener {
    fun onProgressChanged(progress: Float, fromUser: Boolean)
}