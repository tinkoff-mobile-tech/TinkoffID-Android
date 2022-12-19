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
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.text.SpannableString
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.util.TypedValue.COMPLEX_UNIT_PX
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import androidx.annotation.Px
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import ru.tinkoff.core.tinkoffId.R
import kotlin.math.roundToInt

internal typealias TinkoffDrawable = R.drawable
internal typealias TinkoffColor = R.color
internal typealias TinkoffDimen = R.dimen

/**
 * A button with an icon, title text, badge text, and other customization options that can be used for partner authorization.
 *
 * ## Usage
 * ```xml
 * <ru.tinkoff.core.tinkoffId.ui.TinkoffIdSignInButton
 *     android:id="@+id/standardButtonTinkoffAuth"
 *     android:layout_width="wrap_content"
 *     android:layout_height="60dp"
 *     app:tinkoff_id_compact="false"
 *     app:tinkoff_id_title="Sign in with"
 *     app:tinkoff_id_badge="Cashback up to 5%"
 *     app:tinkoff_id_style="yellow"
 *     app:tinkoff_id_corner_radius="8dp"
 *     app:tinkoff_id_font="@font/neue_haas_unica_w1g"/>
 * ```
 *
 * ## View attributes:
 * - `tinkoff_id_compact` - way to customize the button size, one of the options - false (default) / true
 * - `tinkoff_id_style` - special button style, one of the options - "yellow" (default) / "gray" / "black"
 * - `tinkoff_id_title` - text preceding "Tinkoff" on the button. Used only if `tinkoff_id_compact` attribute is false.
 * - `tinkoff_id_badge` - additional text to attract attention. Used only if `tinkoff_id_compact` attribute is false.
 * - `tinkoff_id_corner_radius` - radius for button corners. Used only if `tinkoff_id_compact` attribute is false.
 * - `tinkoff_id_font` - font of the text on the button. Used only if `tinkoff_id_compact` attribute is false.
 *
 * @author Kirill Voskrebentsev
 */
public class TinkoffIdSignInButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyle, defStyleRes)  {

    // Customization elements
    public var title: CharSequence?
        get() = titleView.text
        set(value) {
            titleView.text = if (value.isNullOrEmpty()) {
                SpannableString(permanentTitlePart).apply {
                    setSpan(spannableStyleBold, 0, permanentTitlePart.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            } else {
                val finalValue = "$value $permanentTitlePart"
                SpannableString(finalValue).apply {
                    setSpan(spannableStyleBold, value.length + 1, finalValue.length, SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
            updateChildrenVisibility()
        }

    public var badgeText: CharSequence?
        get() = badgeView.text
        set(value) {
            badgeView.text = value
            updateChildrenVisibility()
        }

    public var isCompact: Boolean = false
        set(value) {
            field = value
            updateChildrenVisibility()
            updateStyle()
        }

    public var style: ButtonStyle = ButtonStyle.YELLOW
        set(value) {
            field = value
            updateStyle()
        }

    @Px
    public var cornerRadius: Int = getDimension(TinkoffDimen.tinkoff_id_default_corner_radius)
        set(value) {
            field = value
            updateStyle()
        }

    public var textFont: Typeface? = getFont(R.font.neue_haas_unica_w1g)
        set(value) {
            field = value
            titleView.typeface = value
            badgeView.typeface = value
        }

    // Auxiliary elements
    private val isBadgeState: Boolean
        get() = !badgeText.isNullOrEmpty()
    private var size: ButtonSize = ButtonSize.LARGE
        set(value) {
            field = value
            updateSize()
        }
    private val permanentTitlePart = context.getString(R.string.tinkoff_id_tinkoff_text)
    private val spannableStyleBold = StyleSpan(Typeface.BOLD)

    // Internal views
    private var titleView: TextView = AppCompatTextView(context).apply {
        gravity = Gravity.CENTER
    }
    private var smallLogoImageView: ImageView = AppCompatImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_INSIDE
    }
    private var badgeView: TextView = AppCompatTextView(context).apply {
        gravity = Gravity.CENTER
    }
    private var logoImageView: ImageView = AppCompatImageView(context).apply {
        scaleType = ImageView.ScaleType.FIT_CENTER
        setImageDrawable(getDrawable(R.drawable.tinkoff_id_tinkoff_logo))
    }

    // Sizes and paddings of views
    private val minHeight = getDimension(ButtonSize.SMALL.minHeight)
    private val compactVerticalPadding = getDimension(TinkoffDimen.tinkoff_id_compact_vertical_padding)
    private val compactHorizontalPadding = getDimension(TinkoffDimen.tinkoff_id_compact_horizontal_padding)
    private val smallLogoBorder = getDimension(TinkoffDimen.tinkoff_id_small_logo_border)
        get() = if (!isCompact && style == ButtonStyle.BLACK) field else 0
    private var minVerticalPadding = 0
    private var minHorizontalPadding = 0
    private var titleSmallLogoOffset = 0
    private var smallLogoHeight = 0
    private var smallLogoWidth = 0
    private var smallLogoBadgeOffset = 0

    init {
        addView(titleView)
        addView(smallLogoImageView)
        addView(badgeView)
        addView(logoImageView)

        context.obtainStyledAttributes(attrs, R.styleable.TinkoffIdSignInButton, defStyle, defStyleRes)
            .apply {
                isCompact = getBoolean(R.styleable.TinkoffIdSignInButton_tinkoff_id_compact, false)
                style = getInt(R.styleable.TinkoffIdSignInButton_tinkoff_id_style, ButtonStyle.YELLOW.ordinal).let(ButtonStyle.values()::get)
                cornerRadius = getDimension(R.styleable.TinkoffIdSignInButton_tinkoff_id_corner_radius, getDimension(TinkoffDimen.tinkoff_id_default_corner_radius).toFloat()).roundToInt()
                textFont = this@TinkoffIdSignInButton.getFont(getResourceId(R.styleable.TinkoffIdSignInButton_tinkoff_id_font, R.font.neue_haas_unica_w1g))
                if (!isCompact) {
                    badgeText = getString(R.styleable.TinkoffIdSignInButton_tinkoff_id_badge)
                    title = getString(R.styleable.TinkoffIdSignInButton_tinkoff_id_title)
                }
                recycle()
            }

        updateStyle()
    }

    @Suppress("ComplexMethod")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        fun measureTextView(textView: TextView) {
            val wrapContentSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            textView.measure(wrapContentSpec, wrapContentSpec)
        }
        fun measureImageView(imageView: ImageView, width: Int, height: Int) {
            imageView.measure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
            )
        }

        val totalHeight = if (MeasureSpec.getSize(heightMeasureSpec) < minHeight) {
            minHeight
        } else {
            resolveSize(minHeight, heightMeasureSpec)
        }

        size = getButtonSize(totalHeight)

        when {
            isCompact -> Unit
            isBadgeState -> {
                measureTextView(titleView)
                measureImageView(
                    smallLogoImageView,
                    smallLogoWidth + smallLogoBorder,
                    smallLogoWidth + smallLogoBorder
                )
                measureTextView(badgeView)
            }
            else -> {
                measureTextView(titleView)
                measureImageView(
                    smallLogoImageView,
                    smallLogoWidth + smallLogoBorder,
                    smallLogoWidth + smallLogoBorder
                )
            }
        }

        val contentWidth = when {
            isCompact -> totalHeight
            isBadgeState -> minHorizontalPadding + titleView.measuredWidth + titleSmallLogoOffset + smallLogoImageView.measuredWidth +
                    + smallLogoBadgeOffset + badgeView.measuredWidth + minHorizontalPadding
            else -> minHorizontalPadding + titleView.measuredWidth + titleSmallLogoOffset + smallLogoImageView.measuredWidth + minHorizontalPadding
        }

        val totalWidth = when {
            isCompact -> contentWidth
            else -> resolveSize(contentWidth, widthMeasureSpec)
        }

        setMeasuredDimension(totalWidth, totalHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val parentWidth = r - l
        val parentHeight = b - t
        val parentTop = minVerticalPadding
        val parentBottom = parentHeight - minVerticalPadding

        fun layoutChild(child: View, childLeft: Int) {
            val width = child.measuredWidth
            val height = child.measuredHeight
            val childTop = parentTop + (parentBottom - parentTop - height) / 2
            child.layout(childLeft, childTop, childLeft + width, childTop + height)
        }

        when {
            isCompact -> {
                logoImageView.layout(
                    compactHorizontalPadding,
                    compactVerticalPadding,
                    parentWidth - compactHorizontalPadding,
                    parentHeight - compactVerticalPadding
                )
            }
            isBadgeState -> {
                var currentLeft = (parentWidth - titleView.measuredWidth - titleSmallLogoOffset - smallLogoImageView.measuredWidth - smallLogoBadgeOffset - badgeView.measuredWidth) / 2

                layoutChild(titleView, currentLeft)
                currentLeft += titleView.measuredWidth + titleSmallLogoOffset

                layoutChild(smallLogoImageView, currentLeft)
                currentLeft += smallLogoWidth + smallLogoBadgeOffset

                layoutChild(badgeView, currentLeft)
            }
            else -> {
                var currentLeft = (parentWidth - titleView.measuredWidth - titleSmallLogoOffset - smallLogoImageView.measuredWidth) / 2

                layoutChild(titleView, currentLeft)
                currentLeft += titleView.measuredWidth + titleSmallLogoOffset

                layoutChild(smallLogoImageView, currentLeft)
            }
        }
    }

    private fun updateStyle() {
        background = if (isCompact) {
            getDrawable(style.backgroundCompactRes)
        } else {
            val pressedState = getColor(style.buttonPressedStateColorRes)
            val contentDrawable = GradientDrawable().apply {
                cornerRadius = this@TinkoffIdSignInButton.cornerRadius.toFloat()
                color = getColor(style.buttonEnabledStateColorRes)
            }
            RippleDrawable(pressedState, contentDrawable, null)
        }
        titleView.setTextColor(getColor(style.textColorRes))
        badgeView.background = getDrawable(style.backgroundBadgeRes)
        badgeView.setTextColor(getColor(style.textColorRes))
        smallLogoImageView.setImageDrawable(getDrawable(style.smallIconImageRes))
    }

    private fun updateSize() {
        minVerticalPadding = getDimension(size.minVerticalPadding)
        minHorizontalPadding = getDimension(size.minHorizontalPadding)
        titleSmallLogoOffset = getDimension(size.titleSmallLogoOffset)
        smallLogoHeight = getDimension(size.smallLogoHeight)
        smallLogoWidth = getDimension(size.smallLogoWidth)
        smallLogoBadgeOffset = getDimension(size.smallLogoBadgeOffset)

        val badgeVerticalPadding = getDimension(size.badgeVerticalPadding)
        val badgeHorizontalPadding = getDimension(size.badgeHorizontalPadding)
        badgeView.setPadding(badgeHorizontalPadding, badgeVerticalPadding, badgeHorizontalPadding, badgeVerticalPadding)

        titleView.setTextSize(COMPLEX_UNIT_PX, getDimension(size.titleFontSize).toFloat())
        badgeView.setTextSize(COMPLEX_UNIT_PX, getDimension(size.badgeFontSize).toFloat())
    }

    private fun updateChildrenVisibility() {
        titleView.visibility = when {
            !isCompact -> VISIBLE
            else -> GONE
        }

        smallLogoImageView.visibility = when {
            !isCompact -> VISIBLE
            else -> GONE
        }

        badgeView.visibility = when {
            !isCompact && isBadgeState -> VISIBLE
            else -> GONE
        }

        logoImageView.visibility = when {
            isCompact -> VISIBLE
            else -> GONE
        }
    }

    public enum class ButtonSize(
        @DimenRes internal val minHeight: Int,
        @DimenRes internal val minVerticalPadding: Int,
        @DimenRes internal val minHorizontalPadding: Int,
        @DimenRes internal val titleFontSize: Int,
        @DimenRes internal val titleSmallLogoOffset: Int,
        @DimenRes internal val smallLogoHeight: Int,
        @DimenRes internal val smallLogoWidth: Int,
        @DimenRes internal val smallLogoBadgeOffset: Int,
        @DimenRes internal val badgeFontSize: Int,
        @DimenRes internal val badgeVerticalPadding: Int,
        @DimenRes internal val badgeHorizontalPadding: Int
    ) {
        SMALL(
            minHeight = TinkoffDimen.tinkoff_id_small_min_height,
            minVerticalPadding = TinkoffDimen.tinkoff_id_small_vertical_padding,
            minHorizontalPadding = TinkoffDimen.tinkoff_id_small_horizontal_padding,
            titleFontSize = TinkoffDimen.tinkoff_id_title_small_font_size,
            titleSmallLogoOffset = TinkoffDimen.tinkoff_id_title_small_logo_small_offset,
            smallLogoHeight = TinkoffDimen.tinkoff_id_small_logo_small_height,
            smallLogoWidth = TinkoffDimen.tinkoff_id_small_logo_small_width,
            smallLogoBadgeOffset = TinkoffDimen.tinkoff_id_small_logo_badge_small_offset,
            badgeFontSize = TinkoffDimen.tinkoff_id_badge_small_font_size,
            badgeVerticalPadding = TinkoffDimen.tinkoff_id_badge_small_vertical_padding,
            badgeHorizontalPadding = TinkoffDimen.tinkoff_id_badge_small_horizontal_padding
        ),
        MEDIUM(
            minHeight = TinkoffDimen.tinkoff_id_medium_min_height,
            minVerticalPadding = TinkoffDimen.tinkoff_id_medium_vertical_padding,
            minHorizontalPadding = TinkoffDimen.tinkoff_id_medium_horizontal_padding,
            titleFontSize = TinkoffDimen.tinkoff_id_title_medium_font_size,
            titleSmallLogoOffset = TinkoffDimen.tinkoff_id_title_small_logo_medium_offset,
            smallLogoHeight = TinkoffDimen.tinkoff_id_small_logo_medium_height,
            smallLogoWidth = TinkoffDimen.tinkoff_id_small_logo_medium_width,
            smallLogoBadgeOffset = TinkoffDimen.tinkoff_id_small_logo_badge_medium_offset,
            badgeFontSize = TinkoffDimen.tinkoff_id_badge_medium_font_size,
            badgeVerticalPadding = TinkoffDimen.tinkoff_id_badge_medium_vertical_padding,
            badgeHorizontalPadding = TinkoffDimen.tinkoff_id_badge_medium_horizontal_padding
        ),
        LARGE(
            minHeight = TinkoffDimen.tinkoff_id_large_min_height,
            minVerticalPadding = TinkoffDimen.tinkoff_id_large_vertical_padding,
            minHorizontalPadding = TinkoffDimen.tinkoff_id_large_horizontal_padding,
            titleFontSize = TinkoffDimen.tinkoff_id_title_large_font_size,
            titleSmallLogoOffset = TinkoffDimen.tinkoff_id_title_small_logo_large_offset,
            smallLogoHeight = TinkoffDimen.tinkoff_id_small_logo_large_height,
            smallLogoWidth = TinkoffDimen.tinkoff_id_small_logo_large_width,
            smallLogoBadgeOffset = TinkoffDimen.tinkoff_id_small_logo_badge_large_offset,
            badgeFontSize = TinkoffDimen.tinkoff_id_badge_large_font_size,
            badgeVerticalPadding = TinkoffDimen.tinkoff_id_badge_large_vertical_padding,
            badgeHorizontalPadding = TinkoffDimen.tinkoff_id_badge_large_horizontal_padding
        )
    }

    private fun getButtonSize(height: Int): ButtonSize = when {
        height < getDimension(ButtonSize.MEDIUM.minHeight) -> ButtonSize.SMALL
        height < getDimension(ButtonSize.LARGE.minHeight) -> ButtonSize.MEDIUM
        else -> ButtonSize.LARGE
    }

    public enum class ButtonStyle(
        @ColorRes internal val buttonEnabledStateColorRes: Int,
        @ColorRes internal val buttonPressedStateColorRes: Int,
        @DrawableRes internal val backgroundCompactRes: Int,
        @DrawableRes internal val backgroundBadgeRes: Int,
        @DrawableRes internal val smallIconImageRes: Int,
        @ColorRes internal val textColorRes: Int
    ) {
        YELLOW(
            buttonEnabledStateColorRes = TinkoffColor.tinkoff_id_button_yellow_style,
            buttonPressedStateColorRes = TinkoffColor.tinkoff_id_button_pressed_yellow_style,
            backgroundCompactRes = TinkoffDrawable.tinkoff_id_compact_background_yellow_style,
            backgroundBadgeRes = TinkoffDrawable.tinkoff_id_badge_background_yellow_style,
            smallIconImageRes = TinkoffDrawable.tinkoff_id_tinkoff_small_logo,
            textColorRes = TinkoffColor.tinkoff_id_text_yellow_style
        ),
        GRAY(
            buttonEnabledStateColorRes = TinkoffColor.tinkoff_id_button_gray_style,
            buttonPressedStateColorRes = TinkoffColor.tinkoff_id_button_pressed_gray_style,
            backgroundCompactRes = TinkoffDrawable.tinkoff_id_compact_background_gray_style,
            backgroundBadgeRes = TinkoffDrawable.tinkoff_id_badge_background_gray_style,
            smallIconImageRes = TinkoffDrawable.tinkoff_id_tinkoff_small_logo,
            textColorRes = TinkoffColor.tinkoff_id_text_gray_style
        ),
        BLACK(
            buttonEnabledStateColorRes = TinkoffColor.tinkoff_id_button_black_style,
            buttonPressedStateColorRes = TinkoffColor.tinkoff_id_button_pressed_black_style,
            backgroundCompactRes = TinkoffDrawable.tinkoff_id_compact_background_black_style,
            backgroundBadgeRes = TinkoffDrawable.tinkoff_id_badge_background_black_style,
            smallIconImageRes = TinkoffDrawable.tinkoff_id_tinkoff_small_logo_border,
            textColorRes = TinkoffColor.tinkoff_id_text_black_style
        )
    }

    private fun getDimension(@DimenRes id: Int) = context.resources.getDimension(id).roundToInt()
    private fun getDrawable(@DrawableRes id: Int) = AppCompatResources.getDrawable(context, id)
    private fun getColor(@ColorRes id: Int) = AppCompatResources.getColorStateList(context, id)
    private fun getFont(@FontRes id: Int) = ResourcesCompat.getFont(context, id)
}
