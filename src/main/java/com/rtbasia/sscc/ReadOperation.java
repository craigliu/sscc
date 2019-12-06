package com.rtbasia.sscc;

public interface ReadOperation<T> extends BaseOperation {
    T readFromCache(String key);
}
