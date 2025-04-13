package com.example.studentattendanceapp;

public class AttendanceRecord {
    private String date;
    private String status;
    private String classId;  // Adding classId field for class identification

    // Default constructor required for Firestore
    public AttendanceRecord() {
        // Firestore requires a default constructor
    }

    // Constructor with parameters
    public AttendanceRecord(String date, String status, String classId) {
        this.date = date;
        this.status = status;
        this.classId = classId;  // Initialize the classId
    }

    // Getters and Setters

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }
}
