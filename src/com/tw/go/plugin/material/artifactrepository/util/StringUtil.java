package com.tw.go.plugin.material.artifactrepository.util;

public class StringUtil {
    public static boolean isBlank(String string) {
        return string == null || string.trim().isEmpty();
    }

    public static Boolean isNotBlank(String string) {
        return !isBlank(string);
    }
}
