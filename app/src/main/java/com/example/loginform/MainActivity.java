package com.example.loginform;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MainActivity extends AppCompatActivity {

    EditText emailEditText, passwordEditText;
    Button loginButton, googleButton;
    ProgressBar progressBar;
    TextView noAccountText, forgetPasswordText;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        // Find views
        emailEditText = findViewById(R.id.editTextTextEmailAddress);
        passwordEditText = findViewById(R.id.editTextTextPassword);
        loginButton = findViewById(R.id.button);
        googleButton = findViewById(R.id.button2);
        progressBar = findViewById(R.id.progressBar);
        noAccountText = findViewById(R.id.textView);
        forgetPasswordText = findViewById(R.id.textView2);

        progressBar.setVisibility(View.GONE);

        // Login button
        loginButton.setOnClickListener(v -> checkLogin());

        // Google link
        googleButton.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com"));
            startActivity(browserIntent);
        });

        // No account action
        noAccountText.setOnClickListener(v -> showDialog("Account Help",
                "You currently don’t have an account.\nPlease contact your administrator to create one."));

        // Forgot password action
        forgetPasswordText.setOnClickListener(v -> showDialog("Password Help",
                "Forgot your password?\nPlease contact the administrator or IT support for assistance."));
    }

    private void checkLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Search user in Firestore
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        boolean matched = false;
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String storedPassword = doc.getString("password");
                            if (storedPassword != null && storedPassword.equals(password)) {
                                matched = true;
                                break;
                            }
                        }

                        if (matched) {
                            Toast.makeText(this, "✅ Login Successful!", Toast.LENGTH_LONG).show();
                            emailEditText.setText("");
                            passwordEditText.setText("");
                        } else {
                            Toast.makeText(this, "❌ Incorrect Password", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(this, "❌ No account found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}
