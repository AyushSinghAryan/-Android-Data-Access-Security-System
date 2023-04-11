package com.example.securefileaccess.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securefileaccess.R;
import com.example.securefileaccess.activity.LogsActivity;
import com.example.securefileaccess.activity.ViewFilesByDirectory;
import com.example.securefileaccess.activity.ViewFilesDetails;
import com.example.securefileaccess.helper.Authentication;
import com.example.securefileaccess.helper.Constants;
import com.example.securefileaccess.helper.Utility;
import com.example.securefileaccess.model.FilesMaster;
import com.example.securefileaccess.webservice.DeleteFile;
import com.example.securefileaccess.webservice.JSONParse;
import com.example.securefileaccess.webservice.RestAPI;
import com.example.securefileaccess.webservice.WebUtility;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ViewFilesByDirAdapter extends RecyclerView.Adapter<ViewFilesByDirAdapter.ViewHolder> {

    Context context;
    List<FilesMaster> fileMasterList;
    RelativeLayout rlMainViewFilesByDir;

    Utility utility;
    RestAPI restAPI;
    JSONParse jsonParse;
    Authentication authentication;
    ProgressDialog progressDialog;

    public ViewFilesByDirAdapter(Context context, List<FilesMaster> fileMasterList, RelativeLayout rlMainViewFilesByDir) {
        this.context = context;
        this.fileMasterList = fileMasterList;
        this.rlMainViewFilesByDir = rlMainViewFilesByDir;
        initObj();
    }

    private void initObj() {
        authentication = new Authentication(context);
        restAPI = new RestAPI();
        jsonParse = new JSONParse();
        utility = new Utility();
        progressDialog = new ProgressDialog(context, R.style.progressDialog);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View detailItem = inflater.inflate(R.layout.list_files_view, parent, false);
        return new ViewHolder(detailItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (fileMasterList.size() != 0) {
            final FilesMaster files = fileMasterList.get(position);
            holder.tvFileName.setText(files.getFileName());
            holder.tvFileSize.setText(files.getFileSize());
            holder.llListFilesView.setOnClickListener(v -> onClickCardView(files));
        }
    }

    private void onClickCardView(FilesMaster files) {

        final String[] items = {"View Logs", "View Details", "Delete"};
        Dialog alertDialog = new Dialog(context);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        Dialog finalAlertDialog = alertDialog;
        alertDialogBuilder.setItems(items, (dialog, which) -> {

            if (items[which].equals("View Logs")) {
                finalAlertDialog.dismiss();
                Intent intent = new Intent(context, LogsActivity.class);
                intent.putExtra("FileMaster", files);
                context.startActivity(intent);
            }
            if (items[which].equals("View Details")) {
                finalAlertDialog.dismiss();
                Intent intent = new Intent(context, ViewFilesDetails.class);
                intent.putExtra("FileMasterDetail", files);
                context.startActivity(intent);
            }
            if (items[which].equals("Delete")) {
                new AlertDialog.Builder(context)
                        .setTitle("Title")
                        .setMessage("Do you really want to delete file?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, (dialog1, whichButton) ->
                                new AsyncDeleteFile(files).execute())
                        .setNegativeButton(android.R.string.no, (dialog12, which1) ->
                                dialog12.dismiss()).show();
            }

        });
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();


    }

    @Override
    public int getItemCount() {
        return fileMasterList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout llListFilesView;
        public TextView tvFileName, tvFileSize;
        private View itemView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            llListFilesView = itemView.findViewById(R.id.llListFilesView);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            tvFileSize = itemView.findViewById(R.id.tvFileSize);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncDeleteFile extends AsyncTask<String, String, String> {

        FilesMaster file;

        public AsyncDeleteFile(FilesMaster file) {
            this.file = file;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            utility.showProgressDialog(progressDialog);
        }

        @Override
        protected String doInBackground(String... strings) {
            String a;
            try {
                JSONObject json = null;
                if (file.getFileId() != 0) {
                    json = restAPI.Deletefile(String.valueOf(file.getFileId()));
                }
                a = jsonParse.Parse(json);
            } catch (Exception e) {
                a = e.getMessage();
            }
            return a;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("GetViewFilesResponse", s);
            progressDialog.cancel();
            if (WebUtility.checkConnection(s)) {
                Pair<String, String> pair = WebUtility.GetErrorMessage(s);
                WebUtility.ShowAlertDialog(context, pair.first, pair.second, false);
            } else {
                try {

                    JSONObject jsonObject = new JSONObject(s);
                    String jsonValue = jsonObject.getString("status");
                    if (jsonValue.compareTo("false") == 0) {
                        new DeleteFile(context).execute(Constants.DB_DIR_FILE_UPLOAD + file.getFileName());
                        Snackbar.make(rlMainViewFilesByDir, "No Files Found", Snackbar.LENGTH_LONG).show();
                    }
                    if (jsonValue.compareTo("true") == 0) {
                        Snackbar.make(rlMainViewFilesByDir, "File Deleted", Snackbar.LENGTH_LONG).show();
                        new DeleteFile(context).execute(Constants.DB_DIR_FILE_UPLOAD + file.getFileName());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
