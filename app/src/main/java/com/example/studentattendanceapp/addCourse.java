package com.example.studentattendanceapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studentattendanceapp.Model.Courses;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;

public class addCourse extends AppCompatActivity {

    private EditText editCourseName, editStartTime, editEndTime;
    private DatePicker datePicker;
    private Button btnSaveCourse;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        // Initialize UI elements
        editCourseName = findViewById(R.id.editCourseName);
        editStartTime = findViewById(R.id.editStartTime);  // EditText for start time
        editEndTime = findViewById(R.id.editEndTime);      // EditText for end time
        datePicker = findViewById(R.id.datePicker);
        btnSaveCourse = findViewById(R.id.btnSaveCourse);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Set up button click listener to save the course
        btnSaveCourse.setOnClickListener(v -> saveCourse());
    }

    private void saveCourse() {
        // Get course name, date, start time, and end time from user input
        String courseName = editCourseName.getText().toString();
        String startTime = editStartTime.getText().toString(); // Assuming format is "HH:mm"
        String endTime = editEndTime.getText().toString();     // Assuming format is "HH:mm"
        int year = datePicker.getYear();
        int month = datePicker.getMonth();
        int dayOfMonth = datePicker.getDayOfMonth();

        // Convert DatePicker to Date object
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);
        Date courseDate = calendar.getTime();

        // Validate user input
        if (!courseName.isEmpty() && !startTime.isEmpty() && !endTime.isEmpty() && courseDate != null) {
            // Create a new course object
            Courses newCourse = new Courses(courseName, courseDate, startTime, endTime);

            // Add course ID to the object
            String courseId = db.collection("courses").document().getId();
            newCourse.setId(courseId);

            // Save course to Firestore
            db.collection("courses")
                    .document(courseId)
                    .set(newCourse)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(addCourse.this, "Course added successfully", Toast.LENGTH_SHORT).show();
                        finish(); // Close this activity after saving
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(addCourse.this, "Error adding course", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(addCourse.this, "Please enter valid course details", Toast.LENGTH_SHORT).show();
        }
    }
}
