package com.example.billiard_foglalo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getName();
    private EditText emailTextInput, passwordTextInput;
    private Button loginBTN;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailTextInput = findViewById(R.id.emailTextInput);
        passwordTextInput = findViewById(R.id.passwordTextInput);
        loginBTN = findViewById(R.id.loginButton);

        loginBTN.setOnClickListener(this::loginWithEmail);

        auth = FirebaseAuth.getInstance();
    }

    public void loginWithEmail(View view) {
        String email = emailTextInput.getText().toString().trim();
        String pass = passwordTextInput.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
        } else {
            auth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Log.d(LOG_TAG, "Sikeres bejelentkezés");
                        startBookingActivity();
                    }else{
                        Log.d(LOG_TAG, "Siketelen bejelentkezés");
                    }
                }
            });
        }
    }

    public void loginWithGoogle(View view){

    }

    public void loginAsGuest(View view){
        auth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d(LOG_TAG, "Sikeres bejelentkezés vendég");
                    startBookingActivity();
                }else{
                    Log.d(LOG_TAG, "Sikertelen bejelentkezés vendég");
                }
            }
        });
    }

    public void redirectToRegister(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    private void startBookingActivity(){
        Intent intent = new Intent(MainActivity.this, BookingActivity.class);
        startActivity(intent);
    }
}