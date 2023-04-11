package com.example.securefileaccess.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securefileaccess.R;
import com.example.securefileaccess.activity.AddUserActivity;
import com.example.securefileaccess.activity.UserActivity;
import com.example.securefileaccess.helper.Utility;
import com.example.securefileaccess.model.Employee;
import com.example.securefileaccess.webservice.JSONParse;
import com.example.securefileaccess.webservice.RestAPI;
import com.example.securefileaccess.webservice.WebUtility;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

import java.util.List;

public class ViewUserAdapter extends RecyclerView.Adapter<ViewUserAdapter.ViewHolder> {

    Context context;
    List<Employee> userList;
    RelativeLayout llMainUserActivity;

    RestAPI restAPI;
    ProgressDialog progressDialog;
    JSONParse jsonParse;
    Utility utility;

    public ViewUserAdapter(Context context, List<Employee> userList, RelativeLayout llMainUserActivity) {
        this.context = context;
        this.userList = userList;
        this.llMainUserActivity = llMainUserActivity;
        initObj();
    }

    private void initObj() {
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
        View detailItem = inflater.inflate(R.layout.list_user, parent, false);
        ViewHolder detailHolder = new ViewHolder(detailItem);
        return detailHolder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (userList.size() != 0) {
            final Employee user = userList.get(position);
            holder.tvUserName.setText("User Name :-  " + user.getName());
            holder.tvEmail.setText("Email id :-  " + user.getEmail());
            holder.tvContact.setText("Contact Number :-  " + user.getContact());
            holder.tvDepartment.setText("Department :-  " + user.getDepartment());
            holder.iv_edit.setOnClickListener(v -> onClickTxtView(user));
            holder.iv_delete.setOnClickListener(v -> onClickTxtDelete(user.getEmployeeId()));
        }
    }

    private void onClickTxtDelete(Integer employeeId) {
        new AsyncDeleteUser(employeeId).execute();
    }

    private void onClickTxtView(Employee user) {
        Intent intent = new Intent(context, AddUserActivity.class);
        intent.putExtra("Employee", user);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvUserName, tvEmail, tvContact, tvDepartment;
        private View itemView;
        ImageView iv_delete,iv_edit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvContact = itemView.findViewById(R.id.tvContact);
            tvDepartment = itemView.findViewById(R.id.tvDepartment);
            iv_delete = itemView.findViewById(R.id.iv_delete);
            iv_edit = itemView.findViewById(R.id.iv_edit);
        }
    }

    private class AsyncDeleteUser extends AsyncTask<String, String, String> {

        private int userId;

        public AsyncDeleteUser(int userId) {
            this.userId = userId;

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
                JSONObject json = restAPI.Deleteuser(String.valueOf(userId));
                a = jsonParse.Parse(json);
            } catch (Exception e) {
                a = e.getMessage();
            }
            return a;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("ViewUserDeleteResponse", s);
            progressDialog.cancel();
            if (WebUtility.checkConnection(s)) {
                Pair<String, String> pair = WebUtility.GetErrorMessage(s);
                WebUtility.ShowAlertDialog(context, pair.first, pair.second, false);
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String statusValue = jsonObject.getString("status");

                    if (statusValue.compareTo("true") == 0) {
                        Snackbar.make(llMainUserActivity, "Deleted Success", Snackbar.LENGTH_LONG).show();
                        new Handler().postDelayed(() -> {
                            ((Activity) context).finish();
                        }, 500);
                    } else {
                        Snackbar.make(llMainUserActivity, "Something Went Wrong", Snackbar.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
