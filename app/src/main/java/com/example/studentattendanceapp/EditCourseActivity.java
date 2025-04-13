package com.example.studentattendanceapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EditCourseActivity extends AppCompatActivity {

    private EditText editCourseName;
    private Button btnSelectDate;
    private Button btnUpdateCourse;
    private Button btnCancel;

    private FirebaseFirestore db;
    private String courseId;
    private Date selectedDate;
    private Calendar calendar;
    private SimpleDateFormat dateFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_course);

        // Set window insets for edge-to-edge support
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components
        editCourseName = findViewById(R.id.editCourseName);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnUpdateCourse = findViewById(R.id.btnUpdateCourse);
        btnCancel = findViewById(R.id.btnCancel);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize date formatter and calendar
        dateFormatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US);
        calendar = Calendar.getInstance();

        // Get course details from intent
        courseId = getIntent().getStringExtra("courseId");
        String courseName = getIntent().getStringExtra("courseName");
        String courseDateString = getIntent().getStringExtra("courseDate");

        // Set the course name in the EditText
        editCourseName.setText(courseName);

        // Parse the date
        try {
            if (courseDateString != null) {
                selectedDate = parseCourseDate(courseDateString);
                if (selectedDate != null) {
                    calendar.setTime(selectedDate);
                    updateDateButtonText();
                }
            }
        } catch (ParseException e) {
            Log.e("EditCourseActivity", "Error parsing date: " + courseDateString, e);
            selectedDate = new Date(); // Default to current date if parsing fails
            calendar.setTime(selectedDate);
            updateDateButtonText();
        }

        // Set up button listeners
        setUpButtonListeners();
    }

    private Date parseCourseDate(String courseDateString) throws ParseException {
        return dateFormatter.parse(courseDateString);
    }

    private void setUpButtonListeners() {
        // Date Picker Listener
        btnSelectDate.setOnClickListener(v -> showDatePickerDialog());

        // Update Course Listener
        btnUpdateCourse.setOnClickListener(v -> updateCourse());

        // Cancel Listener
        btnCancel.setOnClickListener(v -> finish());
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    selectedDate = calendar.getTime();
                    updateDateButtonText();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateButtonText() {
        SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        btnSelectDate.setText(displayFormat.format(calendar.getTime()));
    }

    private void updateCourse() {
        String courseName = editCourseName.getText().toString().trim();

        if (courseName.isEmpty()) {
            Toast.makeText(this, "Please enter a course name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDate == null) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        // Format the date to Cambodia's date format
        String formattedDate = formatDateForCambodia(selectedDate);

        Map<String, Object> courseData = new HashMap<>();
        courseData.put("courseName", courseName);
        courseData.put("classStartTime", formattedDate);

        // Update the course in Firestore
        db.collection("courses").document(courseId)
                .update(courseData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Course updated successfully!", Toast.LENGTH_SHORT).show();

                    // After updating the course, update all users who have this course
                    updateUsersAssignedCourses(courseId, courseName);

                    // Update the attendance records as well
                    updateAttendanceRecords(courseId, courseName);

                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update course", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUsersAssignedCourses(String courseId, String newCourseName) {
        // Fetch all users who have this course assigned
        db.collection("users")
                .whereArrayContains("assignedCourses", courseId) // Find users with this course ID
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String userId = doc.getId();
                        List<String> assignedCourses = (List<String>) doc.get("assignedCourses");
                        List<String> updatedCourses = new ArrayList<>();

                        for (String course : assignedCourses) {
                            if (course.equals(courseId)) {
                                updatedCourses.add(newCourseName); // Replace old course ID with the new name
                            } else {
                                updatedCourses.add(course);
                            }
                        }

                        // Update the user's courses in the Firestore document
                        db.collection("users").document(userId)
                                .update("assignedCourses", updatedCourses)
                                .addOnSuccessListener(aVoid -> {
                                    // Optionally show a toast after the user's courses have been updated
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("EditCourseActivity", "Error updating user's courses: " + e.getMessage());
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("EditCourseActivity", "Error fetching users: " + e.getMessage());
                });
    }

    private void updateAttendanceRecords(String oldCourseId, String newCourseName) {
        db.collection("attendance")
                .whereEqualTo("classId", oldCourseId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String attendanceId = doc.getId();

                        db.collection("attendance").document(attendanceId)
                                .update("classId", newCourseName)
                                .addOnSuccessListener(aVoid -> {
                                    // Successfully updated the attendance record
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("EditCourseActivity", "Error updating attendance record: " + e.getMessage());
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("EditCourseActivity", "Error fetching attendance records: " + e.getMessage());
                });
    }

    private String formatDateForCambodia(Date date) {
        Locale cambodiaLocale = new Locale("km", "KH");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", cambodiaLocale);
        return dateFormat.format(date);
    }
}
