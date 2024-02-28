package com.example.mms.Utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class CircleProgressBar(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var progress: Int = 0
    private val borderPaint = Paint()

    init {
        borderPaint.isAntiAlias = true
        borderPaint.color = Color.BLUE // Couleur de la bordure
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 15f // Épaisseur de la bordure
        borderPaint.strokeCap = Paint.Cap.BUTT // Extrémités de la bordure
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width
        val height = height
        val minSize = width.coerceAtMost(height)
        val halfWidth = width / 2f
        val halfHeight = height / 2f
        val radius = minSize / 2f - borderPaint.strokeWidth / 2f


        // Dessin du trait représentant la progression
        val angle = 360f * progress / 100f
        val oval = RectF(
            halfWidth - radius,
            halfHeight - radius,
            halfWidth + radius,
            halfHeight + radius
        )
        canvas.drawArc(oval, -90f, angle, false, borderPaint)
    }


    fun setProgress(progress: Int) {
        this.progress = progress
        invalidate()
    }
}

