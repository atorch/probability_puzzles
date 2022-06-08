package atorch.statspuzzles;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import org.mariuszgromada.math.mxparser.Expression;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class SolvePuzzle extends AppCompatActivity {
    // Following example at https://developer.android.com/training/sharing/shareaction.html
    private ShareActionProvider mShareActionProvider;

    public final static String PUZZLE_INDEX = "atorch.statspuzzles.PUZZLE_INDEX";
    public final static String LEVEL = "atorch.statspuzzles.LEVEL";
    private final static int roundingScale = 4;

    private ViewPager puzzlePager;  // Displays puzzles one at a time

    private int level;

    private Res res;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solve_puzzle);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();
        if (intent != null) {
            level = intent.getIntExtra(PuzzleSelection.LEVEL, 0);
        }

        FragmentManager fragmentManager = getSupportFragmentManager();

        res = new Res(getResources());
        puzzlePager = findViewById(R.id.pager);
        puzzlePager.setAdapter(new AppSectionsPagerAdapter(fragmentManager, level, res));

        puzzlePager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageScrollStateChanged(int arg0) {
            }
            @Override public void onPageScrolled(int arg0, float arg1, int arg2) {
            }
            @Override public void onPageSelected(int puzzleIndex) {
                callSetShareIntent(res.getPuzzle(level, puzzleIndex));
            }
        });

        puzzlePager.setCurrentItem(indexFirstUnsolvedPuzzle());
    }

    private int indexFirstUnsolvedPuzzle() {
        SharedPreferences preferences = getSharedPreferences("atorch.statspuzzles.data", Context.MODE_PRIVATE);
        int n = res.getPuzzleCount(level);
        for (int i = 0; i < n; i++) {
            String key = level + "_" + i;
            if (!preferences.getBoolean(key, false))
                return i;  // Puzzle i has not been solved
        }
        return n - 1;  // Everything solved, return last index
    }

    private void callSetShareIntent(String puzzleStatement) {
        String extraText = puzzleStatement + "\n\n" + getString(R.string.app_link);
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, extraText);
        shareIntent.setType("text/plain");
        setShareIntent(shareIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.solve_puzzle, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Following http://stackoverflow.com/questions/19118051/unable-to-cast-action-provider-to-share-action-provider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        if (mShareActionProvider == null) {
            // Following http://stackoverflow.com/questions/19358510/why-menuitemcompat-getactionprovider-returns-null
            mShareActionProvider = new ShareActionProvider(this);
            MenuItemCompat.setActionProvider(item, mShareActionProvider);
        }
        callSetShareIntent(res.getPuzzle(level, puzzlePager.getCurrentItem()));
        return true;  // Return true to display menu

    }

    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);  // Should be called whenever new fragment is displayed
        }
    }

    private static class Res {

        // Level -1 is the introduction / how to.

        private static final int[] PUZZLES_ID = {
                R.array.puzzles_intro,
                R.array.puzzles_0,
                R.array.puzzles_1,
                R.array.puzzles_2,
        };

        private static final int[] IMAGES_ID = {
                R.array.images_intro,
                R.array.images_0,
                R.array.images_1,
                R.array.images_2,
        };

        private static final int[] HINTS_ID = {
                R.array.hints_intro,
                R.array.hints_0,
                R.array.hints_1,
                R.array.hints_2,
        };

        private static final int[] ANSWERS_ID = {
                R.array.answers_intro,
                R.array.answers_0,
                R.array.answers_1,
                R.array.answers_2,
        };

        private static final int[] CONGRATULATIONS_FIRST_ID = {
                R.string.congratulations_first_intro,
                R.string.congratulations_first_0,
                R.string.congratulations_first_1,
                R.string.congratulations_first_2,
        };

        private final Resources resources;
        private final Random random = new Random();

        private Res(Resources resources) {
            this.resources = resources;
        }

        String getLevelDescription(int level) {
            return resources.getStringArray(R.array.levelDescriptions)[level];
        }

        int getPuzzleCount(int level) {
            return resources.getStringArray(PUZZLES_ID[level + 1]).length;
        }

        String getPuzzle(int level, int puzzleIndex) {
            return resources.getStringArray(PUZZLES_ID[level + 1])[puzzleIndex];
        }

        String getImage(int level, int puzzleIndex) {
            String[] images = resources.getStringArray(IMAGES_ID[level + 1]);
            return puzzleIndex < images.length ? images[puzzleIndex] : "";
        }

        String getHint(int level, int puzzleIndex) {
            return resources.getStringArray(HINTS_ID[level + 1])[puzzleIndex];
        }

        String getAnswer(int level, int puzzleIndex) {
            return resources.getStringArray(ANSWERS_ID[level + 1])[puzzleIndex];
        }

        String getRandomToast() {
            String[] toasts = resources.getStringArray(R.array.toasts_for_incorrect_answers);
            return toasts[random.nextInt(toasts.length)];
        }

        String getRandomCongratulation() {
            String[] congratulations = resources.getStringArray(R.array.congratulations);
            return congratulations[random.nextInt(congratulations.length)];
        }

        String getFirstCongratulation(int level) {
            return resources.getString(CONGRATULATIONS_FIRST_ID[level + 1]);
        }
    }

    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        private final int level;
        private final Res res;

        public AppSectionsPagerAdapter(FragmentManager fm, int level, Res res) {
            super(fm);
            this.level = level;
            this.res = res;
        }

        @Override
        public Fragment getItem(int puzzleIndex) {
            Fragment fragment = new SolvePuzzleFragment();
            Bundle args = new Bundle();
            args.putInt(LEVEL, level);
            args.putInt(PUZZLE_INDEX, puzzleIndex);

            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return res.getPuzzleCount(level);
        }
    }

    public static class SolvePuzzleFragment extends Fragment implements OnClickListener {

        private int level;
        private int puzzleIndex;
        private double correctAnswer;
        private ViewPager mViewPager;
        private Res res;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_solve_puzzle, container, false);
            res = new Res(getResources());

            Bundle args = getArguments();
            level = args.getInt(LEVEL);
            puzzleIndex = args.getInt(PUZZLE_INDEX);

            TextView description = rootView.findViewById(R.id.puzzleDescription);
            if (puzzleIndex == 0 && level >= 0) {
                // Only show puzzle description on first puzzle, and not in intro level
                description.setText(res.getLevelDescription(level));
            } else {
                ((ViewGroup) description.getParent()).removeView(description);
            }
            // Always show puzzle number
            TextView puzzleNumber = rootView.findViewById(R.id.puzzleNumber);
            puzzleNumber.setText(getString(R.string.puzzle, puzzleIndex + 1));

            TextView puzzleStatement = rootView.findViewById(R.id.puzzleStatement);

            Button hintButton = rootView.findViewById(R.id.button_hint);
            hintButton.setVisibility(View.VISIBLE);
            hintButton.setOnClickListener(unused -> showHint());

            puzzleStatement.setText(res.getPuzzle(level, puzzleIndex));
            Button button = rootView.findViewById(R.id.submit_answer);
            button.setOnClickListener(this);  // onClick defined below

            String answer = res.getAnswer(level, puzzleIndex);
            Expression correctAnswerExpression = new Expression(answer);
            correctAnswer = correctAnswerExpression.calculate();  // This needs to always parse correctly!

            EditText user_answer = rootView.findViewById(R.id.user_answer);
            SharedPreferences preferences = this.getActivity().getSharedPreferences("atorch.statspuzzles.data", Context.MODE_PRIVATE);
            String key = level + "_" + puzzleIndex;
            boolean already_solved_this_puzzle = preferences.getBoolean(key, false);
            if (already_solved_this_puzzle) {
                // If puzzle has already been solved, show check mark and correct answer
                ImageView checkMark = rootView.findViewById(R.id.check_mark);
                checkMark.setVisibility(View.VISIBLE);
                user_answer.setText(answer);
                // Show approx equal for correct answer
                TextView answerApprox = rootView.findViewById(R.id.answerApprox);
                BigDecimal bd = new BigDecimal(correctAnswer).setScale(roundingScale, RoundingMode.HALF_EVEN);
                answerApprox.setText(getString(R.string.approximate_result, bd));
            }

            // Add image below puzzle statement
            String packageName = this.getActivity().getPackageName();
            String image = res.getImage(level, puzzleIndex);
            if (!image.isEmpty()) {
                ImageView imageView = rootView.findViewById(R.id.puzzleImage);
                int id = getResources().getIdentifier(image, "drawable", packageName);
                imageView.setImageResource(id);
            }
            return rootView;
        }

        private void showHint() {
            String hint = res.getHint(level, puzzleIndex);
            final SpannableString hintSpannable = new SpannableString(hint); // msg should have url to enable clicking
            Linkify.addLinks(hintSpannable, Linkify.ALL);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(hintSpannable);
            builder.setCancelable(true);
            builder.setPositiveButton(R.string.button_back_to_puzzle,
                    (dialog, id) -> dialog.cancel()
            );
            AlertDialog alert = builder.create();
            alert.show();

            // See https://stackoverflow.com/a/3367392/610668
            ((TextView)alert.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
        }

        public void onClick(View view) {
            ImageView checkMark = getView().findViewById(R.id.check_mark);
            checkMark.setVisibility(View.INVISIBLE);
            EditText userAnswer = getView().findViewById(R.id.user_answer);
            String answerString = userAnswer.getText().toString();
            TextView answerApprox = getView().findViewById(R.id.answerApprox);
            double answer = Double.NaN;
            boolean hadTroubleParsing = false;

            Expression answerExpression = new Expression(answerString);
            answer = answerExpression.calculate();
            if(!answerExpression.checkSyntax()) {
                openTroubleParsingDialog(view);  // TODO Don't show if user answer is empty string?
                hadTroubleParsing = true;
            }
            if (!Double.isNaN(answer) && !Double.isInfinite(answer)) {
                BigDecimal bd = new BigDecimal(answer).setScale(roundingScale, RoundingMode.HALF_EVEN);  // Number of digits after decimal point
                answerApprox.setText(getString(R.string.approximate_result, bd));
            } else {
                // Empty answerApprox when answer doesn't parse or is NaN or is infinite
                answerApprox.setText("");
            }
            // TODO Careful with accuracy, e.g. clock puzzle
            if (!Double.isNaN(answer) && Math.abs(answer - correctAnswer) < 0.00001) {
                openCongratulationsAlert(view);
            } else {
                if (!Double.isNaN(answer) && Math.abs(answer - correctAnswer) < 0.001) {
                    openAccuracyAlert(view);
                } else if (!hadTroubleParsing) {
                    openIncorrectAnswerToast();  // User answer could parse to NaN
                }
            }
        }

        public void openTroubleParsingDialog(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(getString(R.string.trouble_parsing_answer));
            builder.setCancelable(true);
            builder.setPositiveButton(R.string.okay_button,
                    (dialog, id) -> dialog.cancel()
            );
            AlertDialog alert = builder.create();
            alert.show();
        }

        public void openCongratulationsAlert(View view) {
            ImageView checkmark = getView().findViewById(R.id.check_mark);
            checkmark.setVisibility(View.VISIBLE);
            SharedPreferences preferences = this.getActivity().getSharedPreferences("atorch.statspuzzles.data", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            String key = level + "_" + puzzleIndex;
            boolean already_solved_this_puzzle = preferences.getBoolean(key, false);
            String congratulations = res.getRandomCongratulation();

            String counter_key = level == 2 ? "solved_2"
                    : level == 1 ? "solved_1"
                    : level == 0 ? "solved_0"
                    : "solved_intro";

            int puzzles_solved = preferences.getInt(counter_key, 0);

            if (!already_solved_this_puzzle) {
                if (puzzles_solved == 0) {
                    congratulations = res.getFirstCongratulation(level);
                }
                puzzles_solved = puzzles_solved + 1;
                editor.putInt(counter_key, puzzles_solved);
            }
            editor.putBoolean(key, true);
            editor.commit();

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            mViewPager = view.getRootView().findViewById(R.id.pager);

            int nPuzzles = res.getPuzzleCount(level);
            boolean solvedAllPuzzles = puzzles_solved >= nPuzzles;
            if (!solvedAllPuzzles) {
                builder.setMessage(congratulations);
                builder.setCancelable(true);
                builder.setPositiveButton(R.string.next_puzzle_button,
                        (dialog, id) -> {
                            int next_puzzleIndex = puzzleIndex + 1;
                            if (next_puzzleIndex >= nPuzzles) {
                                next_puzzleIndex = 0;
                            }
                            mViewPager.setCurrentItem(next_puzzleIndex);
                            dialog.cancel();
                        }
                );
                builder.setNegativeButton(R.string.main_menu_button,
                        (dialog, id) -> {
                            Intent intent = new Intent(getActivity(), PuzzleSelection.class);
                            startActivity(intent);
                        }
                );
            } else {
                if(level >= 0) {
                    builder.setMessage(getString(R.string.solved_all_puzzles));
                } else {
                    builder.setMessage(getString(R.string.solved_all_intro));
                }
                builder.setCancelable(true);
                builder.setPositiveButton(R.string.main_menu_button,
                        (dialog, id) -> {
                            Intent intent = new Intent(getActivity(), PuzzleSelection.class);
                            startActivity(intent);
                        }
                );
                builder.setNegativeButton(R.string.stay_here_button,
                        (dialog, id) -> dialog.cancel()
                );
            }
            AlertDialog alert = builder.create();
            alert.show();
        }

        public void openIncorrectAnswerToast() {
            Context context = getActivity();
            Toast.makeText(context, res.getRandomToast(), Toast.LENGTH_LONG).show();
        }

        public void openAccuracyAlert(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(getString(R.string.accuracy));
            builder.setCancelable(true);
            builder.setPositiveButton(R.string.ok_button,
                    (dialog, id) -> dialog.cancel()
            );
            AlertDialog alert = builder.create();
            alert.show();
        }

    }
}
