package com.volks.vehicledashboard.presentation.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class GearIndicatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private val positions = listOf("P", "R", "N", "D")

    private var gear = "P"

    private val accent = Color.parseColor("#00E5FF")

    private val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = (0x33 shl 24) or (accent and 0x00FFFFFF)
    }
    private val activePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; textAlign = Paint.Align.CENTER; isFakeBoldText = true
    }
    private val inactivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3A4656"); textAlign = Paint.Align.CENTER
    }
    private val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = accent; textAlign = Paint.Align.CENTER; isFakeBoldText = true
    }
    private val pillRect = RectF()

    fun setGear(gear: String) {
        if (gear != this.gear) {
            this.gear = gear
            invalidate()
        }
    }

    private fun isDrive() = gear.toIntOrNull()?.let { it in 1..6 } == true

    private fun activePosition(): String = if (isDrive()) "D" else gear

    override fun onDraw(canvas: Canvas) {
        val cx = width / 2f
        val itemH = height / positions.size.toFloat()
        val active = activePosition()
        val activeSize = itemH * 0.60f
        val inactiveSize = itemH * 0.42f

        positions.forEachIndexed { i, pos ->
            val centerY = itemH * i + itemH / 2f

            if (pos == active) {
                // pílula
                val halfH = itemH * 0.40f
                val halfW = width * 0.44f
                pillRect.set(cx - halfW, centerY - halfH, cx + halfW, centerY + halfH)
                canvas.drawRoundRect(pillRect, halfH, halfH, pillPaint)

                activePaint.textSize = activeSize
                val baseY = centerY + activeSize / 3f
                if (isDrive()) {
                    numberPaint.textSize = activeSize * 0.66f
                    canvas.drawText("D", cx - activeSize * 0.28f, baseY, activePaint)
                    canvas.drawText(gear, cx + activeSize * 0.42f, baseY, numberPaint)
                } else {
                    canvas.drawText(pos, cx, baseY, activePaint)
                }
            } else {
                inactivePaint.textSize = inactiveSize
                canvas.drawText(pos, cx, centerY + inactiveSize / 3f, inactivePaint)
            }
        }
    }
}
