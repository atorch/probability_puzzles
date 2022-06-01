package atorch.statspuzzles;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

public class PuzzleSelection extends AppCompatActivity {

    public final static String LEVEL = "atorch.statspuzzles.LEVEL";

    public void startPuzzle(View view) {
        Intent intent = new Intent(this, SolvePuzzle.class);
        int level = -1;
        int view_id = view.getId();
        if (view_id == R.id.button_0) {
            level = 0;
        } else if (view_id == R.id.button_1) {
            level = 1;
        } else if(view_id == R.id.button_2) {
            level = 2;
        }
        intent.putExtra(LEVEL, level);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle_selection);
        updateCounter();
        AppRater.app_launched(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCounter();  // In case user hits back arrow after solving a puzzle
    }

    public void updateCounter() {
        SharedPreferences prefs = getSharedPreferences("atorch.statspuzzles.data", Context.MODE_PRIVATE);

        updateSolved(prefs, "solved_intro", R.id.counter_intro, R.array.puzzles_intro);
        updateSolved(prefs, "solved_0", R.id.counter_0, R.array.puzzles_0);
        updateSolved(prefs, "solved_1", R.id.counter_1, R.array.puzzles_1);
        updateSolved(prefs, "solved_2", R.id.counter_2, R.array.puzzles_2);

        TextView newInThisUpdate = findViewById(R.id.new_in_this_update);
        newInThisUpdate.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void updateSolved(SharedPreferences prefs, String prefsKey, int viewId, int arrayId) {
        int count = prefs.getInt(prefsKey, 0);
        if (count > 0) {
            int total = getResources().getStringArray(arrayId).length;
            String text = getString(R.string.solved, count, total);
            ((TextView) findViewById(viewId)).setText(text);
        }
    }
}
