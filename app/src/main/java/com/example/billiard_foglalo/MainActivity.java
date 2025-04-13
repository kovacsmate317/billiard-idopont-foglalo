package com.example.billiard_foglalo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getName();
    private EditText emailTextInput, passwordTextInput;
    private FirebaseAuth auth;

    ImageView logo;
    TextInputLayout emailLayout, passwordLayout;
    Button loginButton, registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailTextInput = findViewById(R.id.emailTextInput);
        passwordTextInput = findViewById(R.id.passwordTextInput);

        logo = findViewById(R.id.logo);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        loginButton.setOnClickListener(this::loginWithEmail);

        auth = FirebaseAuth.getInstance();

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        new android.os.Handler().postDelayed(() -> emailLayout.startAnimation(fadeIn), 100);
        new android.os.Handler().postDelayed(() -> passwordLayout.startAnimation(fadeIn), 200);
        new android.os.Handler().postDelayed(() -> loginButton.startAnimation(fadeIn), 300);
        new android.os.Handler().postDelayed(() -> registerButton.startAnimation(fadeIn), 400);
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

    public void redirectToRegister(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    private void startBookingActivity(){
        Intent intent = new Intent(MainActivity.this, BookingActivity.class);
        startActivity(intent);
    }
}