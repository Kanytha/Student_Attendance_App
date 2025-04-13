package com.example.studentattendanceapp;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentattendanceapp.Adapter.CourseAdapter;
import com.example.studentattendanceapp.Model.Courses;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StudentDashBoardActivity extends AppCompatActivity {
    FrameLayout banner;
    private RecyclerView recyclerView;
    private CourseAdapter courseAdapter;
    private List<Courses> courseList;
    private FirebaseFirestore db;
    LinearLayout logoutButton;


    CardView qrScannerCard, attendanceCard;
    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_dash_board);

        recyclerView = findViewById(R.id.recentCoursesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        courseList = new ArrayList<>();
        courseAdapter = new CourseAdapter(this, courseList, null);
        recyclerView.setAdapter(courseAdapter);
        logoutButton = findViewById(R.id.logoutButton);


        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadStudentCourses();

        // Banner animation
        banner = findViewById(R.id.banner);
        AnimationDrawable animatedGradient = (AnimationDrawable) banner.getBackground();
        animatedGradient.setEnterFadeDuration(5000);
        animatedGradient.setExitFadeDuration(6000);
        animatedGradient.start();

        initializeViews();
        setupClickListeners();

    }

    private void initializeViews() {
        qrScannerCard = findViewById(R.id.qrScannerCard);
        attendanceCard = findViewById(R.id.attendanceCard);
    }

    private void setupClickListeners() {
        qrScannerCard.setOnClickListener(v -> {
            Intent intent = new Intent(StudentDashBoardActivity.this, QRScannerActivity.class);
            startActivity(intent);
        });

        attendanceCard.setOnClickListener(v -> {
            Intent intent = new Intent(StudentDashBoardActivity.this, AttendanceHistoryActivity.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(StudentDashBoardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadStudentCourses() {
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Log.d("Debug", "Student doc found: " + documentSnapshot.getData());

                    List<String> assignedCourses = (List<String>) documentSnapshot.get("assignedCourses");

                    if (assignedCourses != null && !assignedCourses.isEmpty()) {
                        Log.d("Debug", "Assigned courses: " + assignedCourses.toString());
                        fetchCourseDetails(assignedCourses);
                    } else {
                        Toast.makeText(this, "No assigned courses found", Toast.LENGTH_SHORT).show();
                        Log.d("Debug", "assignedCourses is null or empty");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                    Log.e("Debug", "Error fetching user doc", e);
                });
    }


    private void fetchCourseDetails(List<String> assignedCourses) {
        db.collection("courses")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    courseList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("courseName");
                        Log.d("Debug", "Checking course: " + name);

                        if (assignedCourses.contains(name)) {
                            Log.d("Debug", "Matched course: " + name);
                            Date date = doc.getDate("courseDate");
                            String startTime = doc.getString("startTime");
                            String endTime = doc.getString("endTime");

                            if (name != null && date != null && startTime != null && endTime != null) {
                                Courses course = new Courses(name, date, startTime, endTime);
                                course.setId(doc.getId());
                                courseList.add(course);
                            }
                        }
                    }
                    courseAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading course details", Toast.LENGTH_SHORT).show();
                    Log.e("Debug", "Failed to load courses", e);
                });
    }


}
