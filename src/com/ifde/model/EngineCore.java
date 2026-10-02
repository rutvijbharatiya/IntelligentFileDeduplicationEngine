package com.ifde.model;

import java.util.List;
import java.util.Scanner;
import java.io.*;
import java.nio.file.*;

public class EngineCore {

    private DuplicateDataStore dataStore;
    private ScanListener listener;
    private int totalFileCount = 0;
    private int totalFileScanned = 0;
    private volatile boolean isCancelled = false;

    public EngineCore(DuplicateDataStore dS, ScanListener listener) {
        this.dataStore = dS;
        this.listener = listener;
    }

    public void startScan(Path dir, List<String> types) {
        isCancelled = false;
        totalFileCount = 0;
        totalFileScanned = 0;

        countTotalFiles(dir, types);
        if (!this.isCancelled)
            scanDirectory(dir, types);

        if (listener != null) {
            if (isCancelled)
                listener.onLogMessage("Scanning stopped by user");
            else {
                listener.onProgress(100);
                listener.onLogMessage("Scanning Finished Successfully!");
            }
        }
    }

    public void scanDirectory(Path dir, List<String> types) {

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            listener.onLogMessage("Scanning " + dir.toString());

            for (Path entry : stream) {

                if (isCancelled)
                    break;

                if (Files.isSymbolicLink(entry))
                    continue;

                if (Files.isDirectory(entry))
                    scanDirectory(entry, types);

                else if (isValidType(entry, types)) {
                    String hash = HashGenerator.getHash(entry);

                    if (hash != null) {
                        this.dataStore.addFile(hash, entry);
                        this.totalFileScanned++;
                    }
                }

                if (totalFileCount > 0 && listener != null) {
                    int progress = (totalFileScanned * 100) / totalFileCount;
                    listener.onProgress(progress);
                }

            }
        } catch (IOException | DirectoryIteratorException e) {

            listener.onLogMessage("Skipped unreadable directory: " + dir.toString());

        }

    }

    private void countTotalFiles(Path dir, List<String> types) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {

            for (Path entry : stream) {

                if (isCancelled)
                    break;

                if (Files.isSymbolicLink(entry))
                    continue;

                if (Files.isDirectory(entry))
                    countTotalFiles(entry, types);

                else if (isValidType(entry, types)) {
                    this.totalFileCount++;
                }
            }
        } catch (IOException | DirectoryIteratorException e) {
            // Silently ignore during counting phase
        }
    }

    public boolean isValidType(Path file, List<String> typeList) {

        if (typeList == null || typeList.isEmpty())
            return true;

        String fileName = file.getFileName().toString().toLowerCase();
        for (String ext : typeList) {
            if (fileName.endsWith(ext.toLowerCase()))
                return true;
        }

        return false;
    }

    public void cancelScan() {
        this.isCancelled = true;
    }

    public int getTotalFilesScanned() {
        return this.totalFileScanned;
    }
}