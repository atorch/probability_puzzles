package atorch.statspuzzles;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import static androidx.test.espresso.action.ViewActions.click;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class PuzzleSelectionTest {

    @Rule
    public ActivityScenarioRule<PuzzleSelection> activityRule =
            new ActivityScenarioRule<>(PuzzleSelection.class);

    @Test
    public void mainActivityLoads() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Espresso.onView(ViewMatchers.withText(context.getString(R.string.app_name)))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void easyPuzzlesButton_loadsEasyPuzzles() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Espresso.onView(ViewMatchers.withText(context.getString(R.string.button_level_0))).perform(click());
        Espresso.onView(ViewMatchers.withText(context.getString(R.string.puzzle, 1)))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}
