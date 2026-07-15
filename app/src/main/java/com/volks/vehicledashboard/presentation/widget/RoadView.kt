package com.volks.vehicledashboard.presentation.widget

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.Choreographer
import android.view.View
import kotlin.math.ceil

class RoadView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private var speedKmh = 0f
    private var offset = 0f

    private var running = false
    private var lastFrameNanos = 0L

    private val cyan = Color.parseColor("#21E6FF")
    private val orange = Color.parseColor("#FF7A18")

    private val trackGlow = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = cyan }
    private val trackCore = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = Color.parseColor("#CFFBFF") }
    private val carFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL; color = Color.parseColor("#CCFF7A18") }
    private val carGlow = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = orange; strokeJoin = Paint.Join.ROUND }
    private val carCore = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = Color.parseColor("#FFE0BE"); strokeJoin = Paint.Join.ROUND }

    private val clipPath = Path()
    private val carPath = Path()

    private val nearZ = 1f
    private val farZ = 22f
    private val spacing = 2.2f

    private val frameCallback = Choreographer.FrameCallback { now -> onFrame(now) }

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null) // BlurMaskFilter exige camada de software
    }

    fun setSpeed(kmh: Float) { speedKmh = kmh }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        running = true
        lastFrameNanos = 0L
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        running = false
        Choreographer.getInstance().removeFrameCallback(frameCallback)
    }

    private fun onFrame(now: Long) {
        if (!running) return
        if (lastFrameNanos != 0L && speedKmh > 0.5f) {
            val dt = ((now - lastFrameNanos) / 1_000_000_000.0).toFloat().coerceAtMost(0.05f)
            offset = (offset + speedKmh * dt * 0.20f) % 1_000_000f
            invalidate()
        }
        lastFrameNanos = now
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val glow = w * 0.02f
        trackGlow.maskFilter = BlurMaskFilter(glow, BlurMaskFilter.Blur.NORMAL)
        carGlow.maskFilter = BlurMaskFilter(glow, BlurMaskFilter.Blur.NORMAL)
        trackGlow.strokeWidth = w * 0.020f
        trackCore.strokeWidth = w * 0.007f
        carGlow.strokeWidth = w * 0.022f
        carCore.strokeWidth = w * 0.008f
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        val corner = h * 0.10f
        clipPath.reset()
        clipPath.addRoundRect(0f, 0f, w, h, corner, corner, Path.Direction.CW)
        canvas.clipPath(clipPath)

        canvas.drawColor(Color.parseColor("#05070A"))

        val horizon = h * 0.30f
        val cx = w / 2f
        val topHW = w * 0.03f
        val botHW = w * 0.46f

        drawGrid(canvas, cx, horizon, h, topHW, botHW)
        glowLine(canvas, cx - topHW, horizon, cx - botHW, h, trackGlow, trackCore)
        glowLine(canvas, cx + topHW, horizon, cx + botHW, h, trackGlow, trackCore)

        drawPlayerCar(canvas, w, h)
    }

    private fun glowLine(canvas: Canvas, x1: Float, y1: Float, x2: Float, y2: Float, glow: Paint, core: Paint) {
        canvas.drawLine(x1, y1, x2, y2, glow)
        canvas.drawLine(x1, y1, x2, y2, core)
    }

    private fun drawGrid(canvas: Canvas, cx: Float, horizon: Float, h: Float, topHW: Float, botHW: Float) {
        val sHorizon = nearZ / farZ
        var k = ceil((offset + nearZ) / spacing).toInt()
        while (true) {
            val z = k * spacing - offset
            if (z > farZ) break
            if (z >= nearZ) {
                val s = nearZ / z
                val yNorm = (s - sHorizon) / (1f - sHorizon)
                val y = horizon + (h - horizon) * yNorm
                val roadHW = topHW + (botHW - topHW) * yNorm
                glowLine(canvas, cx - roadHW, y, cx + roadHW, y, trackGlow, trackCore)
            }
            k++
        }
    }

    private fun drawPlayerCar(canvas: Canvas, w: Float, h: Float) {
        val ccx = w / 2f
        val bottom = h * 0.95f
        val cH = h * 0.26f
        val top = bottom - cH
        val topHW = w * 0.11f
        val botHW = w * 0.17f

        carPath.reset()
        carPath.moveTo(ccx - topHW, top)
        carPath.lineTo(ccx + topHW, top)
        carPath.lineTo(ccx + botHW, bottom)
        carPath.lineTo(ccx - botHW, bottom)
        carPath.close()

        canvas.drawPath(carPath, carFill)
        canvas.drawPath(carPath, carGlow)
        canvas.drawPath(carPath, carCore)
    }
}
