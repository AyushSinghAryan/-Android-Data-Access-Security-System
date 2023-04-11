package com.example.securefileaccess.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.securefileaccess.activity.AddUserActivity;
import com.example.securefileaccess.helper.Authentication;
import com.example.securefileaccess.helper.Constants;
import com.example.securefileaccess.helper.PathUtils;
import com.example.securefileaccess.helper.UserPref;
import com.example.securefileaccess.helper.Utility;
import com.example.securefileaccess.model.FileType;
import com.example.securefileaccess.model.FilesMaster;
import com.example.securefileaccess.webservice.JSONParse;
import com.example.securefileaccess.webservice.RestAPI;
import com.example.securefileaccess.webservice.UploadFileFTP;
import com.example.securefileaccess.webservice.WebUtility;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

import java.io.File;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SaveAndUploadFile implements UploadFileFTP.AsyncResponseFTP {

    Context context;
    Uri fileUri;
    FileType selectedSpinnerFileType;
    String newFileName, fileDescription,fileDirectory;
    LinearLayout linearLayout;

    FilesMaster entity;
    PathUtils pathUtils;
    Utility utility;

    String path;
    String extension;
    String databaseDir;

    Authentication authentication;
    UploadFileFTP.AsyncResponseFTP asyncResponseFTP;


    public SaveAndUploadFile(Context context, Uri fileUri, FileType selectedSpinnerFileType,String fileDirectory,
                             String newFileName, String fileDescription, LinearLayout linearLayout) {
        this.context = context;
        this.fileUri = fileUri;
        this.selectedSpinnerFileType = selectedSpinnerFileType;
        this.fileDirectory = fileDirectory;
        this.newFileName = newFileName;
        this.fileDescription = fileDescription;
        this.linearLayout = linearLayout;
        asyncResponseFTP = this;

    }


    private void initObj() {
        authentication = new Authentication(context);
        pathUtils = new PathUtils();
        entity = new FilesMaster();
        utility = new Utility();
    }

    public void upload() {

        initObj();


        path = PathUtils.getPath(context, fileUri);
        Uri fileP = Uri.fromFile(new File(path));
        MimeTypeMap.getSingleton();
        extension = MimeTypeMap.getFileExtensionFromUrl(fileP.toString());
        Log.e("UPLOAD_EXTENSION",extension);
        databaseDir = Constants.DB_DIR_FILE_UPLOAD + newFileName;

        File file = new File(path);
        Log.e("FILE_TO_UPLOAD",file.toString());
        boolean b = file.exists();
        if (b) {
            try {
                long fileSize = file.length();
                if (fileSize > 1048576) {
                    Snackbar.make(linearLayout, "File must be less than 10mb", Snackbar.LENGTH_LONG).show();
                } else {
                    encryptDataAndSaveFile(file);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show();
        }

    }


    private void encryptDataAndSaveFile(File file) {

        String size = utility.formatSize(file.length());

        long startTime = System.nanoTime();

        String fileName = authentication.EncyptMesg(newFileName);
        String fileType = authentication.EncyptMesg(extension.toUpperCase());
        String description = authentication.EncyptMesg(fileDescription);
        String fileSize = authentication.EncyptMesg(size);
        String fileDir = authentication.EncyptMesg(fileDirectory);
        String dateTime = authentication.EncyptMesg(utility.currentDateTime());

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;

        Log.e("type",fileType);
        setDataToEntity(fileName, fileType, description, fileSize, fileDir, dateTime, duration);

        saveFile(file);
    }

    private void setDataToEntity(String fileName, String fileType, String description,
                                 String fileSize, String fileDir, String dateTime, long duration) {
        entity.setFileName(fileName);
        entity.setFileType(fileType);
        entity.setFileSize(fileSize);
        entity.setFileDescription(description);
        entity.setFileDirectory(fileDir);
        entity.setDateTime(dateTime);
        entity.setAdminId(UserPref.getId(context));
        entity.setEncryptionTime(authentication.EncyptMesg(String.valueOf(duration)));
    }

    private void saveFile(File file) {

        if (databaseDir != null && extension != null || extension.trim().equals("")) {
            new UploadFileFTP(context, linearLayout, asyncResponseFTP).execute(file.getPath(), databaseDir,newFileName);
        } else {
            Toast.makeText(context, "Dir or file not valid", Toast.LENGTH_SHORT).show();
        }
//        Toast.makeText(context, file.getPath(), Toast.LENGTH_LONG).show();
    }


    @Override
    public void onAsyncResponseFTP(String s) {
        new AsyncSaveFileToDB().execute();
    }

    private class AsyncSaveFileToDB extends AsyncTask<String, String, String> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            progressDialog = new ProgressDialog(context);
//            progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//            progressDialog.setMessage("Saving...");
//            progressDialog.setIndeterminate(false);
//            progressDialog.setCancelable(false);
//            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//            progressDialog.setProgress(0);
//            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String a;

            try {
                if (entity.getAdminId() != null) {
                    RestAPI restAPI = new RestAPI();
                    JSONParse jsonParse = new JSONParse();
                    JSONObject json = restAPI.Addfile(
                            entity.getAdminId(),
                            entity.getFileType(),
                            entity.getFileName(),
                            entity.getFileSize(),
                            entity.getFileDescription(),
                            entity.getFileDirectory(),
                            entity.getEncryptionTime(),
                            entity.getDateTime());
                    a = jsonParse.Parse(json);
                } else {
                    a= "";
                    Snackbar.make(linearLayout, "Need to login to add a file", Snackbar.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                a = e.getMessage();
            }
            return a;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
//            progressDialog.dismiss();
            Log.d("SaveAndUploadAsync", s);
            if (WebUtility.checkConnection(s)) {
                Pair<String, String> pair = WebUtility.GetErrorMessage(s);
                WebUtility.ShowAlertDialog(context, pair.first, pair.second, false);
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String statusValue = jsonObject.getString("status");

                    if (statusValue.compareTo("true") == 0) {
                        Snackbar.make(linearLayout, "Save Success", Snackbar.LENGTH_LONG).show();
                        final Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(() -> {
                            ((Activity) context).finish();
                        }, 300);
                    } else if (statusValue.compareTo("already") == 0) {
                        Snackbar.make(linearLayout, "File already exists", Snackbar.LENGTH_LONG).show();
                        final Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(() -> {
                            ((Activity) context).finish();
                        }, 300);
                    } else {
                        Snackbar.make(linearLayout, "Something Went Wrong", Snackbar.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
