package com.example.studentattendanceapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.studentattendanceapp.Adapter.AttendanceAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OverallAttendanceReportActivity extends AppCompatActivity {

    private RecyclerView overallAttendanceRecyclerView;
    private AttendanceAdapter attendanceAdapter;
    private List<AttendanceRecord> attendanceList;
    private TextView noDataTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overall_attendance_report);

        // Initialize UI
        overallAttendanceRecyclerView = findViewById(R.id.overallAttendanceRecyclerView);
        noDataTextView = findViewById(R.id.noDataTextView);

        overallAttendanceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        attendanceList = new ArrayList<>();
        attendanceAdapter = new AttendanceAdapter(this, attendanceList);
        overallAttendanceRecyclerView.setAdapter(attendanceAdapter);

        fetchOverallAttendanceRecords();
    }

    private void fetchOverallAttendanceRecords() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("attendances")
                .document("all")
                .collection("records")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    attendanceList.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        noDataTextView.setVisibility(View.VISIBLE);
                        overallAttendanceRecyclerView.setVisibility(View.GONE);
                    } else {
                        // Group by courseId
                        Map<String, List<AttendanceRecord>> groupedByCourse = new LinkedHashMap<>();

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String classId = document.getString("classId");
                            String status = document.getString("attendanceStatus");
                            String date = document.getString("date");
                            String studentEmail = document.getString("studentEmail");

                            if (classId != null && status != null && date != null && studentEmail != null) {
                                AttendanceRecord record = new AttendanceRecord(
                                        date,
                                        status,
                                        studentEmail
                                );

                                // Group by course
                                if (!groupedByCourse.containsKey(classId)) {
                                    groupedByCourse.put(classId, new ArrayList<>());
                                }
                                groupedByCourse.get(classId).add(record);
                            }
                        }

                        // For each courseId, fetch the course name and flatten
                        for (String courseId : groupedByCourse.keySet()) {
                            // Fetch the course name from the "courses" collection
                            db.collection("courses")
                                    .document(courseId)
                                    .get()
                                    .addOnSuccessListener(courseDocument -> {
                                        String courseName = courseDocument.getString("courseName");
                                        if (courseName != null) {
                                            // Add course name as a header
                                            attendanceList.add(new AttendanceRecord("", "", "Course: " + courseName));

                                            // Add the attendance records
                                            attendanceList.addAll(groupedByCourse.get(courseId));

                                            // Notify the adapter
                                            attendanceAdapter.notifyDataSetChanged();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("ATT_FETCH", "Error fetching course name", e);
                                    });
                        }

                        noDataTextView.setVisibility(View.GONE);
                        overallAttendanceRecyclerView.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ATT_FETCH", "Error fetching documents", e);
                    noDataTextView.setVisibility(View.VISIBLE);
                    noDataTextView.setText("Failed to load attendance data.");
                    overallAttendanceRecyclerView.setVisibility(View.GONE);
                });
    }



}


