package hw.first.calcucator

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class MyTestIT {

    @Rule
    @JvmField
    var rule: ActivityScenarioRule<*> = ActivityScenarioRule(MainActivity::class.java)
    lateinit var scenario: ActivityScenario<out Activity>

    @Before
    fun scenario() {
        scenario = rule.scenario
    }

    @Test
    fun buttons(){
        onView(withId(R.id.button_1)).perform(click())
        onView(withId(R.id.button_2)).perform(click())
        onView(withId(R.id.button_3)).perform(click())
        onView(withId(R.id.button_4)).perform(click())
        onView(withId(R.id.button_5)).perform(click())
        onView(withId(R.id.button_6)).perform(click())
        onView(withId(R.id.button_7)).perform(click())
        onView(withId(R.id.button_8)).perform(click())
        onView(withId(R.id.button_9)).perform(click())
        onView(withId(R.id.button_0)).perform(click())
        onView(withId(R.id.equal)).perform(click())
        onView(withId(R.id.disp)).check(matches(withText("1234567890")))
    }

    @Test
    fun evaluate1(){
        onView(withId(R.id.button_4)).perform(click())
        onView(withId(R.id.plus)).perform(click())
        onView(withId(R.id.button_2)).perform(click())
        onView(withId(R.id.equal)).perform(click())
        onView(withId(R.id.disp)).check(matches(withText("6")))
    }

    @Test
    fun evaluate2(){
        onView(withId(R.id.button_3)).perform(click())
        onView(withId(R.id.minus)).perform(click())
        onView(withId(R.id.button_2)).perform(click())
        onView(withId(R.id.equal)).perform(click())
        onView(withId(R.id.disp)).check(matches(withText("1")))
    }

    @Test
    fun evaluate3(){
        onView(withId(R.id.button_5)).perform(click())
        onView(withId(R.id.mult)).perform(click())
        onView(withId(R.id.button_6)).perform(click())
        onView(withId(R.id.equal)).perform(click())
        onView(withId(R.id.disp)).check(matches(withText("30")))
    }

    @Test
    fun evaluate4(){
        onView(withId(R.id.button_9)).perform(click())
        onView(withId(R.id.div)).perform(click())
        onView(withId(R.id.button_8)).perform(click())
        onView(withId(R.id.equal)).perform(click())
        onView(withId(R.id.disp)).check(matches(withText("1.125")))
    }

    @Test
    fun flip(){
        val device = UiDevice.getInstance(getInstrumentation())
        onView(withId(R.id.button_9)).perform(click())
        device.setOrientationLeft()
        onView(withId(R.id.disp)).check(matches(withText("9")))
        onView(withId(R.id.div)).perform(click())
        onView(withId(R.id.button_8)).perform(click())
        device.setOrientationNatural()
        onView(withId(R.id.disp)).check(matches(withText("9/8")))
        onView(withId(R.id.equal)).perform(click())
        onView(withId(R.id.disp)).check(matches(withText("1.125")))
    }

    @Test
    fun copy(){
        lateinit var context: Context
        onView(withId(R.id.button_7)).perform(click())
        onView(withId(R.id.plus)).perform(click())
        onView(withId(R.id.button_5)).perform(click())
        onView(withId(R.id.mult)).perform(click())
        onView(withId(R.id.button_1)).perform(click())
        onView(withId(R.id.copy)).perform(click())
        lateinit var clipboard: ClipboardManager
        scenario.onActivity {
            activity -> context = activity.applicationContext
            clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        }
        var pasteData = ""
        if (clipboard.hasPrimaryClip()) {
            val item: ClipData.Item = clipboard.primaryClip!!.getItemAt(0)
            pasteData = item.text.toString()
        }
        onView(withId(R.id.disp)).check(matches(withText("7+5*1")))
        onView(withId(R.id.disp)).check(matches(withText(pasteData)))
    }

    @Test
    fun del(){
        onView(withId(R.id.button_9)).perform(click())
        onView(withId(R.id.div)).perform(click())
        onView(withId(R.id.button_8)).perform(click())
        onView(withId(R.id.del)).perform(click())
        onView(withId(R.id.disp)).check(matches(withText("")))
    }

    @Test
    fun custom(){
        val device = UiDevice.getInstance(getInstrumentation())
        onView(withId(R.id.button_6)).perform(click())
        onView(withId(R.id.del)).perform(click())
        onView(withId(R.id.button_9)).perform(click())
        onView(withId(R.id.plus)).perform(click())
        onView(withId(R.id.button_8)).perform(click())
        onView(withId(R.id.button_9)).perform(click())
        onView(withId(R.id.minus)).perform(click())
        device.setOrientationLeft()
        onView(withId(R.id.button_8)).perform(click())
        onView(withId(R.id.div)).perform(click())
        onView(withId(R.id.button_9)).perform(click())
        device.setOrientationNatural()
        onView(withId(R.id.mult)).perform(click())
        onView(withId(R.id.button_1)).perform(click())
        onView(withId(R.id.button_5)).perform(click())
        onView(withId(R.id.equal)).perform(click())

        onView(withId(R.id.disp)).check(matches(withText("150")))
    }
}