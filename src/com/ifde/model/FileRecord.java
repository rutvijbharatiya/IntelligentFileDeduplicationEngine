package com.ifde.model;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

//This class is used to populate the File info table
public class FileRecord {

    private String name = null;
    private String hash = null;
    private Path path = null;
    private long sizeInBytes = 0;
    private String lastModified = null;

    public FileRecord(Path path) throws IOException {
        this.path = path;
        this.name = this.path.getFileName().toString();
        this.lastModified = DateFormatterHelper
                .formatFileTime(Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS));
        this.sizeInBytes = Files.size(path);
    }

    public static String formatSizeString(long bytes) {
        String[] units = { "B", "KB", "MB", "GB", "TB" };
        int unitIndex = 0;
        double sizeDouble = bytes;

        while (sizeDouble >= 1024 && unitIndex < units.length - 1) {
            sizeDouble = sizeDouble / 1024;
            unitIndex++;
        }

        return String.format("%.1f %s", sizeDouble, units[unitIndex]);
    }

    public String getName() {
        return name;
    }

    public Path getPath() {
        return path;
    }

    public String getLastModified() {
        return lastModified;
    }

    public long getSizeInBytes() {
        return sizeInBytes;
    }

    public String getSizeString() {
        return formatSizeString(this.sizeInBytes);
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getHash() {
        return hash;
    }

}

class DateFormatterHelper {

    public static String formatFileTime(FileTime date) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());

        return localDateTime.format(formatter);
    }
}