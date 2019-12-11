package com.rtbasia.sscc;

public interface ReadOperation<T> extends BaseOperation {
    T readFromCache(String key);
    T readFromDB(String key);
    void setCache(String key, T obj);
}
