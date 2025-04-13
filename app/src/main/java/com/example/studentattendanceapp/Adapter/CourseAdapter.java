package com.example.studentattendanceapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentattendanceapp.Model.Courses;
import com.example.studentattendanceapp.R;

import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private Context context;
    private List<Courses> courseList;
    private OnCourseClickListener listener;

    public interface OnCourseClickListener {
        void onEditClick(Courses course, int position);
        void onDeleteClick(Courses course, int position);
    }

    public CourseAdapter(Context context, List<Courses> courseList, OnCourseClickListener listener) {
        this.context = context;
        this.courseList = courseList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Courses course = courseList.get(position);

        holder.tvCourseName.setText(course.getCourseName());
        holder.tvCourseDate.setText(course.getFormattedCourseDate()); // Display formatted date

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(course, holder.getAdapterPosition());
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(course, holder.getAdapterPosition());
            }
        });
    }


    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public void updateData(List<Courses> courses) {
        this.courseList = courses;
        notifyDataSetChanged();
    }

    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourseName, tvCourseDate,tvTeacherId;
        ImageButton btnEdit, btnDelete;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseName = itemView.findViewById(R.id.courseName);
            tvCourseDate = itemView.findViewById(R.id.courseSchedule);
//            tvTeacherId = itemView.findViewById(R.id.teacherId);// Assuming courseName2 is where you display the date
            btnEdit = itemView.findViewById(R.id.editButton);
            btnDelete = itemView.findViewById(R.id.deleteButton);
        }
    }
}
