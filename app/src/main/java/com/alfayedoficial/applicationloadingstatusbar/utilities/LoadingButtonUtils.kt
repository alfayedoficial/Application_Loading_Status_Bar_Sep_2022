package com.alfayedoficial.applicationloadingstatusbar.utilities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import com.alfayedoficial.applicationloadingstatusbar.R
import java.util.concurrent.TimeUnit
import kotlin.math.min
import kotlin.properties.Delegates

class LoadingButtonUtils @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    private var buttonDefaultBackgroundColor = 0
    private var buttonBackgroundColor = 0
    private var buttonDefaultText: CharSequence = ""
    private var buttonText: CharSequence = ""
    private var buttonTextColor = 0
    private var progressCircleBackgroundColor = 0


    private var widthSize = 0
    private var heightSize = 0
    private var currentBtnAnimationValue = 0f
    private var currentProgressAnimationValue = 0f
    private var btnText = ""


    private val btnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val btnTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
    }

    private var progressValueAnimator = ValueAnimator()
    private var btnValueAnimator = ValueAnimator()
    private val progressCRect = RectF()
    private lateinit var btnTxtBounds: Rect
    private var progressCSize = 0f

    private val animatorSetBtn: AnimatorSet = AnimatorSet().apply {
        duration = TimeUnit.SECONDS.toMillis(3)
        doOnStart { isEnabled = false }
        doOnEnd { isEnabled = true }
    }

    private var buttonState: ButtonState by Delegates.observable(ButtonState.Completed) { _, _, state ->

        when(state) {
            ButtonState.Loading -> {
                btnText = buttonText.toString()

                animatorSetBtn.start()
                if (!::btnTxtBounds.isInitialized){
                    btnTxtBounds = Rect()
                    btnTextPaint.getTextBounds(btnText, 0, btnText.length, btnTxtBounds)
                    val hCenter = (btnTxtBounds.right + btnTxtBounds.width() + 16f)
                    val vCenter = (heightSize / 2f)

                    progressCRect.set(
                        hCenter - progressCSize,
                        vCenter - progressCSize,
                        hCenter + progressCSize,
                        vCenter + progressCSize
                    )

                }
                invalidate()
                requestLayout()
            }
            else -> {
                btnText = buttonDefaultText.toString()
                animatorSetBtn.cancel()
                invalidate()
                requestLayout()
            }
        }
    }

    fun changeButtonState(state: ButtonState){
        buttonState = state
    }


    init {
        isClickable = true
        context.withStyledAttributes(attrs, R.styleable.LoadingButtonV2) {
            buttonDefaultBackgroundColor = getColor(R.styleable.LoadingButtonV2_buttonDefaultBackgroundColor, 0)
            buttonBackgroundColor = getColor(R.styleable.LoadingButtonV2_buttonBackgroundColor, 0)
            buttonDefaultText = getText(R.styleable.LoadingButtonV2_buttonDefaultText)
            buttonText = getText(R.styleable.LoadingButtonV2_buttonText)
            buttonTextColor = getColor(R.styleable.LoadingButtonV2_buttonTextColor, 0)
        }.also {
            btnText = buttonDefaultText.toString()
            progressCircleBackgroundColor = ContextCompat.getColor(context, R.color.teal_200)
        }

        animateFunctions()
    }

    private fun animateFunctions() {
        progressValueAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ObjectAnimator.RESTART
            addUpdateListener {
                currentProgressAnimationValue = this.animatedValue as Float
                invalidate()
                requestLayout()
            }
        }


    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        btnValueAnimator = ValueAnimator.ofFloat(0f, widthSize.toFloat()).apply {
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            repeatMode = ValueAnimator.RESTART
            addUpdateListener {
                currentBtnAnimationValue = it.animatedValue as Float
                invalidate()
                requestLayout()
            }
        }

        animatorSetBtn.playTogether(progressValueAnimator, btnValueAnimator)
        progressCSize = (min(w, h) / 2f) * 0.4f

    }

    override fun performClick(): Boolean {
        super.performClick()
        // We only change button state to Clicked if the current state is Completed
        if (buttonState == ButtonState.Completed) {
            buttonState = ButtonState.Clicked
            invalidate()
        }
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            drawButtonBackground()
            drawButtonText()
        }
    }

    private fun Canvas.drawButtonBackground() {
        when(buttonState){
            ButtonState.Loading -> {
                btnPaint.apply {
                    color = buttonBackgroundColor
                    drawRect(0f, 0f, currentBtnAnimationValue, heightSize.toFloat(), this)
                    color = buttonDefaultBackgroundColor
                    drawRect(currentBtnAnimationValue, 0f, widthSize.toFloat(), heightSize.toFloat(), this)
                }
                btnPaint.color = progressCircleBackgroundColor
                drawArc(progressCRect, 0f, currentProgressAnimationValue, true, btnPaint)
            }
            else ->{
                drawColor(buttonDefaultBackgroundColor)}
        }
    }

    private fun Canvas.drawButtonText() {
        btnTextPaint.color = buttonTextColor
        drawText(btnText, (widthSize / 2f), (heightSize / 2f) + btnTextPaint.computeTxtOffset(), btnTextPaint)
    }

    private fun TextPaint.computeTxtOffset() = ((descent() - ascent()) / 2) - descent()


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

}