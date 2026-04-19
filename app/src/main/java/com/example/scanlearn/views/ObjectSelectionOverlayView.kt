package com.example.scanlearn.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.scanlearn.models.ObjectSelectionBox
import kotlin.math.max
import kotlin.math.min

class ObjectSelectionOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }

    private val selectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFB300")
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    private val dimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#88000000")
        style = Paint.Style.FILL
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 36f
        style = Paint.Style.FILL
    }

    private var boxes: List<Rect> = emptyList()
    private var selectedIndex = -1
    private var imageWidth = 0
    private var imageHeight = 0
    private var onBoxSelected: ((Rect) -> Unit)? = null

    fun bindImageSize(width: Int, height: Int) {
        imageWidth = width
        imageHeight = height
        invalidate()
    }

    fun setBoxes(boxes: List<Rect>) {
        this.boxes = boxes
        selectedIndex = if (boxes.size == 1) 0 else -1
        invalidate()
        if (selectedIndex >= 0) {
            onBoxSelected?.invoke(boxes[selectedIndex])
        }
    }

    fun setOnBoxSelectedListener(listener: (Rect) -> Unit) {
        onBoxSelected = listener
        if (selectedIndex >= 0 && boxes.indices.contains(selectedIndex)) {
            listener(boxes[selectedIndex])
        }
    }

    fun getSelectedBox(): ObjectSelectionBox? {
        if (!boxes.indices.contains(selectedIndex)) return null
        val box = boxes[selectedIndex]
        return ObjectSelectionBox(box.left, box.top, box.right, box.bottom)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (imageWidth <= 0 || imageHeight <= 0 || boxes.isEmpty()) return

        val imageRect = getImageDisplayRect()
        canvas.drawRect(0f, 0f, width.toFloat(), imageRect.top, dimPaint)
        canvas.drawRect(0f, imageRect.bottom, width.toFloat(), height.toFloat(), dimPaint)
        canvas.drawRect(0f, imageRect.top, imageRect.left, imageRect.bottom, dimPaint)
        canvas.drawRect(imageRect.right, imageRect.top, width.toFloat(), imageRect.bottom, dimPaint)

        boxes.forEachIndexed { index, rect ->
            val mapped = mapRect(rect, imageRect)
            val paint = if (index == selectedIndex) selectedPaint else boxPaint
            canvas.drawRoundRect(mapped, 20f, 20f, paint)
            if (index == selectedIndex) {
                canvas.drawText("Selected", mapped.left, max(40f, mapped.top - 16f), labelPaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_UP || imageWidth <= 0 || imageHeight <= 0) {
            return true
        }

        val imageRect = getImageDisplayRect()
        val tappedIndex = boxes.indexOfFirst { rect ->
            mapRect(rect, imageRect).contains(event.x, event.y)
        }

        if (tappedIndex >= 0) {
            selectedIndex = tappedIndex
            invalidate()
            onBoxSelected?.invoke(boxes[tappedIndex])
        }

        return true
    }

    private fun getImageDisplayRect(): RectF {
        val scale = min(width.toFloat() / imageWidth, height.toFloat() / imageHeight)
        val scaledWidth = imageWidth * scale
        val scaledHeight = imageHeight * scale
        val left = (width - scaledWidth) / 2f
        val top = (height - scaledHeight) / 2f
        return RectF(left, top, left + scaledWidth, top + scaledHeight)
    }

    private fun mapRect(rect: Rect, imageRect: RectF): RectF {
        val widthScale = imageRect.width() / imageWidth
        val heightScale = imageRect.height() / imageHeight
        return RectF(
            imageRect.left + (rect.left * widthScale),
            imageRect.top + (rect.top * heightScale),
            imageRect.left + (rect.right * widthScale),
            imageRect.top + (rect.bottom * heightScale)
        )
    }
}
