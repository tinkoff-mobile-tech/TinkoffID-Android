/*
 * Copyright © 2021 Tinkoff Bank
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.tinkoff.core.tinkoffId.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import ru.tinkoff.core.tinkoffId.R
import kotlin.math.roundToInt

/**
 * @author Dmitry Naymushin
 * A button with an icon and a text which can be used for partner authorization.
 * Right now the icon is not customizable as well as the text (although it's open for i18n)
 * Text should represent a single line string.
 * A minimum of 6dp padding is applied to the top and to the bottom and a minimum of 32dp padding is
 * applied to the sides of the button. There is also a margin of 8dp between the icon and the text.
 * This button should only be used with the wrap_content option for its layout_height attribute and
 * the wrap_content / match_parent / match_constraint option for the layout_width attribute.
 * In order to customize its size, one of the options - "standard" (default) / "compact" should be
 * applied via the app:tinkoff_id_size attribute.
 * In case of the "compact" option height is exactly 40dp and for the "standard" option it could be
 * anything between 40dp and 56dp, depending on the constraints passed from the parent view. As per
 * width, in case of the "compact" option it has the smallest size which is just enough to fit the
 * content, and for the "standard" option it depends on the constraints passed from the parent view
 * but no smaller than enough to fit the content.
 * In case width of a button is more than enough to fit its content, it (the content) is centered
 * horizontally.
 * Size of the icon and size of font increase following the height increase.
 */
public class TinkoffIdSignInButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : androidx.appcompat.widget.AppCompatButton(context, attrs, defStyle) {

    private val minFontSize = MIN_FONT_SIZE_DP.dpToPx()
    private val minHeight = MIN_HEIGHT_DP.dpToPx()
    private val maxHeight = MAX_HEIGHT_DP.dpToPx()
    private val textFontIncrementPxPerHeightPx = (MAX_FONT_SIZE_DP - MIN_FONT_SIZE_DP) / (MAX_HEIGHT_DP - MIN_HEIGHT_DP).toFloat()
    private val iconHeightIncrementPxPerHeightPx = (MAX_ICON_HEIGHT_DP - MIN_ICON_HEIGHT_DP) / (MAX_HEIGHT_DP - MIN_HEIGHT_DP).toFloat()
    private val minIconHeight = MIN_ICON_HEIGHT_DP.dpToPx()
    private val horizontalPadding = HORIZONTAL_PADDING_DP.dpToPx()
    private val iconTextOffset = ICON_TEXT_OFFSET_DP.dpToPx()

    private val icon = ContextCompat.getDrawable(context, R.drawable.tinkoff_id_tinkoff_logo)
    private val text = context.getString(R.string.tinkoff_id_sign_in)

    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = minFontSize
        textAlign = Paint.Align.LEFT
        color = ContextCompat.getColor(context, R.color.tinkoff_id_n1)
        typeface = ResourcesCompat.getFont(context, R.font.tinkoff_id_font_roboto_regular)
    }

    private var isCompact = false

    private var iconHeight = minIconHeight
    private var iconWidth = minIconHeight * ICON_WIDTH_TO_HEIGHT_RATIO

    private var staticLayout: StaticLayout? = null

    init {
        context.obtainStyledAttributes(attrs, R.styleable.TinkoffIdSignInButton, defStyle, 0)
            .apply {
                isCompact = getInt(R.styleable.TinkoffIdSignInButton_tinkoff_id_size, SIZE_STANDARD) == SIZE_COMPACT
                recycle()
            }
        background = ContextCompat.getDrawable(context, R.drawable.tinkoff_id_sign_in_button_background)
    }

    // this is done here intentionally to ignore setting of a text to a custom value
    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText("", type)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec).toFloat()
        val heightSize = MeasureSpec.getSize(heightMeasureSpec).toFloat()

        val height = when {
            isCompact -> minHeight
            heightSize > maxHeight -> maxHeight
            heightSize < minHeight -> minHeight
            else -> heightSize
        }

        layoutTextAndIcon()

        var width = 2 * horizontalPadding + iconWidth + iconTextOffset + (staticLayout?.width ?: 0)
        if (!isCompact && width < widthSize) {
            width = widthSize
        }

        setMeasuredDimension(width.roundToInt(), height.toInt())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        staticLayout?.let { staticLayout ->
            canvas.save()
            canvas.translate(
                (width + iconWidth + iconTextOffset - staticLayout.width) / 2f,
                (height - staticLayout.height) / 2f
            )
            staticLayout.draw(canvas)
            canvas.restore()
        }

        icon?.draw(canvas)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        layoutTextAndIcon()
    }

    private fun layoutTextAndIcon() {
        val heightDiff = (measuredHeight - minHeight).takeIf { it > 0f } ?: 0f

        textPaint.textSize = minFontSize + textFontIncrementPxPerHeightPx * heightDiff
        val textWidth = textPaint.measureText(text)
        val textAlignment = Layout.Alignment.ALIGN_NORMAL
        val sl = createStaticLayout(textWidth.roundToInt(), textAlignment)

        staticLayout = sl

        iconHeight = minIconHeight + iconHeightIncrementPxPerHeightPx * heightDiff
        iconWidth = iconHeight * ICON_WIDTH_TO_HEIGHT_RATIO

        val left = (width - iconWidth - iconTextOffset - sl.width) / 2f
        icon?.setBounds(
            left.roundToInt(),
            ((maxOf(measuredHeight.toFloat(), minHeight) - iconHeight) / 2).roundToInt(),
            (left + iconWidth).roundToInt(),
            ((maxOf(measuredHeight.toFloat(), minHeight) + iconHeight) / 2).roundToInt()
        )
    }

    private fun createStaticLayout(textWidth: Int, alignment: Layout.Alignment): StaticLayout {
        val spacingMultiplier = 1f
        val spacingAddition = 0f

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(
                text,
                0,
                text.length,
                textPaint,
                textWidth
            )
                .setAlignment(alignment)
                .setLineSpacing(spacingAddition, spacingMultiplier)
                .setIncludePad(false)
                .build()
        } else {
            StaticLayout(
                text,
                textPaint,
                textWidth,
                alignment,
                spacingMultiplier,
                spacingAddition,
                false
            )
        }
    }

    private fun Int.dpToPx() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        resources.displayMetrics
    )

    private companion object {
        private const val SIZE_STANDARD = 0
        private const val SIZE_COMPACT = 1

        private const val MIN_FONT_SIZE_DP = 14
        private const val MAX_FONT_SIZE_DP = 16
        private const val MIN_HEIGHT_DP = 40
        private const val MAX_HEIGHT_DP = 56
        private const val VERTICAL_PADDING_DP = 6
        private const val HORIZONTAL_PADDING_DP = 32
        private const val ICON_TEXT_OFFSET_DP = 8
        private const val MIN_ICON_HEIGHT_DP = MIN_HEIGHT_DP - 2 * VERTICAL_PADDING_DP
        private const val MAX_ICON_HEIGHT_DP = 31.5f
        private const val ICON_WIDTH_TO_HEIGHT_RATIO = 32 / 28f
    }
}