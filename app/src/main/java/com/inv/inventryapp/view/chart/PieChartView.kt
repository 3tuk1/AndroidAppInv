package com.inv.inventryapp.view.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
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

        var startAngle = 0f
        if (total > 0f) {
            data.forEachIndexed { index, value ->
                val sweepAngle = value / total * 360f
                paint.color = colors[index % colors.size]
                canvas.drawArc(rectF, startAngle, sweepAngle, true, paint)
                startAngle += sweepAngle
            }
        }

        // 中央の穴を描画
        canvas.drawCircle(centerX, centerY, radius * holeRadiusRatio, holePaint)
    }
}
