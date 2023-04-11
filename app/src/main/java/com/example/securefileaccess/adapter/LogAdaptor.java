package com.example.securefileaccess.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securefileaccess.R;
import com.example.securefileaccess.model.Logs;

import java.util.ArrayList;

public class LogAdaptor extends RecyclerView.Adapter<LogAdaptor.ViewHolder>{

    private final Context context;
    private ArrayList<Logs> logs = null;

    public LogAdaptor(ArrayList<Logs> logs, Context context) {
        this.logs = logs;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View item = layoutInflater.inflate(R.layout.logs_list_content, parent, false);
        ViewHolder viewHolder = new ViewHolder(item);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.txtEid.setText("Eid: " + logs.get(position).getEmployeeId());
        holder.txtType.setText("Type: " + logs.get(position).getType());
        holder.txtStatus.setText("Status: " + logs.get(position).getStatus());
        holder.txtDT.setText("Date & Time: " + logs.get(position).getDateTime());
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtEid, txtType,txtStatus,txtDT;

        public ViewHolder(View view) {
            super(view);
            this.txtEid = view.findViewById(R.id.txtEid);
            this.txtType = view.findViewById(R.id.txtType);
            this.txtStatus = view.findViewById(R.id.txtStatus);
            this.txtDT = view.findViewById(R.id.txtDT);
        }
    }

}
