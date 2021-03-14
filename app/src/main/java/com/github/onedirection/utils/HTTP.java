package com.github.onedirection.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public final class HTTP {

    private HTTP() {}

    private static final String ENCODING = "utf-8";

    public static String encode(String arg){
        try {
            return URLEncoder.encode(arg, ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }
}
