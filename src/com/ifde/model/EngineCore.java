package com.ifde.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.*;
import java.nio.file.*;

public class EngineCore {

    private DuplicateDataStore dataStore;
    private volatile boolean isCancelled = false;

    public EngineCore(DuplicateDataStore dS) {
        this.dataStore = dS;
    }

    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);
        String targetDirec, filetype;

        // Replace when controller is implemented
        System.out.print("Enter Directory to search : ");
        targetDirec = scan.nextLine();
        System.out.print("Enter file type (leave blank for all) : ");
        filetype = scan.nextLine();

        List<String> listType = new ArrayList<>();
        listType.add(filetype.toLowerCase());

        EngineCore eCore = new EngineCore(new DuplicateDataStore());
        eCore.scanDirectory(Path.of(targetDirec), listType);

        eCore.dataStore.print();
        scan.close();
    }

    public void scanDirectory(Path dir, List<String> types) {

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
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
                    }
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

}