package com.ifde.model;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.security.*;

//Generates the Hash of a file
public class HashGenerator {

    public static String getHash(Path path) {

        byte[] hashBytes = new byte[0];

        try {

            MessageDigest md = MessageDigest.getInstance("SHA-256");

            try (FileChannel inChannel = FileChannel.open(path, StandardOpenOption.READ)) {

                ByteBuffer buff = ByteBuffer.allocate(64000);
                while (inChannel.read(buff) > 0) {
                    buff.flip();
                    md.update(buff);
                    buff.clear();
                }
            }
            hashBytes = md.digest();

            return bytesToHexString(hashBytes);
        } catch (IOException e) {
            return null;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm missing from Java runtime", e);
        }

    }

    private static String bytesToHexString(byte[] hashBytes) {
        StringBuilder hexString = new StringBuilder();
        
        for (Byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
    }

}
