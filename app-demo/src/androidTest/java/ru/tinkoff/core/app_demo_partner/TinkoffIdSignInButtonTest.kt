package ru.tinkoff.core.app_demo_partner

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnitRunner
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import org.junit.Assert.assertTrue
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

    lateinit var standardButton: TinkoffIdSignInButton

    @Before
    fun setUp() {
        runOnMainThread {
            standardButton =  activityRule.activity.findViewById(standardButtonId)
        }
    }

    @Test
    fun testStandardButtonTextPresence() {
        checkPresenceTextOnScreen(originalText)
    }

    @Test
    fun testStandardButtonTextUpdate() {
        runOnMainThread {
            standardButton.text = SOME_TEXT
        }

        checkPresenceTextOnScreen(SOME_TEXT)
    }

    @Test
    fun testStandardButtonTextAbsence() {
        runOnMainThread {
            standardButton.text = EMPTY_TEXT
        }

        checkAbsenceTextOnScreen(originalText)
    }

    @Test
    fun testStandardButtonCashbackPresence() {
        checkPresenceTextOnScreen(wrapCashbackInTemplate(originalCashback))
    }

    @Test
    fun testStandardButtonCashbackUpdate() {
        runOnMainThread {
            standardButton.cashback = SOME_TEXT
        }

        checkPresenceTextOnScreen(wrapCashbackInTemplate(SOME_TEXT))
    }

    @Test
    fun testStandardButtonCashbackAbsence() {
        runOnMainThread {
            standardButton.cashback = EMPTY_TEXT
        }

        checkAbsenceTextOnScreen(wrapCashbackInTemplate(originalCashback))
    }

    private fun checkPresenceTextOnScreen(text: String) {
        assertTrue("Not found text \"$text\" on screen", UiDevice.getInstance(getInstrumentation()).hasObject(By.textContains(text)))
    }
    private fun checkAbsenceTextOnScreen(text: String) {
        assertTrue("Found text \"$text\" on screen", !UiDevice.getInstance(getInstrumentation()).hasObject(By.textContains(text)))
    }
    private fun wrapCashbackInTemplate(cashback: String) = getInstrumentation().targetContext.getString(ru.tinkoff.core.tinkoffId.R.string.tinkoff_id_cashback, cashback)
    private fun runOnMainThread(block: () -> Unit) {
        activityRule.runOnUiThread(block)
    }

    private companion object {
        private const val SOME_TEXT = "test"
        private const val EMPTY_TEXT = ""

        private const val standardButtonId = R.id.standardButtonTinkoffAuth

        private val originalText = getInstrumentation().targetContext.resources.getString(R.string.partner_auth_sign_in_button_text)
        private val originalCashback = getInstrumentation().targetContext.resources.getString(R.string.partner_auth_sign_in_button_cashback)
    }
}
