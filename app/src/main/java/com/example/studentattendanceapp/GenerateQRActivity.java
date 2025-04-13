package com.example.studentattendanceapp;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GenerateQRActivity extends AppCompatActivity {

    private ImageView qrCodeImageView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_qractivity);

        qrCodeImageView = findViewById(R.id.qrCodeImageView);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        generateAndDisplayQRCode();
    }

    private void generateAndDisplayQRCode() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String teacherId = currentUser.getUid();

        db.collection("users").document(teacherId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Object courses = documentSnapshot.get("assignedCourses");
                if (courses instanceof List && !((List<?>) courses).isEmpty()) {
                    String courseName = (String) ((List<?>) courses).get(0);

                    db.collection("courses")
                            .whereEqualTo("courseName", courseName)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                if (!querySnapshot.isEmpty()) {
                                    DocumentSnapshot courseSnapshot = querySnapshot.getDocuments().get(0);

                                    Timestamp courseTimestamp = courseSnapshot.getTimestamp("courseDate");
                                    Long durationWeeks = courseSnapshot.getLong("numberOfWeeks");

                                    if (courseTimestamp == null || durationWeeks == null || durationWeeks <= 0) {
                                        Toast.makeText(this, "Invalid course data", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    if (isValidClassToday(courseTimestamp, durationWeeks)) {
                                        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                                        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date());

                                        String qrContent = courseSnapshot.getId() + ",Present," + currentDate + "," + currentDateTime;
                                        generateQRCode(qrContent);
                                    } else {
                                        Toast.makeText(this, "Class not scheduled today", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(this, "Course not found", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e("Firestore", "Error querying course: " + e.getMessage());
                            });

                } else {
                    Toast.makeText(this, "No courses assigned", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidClassToday(Timestamp startDate, long weeksDuration) {
        Calendar classStart = Calendar.getInstance();
        classStart.setTime(startDate.toDate());

        Calendar today = Calendar.getInstance();
        long diffDays = (today.getTimeInMillis() - classStart.getTimeInMillis()) / (1000 * 60 * 60 * 24);

        return diffDays >= 0 && diffDays < weeksDuration * 7;
    }

    private void generateQRCode(String content) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            int size = 512;
            com.google.zxing.common.BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size);
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);

            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? android.graphics.Color.BLACK : android.graphics.Color.WHITE);
                }
            }

            qrCodeImageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Toast.makeText(this, "Failed to generate QR", Toast.LENGTH_SHORT).show();
            Log.e("QRGen", "QR code generation error", e);
        }
    }
}
