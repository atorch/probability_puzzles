package atorch.statspuzzles;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
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
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCounter();  // In case user hits back arrow after solving a puzzle
    }

    public void updateCounter() {
        SharedPreferences sharedPref = getSharedPreferences("atorch.statspuzzles.data", Context.MODE_PRIVATE);
        Resources res = getResources();

        int countHint = sharedPref.getInt(getString(R.string.counter_intro), 0);
        int count0 = sharedPref.getInt(getString(R.string.counter_0), 0);
        int count1 = sharedPref.getInt(getString(R.string.counter_1), 0);
        int count2 = sharedPref.getInt(getString(R.string.counter_2), 0);

        TextView counterView;
        if (countHint > 0) {
            counterView = (TextView) findViewById(R.id.counter_intro);
            counterView.setText(getString(R.string.counter) + " " + countHint + " / " + res.getStringArray(R.array.puzzles_intro).length);
        }
        if (count0 > 0) {
            counterView = (TextView) findViewById(R.id.counter_0);
            counterView.setText(getString(R.string.counter) + " " + count0 + " / " + res.getStringArray(R.array.puzzles_0).length);
        }
        if (count1 > 0) {
            counterView = (TextView) findViewById(R.id.counter_1);
            counterView.setText(getString(R.string.counter) + " " + count1 + " / " + res.getStringArray(R.array.puzzles_1).length);
        }
        if (count2 > 0) {
            counterView = (TextView) findViewById(R.id.counter_2);
            counterView.setText(getString(R.string.counter) + " " + count2 + " / " + res.getStringArray(R.array.puzzles_2).length);
        }

        TextView newInThisUpdate = (TextView) findViewById(R.id.new_in_this_update);
        newInThisUpdate.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
