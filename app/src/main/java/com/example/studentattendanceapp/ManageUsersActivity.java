package com.example.studentattendanceapp;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;


public class ManageUsersActivity extends AppCompatActivity {

    private TextInputEditText emailInput, passwordInput;
    private Spinner roleSpinner;
    private LinearLayout courseSelectionLayout, studentCourseSelectionLayout;
    private LinearLayout courseSpinnersContainer, studentCourseSpinnersContainer;
    private MaterialButton addCourseButton, addStudentCourseButton, addUserButton;
    private RecyclerView userRecyclerView;

    private ArrayList<String> userList = new ArrayList<>();

    private int maxStudentCourses = 5;
    private int currentStudentSpinners = 0;
    private boolean teacherSpinnerAdded = false;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CollectionReference usersRef, coursesRef;

    private ArrayList<String> courseList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        // Firebase initialization
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users"); // Firestore collection for users
        coursesRef = db.collection("courses"); // Firestore collection for courses

        // Bind Views
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordCreation);
        roleSpinner = findViewById(R.id.roleSpinner);

        courseSelectionLayout = findViewById(R.id.courseSelectionLayout);
        studentCourseSelectionLayout = findViewById(R.id.studentCourseSelectionLayout);
        courseSpinnersContainer = findViewById(R.id.courseSpinnersContainer);
        studentCourseSpinnersContainer = findViewById(R.id.studentCourseSpinnersContainer);

        addCourseButton = findViewById(R.id.addCourseButton);
        addStudentCourseButton = findViewById(R.id.addStudentCourseButton);
        addUserButton = findViewById(R.id.addUserButton);



        // Setup Role Spinner
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Select Role", "admin", "student", "teacher"});
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);

        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String role = parent.getItemAtPosition(position).toString();
                courseSelectionLayout.setVisibility(View.GONE);
                studentCourseSelectionLayout.setVisibility(View.GONE);
                courseSpinnersContainer.removeAllViews();
                studentCourseSpinnersContainer.removeAllViews();
                teacherSpinnerAdded = false;
                currentStudentSpinners = 0;

                if (role.equals("teacher")) {
                    courseSelectionLayout.setVisibility(View.VISIBLE);
                    addTeacherCourseSpinner(); // Auto add one
                } else if (role.equals("student")) {
                    studentCourseSelectionLayout.setVisibility(View.VISIBLE);
                    addStudentCourseSpinner(); // Auto add one
                }
            }

            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        // Button: Add Course for Teacher
        addCourseButton.setOnClickListener(v -> {
            if (!teacherSpinnerAdded) {
                addTeacherCourseSpinner();
            } else {
                Toast.makeText(this, "Teacher can have only 1 course", Toast.LENGTH_SHORT).show();
            }
        });

        // Button: Add Course for Student
        addStudentCourseButton.setOnClickListener(v -> {
            if (currentStudentSpinners < maxStudentCourses) {
                addStudentCourseSpinner();
            } else {
                Toast.makeText(this, "Maximum 5 courses allowed for student", Toast.LENGTH_SHORT).show();
            }
        });

        // Button: Add User
        // Button: Add User
        addUserButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();
            String role = roleSpinner.getSelectedItem().toString();

            if (email.isEmpty() || password.isEmpty() || role.equals("Select Role")) {
                Toast.makeText(this, "Please fill in email, password, and role", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            String userId = firebaseUser != null ? firebaseUser.getUid() : "";

                            // Create user data to store in Firestore
                            java.util.Map<String, Object> userData = new java.util.HashMap<>();
                            userData.put("email", email);
                            userData.put("userType", role);

                            if (role.equals("teacher")) {
                                if (courseSpinnersContainer.getChildCount() > 0) {
                                    Spinner spinner = (Spinner) courseSpinnersContainer.getChildAt(0);
                                    String selectedCourse = spinner.getSelectedItem().toString();
                                    userData.put("assignedCourses", java.util.Collections.singletonList(selectedCourse));
                                }
                            } else if (role.equals("student")) {
                                ArrayList<String> selectedCourses = new ArrayList<>();
                                for (int i = 0; i < studentCourseSpinnersContainer.getChildCount(); i++) {
                                    Spinner spinner = (Spinner) studentCourseSpinnersContainer.getChildAt(i);
                                    String selectedCourse = spinner.getSelectedItem().toString();
                                    selectedCourses.add(selectedCourse);
                                }
                                userData.put("assignedCourses", selectedCourses);
                            }

                            // Store in "users" collection with UID as document ID
                            usersRef.document(userId).set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "User added", Toast.LENGTH_SHORT).show();
                                        clearForm();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Error saving user to Firestore", Toast.LENGTH_SHORT).show();
                                    });

                        } else {
                            Toast.makeText(this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });


        fetchCourses();


    }
    private void fetchCourses() {
        coursesRef.get().addOnSuccessListener(querySnapshot -> {
            courseList.clear();
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                String courseName = doc.getString("courseName");
                if (courseName != null) courseList.add(courseName);
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Failed to load courses", Toast.LENGTH_SHORT).show()
        );
    }







    private void addTeacherCourseSpinner() {
        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courseList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        courseSpinnersContainer.addView(spinner);
        teacherSpinnerAdded = true;
    }

    private void addStudentCourseSpinner() {
        if (currentStudentSpinners < maxStudentCourses) {
            Spinner spinner = new Spinner(this);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courseList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            studentCourseSpinnersContainer.addView(spinner);
            currentStudentSpinners++;
        }
    }


    private void clearForm() {
        emailInput.setText("");
        passwordInput.setText("");
        roleSpinner.setSelection(0);
        courseSpinnersContainer.removeAllViews();
        studentCourseSpinnersContainer.removeAllViews();
        teacherSpinnerAdded = false;
        currentStudentSpinners = 0;
    }



}
