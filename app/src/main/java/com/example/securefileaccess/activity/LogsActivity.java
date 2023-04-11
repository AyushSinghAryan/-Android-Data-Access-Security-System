package com.example.securefileaccess.activity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securefileaccess.R;
import com.example.securefileaccess.adapter.LogAdaptor;
import com.example.securefileaccess.helper.Authentication;
import com.example.securefileaccess.helper.Constants;
import com.example.securefileaccess.helper.Utility;
import com.example.securefileaccess.model.FilesMaster;
import com.example.securefileaccess.model.Logs;
import com.example.securefileaccess.webservice.JSONParse;
import com.example.securefileaccess.webservice.RestAPI;
import com.example.securefileaccess.webservice.WebUtility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class LogsActivity extends AppCompatActivity {

    private final String TAG = "RESPONSE:-";
    Spinner spinnerFileType;
    String[] fileType = {"CHECKED", "DOWNLOADED"};
    RecyclerView fileRV;
    ArrayList<Logs> logsArrayList;
    TextView txtNoResult;
    private RestAPI restAPI;
    private Utility utility;
    private JSONParse jsonParse;
    private Authentication authentication;
    private ProgressDialog progressDialog;
    private FilesMaster entity;
    private Toolbar toolbar;
    private TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);

        initUI();
        initObj();
        initToolBar();
        new AsyncGetCheckedList().execute();
    }

    private void initUI() {
        fileRV = findViewById(R.id.fileRV);
        spinnerFileType = findViewById(R.id.spinnerFileType);
        txtNoResult = findViewById(R.id.txtNoResult);
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, fileType);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFileType.setAdapter(arrayAdapter);

        entity = (FilesMaster) getIntent().getSerializableExtra("FileMaster");
        new getCheckedList().execute(String.valueOf(entity.getFileId()));
        spinnerFileType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                new getCheckedList().execute(String.valueOf(entity.getFileId()));
                if (spinnerFileType.getSelectedItem().toString().equals("CHECKED")) {
                    new getCheckedList().execute(String.valueOf(entity.getFileId()));
                } else {
                    new getDownloadedList().execute(String.valueOf(entity.getFileId()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initToolBar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvTitle = toolbar.findViewById(R.id.tvTitle);
        tvTitle.setText(Constants.VIEW_LOGS);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void initObj() {
        restAPI = new RestAPI();
        utility = new Utility();
        jsonParse = new JSONParse();

        authentication = new Authentication(this);
        progressDialog = new ProgressDialog(this, R.style.progressDialog);

    }


    private class AsyncGetCheckedList extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            utility.showProgressDialog(progressDialog);
        }

        @Override
        protected String doInBackground(String... strings) {
            String a;

            String fileId = "28";
//            String fileId = String.valueOf(entity.getFileId());
            try {
                if (fileId != null) {
                    RestAPI restAPI = new RestAPI();
                    JSONParse jsonParse = new JSONParse();
                    JSONObject json = restAPI.getCheckedList(fileId);
                    a = jsonParse.Parse(json);
                } else {
                    a = "";
//                    Snackbar.make(linearLayout, "Need to login to add a file", Snackbar.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                a = e.getMessage();
            }
            return a;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
            Log.d("SaveAndUploadAsync", s);
            if (WebUtility.checkConnection(s)) {
                Pair<String, String> pair = WebUtility.GetErrorMessage(s);
                WebUtility.ShowAlertDialog(LogsActivity.this, pair.first, pair.second, false);
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String statusValue = jsonObject.getString("status");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class getCheckedList extends AsyncTask<String, JSONObject, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            utility.showProgressDialog(progressDialog);
        }

        @Override
        protected String doInBackground(String... strings) {
            String data = null;
            RestAPI restAPI = new RestAPI();
            try {
                JSONObject json = restAPI.getCheckedList(strings[0]);
                JSONParse jp = new JSONParse();
                data = jp.Parse(json);
                Log.d(TAG, data.toString());
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
                data = e.getMessage();
            }
            return data;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
//            progressDialog.dismiss();
            Log.d(TAG, s);
            try {
                if (WebUtility.checkConnection(s)) {
                    Pair<String, String> pair = WebUtility.GetErrorMessage(s);
                    WebUtility.ShowAlertDialog(LogsActivity.this, pair.first, pair.second, false);
                } else {
                    JSONObject json = new JSONObject(s);
                    String StatusValue = json.getString("status");
                    if (StatusValue.compareTo("ok") == 0) {
                        JSONArray jsonArray = json.getJSONArray("Data");
                        logsArrayList = new ArrayList<>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObj = jsonArray.getJSONObject(i);
                            logsArrayList.add(new Logs(
                                    Integer.parseInt(jsonObj.getString("data0")),
                                    jsonObj.getString("data1"),
                                    jsonObj.getString("data2"),
                                    jsonObj.getString("data3"),
                                    jsonObj.getString("data4"),
                                    jsonObj.getString("data5"),
                                    jsonObj.getString("data6"),
                                    jsonObj.getString("data7")
                            ));
                        }
//
                        txtNoResult.setVisibility(View.GONE);
                        fileRV.setVisibility(View.VISIBLE);
                        LogAdaptor listAdapters = new LogAdaptor(logsArrayList, LogsActivity.this);
                        fileRV.setHasFixedSize(true);
                        fileRV.setLayoutManager(new LinearLayoutManager(LogsActivity.this));
                        fileRV.setAdapter(listAdapters);

                    } else if (StatusValue.compareTo("no") == 0) {
                        txtNoResult.setVisibility(View.VISIBLE);
                        fileRV.setVisibility(View.GONE);
                        Toast.makeText(LogsActivity.this, "No data Found", Toast.LENGTH_LONG).show();
                    }
                }
            } catch (Exception e) {
                Toast.makeText(LogsActivity.this, "Something Went Wrong,Try Again Later.", Toast.LENGTH_LONG).show();
                Log.d(TAG + "EXE", e.getMessage());
            }
        }
    }


    private class getDownloadedList extends AsyncTask<String, JSONObject, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            utility.showProgressDialog(progressDialog);
        }

        @Override
        protected String doInBackground(String... strings) {
            String data = null;
            RestAPI restAPI = new RestAPI();
            try {
                JSONObject json = restAPI.getDownloadedList(strings[0]);
                JSONParse jp = new JSONParse();
                data = jp.Parse(json);
                Log.d(TAG, data.toString());
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
                data = e.getMessage();
            }
            return data;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
//            progressDialog.dismiss();
            Log.d(TAG, s);
            try {
                if (WebUtility.checkConnection(s)) {
                    Pair<String, String> pair = WebUtility.GetErrorMessage(s);
                    WebUtility.ShowAlertDialog(LogsActivity.this, pair.first, pair.second, false);
                } else {
                    JSONObject json = new JSONObject(s);
                    String StatusValue = json.getString("status");
                    if (StatusValue.compareTo("ok") == 0) {
                        JSONArray jsonArray = json.getJSONArray("Data");
                        logsArrayList = new ArrayList<>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObj = jsonArray.getJSONObject(i);
                            logsArrayList.add(new Logs(
                                    Integer.parseInt(jsonObj.getString("data0")),
                                    jsonObj.getString("data1"),
                                    jsonObj.getString("data2"),
                                    jsonObj.getString("data3"),
                                    jsonObj.getString("data4"),
                                    jsonObj.getString("data5"),
                                    jsonObj.getString("data6"),
                                    jsonObj.getString("data7")
                            ));
                        }
//
                        txtNoResult.setVisibility(View.GONE);
                        fileRV.setVisibility(View.VISIBLE);
                        LogAdaptor listAdapters = new LogAdaptor(logsArrayList, LogsActivity.this);
                        fileRV.setHasFixedSize(true);
                        fileRV.setLayoutManager(new LinearLayoutManager(LogsActivity.this));
                        fileRV.setAdapter(listAdapters);

                    } else if (StatusValue.compareTo("no") == 0) {
                        txtNoResult.setVisibility(View.VISIBLE);
                        fileRV.setVisibility(View.GONE);
                        Toast.makeText(LogsActivity.this, "No data Found", Toast.LENGTH_LONG).show();
                    }
                }
            } catch (Exception e) {
                Toast.makeText(LogsActivity.this, "Something Went Wrong,Try Again Later.", Toast.LENGTH_LONG).show();
                Log.d(TAG + "EXE", e.getMessage());
            }
        }
    }
}
