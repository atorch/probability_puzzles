package atorch.statspuzzles;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;

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
        Espresso.onView(ViewMatchers.withText("Probability Puzzles"))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}
