package atorch.statspuzzles;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.ScrollView;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.viewpager2.widget.ViewPager2;

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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

    // Level 1, puzzle index 32 ("Alice would like to send Bob...") is the longest puzzle
    // statement in the app (1530 characters) and has no image, making it the puzzle most likely
    // to have its answer field pushed below the fold once the soft keyboard is showing.
    private static final int LEVEL_WITH_LONG_PUZZLE = 1;
    private static final int LONGEST_PUZZLE_INDEX = 32;

    @Test
    public void answerField_notObscuredByKeyboard_whenPuzzleTextIsLong() throws InterruptedException {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, SolvePuzzle.class);
        intent.putExtra(SolvePuzzle.LEVEL, LEVEL_WITH_LONG_PUZZLE);
        ActivityScenario<SolvePuzzle> scenario = ActivityScenario.launch(intent);

        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();

        scenario.onActivity(activity -> {
            ViewPager2 pager = activity.findViewById(R.id.pager);
            pager.setCurrentItem(LONGEST_PUZZLE_INDEX, false);
        });
        instrumentation.waitForIdleSync();
        Thread.sleep(300);

        // Scroll (without focusing) to bring the field on screen, mimicking a user manually
        // scrolling down to find it before tapping -- as opposed to requestFocus(), which
        // triggers a special "scroll parent to reveal" behavior that real touch-driven focus
        // doesn't get, and which would mask this bug.
        scenario.onActivity(activity -> {
            EditText userAnswer = findCurrentPageEditText(activity);
            ScrollView scrollView = findAncestorScrollView(userAnswer);
            scrollView.scrollTo(0, scrollView.getChildAt(0).getHeight());
        });
        instrumentation.waitForIdleSync();
        Thread.sleep(300);

        // Inject a REAL touch tap at the field's current on-screen location, going through the
        // actual touch dispatch pipeline (requestFocusFromTouch), just like a real user tap --
        // this matters because real touch-driven focus does *not* get the same automatic
        // "scroll parent to reveal" behavior that requestFocus() does.
        int[] tapPoint = new int[2];
        scenario.onActivity(activity -> {
            EditText userAnswer = findCurrentPageEditText(activity);
            int[] loc = new int[2];
            userAnswer.getLocationOnScreen(loc);
            tapPoint[0] = loc[0] + userAnswer.getWidth() / 2;
            tapPoint[1] = loc[1] + userAnswer.getHeight() / 2;
        });
        long downTime = SystemClock.uptimeMillis();
        MotionEvent down = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, tapPoint[0], tapPoint[1], 0);
        instrumentation.sendPointerSync(down);
        down.recycle();
        Thread.sleep(50);
        MotionEvent up = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, tapPoint[0], tapPoint[1], 0);
        instrumentation.sendPointerSync(up);
        up.recycle();

        // Poll until the measured position stabilizes with the IME showing. The keyboard
        // animates in, and the app then asynchronously scrolls the focused field into view (see
        // SolvePuzzleFragment's insets/layout listeners), so we can't just stop at the first sign
        // of a non-zero IME inset -- we need to wait for things to settle.
        int[] measurements = new int[3]; // [editTextBottom, imeBottomInset, visibleBottom]
        int previousEditTextBottom = Integer.MIN_VALUE;
        long deadlineMillis = System.currentTimeMillis() + 8000;
        do {
            instrumentation.waitForIdleSync();
            Thread.sleep(300);
            scenario.onActivity(activity -> {
                EditText userAnswer = findCurrentPageEditText(activity);
                int[] locationOnScreen = new int[2];
                userAnswer.getLocationOnScreen(locationOnScreen);
                measurements[0] = locationOnScreen[1] + userAnswer.getHeight();

                WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(userAnswer);
                measurements[1] = insets == null ? 0 : insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;

                // The decor view doesn't necessarily start at screen Y=0 (e.g. it sits below the
                // status bar), so we need its own on-screen offset to translate "root view height
                // minus IME inset" into an absolute screen coordinate comparable to
                // getLocationOnScreen() above.
                View rootView = userAnswer.getRootView();
                int[] rootLocationOnScreen = new int[2];
                rootView.getLocationOnScreen(rootLocationOnScreen);
                measurements[2] = rootLocationOnScreen[1] + rootView.getHeight() - measurements[1];
            });
            if (measurements[1] > 0 && measurements[0] == previousEditTextBottom) {
                break;
            }
            previousEditTextBottom = measurements[0];
        } while (System.currentTimeMillis() < deadlineMillis);

        int editTextBottom = measurements[0];
        int imeBottomInset = measurements[1];
        int visibleBottom = measurements[2];

        assertTrue("Expected the soft keyboard to be showing (IME inset > 0) but it never appeared; "
                        + "imeBottomInset=" + imeBottomInset,
                imeBottomInset > 0);

        assertTrue("The answer field (bottom=" + editTextBottom + ") is covered by the soft keyboard "
                        + "(visible area ends at " + visibleBottom
                        + "); the user can't see what they're typing",
                editTextBottom <= visibleBottom);
    }

    private static EditText findCurrentPageEditText(SolvePuzzle activity) {
        ViewPager2 pager = activity.findViewById(R.id.pager);
        RecyclerView recyclerView = (RecyclerView) pager.getChildAt(0);
        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(pager.getCurrentItem());
        assertNotNull("No ViewHolder bound yet for adapter position " + pager.getCurrentItem()
                + "; the page may not have finished laying out", holder);
        return holder.itemView.findViewById(R.id.user_answer);
    }

    private static ScrollView findAncestorScrollView(View view) {
        if (view instanceof ScrollView) {
            return (ScrollView) view;
        }
        for (ViewParent parent = view.getParent(); parent instanceof View; parent = ((View) parent).getParent()) {
            if (parent instanceof ScrollView) {
                return (ScrollView) parent;
            }
        }
        throw new IllegalStateException("No ScrollView ancestor found for " + view);
    }
}
