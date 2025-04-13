package com.example.studentattendanceapp.Model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Courses {
    private String courseName;
    private Date courseDate;         // Start date of the course
    private String id;
    private String startTime;        // e.g. "10:00"
    private String endTime;          // e.g. "12:00"
    private int numberOfWeeks;       // Total weeks the course runs

    public Courses() {
    }

    public Courses(String courseName, Date courseDate, String startTime, String endTime) {
        this.courseName = courseName;
        this.courseDate = courseDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.numberOfWeeks = 12; // default if not set explicitly
    }

    // Getters and Setters

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Date getCourseDate() {
        return courseDate;
    }

    public void setCourseDate(Date courseDate) {
        this.courseDate = courseDate;
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getNumberOfWeeks() {
        return numberOfWeeks;
    }

    public void setNumberOfWeeks(int numberOfWeeks) {
        this.numberOfWeeks = numberOfWeeks;
    }

    public String getFormattedCourseDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        return dateFormat.format(courseDate);
    }

    // Check if today is one of the weekly scheduled days
    public boolean isTodayScheduledClass() {
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(courseDate);

        Calendar todayCal = Calendar.getInstance();

        long diffInMillis = todayCal.getTimeInMillis() - startCal.getTimeInMillis();
        long daysDiff = diffInMillis / (1000 * 60 * 60 * 24);

        boolean isSameDayOfWeek = (todayCal.get(Calendar.DAY_OF_WEEK) == startCal.get(Calendar.DAY_OF_WEEK));
        boolean isWithinWeeks = (daysDiff >= 0) && (daysDiff / 7 < numberOfWeeks);

        return isSameDayOfWeek && isWithinWeeks;
    }
}
