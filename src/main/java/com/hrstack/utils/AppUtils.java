package com.hrstack.utils;

import java.security.SecureRandom;

public class AppUtils {
    private static final SecureRandom RANDOM = new SecureRandom();


    public static String generateOtp() {
        return String.valueOf(100000 + RANDOM.nextInt(900000));
    }

}
