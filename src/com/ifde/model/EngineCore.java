package com.ifde.model;

import java.util.Scanner;
import java.io.*;
import java.nio.file.*;

public class EngineCore {

    private DuplicateDataStore dataStore;

    public EngineCore(DuplicateDataStore dS) {
        this.dataStore = dS;
    }

    public void scanDirectory(Path dir) {

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            System.out.println("\nScanning " + dir.toString() + " :");
            for (Path entry : stream) {

                if (Files.isDirectory(entry))
                    scanDirectory(entry);
                else {
                    String hash = HashGenerator.getHash(entry);

                    if (hash != null) {
                        this.dataStore.addFile(hash, entry);
                    }
                }
            }
        } catch (IOException e) {
            //Redirect output to controller
            System.out.println("Directory does not exist!");
        }

    }

    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);
        String targetDirec;

        System.out.print("Enter Directory to search : ");
        targetDirec = scan.nextLine();

        EngineCore eCore = new EngineCore(new DuplicateDataStore());
        eCore.scanDirectory(Path.of(targetDirec));

        eCore.dataStore.print();
        scan.close();
    }
}