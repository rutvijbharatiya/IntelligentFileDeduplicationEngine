package com.ifde.model;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.*;

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
            System.out.print("Can't read file at " + path.toString());
            return null;
        } catch (NoSuchAlgorithmException e) {
            System.out.println("SHA-256 algo isn't inside provider list");
            return null;
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
