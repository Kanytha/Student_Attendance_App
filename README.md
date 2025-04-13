Student Attendance Mobile App
## Table of Contents
1. [Project Overview](#project-overview)
2. [Features](#features)
- [For Students](#for-students)
- [For Teachers](#for-teachers)
- [For Admins](#for-admins)
3. [Technologies Used](#technologies-used)
4. [Setup Instructions](#setup-instructions)
5. [Firebase Configuration](#firebase-configuration)
6. [How to Run the App](#how-to-run-the-app)
---
## Project Overview
The **Student Attendance Mobile App** is designed to streamline the process of logging and
managing student attendance in educational institutions. The app leverages QR code scanning
technology to allow students to mark their attendance quickly and efficiently. Teachers can
generate QR codes for attendance and view detailed reports on student participation.
Additionally, admins can manage users and generate overall attendance reports.
This project was developed as part of the **Final Project** for the **BSc(Hons) Software
Engineering with Multimedia** program at Limkokwing University of Creative Technology.
---
## Features
### For Students
- **QR Code Scanning**: Students can use their phones to scan QR codes provided by teachers
to mark their attendance.
- **Attendance History**: Students can view their attendance records over time, including
statuses such as **Present**, **Absent**, and **Late**.
### For Teachers
- **QR Code Generation**: Teachers can generate QR codes for students to scan during class
sessions.
- **Attendance Reports**: Teachers can view detailed attendance reports for their students and
generate summaries (e.g., total present, absent, late counts).
### For Admins
- **User Management**: Admins can add, edit, or delete users (students and teachers).
- **Overall Reports**: Admins can generate overall attendance reports across all students.
---
## Technologies Used
- **Programming Language**: Java
- **Database**: Firebase Firestore
- **Authentication**: Firebase Authentication
- **QR Code Handling**: ZXing Library (`com.journeyapps:zxing-android-embedded`)
- **Version Control**: Git and GitHub
- **Development Environment**: Android Studio
---
## Setup Instructions
### Prerequisites
Before running the app, ensure you have the following installed:
1. **Android Studio**: Latest version (https://developer.android.com/studio)
2. **Java Development Kit (JDK)**: Version 11 or higher
3. **Firebase Account**: A Firebase project set up with Authentication and Firestore enabled.
---
## Firebase Configuration
1. **Create a Firebase Project**:
- Go to the [Firebase Console](https://console.firebase.google.com/).
- Create a new project and enable **Authentication** and **Firestore Database**.
2. **Enable Email/Password Authentication**:
- Navigate to **Authentication > Sign-in method**.
- Enable **Email/Password** as a sign-in provider.
3. **Set Up Firestore Rules**:
Use the following rules during development:
```json
rules_version = '2';
service cloud.firestore {
match /databases/{database}/documents {
match /{document=**} {
allow read, write: if true; // Allow all access (only for testing)
}
}
}
```
4. **Download `google-services.json`**:
- Download the `google-services.json` file from Firebase and place it in the `app/` directory of
your project.
5. **Add Dependencies**:
Ensure the following dependencies are added to your `build.gradle` file:
```gradle
implementation platform('com.google.firebase:firebase-bom:33.11.0')
implementation 'com.google.firebase:firebase-auth'
implementation 'com.google.firebase:firebase-firestore'
implementation 'com.journeyapps:zxing-android-embedded:4.3.0'
implementation 'com.google.zxing:core:3.5.1'
```
---
## How to Run the App
1. **Clone the Repository**:
```bash
git clone https://github.com/Kanytha/Student_Attendance_App.git
cd student-attendance-app
```
2. **Open in Android Studio**:
- Open the project in Android Studio.
- Ensure Gradle syncs successfully.
3. **Run the App**:
- Connect an Android device or start an emulator.
- Click the **Run** button in Android Studio to install and launch the app.
4. **Test Features**:
- Register or log in as a student, teacher, or admin.
- Test QR code generation, scanning, and attendance history.
