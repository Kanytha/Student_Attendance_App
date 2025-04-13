package com.example.studentattendanceapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentattendanceapp.Adapter.AttendanceAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AttendanceHistoryActivity extends AppCompatActivity {

    private RecyclerView attendanceRecyclerView;
    private AttendanceAdapter attendanceAdapter;
    private List<AttendanceRecord> attendanceList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_history);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize RecyclerView
        attendanceRecyclerView = findViewById(R.id.attendanceRecyclerView);
        attendanceRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Prepare the list and adapter
        attendanceList = new ArrayList<>();
        attendanceAdapter = new AttendanceAdapter(this, attendanceList);
        attendanceRecyclerView.setAdapter(attendanceAdapter);

        // Fetch attendance data for this student
        fetchAttendanceData();
    }

    private void fetchAttendanceData() {
        String userId = auth.getCurrentUser().getUid();  // Get the logged-in student's UID

        db.collection("attendances")
                .document("users")
                .collection(userId)
                .document("self_attendance")
                .collection("records")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        attendanceList.clear();

                        // Loop through each attendance record
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String classId = document.getString("classId");
                            String date = document.getString("date");
                            String status = document.getString("attendanceStatus");

                            if (classId != null && date != null && status != null) {
                                // Fetch course name using classId
                                fetchCourseName(classId, date, status);
                            }
                        }

                    } else {
                        Toast.makeText(AttendanceHistoryActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                        Log.e("AttendanceFetch", "Error: ", task.getException());
                    }
                });
    }

    private void fetchCourseName(String classId, String date, String status) {
        db.collection("courses")
                .document(classId)
                .get()
                .addOnCompleteListener(courseTask -> {
                    if (courseTask.isSuccessful()) {
                        String courseName = courseTask.getResult().getString("courseName");

                        if (courseName != null) {
                            // Add record with course name to the list
                            attendanceList.add(new AttendanceRecord(date, status, courseName));
                            Log.d("AttendanceData", "Record: " + date + " - " + status + " - " + courseName);

                            // Notify adapter that the data has been updated
                            attendanceAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.e("CourseFetch", "Error fetching course name", courseTask.getException());
                    }
                });
    }

}
