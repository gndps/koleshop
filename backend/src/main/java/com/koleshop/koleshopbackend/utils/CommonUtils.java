package com.koleshop.koleshopbackend.utils;

import java.util.Random;

/**
 * Created by Gundeep on 30/09/15.
 */
public class CommonUtils {

    static final String DIGITS = "0123456789";
    static final String NON_ZERO_DIGITS = "123456789";
    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static Random rnd = new Random();

    public static String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    public static int generateOtp(int len) {
        StringBuilder sb = new StringBuilder(len);
        char firstDigit = NON_ZERO_DIGITS.charAt(rnd.nextInt(NON_ZERO_DIGITS.length()));
        sb.append(firstDigit);
        for (int i = 0; i < len-1; i++)
            sb.append(DIGITS.charAt(rnd.nextInt(DIGITS.length())));
        return Integer.parseInt(sb.toString().trim());
    }

}
