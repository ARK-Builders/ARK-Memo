package dev.arkbuilders.arkmemo.ui.views

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Checkable
import dev.arkbuilders.arkmemo.R
import dev.arkbuilders.arkmemo.utils.dpToPx

/**
 * SwitchButton.
 */
class SwitchButton : View, Checkable {
    private val ANIMATE_STATE_NONE = 0
    private val ANIMATE_STATE_PENDING_DRAG = 1
    private val ANIMATE_STATE_DRAGING = 2
    private val ANIMATE_STATE_PENDING_RESET = 3
    private val ANIMATE_STATE_PENDING_SETTLE = 4
    private val ANIMATE_STATE_SWITCH = 5

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(0, 0, 0, 0)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        var typedArray: TypedArray? = null
        if (attrs != null) {
            typedArray = context.obtainStyledAttributes(attrs, R.styleable.SwitchButton)
        }
        shadowEffect = optBoolean(
            typedArray,
            R.styleable.SwitchButton_sb_shadow_effect,
            true
        )
        uncheckCircleColor = optColor(
            typedArray,
            R.styleable.SwitchButton_sb_uncheckcircle_color,
            R.color.sb_unchecked_circle_color
        )
        uncheckCircleWidth = optPixelSize(
            typedArray,
            R.styleable.SwitchButton_sb_uncheckcircle_width,
            dpToPxInt(1.5f)
        )
        uncheckCircleOffsetX = 10f.dpToPx()
        uncheckCircleRadius = optPixelSize(
            typedArray,
            R.styleable.SwitchButton_sb_uncheckcircle_radius,
            4f.dpToPx()
        )
        checkedLineOffsetX = 4f.dpToPx()
        checkedLineOffsetY = 4f.dpToPx()
        shadowRadius = optPixelSize(
            typedArray,
            R.styleable.SwitchButton_sb_shadow_radius,
            dpToPxInt(2.5f)
        )
        shadowOffset = optPixelSize(
            typedArray,
            R.styleable.SwitchButton_sb_shadow_offset,
            dpToPxInt(1.5f)
        )
        shadowColor = optColor(
            typedArray,
            R.styleable.SwitchButton_sb_shadow_color,
            R.color.sb_shadow_color
        )
        uncheckColor = optColor(
            typedArray,
            R.styleable.SwitchButton_sb_uncheck_color,
            R.color.sb_unchecked_color
        )
        checkedColor = optColor(
            typedArray,
            R.styleable.SwitchButton_sb_checked_color,
            R.color.sb_checked_color
        )
        borderWidth = optPixelSize(
            typedArray,
            R.styleable.SwitchButton_sb_border_width,
            dpToPxInt(1f)
        )
        checkLineColor = optColor(
            typedArray,
            R.styleable.SwitchButton_sb_checkline_color,
            Color.WHITE
        )
        checkLineWidth = optPixelSize(
            typedArray,
            R.styleable.SwitchButton_sb_checkline_width,
            dpToPxInt(1f)
        )
        checkLineLength = 6f.dpToPx()
        val buttonColor = optColor(
            typedArray,
            R.styleable.SwitchButton_sb_button_color,
            Color.WHITE
        )
        uncheckButtonColor = optColor(
            typedArray,
            R.styleable.SwitchButton_sb_uncheckbutton_color,
            buttonColor
        )
        checkedButtonColor = optColor(
            typedArray,
            R.styleable.SwitchButton_sb_checkedbutton_color,
            buttonColor
        )
        val effectDuration = optInt(
            typedArray,
            R.styleable.SwitchButton_sb_effect_duration,
            300
        )
        isChecked = optBoolean(
            typedArray,
            R.styleable.SwitchButton_sb_checked,
            false
        )
        showIndicator = optBoolean(
            typedArray,
            R.styleable.SwitchButton_sb_show_indicator,
            true
        )
        background = optColor(
            typedArray,
            R.styleable.SwitchButton_sb_background,
            Color.WHITE
        )
        enableEffect = optBoolean(
            typedArray,
            R.styleable.SwitchButton_sb_enable_effect,
            true
        )
        typedArray?.recycle()
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        buttonPaint.color = buttonColor
        if (shadowEffect) {
            buttonPaint.setShadowLayer(
                shadowRadius.toFloat(),
                0f, shadowOffset.toFloat(),
                shadowColor
            )
        }
        viewState = ViewState()
        beforeState = ViewState()
        afterState = ViewState()
        valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator.duration = effectDuration.toLong()
        valueAnimator.repeatCount = 0
        valueAnimator.addUpdateListener(animatorUpdateListener)
        valueAnimator.addListener(animatorListener)
        super.setClickable(true)
        setPadding(0, 0, 0, 0)
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var newWidthSpec = widthMeasureSpec
        var newHeightSpec = heightMeasureSpec
        val widthMode = MeasureSpec.getMode(newWidthSpec)
        val heightMode = MeasureSpec.getMode(newHeightSpec)
        if (widthMode == MeasureSpec.UNSPECIFIED
            || widthMode == MeasureSpec.AT_MOST
        ) {
            newWidthSpec = MeasureSpec.makeMeasureSpec(DEFAULT_WIDTH, MeasureSpec.EXACTLY)
        }
        if (heightMode == MeasureSpec.UNSPECIFIED
            || heightMode == MeasureSpec.AT_MOST
        ) {
            newHeightSpec = MeasureSpec.makeMeasureSpec(DEFAULT_HEIGHT, MeasureSpec.EXACTLY)
        }
        super.onMeasure(newWidthSpec, newHeightSpec)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val viewPadding = (shadowRadius + shadowOffset).coerceAtLeast(borderWidth).toFloat()
        height = h - viewPadding - viewPadding
        width = w - viewPadding - viewPadding
        viewRadius = height * .5f
        buttonRadius = viewRadius - borderWidth
        left = viewPadding
        top = viewPadding
        right = w - viewPadding
        bottom = h - viewPadding
        centerX = (left + right) * .5f
        centerY = (top + bottom) * .5f
        buttonMinX = left + viewRadius
        buttonMaxX = right - viewRadius
        if (isChecked()) {
            setCheckedViewState(viewState)
        } else {
            setUncheckViewState(viewState)
        }
        isUiInited = true
        postInvalidate()
    }

    /**
     * @param viewState
     */
    private fun setUncheckViewState(viewState: ViewState) {
        viewState.radius = 0f
        viewState.checkStateColor = uncheckColor
        viewState.checkedLineColor = Color.TRANSPARENT
        viewState.buttonX = buttonMinX
        buttonPaint.color = uncheckButtonColor
    }

    /**
     * @param viewState
     */
    private fun setCheckedViewState(viewState: ViewState) {
        viewState.radius = viewRadius
        viewState.checkStateColor = checkedColor
        viewState.checkedLineColor = checkLineColor
        viewState.buttonX = buttonMaxX
        buttonPaint.color = checkedButtonColor
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.strokeWidth = borderWidth.toFloat()
        paint.style = Paint.Style.FILL
        paint.color = background
        drawRoundRect(
            canvas,
            left, top, right, bottom,
            viewRadius, paint
        )
        paint.style = Paint.Style.STROKE
        paint.color = uncheckColor
        drawRoundRect(
            canvas,
            left, top, right, bottom,
            viewRadius, paint
        )
        if (showIndicator) {
            drawUncheckIndicator(canvas)
        }
        val des = viewState.radius * .5f //[0-backgroundRadius*0.5f]
        paint.style = Paint.Style.STROKE
        paint.color = viewState.checkStateColor
        paint.strokeWidth = borderWidth + des * 2f
        drawRoundRect(
            canvas,
            left + des, top + des, right - des, bottom - des,
            viewRadius, paint
        )
        paint.style = Paint.Style.FILL
        paint.strokeWidth = 1f
        drawArc(
            canvas,
            left, top,
            left + 2 * viewRadius, top + 2 * viewRadius,
            90f, 180f, paint
        )
        canvas.drawRect(
            left + viewRadius, top,
            viewState.buttonX, top + 2 * viewRadius,
            paint
        )
        if (showIndicator) {
            drawCheckedIndicator(canvas)
        }
        drawButton(canvas, viewState.buttonX, centerY)
    }

    private fun drawCheckedIndicator(
        canvas: Canvas,
        color: Int =
            viewState.checkedLineColor,
        lineWidth: Float =
            checkLineWidth.toFloat(),
        sx: Float =
            left + viewRadius - checkedLineOffsetX,
        sy: Float = centerY - checkLineLength,
        ex: Float =
            left + viewRadius - checkedLineOffsetY,
        ey: Float = centerY + checkLineLength,
        paint: Paint = this.paint
    ) {
        paint.style = Paint.Style.STROKE
        paint.color = color
        paint.strokeWidth = lineWidth
        canvas.drawLine(
            sx, sy, ex, ey,
            paint
        )
    }

    private fun drawUncheckIndicator(canvas: Canvas) {
        drawUncheckIndicator(
            canvas,
            uncheckCircleColor,
            uncheckCircleWidth.toFloat(),
            right - uncheckCircleOffsetX, centerY,
            uncheckCircleRadius,
            paint
        )
    }

    private fun drawUncheckIndicator(
        canvas: Canvas,
        color: Int,
        lineWidth: Float,
        centerX: Float, centerY: Float,
        radius: Float,
        paint: Paint
    ) {
        paint.style = Paint.Style.STROKE
        paint.color = color
        paint.strokeWidth = lineWidth
        canvas.drawCircle(centerX, centerY, radius, paint)
    }

    private fun drawArc(
        canvas: Canvas,
        left: Float, top: Float,
        right: Float, bottom: Float,
        startAngle: Float, sweepAngle: Float,
        paint: Paint
    ) {
        canvas.drawArc(
            left, top, right, bottom,
            startAngle, sweepAngle, true, paint
        )
    }

    private fun drawRoundRect(
        canvas: Canvas,
        left: Float, top: Float,
        right: Float, bottom: Float,
        backgroundRadius: Float,
        paint: Paint
    ) {
        canvas.drawRoundRect(
            left, top, right, bottom,
            backgroundRadius, backgroundRadius, paint
        )
    }

    private fun drawButton(canvas: Canvas, x: Float, y: Float) {
        canvas.drawCircle(x, y, buttonRadius, buttonPaint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = -0x222223
        canvas.drawCircle(x, y, buttonRadius, paint)
    }

    override fun setChecked(checked: Boolean) {
        if (checked == isChecked()) {
            postInvalidate()
            return
        }
        toggle(enableEffect, false)
    }

    override fun isChecked(): Boolean {
        return isChecked
    }

    override fun toggle() {
        toggle(true)
    }

    private fun toggle(animate: Boolean) {
        toggle(animate, true)
    }

    private fun toggle(animate: Boolean, broadcast: Boolean) {
        if (!isEnabled) {
            return
        }
        if (isEventBroadcast) {
            throw RuntimeException("should NOT switch the state in method: [onCheckedChanged]!")
        }
        if (!isUiInited) {
            isChecked = !isChecked
            if (broadcast) {
                broadcastEvent()
            }
            return
        }
        if (valueAnimator.isRunning) {
            valueAnimator.cancel()
        }
        if (!enableEffect || !animate) {
            isChecked = !isChecked
            if (isChecked()) {
                setCheckedViewState(viewState)
            } else {
                setUncheckViewState(viewState)
            }
            postInvalidate()
            if (broadcast) {
                broadcastEvent()
            }
            return
        }
        animateState = ANIMATE_STATE_SWITCH
        beforeState.copy(viewState)
        if (isChecked()) {
            setUncheckViewState(afterState)
        } else {
            setCheckedViewState(afterState)
        }
        valueAnimator.start()
    }

    /**
     *
     */
    private fun broadcastEvent() {
        onCheckedChangeListener?.let {
            isEventBroadcast = true
            it.onCheckedChanged(this, isChecked())
        }
        isEventBroadcast = false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isTouchingDown = true
                touchDownTime = System.currentTimeMillis()
                removeCallbacks(postPendingDrag)
                postDelayed(postPendingDrag, 100)
            }

            MotionEvent.ACTION_MOVE -> {
                val eventX = event.x
                if (isPendingDragState) {
                    var fraction = eventX / getWidth()
                    fraction = 0f.coerceAtLeast(1f.coerceAtMost(fraction))
                    viewState.buttonX = (buttonMinX
                            + (buttonMaxX - buttonMinX)
                            * fraction)
                } else if (isDragState) {
                    var fraction = eventX / getWidth()
                    fraction = 0f.coerceAtLeast(1f.coerceAtMost(fraction))
                    viewState.buttonX = (buttonMinX
                            + (buttonMaxX - buttonMinX)
                            * fraction)
                    viewState.checkStateColor = argbEvaluator.evaluate(
                        fraction,
                        uncheckColor,
                        checkedColor
                    ) as Int
                    postInvalidate()
                }
            }

            MotionEvent.ACTION_UP -> {
                isTouchingDown = false
                removeCallbacks(postPendingDrag)
                if (System.currentTimeMillis() - touchDownTime <= 300) {
                    toggle()
                } else if (isDragState) {
                    val eventX = event.x
                    var fraction = eventX / getWidth()
                    fraction = 0f.coerceAtLeast(1f.coerceAtMost(fraction))
                    val newCheck = fraction > .5f
                    if (newCheck == isChecked()) {
                        pendingCancelDragState()
                    } else {
                        isChecked = newCheck
                        pendingSettleState()
                    }
                } else if (isPendingDragState) {
                    pendingCancelDragState()
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                isTouchingDown = false
                removeCallbacks(postPendingDrag)
                if (isPendingDragState
                    || isDragState
                ) {
                    pendingCancelDragState()
                }
            }
        }
        return true
    }

    private val isInAnimating: Boolean
        get() = animateState != ANIMATE_STATE_NONE
    private val isPendingDragState: Boolean
        get() = (animateState == ANIMATE_STATE_PENDING_DRAG
                || animateState == ANIMATE_STATE_PENDING_RESET)
    private val isDragState: Boolean
        get() = animateState == ANIMATE_STATE_DRAGING

    fun setShadowEffect(shadowEffect: Boolean) {
        if (this.shadowEffect == shadowEffect) {
            return
        }
        this.shadowEffect = shadowEffect
        if (this.shadowEffect) {
            buttonPaint.setShadowLayer(
                shadowRadius.toFloat(),
                0f, shadowOffset.toFloat(),
                shadowColor
            )
        } else {
            buttonPaint.setShadowLayer(
                0f,
                0f, 0f,
                0
            )
        }
    }

    fun setEnableEffect(enable: Boolean) {
        enableEffect = enable
    }

    private fun pendingDragState() {
        if (isInAnimating) {
            return
        }
        if (!isTouchingDown) {
            return
        }
        if (valueAnimator.isRunning) {
            valueAnimator.cancel()
        }
        animateState = ANIMATE_STATE_PENDING_DRAG
        beforeState.copy(viewState)
        afterState.copy(viewState)
        if (isChecked()) {
            afterState.checkStateColor = checkedColor
            afterState.buttonX = buttonMaxX
            afterState.checkedLineColor = checkedColor
        } else {
            afterState.checkStateColor = uncheckColor
            afterState.buttonX = buttonMinX
            afterState.radius = viewRadius
        }
        valueAnimator.start()
    }

    private fun pendingCancelDragState() {
        if (isDragState || isPendingDragState) {
            if (valueAnimator.isRunning) {
                valueAnimator.cancel()
            }
            animateState = ANIMATE_STATE_PENDING_RESET
            beforeState.copy(viewState)
            if (isChecked()) {
                setCheckedViewState(afterState)
            } else {
                setUncheckViewState(afterState)
            }
            valueAnimator.start()
        }
    }

    private fun pendingSettleState() {
        if (valueAnimator.isRunning) {
            valueAnimator.cancel()
        }
        animateState = ANIMATE_STATE_PENDING_SETTLE
        beforeState.copy(viewState)
        if (isChecked()) {
            setCheckedViewState(afterState)
        } else {
            setUncheckViewState(afterState)
        }
        valueAnimator.start()
    }

    override fun setOnClickListener(l: OnClickListener?) {}
    override fun setOnLongClickListener(l: OnLongClickListener?) {}
    fun setOnCheckedChangeListener(l: OnCheckedChangeListener?) {
        onCheckedChangeListener = l
    }

    interface OnCheckedChangeListener {
        fun onCheckedChanged(view: SwitchButton?, isChecked: Boolean)
    }

    private var shadowRadius = 0
    private var shadowOffset = 0
    private var shadowColor = 0
    private var viewRadius = 0f
    private var buttonRadius = 0f
    private var height = 0f
    private var width = 0f
    private var left = 0f
    private var top = 0f
    private var right = 0f
    private var bottom = 0f
    private var centerX = 0f
    private var centerY = 0f
    private var background = 0
    private var uncheckColor = 0
    private var checkedColor = 0
    private var borderWidth = 0
    private var checkLineColor = 0
    private var checkLineWidth = 0
    private var checkLineLength = 0f
    private var uncheckCircleColor = 0
    private var uncheckCircleWidth = 0
    private var uncheckCircleOffsetX = 0f
    private var uncheckCircleRadius = 0f
    private var checkedLineOffsetX = 0f
    private var checkedLineOffsetY = 0f

    /**
     * Color for button when it's unchecked
     */
    private var uncheckButtonColor = 0

    /**
     * Color for button when it's checked
     */
    private var checkedButtonColor = 0
    private var buttonMinX = 0f
    private var buttonMaxX = 0f
    private lateinit var buttonPaint: Paint
    private lateinit var paint: Paint
    private lateinit var viewState: ViewState
    private lateinit var beforeState: ViewState
    private lateinit var afterState: ViewState
    private var animateState = ANIMATE_STATE_NONE
    private lateinit var valueAnimator: ValueAnimator
    private val argbEvaluator = ArgbEvaluator()
    private var isChecked = false
    private var enableEffect = false
    private var shadowEffect = false
    private var showIndicator = false
    private var isTouchingDown = false
    private var isUiInited = false
    private var isEventBroadcast = false
    private var onCheckedChangeListener: OnCheckedChangeListener? = null
    private var touchDownTime: Long = 0
    private val postPendingDrag: Runnable = Runnable {
        if (!isInAnimating) {
            pendingDragState()
        }
    }
    private val animatorUpdateListener: AnimatorUpdateListener = object : AnimatorUpdateListener {
        override fun onAnimationUpdate(animation: ValueAnimator) {
            val value = animation.animatedValue as Float
            when (animateState) {
                ANIMATE_STATE_PENDING_SETTLE -> {
                    run {
                        viewState.checkedLineColor = argbEvaluator.evaluate(
                            value,
                            beforeState.checkedLineColor,
                            afterState.checkedLineColor
                        ) as Int
                        viewState.radius = (beforeState.radius
                                + (afterState.radius - beforeState.radius) * value)
                        if (animateState != ANIMATE_STATE_PENDING_DRAG) {
                            viewState.buttonX = (beforeState.buttonX
                                    + (afterState.buttonX - beforeState.buttonX) * value)
                        }
                        viewState.checkStateColor = argbEvaluator.evaluate(
                            value,
                            beforeState.checkStateColor,
                            afterState.checkStateColor
                        ) as Int
                    }
                }

                ANIMATE_STATE_PENDING_RESET -> {
                    run {
                        viewState.checkedLineColor = argbEvaluator.evaluate(
                            value,
                            beforeState.checkedLineColor,
                            afterState.checkedLineColor
                        ) as Int
                        viewState.radius = (beforeState.radius
                                + (afterState.radius - beforeState.radius) * value)
                        if (animateState != ANIMATE_STATE_PENDING_DRAG) {
                            viewState.buttonX = (beforeState.buttonX
                                    + (afterState.buttonX - beforeState.buttonX) * value)
                        }
                        viewState.checkStateColor = argbEvaluator.evaluate(
                            value,
                            beforeState.checkStateColor,
                            afterState.checkStateColor
                        ) as Int
                    }
                }

                ANIMATE_STATE_PENDING_DRAG -> {
                    viewState.checkedLineColor = argbEvaluator.evaluate(
                        value,
                        beforeState.checkedLineColor,
                        afterState.checkedLineColor
                    ) as Int
                    viewState.radius = (beforeState.radius
                            + (afterState.radius - beforeState.radius) * value)
                    if (animateState != ANIMATE_STATE_PENDING_DRAG) {
                        viewState.buttonX = (beforeState.buttonX
                                + (afterState.buttonX - beforeState.buttonX) * value)
                    }
                    viewState.checkStateColor = argbEvaluator.evaluate(
                        value,
                        beforeState.checkStateColor,
                        afterState.checkStateColor
                    ) as Int
                }

                ANIMATE_STATE_SWITCH -> {
                    viewState.buttonX = (beforeState.buttonX
                            + (afterState.buttonX - beforeState.buttonX) * value)
                    val fraction = (viewState.buttonX - buttonMinX) / (buttonMaxX - buttonMinX)
                    viewState.checkStateColor = argbEvaluator.evaluate(
                        fraction,
                        uncheckColor,
                        checkedColor
                    ) as Int
                    viewState.radius = fraction * viewRadius
                    viewState.checkedLineColor = argbEvaluator.evaluate(
                        fraction,
                        Color.TRANSPARENT,
                        checkLineColor
                    ) as Int
                }

                ANIMATE_STATE_DRAGING -> {
                }

                ANIMATE_STATE_NONE -> {}
                else -> {
                }
            }
            postInvalidate()
        }
    }
    private val animatorListener: Animator.AnimatorListener = object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator) {}
        override fun onAnimationEnd(animation: Animator) {
            when (animateState) {
                ANIMATE_STATE_DRAGING -> {}
                ANIMATE_STATE_PENDING_DRAG -> {
                    animateState = ANIMATE_STATE_DRAGING
                    viewState.checkedLineColor = Color.TRANSPARENT
                    viewState.radius = viewRadius
                    postInvalidate()
                }

                ANIMATE_STATE_PENDING_RESET -> {
                    animateState = ANIMATE_STATE_NONE
                    postInvalidate()
                }

                ANIMATE_STATE_PENDING_SETTLE -> {
                    animateState = ANIMATE_STATE_NONE
                    postInvalidate()
                    broadcastEvent()
                }

                ANIMATE_STATE_SWITCH -> {
                    isChecked = !isChecked
                    animateState = ANIMATE_STATE_NONE
                    postInvalidate()
                    broadcastEvent()
                }

                ANIMATE_STATE_NONE -> {}
                else -> {}
            }
        }

        override fun onAnimationCancel(animation: Animator) {}
        override fun onAnimationRepeat(animation: Animator) {}
    }

    private class ViewState {
        var buttonX = 0f
        var checkStateColor = 0
        var checkedLineColor = 0
        var radius = 0f
        fun copy(source: ViewState) {
            buttonX = source.buttonX
            checkStateColor = source.checkStateColor
            checkedLineColor = source.checkedLineColor
            radius = source.radius
        }
    }

    companion object {
        private val DEFAULT_WIDTH = dpToPxInt(50f)
        private val DEFAULT_HEIGHT = dpToPxInt(31f)

        private fun dpToPxInt(dp: Float): Int {
            return dp.dpToPx().toInt()
        }

        private fun optInt(
            typedArray: TypedArray?,
            index: Int,
            def: Int
        ): Int {
            return typedArray?.getInt(index, def) ?: def
        }

        private fun optPixelSize(
            typedArray: TypedArray?,
            index: Int,
            def: Float
        ): Float {
            return typedArray?.getDimension(index, def) ?: def
        }

        private fun optPixelSize(
            typedArray: TypedArray?,
            index: Int,
            def: Int
        ): Int {
            return typedArray?.getDimensionPixelOffset(index, def) ?: def
        }

        private fun optColor(
            typedArray: TypedArray?,
            index: Int,
            def: Int
        ): Int {
            return typedArray?.getColor(index, def) ?: def
        }

        private fun optBoolean(
            typedArray: TypedArray?,
            index: Int,
            def: Boolean
        ): Boolean {
            return typedArray?.getBoolean(index, def) ?: def
        }
    }
}