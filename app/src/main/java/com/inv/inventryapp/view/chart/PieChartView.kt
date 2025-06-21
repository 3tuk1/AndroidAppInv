package com.inv.inventryapp.view.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class PieChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectF = RectF()
    private var data: List<Float> = emptyList()
    private var colors: List<Int> = listOf(
        Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN, Color.MAGENTA
    )
    private val holePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE // 背景色に合わせて変更してください
    }
    private var holeRadiusRatio = 0.5f // 中央の穴の半径の割合

    fun setData(data: List<Float>) {
        this.data = data
        invalidate() // データをセットしたら再描画
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (data.isEmpty()) {
            return
        }

        val total = data.sum()
        val width = width.toFloat()
        val height = height.toFloat()
        val radius = Math.min(width, height) / 2f
        val centerX = width / 2f
        val centerY = height / 2f

        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)

        var startAngle = 0f
        data.forEachIndexed { index, value ->
            val sweepAngle = value / total * 360f
            paint.color = colors[index % colors.size]
            canvas.drawArc(rectF, startAngle, sweepAngle, true, paint)
            startAngle += sweepAngle
        }

        // 中央の穴を描画
        canvas.drawCircle(centerX, centerY, radius * holeRadiusRatio, holePaint)
    }
}
