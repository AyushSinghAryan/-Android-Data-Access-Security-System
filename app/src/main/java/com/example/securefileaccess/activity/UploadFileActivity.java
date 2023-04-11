package com.example.securefileaccess.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.securefileaccess.R;
import com.example.securefileaccess.adapter.SaveAndUploadFile;
import com.example.securefileaccess.helper.Authentication;
import com.example.securefileaccess.helper.Constants;
import com.example.securefileaccess.helper.PathUtils;
import com.example.securefileaccess.helper.Utility;
import com.example.securefileaccess.model.Directory;
import com.example.securefileaccess.model.FileType;
import com.example.securefileaccess.model.FilesMaster;
import com.example.securefileaccess.webservice.JSONParse;
import com.example.securefileaccess.webservice.RestAPI;
import com.example.securefileaccess.webservice.UpdateFiles;
import com.example.securefileaccess.webservice.WebUtility;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UploadFileActivity extends AppCompatActivity {

    private static final int PICKFILE_RESULT_CODE = 100;
    public static final int REQUEST_PERMISSION_CODE = 100;

    private Toolbar toolbar;
    private TextView tvTitle;
    private LinearLayout linearLayout;
    private Button btnSelectFile, btnUploadFile;
    private TextInputLayout tilFileName, tilFileDescription;
    private EditText etFileName, etFileDescription;
    private Spinner spinnerFileType, spinnerDirectory;
    private Intent data = null;

    private RestAPI restAPI;
    private ProgressDialog progressDialog;
    private JSONParse jsonParse;
    private Utility utility;
    Authentication authentication;


    private Directory entity;
    private FilesMaster filesMasterEntity;
    private List<String> directoryList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_file);

        initToolBar();
        initUI();
        initObj();
        loadSpinner();
        loadDirectories();
        loadFile();
        widgetsVisibility();
    }

    private void widgetsVisibility() {
        if (filesMasterEntity.getFileType() != null) {
            btnUploadFile.setEnabled(true);
            btnSelectFile.setVisibility(View.GONE);
            btnUploadFile.setVisibility(View.VISIBLE);
            btnUploadFile.setText("Update File");
            tilFileName.setVisibility(View.VISIBLE);
            tilFileDescription.setVisibility(View.VISIBLE);
        } else if (filesMasterEntity.getFileType() == null) {
            btnUploadFile.setEnabled(false);
            btnSelectFile.setVisibility(View.VISIBLE);
            btnUploadFile.setVisibility(View.GONE);
            btnUploadFile.setText("Upload File");
            tilFileName.setVisibility(View.GONE);
            tilFileDescription.setVisibility(View.GONE);
        }
    }


    private void loadFile() {
        Serializable bundle = getIntent().getSerializableExtra("FileMaster");
        if (bundle != null) {
            filesMasterEntity = (FilesMaster) bundle;
            tvTitle.setText(Constants.UPDATE_FILES);
            String[] tokens = filesMasterEntity.getFileName().split("\\.(?=[^\\.]+$)");
            etFileName.setText(tokens[0]);
            etFileDescription.setText(filesMasterEntity.getFileDescription());
        }
    }

    private void initToolBar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvTitle = toolbar.findViewById(R.id.tvTitle);
        tvTitle.setText(Constants.ADD_FILES);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void loadDirectories() {
        new GetDir().execute();
    }

    private void setUpSpinner(List<String> directoryList) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, directoryList);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        spinnerDirectory.setAdapter(adapter);
    }

    private void loadSpinner() {
        ArrayAdapter<FileType> adapter = new ArrayAdapter<FileType>(this,
                android.R.layout.simple_spinner_item, FileType.values());
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        spinnerFileType.setAdapter(adapter);
    }

    private void initObj() {
        directoryList = new ArrayList<>();
        entity = new Directory();
        filesMasterEntity = new FilesMaster();
        authentication = new Authentication(this);
        restAPI = new RestAPI();
        jsonParse = new JSONParse();
        utility = new Utility();
        progressDialog = new ProgressDialog(UploadFileActivity.this, R.style.progressDialog);
    }

    private void initUI() {

        linearLayout = findViewById(R.id.llMainActivityUploadFile);

        tilFileName = findViewById(R.id.tilFileName);
        tilFileDescription = findViewById(R.id.tilFileDescription);

        etFileName = findViewById(R.id.etFileName);
        etFileDescription = findViewById(R.id.etDescription);
        spinnerFileType = findViewById(R.id.spinnerFileType);
        spinnerDirectory = findViewById(R.id.spinnerDirectory);

        btnSelectFile = findViewById(R.id.btnSelectFile);
        btnUploadFile = findViewById(R.id.btnUploadFile);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_CODE);
        }

        btnSelectFile.setVisibility(View.VISIBLE);

        btnSelectFile.setOnClickListener(v -> onClickSelectFile());
        btnUploadFile.setOnClickListener(v -> onClickUploadFile(data));

        spinnerFileType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                clear();
                loadFile();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    private void onClickUploadFile(Intent data) {

        if (filesMasterEntity.getFileName() == null) {
            if (data != null) {
                if (!etFileName.getText().toString().trim().isEmpty()) {
                    boolean fileExistsAndSavedToDb = checkIfExistAndSaveFile(data);
                    if (fileExistsAndSavedToDb) {
                        clear();
                    }
                } else {
                    Snackbar.make(linearLayout, "Enter File Name", Snackbar.LENGTH_LONG).show();
                }
            } else {
                Snackbar.make(linearLayout, "No files selected", Snackbar.LENGTH_LONG).show();
            }
        } else if (filesMasterEntity.getFileName() != null) {
            btnSelectFile.setVisibility(View.GONE);
            String[] tokens = filesMasterEntity.getFileName().split("\\.(?=[^\\.]+$)");
            String newFilename = etFileName.getText().toString() + "." + tokens[1];
            new UpdateFiles(this, linearLayout, filesMasterEntity,
                    newFilename, etFileDescription.getText().toString(),
                    (String) spinnerDirectory.getSelectedItem()).execute();
        }

    }

    private void onClickSelectFile() {

        FileType fileType = (FileType) spinnerFileType.getSelectedItem();
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);

        switch (fileType) {
            case IMAGES:
                chooseFile.setType("image/*");
                break;
            case AUDIOS:
                chooseFile.setType("audio/*");
                break;
            case VIDEOS:
                chooseFile.setType("video/*");
                break;
            case DOCUMENTS:
                chooseFile.setType("application/pdf");
                break;
            case APK:
                chooseFile.setType("application/vnd.android.package-archive");
                break;
            case OTHERS:
                chooseFile.setType("*/*");
                break;

        }

        chooseFile = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
    }

    private void clear() {
        etFileName.setText("");
        etFileDescription.setText("");
        widgetsVisibility();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE && grantResults.length > 0) {
        } else {
            Toast.makeText(this, "PERMISSION DENIED", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICKFILE_RESULT_CODE) {
            if (requestCode == PICKFILE_RESULT_CODE) {
                if (resultCode == RESULT_OK) {
                    this.data = data;
                    Uri uri = data.getData();

                    String path = PathUtils.getPath(this, uri);
                    Log.d("PATH",path+"----"+path.substring(path.lastIndexOf("/")+1));
                    etFileName.setText(path.substring(path.lastIndexOf("/")+1));
                    btnSelectFile.setVisibility(View.GONE);
                    btnUploadFile.setVisibility(View.VISIBLE);
                    btnUploadFile.setEnabled(true);
                    tilFileName.setVisibility(View.VISIBLE);
                    tilFileDescription.setVisibility(View.VISIBLE);
                    etFileName.setVisibility(View.VISIBLE);
                    etFileDescription.setVisibility(View.VISIBLE);

                } else if (resultCode == RESULT_CANCELED) {
                    btnSelectFile.setVisibility(View.VISIBLE);
                    btnUploadFile.setVisibility(View.GONE);
                    Toast.makeText(this, "You cancelled the operation", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private boolean checkIfExistAndSaveFile(Intent data) {


        try {
            if (data != null) {
                Uri uri = data.getData();

                String path = PathUtils.getPath(this, uri);
                Log.e("PATH", path);
                Uri file = Uri.fromFile(new File(path));
                MimeTypeMap.getSingleton();
                String extension = MimeTypeMap.getFileExtensionFromUrl(file.toString());
                Log.e("Extension", extension);

                String newFileName = etFileName.getText().toString() + "." + extension;
                String fileDescription = etFileDescription.getText().toString();
                SaveAndUploadFile saveAndUploadFile = new SaveAndUploadFile(this, uri,
                        (FileType) spinnerFileType.getSelectedItem(), (String) spinnerDirectory.getSelectedItem(), newFileName, fileDescription, linearLayout);
                saveAndUploadFile.upload();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private class GetDir extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            utility.showProgressDialog(progressDialog);

        }

        @Override
        protected String doInBackground(String... strings) {
            String a;
            try {
                JSONObject json = restAPI.getDirectory();
                a = jsonParse.Parse(json);
            } catch (Exception e) {
                a = e.getMessage();
            }
            return a;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("GetDirResponseUpldFile", s);
            progressDialog.cancel();
            if (WebUtility.checkConnection(s)) {
                Pair<String, String> pair = WebUtility.GetErrorMessage(s);
                WebUtility.ShowAlertDialog(UploadFileActivity.this, pair.first, pair.second, false);
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String jsonValue = jsonObject.getString("status");
                    if (jsonValue.compareTo("no") == 0) {
                        Toast.makeText(UploadFileActivity.this, "SOMETHING WENT WRONG", Toast.LENGTH_LONG).show();
                    } else if (jsonValue.compareTo("ok") == 0) {
                        try {
                            JSONArray jsonArray = jsonObject.getJSONArray("Data");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonArrayObject = jsonArray.getJSONObject(i);
                                entity.setName(jsonArrayObject.getString("data1"));
                                directoryList.add(entity.getName());
                                entity = new Directory();
                            }
                            setUpSpinner(directoryList);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}