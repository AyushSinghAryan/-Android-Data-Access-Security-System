package com.example.securefileaccess.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.securefileaccess.R;
import com.example.securefileaccess.adapter.ViewFilesByDirAdapter;
import com.example.securefileaccess.helper.Authentication;
import com.example.securefileaccess.helper.Constants;
import com.example.securefileaccess.helper.Utility;
import com.example.securefileaccess.model.Employee;
import com.example.securefileaccess.model.FileType;
import com.example.securefileaccess.model.FilesMaster;
import com.example.securefileaccess.webservice.JSONParse;
import com.example.securefileaccess.webservice.RestAPI;
import com.example.securefileaccess.webservice.WebUtility;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ViewFilesByDirectory extends AppCompatActivity {

    private Toolbar toolbar;
    private RelativeLayout rlMainViewFilesByDir;
    private LinearLayout llRvFiles;
    private TextView tvNoData;
    private RecyclerView rvViewFiles;
    private BarChart barChartEncryption;

    private String fileType = "";
    private FilesMaster entity;
    private List<FilesMaster> filesMasterList;

    private RestAPI restAPI;
    private JSONParse jsonParse;
    private Utility utility;

    private ProgressDialog progressDialog;

    private Authentication authentication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_files_by_directory);

        initToolBar();
        initUI();
        initObj();
        getFileType();
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
        tvTitle.setText(Constants.VIEW_FILES_BY_DIR);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setUpRecyclerView(List<FilesMaster> filesMasterList) {
        ViewFilesByDirAdapter viewFilesByDirAdapter = new ViewFilesByDirAdapter(this, filesMasterList, rlMainViewFilesByDir);
        rvViewFiles.setAdapter(viewFilesByDirAdapter);
        rvViewFiles.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                false));
    }

    private void loadList() {
        rvViewFiles.setAdapter(null);
        new AsyncGetFilesByDir().execute(fileType);
    }

    private void getFileType() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String selectedFileType = bundle.getString("directory");
            if (selectedFileType != null) {
                fileType = selectedFileType;
            }
        }
    }

    private void initObj() {
        authentication = new Authentication(this);
        entity = new FilesMaster();
        filesMasterList = new ArrayList<>();
        restAPI = new RestAPI();
        jsonParse = new JSONParse();
        utility = new Utility();
        progressDialog = new ProgressDialog(this, R.style.progressDialog);
    }

    private void initUI() {
        rlMainViewFilesByDir = findViewById(R.id.rlMainViewFilesByDir);
        llRvFiles = findViewById(R.id.llRvFiles);
        tvNoData = findViewById(R.id.tvNoData);
        rvViewFiles = findViewById(R.id.rvViewFiles);
        barChartEncryption = findViewById(R.id.barChartEncryption);

        tvNoData.setText(R.string.loadingData);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncGetFilesByDir extends AsyncTask<String, String, String> {
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
                if (fileType != null) {
                    json = restAPI.Agetfiles(authentication.EncyptMesg(fileType));
                } else {
                    json = restAPI.Agetfiles("All");
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
                WebUtility.ShowAlertDialog(ViewFilesByDirectory.this, pair.first, pair.second, false);
            } else {
                try {
                    filesMasterList.removeAll(filesMasterList);
                    JSONObject jsonObject = new JSONObject(s);
                    String jsonValue = jsonObject.getString("status");
                    if (jsonValue.compareTo("no") == 0) {
                        tvNoData.setText(R.string.no_data_available);
                        Snackbar.make(rlMainViewFilesByDir, "No Files Found", Snackbar.LENGTH_LONG).show();
                    }
                    if (jsonValue.compareTo("ok") == 0) {
                        tvNoData.setVisibility(View.GONE);
                        rvViewFiles.setVisibility(View.VISIBLE);
                        JSONArray jsonArray = jsonObject.getJSONArray("Data");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonArrayObject = jsonArray.getJSONObject(i);
                            setDataFromResponseToEntity(jsonArrayObject);
                            filesMasterList.add(entity);
                            entity = new FilesMaster();
                        }
                        setUpRecyclerView(filesMasterList);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setDataFromResponseToEntity(JSONObject jsonArrayObject) throws JSONException {
        entity.setFileId(jsonArrayObject.getInt("data0"));
        entity.setAdminId(jsonArrayObject.getString("data1"));
        entity.setFileType(authentication.DecryptMesg(jsonArrayObject.getString("data2")));
        entity.setFileName(authentication.DecryptMesg(jsonArrayObject.getString("data3")));
        entity.setFileSize(authentication.DecryptMesg(jsonArrayObject.getString("data4")));
        entity.setFileDescription(authentication.DecryptMesg(jsonArrayObject.getString("data5")));
        entity.setFileDirectory(authentication.DecryptMesg(jsonArrayObject.getString("data6")));
        entity.setEncryptionTime(authentication.DecryptMesg(jsonArrayObject.getString("data7")));
    }

}