package infiapp.kidsgame.flipclockbook

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.animation.ValueAnimator

class FlipDigitView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var oldDigit = 0
    private var newDigit = 0

    private var digitTop: Bitmap? = null
    private var digitBottom: Bitmap? = null
    private var nextDigitTop: Bitmap? = null
    private var nextDigitBottom: Bitmap? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var flipRotation = 0f
    private val camera = Camera()
    private var animator: ValueAnimator? = null

    fun setDigit(newDigit: Int) {
        if (this.newDigit == newDigit) return
        oldDigit = this.newDigit
        this.newDigit = newDigit
        prepareBitmaps()
        startFlip()
    }

    private fun prepareBitmaps() {
        digitTop = createDigitBitmap(oldDigit, true)
        digitBottom = createDigitBitmap(oldDigit, false)
        nextDigitTop = createDigitBitmap(newDigit, true)
        nextDigitBottom = createDigitBitmap(newDigit, false)
    }

    private fun createDigitBitmap(digit: Int, isTop: Boolean): Bitmap {
        val bmp = Bitmap.createBitmap(width, height / 2, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        paint.color = Color.BLACK
        canvas.drawRect(0f, 0f, width.toFloat(), height / 2f, paint)

        paint.color = Color.WHITE
        paint.textSize = height * 0.6f
        paint.textAlign = Paint.Align.CENTER

        val y = (height / 4 - (paint.descent() + paint.ascent()) / 2)
        canvas.drawText("$digit", width / 2f, y, paint)

        return bmp
    }

    private fun startFlip() {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, 180f).apply {
            duration = 600
            interpolator = LinearInterpolator()
            addUpdateListener {
                flipRotation = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }
/*
    override fun onDraw1(canvas: Canvas) {
        if (digitTop == null || nextDigitTop == null) return

        val centerX = width / 2f
        val centerY = height / 2f

        // Draw static top
        canvas.drawBitmap(digitTop!!, 0f, 0f, null)

        // Draw static bottom or flipped part
        if (flipRotation < 90f) {
            canvas.drawBitmap(digitBottom!!, 0f, centerY, null)
        } else {
            canvas.drawBitmap(nextDigitBottom!!, 0f, centerY, null)
        }

        // Draw flipping layer
        canvas.save()
        canvas.translate(centerX, centerY)
        camera.save()
        camera.rotateX(if (flipRotation < 90f) -flipRotation else 180f - flipRotation)
        camera.applyToCanvas(canvas)
        camera.restore()
        canvas.translate(-centerX, -centerY)

        if (flipRotation < 90f) {
            canvas.drawBitmap(digitBottom!!, 0f, centerY, null)
        } else {
            canvas.drawBitmap(nextDigitTop!!, 0f, 0f, null)
        }
        canvas.restore()
    }

 */

    override fun onDraw(canvas: Canvas) {
        if (digitTop == null || nextDigitTop == null) return

        val centerX = width / 2f
        val centerY = height / 2f

        // Draw top half (static)
        canvas.drawBitmap(digitTop!!, 0f, 0f, null)

        // Draw bottom half (static or flipped)
        val bottomPart = if (flipRotation < 90f) digitBottom!! else nextDigitBottom!!
        canvas.drawBitmap(bottomPart, 0f, centerY, null)

        // --- DRAW DIVIDER LINE ---
        val dividerPaint = Paint().apply {
            color = Color.DKGRAY
            strokeWidth = 2f
        }
        canvas.drawLine(0f, centerY, width.toFloat(), centerY, dividerPaint)

        // --- FLIP ANIMATION ---
        canvas.save()
        canvas.translate(centerX, centerY)
        camera.save()

        val flipAngle = if (flipRotation < 90f) -flipRotation else 180f - flipRotation
        camera.rotateX(flipAngle)
        camera.applyToCanvas(canvas)
        camera.restore()
        canvas.translate(-centerX, -centerY)

        // Draw flipping part
        if (flipRotation < 90f) {
            canvas.drawBitmap(digitBottom!!, 0f, centerY, null)
            drawFlipShadow(canvas, false) // bottom flipping
        } else {
            canvas.drawBitmap(nextDigitTop!!, 0f, 0f, null)
            drawFlipShadow(canvas, true) // top flipping
        }

        canvas.restore()
    }

    private fun drawFlipShadow(canvas: Canvas, isTop: Boolean) {
        val gradient = LinearGradient(
            0f, 0f, 0f, height / 2f,
            if (isTop) 0x80000000.toInt() else 0x00000000,
            if (isTop) 0x00000000 else 0x80000000.toInt(),
            Shader.TileMode.CLAMP
        )
        paint.shader = gradient
        canvas.drawRect(
            0f,
            if (isTop) 0f else height / 2f,
            width.toFloat(),
            if (isTop) height / 2f else height.toFloat(),
            paint
        )
        paint.shader = null
    }


}
