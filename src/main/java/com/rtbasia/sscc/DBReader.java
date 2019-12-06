package com.rtbasia.sscc;

public interface DBReader<T> {
    T readFromDB(String key);
}
