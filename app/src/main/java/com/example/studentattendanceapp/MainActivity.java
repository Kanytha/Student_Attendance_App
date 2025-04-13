package com.example.studentattendanceapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    // Declare UI elements
    private EditText emailInput, passwordInput;
    private Button loginButton;
    ConstraintLayout layout;


    // Firebase Authentication and Firestore
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);

        // Set an OnClickListener for the login button
        loginButton.setOnClickListener(v -> {
            // Get the entered email and password
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            // Perform basic validation
            if (email.isEmpty() || password.isEmpty()) {
                // Show an error message if fields are empty
                Toast.makeText(MainActivity.this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            } else if (!isValidEmail(email)) {
                // Validate email format
                Toast.makeText(MainActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            } else {
                // Call handleLogin to authenticate with Firebase
                handleLogin(email, password);
            }
        });
        layout = findViewById(R.id.main_layout);
        AnimationDrawable animatedGradient = (AnimationDrawable) layout.getBackground();
        animatedGradient.setEnterFadeDuration(5000);
        animatedGradient.setExitFadeDuration(6000);
        animatedGradient.start();
    }

    // Helper method to validate email format
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Method to handle Firebase Authentication and userType-based redirection
    private void handleLogin(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login successful, get the user's userType from Firestore
                        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

                        db.collection("users").document(userId).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        String userType = documentSnapshot.getString("userType");

                                        if (userType == null || userType.isEmpty()) {
                                            Toast.makeText(MainActivity.this, "User type not found", Toast.LENGTH_SHORT).show();
                                            Log.d("Firestore", "User Data: " + documentSnapshot.getData());
                                            return;
                                        }

                                        // Redirect based on userType
                                        Intent intent;
                                        switch (userType) {
                                            case "student":
                                                intent = new Intent(MainActivity.this, StudentDashBoardActivity.class);
                                                break;
                                            case "teacher":
                                                intent = new Intent(MainActivity.this, TeacherDashboardActivity.class);
                                                break;
                                            case "admin":
                                                intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
                                                break;
                                            default:
                                                Toast.makeText(MainActivity.this, "Unknown user type", Toast.LENGTH_SHORT).show();
                                                return;
                                        }
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(MainActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(MainActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace(); // Log the error for debugging
                                });
                    } else {
                        // Login failed
                        Toast.makeText(MainActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(MainActivity.this, "Authentication failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                        Log.e("LoginError", "Authentication failed: " + errorMessage);
                    }
                });

    }
}