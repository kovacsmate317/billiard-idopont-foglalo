package com.example.billiard_foglalo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText username, password;
    private Button loginBTN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = findViewById(R.id.edittext1);
        password = findViewById(R.id.edittext2);
        loginBTN = findViewById(R.id.loginBTN);

        loginBTN.setOnClickListener(view -> login(view));
    }

    public void login(View view) {
        String user = username.getText().toString().trim();
        String pass = password.getText().toString().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
        } else {
            // Handle login logic here
            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
        }
    }
}