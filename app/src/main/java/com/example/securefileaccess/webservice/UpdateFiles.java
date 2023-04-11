package com.example.securefileaccess.webservice;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import com.example.securefileaccess.R;
import com.example.securefileaccess.activity.UploadFileActivity;
import com.example.securefileaccess.helper.Authentication;
import com.example.securefileaccess.helper.Constants;
import com.example.securefileaccess.helper.Utility;
import com.example.securefileaccess.model.FilesMaster;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.io.CopyStreamAdapter;
import org.json.JSONObject;

public class UpdateFiles extends AsyncTask<String, String, String> {

    View linearLayout;
    Context context;
    Authentication authentication;
    Utility utility;
    FilesMaster fileMasterEntity;

    String newFileName, newFileDescription, newFileDir;

    FTPClient ftpClient;
    ProgressDialog progressDialog;

    CopyStreamAdapter streamListener;

    public UpdateFiles(Context context, View view, FilesMaster fileMasterEntity,
                       String newFileName, String newFileDescription, String newFileDir) {
        this.context = context;
        this.linearLayout = view;
        this.fileMasterEntity = fileMasterEntity;
        this.newFileName = newFileName;
        this.newFileDescription = newFileDescription;
        this.newFileDir = newFileDir;
        iniObj();
    }

    private void iniObj() {
        authentication = new Authentication(context);
        utility = new Utility();
        ftpClient = new FTPClient();
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(context, R.style.progressDialog);
        utility.showProgressDialog(progressDialog);
    }

    @Override
    protected String doInBackground(String... strings) {
        String ans = "false";
        try {
            ftpClient.connect(Constants.SITENAME);
            if (ftpClient.login(Constants.USER_NAME, Constants.PASSWORD)) {
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                ftpClient.changeWorkingDirectory(Constants.DB_DIR_FILE_UPLOAD);


                if (!fileMasterEntity.getFileName().equals(newFileName)) {
                    FTPFile[] filesFTP = ftpClient.listFiles(Constants.DB_DIR_FILE_UPLOAD);


                    for (FTPFile ftpFile : filesFTP) {
                        if (ftpFile.getName().contains(newFileName)) {
                            ans = "already";
                        } else {
                            boolean rename = ftpClient.rename(Constants.DB_DIR_FILE_UPLOAD + "/" + fileMasterEntity.getFileName(),
                                    newFileName);
                            if (rename = true) {
                                ans = "true";
                            } else {
                                ans = "false";
                            }
                            String replyString = ftpClient.getReplyString();
                            Log.e("SRENAME", replyString);
                        }
                    }
                }else {
                    ans = "true";
                }

                ftpClient.logout();
                ftpClient.disconnect();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ans;
    }


    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        progressDialog.dismiss();
        if (s == "true") {
            encryptDataAndSaveFile();
        } else if (s == "already") {
            Snackbar.make(linearLayout, "FILE NAME ALREADY EXISTS", Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(linearLayout, "FAILED TO UPDATE FILE", Snackbar.LENGTH_LONG).show();
        }

    }

    private void encryptDataAndSaveFile() {

        String fileId = String.valueOf(fileMasterEntity.getFileId());
        String adminId = fileMasterEntity.getAdminId();
        String fileName = authentication.EncyptMesg(newFileName);
        String fileType = authentication.EncyptMesg(fileMasterEntity.getFileType());
        String description = authentication.EncyptMesg(newFileDescription);
        String fileSize = authentication.EncyptMesg(fileMasterEntity.getFileSize());
        String databaseDirectory = authentication.EncyptMesg(newFileDir);
        String dateTime = authentication.EncyptMesg(utility.currentDateTime());
        String duration = authentication.EncyptMesg(fileMasterEntity.getEncryptionTime());


        new AsyncSaveFileToDB(fileId, adminId, fileName, fileType, description, fileSize,
                databaseDirectory, dateTime, duration).execute();
    }

    private class AsyncSaveFileToDB extends AsyncTask<String, String, String> {

        ProgressDialog progressDialog;

        String fileId;
        String adminId;
        String fileName;
        String fileType;
        String description;
        String fileSize;
        String databaseDirectory;
        String dateTime;
        String encryptionTime;

        public AsyncSaveFileToDB(String fileId, String adminId,
                                 String fileName, String fileType, String description,
                                 String fileSize, String databaseDirectory, String dateTime, String encryptionTime) {
            this.fileId = fileId;
            this.adminId = adminId;
            this.fileName = fileName;
            this.fileType = fileType;
            this.description = description;
            this.fileSize = fileSize;
            this.databaseDirectory = databaseDirectory;
            this.dateTime = dateTime;
            this.encryptionTime = encryptionTime;
        }

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
                RestAPI restAPI = new RestAPI();
                JSONParse jsonParse = new JSONParse();

                Log.e("fileId", fileId);
                Log.e("adminId", adminId);
                Log.e("fileType", fileType);
                Log.e("fileSize", fileSize);
                Log.e("description", description);
                Log.e("databaseDirectory", databaseDirectory);
                Log.e("encryptionTime", String.valueOf(encryptionTime));
                Log.e("dateTime", dateTime);

                Log.e("fileName", fileName);
                Log.e("directory", databaseDirectory);


                JSONObject json = restAPI.Updatefile(
                        fileId, adminId, fileType, fileName, fileSize,
                        description, databaseDirectory, String.valueOf(encryptionTime), dateTime);
                a = jsonParse.Parse(json);

            } catch (Exception e) {
                a = e.getMessage();
            }
            return a;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
//            progressDialog.dismiss();
            Log.d("UpdateFile", s);
            if (WebUtility.checkConnection(s)) {
                Pair<String, String> pair = WebUtility.GetErrorMessage(s);
                WebUtility.ShowAlertDialog(context, pair.first, pair.second, false);
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String statusValue = jsonObject.getString("status");

                    if (statusValue.compareTo("true") == 0) {
                        Snackbar.make(linearLayout, "Update Success", Snackbar.LENGTH_LONG).show();
                        final Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(((Activity) context)::finish, 300);
                    } else if (statusValue.compareTo("already") == 0) {
                        Snackbar.make(linearLayout, "File already updated", Snackbar.LENGTH_LONG).show();
                        final Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(((Activity) context)::finish, 300);
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