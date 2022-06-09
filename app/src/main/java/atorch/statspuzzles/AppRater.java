package atorch.statspuzzles;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.appcompat.app.AlertDialog;

public class AppRater {

    private static final long HOURS_BETWEEN_PROMPT = 25;  // Avoid spamming people with review prompts
    private static final int LAUNCHES_UNTIL_PROMPT = 3;  // Let people open the app a few times before asking
    private static final int PUZZLES_SOLVED_UNTIL_PROMPT = 5;  // Want to prompt engaged users

    public static void app_launched(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("apprater", Context.MODE_PRIVATE);
        if (prefs.getBoolean("dontshowagain", false)) {
            return;
        }

        SharedPreferences.Editor editor = prefs.edit();

        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);

        long datePreviousPrompt = prefs.getLong("date_previous_prompt", 0);
        if (datePreviousPrompt == 0) {
            datePreviousPrompt = System.currentTimeMillis();
            editor.putLong("date_previous_prompt", datePreviousPrompt);
        }

        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            long minimumCurrentTime = datePreviousPrompt + HOURS_BETWEEN_PROMPT * 60 * 60 * 1000;
            SharedPreferences appData = context.getSharedPreferences("atorch.statspuzzles.data", Context.MODE_PRIVATE);
            int solved0 = appData.getInt("solved_0", 0);
            int solved1 = appData.getInt("solved_1", 0);
            int solved2 = appData.getInt("solved_2", 0);
            int solvedTotal = solved0 + solved1 + solved2;
            if (System.currentTimeMillis() >= minimumCurrentTime &&
                    solvedTotal >= PUZZLES_SOLVED_UNTIL_PROMPT) {
                datePreviousPrompt = System.currentTimeMillis();
                editor.putLong("date_previous_prompt", datePreviousPrompt);
                showRateDialog(context, editor);
            }
        }

        editor.commit();
    }

    public static void showRateDialog(Context context, SharedPreferences.Editor editor) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.apprater_title);
        builder.setMessage(R.string.apprater_message);
        builder.setPositiveButton(R.string.apprater_rate_button,
                (dialog, id) -> {
                    context.startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=atorch.statspuzzles")));
                    dialog.dismiss();
                });

        builder.setNeutralButton(R.string.apprater_later_button,
                (dialog, id) -> dialog.dismiss());

        builder.setNegativeButton(R.string.apprater_never_button,
                (dialog, id) -> {
                    if (editor != null) {
                        editor.putBoolean("dontshowagain", true);
                        editor.commit();
                    }
                    dialog.dismiss();
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
