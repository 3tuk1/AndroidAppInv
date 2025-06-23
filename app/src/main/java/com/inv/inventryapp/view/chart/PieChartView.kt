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

    // ▼▼▼ 新しい配色を読み込むように変更 ▼▼▼
    private var colors: List<Int> = listOf(
        context.getColor(R.color.pie_color_purchase),
        context.getColor(R.color.pie_color_consumption),
        context.getColor(R.color.pie_color_disposal),
        context.getColor(R.color.pie_color_other)
    )

    private val holePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.intellij_background)
    }
    private var holeRadiusRatio = 0.6f // 中央の穴を少し大きく

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.intellij_text_primary)
        textAlign = Paint.Align.CENTER
        textSize = 60f // メインテキストを大きく
        isFakeBoldText = true
    }
    private val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.intellij_text_hint)
        textAlign = Paint.Align.CENTER
        textSize = 40f
    }
    private val valueTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 36f
    }

    private var centerText: String = "0円"
    private var centerSubText: String = "今月の合計"
    private val textBounds = Rect()

    fun setData(data: List<Float>, dataColors: List<Int>? = null) {
        this.data = data
        dataColors?.let { this.colors = it }
        invalidate()
    }

    fun setCenterText(mainText: String, subText: String) {
        this.centerText = mainText
        this.centerSubText = subText
        invalidate()
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
        val radius = Math.min(width, height) / 2f * 0.9f // 少しパディング

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

                startAngle += sweepAngle
            }
        }

        // 中央の穴を描画
        canvas.drawCircle(centerX, centerY, radius * holeRadiusRatio, holePaint)

        // 中央のテキストを描画
        textPaint.getTextBounds(centerText, 0, centerText.length, textBounds)
        canvas.drawText(centerText, centerX, centerY - textBounds.height() / 2 + 20, textPaint)
        subTextPaint.getTextBounds(centerSubText, 0, centerSubText.length, textBounds)
        canvas.drawText(centerSubText, centerX, centerY + textBounds.height() + 30, subTextPaint)
    }
}
