package com.example.studentattendanceapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentattendanceapp.Adapter.CourseAdapter;
import com.example.studentattendanceapp.Model.Courses;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ManageCoursesActivity extends AppCompatActivity implements CourseAdapter.OnCourseClickListener {

    private RecyclerView recyclerView;
    private CourseAdapter courseAdapter;
    private List<Courses> courseList;
    private FirebaseFirestore db;
    private Button btnAddCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_courses);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        btnAddCourse = findViewById(R.id.addCourseButton);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        courseList = new ArrayList<>();
        courseAdapter = new CourseAdapter(this, courseList, this);
        recyclerView.setAdapter(courseAdapter);

        // Init Firebase
        db = FirebaseFirestore.getInstance();

        // Load courses from Firebase
        loadCoursesFromFirestore();

        btnAddCourse.setOnClickListener(v -> {
            Intent intent = new Intent(ManageCoursesActivity.this, addCourse.class);
            startActivity(intent);
        });
    }

    private void loadCoursesFromFirestore() {
        db.collection("courses")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    courseList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("courseName");
                        Date date = doc.getDate("courseDate");
                        String startTime = doc.getString("startTime"); // Fetch startTime
                        String endTime = doc.getString("endTime");     // Fetch endTime

                        // Create Courses object with the new attributes
                        if (name != null && date != null && startTime != null && endTime != null) {
                            Courses course = new Courses(name, date, startTime, endTime);
                            course.setId(doc.getId()); // this line is important!
                            courseList.add(course);
                        }

                        Log.d("FirestoreDebug", "Course: " + name + " Date: " + date + " StartTime: " + startTime + " EndTime: " + endTime);
                    }
                    courseAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading courses", Toast.LENGTH_SHORT).show();
                });
    }

    protected void onResume() {
        super.onResume();
        loadCoursesFromFirestore(); // this refreshes your list!
    }

    @Override
    public void onEditClick(Courses course, int position) {
        // Open the EditCourseActivity with the course details
        Intent intent = new Intent(ManageCoursesActivity.this, EditCourseActivity.class);
        intent.putExtra("courseId", course.getId());          // Pass the course ID for editing
        intent.putExtra("courseName", course.getCourseName()); // Pass the course name
        intent.putExtra("courseDate", course.getCourseDate().toString()); // Pass the course date
        intent.putExtra("startTime", course.getStartTime());   // Pass start time
        intent.putExtra("endTime", course.getEndTime());       // Pass end time
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Courses course, int position) {
        // Show a confirmation dialog before deleting
        new AlertDialog.Builder(ManageCoursesActivity.this)
                .setTitle("Delete Course")
                .setMessage("Are you sure you want to delete this course?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Delete from Firebase
                    deleteCourse(course.getId(), position);
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss()) // Dismiss dialog
                .show();
    }

    private void deleteCourse(String courseId, int position) {
        db.collection("courses").document(courseId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Remove the course from the list and update RecyclerView
                    courseList.remove(position);
                    courseAdapter.notifyItemRemoved(position);
                    Toast.makeText(this, "Course deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error deleting course", Toast.LENGTH_SHORT).show();
                });
    }
    private void updateAssignedCourseNames(String oldName, String newName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        List<String> assignedCourses = (List<String>) doc.get("assignedCourses");

                        if (assignedCourses != null && assignedCourses.contains(oldName)) {
                            List<String> updatedCourses = new ArrayList<>(assignedCourses);
                            int index = updatedCourses.indexOf(oldName);
                            updatedCourses.set(index, newName);

                            db.collection("users").document(doc.getId())
                                    .update("assignedCourses", updatedCourses)
                                    .addOnSuccessListener(unused -> Log.d("CourseUpdate", "Updated course for user: " + doc.getId()))
                                    .addOnFailureListener(e -> Log.e("CourseUpdate", "Failed to update user: " + doc.getId(), e));
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("CourseUpdate", "Failed to fetch users", e));
    }

}
