package ru.tinkoff.core.tinkoffId

import android.content.Context
import android.os.Build
import android.text.SpannedString
import androidx.core.content.res.ResourcesCompat
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import ru.tinkoff.core.tinkoffId.ui.TinkoffDimen
import ru.tinkoff.core.tinkoffId.ui.TinkoffIdSignInButton

/**
 * @author k.voskrebentsev
 */
@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
internal class TinkoffIdSignInButtonTest {

    private lateinit var button: TinkoffIdSignInButton

    @Before
    fun setUp() {
        button = TinkoffIdSignInButton(context = context)
    }

    @Test
    fun testSetEmptyTitle() {
        button.title = ""

        val result = button.title as SpannedString

        assertEquals(PERMANENT_TITLE_PART, result.toString())
    }

    @Test
    fun testSetTitle() {
        button.title = CUSTOM_TITLE_PART

        val result = button.title as SpannedString

        assertEquals("$CUSTOM_TITLE_PART $PERMANENT_TITLE_PART", result.toString())
    }

    @Test
    fun testDefaultBadgeText() {
        assertEquals("", button.badgeText)
    }

    @Test
    fun testDefaultCompact() {
        assertEquals(false, button.isCompact)
    }

    @Test
    fun testDefaultStyle() {
        assertEquals(TinkoffIdSignInButton.ButtonStyle.YELLOW, button.style)
    }

    @Test
    fun testDefaultCornerSize() {
        assertEquals(context.resources.getDimension(TinkoffDimen.tinkoff_id_default_corner_radius).toInt(), button.cornerRadius)
    }

    @Test
    fun testDefaultFont() {
        assertEquals(ResourcesCompat.getFont(context, R.font.neue_haas_unica_w1g), button.textFont)
    }

    companion object {
        val context: Context = ApplicationProvider.getApplicationContext()

        const val CUSTOM_TITLE_PART = "title"
        val PERMANENT_TITLE_PART = context.getString(R.string.tinkoff_id_tinkoff_text)
    }
}
