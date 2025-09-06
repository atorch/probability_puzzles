package atorch.statspuzzles;

import android.content.Context;
import android.content.Intent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasData;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.allOf;

@RunWith(AndroidJUnit4.class)
public class SolvePuzzleTest {

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void geminiButton_launchesPlayStore() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, SolvePuzzle.class);
        intent.putExtra(SolvePuzzle.LEVEL, 0);
        ActivityScenario.launch(intent);

        // On smaller screens, the button might be off-screen, so we scroll to it first.
        // isDisplayed() is still needed to ensure we click the button in the currently visible
        // fragment, as multiple fragments can exist in a ViewPager2.
        Espresso.onView(allOf(withId(R.id.button_gemini_hint), isDisplayed())).perform(scrollTo(), click());

        // This tests the logic in SolvePuzzle's showGeminiHint, which tries to open the Gemini app
        // directly if it's installed, and otherwise falls back to the Play Store.
        // The test framework doesn't have Gemini installed, so it should always go to the Play Store.
        intended(allOf(
                hasAction(Intent.ACTION_VIEW),
                hasData("market://details?id=com.google.android.apps.bard")
        ));
    }
}
