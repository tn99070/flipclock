package infiapp.kidsgame.flipclockbook

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.animation.DecelerateInterpolator

/**
 * Book‑style page flip (rotationY) for optional use.
 * Combines full digit (top+bottom) into one bitmap.
 */
class BookDigitView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null
) : BaseDigitView(ctx) {

    private var flip = 0f
    private var anim: ValueAnimator? = null
    private val cam = Camera()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    /* Public */
    override fun setDigit(d: Int) {
        if (d == next) return
        prev = next
        next = d
        startAnim()
    }

    /* Drawing */
    override fun onDraw(c: Canvas) {
        if (width == 0 || height == 0) return

        c.save()
        c.translate(width / 2f, height / 2f)

        cam.save()
        cam.setLocation(0f, 0f, -8 * resources.displayMetrics.density)
        cam.rotateY(flip)
        cam.applyToCanvas(c)
        cam.restore()

        c.translate(-width / 2f, -height / 2f)

        val showDigit = if (flip < 90f) prev else next
        c.drawBitmap(getScaled(showDigit), 0f, 0f, null)
        c.restore()

        drawShadow(c)
    }

    /* Animation */
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

    /* Simple dark overlay proportional to angle */
    private fun drawShadow(c: Canvas) {
        val alpha = if (flip < 90f) flip / 90f else (180 - flip) / 90f
        paint.color = Color.BLACK
        paint.alpha = (alpha * 120).toInt()
        c.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }

    /* Merge top+bottom once, then scale */
    private val mergedCache = HashMap<Int, Bitmap>()
    private fun getScaled(d: Int): Bitmap {
        mergedCache[d]?.let { return it }

        val topSrc = getHalf(context, d, true)
        val bottomSrc = getHalf(context, d, false)

        val merged = Bitmap.createBitmap(
            topSrc.width,
            topSrc.height + bottomSrc.height,
            Bitmap.Config.ARGB_8888
        )
        val cv = Canvas(merged)
        cv.drawBitmap(topSrc, 0f, 0f, null)
        cv.drawBitmap(bottomSrc, 0f, topSrc.height.toFloat(), null)

        val scaled = Bitmap.createScaledBitmap(merged, width, height, true)
        mergedCache[d] = scaled
        return scaled
    }
}

