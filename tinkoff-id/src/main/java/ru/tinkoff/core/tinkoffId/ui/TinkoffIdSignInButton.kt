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
import android.graphics.Paint
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import ru.tinkoff.core.tinkoffId.R
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * A button with an icon, text, cashback and other customization options that can be used to partner authorization.
 *
 * ## Requirements
 * Text should represent a single line string.
 * This button should only be used with the wrap_content option for its layout_height attribute and
 * the wrap_content / match_parent / match_constraint option for the layout_width attribute.
 *
 * ## Usage
 * ```xml
 * <ru.tinkoff.core.tinkoffId.ui.TinkoffIdSignInButton
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:tinkoff_id_size="standard"
 *     app:tinkoff_id_style="gray"
 *     app:tinkoff_id_text="Sing in with Tinkoff"
 *     app:tinkoff_id_cashback="15" />
 * ```
 *
 * ## View attributes:
 * - `tinkoff_id_size` - way to customize the button size, one of the options - "standard" (default) / "compact"
 * - `tinkoff_id_text` - text to display on the button. Used only if `tinkoff_id_size` attribute is standard.
 * - `tinkoff_id_cashback` - value of cashback in the template R.string.tinkoff_id_cashback. Used only if `tinkoff_id_size` attribute is standard.
 * - `tinkoff_id_style` - special button style, one of the options - "yellow" (default) / "gray" / "black"
 *
 * ## Behavior description
 * ### "compact" option
 * Width and height of the button are 56dp, the button becomes round,
 * inside the Tinkoff logo bounded by a vertical paddings of 12dp and logo width to height ratio.
 *
 * ### "standard" option
 * Height could be anything between 40dp and 56dp, depending on the constraints
 * passed from the parent view.
 *
 * As for the width, if cashback isn't specified, then it depends on the
 * constraints passed from the parent view, but not less than enough to fit the content. In case
 * width of a button is more than enough to fit its content, it (the content) is centered horizontally.
 *
 * If the cashback is specified, then the width takes up exactly
 * as much as is necessary to display the content.
 *
 * Size of the icon and size of text fonts increase following the height increase.
 * The paddings between and inside the elements are fixed.
 *
 * @author k.voskrebentsev
 */
public class TinkoffIdSignInButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyle, defStyleRes)  {

    public var text: CharSequence
        get() = textView.text
        set(value) {
            textView.text = value
            updateChildrenVisibility()
        }
    public var cashback: CharSequence
        get() = cashbackView.text
        set(value) {
            cashbackView.text = if (value.isNotBlank()) context.getString(R.string.tinkoff_id_cashback, value) else ""
            updateChildrenVisibility()
        }
    public var isCompact: Boolean = false
        set(value) {
            field = value
            updateChildrenVisibility()
            updateStyle()
        }
    private var style: ButtonStyle = ButtonStyle.YELLOW

    private val cashbackAvailable: Boolean
        get() = cashback.isNotEmpty()

    private var textView: TextView = AppCompatTextView(context).apply {
        gravity = Gravity.CENTER
        typeface = ResourcesCompat.getFont(context, R.font.tinkoff_id_font_roboto_regular)
    }

    private var smallIconImageView: ImageView = AppCompatImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_INSIDE
        setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.tinkoff_id_tinkoff_small_logo))
    }

    private var cashbackView: TextView = AppCompatTextView(context).apply {
        gravity = Gravity.CENTER
        typeface = ResourcesCompat.getFont(context, R.font.tinkoff_id_font_roboto_regular)
    }

    private var iconImageView: ImageView = AppCompatImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_INSIDE
        setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.tinkoff_id_tinkoff_logo))
    }

    private val textPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.LEFT
        typeface = ResourcesCompat.getFont(context, R.font.tinkoff_id_font_roboto_regular)
    }

    private val minHeight = MIN_HEIGHT_DP.dpToPx()
    private val maxHeight = MAX_HEIGHT_DP.dpToPx()

    private val horizontalPadding = HORIZONTAL_PADDING_DP.dpToPx()
    private val minFontSize = MIN_FONT_SIZE_DP.dpToPx()
    private val textFontIncrementPxPerHeightPx = (MAX_FONT_SIZE_DP - MIN_FONT_SIZE_DP) / (MAX_HEIGHT_DP - MIN_HEIGHT_DP).toFloat()

    private val smallIconHeightIncrementPxPerHeightPx = (MAX_SMALL_ICON_HEIGHT_DP - MIN_SMALL_ICON_HEIGHT_DP) / (MAX_HEIGHT_DP - MIN_HEIGHT_DP).toFloat()
    private val minSmallIconHeight = MIN_SMALL_ICON_HEIGHT_DP.dpToPx()
    private val iconTextOffset = ICON_TEXT_OFFSET_DP.dpToPx()
    private val smallIconBorder = SMALL_ICON_BORDER_DP.dpToPx()
    private var smallIconHeight = minSmallIconHeight + 2 * smallIconBorder
    private var smallIconWidth = smallIconHeight * SMALL_ICON_WIDTH_TO_HEIGHT_RATIO

    private val minCashbackFontSize = MIN_CASHBACK_FONT_SIZE_DP.dpToPx()
    private val cashbackFontIncrementPxPerHeightPx = (MAX_CASHBACK_FONT_SIZE_DP - MIN_CASHBACK_FONT_SIZE_DP) / (MAX_HEIGHT_DP - MIN_HEIGHT_DP).toFloat()
    private val horizontalPaddingCashback = HORIZONTAL_PADDING_CASHBACK_DP.dpToPx()
    private val iconCashbackOffset = ICON_CASHBACK_OFFSET_DP.dpToPx()
    private val horizontalPaddingInCashback = context.resources.getDimension(R.dimen.tinkoff_id_horizontal_cashback_padding)
    private val verticalPaddingInCashback = context.resources.getDimension(R.dimen.tinkoff_id_vertical_cashback_padding)

    private var iconHeight = ICON_HEIGHT_DP.dpToPx()
    private var iconWidth = ICON_WIDTH_DP.roundToInt().dpToPx()

    init {
        addView(textView)
        addView(smallIconImageView)
        addView(cashbackView)
        addView(iconImageView)

        context.obtainStyledAttributes(attrs, R.styleable.TinkoffIdSignInButton, defStyle, 0)
            .apply {
                isCompact = getInt(R.styleable.TinkoffIdSignInButton_tinkoff_id_size, ButtonSize.STANDARD.ordinal) == ButtonSize.COMPACT.ordinal
                style = getInt(R.styleable.TinkoffIdSignInButton_tinkoff_id_style, ButtonStyle.YELLOW.ordinal).let(ButtonStyle.values()::get)
                if (!isCompact) {
                    getString(R.styleable.TinkoffIdSignInButton_tinkoff_id_cashback)?.let { cashback = it }
                    getString(R.styleable.TinkoffIdSignInButton_tinkoff_id_text).let {
                        text = checkNotNull(it) {
                            "Text should be passed in the tinkoff_id_text attribute at the standard size of the button"
                        }
                    }
                }
                recycle()
            }

        updateStyle()
    }

    @Suppress("ComplexMethod")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightDiff = (measuredHeight - minHeight).takeIf { it > 0f } ?: 0f
        val specWidth = MeasureSpec.getSize(widthMeasureSpec).toFloat()
        val specHeight = MeasureSpec.getSize(heightMeasureSpec).toFloat()

        fun defineTextSizes() {
            val textSize = minFontSize + textFontIncrementPxPerHeightPx * heightDiff
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
            textPaint.textSize = textSize
            val layout = createStaticLayout(textView.text.toString(), textPaint)
            textView.measure(layout.width, layout.height)
        }

        fun defineSmallIconSizes() {
            smallIconHeight = minSmallIconHeight + smallIconHeightIncrementPxPerHeightPx * heightDiff + if (style == ButtonStyle.BLACK) 2 * smallIconBorder else 0f
            smallIconWidth = smallIconHeight * SMALL_ICON_WIDTH_TO_HEIGHT_RATIO
        }

        fun defineCashbackSizes() {
            val textSize = minCashbackFontSize + cashbackFontIncrementPxPerHeightPx * heightDiff
            cashbackView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
            textPaint.textSize = textSize
            val layout = createStaticLayout(cashbackView.text.toString(), textPaint)
            cashbackView.measure(
                (layout.width + 2 * horizontalPaddingInCashback).roundToInt(),
                (layout.height + 2 * verticalPaddingInCashback).roundToInt()
            )
        }

        val totalHeight = when {
            isCompact -> maxHeight
            specHeight > maxHeight -> maxHeight
            specHeight < minHeight -> minHeight
            else -> specHeight
        }

        when {
            isCompact -> Unit
            cashbackAvailable -> {
                defineTextSizes()
                defineSmallIconSizes()
                defineCashbackSizes()
            }
            else -> {
                defineTextSizes()
                defineSmallIconSizes()
            }
        }

        val contentWidth = when {
            isCompact -> totalHeight
            cashbackAvailable -> 2 * horizontalPaddingCashback + textView.measuredWidth + smallIconWidth +
                + iconTextOffset + iconCashbackOffset + cashbackView.measuredWidth
            else -> 2 * horizontalPadding + textView.measuredWidth + smallIconWidth + iconTextOffset
        }

        val totalWidth = if (isCompact || cashbackAvailable) contentWidth else max(specWidth, contentWidth)

        setMeasuredDimension(totalWidth.roundToInt(), totalHeight.roundToInt())
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val parentWidth = r - l
        val parentHeight = b - t

        fun View.layout(l: Float, t: Float, r: Float, b: Float) {
            this.layout(l.roundToInt(), t.roundToInt(), r.roundToInt(), b.roundToInt())
        }

        when {
            isCompact -> {
                iconImageView.layout(
                    (parentWidth - iconWidth) / 2f,
                    (parentHeight - iconHeight) / 2f,
                    (parentWidth + iconWidth) / 2f,
                    (parentHeight + iconHeight) / 2f
                )
            }
            cashbackAvailable -> {
                var currentLeft = (parentWidth - textView.measuredWidth - iconTextOffset - smallIconWidth - iconCashbackOffset - cashbackView.measuredWidth) / 2
                textView.layout(
                    currentLeft,
                    (height - textView.measuredHeight) / 2f,
                    currentLeft + textView.measuredWidth,
                    (height + textView.measuredHeight) / 2f,
                )
                currentLeft += textView.measuredWidth + iconTextOffset
                smallIconImageView.layout(
                    currentLeft,
                    (parentHeight - smallIconHeight) / 2f,
                    currentLeft + smallIconWidth,
                    (parentHeight + smallIconHeight) / 2f
                )
                currentLeft += smallIconWidth + iconCashbackOffset
                cashbackView.layout(
                    currentLeft,
                    (parentHeight - cashbackView.measuredHeight - verticalPaddingInCashback) / 2f,
                    currentLeft + cashbackView.measuredWidth,
                    (parentHeight + cashbackView.measuredHeight + verticalPaddingInCashback) / 2f
                )
            }
            else -> {
                var currentLeft = (parentWidth - smallIconWidth - iconTextOffset - textView.measuredWidth) / 2f
                textView.layout(
                    currentLeft,
                (height - textView.measuredHeight) / 2f,
                    currentLeft + textView.measuredWidth,
                    (height + textView.measuredHeight) / 2f,
                )
                currentLeft += textView.measuredWidth + iconTextOffset
                smallIconImageView.layout(
                    currentLeft,
                    (parentHeight - smallIconHeight) / 2f,
                    currentLeft + smallIconWidth,
                    (parentHeight + smallIconHeight) / 2f
                )
            }
        }
    }

    private fun updateStyle() {
        background = if (isCompact) {
            AppCompatResources.getDrawable(context, style.backgroundCompactRes)
        } else {
            AppCompatResources.getDrawable(context, style.backgroundRes)
        }
        textView.setTextColor(AppCompatResources.getColorStateList(context, style.textColorRes))
        cashbackView.background = AppCompatResources.getDrawable(context, style.backgroundCashBackRes)
        cashbackView.setTextColor(AppCompatResources.getColorStateList(context, style.textColorRes))
        smallIconImageView.setImageDrawable(AppCompatResources.getDrawable(context, style.smallIconImageRes))
    }

    private fun updateChildrenVisibility() {
        textView.visibility = when {
            !isCompact -> VISIBLE
            else -> GONE
        }

        smallIconImageView.visibility = when {
            !isCompact -> VISIBLE
            else -> GONE
        }

        cashbackView.visibility = when {
            !isCompact && cashbackAvailable -> VISIBLE
            else -> GONE
        }

        iconImageView.visibility = when {
            isCompact -> VISIBLE
            else -> GONE
        }
    }

    private fun createStaticLayout(
        text: String,
        textPaint: TextPaint,
        textWidth: Int = textPaint.measureText(text).roundToInt(),
        alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL
    ): StaticLayout {
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

    public enum class ButtonSize {
        STANDARD,
        COMPACT
    }

    public enum class ButtonStyle(
        @DrawableRes internal val backgroundRes: Int,
        @DrawableRes internal val backgroundCompactRes: Int,
        @DrawableRes internal val backgroundCashBackRes: Int,
        @DrawableRes internal val smallIconImageRes: Int,
        @ColorRes internal val textColorRes: Int
    ) {
        YELLOW(
            backgroundRes = R.drawable.tinkoff_id_sign_in_button_background_yellow_style,
            backgroundCompactRes = R.drawable.tinkoff_id_sign_in_compact_button_background_yellow_style,
            backgroundCashBackRes = R.drawable.tinkoff_id_cashback_background_yellow_style,
            smallIconImageRes = R.drawable.tinkoff_id_tinkoff_small_logo,
            textColorRes = R.color.tinkoff_id_text_yellow_style
        ),
        GRAY(
            backgroundRes = R.drawable.tinkoff_id_sign_in_button_background_gray_style,
            backgroundCompactRes = R.drawable.tinkoff_id_sign_in_compact_button_background_gray_style,
            backgroundCashBackRes = R.drawable.tinkoff_id_cashback_background_gray_style,
            smallIconImageRes = R.drawable.tinkoff_id_tinkoff_small_logo,
            textColorRes = R.color.tinkoff_id_text_gray_style
        ),
        BLACK(
            backgroundRes = R.drawable.tinkoff_id_sign_in_button_background_black_style,
            backgroundCompactRes = R.drawable.tinkoff_id_sign_in_compact_button_background_black_style,
            backgroundCashBackRes = R.drawable.tinkoff_id_cashback_background_black_style,
            smallIconImageRes = R.drawable.tinkoff_id_tinkoff_small_logo_border,
            textColorRes = R.color.tinkoff_id_text_black_style
        )
    }

    private fun Int.dpToPx() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        resources.displayMetrics
    )

    private companion object {
        private const val MIN_FONT_SIZE_DP = 14
        private const val MAX_FONT_SIZE_DP = 16
        private const val MIN_HEIGHT_DP = 40
        private const val MAX_HEIGHT_DP = 56
        private const val ICON_TEXT_OFFSET_DP = 8
        private const val VERTICAL_PADDING_DP = 12

        // no cashback style parameters
        private const val HORIZONTAL_PADDING_DP = 32
        // cashback style parameters
        private const val HORIZONTAL_PADDING_CASHBACK_DP = 16
        private const val ICON_CASHBACK_OFFSET_DP = 32
        private const val MIN_CASHBACK_FONT_SIZE_DP = 10
        private const val MAX_CASHBACK_FONT_SIZE_DP = 12
        // icon parameters
        private const val ICON_HEIGHT_DP = MAX_HEIGHT_DP - 2 * VERTICAL_PADDING_DP
        private const val ICON_WIDTH_DP = ICON_HEIGHT_DP * 32 / 28f
        // small icon parameters
        private const val SMALL_ICON_VERTICAL_PADDING_DP = 15
        private const val MIN_SMALL_ICON_HEIGHT_DP = MIN_HEIGHT_DP - 2 * SMALL_ICON_VERTICAL_PADDING_DP
        private const val MAX_SMALL_ICON_HEIGHT_DP = 18
        private const val SMALL_ICON_WIDTH_TO_HEIGHT_RATIO = 2f
        private const val SMALL_ICON_BORDER_DP = 2
    }
}
