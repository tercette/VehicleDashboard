package com.volks.vehicledashboard.presentation.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlin.math.min
import kotlin.math.roundToInt

class PedalView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private var accentColor = Color.parseColor("#2ECC71")
    private var label = ""

    private var isDown = false
    private var pressProgress = 0f
    private var animator: ValueAnimator? = null

    var onPressedChange: ((Boolean) -> Unit)? = null

    private val platePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; color = Color.parseColor("#3A3F45")
    }
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#66000000")
    }
    private val holeFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1B1E22")
    }
    private val holeRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; color = Color.parseColor("#F3F5F7")
    }
    private val holeShadePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; color = Color.parseColor("#55000000")
    }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; textAlign = Paint.Align.CENTER; isFakeBoldText = true
    }

    private var plateShader: LinearGradient? = null
    private val plateRect = RectF()
    private val shadowRect = RectF()

    fun configure(accentColor: Int, label: String) {
        this.accentColor = accentColor
        this.label = label
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        plateShader = LinearGradient(
            0f, 0f, 0f, h.toFloat(),
            intArrayOf(
                Color.parseColor("#E9EDF0"),
                Color.parseColor("#FBFCFD"),
                Color.parseColor("#C2C9D0"),
                Color.parseColor("#949BA3"),
                Color.parseColor("#6E757C")
            ),
            floatArrayOf(0f, 0.18f, 0.5f, 0.8f, 1f),
            Shader.TileMode.CLAMP
        )
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val pad = w * 0.03f
        val labelH = if (label.isEmpty()) 0f else h * 0.17f
        val availH = h - labelH

        val basePlateH = availH - pad * 2f
        val plateH = basePlateH * (1f - 0.06f * pressProgress)
        val plateW = (w - pad * 2f) * (1f - 0.02f * pressProgress)
        val cx = w / 2f
        val topY = pad + (basePlateH - plateH)
        plateRect.set(cx - plateW / 2f, topY, cx + plateW / 2f, topY + plateH)
        val corner = plateH * 0.16f

        shadowRect.set(plateRect)
        shadowRect.offset(0f, h * 0.02f)
        canvas.drawRoundRect(shadowRect, corner, corner, shadowPaint)

        platePaint.shader = plateShader
        canvas.drawRoundRect(plateRect, corner, corner, platePaint)

        borderPaint.strokeWidth = plateH * 0.02f
        canvas.drawRoundRect(plateRect, corner, corner, borderPaint)

        drawHoles(canvas)

        if (pressProgress > 0.01f) {
            glowPaint.color = accentColor
            glowPaint.alpha = (255 * pressProgress).roundToInt().coerceIn(0, 255)
            glowPaint.strokeWidth = plateH * 0.04f
            canvas.drawRoundRect(plateRect, corner, corner, glowPaint)
        }

        if (label.isNotEmpty()) {
            labelPaint.textSize = labelH * 0.55f
            canvas.drawText(label, cx, h - labelH * 0.25f, labelPaint)
        }
    }

    private fun drawHoles(canvas: Canvas) {
        val labelH = if (label.isEmpty()) 0f else height * 0.17f
        val refW = width.toFloat()
        val refH = height.toFloat() - labelH
        val refCell = min(refW, refH) / 3.2f
        val cols = (refW / refCell).roundToInt().coerceAtLeast(2)
        val rows = (refH / refCell).roundToInt().coerceAtLeast(2)

        val marginX = plateRect.width() * 0.12f
        val marginY = plateRect.height() * 0.12f
        val left = plateRect.left + marginX
        val top = plateRect.top + marginY
        val spanW = plateRect.width() - marginX * 2f
        val spanH = plateRect.height() - marginY * 2f

        val spacing = min(spanW / (cols - 1), spanH / (rows - 1))
        val r = spacing * 0.34f * (1f - 0.28f * pressProgress)
        if (r <= 0.5f) return

        holeRingPaint.strokeWidth = r * 0.26f
        holeShadePaint.strokeWidth = r * 0.26f

        for (row in 0 until rows) {
            val hy = top + spanH * row / (rows - 1)
            if (row % 2 == 0) {
                for (col in 0 until cols) {
                    val hx = left + spanW * col / (cols - 1)
                    drawHole(canvas, hx, hy, r)
                }
            } else {
                for (col in 0 until cols - 1) {
                    val hx = left + spanW * (col + 0.5f) / (cols - 1)
                    drawHole(canvas, hx, hy, r)
                }
            }
        }
    }

    private fun drawHole(canvas: Canvas, hx: Float, hy: Float, r: Float) {
        canvas.drawCircle(hx, hy, r, holeFillPaint)
        canvas.drawCircle(hx, hy, r + r * 0.12f, holeRingPaint)
        canvas.drawCircle(hx, hy, r * 0.72f, holeShadePaint)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> { setDown(true); return true }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                setDown(false); performClick(); return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun setDown(down: Boolean) {
        if (down == isDown) return
        isDown = down
        onPressedChange?.invoke(down)
        animatePress(if (down) 1f else 0f)
    }

    private fun animatePress(target: Float) {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(pressProgress, target).apply {
            duration = 130
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                pressProgress = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }
}
