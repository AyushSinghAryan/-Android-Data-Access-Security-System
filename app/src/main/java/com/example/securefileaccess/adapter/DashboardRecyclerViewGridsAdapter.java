package com.example.securefileaccess.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securefileaccess.R;
import com.example.securefileaccess.activity.UserActivity;
import com.example.securefileaccess.activity.ViewFilesByDirectory;
import com.example.securefileaccess.model.Directory;

import java.util.List;

public class DashboardRecyclerViewGridsAdapter extends RecyclerView.Adapter<DashboardRecyclerViewGridsAdapter.DashboardGridView> {

    Context context;
    private final List<Directory> dashboardDirectoryGridsList;

    public DashboardRecyclerViewGridsAdapter(Context context, List<Directory> dashboardDirectoryGridsList) {
        this.context = context;
        this.dashboardDirectoryGridsList = dashboardDirectoryGridsList;
    }

    @NonNull
    @Override
    public DashboardGridView onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dashboard_grids_view, parent, false);
        return new DashboardGridView(view);
    }

    @Override
    public void onBindViewHolder(DashboardGridView holder, int position) {

        Directory directory = dashboardDirectoryGridsList.get(position);
        Log.i("DashboardGridAdapter ", directory.getName());
        holder.cardView.setRadius(20);
        holder.cardView.setOnClickListener(v -> onClickCardView(directory));
        setGridTextAndImage(holder, directory);
    }

    @Override
    public int getItemCount() {
        return dashboardDirectoryGridsList.size();
    }

    private void onClickCardView(Directory directory) {

        Intent intent = new Intent(context, ViewFilesByDirectory.class);
        if (directory.getName() != null) {
            intent.putExtra("directory", directory.getName());
        }
        context.startActivity(intent);
    }

    private String getExtensionFromFileName(Directory directory) {
        String extension = null;
        String fileName = directory.getName();
        int index = fileName.lastIndexOf('.');
        if (index > 0) {
            extension = fileName.substring(index + 1);
            System.out.println(fileName + "\t" + extension);
        }
        return extension;
    }

    @SuppressLint("SetTextI18n")
    private void setGridTextAndImage(DashboardGridView holder, Directory gridName) {
        holder.tvGridName.setText(gridName.getName());
    }

    public static class DashboardGridView extends RecyclerView.ViewHolder {

        private final CardView cardView;
        private final TextView tvGridName;
        private final ImageView ivGridImage;

        public DashboardGridView(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardGridItem);
            tvGridName = itemView.findViewById(R.id.tvGridName);
            ivGridImage = itemView.findViewById(R.id.ivGridImage);

        }
    }
}
