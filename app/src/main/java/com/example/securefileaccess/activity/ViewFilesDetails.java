package com.example.securefileaccess.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.securefileaccess.R;
import com.example.securefileaccess.adapter.ShareAccessUserAdapter;
import com.example.securefileaccess.helper.Authentication;
import com.example.securefileaccess.helper.Constants;
import com.example.securefileaccess.helper.UserPref;
import com.example.securefileaccess.helper.Utility;
import com.example.securefileaccess.model.Employee;
import com.example.securefileaccess.model.FilesMaster;
import com.example.securefileaccess.webservice.JSONParse;
import com.example.securefileaccess.webservice.RestAPI;
import com.example.securefileaccess.webservice.WebUtility;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ViewFilesDetails extends AppCompatActivity {

    private Toolbar toolbar;
    private RelativeLayout rlMainViewFilesDetails;
    private LinearLayout llRvContent;
    private TextView tvFileName, tvFileSize, tvFileEncryptionTime, tvNoData;
    private RecyclerView rvShareAccess;
    private Button btnShareAccess;

    private FilesMaster entity;
    private Employee entityUser;
    private List<Employee> userList;

    private Utility utility;
    private Authentication authentication;
    private RestAPI restAPI;
    private JSONParse jsonParse;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_files_details);

        initToolBar();
        initUI();
        initObj();
        loadIntentData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        rvShareAccess.setAdapter(null);
    }

    private void initToolBar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView tvTitle = toolbar.findViewById(R.id.tvTitle);
        tvTitle.setText(Constants.VIEW_FILES_DETAILS);
        ImageView ivEditFile = toolbar.findViewById(R.id.ivEditFile);
        ivEditFile.setOnClickListener(v -> onClickTvEditFile());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void onClickTvEditFile() {
        Intent intent = new Intent(this,UploadFileActivity.class);
        intent.putExtra("FileMaster", entity);
        startActivity(intent);

    }

    private void loadIntentData() {
        Serializable bundle = getIntent().getSerializableExtra("FileMasterDetail");
        if (bundle != null) {
            entity = (FilesMaster) bundle;
            setEntityDataToText();
        }
    }

    @SuppressLint("SetTextI18n")
    private void setEntityDataToText() {
        tvFileName.setText(entity.getFileName());
        tvFileSize.setText(entity.getFileSize());
        tvFileEncryptionTime.setText(entity.getEncryptionTime() + " ms");
    }

    private void initObj() {
        entity = new FilesMaster();
        authentication = new Authentication(this);
        entityUser = new Employee();
        userList = new ArrayList<>();
        restAPI = new RestAPI();
        jsonParse = new JSONParse();
        utility = new Utility();
        progressDialog = new ProgressDialog(this, R.style.progressDialog);
    }


    private void initUI() {
        rlMainViewFilesDetails = findViewById(R.id.rlMainViewFilesDetails);
        llRvContent = findViewById(R.id.llRvContent);
        tvFileName = findViewById(R.id.tvFileName);
        tvFileSize = findViewById(R.id.tvFileSize);
        tvFileEncryptionTime = findViewById(R.id.tvFileEncryptionTime);
        tvNoData = findViewById(R.id.tvNoData);


        rvShareAccess = findViewById(R.id.rvShareAccess);

        btnShareAccess = findViewById(R.id.btnShareAccess);
        btnShareAccess.setOnClickListener(v -> {
            tvNoData.setText(R.string.loadingData);
            llRvContent.setVisibility(View.VISIBLE);
            new AsyncGetUser().execute();
        });
    }

    private void setUpRecyclerView(List<Employee> userList) {
        ShareAccessUserAdapter shareAccessUserAdapter = new ShareAccessUserAdapter(this,
                entity.getFileId(), userList, rlMainViewFilesDetails);
        rvShareAccess.setAdapter(shareAccessUserAdapter);
        rvShareAccess.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
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
                WebUtility.ShowAlertDialog(ViewFilesDetails.this, pair.first, pair.second, false);
            } else {
                try {
                    if (userList.size() > 0) {
                        userList.removeAll(userList);
                    }
                    JSONObject jsonObject = new JSONObject(s);
                    String jsonValue = jsonObject.getString("status");
                    if (jsonValue.compareTo("no") == 0) {
                        tvNoData.setText(R.string.no_data_available);
                        Snackbar.make(rlMainViewFilesDetails, "No User Found", Snackbar.LENGTH_LONG).show();
                    }

                    if (jsonValue.compareTo("ok") == 0) {
                        tvNoData.setVisibility(View.GONE);
                        rvShareAccess.setVisibility(View.VISIBLE);
                        JSONArray jsonArray = jsonObject.getJSONArray("Data");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonArrayObject = jsonArray.getJSONObject(i);
                            setDataFromResponseToEntity(jsonArrayObject);
                            userList.add(entityUser);
                            entityUser = new Employee();
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
        entityUser.setEmployeeId(jsonArrayObject.getInt("data0"));
        entityUser.setName(authentication.DecryptMesg(jsonArrayObject.getString("data1")));
        entityUser.setEmail(authentication.DecryptMesg(jsonArrayObject.getString("data2")));
        entityUser.setContact(authentication.DecryptMesg(jsonArrayObject.getString("data3")));
        entityUser.setDepartment(authentication.DecryptMesg(jsonArrayObject.getString("data4")));
        entityUser.setPassword(authentication.DecryptMesg(jsonArrayObject.getString("data5")));
    }

}