package com.aini365.util;

import java.util.regex.Pattern;

public class InputCheckUtil {
    public static boolean isIp(String input){
        String pattern = "^((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)$";
        return Pattern.matches(pattern, input);
    }

    public static boolean isInterger(String input){
        String pattern = "^[1-9]\\d*$";
        return Pattern.matches(pattern, input);
    }
}
