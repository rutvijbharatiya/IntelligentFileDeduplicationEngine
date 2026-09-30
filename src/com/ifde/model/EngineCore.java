package com.ifde.model;

import java.util.ArrayList;
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

    // TODO: CONTROLLER - This main() method is just for CLI testing.
    // You need to instantiate EngineCore in your Controller class,
    // implement the ScanListener interface, and pass yourself into the EngineCore
    // constructor.
    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);
        String targetDirec, filetype;

        System.out.print("Enter Directory to search : ");
        targetDirec = scan.nextLine();
        System.out.print("Enter file type (leave blank for all) : ");
        filetype = scan.nextLine();

        List<String> listType = new ArrayList<>();
        listType.add(filetype.toLowerCase());

        EngineCore eCore = new EngineCore(new DuplicateDataStore(), null);
        eCore.countTotalFiles(Path.of(targetDirec), listType);
        eCore.scanDirectory(Path.of(targetDirec), listType);

        eCore.dataStore.print();
        scan.close();
    }

    public void scanDirectory(Path dir, List<String> types) {

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            // TODO: CONTROLLER - Replace this System.out.println with
            // listener.onLogMessage() so it prints to the UI's scrolling text box!

            System.out.println("Scanning " + dir.toString() + "...");

            for (Path entry : stream) {

                if (isCancelled)
                    break;

                if (Files.isDirectory(entry))
                    scanDirectory(entry, types);

                else if (isValidType(entry, types)) {
                    String hash = HashGenerator.getHash(entry);

                    if (hash != null) {
                        this.dataStore.addFile(hash, entry);
                        this.totalFileScanned++;
                    }
                }

                // TODO: CONTROLLER - Uncomment listener.onProgress(progress) so the green bar moves!
                if (totalFileCount > 0 && listener != null) {
                    int progress = (totalFileScanned * 100) / totalFileCount;
                    // listener.onProgress(progress); 
                }

            }
        } catch (IOException | DirectoryIteratorException e) {

            // Redirect output to Controller
            System.out.println("Skipped unreadable directory: " + dir.toString());

        }

    }

    private void countTotalFiles(Path dir, List<String> types) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {

            for (Path entry : stream) {

                if (isCancelled)
                    break;

                if (Files.isDirectory(entry))
                    countTotalFiles(entry, types);

                else if (isValidType(entry, types)) {
                    this.totalFileCount++;
                }
            }
        } catch (IOException | DirectoryIteratorException e) {

            // Redirect output to Controller
            System.out.println("Skipped unreadable directory: " + dir.toString());

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