package com.example.goals;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText et_Nickname;
    private EditText et_Secret;
    private Button bt_Join;

    private DatabaseReference databaseRef;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        // ✅ Step 1: Check if already logged in
        String savedSecret = prefs.getString("secret", null);
        String savedNickname = prefs.getString("nickname", null);

        if (savedSecret != null && savedNickname != null) {
            // Skip login
            goToHome(savedSecret, savedNickname);
            return;
        }

        // Otherwise, show login screen
        setContentView(R.layout.activity_main);

        et_Nickname = findViewById(R.id.Nickname);
        et_Secret = findViewById(R.id.Secret);
        bt_Join = findViewById(R.id.Join);

        // Root reference to "secrets"
        databaseRef = FirebaseDatabase
                .getInstance("https://checkfirebase-e2d78-default-rtdb.firebaseio.com/")
                .getReference("secrets");

        bt_Join.setOnClickListener(v -> {
            String nickname = et_Nickname.getText().toString().trim();
            String secret = et_Secret.getText().toString().trim();

            if (nickname.isEmpty() || secret.isEmpty()) {
                Toast.makeText(MainActivity.this, "Enter both Nickname & Secret", Toast.LENGTH_SHORT).show();
            } else {
                handleLoginOrSignup(secret, nickname);
            }
        });
    }

    private void handleLoginOrSignup(final String secret, final String nickname) {
        databaseRef.child(secret).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Secret exists
                    if (snapshot.hasChild(nickname)) {
                        // Nickname already exists -> login success
                        Toast.makeText(MainActivity.this, "Login Success!", Toast.LENGTH_SHORT).show();
                        saveUser(secret, nickname);
                        goToHome(secret, nickname);
                    } else {
                        // Nickname not found -> create new nickname under secret
                        Map<String, Object> nicknameMap = new HashMap<>();
                        nicknameMap.put("name", nickname);

                        databaseRef.child(secret).child(nickname).setValue(nicknameMap)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(MainActivity.this, "New Nickname added!", Toast.LENGTH_SHORT).show();
                                    saveUser(secret, nickname);
                                    goToHome(secret, nickname);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(MainActivity.this, "Failed to add nickname", Toast.LENGTH_SHORT).show();
                                });
                    }
                } else {
                    // Secret not found -> create new secret with this nickname
                    Map<String, Object> nicknameMap = new HashMap<>();
                    nicknameMap.put("name", nickname);

                    Map<String, Object> secretMap = new HashMap<>();
                    secretMap.put("date", ""); // empty date initially
                    secretMap.put(nickname, nicknameMap);

                    databaseRef.child(secret).setValue(secretMap)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(MainActivity.this, "Account Created!", Toast.LENGTH_SHORT).show();
                                saveUser(secret, nickname);
                                goToHome(secret, nickname);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(MainActivity.this, "Failed to create account", Toast.LENGTH_SHORT).show();
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "DB Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ Save login locally
    private void saveUser(String secret, String nickname) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("secret", secret);
        editor.putString("nickname", nickname);
        editor.apply();
    }

    private void goToHome(String secret, String nickname) {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        intent.putExtra("secret", secret);
        intent.putExtra("nickname", nickname);
        startActivity(intent);
        finish();
    }
}
