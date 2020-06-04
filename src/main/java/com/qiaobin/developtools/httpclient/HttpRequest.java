package com.qiaobin.developtools.httpclient;

import lombok.Data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author qiaobinwang@qq.com
 * @version 1.0.0
 * @date 2020-05-08 09:42
 */
@Data
public class HttpRequest {
    private List<Header> headers = new ArrayList<>();
    private String url;
    private int connectTimeout = 3000;
    private int readTimeout = 3000;
    private boolean verify = false;
    private HttpMethod requestMethod = HttpMethod.GET;
    private String charset = "UTF-8";
    private String body;

    public boolean hasHeader(String key) {
        for (Header header : headers) {
            if (header.getKey().equalsIgnoreCase(key)) {
                return true;
            }
        }
        return false;
    }

    public Header getHeader(String key) {
        for (Header header : headers) {
            if (header.getKey().equalsIgnoreCase(key)) {
                return header;
            }
        }
        return null;
    }

    public void setHeader(String key, String value) {
        Iterator<Header> iterator = headers.iterator();
        while (iterator.hasNext()) {
            Header next = iterator.next();
            if (next.getKey().equalsIgnoreCase(key)) {
                next.setValue(value);
                return;
            }
        }
        putHeader(key, value);
    }

    public void putHeader(String key, String value) {
        Header header = new Header();
        header.setValue(value);
        header.setKey(key);
        headers.add(header);
    }

    public void headers(Map<String, String> param) {
        headers.clear();
        param.forEach(this::putHeader);
    }
}
