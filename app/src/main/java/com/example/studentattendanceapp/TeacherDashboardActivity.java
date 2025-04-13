package com.example.studentattendanceapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TeacherDashboardActivity extends AppCompatActivity {
    LinearLayout logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        LinearLayout generateQRButton = findViewById(R.id.generateQRButton);
        LinearLayout viewReportButton = findViewById(R.id.viewReportButton);
        logoutButton = findViewById(R.id.logoutButton);


        generateQRButton.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherDashboardActivity.this, GenerateQRActivity.class);
            startActivity(intent);
        });

        viewReportButton.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherDashboardActivity.this, AttendanceReportActivity.class);
            startActivity(intent);
        });
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(TeacherDashboardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        fetchTodayAttendanceSummary();
    }

    private void fetchTodayAttendanceSummary() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) return;

        String teacherId = currentUser.getUid();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        db.collection("users").document(teacherId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                List<String> courses = (List<String>) doc.get("assignedCourses");
                if (courses != null && !courses.isEmpty()) {
                    String classId = courses.get(0); // Assuming one course per teacher

                    db.collection("attendance")
                            .whereEqualTo("className", classId) // Changed classId to className
                            .whereEqualTo("date", today) // Ensure date is formatted as yyyy-MM-dd
                            .get()
                            .addOnSuccessListener(query -> {
                                int present = 0, absent = 0, late = 0;

                                // Debugging: Check what is returned from Firestore
                                for (var docSnap : query.getDocuments()) {
                                    String status = docSnap.getString("status");
                                    String student = docSnap.getString("studentId");
                                    String loggedClass = docSnap.getString("className");
                                    String loggedDate = docSnap.getString("date");

                                    System.out.println("Attendance -> student: " + student + ", class: " + loggedClass + ", date: " + loggedDate + ", status: " + status);

                                    if ("Present".equalsIgnoreCase(status)) present++;
                                    else if ("Late".equalsIgnoreCase(status)) late++;
                                    else if ("Absent".equalsIgnoreCase(status)) absent++;
                                }


                            })
                            .addOnFailureListener(e -> {
                                // Handle any errors in Firestore query
                                e.printStackTrace();
                            });
                }
            }
        });
    }
}
