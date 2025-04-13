package com.example.studentattendanceapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize Views
        LinearLayout manageUsersButton = findViewById(R.id.manageUsersButton);
        LinearLayout manageCoursesButton = findViewById(R.id.manageCoursesButton);
        LinearLayout viewReportsButton = findViewById(R.id.viewOverallReportButton);
        LinearLayout logoutButton = findViewById(R.id.logoutButton);
        TextView welcomeText = findViewById(R.id.welcomeText);

        // Set welcome message
        welcomeText.setText("Welcome, Admin");

        // Manage Users Button
        manageUsersButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageUsersActivity.class);
            startActivity(intent);
        });

        // Manage Courses Button
        manageCoursesButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageCoursesActivity.class);
            startActivity(intent);
        });

        // View Reports Button
        viewReportsButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, OverallAttendanceReportActivity.class);
            startActivity(intent);
        });


        // Logout Button
        logoutButton.setOnClickListener(v -> {
            // Sign out from Firebase
            FirebaseAuth.getInstance().signOut();

            // Show logout message
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

            // Return to login screen
            Intent intent = new Intent(AdminDashboardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        // Prevent going back to login screen
        moveTaskToBack(true);
    }
}
