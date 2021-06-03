package com.udacity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.widget.Toast
import androidx.core.content.withStyledAttributes
import kotlinx.android.synthetic.main.content_main.view.*
import timber.log.Timber
import kotlin.math.min
import kotlin.properties.Delegates

private var buttonColourCompleted = 0
private var buttonColourClicked = 0
private var buttonColourLoading = 0

//Declare the rectangle and text objects
private lateinit var buttonRect: Rect
private lateinit var clickedRect: Rect
private lateinit var buttonLabel: String


class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var widthSize = 0
    private var heightSize = 0

    //private var rectX = 0

    private val valueAnimator = ValueAnimator.ofInt(0, 750)

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        //Action when buttonState change is observed. eg:
        Timber.i("buttonState change: ${old} to $new")

        //TODO: If state is Clicked: start rectangle animation
        growRect()

        //TODO: If state is Loading: start circle animation



    }

    //Implement paint class with style and colour information
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create( "", Typeface.NORMAL)
    }

    init {
        Timber.i("LoadingButton initialised")

        //Make view clickable
        isClickable = true

        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            buttonColourCompleted = getColor(R.styleable.LoadingButton_buttonColourCompleted, 0)
            buttonColourClicked = getColor(R.styleable.LoadingButton_buttonColourClicked, 0)
            buttonColourLoading = getColor(R.styleable.LoadingButton_buttonColourLoading, 0)
        }

        buttonLabel = when (buttonState) {
            ButtonState.Completed -> resources.getString(R.string.button_name)
            ButtonState.Clicked -> resources.getString(R.string.button_loading)
            ButtonState.Loading -> resources.getString(R.string.button_loading)
        }

    }

    /**
     *  Override functions
     */

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        Timber.i("onDraw called")

        //Draw button background rectangle
        paint.color = buttonColourCompleted
        canvas?.drawRect(buttonRect, paint)

        //TODO: Draw button clicked rectangle
        paint.color = buttonColourClicked
        canvas?.drawRect(clickedRect, paint)

        //Draw text label
        paint.color = Color.WHITE
        canvas?.drawText(buttonLabel, width/2f, height/2f, paint)

    }

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

    //TODO: Overide function performClick
    override fun performClick(): Boolean {
        Timber.i("performClick called")

        //super.performClick() always returning true and rest of function not running
        Timber.i("Super performClick(): ${super.performClick()}")
        //if (super.performClick()) return true

        //Button state changed to clicked
        buttonState = ButtonState.Clicked

        //Calls onDraw to redraw canvas
        invalidate()
        return true
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        Timber.i("onSizeChanged called")
        buttonRect = Rect(0, 0, width, height)
        clickedRect = Rect(0, 0, 0, height)
    }

    /**
     * Animation functions
     */

    //Set animator to change width of button (0 to 100%)
    private fun growRect() {
        Timber.i("growRect called")
        valueAnimator.duration = 5000
        valueAnimator.disableViewDuringAnimation(this)
        valueAnimator.updateClickedRect()
        valueAnimator.start()
    }

    //Extension function to disable view during animation
    private fun ValueAnimator.disableViewDuringAnimation(view: View) {
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                view.isEnabled = false
                Timber.i("Animator value: ${valueAnimator.animatedValue}")
            }

            override fun onAnimationEnd(animation: Animator?) {
                Timber.i("Animator value: ${valueAnimator.animatedValue}")
                view.isEnabled = true
            }
        })
    }

    //Extension function to monitor animation values

    private fun ValueAnimator.updateClickedRect() {
        addUpdateListener() {
            Timber.i("Animated value (growRect): ${it.animatedValue}")
            clickedRect = Rect(0, 0, it.animatedValue as Int, height)
            invalidate()
        }
    }

}