package com.example.securefileaccess.helper;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.View;

import com.example.securefileaccess.R;
import com.example.securefileaccess.activity.LoginActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Utility {

    public void showProgressDialog(ProgressDialog progressDialog) {
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        progressDialog.show();
    }

    public void showProgressDialogHorizontal(ProgressDialog progressDialog) {
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Uploading...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();
    }

    public String formatSize(long size) {
        String suffix = null;

        if (size >= 1024) {
            suffix = " Bytes";
            size /= 1024;
            if (size >= 1024) {
                suffix = " MB";
                size /= 1024;
            }
        }
        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));

        int commaOffset = resultBuffer.length() - 3;
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',');
            commaOffset -= 3;
        }
        if (suffix != null) resultBuffer.append(suffix);
        return resultBuffer.toString();
    }

    public String currentDateTime() {
        Date currentTime = Calendar.getInstance().getTime();
        String formattedDate =
                new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH).format(currentTime);
        Log.e("current_time", formattedDate);
        return formattedDate;
    }
}
