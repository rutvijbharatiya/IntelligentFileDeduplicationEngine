package com.ifde.model;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DuplicateDataStore {

    private HashMap<String, ArrayList<FileRecord>> map;

    public DuplicateDataStore() {
        this.map = new HashMap<>();
    }

    public synchronized void addFile(String hash, Path path) {

        if (!map.containsKey(hash))
            map.put(hash, new ArrayList<>());
        try {
            map.get(hash).add(new FileRecord(path));
        } catch (IOException e) {
            System.out.println("I/O error: " + e.getMessage());
        }

    }

    public void print() {

        // Redirect output to controller
        for (String hash : map.keySet()) {
            if (map.get(hash).size() <= 1)
                continue;
            System.out.println("Hash : " + hash);
            System.out.print("Paths : ");
            for (FileRecord file : map.get(hash)) {
                System.out.print(" " + file.getName() + " " + file.getSizeString() + " " + file.getLastModified());
            }
            System.out.println("\n");
        }
    }

    public void deleteFiles(List<FileRecord> files) {

        for (FileRecord file : files) {
            try {
                Files.delete(file.getPath());
                String hash = file.getHash();

                if(map.containsKey(hash)) {
                    map.get(hash).remove(file);

                    if(map.get(hash).size() == 0)
                        map.remove(hash);
                }
            }
            // Replace when controller is implemented
            catch (NoSuchFileException e) {
                System.out.println("File doesn't exist at path " + file.toString());
            } catch (FileSystemException e) {
                System.out.println("File is locked by another process: " + file.toString());
            } catch (IOException e) {
                System.out.println("I/O error: " + e.getMessage());
            }
        }
    }

    public HashMap<String, ArrayList<FileRecord>> getMap() {
        return this.map;
    }

}
