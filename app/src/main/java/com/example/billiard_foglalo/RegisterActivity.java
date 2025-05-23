package com.example.billiard_foglalo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    private static final String LOG_TAG = RegisterActivity.class.getName();
    private EditText fullNameTextInput;
    private EditText emailTextInput;
    private EditText phoneTextInput;
    private EditText passwordTextInput;
    private EditText confirmPasswordTextInput;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        fullNameTextInput = findViewById(R.id.fullNameTextInput);
        emailTextInput = findViewById(R.id.emailTextInput);
        phoneTextInput = findViewById(R.id.phoneTextInput);
        passwordTextInput = findViewById(R.id.passwordTextInput);
        confirmPasswordTextInput = findViewById(R.id.confirmPasswordTextInput);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public void registerUser(View view) {
        String fullName = fullNameTextInput.getText().toString().trim();
        String email = emailTextInput.getText().toString().trim();
        String phone = phoneTextInput.getText().toString().trim();
        String password = passwordTextInput.getText().toString();
        String confirmPassword = confirmPasswordTextInput.getText().toString();

        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Minden mezőt ki kell tölteni!", Toast.LENGTH_LONG).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(RegisterActivity.this, "A jelszavak nem egyeznek", Toast.LENGTH_LONG).show();
            return;
        }

        if (password.length() < 8) {
            Toast.makeText(RegisterActivity.this, "A jelszónak legalább 8 karakter hosszúnak kell lennie", Toast.LENGTH_LONG).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(LOG_TAG, "Felhasználó sikeresen regisztrálva");

                    String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();

                    Map<String, Object> userData = new HashMap<>();
                    userData.put("fullName", fullName);
                    userData.put("email", email);
                    userData.put("phone", phone);
                    userData.put("reservations", new java.util.ArrayList<String>());

                    db.collection("users").document(userId)
                            .set(userData)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(RegisterActivity.this, "Sikeres regisztráció!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(RegisterActivity.this, BookingActivity.class);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(LOG_TAG, "Felhasználó adatainak mentése sikertelen", e);
                                Toast.makeText(RegisterActivity.this, "Sikertelen adatmentés: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });

                } else {
                    try {
                        throw Objects.requireNonNull(task.getException());
                    } catch (FirebaseAuthUserCollisionException e) {
                        Toast.makeText(RegisterActivity.this, "Ez az email már regisztrálva van.", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(RegisterActivity.this, "Sikertelen regisztráció: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    Log.d(LOG_TAG, "Regisztráció sikertelen: " + task.getException());
                }
            }
        });
    }

    public void redirectToLogin(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
