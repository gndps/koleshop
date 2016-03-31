package com.koleshop.koleshopbackend.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

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
        for (int i = 0; i < len - 1; i++)
            sb.append(DIGITS.charAt(rnd.nextInt(DIGITS.length())));
        return Integer.parseInt(sb.toString().trim());
    }

    public static String getDateTimeDdMmYy(String timezoneString) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("IST"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
        TimeZone istTimeZone = TimeZone.getTimeZone(timezoneString);
        Date d = new Date();
        sdf.setTimeZone(istTimeZone);
        String strtime = sdf.format(d);
        return strtime;
    }

    public static Date getDate(Date date, int addMinutes, int addHours) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MINUTE, addMinutes);
        cal.add(Calendar.HOUR, addHours);
        return cal.getTime();
    }

    public static String getDigestedString(String digString) {
        try {
            byte[] bytesOfMessage = new byte[0];
            try {
                bytesOfMessage = digString.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            MessageDigest md = null;
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            byte[] thedigest = md.digest(bytesOfMessage);
            return thedigest.toString();
        } catch (Exception e) {
            return randomString(3);
        }
    }

    public static Date addMinutesToDate(Date date, int addMinutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, addMinutes);
        return calendar.getTime();
    }

    public static String getQueryQuestionMarks(List<Long> arrayList) {
        if (arrayList != null || arrayList.size() > 0) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < arrayList.size(); i++) {
                builder.append("?,");
            }
            return builder.deleteCharAt(builder.length() - 1).toString();
        } else {
            return null;
        }
    }

}
