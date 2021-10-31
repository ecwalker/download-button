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

//TODO: Fix order of button state change

private var buttonColourCompleted = 0
private var buttonColourClicked = 0
private var buttonColourLoading = 0
private var circleColourLoading = 0

//Declare the rectangle and text objects
private lateinit var buttonRect: Rect
private lateinit var clickedRect: Rect
private lateinit var buttonLabel: String

private var downloadStarted = false


class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var widthSize = 0
    private var heightSize = 0

    private lateinit var oval: RectF
    private var sweepAngle = 0f

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        //Action when buttonState change is observed. eg:
        Timber.i("buttonState change: $old to $new")

        //Update button label
        updateButtonLabel()

        //observable: buttonState change: com.udacity.ButtonState$Completed@7242c5e
        //How to access object name??
        when (buttonState) {

            //If state is Clicked: start rectangle animation
            ButtonState.Clicked -> {
                growRect()
                //TODO: What if this runs before onClickListener in Main Activity?
                if (downloadStarted) {
                    buttonState = ButtonState.Loading
                }
            }

            //If state is Loading: start circle animation
            ButtonState.Loading -> {
                growCircle()
            }

            //Reset view: animation objects and enable view
            ButtonState.Completed -> {
                enableViewOnComplete(this)
                sweepAngle = 0f
                clickedRect = Rect(0, 0, 0, height)
                downloadStarted = false
                invalidate()
            }
        }
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

        //Initialise button label
        updateButtonLabel()

        //Access colour attributes for different button states
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            buttonColourCompleted = getColor(R.styleable.LoadingButton_buttonColourCompleted, 0)
            buttonColourClicked = getColor(R.styleable.LoadingButton_buttonColourClicked, 0)
            buttonColourLoading = getColor(R.styleable.LoadingButton_buttonColourLoading, 0)
            circleColourLoading = getColor(R.styleable.LoadingButton_loadingCircleColour, 0)
        }

    }


    /**
     *  Override functions
     */

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        //Timber.i("onDraw called")

        //Draw button background rectangle
        paint.color = buttonColourCompleted
        canvas?.drawRect(buttonRect, paint)

        //Draw button clicked rectangle
        paint.color = buttonColourClicked
        canvas?.drawRect(clickedRect, paint)

        //Draw loading circle
        paint.color = circleColourLoading
        canvas?.drawArc(oval, 0f, sweepAngle, true, paint)

        //Draw text label
        paint.color = Color.WHITE
        canvas?.drawText(buttonLabel, width/2f, height/1.75f, paint)

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
        val rectFWidth = width*0.7f
        val rectFHeight = height*0.25f
        oval = RectF(rectFWidth, rectFHeight, rectFWidth+75f, rectFHeight+75f)
    }


    /**
     * Animation functions
     */

    //Set animator to change width of button (0 to full width of view)
    private fun growRect() {
        Timber.i("growRect called")
        val valueAnimator = ValueAnimator.ofInt(0, width)
        valueAnimator.duration = 5000
        valueAnimator.disableViewDuringAnimation(this)
        valueAnimator.updateClickedRect()
        valueAnimator.onDownloadNotStarted()
        valueAnimator.start()
    }

    //Set animator to change size of circle segment (0 to 360 degrees)
    private fun growCircle() {
        Timber.i("growCircle called")
        val valueAnimator = ValueAnimator.ofFloat(0f, 360f)
        valueAnimator.duration = 5000
        valueAnimator.disableViewDuringAnimation(this)
        valueAnimator.updateSweepAngle()
        valueAnimator.callOnDownloadComplete()
        valueAnimator.start()
    }

    //Extension function to disable view during animation
    private fun ValueAnimator.disableViewDuringAnimation(view: View) {
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                view.isEnabled = false
            }
        })
    }

    //Extension function to change call onDownloadComplete when animation ends
    private fun ValueAnimator.callOnDownloadComplete() {
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                    if (buttonState == ButtonState.Loading) {
                    onDownloadComplete()
                }
            }
        })
    }

    //Extension function to change call onDownloadComplete when animation ends
    private fun ValueAnimator.onDownloadNotStarted() {
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                if (buttonState == ButtonState.Clicked) {
                    onDownloadComplete()
                }
            }
        })
    }

    //Extension function to update the clicked rectangle X value and create animation
    private fun ValueAnimator.updateClickedRect() {
        addUpdateListener() {
            clickedRect = Rect(0, 0, it.animatedValue as Int, height)
            invalidate()
        }
    }

    //Extension function to update loading circle segment angle
    private fun ValueAnimator.updateSweepAngle() {
        addUpdateListener {
            sweepAngle = it.animatedValue as Float
            invalidate()
        }
    }


    /**
     * Handle download and completed button state changes
     */

    fun onDownloadStart() {
        downloadStarted = true
    }

    fun onDownloadComplete() {
        buttonState = ButtonState.Completed
    }


    /**
     *  Update button text label
     */

    private fun updateButtonLabel() {
        buttonLabel = when (buttonState) {
            ButtonState.Completed -> resources.getString(R.string.button_name)
            ButtonState.Clicked -> resources.getString(R.string.button_loading)
            ButtonState.Loading -> resources.getString(R.string.button_loading)
        }
    }

    private fun enableViewOnComplete(view: View) {
        view.isEnabled = true
    }

}