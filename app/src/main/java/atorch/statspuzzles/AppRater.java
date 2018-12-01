package atorch.statspuzzles;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

public class AppRater {
    private final static String APP_TITLE = "Probability Puzzles";
    private final static String APP_PACKAGE_NAME = "atorch.statspuzzles";

    private final static double HOURS_BETWEEN_PROMPT = 25;  // Avoid spamming people with review prompts
    private final static int LAUNCHES_UNTIL_PROMPT = 3;
    private final static int PUZZLES_SOLVED_UNTIL_PROMPT = 5;  // Want to prompt engaged users

    public static void app_launched(Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("apprater", 0);
        if (prefs.getBoolean("dontshowagain", false)) { return ; }

        SharedPreferences.Editor editor = prefs.edit();

        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);

        Long datePreviousPrompt = prefs.getLong("date_previous_prompt", 0);
        if (datePreviousPrompt == 0) {
            datePreviousPrompt = System.currentTimeMillis();
            editor.putLong("date_previous_prompt", datePreviousPrompt);
        }

        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            double minimumCurrentTime = datePreviousPrompt + HOURS_BETWEEN_PROMPT * 60 * 60 * 1000;
            SharedPreferences appData = mContext.getSharedPreferences("atorch.statspuzzles.data", Context.MODE_PRIVATE);
            int solved0 = appData.getInt("solved_0", 0);
            int solved1 = appData.getInt("solved_1", 0);
            int solved2 = appData.getInt("solved_2", 0);
            int solvedTotal = solved0 + solved1 + solved2;
            if (System.currentTimeMillis() >= minimumCurrentTime &&
                solvedTotal >= PUZZLES_SOLVED_UNTIL_PROMPT) {
                datePreviousPrompt = System.currentTimeMillis();
                editor.putLong("date_previous_prompt", datePreviousPrompt);
                showRateDialog(mContext, editor);
            }
        }

        editor.commit();
    }

    public static void showRateDialog(final Context mContext, final SharedPreferences.Editor editor) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Rate " + APP_TITLE);
        builder.setMessage("Enjoying " + APP_TITLE + "? Please take a moment to rate the app in the Play Store. Every review counts. Thank you for helping out!");
        builder.setPositiveButton("Rate It!",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PACKAGE_NAME)));
                        dialog.dismiss();
                    }
                });

        builder.setNeutralButton("Maybe Later",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        builder.setNegativeButton("Never",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (editor != null) {
                            editor.putBoolean("dontshowagain", true);
                            editor.commit();
                        }
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
