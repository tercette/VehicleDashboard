package com.volks.vehicledashboard.presentation.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class GaugeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private var minValue = 0f
    private var maxValue = 240f
    private var majorStep = 20f
    private var tickLabelDivisor = 1f
    private var redlineStart = Float.MAX_VALUE
    private var label = ""
    private var unit = ""
    private var accentColor = Color.parseColor("#00E5FF")

    private var displayedValue = minValue
    private var animator: ValueAnimator? = null

    private val startAngle = 135f
    private val sweepAngle = 270f

    private val arcRect = RectF()

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#243040")
    }
    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND
    }
    private val redlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeCap = Paint.Cap.BUTT
        color = Color.parseColor("#FF3B30")
    }
    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; color = Color.parseColor("#5A6B7E")
    }
    private val tickTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#AEB9C7"); textAlign = Paint.Align.CENTER
    }
    private val needlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND
    }
    private val hubPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val valueTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; textAlign = Paint.Align.CENTER; isFakeBoldText = true
    }
    private val unitTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#8A93A2"); textAlign = Paint.Align.CENTER
    }
    private val labelTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#8A93A2"); textAlign = Paint.Align.CENTER
    }

    fun configure(
        min: Float,
        max: Float,
        majorStep: Float,
        accentColor: Int,
        label: String,
        unit: String,
        tickLabelDivisor: Float = 1f,
        redlineStart: Float = Float.MAX_VALUE
    ) {
        this.minValue = min
        this.maxValue = max
        this.majorStep = majorStep
        this.accentColor = accentColor
        this.label = label
        this.unit = unit
        this.tickLabelDivisor = tickLabelDivisor
        this.redlineStart = redlineStart
        this.displayedValue = min
        valuePaint.color = accentColor
        invalidate()
    }

    fun setValue(target: Float) {
        val clamped = target.coerceIn(minValue, maxValue)
        animator?.cancel()
        animator = ValueAnimator.ofFloat(displayedValue, clamped).apply {
            duration = 220
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                displayedValue = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun valueToAngle(v: Float): Float =
        startAngle + (v - minValue) / (maxValue - minValue) * sweepAngle

    override fun onDraw(canvas: Canvas) {
        val cx = width / 2f
        val cy = height / 2f
        val radius = min(width, height) / 2f
        val arcStroke = radius * 0.12f
        val r = radius - arcStroke / 2f - radius * 0.04f

        trackPaint.strokeWidth = arcStroke
        valuePaint.strokeWidth = arcStroke
        redlinePaint.strokeWidth = arcStroke
        tickPaint.strokeWidth = radius * 0.012f
        needlePaint.strokeWidth = radius * 0.035f
        tickTextPaint.textSize = radius * 0.11f
        valueTextPaint.textSize = radius * 0.42f
        unitTextPaint.textSize = radius * 0.12f
        labelTextPaint.textSize = radius * 0.11f

        arcRect.set(cx - r, cy - r, cx + r, cy + r)

        canvas.drawArc(arcRect, startAngle, sweepAngle, false, trackPaint)

        // redline
        if (redlineStart < maxValue) {
            val redStart = valueToAngle(redlineStart)
            val redSweep = valueToAngle(maxValue) - redStart
            canvas.drawArc(arcRect, redStart, redSweep, false, redlinePaint)
        }

        // arco de valor
        val valueSweep = valueToAngle(displayedValue) - startAngle
        canvas.drawArc(arcRect, startAngle, valueSweep, false, valuePaint)

        // marcações + números
        val tickOuter = r - arcStroke / 2f - radius * 0.02f
        val tickInner = tickOuter - radius * 0.10f
        val textR = tickInner - radius * 0.10f
        var v = minValue
        while (v <= maxValue + 0.001f) {
            val a = Math.toRadians(valueToAngle(v).toDouble())
            val cosA = cos(a).toFloat()
            val sinA = sin(a).toFloat()
            canvas.drawLine(
                cx + cosA * tickOuter, cy + sinA * tickOuter,
                cx + cosA * tickInner, cy + sinA * tickInner,
                tickPaint
            )
            val labelNum = (v / tickLabelDivisor).toInt().toString()
            canvas.drawText(
                labelNum,
                cx + cosA * textR,
                cy + sinA * textR + tickTextPaint.textSize / 3f,
                tickTextPaint
            )
            v += majorStep
        }

        // ponteiro + cubo central
        val needleAngle = Math.toRadians(valueToAngle(displayedValue).toDouble())
        val needleLen = r - arcStroke - radius * 0.06f
        needlePaint.color = if (displayedValue >= redlineStart) redlinePaint.color else accentColor
        canvas.drawLine(
            cx, cy,
            cx + cos(needleAngle).toFloat() * needleLen,
            cy + sin(needleAngle).toFloat() * needleLen,
            needlePaint
        )
        hubPaint.color = needlePaint.color
        canvas.drawCircle(cx, cy, radius * 0.06f, hubPaint)

        // leitura digital central
        canvas.drawText(label, cx, cy - radius * 0.30f, labelTextPaint)
        canvas.drawText(
            displayedValue.toInt().toString(),
            cx, cy + radius * 0.55f, valueTextPaint
        )
        canvas.drawText(unit, cx, cy + radius * 0.72f, unitTextPaint)
    }
}
