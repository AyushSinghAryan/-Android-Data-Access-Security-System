package com.example.securefileaccess.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.securefileaccess.R;
import com.example.securefileaccess.activity.baseActivity.BaseNaveActivity;
import com.example.securefileaccess.helper.Authentication;
import com.example.securefileaccess.helper.Constants;
import com.example.securefileaccess.helper.Utility;
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

import java.util.ArrayList;
import java.util.List;

public class ReportActivity extends BaseNaveActivity {

    private Toolbar toolbar;
    private RelativeLayout rlMainReportActivity;
    private BarChart barChartEncryption;
    private ArrayList<String> barEntryLabels;
    private ArrayList<BarEntry> barEntries;
    private BarDataSet barDataSet;
    private BarData barData;

    private RestAPI restAPI;
    private JSONParse jsonParse;
    private Utility utility;
    private FilesMaster entity;

    private ProgressDialog progressDialog;

    private Authentication authentication;
    private List<FilesMaster> filesMasterList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_report, null, false);
        drawerLayout.addView(contentView, 0);


        initToolBar();
        initUI();
        initObj();
        new AsyncGetFilesByDir().execute();
    }

    private void initToolBar() {
        toolbar = findViewById(R.id.toolbar);

        TextView tvTitle = toolbar.findViewById(R.id.tvTitle);
        tvTitle.setText(Constants.VIEW_REPORT);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initObj() {
        barEntryLabels = new ArrayList<>();
        barEntries = new ArrayList<>();
        authentication = new Authentication(this);
        entity = new FilesMaster();
        filesMasterList = new ArrayList<>();
        restAPI = new RestAPI();
        jsonParse = new JSONParse();
        utility = new Utility();
        progressDialog = new ProgressDialog(this, R.style.progressDialog);
    }

    private void initUI() {
        rlMainReportActivity = findViewById(R.id.rlMainReportActivity);
        barChartEncryption = findViewById(R.id.barChartEncryption);
    }

    private void setBarChart(List<FilesMaster> filesMasterList) {
        try {
            for (int i = 0; i < filesMasterList.size(); i++) {
                barEntryLabels.add(filesMasterList.get(i).getFileName());
            }
            for (int i = 0; i < filesMasterList.size(); i++) {
                float encryptionTime = Float.parseFloat(filesMasterList.get(i).getEncryptionTime());
                barEntries.add(new BarEntry(encryptionTime, i));
            }

            barDataSet = new BarDataSet(barEntries, "Encryption Time (in milliseconds)");
            barData = new BarData(barEntryLabels, barDataSet);
            barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
            barChartEncryption.setData(barData);
            barChartEncryption.animateY(1000);
            barChartEncryption.invalidate();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

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
                JSONObject json = restAPI.Agetfiles("All");
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
                WebUtility.ShowAlertDialog(ReportActivity.this, pair.first, pair.second, false);
            } else {
                try {
                    if (filesMasterList.size() > 0) {
                        filesMasterList.removeAll(filesMasterList);
                    }
                    JSONObject jsonObject = new JSONObject(s);
                    String jsonValue = jsonObject.getString("status");
                    if (jsonValue.compareTo("no") == 0) {
                        Snackbar.make(rlMainReportActivity, "No Files Found", Snackbar.LENGTH_LONG).show();
                    }
                    if (jsonValue.compareTo("ok") == 0) {
                        JSONArray jsonArray = jsonObject.getJSONArray("Data");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonArrayObject = jsonArray.getJSONObject(i);
                            setDataFromResponseToEntity(jsonArrayObject);
                            filesMasterList.add(entity);
                            entity = new FilesMaster();
                        }
                        setBarChart(filesMasterList);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setDataFromResponseToEntity(JSONObject jsonArrayObject) throws JSONException {
        entity.setFileId(jsonArrayObject.getInt("data0"));
        entity.setFileType(authentication.DecryptMesg(jsonArrayObject.getString("data2")));
        entity.setFileName(authentication.DecryptMesg(jsonArrayObject.getString("data3")));
        entity.setFileSize(authentication.DecryptMesg(jsonArrayObject.getString("data4")));
        entity.setFileDescription(authentication.DecryptMesg(jsonArrayObject.getString("data5")));
        entity.setFileDirectory(authentication.DecryptMesg(jsonArrayObject.getString("data6")));
        entity.setEncryptionTime(authentication.DecryptMesg(jsonArrayObject.getString("data7")));
    }
}