package com.github.onedirection.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Simple HTTP class to provide an encode URL encoding
 */
public final class HTTP {

    private HTTP() {}

    private static final String ENCODING = "utf-8";

    public static String encode(String arg){
        try {
            return URLEncoder.encode(arg, ENCODING);
        } catch (UnsupportedEncodingException e) {
            // Should never happen
            throw new Error(e);
        }
    }
}
