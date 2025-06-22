package com.inv.inventryapp.view.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.inv.inventryapp.R

class PieChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectF = RectF()
    private var data: List<Float> = emptyList()
    private var colors: List<Int> = listOf(
        context.getColor(R.color.pie_color_1),
        context.getColor(R.color.pie_color_2),
        context.getColor(R.color.pie_color_3),
        context.getColor(R.color.pie_color_4),
        context.getColor(R.color.pie_color_5),
        context.getColor(R.color.pie_color_6)
    )
    private val holePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.intellij_background)
    }
    private var holeRadiusRatio = 0.5f // 中央の穴の半径の割合
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.intellij_text_primary)
        textAlign = Paint.Align.CENTER
        textSize = 48f
    }
    private val valueTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 40f
    }

    fun setData(data: List<Float>) {
        this.data = data
        invalidate() // データをセットしたら再描画
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f

        if (data.isEmpty() || data.all { it == 0f }) {
            canvas.drawText("データがありません", centerX, centerY, textPaint)
            return
        }

        val total = data.sum()
        val radius = Math.min(width, height) / 2f

        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)

        var startAngle = -90f
        if (total > 0f) {
            data.forEachIndexed { index, value ->
                if (value == 0f) {
                    return@forEachIndexed
                }
                val sweepAngle = value / total * 360f
                paint.color = colors[index % colors.size]
                canvas.drawArc(rectF, startAngle, sweepAngle, true, paint)

                // Draw value text
                val textAngle = Math.toRadians((startAngle + sweepAngle / 2).toDouble())
                val textRadius = radius * (holeRadiusRatio + (1 - holeRadiusRatio) / 2)
                val textX = centerX + (textRadius * Math.cos(textAngle)).toFloat()
                val textY = centerY + (textRadius * Math.sin(textAngle)).toFloat()
                val text = value.toInt().toString()
                val bounds = Rect()
                valueTextPaint.getTextBounds(text, 0, text.length, bounds)
                canvas.drawText(text, textX, textY + bounds.height() / 2, valueTextPaint)

                startAngle += sweepAngle
            }
        }

        // 中央の穴を描画
        canvas.drawCircle(centerX, centerY, radius * holeRadiusRatio, holePaint)
    }
}
