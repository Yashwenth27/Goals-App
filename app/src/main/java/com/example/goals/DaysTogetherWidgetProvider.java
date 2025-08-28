package com.example.goals;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.RemoteViews;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DaysTogetherWidgetProvider extends AppWidgetProvider {

    private static final String PREFS_NAME = "UserPrefs"; // same as MainActivity
    private static final String KEY_SECRET = "secret";
    private static final String KEY_NICKNAME = "nickname";

    // Method to update a single widget
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_days_together);

        // Load saved secret & nickname
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String secret = prefs.getString(KEY_SECRET, null);
        String nickname = prefs.getString(KEY_NICKNAME, null);

        if (secret == null || nickname == null) {
            // User not logged in
            views.setTextViewText(R.id.widget_textViewDays, "Login required");
            appWidgetManager.updateAppWidget(appWidgetId, views);
            return;
        }

        // Reference to Firebase
        DatabaseReference dbRef = FirebaseDatabase
                .getInstance("https://checkfirebase-e2d78-default-rtdb.firebaseio.com/")
                .getReference("secrets")
                .child(secret)
                .child("date");

        // Fetch date from Firebase
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String savedDate = snapshot.getValue(String.class);

                String daysText;
                if (savedDate == null || savedDate.isEmpty()) {
                    daysText = "Pick start date";
                } else {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            // Use the correct pattern to match your Firebase date
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

                            // Parse the saved string into a LocalDate
                            LocalDate startDate = LocalDate.parse(savedDate, formatter);

                            // Current date
                            LocalDate today = LocalDate.now();

                            // Days between
                            long daysBetween = ChronoUnit.DAYS.between(startDate, today);

                            daysText = daysBetween + " days";
                        } else {
                            daysText = "Needs Android 8+";
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        daysText = "Error parsing: " + savedDate;
                    }


                }

                views.setTextViewText(R.id.widget_textViewDays, daysText);

                // Make widget clickable → open app
                Intent intent = new Intent(context, MainActivity.class);
                PendingIntent pendingIntent;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    pendingIntent = PendingIntent.getActivity(context, 0, intent,
                            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
                } else {
                    pendingIntent = PendingIntent.getActivity(context, 0, intent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                }
                views.setOnClickPendingIntent(R.id.widget_root_layout, pendingIntent);

                // Update widget
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                views.setTextViewText(R.id.widget_textViewDays, "DB Error");
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        });
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Optionally schedule periodic updates using AlarmManager/WorkManager if needed
    }

    @Override
    public void onDisabled(Context context) {
        // Clean up if required
    }
}
