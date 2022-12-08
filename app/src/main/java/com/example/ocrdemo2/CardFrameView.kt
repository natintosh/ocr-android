package com.example.ocrdemo2

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class CardFrameView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        const val DEBIT_CARD_ASPECT_RATIO = 1.586
    }


    private val borderWidth: Float
    private val borderRound: Float
    private val borderColor: Int


    init {

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CardFrameView)

        typedArray.apply {
            try {
                borderWidth = getDimension(
                    R.styleable.CardFrameView_borderWidth,
                    context.resources.getDimension(R.dimen.card_frame_view_border_width)
                )
                borderRound = getDimension(
                    R.styleable.CardFrameView_borderRound,
                    context.resources.getDimension(R.dimen.card_frame_view_border_round)
                )
                borderColor = getResourceId(R.styleable.CardFrameView_borderColor, R.color.white)
            } finally {
                recycle()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)


        val significantSide = if (heightSize > widthSize) widthSize else heightSize

        val width = (significantSide * 10 / 12)

        val height = (width / DEBIT_CARD_ASPECT_RATIO).toInt()


        setMeasuredDimension(width, height)
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (canvas != null) {
            canvas.save()

            drawCardFrame(canvas)
            canvas.restore()
        }
    }

    private fun drawCardFrame(canvas: Canvas) {
        val left = borderWidth
        val right = width - borderWidth
        val top = borderWidth
        val bottom = height - borderWidth
        val rWidth = borderRound * 2
        val rHeight = borderRound * 2

        val paint = Paint().apply {
            color = ContextCompat.getColor(context, borderColor)
            style = Paint.Style.STROKE
            strokeWidth = borderWidth.toFloat()
            pathEffect = DashPathEffect(floatArrayOf(150f, 400f), 10f)
        }
        val path = Path().apply {
            moveTo(left, top + rHeight)
            arcTo(
                left,
                top,
                left + rWidth,
                top + rHeight,
                180f,
                90f,
                true
            )
            moveTo(right - rWidth, top)
            arcTo(
                right - rWidth,
                top,
                right,
                top + rHeight,
                270f,
                90f,
                true
            )
            moveTo(right, bottom - rHeight)
            arcTo(
                right - rWidth,
                bottom - rHeight,
                right,
                bottom,
                0f,
                90f,
                true
            )
            moveTo(left + rWidth, bottom)
            arcTo(
                left,
                bottom - rHeight,
                left + rWidth,
                bottom,
                90f,
                90f,
                true
            )
        }

        canvas.drawPath(path, paint);
    }
}