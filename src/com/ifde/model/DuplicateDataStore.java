package com.ifde.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

public class DuplicateDataStore {
    
    private HashMap<String, ArrayList<Path>> map;

    public DuplicateDataStore() {
        this.map = new HashMap<>();
    }
    
    public synchronized void addFile(String hash, Path path) {

        if(!map.containsKey(hash))
            map.put(hash, new ArrayList<>());
        map.get(hash).add(path);

    }

    public void print() {
        for (String hash : map.keySet()) {
            if(map.get(hash).size() <= 1)
                continue;
            System.out.println("Hash : " + hash);
            System.out.print("Paths : ");
            for (Path path : map.get(hash)) {
                System.out.print(" " + path.getFileName());
            }
            System.out.println("\n");
        }
    }

    public HashMap<String, ArrayList<Path>> getMap() {
        return this.map;
    }

}
