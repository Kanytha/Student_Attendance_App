package com.example.studentattendanceapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class QRScannerActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private static final int GRACE_PERIOD_MINUTES = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        new IntentIntegrator(this).initiateScan(); // Start QR scanner
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            Log.d("QR_SCAN", "Scanned QR content: " + result.getContents());
            handleScannedData(result.getContents());
        } else {
            Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
            Log.w("QR_SCAN", "Scan was cancelled or no data returned.");
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleScannedData(@NonNull String qrContent) {
        String[] parts = qrContent.split(",");

        if (parts.length != 4) {
            Toast.makeText(this, "Invalid QR code", Toast.LENGTH_SHORT).show();
            Log.e("QR_PARSE", "QR code does not have exactly 4 parts: " + qrContent);
            return;
        }

        String classId = parts[0].trim();
        String statusFromQR = parts[1].trim();
        String date = parts[2].trim();
        String dateTimeFromQR = parts[3].trim();

        Log.d("QR_PARSED", "classId: " + classId + ", statusFromQR: " + statusFromQR + ", date: " + date + ", dateTimeFromQR: " + dateTimeFromQR);

        try {
            Date qrTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(dateTimeFromQR);
            Date nowTime = new Date();
            long diffMillis = nowTime.getTime() - qrTime.getTime();
            long diffMinutes = diffMillis / (60 * 1000);

            FirebaseUser student = mAuth.getCurrentUser();
            if (student == null) {
                Toast.makeText(this, "You are not logged in", Toast.LENGTH_SHORT).show();
                Log.e("FIREBASE_AUTH", "Current user is null");
                return;
            }

            String studentId = student.getUid();
            String studentEmail = student.getEmail();
            Log.d("USER_INFO", "studentId: " + studentId + ", studentEmail: " + studentEmail);

            db.collection("attendances")
                    .document("users")
                    .collection(studentId)
                    .document("self_attendance")
                    .collection("records")
                    .whereEqualTo("classId", classId)
                    .whereEqualTo("date", date)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            Toast.makeText(this, "Already marked attendance for this class today.", Toast.LENGTH_LONG).show();
                            Log.w("ATTENDANCE_DUPLICATE", "Attendance already exists for classId: " + classId + ", date: " + date);
                            finish();
                            return;
                        }

                        db.collection("courses").document(classId).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (!documentSnapshot.exists()) {
                                        Toast.makeText(this, "Class not found: " + classId, Toast.LENGTH_SHORT).show();
                                        Log.e("CLASS_NOT_FOUND", "No such class in Firestore. classId = " + classId);
                                        return;
                                    }

                                    Log.d("CLASS_FOUND", "Class found: " + documentSnapshot.getData());

                                    String endTimeString = documentSnapshot.getString("endTime");
                                    if (endTimeString == null) {
                                        Toast.makeText(this, "Class end time not set", Toast.LENGTH_SHORT).show();
                                        Log.e("CLASS_TIME", "End time is null for classId: " + classId);
                                        return;
                                    }

                                    try {
                                        Date classEndTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(endTimeString);
                                        if (nowTime.after(classEndTime)) {
                                            Toast.makeText(this, "QR expired. Class already ended.", Toast.LENGTH_LONG).show();
                                            Log.w("QR_EXPIRED", "Now: " + nowTime + ", Class End: " + classEndTime);
                                            finish();
                                            return;
                                        }

                                        String attendanceStatus = statusFromQR.equals("Absent")
                                                ? "Absent"
                                                : (diffMinutes <= GRACE_PERIOD_MINUTES ? "Present" : "Late");

                                        HashMap<String, Object> attendanceData = new HashMap<>();
                                        attendanceData.put("classId", classId);
                                        attendanceData.put("status", statusFromQR);
                                        attendanceData.put("date", date);
                                        attendanceData.put("timestamp", Timestamp.now());
                                        attendanceData.put("attendanceStatus", attendanceStatus);
                                        attendanceData.put("studentId", studentId);
                                        attendanceData.put("studentEmail", studentEmail);

                                        Log.d("ATTENDANCE_RECORD", "Attendance Data: " + attendanceData.toString());

                                        // Save to: Global
                                        db.collection("attendances")
                                                .document("all")
                                                .collection("records")
                                                .add(attendanceData);

                                        // Save to: Student-specific
                                        db.collection("attendances")
                                                .document("users")
                                                .collection(studentId)
                                                .document("self_attendance")
                                                .collection("records")
                                                .add(attendanceData);

                                        // Save to: Class-specific
                                        db.collection("attendances")
                                                .document("classes")
                                                .collection(classId)
                                                .document("class_attendance")
                                                .collection("records")
                                                .add(attendanceData)
                                                .addOnSuccessListener(docRef -> {
                                                    Toast.makeText(this, "Attendance marked: " + attendanceStatus, Toast.LENGTH_LONG).show();
                                                    Log.i("ATTENDANCE_SUCCESS", "Marked as: " + attendanceStatus);
                                                    finish();
                                                });

                                    } catch (ParseException e) {
                                        Toast.makeText(this, "Error parsing class end time", Toast.LENGTH_SHORT).show();
                                        Log.e("PARSE_ERROR", "End time parse error: " + endTimeString, e);
                                    }

                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to fetch class info", Toast.LENGTH_SHORT).show();
                                    Log.e("FIREBASE_FETCH_FAIL", "Failed to fetch class document", e);
                                });

                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error checking existing attendance", Toast.LENGTH_SHORT).show();
                        Log.e("FIREBASE_QUERY_FAIL", "Error checking existing attendance", e);
                    });

        } catch (ParseException e) {
            Toast.makeText(this, "Date parse error", Toast.LENGTH_SHORT).show();
            Log.e("PARSE_ERROR", "QR dateTime parse failed: " + dateTimeFromQR, e);
        }
    }
}
