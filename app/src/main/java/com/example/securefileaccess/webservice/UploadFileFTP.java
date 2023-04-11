package com.example.securefileaccess.webservice;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.Window;
import android.widget.LinearLayout;

import com.example.securefileaccess.R;
import com.example.securefileaccess.helper.Constants;
import com.example.securefileaccess.helper.Utility;
import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.io.CopyStreamAdapter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

public class UploadFileFTP extends AsyncTask<String, String, String> {

    FTPClient ftpClient;
    ProgressDialog progressDialog;
    Context context;
    LinearLayout llMainSaveFileToDb;
    Utility utility;

    CopyStreamAdapter streamListener;

    AsyncResponseFTP asyncResponseFTP;

    public UploadFileFTP(Context context, LinearLayout llMainSaveFileToDb, AsyncResponseFTP asyncResponseFTP) {
        this.ftpClient = new FTPClient();
        this.context = context;
        this.llMainSaveFileToDb = llMainSaveFileToDb;
        this.asyncResponseFTP = asyncResponseFTP;
        utility = new Utility();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(context, R.style.progressDialog);
        utility.showProgressDialogHorizontal(progressDialog);

    }

    @Override
    protected String doInBackground(String... strings) {
        String ans = "";
        try {
            ftpClient.connect(Constants.SITENAME);
            if (ftpClient.login(Constants.USER_NAME, Constants.PASSWORD)) {
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();

                BufferedInputStream bufferedInputStream = null;
                final File file = new File(strings[0]);

                ftpClient.changeWorkingDirectory(Constants.DB_DIR_FILE_UPLOAD);
                FTPFile[] filesFTP = ftpClient.listFiles(Constants.DB_DIR_FILE_UPLOAD);

                for (FTPFile ftpFile : filesFTP) {
                    if (ftpFile.getName().contains(strings[2])) {
                        ans = "already";
                    } else {
                        bufferedInputStream = new BufferedInputStream(new FileInputStream(file));

                        streamListener = new CopyStreamAdapter() {
                            @Override
                            public void bytesTransferred(long totalBytesTransferred,
                                                         int bytesTransferred, long streamSize) {

                                int percent = (int) (totalBytesTransferred * 100 / file.length());
                                progressDialog.setProgress(percent);
                                publishProgress();

                                if (totalBytesTransferred == file.length()) {
                                    System.out.println("100% transfered");
                                    removeCopyStreamListener(streamListener);
                                }
                            }
                        };

                        ftpClient.setCopyStreamListener(streamListener);


                        boolean status = ftpClient.storeFile(strings[1], bufferedInputStream);
                        if (status) {
                            ans = "true";

                        } else {
                            ans = "false";
                        }

                        bufferedInputStream.close();
                    }
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
        progressDialog.cancel();

        if (s.compareTo("true") == 0) {
            Snackbar.make(llMainSaveFileToDb, "UPLOADED", Snackbar.LENGTH_LONG).show();
            asyncResponseFTP.onAsyncResponseFTP(s);
        } else if (s.compareTo("already") == 0) {
            Snackbar.make(llMainSaveFileToDb, "FILE ALREADY EXISTS", Snackbar.LENGTH_LONG).show();
        } else
            Snackbar.make(llMainSaveFileToDb, "FAILED TO UPLOAD", Snackbar.LENGTH_LONG).show();
    }

    public interface AsyncResponseFTP {
        void onAsyncResponseFTP(String s);
    }


}
