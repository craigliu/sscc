package com.rtbasia.sscc;

public class ReadTimeoutException extends Exception {
    public ReadTimeoutException(String key) {
        super(String.format("read key: %s timeout", key));
    }
}
