package infiapp.kidsgame.flipclockbook

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import java.util.*

class FlipClockView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null
) : LinearLayout(ctx, attrs) {

    private val digits = mutableListOf<BaseDigitView>()
    private var flipStyle = Style.CUBIC
    private var dWidthPx = 0
    private var dHeightPx = 0

    enum class Style { CUBIC, BOOK }

    init {
        orientation = HORIZONTAL
        parseAttrs(ctx, attrs)
        buildDigits(ctx)
    }

    private fun parseAttrs(ctx: Context, attrs: AttributeSet?) {
        val ta: TypedArray = ctx.obtainStyledAttributes(attrs, R.styleable.FlipClockView)
        dWidthPx = ta.getDimensionPixelSize(
            R.styleable.FlipClockView_digitWidth,
            dp(80)
        )
        dHeightPx = ta.getDimensionPixelSize(
            R.styleable.FlipClockView_digitHeight,
            dp(120)
        )
        flipStyle = when (ta.getString(R.styleable.FlipClockView_flipStyle) ?: "cubic") {
            "book" -> Style.BOOK
            else -> Style.CUBIC
        }
        ta.recycle()
    }

    private fun buildDigits(ctx: Context) {
        repeat(6) { index ->
            val v: BaseDigitView = when (flipStyle) {
                Style.CUBIC -> CubicDigitView(ctx)
                Style.BOOK -> BookDigitView(ctx)
            }
            v.layoutParams = LayoutParams(dWidthPx, dHeightPx)
            addView(v)
            digits += v

            // colon separators every 2 digits
            if (index == 1 || index == 3) addView(ColonSeparator(ctx, dHeightPx))
        }
    }

    /** call each second */
    fun updateTime() {
        val now = Calendar.getInstance()
        val list = listOf(
            now.get(Calendar.HOUR_OF_DAY) / 10,
            now.get(Calendar.HOUR_OF_DAY) % 10,
            now.get(Calendar.MINUTE) / 10,
            now.get(Calendar.MINUTE) % 10,
            now.get(Calendar.SECOND) / 10,
            now.get(Calendar.SECOND) % 10
        )
        digits.indices.forEach { i -> digits[i].setDigit(list[i]) }
    }

    private fun dp(v: Int) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt()

    /** Fancy blinking colon */
    private class ColonSeparator(ctx: Context, heightPx: Int) : View(ctx) {
        private var show = true
        init {
            layoutParams = LayoutParams(dp(24), heightPx)
            post(object : Runnable {
                override fun run() {
                    show = !show
                    invalidate()
                    postDelayed(this, 500)
                }
            })
        }
        override fun onDraw(c: android.graphics.Canvas) {
            if (!show) return
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                isAntiAlias = true
            }
            val r = width / 8f
            c.drawCircle(width / 2f, height / 3f, r, paint)
            c.drawCircle(width / 2f, 2 * height / 3f, r, paint)
        }
        private fun dp(v: Int) =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt()
    }
}

