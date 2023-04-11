package com.example.securefileaccess.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.securefileaccess.R;
import com.example.securefileaccess.helper.Authentication;
import com.example.securefileaccess.helper.Constants;
import com.example.securefileaccess.helper.Utility;
import com.example.securefileaccess.model.Employee;
import com.example.securefileaccess.webservice.JSONParse;
import com.example.securefileaccess.webservice.RestAPI;
import com.example.securefileaccess.webservice.WebUtility;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.concurrent.Callable;

public class AddUserActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvTitle;
    private RelativeLayout rlMainAddUser;
    private EditText etUserName, etEmail, etContact, etDepartment, etPassword;
    private Button btnRegisterUser;

    private Employee entity;
    private Serializable serializableBundle;

    private RestAPI restAPI;
    private ProgressDialog progressDialog;
    private JSONParse jsonParse;
    private Utility utility;

    Authentication authentication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        initToolBar();
        initUI();
        loadIntentData();
        initOBJ();
    }

    private void initToolBar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvTitle = toolbar.findViewById(R.id.tvTitle);
        tvTitle.setText(Constants.ADD_USER);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(Constants.ADD_USER);
    }

    private Serializable loadIntentData() {
        Serializable bundle = getIntent().getSerializableExtra("Employee");
        if (bundle != null) {
            entity = (Employee) bundle;
            setEntityDataToText();
        }
        return bundle;
    }

    private void setEntityDataToText() {
        etUserName.setText(entity.getName());
        etEmail.setText(entity.getEmail());
        etContact.setText(entity.getContact());
        etDepartment.setText(entity.getDepartment());
        etPassword.setText(entity.getPassword());

        etPassword.setVisibility(View.GONE);
        tvTitle.setText(Constants.UPDATE_USER);
        getSupportActionBar().setTitle(Constants.UPDATE_USER);
        btnRegisterUser.setText("Update User");
    }

    private void initOBJ() {

        authentication = new Authentication(this);
        entity = new Employee();
        serializableBundle = loadIntentData();
        restAPI = new RestAPI();
        jsonParse = new JSONParse();
        utility = new Utility();
        progressDialog = new ProgressDialog(AddUserActivity.this, R.style.progressDialog);
    }

    private void initUI() {
        rlMainAddUser = findViewById(R.id.rlMainAddUser);
        etUserName = findViewById(R.id.etUserName);
        etEmail = findViewById(R.id.etEmail);
        etContact = findViewById(R.id.etContact);
        etDepartment = findViewById(R.id.etDepartment);
        etPassword = findViewById(R.id.etPassword);
        btnRegisterUser = findViewById(R.id.btnRegisterUser);

        etPassword.setVisibility(View.VISIBLE);
        btnRegisterUser.setOnClickListener(v -> onClickRegisterUser());
    }

    private void onClickRegisterUser() {
        if (isValidate()) {
            String name = etUserName.getText().toString();
            String email = etEmail.getText().toString();
            String contact = etContact.getText().toString();
            String department = etDepartment.getText().toString();
            String password = etPassword.getText().toString();

            try {
                if (serializableBundle == null) {
                    new AsyncSaveUser(name, email, contact, department, password).execute();
                } else {
                    new AsyncUpdateUser(entity.getEmployeeId(), name, email, contact, department).execute();
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    private boolean isValidate() {
        String required = "required";
        String error = "";

        if (etUserName.getText().toString().equals("")) {
            error = required;
            Snackbar.make(rlMainAddUser, "Name Required", Snackbar.LENGTH_LONG).show();
            etUserName.setText(required);
        } else if (etEmail.getText().toString().equals("")) {
            Snackbar.make(rlMainAddUser, "Email Required", Snackbar.LENGTH_LONG).show();
            etEmail.setText(required);
            error = required;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(etEmail.getText().toString()).matches()) {
            Snackbar.make(rlMainAddUser, "Invalid Email Id", Snackbar.LENGTH_LONG).show();
            return false;
        } else if (etContact.getText().toString().equals("")) {
            Snackbar.make(rlMainAddUser, "Contact  Number Required", Snackbar.LENGTH_LONG).show();
            etContact.setText(required);
            error = required;
        } else if (!Patterns.PHONE.matcher(etContact.getText().toString()).matches()) {
            Snackbar.make(rlMainAddUser, "Invalid Contact Number", Snackbar.LENGTH_LONG).show();
            return false;
        } else if (etContact.getText().toString().length() < 10) {
            Snackbar.make(rlMainAddUser, "Invalid Contact Number", Snackbar.LENGTH_LONG).show();
            return false;
        } else if (etDepartment.getText().toString().equals("")) {
            Snackbar.make(rlMainAddUser, "Department Required", Snackbar.LENGTH_LONG).show();
            etDepartment.setText(required);
            error = required;
        } else if (serializableBundle == null) {
            if (etPassword.getText().toString().trim().equals("")) {
                Snackbar.make(rlMainAddUser, "Password Required", Snackbar.LENGTH_LONG).show();
                etPassword.setText(required);
                error = required;
            }
        } else if (serializableBundle != null) {
            if (entity.getEmployeeId() == null) {
                Snackbar.make(rlMainAddUser, "Employee Not Found", Snackbar.LENGTH_LONG).show();
                error = required;
            }
        }
        return error.equals("");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private class AsyncSaveUser extends AsyncTask<String, String, String> {

        private String name, email, contact, department, password;

        public AsyncSaveUser(String name, String email, String contact, String department, String password) {
            this.name = name;
            this.email = email;
            this.contact = contact;
            this.department = department;
            this.password = password;
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
                JSONObject json = restAPI.Adduser(authentication.EncyptMesg(name),
                        authentication.EncyptMesg(email), authentication.EncyptMesg(contact),
                        authentication.EncyptMesg(department), authentication.EncyptMesg(password));
                a = jsonParse.Parse(json);
            } catch (Exception e) {
                a = e.getMessage();
            }
            return a;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("AddUserResponse", s);
            progressDialog.cancel();
            if (WebUtility.checkConnection(s)) {
                Pair<String, String> pair = WebUtility.GetErrorMessage(s);
                WebUtility.ShowAlertDialog(AddUserActivity.this, pair.first, pair.second, false);
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String statusValue = jsonObject.getString("status");

                    if (statusValue.compareTo("true") == 0) {
                        Snackbar.make(rlMainAddUser, "Save Success", Snackbar.LENGTH_LONG).show();
                        finish();
                    } else {
                        Snackbar.make(rlMainAddUser, "Something Went Wrong", Snackbar.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class AsyncUpdateUser extends AsyncTask<String, String, String> {

        private int userId;
        private String name, email, contact, department;

        public AsyncUpdateUser(int userId, String name, String email, String contact, String department) {
            this.userId = userId;
            this.name = name;
            this.email = email;
            this.contact = contact;
            this.department = department;
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
                JSONObject json = restAPI.Updateuser(String.valueOf(userId),
                        authentication.EncyptMesg(name), authentication.EncyptMesg(email),
                        authentication.EncyptMesg(contact), authentication.EncyptMesg(department)
                );
                a = jsonParse.Parse(json);
            } catch (Exception e) {
                a = e.getMessage();
            }
            return a;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("AddUserResponse", s);
            progressDialog.cancel();
            if (WebUtility.checkConnection(s)) {
                Pair<String, String> pair = WebUtility.GetErrorMessage(s);
                WebUtility.ShowAlertDialog(AddUserActivity.this, pair.first, pair.second, false);
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String statusValue = jsonObject.getString("status");

                    if (statusValue.compareTo("true") == 0) {
                        Snackbar.make(rlMainAddUser, "Update Success", Snackbar.LENGTH_LONG).show();
                        finish();
                    } else {
                        Snackbar.make(rlMainAddUser, "Something Went Wrong", Snackbar.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}