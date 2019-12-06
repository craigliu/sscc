package com.rtbasia.sscc;

public interface BaseOperation<E> {
    String key();
    E readFromDB(String key);
}
