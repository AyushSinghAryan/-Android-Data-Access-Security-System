package com.example.securefileaccess.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.securefileaccess.R;
import com.example.securefileaccess.helper.UserPref;
import com.example.securefileaccess.helper.Utility;
import com.example.securefileaccess.webservice.JSONParse;
import com.example.securefileaccess.webservice.RestAPI;

import org.json.JSONArray;
import org.json.JSONObject;


public class LoginActivity extends AppCompatActivity {

    EditText etUserName, etPassword;
    TextView etValidationText;
    Button btnSignIn;

    RestAPI restAPI;
    JSONParse jsonParse;
    Utility utility;

    ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean loginStatus = UserPref.getLoginStatus(this);

        if (loginStatus) {
            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            setContentView(R.layout.activity_login);
            initUI();
            initObj();
        }

    }

    private void initObj() {
        restAPI = new RestAPI();
        jsonParse = new JSONParse();
        utility = new Utility();
        progressDialog = new ProgressDialog(LoginActivity.this, R.style.progressDialog);

    }

    private void initUI() {
        etUserName = findViewById(R.id.etUserName);
        etPassword = findViewById(R.id.etPassword);
        etValidationText = findViewById(R.id.etValidationText);
        btnSignIn = findViewById(R.id.btnSignIn);

        btnSignIn.setOnClickListener(v -> onClickSignIn());
    }

    private void onClickSignIn() {
        if (isValidate()) {
            new login().execute(etUserName.getText().toString(), etPassword.getText().toString());
        }
    }

    @SuppressLint("SetTextI18n")
    private boolean isValidate() {
        if (etUserName.getText().toString().trim().equals("")) {
            etValidationText.setText("UserName empty");
            return false;
        } else if (etPassword.getText().toString().trim().equals("")) {
            etValidationText.setText("Password empty");
            return false;
        } else {
            return true;
        }
    }

    private class login extends AsyncTask<String, JSONObject, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            utility.showProgressDialog(progressDialog);
        }

        @Override
        protected String doInBackground(String... params) {
            String a = "back";

            try {
                JSONObject json = restAPI.Alogin(params[0], params[1]);
                a = jsonParse.Parse(json);
            } catch (Exception e) {
                a = e.getMessage();
            }
            return a;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.cancel();
            Log.d("LoginResponse", s);

            if (s.contains("Unable to resolve host")) {
                AlertDialog.Builder ad = new AlertDialog.Builder(LoginActivity.this);
                ad.setTitle("Unable to Connect!");
                ad.setMessage("Check your Internet Connection,Unable to connect the Server");
                ad.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                ad.show();
            } else {
                try {
                    JSONObject json = new JSONObject(s);
                    String ans = json.getString("status");
                    if (ans.compareTo("false") == 0) {
                        AlertDialog.Builder ad = new AlertDialog.Builder(LoginActivity.this);
                        ad.setTitle("Incorrect Credentials!");
                        ad.setMessage("Please check your Email or Password!");
                        ad.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        ad.show();
                    } else if (ans.compareTo("ok") == 0) {

                        JSONArray jarry = json.getJSONArray("Data");
                        JSONObject jobj = jarry.getJSONObject(0);
                        String adminId = jobj.getString("data0");

                        UserPref.setValue(LoginActivity.this, "adminId", adminId);
                        UserPref.setLoginStatus(LoginActivity.this, true);

                        Intent i = new Intent(LoginActivity.this, DashboardActivity.class);
                        startActivity(i);
                        finish();

                    } else if (ans.compareTo("error") == 0) {
                        String error = json.getString("Data");
                        Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(LoginActivity.this, "catch - " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}