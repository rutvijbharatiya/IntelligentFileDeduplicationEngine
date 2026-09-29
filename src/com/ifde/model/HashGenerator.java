package com.ifde.model;

import java.nio.charset.StandardCharsets;
import java.security.*;

public class HashGenerator {


    public static String getHash(byte[] data) {
        byte[] hashBytes = new byte[0];

        try {

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            hashBytes = md.digest(data);
            
        } catch (Exception e) {
            System.out.println("SHA-256 algo isn't inside provider list");
        }

        StringBuilder hexString = new StringBuilder();
        for(Byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
    }

}
