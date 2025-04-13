package com.example.studentattendanceapp.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.studentattendanceapp.AttendanceRecord;
import com.example.studentattendanceapp.R;

import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {

    private Context context;
    private List<AttendanceRecord> attendanceList;

    public AttendanceAdapter(Context context, List<AttendanceRecord> attendanceList) {
        this.context = context;
        this.attendanceList = attendanceList;
    }

    @Override
    public AttendanceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_attendance_record, parent, false);
        return new AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AttendanceViewHolder holder, int position) {
        AttendanceRecord record = attendanceList.get(position);
        String classId = record.getClassId();

        if (classId != null && classId.startsWith("Course:")) {
            // Show course header
            holder.courseHeaderTextView.setVisibility(View.VISIBLE);
            holder.courseHeaderTextView.setText(classId);

            // Hide normal student info layout
            holder.studentInfoLayout.setVisibility(View.GONE);
        } else {
            // Hide course header
            holder.courseHeaderTextView.setVisibility(View.GONE);

            // Show student info layout
            holder.studentInfoLayout.setVisibility(View.VISIBLE);

            // Extract display name from email
            String displayName = classId != null && classId.contains("@")
                    ? classId.split("@")[0]
                    : classId;

            holder.classIdTextView.setText(displayName);
            holder.dateTextView.setText(record.getDate());
            holder.statusTextView.setText(record.getStatus());
        }
    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    public void updateAttendanceList(List<AttendanceRecord> newAttendanceList) {
        this.attendanceList.clear();
        this.attendanceList.addAll(newAttendanceList);
        notifyDataSetChanged();
        Log.d("DEBUG_ADAPTER", "List size: " + attendanceList.size());

    }

    public static class AttendanceViewHolder extends RecyclerView.ViewHolder {
        TextView courseHeaderTextView;
        TextView classIdTextView, dateTextView, statusTextView;
        View studentInfoLayout;

        public AttendanceViewHolder(View itemView) {
            super(itemView);

            // Initialize the course header view
            courseHeaderTextView = itemView.findViewById(R.id.courseHeaderTextView);

            // Initialize the student info views
            classIdTextView = itemView.findViewById(R.id.classIdTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            studentInfoLayout = itemView.findViewById(R.id.studentInfoLayout);
        }
    }
}
