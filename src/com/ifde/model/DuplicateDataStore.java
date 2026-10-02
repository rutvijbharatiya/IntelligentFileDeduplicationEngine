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
            FileRecord rec = new FileRecord(path);
            rec.setHash(hash);
            map.get(hash).add(rec);
        } catch (IOException e) {
            System.out.println("I/O error: " + e.getMessage());
        }

    }

    public HashMap<String, ArrayList<FileRecord>> getMap() {
        return this.map;
    }

    public void deleteFiles(List<FileRecord> files) {

        for (FileRecord file : files) {
            try {
                Files.delete(file.getPath());
                String hash = file.getHash();

                if (map.containsKey(hash)) {
                    map.get(hash).remove(file);

                    if (map.get(hash).size() == 0)
                        map.remove(hash);
                }
            }
            // TODO: CONTROLLER - Instead of just printing this, we should
            // probably throw an exception here or return a list of failed files
            // so you can show a Java Swing warning popup to the user!

            catch (NoSuchFileException e) {
                System.out.println("File doesn't exist at path " + file.toString());
            } catch (FileSystemException e) {
                System.out.println("File is locked by another process: " + file.toString());
            } catch (IOException e) {
                System.out.println("I/O error: " + e.getMessage());
            }
        }
    }

    public long calcWastedSpace() {

        long wasteSize = 0;

        for (ArrayList<FileRecord> files : this.map.values()) {
            if (files.size() > 1)
                wasteSize += (files.getFirst().getSizeInBytes() * (files.size() - 1));
        }

        return wasteSize;
    }

}
