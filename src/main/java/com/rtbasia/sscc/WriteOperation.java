package com.rtbasia.sscc;

public interface WriteOperation<E> extends BaseOperation {
    void writeDB();
    void clearCache(String key);
}
