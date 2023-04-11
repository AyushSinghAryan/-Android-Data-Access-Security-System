package com.example.securefileaccess.adapter;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securefileaccess.R;
import com.example.securefileaccess.activity.AddUserActivity;
import com.example.securefileaccess.helper.Authentication;
import com.example.securefileaccess.helper.UserPref;
import com.example.securefileaccess.helper.Utility;
import com.example.securefileaccess.model.Employee;
import com.example.securefileaccess.model.FilesMaster;
import com.example.securefileaccess.webservice.JSONParse;
import com.example.securefileaccess.webservice.RestAPI;
import com.example.securefileaccess.webservice.WebUtility;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ShareAccessUserAdapter extends RecyclerView.Adapter<ShareAccessUserAdapter.ViewHolder> {

    Context context;
    int fileId;
    RelativeLayout rlMainViewFilesDetails;

    List<Employee> userList;
    ArrayList<String> employeeIdsList;
    ArrayList<String> previousGrantedAccessEmployeeListFromDB;

    RestAPI restAPI;
    ProgressDialog progressDialog;
    JSONParse jsonParse;
    Utility utility;


    public ShareAccessUserAdapter(Context context, int fileId, List<Employee> userList,
                                  RelativeLayout rlMainViewFilesDetails) {
        this.context = context;
        this.fileId = fileId;
        this.userList = userList;
        this.rlMainViewFilesDetails = rlMainViewFilesDetails;

        initObj();
    }

    private void initObj() {
        restAPI = new RestAPI();
        jsonParse = new JSONParse();
        utility = new Utility();
        progressDialog = new ProgressDialog(context, R.style.progressDialog);
        employeeIdsList = new ArrayList<>();
        previousGrantedAccessEmployeeListFromDB = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View detailItem = inflater.inflate(R.layout.list_file_users, parent, false);
        return new ShareAccessUserAdapter.ViewHolder(detailItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (userList.size() != 0) {
            final Employee user = userList.get(position);
            new AsyncCheckAccess(user, holder).execute();
        }
    }

    private void bindDataToHolder(ViewHolder holder, Employee user) {
        holder.cbAccess.setChecked(previousGrantedAccessEmployeeListFromDB.contains(
                String.valueOf(user.getEmployeeId())));
        holder.tvUserName.setText(user.getName());
        holder.cbAccess.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                employeeIdsList.add(String.
                        valueOf(user.getEmployeeId()));
            } else {
                if (employeeIdsList.contains(String.valueOf(user.getEmployeeId()))) {
                    employeeIdsList.remove(String.valueOf(user.getEmployeeId()));
                }
            }
            new AsyncUpdateAccess().execute();
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvUserName;
        private CheckBox cbAccess;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            cbAccess = itemView.findViewById(R.id.cbAccess);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncCheckAccess extends AsyncTask<String, String, String> {

        Employee user;
        ViewHolder holder;

        public AsyncCheckAccess(Employee user, ViewHolder holder) {
            this.user = user;
            this.holder = holder;
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
                JSONObject json;
                json = restAPI.checkAccess(String.valueOf(fileId), String.valueOf(user.getEmployeeId()));
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
                    previousGrantedAccessEmployeeListFromDB.clear();
                    if (jsonValue.compareTo("true") == 0) {
                        previousGrantedAccessEmployeeListFromDB.add(String.valueOf(user.getEmployeeId()));
                    }

                    bindDataToHolder(holder, user);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


    }


    private class AsyncUpdateAccess extends AsyncTask<String, String, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            utility.showProgressDialog(progressDialog);

        }

        @Override
        protected String doInBackground(String... strings) {
            String a;
            try {
                JSONObject json = restAPI.updateAccess(String.valueOf(fileId),
                        employeeIdsList);
                a = jsonParse.Parse(json);
            } catch (Exception e) {
                a = e.getMessage();
            }
            return a;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("CheckedList", s);
            progressDialog.cancel();
            if (WebUtility.checkConnection(s)) {
                Pair<String, String> pair = WebUtility.GetErrorMessage(s);
                WebUtility.ShowAlertDialog(context, pair.first, pair.second, false);
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String statusValue = jsonObject.getString("status");

                    if (statusValue.compareTo("true") == 0) {
                        Snackbar.make(rlMainViewFilesDetails, "List Updated", Snackbar.LENGTH_LONG).show();
                    } else {
                        Snackbar.make(rlMainViewFilesDetails, "Something Went Wrong", Snackbar.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
