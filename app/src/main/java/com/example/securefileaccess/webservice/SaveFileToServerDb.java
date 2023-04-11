//package com.example.securefileaccess.webservice;
//
//import android.app.ProgressDialog;
//import android.content.Context;
//import android.os.AsyncTask;
//import android.util.Log;
//import android.util.Pair;
//import android.view.Window;
//import android.widget.LinearLayout;
//import android.widget.Toast;
//
//import com.example.securefileaccess.R;
//import com.example.securefileaccess.helper.Utility;
//import com.google.android.material.snackbar.Snackbar;
//
//import org.json.JSONObject;
//
//public class SaveFileToServerDb extends AsyncTask<String, JSONObject, String> {
//
//    Context context;
//    LinearLayout linearlayout;
//
//    RestAPI restAPI;
//    JSONParse jsonParse;
//
//    String adminId, fileType, fileName, fileSize, fileDesc, fileDir, encryptionTime, dateTime;
//
//
//    public SaveFileToServerDb(Context context, LinearLayout linearlayout,
//                              String adminId, String fileType, String fileName, String fileSize,
//                              String fileDesc, String fileDir, String encryptionTime, String dateTime) {
//        this.context = context;
//        this.linearlayout = linearlayout;
//        this.adminId = adminId;
//        this.fileType = fileType;
//        this.fileName = fileName;
//        this.fileSize = fileSize;
//        this.fileDesc = fileDesc;
//        this.fileDir = fileDir;
//        this.encryptionTime = encryptionTime;
//        this.dateTime = dateTime;
//    }
//
//    @Override
//    protected String doInBackground(String... strings) {
//
//        String result = "";
//
//        try {
//            restAPI = new RestAPI();
//            jsonParse = new JSONParse();
//            JSONObject jsonObject = restAPI.Addfile(adminId, fileType, fileName,
//                    fileSize, fileDesc, fileDir, encryptionTime, dateTime);
//            result = jsonParse.Parse(jsonObject);
//
//        } catch (Exception exception) {
//            result = exception.getMessage();
//        }
//        Log.e("VALUESTATUS", result);
//        return result;
//    }
//
//
//    @Override
//    protected void onPreExecute() {
//        super.onPreExecute();
//    }
//
//
//    @Override
//    protected void onPostExecute(String s) {
//        if (WebUtility.checkConnection(s)) {
//            Pair<String, String> pair = WebUtility.GetErrorMessage(s);
//            WebUtility.ShowAlertDialog(context, pair.first, pair.second, false);
//        } else {
//            try {
//                JSONObject jsonObject = new JSONObject(s);
//                String jsonValue = jsonObject.getString("status");
//
//                if (jsonValue.compareTo("true") == 0) {
//                    Snackbar.make(linearlayout, "SAVED SUCCESSFULLY", Snackbar.LENGTH_LONG).show();
//                } else if (jsonValue.compareTo("already") == 0) {
//                    Snackbar.make(linearlayout, "FILE ALREADY EXISTS", Snackbar.LENGTH_LONG).show();
//                } else {
//                    Snackbar.make(linearlayout, "SOMETHING WENT WRONG", Snackbar.LENGTH_LONG).show();
//                }
//
//            } catch (Exception exception) {
//                Toast.makeText(context, exception.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//}
