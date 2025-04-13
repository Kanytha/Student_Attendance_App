package com.example.studentattendanceapp.Model;

import java.util.List;

public class User {
    private String email;
    private String role;
    private List<String> assignedCourses;
    private String userId;

    public User(String email, String role, String userId, List<String> assignedCourses) {
        this.email = email;
        this.role = role;
        this.userId = userId;
        this.assignedCourses = assignedCourses;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<String> getAssignedCourses() {
        return assignedCourses;
    }

    public void setAssignedCourses(List<String> assignedCourses) {
        this.assignedCourses = assignedCourses;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
