package atorch.statspuzzles;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
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
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

import org.mariuszgromada.math.mxparser.*;

public class SolvePuzzle extends AppCompatActivity {
    // Following example at https://developer.android.com/training/sharing/shareaction.html
    private ShareActionProvider mShareActionProvider;

    public final static String N_PUZZLES = "atorch.statspuzzles.N_PUZZLES";
    public final static String PUZZLE_INDEX = "atorch.statspuzzles.PUZZLE_INDEX";
    public final static String PUZZLE = "atorch.statspuzzles.PUZZLE";
    public final static String LEVEL = "atorch.statspuzzles.LEVEL";
    public final static String ANSWER = "atorch.statspuzzles.ANSWER";
    public final static String HINT = "atorch.statspuzzles.HINT";
    public final static String IMAGE_STRING = "atorch.statspuzzles.IMAGE_STRING";
    public final static String LEVEL_DESCRIPTION = "atorch.statspuzzles.LEVEL_DESCRIPTION";
    public final static Random random = new Random();
    private final static int roundingScale = 4;

    private AppSectionsPagerAdapter mAppSectionsPagerAdapter;  // Returns fragments
    private ViewPager mViewPager;  // Displays puzzles one at a time

    private int level;
    private String images[];
    private String puzzles[];
    private String answers[];
    private String hints[];

    static private String levelDescriptions[];
    static private String congratulationsArray[];

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

        Resources resources = getResources();
        levelDescriptions = resources.getStringArray(R.array.levelDescriptions);
        congratulationsArray = resources.getStringArray(R.array.congratulations);
        if (level == 2) {
            images = resources.getStringArray(R.array.images_2);
            puzzles = resources.getStringArray(R.array.puzzles_2);
            answers = resources.getStringArray(R.array.answers_2);
            hints = resources.getStringArray(R.array.hints_2);
        } else if (level == 1) {
            images = resources.getStringArray(R.array.images_1);
            puzzles = resources.getStringArray(R.array.puzzles_1);
            answers = resources.getStringArray(R.array.answers_1);
            hints = resources.getStringArray(R.array.hints_1);
        } else if (level == 0){
            images = resources.getStringArray(R.array.images_0);
            puzzles = resources.getStringArray(R.array.puzzles_0);
            answers = resources.getStringArray(R.array.answers_0);
            hints = resources.getStringArray(R.array.hints_0);
        } else {
            // Level -1, introduction / how to
            images = resources.getStringArray(R.array.images_intro);
            puzzles = resources.getStringArray(R.array.puzzles_intro);
            answers = resources.getStringArray(R.array.answers_intro);
            hints = resources.getStringArray(R.array.hints_intro);
        }

        FragmentManager fragmentManager = getSupportFragmentManager();

        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(fragmentManager,
                level,
                images,
                levelDescriptions,
                puzzles,
                answers,
                hints);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageScrollStateChanged(int arg0) {
            }
            @Override public void onPageScrolled(int arg0, float arg1, int arg2) {
            }
            @Override public void onPageSelected(int puzzleIndex) {
                callSetShareIntent(puzzles[puzzleIndex]);
            }
        });

        mViewPager.setCurrentItem(indexFirstUnsolvedPuzzle());
    }

    private int indexFirstUnsolvedPuzzle() {
        SharedPreferences preferences = getSharedPreferences("atorch.statspuzzles.data", Context.MODE_PRIVATE);
        for(int i=0; i<puzzles.length; i++) {
            String key = level + "_" + i;
            if(!preferences.getBoolean(key, false))
                return i;  // Puzzle i has not been solved
        }
        return puzzles.length - 1;  // Everything solved, return last index
    }

    private void callSetShareIntent(String puzzleStatement) {
        String extraText = puzzleStatement + "\n\n" + getString(R.string.app_link);
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, extraText);
        shareIntent.setType("text/plain");
        setShareIntent(shareIntent);
    }

    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }*/

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
        callSetShareIntent(puzzles[mViewPager.getCurrentItem()]);
        return true;  // Return true to display menu

    }

    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);  // Should be called whenever new fragment is displayed
        }
    }

    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        private int level;
        private String images[];
        private String puzzles[];
        private String answers[];
        private String hints[];

        private String levelDescriptions[];

        public AppSectionsPagerAdapter(
                FragmentManager fm,
                int level,
                String images[],
                String levelDescriptions[],
                String[] puzzles,
                String[] answers,
                String[] hints
        ) {
            super(fm);
            this.level = level;
            this.images = images;
            this.levelDescriptions = levelDescriptions;
            this.puzzles = puzzles;
            this.answers = answers;
            this.hints = hints;
        }

        @Override
        public Fragment getItem(int puzzleIndex) {
            Fragment fragment = new SolvePuzzleFragment();
            Bundle args = new Bundle();
            args.putInt(LEVEL, level);
            args.putInt(N_PUZZLES, puzzles.length);
            args.putInt(PUZZLE_INDEX, puzzleIndex);
            args.putString(PUZZLE, puzzles[puzzleIndex]);
            args.putString(HINT, hints[puzzleIndex]);
            args.putString(ANSWER, answers[puzzleIndex]);
            if(puzzleIndex < images.length - 1) {
                args.putString(IMAGE_STRING, images[puzzleIndex]);
            }
            if(level >= 0) {
                args.putString(LEVEL_DESCRIPTION, levelDescriptions[level]);
            }

            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return puzzles.length;
        }
    }

    public static class SolvePuzzleFragment extends Fragment implements OnClickListener {

        private int level;
        private int puzzleIndex;
        private int nPuzzles;
        private double correctAnswer;
        private String levelDescription;
        private String hint;
        private String puzzle;
        private String answer;
        private String imageString;
        private ViewPager mViewPager;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_solve_puzzle, container, false);

            Bundle args = getArguments();
            nPuzzles = args.getInt(N_PUZZLES);
            level = args.getInt(LEVEL);
            puzzleIndex = args.getInt(PUZZLE_INDEX);
            hint = args.getString(HINT);
            puzzle = args.getString(PUZZLE);
            answer = args.getString(ANSWER);
            if(args.containsKey(IMAGE_STRING)){
                imageString = args.getString(IMAGE_STRING);
            } else {
                imageString = "";
            }

            TextView description = (TextView) rootView.findViewById(R.id.puzzleDescription);
            if(puzzleIndex == 0 && level >= 0) {
                // Only show puzzle description on first puzzle, and not in intro level
                levelDescription = args.getString(LEVEL_DESCRIPTION);
                description.setText(levelDescription);
            } else {
                ((ViewGroup) description.getParent()).removeView(description);
            }
            // Always show puzzle number
            TextView puzzleNumber = (TextView) rootView.findViewById(R.id.puzzleNumber);
            puzzleNumber.setText(getString(R.string.puzzle) + " " + String.valueOf(puzzleIndex + 1));

            TextView puzzleStatement = (TextView) rootView.findViewById(R.id.puzzleStatement);

            Button hintButton = (Button) rootView.findViewById(R.id.button_hint);
            hintButton.setVisibility(View.VISIBLE);
            hintButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View unused) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(hint);
                    builder.setCancelable(true);
                    builder.setPositiveButton("Back to Puzzle",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            }
                    );
                    AlertDialog alert = builder.create();
                    alert.show();

                }
            });

            // getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

            puzzleStatement.setText(puzzle);
            Button button = (Button) rootView.findViewById(R.id.submit_answer);
            button.setOnClickListener(this);  // onClick defined below

            Expression correctAnswerExpression = new Expression(answer);
            correctAnswer = correctAnswerExpression.calculate();  // This needs to always parse correctly!

            EditText user_answer = (EditText)rootView.findViewById(R.id.user_answer);
            SharedPreferences preferences = this.getActivity().getSharedPreferences("atorch.statspuzzles.data", Context.MODE_PRIVATE);
            String key = level + "_" + puzzleIndex;
            boolean already_solved_this_puzzle = preferences.getBoolean(key, false);
            if (already_solved_this_puzzle) {
                // If puzzle has already been solved, show check mark and correct answer
                ImageView checkMark = (ImageView)rootView.findViewById(R.id.check_mark);
                checkMark.setVisibility(View.VISIBLE);
                user_answer.setText(answer);
                // Show approx equal for correct answer
                TextView answerApprox = (TextView)rootView.findViewById(R.id.answerApprox);
                BigDecimal bd = new BigDecimal(correctAnswer).setScale(roundingScale, RoundingMode.HALF_EVEN);
                answerApprox.setText(" ≈ " + bd.toString());
            }

            // Add image below puzzle statement
            String packageName = this.getActivity().getPackageName();
            if (imageString != "") {
                ImageView imageView = (ImageView)rootView.findViewById(R.id.puzzleImage);
                int id = getResources().getIdentifier(imageString, "drawable", packageName);
                imageView.setImageResource(id);
            }
            return rootView;
        }

        public void onClick(View view) {
            ImageView checkMark = (ImageView)getView().findViewById(R.id.check_mark);
            checkMark.setVisibility(View.INVISIBLE);
            EditText userAnswer = (EditText)getView().findViewById(R.id.user_answer);
            String answerString = userAnswer.getText().toString();
            TextView answerApprox = (TextView)getView().findViewById(R.id.answerApprox);
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
                answerApprox.setText(" ≈ " + bd.toString());
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
            builder.setPositiveButton("Okay",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    }
            );
            AlertDialog alert = builder.create();
            alert.show();
        }

        public void openCongratulationsAlert(View view) {
            ImageView checkmark = (ImageView)getView().findViewById(R.id.check_mark);
            checkmark.setVisibility(View.VISIBLE);
            SharedPreferences preferences = this.getActivity().getSharedPreferences("atorch.statspuzzles.data", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            String key = level + "_" + puzzleIndex;
            boolean already_solved_this_puzzle = preferences.getBoolean(key, false);
            String congratulations = congratulationsArray[random.nextInt(congratulationsArray.length)];

            String counter_key, congratulations_first;
            if (level == 2) {
                counter_key = getString(R.string.counter_2);
                congratulations_first = getString(R.string.congratulations_first_2);
            } else if (level == 1) {
                counter_key = getString(R.string.counter_1);
                congratulations_first = getString(R.string.congratulations_first_1);
            } else if (level == 0){
                counter_key = getString(R.string.counter_0);
                congratulations_first = getString(R.string.congratulations_first_0);
            } else {
                counter_key = getString(R.string.counter_intro);
                congratulations_first = getString(R.string.congratulations_first_intro);
            }
            int puzzles_solved = preferences.getInt(counter_key, 0);

            if (!already_solved_this_puzzle) {
                if (puzzles_solved == 0) {
                    congratulations = congratulations_first;
                }
                puzzles_solved = puzzles_solved + 1;
                editor.putInt(counter_key, puzzles_solved);
            }
            editor.putBoolean(key, true);
            editor.commit();

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            mViewPager = (ViewPager) view.getRootView().findViewById(R.id.pager);

            boolean solvedAllPuzzles = puzzles_solved >= nPuzzles;
            if(!solvedAllPuzzles) {
                builder.setMessage(congratulations);
                builder.setCancelable(true);
                builder.setPositiveButton("Next Puzzle",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                int next_puzzleIndex = puzzleIndex + 1;
                                if (next_puzzleIndex >= nPuzzles) {
                                    next_puzzleIndex = 0;
                                }
                                mViewPager.setCurrentItem(next_puzzleIndex);
                                dialog.cancel();
                            }
                        }
                );
                builder.setNegativeButton("Main Menu",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(getActivity(), PuzzleSelection.class);
                                startActivity(intent);
                            }
                        }
                );
            } else {
                if(level >= 0) {
                    builder.setMessage(getString(R.string.solved_all_puzzles));
                } else {
                    builder.setMessage(getString(R.string.solved_all_intro));
                }
                builder.setCancelable(true);
                builder.setPositiveButton("Main Menu",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(getActivity(), PuzzleSelection.class);
                                startActivity(intent);
                            }
                        }
                );
                builder.setNegativeButton("Stay Here",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }
                );
            }
            AlertDialog alert = builder.create();
            alert.show();
        }

        public void openIncorrectAnswerToast() {
            Context context = getActivity();
            Resources resources = getResources();
            String[] toasts = resources.getStringArray(R.array.toasts_for_incorrect_answers);
            Toast.makeText(context, toasts[random.nextInt(toasts.length)], Toast.LENGTH_LONG).show();
        }

        public void openAccuracyAlert(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(getString(R.string.accuracy));
            builder.setCancelable(true);
            builder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }

    }
}
