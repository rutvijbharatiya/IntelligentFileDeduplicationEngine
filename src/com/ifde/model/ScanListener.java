package com.ifde.model;

public interface ScanListener {
    void onProgress(int percentage);
    void onLogMessage(String message);
}
