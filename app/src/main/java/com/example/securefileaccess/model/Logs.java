package com.example.securefileaccess.model;

public class Logs {

    private int logId;
    private String employeeId, fileId, type, status, dateTime,employeeName,employeeDept;

    public Logs(int logId,String employeeId,String fileId,String type,String status,
                String dateTime,String employeeName,String employeeDept){
        this.logId = logId;
        this.employeeId = employeeId;
        this.fileId = fileId;
        this.type = type;
        this.status = status;
        this.dateTime = dateTime;
        this.employeeName = employeeName;
        this.employeeDept = employeeDept;

    }

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeDept() {
        return employeeDept;
    }

    public void setEmployeeDept(String employeeDept) {
        this.employeeDept = employeeDept;
    }
}
