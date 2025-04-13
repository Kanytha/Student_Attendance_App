package com.example.studentattendanceapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.studentattendanceapp.Adapter.AttendanceAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AttendanceReportActivity extends AppCompatActivity {

    private RecyclerView attendanceRecyclerView;
    private AttendanceAdapter attendanceAdapter;
    private List<AttendanceRecord> attendanceList;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ProgressBar loadingSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_report);

        // Initialize Firestore & Auth
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Setup RecyclerView
        attendanceRecyclerView = findViewById(R.id.attendanceRecyclerView);
        attendanceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        attendanceList = new ArrayList<>();
        attendanceAdapter = new AttendanceAdapter(this, attendanceList);
        attendanceRecyclerView.setAdapter(attendanceAdapter);

        // Loading spinner
        loadingSpinner = findViewById(R.id.loadingSpinner);

        // Start fetching data
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            fetchAssignedCoursesAndAttendance();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }

    }

    private void showLoading(boolean isLoading) {
        loadingSpinner.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void fetchAssignedCoursesAndAttendance() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String teacherId = currentUser.getUid();
        Log.d("DEBUG_USER", "Fetching courses for teacherId: " + teacherId);

        db.collection("users").document(teacherId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        List<String> assignedCourses = (List<String>) doc.get("assignedCourses");
                        Log.d("DEBUG_COURSES", "Assigned courses: " + assignedCourses);
                        if (assignedCourses != null && !assignedCourses.isEmpty()) {
                            showLoading(true);
                            for (String courseName : assignedCourses) {
                                fetchClassAttendance(courseName);
                            }
                        } else {
                            Toast.makeText(this, "No assigned courses found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                    Log.e("FetchAssignedCourses", "Error: ", e);
                });
    }

    private void fetchClassAttendance(String courseName) {
        Log.d("DEBUG_COURSE_FETCH", "Fetching attendance for course: " + courseName);

        db.collection("courses")
                .whereEqualTo("courseName", courseName)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    if (!querySnapshots.isEmpty()) {
                        DocumentSnapshot courseDoc = querySnapshots.getDocuments().get(0);
                        String courseId = courseDoc.getId();  // Use courseId directly

                        // Log courseId to verify
                        Log.d("DEBUG_COURSE_FETCH", "Found course: " + courseName + " | ID: " + courseId);

                        // Fetch attendance records using classId
                        db.collection("attendances")
                                .document("classes")
                                .collection(courseId)  // Now use courseId directly
                                .document("class_attendance")
                                .collection("records")
                                .get()
                                .addOnSuccessListener(attendanceSnapshots -> {
                                    Log.d("DEBUG_ATTENDANCE", "Found " + attendanceSnapshots.size() + " attendance records.");
                                    for (QueryDocumentSnapshot doc : attendanceSnapshots) {
                                        String date = doc.getString("date");
                                        String status = doc.getString("attendanceStatus");
                                        String studentEmail = doc.getString("studentEmail");

                                        // Log the data to verify
                                        Log.d("DEBUG_ATTENDANCE_RECORD", "Date: " + date + " | Status: " + status + " | Email: " + studentEmail);

                                        if (date != null && status != null && studentEmail != null) {
                                            // Extract the student name by cutting the email at '@'
                                            String studentName = studentEmail != null ? studentEmail.split("@")[0] : "Unknown";

                                            // Create an AttendanceRecord with the student's name and status
                                            AttendanceRecord record = new AttendanceRecord(date, status, studentName);
                                            attendanceList.add(record);
                                        }
                                    }
                                    attendanceAdapter.updateAttendanceList(new ArrayList<>(attendanceList));
                                    Log.d("DEBUG_UI", "Adapter item count: " + attendanceList.size());

                                    showLoading(false);
                                })
                                .addOnFailureListener(e -> {
                                    showLoading(false);
                                    Toast.makeText(this, "Error fetching attendance", Toast.LENGTH_SHORT).show();
                                    Log.e("FetchAttendanceRecords", "Error: ", e);
                                });
                    } else {
                        Log.d("DEBUG_COURSE", "Course not found: " + courseName);
                        showLoading(false);
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Error fetching course data", Toast.LENGTH_SHORT).show();
                    Log.e("FetchCourseDocument", "Error: ", e);
                });
    }

}
