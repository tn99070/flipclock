package infiapp.kidsgame.flipclockbook

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.animation.DecelerateInterpolator

/**
 * Physical flip‑clock style:
 *  – Separate top & bottom halves (digit images from BaseDigitView)
 *  – Cubic 3‑D flip on the X‑axis (TOP → BOTTOM)
 */
class CubicDigitView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null
) : BaseDigitView(ctx) {

    /** current animation angle 0‒180 deg */
    private var flip = 0f

    private val cam = Camera()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var anim: ValueAnimator? = null

    /** Public API */
    override fun setDigit(d: Int) {
        if (d == next) return           // nothing to do
        prev = next
        next = d
        startAnim()
    }

    /* ─────────────────────────  Drawing  ───────────────────────── */

    override fun onDraw(c: Canvas) {
        if (width == 0 || height == 0) return

        val halfH = height / 2f

        /* ---- 1.  Static halves (background) ---- */
        c.drawBitmap(getScaled(prev, true), 0f, 0f, null)
        c.drawBitmap(
            if (flip < 90f) getScaled(prev, false) else getScaled(next, false),
            0f, halfH, null
        )

        /* Divider line */
        paint.color = Color.DKGRAY
        paint.strokeWidth = 2f
        c.drawLine(0f, halfH, width.toFloat(), halfH, paint)

        /* ---- 2.  Flipping half ---- */
        c.save()
        c.translate(width / 2f, halfH)

        cam.save()
        // Deeper perspective → more realistic
        cam.setLocation(0f, 0f, -8 * resources.displayMetrics.density)
        //  TOP‑to‑BOTTOM direction
        cam.rotateX(if (flip < 90f) flip else flip - 180)
        cam.applyToCanvas(c)
        cam.restore()

        c.translate(-width / 2f, -halfH)

        if (flip < 90f) {
            // Bottom half of OLD digit is moving down
            c.drawBitmap(getScaled(prev, false), 0f, halfH, null)
            drawShadow(c, isTop = false)
        } else {
            // Top half of NEW digit is moving down
            c.drawBitmap(getScaled(next, true), 0f, 0f, null)
            drawShadow(c, isTop = true)
        }
        c.restore()
    }

    /* ───────────────────────  Helpers  ─────────────────────── */

    private fun startAnim() {
        anim?.cancel()
        anim = ValueAnimator.ofFloat(0f, 180f).apply {
            duration = 600
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                flip = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    /** Gradient overlay to sell the 3‑D curvature */
    private fun drawShadow(c: Canvas, isTop: Boolean) {
        val shader = LinearGradient(
            0f, 0f, 0f, height / 2f,
            if (isTop) 0x90000000.toInt() else 0x00000000,
            if (isTop) 0x00000000 else 0x90000000.toInt(),
            Shader.TileMode.CLAMP
        )
        paint.shader = shader
        c.drawRect(
            0f,
            if (isTop) 0f else height / 2f,
            width.toFloat(),
            if (isTop) height / 2f else height.toFloat(),
            paint
        )
        paint.shader = null
    }

    /* Cache of scaled bitmaps to avoid re‑scaling each frame */
    private val scaledTop = HashMap<Int, Bitmap>()
    private val scaledBottom = HashMap<Int, Bitmap>()

    private fun getScaled(digit: Int, top: Boolean): Bitmap {
        val map = if (top) scaledTop else scaledBottom
        map[digit]?.let { return it }

        val src = getHalf(context, digit, top)
        val bmp = Bitmap.createScaledBitmap(src, width, height / 2, true)
        map[digit] = bmp
        return bmp
    }
}

