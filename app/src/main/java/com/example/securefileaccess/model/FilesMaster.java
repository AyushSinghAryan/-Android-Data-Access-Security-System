package com.example.securefileaccess.model;

import java.io.Serializable;

public class FilesMaster implements Serializable {


//    String adminId, String fileType, String fileName, String fileSize,
//    String fileDesc, String fileDir, String encryptionTime, String dateTime

    private int fileId;
    private String adminId,
            fileType, fileName, fileSize, fileDescription, fileDirectory, encryptionTime, dateTime;

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileDescription() {
        return fileDescription;
    }

    public void setFileDescription(String fileDescription) {
        this.fileDescription = fileDescription;
    }

    public String getFileDirectory() {
        return fileDirectory;
    }

    public void setFileDirectory(String fileDirectory) {
        this.fileDirectory = fileDirectory;
    }

    public String getEncryptionTime() {
        return encryptionTime;
    }

    public void setEncryptionTime(String encryptionTime) {
        this.encryptionTime = encryptionTime;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
