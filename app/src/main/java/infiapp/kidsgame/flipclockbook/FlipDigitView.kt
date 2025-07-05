package infiapp.kidsgame.flipclockbook

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import android.animation.ValueAnimator

class FlipDigitView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : BaseDigitView(context) { // Inherit from BaseDigitView

    // oldDigit is prev, newDigit is next from BaseDigitView
    // private var oldDigit = 0
    // private var newDigit = 0

    private var currentTopBitmap: Bitmap? = null
    private var currentBottomBitmap: Bitmap? = null
    private var nextTopBitmap: Bitmap? = null
    private var nextBottomBitmap: Bitmap? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var flipRotation = 0f
    private val camera = Camera()
    private var animator: ValueAnimator? = null

    // Implement abstract method from BaseDigitView
    override fun setDigit(d: Int) {
        if (d == next) return // Already showing this digit or animating to it
        prev = next // Current digit becomes the previous one
        next = d    // New digit is the next one
        prepareBitmaps()
        startFlip()
    }

    private fun prepareBitmaps() {
        if (width == 0 || height == 0) return // Not measured yet

        // prev is the digit currently shown (will animate out)
        // next is the new digit (will animate in)
        val prevTopSrc = getHalf(context, prev, true)
        val prevBottomSrc = getHalf(context, prev, false)
        val nextTopSrc = getHalf(context, next, true)
        val nextBottomSrc = getHalf(context, next, false)

        // Scale bitmaps to fit view dimensions
        currentTopBitmap = Bitmap.createScaledBitmap(prevTopSrc, width, height / 2, true)
        currentBottomBitmap = Bitmap.createScaledBitmap(prevBottomSrc, width, height / 2, true)
        nextTopBitmap = Bitmap.createScaledBitmap(nextTopSrc, width, height / 2, true)
        nextBottomBitmap = Bitmap.createScaledBitmap(nextBottomSrc, width, height / 2, true)
    }

    // Removed createDigitBitmap method as we now use getHalf from BaseDigitView

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

    // Paint for the divider line, initialized once
    private val dividerPaint = Paint().apply {
        color = Color.BLACK // Or Color.TRANSPARENT if images have built-in dividers
        strokeWidth = 1f // A thin line
    }

    // Camera Z distance, affects perspective. Adjusted for screen density.
    private val cameraZ = -8 * resources.displayMetrics.density

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // If size changes, bitmaps need to be re-prepared with new dimensions
        if (w > 0 && h > 0) {
            prepareBitmaps()
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (width == 0 || height == 0) return // Not measured yet

        // Ensure bitmaps are valid. This should be handled by setDigit and onSizeChanged.
        if (currentTopBitmap == null || currentBottomBitmap == null || nextTopBitmap == null || nextBottomBitmap == null) {
            // Fallback: if somehow bitmaps are null, try to prepare them.
            // This indicates an issue elsewhere if reached frequently.
            prepareBitmaps()
            // If still null, can't draw.
            if (currentTopBitmap == null || currentBottomBitmap == null || nextTopBitmap == null || nextBottomBitmap == null) return
        }

        val w = width.toFloat()
        val h = height.toFloat()
        val centerX = w / 2f
        val centerY = h / 2f

        // 1. Draw the static top half of the current digit (prev).
        // This is the half that remains visible until the flip animation covers it.
        canvas.drawBitmap(currentTopBitmap!!, 0f, 0f, paint)

        // 2. Draw the static bottom half of the next digit.
        // This is the half that will be revealed after the flip.
        canvas.drawBitmap(nextBottomBitmap!!, 0f, centerY, paint)

        // --- OPTIONAL: DRAW DIVIDER LINE ---
        // Draw a line separating the top and bottom halves.
        // Consider removing if your digit images already include such a line.
        canvas.drawLine(0f, centerY, w, centerY, dividerPaint)

        // --- FLIP ANIMATION ---
        // The flipping piece changes based on the angle of rotation.
        if (flipRotation < 90f) {
            // Phase 1: The bottom half of the current digit (prev) is flipping down.
            // It rotates around the X-axis at centerY.
            canvas.save()
            camera.save()
            camera.setLocation(0f, 0f, cameraZ) // Set camera distance
            camera.rotateX(flipRotation)        // Positive rotation makes the bottom part come towards the viewer.
            canvas.translate(centerX, centerY)  // Move canvas origin to the center of the view for rotation.
            camera.applyToCanvas(canvas)
            canvas.translate(-centerX, -centerY) // Restore canvas origin.
            camera.restore()

            // Clip to draw only the top half (where the flipping part appears).
            canvas.clipRect(0f, 0f, w, centerY)
            // Draw the current bottom bitmap as if it's a top half flipping down.
            canvas.drawBitmap(currentBottomBitmap!!, 0f, 0f, paint)
            // Optional: drawFlipShadow(canvas, true) // Shadow on the flipping bottom-half.
            canvas.restore()
        } else {
            // Phase 2: The top half of the next digit is flipping down.
            // It has already rotated past 90 degrees (invisible, facing away) and is now rotating into view.
            canvas.save()
            camera.save()
            camera.setLocation(0f, 0f, cameraZ) // Set camera distance
            // As flipRotation goes from 90 to 180, this rotation goes from 90 down to 0.
            // This makes the top half of the next digit appear to rotate in from the top.
            camera.rotateX(180f - flipRotation)
            canvas.translate(centerX, centerY)  // Move canvas origin for rotation.
            camera.applyToCanvas(canvas)
            canvas.translate(-centerX, -centerY) // Restore canvas origin.
            camera.restore()

            // Clip to draw only the bottom half (where this flipping part appears).
            canvas.clipRect(0f, centerY, w, h)
            // Draw the next top bitmap as if it's a bottom half.
            canvas.drawBitmap(nextTopBitmap!!, 0f, centerY, paint)
            // Optional: drawFlipShadow(canvas, false) // Shadow on the flipping top-half.
            canvas.restore()
        }
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
