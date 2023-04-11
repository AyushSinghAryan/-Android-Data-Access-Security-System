package com.example.securefileaccess.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.securefileaccess.R;
import com.example.securefileaccess.adapter.ViewUserAdapter;
import com.example.securefileaccess.helper.Authentication;
import com.example.securefileaccess.helper.Constants;
import com.example.securefileaccess.helper.Utility;
import com.example.securefileaccess.model.Employee;
import com.example.securefileaccess.webservice.JSONParse;
import com.example.securefileaccess.webservice.RestAPI;
import com.example.securefileaccess.webservice.WebUtility;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UserActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RelativeLayout llMainUserActivity;
    private RecyclerView rvViewUsers;
    private FloatingActionButton fabAddUser;

    private Employee entity;
    private List<Employee> userList;
    private ViewUserAdapter viewUserAdapter;

    private RestAPI restAPI;
    private JSONParse jsonParse;
    private Utility utility;

    private ProgressDialog progressDialog;

    private Authentication authentication;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        initToolBar();
        initUI();
        initObj();
        loadList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadList();
    }

    private void initToolBar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView tvTitle = toolbar.findViewById(R.id.tvTitle);
        tvTitle.setText(Constants.VIEW_USER);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void loadList() {
        rvViewUsers.setAdapter(null);
        new AsyncGetUser().execute();
    }

    private void initObj() {
        authentication = new Authentication(this);
        entity = new Employee();
        userList = new ArrayList<>();
        restAPI = new RestAPI();
        jsonParse = new JSONParse();
        utility = new Utility();
        progressDialog = new ProgressDialog(this, R.style.progressDialog);
    }

    private void initUI() {
        llMainUserActivity = findViewById(R.id.llMainUserActivity);
        rvViewUsers = findViewById(R.id.rvViewUsers);
        fabAddUser = findViewById(R.id.fabAddUser);

        fabAddUser.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddUserActivity.class);
            startActivity(intent);
        });

    }

    private void setUpRecyclerView(List<Employee> userList) {
        viewUserAdapter = new ViewUserAdapter(this, userList, llMainUserActivity);
        rvViewUsers.setAdapter(viewUserAdapter);
        rvViewUsers.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                false));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private class AsyncGetUser extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            utility.showProgressDialog(progressDialog);

        }

        @Override
        protected String doInBackground(String... strings) {
            String a;
            try {
                JSONObject json = restAPI.getUser();
                a = jsonParse.Parse(json);
            } catch (Exception e) {
                a = e.getMessage();
            }
            return a;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("GetUserActivityResponse", s);
            progressDialog.cancel();
            if (WebUtility.checkConnection(s)) {
                Pair<String, String> pair = WebUtility.GetErrorMessage(s);
                WebUtility.ShowAlertDialog(UserActivity.this, pair.first, pair.second, false);
            } else {
                try {
                    if (userList.size() > 0) {
                        userList.removeAll(userList);
                    }
                    JSONObject jsonObject = new JSONObject(s);
                    String jsonValue = jsonObject.getString("status");
                    if (jsonValue.compareTo("no") == 0) {
                        Snackbar.make(llMainUserActivity, "No User Found", Snackbar.LENGTH_LONG).show();
                    }

                    if (jsonValue.compareTo("ok") == 0) {
                        JSONArray jsonArray = jsonObject.getJSONArray("Data");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonArrayObject = jsonArray.getJSONObject(i);
                            setDataFromResponseToEntity(jsonArrayObject);
                            userList.add(entity);
                            entity = new Employee();
                        }
                        setUpRecyclerView(userList);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setDataFromResponseToEntity(JSONObject jsonArrayObject) throws JSONException {
        entity.setEmployeeId(jsonArrayObject.getInt("data0"));
        entity.setName(authentication.DecryptMesg(jsonArrayObject.getString("data1")));
        entity.setEmail(authentication.DecryptMesg(jsonArrayObject.getString("data2")));
        entity.setContact(authentication.DecryptMesg(jsonArrayObject.getString("data3")));
        entity.setDepartment(authentication.DecryptMesg(jsonArrayObject.getString("data4")));
        entity.setPassword(authentication.DecryptMesg(jsonArrayObject.getString("data5")));
    }

}