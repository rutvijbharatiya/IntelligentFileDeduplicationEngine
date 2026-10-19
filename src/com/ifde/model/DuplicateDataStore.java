package com.ifde.model;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

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

    public List<FileRecord> deleteFiles(List<FileRecord> files) {
        List<FileRecord> failed = new ArrayList<>();

        for (FileRecord file : files) {
            try {
                Files.delete(file.getPath());
                String hash = file.getHash();

                if (map.containsKey(hash)) {
                    map.get(hash).remove(file);

                    if (map.get(hash).size() == 0)
                        map.remove(hash);
                }
            } catch (IOException e) {
                failed.add(file);
            }
        }
        return failed;
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
