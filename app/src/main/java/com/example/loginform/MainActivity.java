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

        // Login button → check credentials
        loginButton.setOnClickListener(v -> checkLogin());

        // Google button → open browser
        googleButton.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com"));
            startActivity(browserIntent);
        });

        // No account? → Go to Signup screen
        noAccountText.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, signup.class);
            startActivity(intent);
        });

        // Forgot password → show alert
        forgetPasswordText.setOnClickListener(v -> showDialog(
                "Password Help",
                "Forgot your password?\nPlease contact the administrator or IT support for assistance."
        ));
    }

    // ✅ Check user login from Firestore
    private void checkLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

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

                            // Clear fields
                            emailEditText.setText("");
                            passwordEditText.setText("");

                            // 🔹 Navigate to MainActivity2 after successful login
                            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                            startActivity(intent);
                            finish(); // close login screen
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

    // Simple alert dialog helper
    private void showDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}
