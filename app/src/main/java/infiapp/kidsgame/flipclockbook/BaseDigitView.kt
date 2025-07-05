package infiapp.kidsgame.flipclockbook

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.SparseArray
import android.view.View

abstract class BaseDigitView(ctx: Context) : View(ctx) {

    protected var prev = 0
    protected var next = 0

    // image cache (top & bottom for each digit)
    companion object {
        private val topCache = SparseArray<Bitmap>()
        private val bottomCache = SparseArray<Bitmap>()

        fun getHalf(context: Context, digit: Int, top: Boolean): Bitmap {
            val cache = if (top) topCache else bottomCache
            cache[digit]?.let { return it }

            val resId = context.resources.getIdentifier(
                "number_${digit}_${if (top) "upper" else "lower"}",
                "drawable",
                context.packageName
            )

            if (resId == 0) {
                // fallback: red box if image not found
                val fallback = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
                val c = Canvas(fallback)
                val p = Paint().apply { color = Color.RED }
                c.drawRect(0f, 0f, 100f, 100f, p)
                return fallback
            }

            val bmp = BitmapFactory.decodeResource(context.resources, resId)
                ?: throw NullPointerException("Image decode failed for digit $digit (${if (top) "upper" else "lower"})")

            cache.put(digit, bmp)
            return bmp
        }

    }

    abstract fun setDigit(d: Int)
}
