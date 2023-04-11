package com.example.securefileaccess.activity;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.example.securefileaccess.R;
import com.example.securefileaccess.activity.baseActivity.BaseNaveActivity;
import com.example.securefileaccess.adapter.DashboardRecyclerViewGridsAdapter;
import com.example.securefileaccess.helper.Constants;
import com.example.securefileaccess.helper.FabAnimation;
import com.example.securefileaccess.helper.UserPref;
import com.example.securefileaccess.helper.Utility;
import com.example.securefileaccess.model.Directory;
import com.example.securefileaccess.webservice.JSONParse;
import com.example.securefileaccess.webservice.RestAPI;
import com.example.securefileaccess.webservice.WebUtility;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends BaseNaveActivity {

    private Toolbar toolbar;
    private TextView tvTitle;
    private Button btnLogout;
    private RecyclerView rvDashboardGrids;
    private FloatingActionButton fabDashboardMain;
    private Directory entity;
    private List<Directory> directoryList;

    private RestAPI restAPI;
    private ProgressDialog progressDialog;
    private JSONParse jsonParse;
    private Utility utility;

    boolean isRotate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean loginStatus = UserPref.getLoginStatus(this);

        if (!loginStatus) {
            Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View contentView = inflater.inflate(R.layout.activity_dashboard, null, false);
            drawerLayout.addView(contentView, 0);

            initToolBar();
            initUI();
            initObj();
            loadList();
        }

    }

    private void loadList() {
        new GetDir().execute();
    }

    private void initObj() {
        directoryList = new ArrayList<>();
        entity = new Directory();
        restAPI = new RestAPI();
        jsonParse = new JSONParse();
        utility = new Utility();
        progressDialog = new ProgressDialog(DashboardActivity.this, R.style.progressDialog);
    }

    private void initToolBar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvTitle = toolbar.findViewById(R.id.tvTitle);
        tvTitle.setText(Constants.DASHBOARD);
        btnLogout = toolbar.findViewById(R.id.btnLogout);
        btnLogout.setVisibility(View.VISIBLE);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(Constants.DASHBOARD);
    }

    private void initUI() {


        rvDashboardGrids = findViewById(R.id.rvDashboardGrids);
        fabDashboardMain = findViewById(R.id.fabDashboardMain);

        fabDashboardMain.setOnClickListener(this::onClickFabDashboardMain);

        btnLogout.setOnClickListener(v -> logout());
    }

    private void onClickFabDashboardMain(View v) {
        Intent intent = new Intent(this, UploadFileActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        int value = this.getResources().getConfiguration().orientation;

        if (value == Configuration.ORIENTATION_PORTRAIT) {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(DashboardActivity.this, 2);
            rvDashboardGrids.setLayoutManager(gridLayoutManager);
        } else if (value == Configuration.ORIENTATION_LANDSCAPE) {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(DashboardActivity.this, 3);
            rvDashboardGrids.setLayoutManager(gridLayoutManager);
        }
    }

    private void setUpRecyclerView(List<Directory> directoryList) {
        DashboardRecyclerViewGridsAdapter recyclerViewGridsAdapter =
                new DashboardRecyclerViewGridsAdapter(DashboardActivity.this, directoryList);
        rvDashboardGrids.setAdapter(recyclerViewGridsAdapter);
    }


    private void logout() {

        View alertView = getLayoutInflater().inflate(R.layout.alert_logout_confirmation, null);

        Button alertBtnBack = alertView.findViewById(R.id.btnBack);
        Button alertBtnLogout = alertView.findViewById(R.id.btnLogout);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        if (alertView.getParent() != null) {
            ((ViewGroup) alertView.getParent()).removeView(alertView);
        }
        alert.setView(alertView);

        final AlertDialog alertDialog = alert.show();

        alertBtnLogout.setOnClickListener(v -> alertButtonLogoutClick());

        alertBtnBack.setOnClickListener(v -> {
            alertDialog.dismiss();
        });

    }

    private void alertButtonLogoutClick() {
        UserPref.setLoginStatus(DashboardActivity.this, false);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.btnLogout) {
            logout();
        }
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout == null) {
            finish();
            return;
        }
        if (!drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.openDrawer(GravityCompat.START);
        else
            finish();
    }

    private class GetDir extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            utility.showProgressDialog(progressDialog);

        }

        @Override
        protected String doInBackground(String... strings) {
            String a;
            try {
                JSONObject json = restAPI.getDirectory();
                a = jsonParse.Parse(json);
            } catch (Exception e) {
                a = e.getMessage();
            }
            return a;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("GetDirResponse", s);
            progressDialog.cancel();
            if (WebUtility.checkConnection(s)) {
                Pair<String, String> pair = WebUtility.GetErrorMessage(s);
                WebUtility.ShowAlertDialog(DashboardActivity.this, pair.first, pair.second, false);
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String jsonValue = jsonObject.getString("status");
                    if (jsonValue.compareTo("no") == 0) {
                        Toast.makeText(DashboardActivity.this, "SOMETHING WENT WRONG", Toast.LENGTH_LONG).show();
                    } else if (jsonValue.compareTo("ok") == 0) {
                        try {
                            JSONArray jsonArray = jsonObject.getJSONArray("Data");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonArrayObject = jsonArray.getJSONObject(i);
                                entity.setName(jsonArrayObject.getString("data1"));
                                directoryList.add(entity);
                                entity = new Directory();
                            }
                            setUpRecyclerView(directoryList);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }
}