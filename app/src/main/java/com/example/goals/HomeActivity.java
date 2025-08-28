package com.example.goals;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity {

    private String secret;
    private String nickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        secret = getIntent().getStringExtra("secret");
        nickname = getIntent().getStringExtra("nickname");

        TextView welcomeMessage = findViewById(R.id.welcome_message);
        if (nickname != null && !nickname.isEmpty()) {
            welcomeMessage.setText("Hello,\n" + nickname);
        }

        // 🔍 Check if date exists in Firebase
        DatabaseReference dbRef = FirebaseDatabase.getInstance()
                .getReference("secrets")
                .child(secret)
                .child("date");

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String dateValue = snapshot.getValue(String.class);

                // ✅ Show toast with the date (or fallback message)
                if (dateValue != null && !dateValue.isEmpty()) {
                    Toast.makeText(HomeActivity.this,
                            "Date from Firebase: " + dateValue,
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(HomeActivity.this,
                            "No date set yet!",
                            Toast.LENGTH_SHORT).show();

                    // open fragment if no date found
                    FragmentManager fm = getSupportFragmentManager();
                    DatePickerFragment fragment = DatePickerFragment.newInstance(secret);
                    fragment.show(fm, "datePickerFragment");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this,
                        "DB Error: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
