package ru.tinkoff.core.app_demo_partner

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnitRunner
import org.hamcrest.CoreMatchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.tinkoff.core.tinkoffId.ui.TinkoffIdSignInButton

/**
 * @author k.voskrebentsev
 */
@RunWith(AndroidJUnit4ClassRunner::class)
class TinkoffIdSignInButtonTest : AndroidJUnitRunner() {

    @get:Rule
    val activityRule = ActivityTestRule(PartnerActivity::class.java, false, true)

    private lateinit var standardButton: TinkoffIdSignInButton

    @Before
    fun setUp() {
        runOnMainThread {
            standardButton =  activityRule.activity.findViewById(standardButtonId)
        }
    }

    @Test
    fun testTitleChanging() {
        runOnMainThread {
            standardButton.title = SOME_TEXT
        }

        checkTextOnView(createFinalTitle(SOME_TEXT))
    }

    @Test
    fun testBadgeTextChanging() {
        runOnMainThread {
            standardButton.badgeText = SOME_TEXT
        }

        checkTextOnView(SOME_TEXT)
    }

    @Test
    fun testTextElementsHidingInCompactMode() {
        runOnMainThread {
            standardButton.title = SOME_TEXT
            standardButton.badgeText = SOME_TEXT
            standardButton.isCompact = true
        }

        onView(
            allOf(
                withId(standardButtonId),
                // title element
                withChild(
                    allOf(
                        withText(createFinalTitle(SOME_TEXT)),
                        withEffectiveVisibility(ViewMatchers.Visibility.GONE)
                    )
                ),
                // badge element
                withChild(
                    allOf(
                        withText(SOME_TEXT),
                        withEffectiveVisibility(ViewMatchers.Visibility.GONE)
                    )
                ),
            )
        ).check(matches(isDisplayed()))
    }

    private fun checkTextOnView(text: String) {
        onView(
            allOf(
                withId(standardButtonId),
                withChild(withText(text))
            )
        ).check(matches(isDisplayed()))
    }

    private fun createFinalTitle(titlePart: String) = "$titlePart $permanentTitlePart"
    private fun runOnMainThread(block: () -> Unit) {
        activityRule.runOnUiThread(block)
    }

    private companion object {
        private const val SOME_TEXT = "test"

        private const val standardButtonId = R.id.standardSmallBlackButtonTinkoffAuth

        private val permanentTitlePart = getInstrumentation().targetContext.resources.getString(ru.tinkoff.core.tinkoffId.R.string.tinkoff_id_tinkoff_text)
    }
}
