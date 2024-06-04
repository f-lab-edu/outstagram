package com.outstagram.outstagram.util;

import com.outstagram.outstagram.exception.ApiException;

import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256Util {
    public static final String ENCRYPTION_TYPE = "SHA-256";

    public static String encryptedPassword(String password) {
        String SHA;

        MessageDigest sh;
        try {
            sh = MessageDigest.getInstance(ENCRYPTION_TYPE);
            sh.update(password.getBytes());
            byte[] byteData = sh.digest();
            StringBuilder sb = new StringBuilder();
            for (byte byteDatum : byteData) {
                sb.append(Integer.toString((byteDatum & 0xff) + 0x100, 16).substring(1));
            }
            SHA = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new ApiException(ErrorCode.ALGORITHM_NOT_FOUND);
        }
        return SHA;
    }
}
