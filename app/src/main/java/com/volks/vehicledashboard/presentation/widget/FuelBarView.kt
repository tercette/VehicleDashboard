package com.volks.vehicledashboard.presentation.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.max

class FuelBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private var level = 0f

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL; color = Color.parseColor("#243040")
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val valueTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; textAlign = Paint.Align.CENTER; isFakeBoldText = true
    }
    private val endLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#AEB9C7"); textAlign = Paint.Align.CENTER
    }

    private val trackRect = RectF()
    private val fillRect = RectF()

    fun setLevel(percent: Float) {
        val clamped = percent.coerceIn(0f, 100f)
        if (clamped != level) {
            level = clamped
            invalidate()
        }
    }

    private fun colorForLevel(): Int = when {
        level > 50f -> Color.parseColor("#1B9E4B")
        level > 20f -> Color.parseColor("#FFB300")
        else -> Color.parseColor("#FF3B30")
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val radius = h / 2f

        trackRect.set(0f, 0f, w, h)
        canvas.drawRoundRect(trackRect, radius, radius, trackPaint)

        if (level > 0f) {
            val fillW = max(w * (level / 100f), h)
            fillPaint.color = colorForLevel()
            fillRect.set(0f, 0f, fillW, h)
            canvas.drawRoundRect(fillRect, radius, radius, fillPaint)
        }

        endLabelPaint.textSize = h * 0.5f
        canvas.drawText("E", h * 0.5f, h / 2f + endLabelPaint.textSize / 3f, endLabelPaint)
        canvas.drawText("F", w - h * 0.5f, h / 2f + endLabelPaint.textSize / 3f, endLabelPaint)

        valueTextPaint.textSize = h * 0.55f
        canvas.drawText(
            "${level.toInt()}%",
            w / 2f,
            h / 2f + valueTextPaint.textSize / 3f,
            valueTextPaint
        )
    }
}
