package com.example.securefileaccess.webservice;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.Window;
import android.widget.Toast;


import androidx.core.content.ContextCompat;

import com.example.securefileaccess.R;
import com.example.securefileaccess.activity.DashboardActivity;
import com.example.securefileaccess.activity.ViewFilesByDirectory;
import com.example.securefileaccess.helper.Constants;
import com.example.securefileaccess.helper.Utility;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.io.CopyStreamAdapter;

public class DeleteFile extends AsyncTask<String, String, String> {

    FTPClient ftpClient;
    ProgressDialog progressDialog;
    Context context;
    Utility utility;

    CopyStreamAdapter streamListener;

    public DeleteFile(Context context) {
        this.context = context;
        this.ftpClient = new FTPClient();
        utility = new Utility();
        progressDialog = new ProgressDialog(context, R.style.progressDialog);

    }

    @Override
    protected String doInBackground(String... strings) {

        String ans = "";
        try {
            ftpClient.connect(Constants.SITENAME);
            if (ftpClient.login(Constants.USER_NAME, Constants.PASSWORD)) {
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();

                Boolean status = ftpClient.deleteFile(strings[0]);
                if (status) {
                    ans = "true";
                } else {
                    ans = "false";
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
    protected void onPreExecute() {
        super.onPreExecute();
        utility.showProgressDialog(progressDialog);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        progressDialog.cancel();
        if (s.compareTo("true") == 0) {
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(context, ViewFilesByDirectory.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                ContextCompat.startActivity(context,intent,null);
            }, 500);
        }
        else
            Toast.makeText(context, "File not found", Toast.LENGTH_LONG).show();
    }

}
