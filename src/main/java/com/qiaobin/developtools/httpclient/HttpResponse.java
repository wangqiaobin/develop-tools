package com.qiaobin.developtools.httpclient;

import lombok.Data;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author qiaobinwang@qq.com
 * @version 1.0.0
 * @date 2020-05-07 19:33
 */
@Data
public class HttpResponse {

    private static final String[] type = new String[0];

    private int status;
    private Header[] headers;
    private ByteArrayOutputStream stream;

    public String getHeaderValue(String key) {
        if (headers == null || headers.length == 0) {
            return null;
        }
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].getKey().equalsIgnoreCase(key)) {
                return headers[i].getValue();
            }
        }
        return null;
    }

    public String[] getHeaderValues(String key) {
        if (headers == null || headers.length == 0) {
            return type;
        }
        List<String> result = new ArrayList<>();
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].getKey().equalsIgnoreCase(key)) {
                result.add(headers[i].getValue());
            }
        }
        return result.toArray(type);
    }
}
