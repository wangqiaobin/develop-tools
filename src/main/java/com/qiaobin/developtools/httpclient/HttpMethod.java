package com.qiaobin.developtools.httpclient;

/**
 * @author qiaobinwang@qq.com
 * @version 1.0.0
 * @date 2020-05-07 18:59
 */
public enum HttpMethod {

    GET("GET"), POST("POST"), HEAD("HEAD"), OPTIONS("OPTIONS"), PUT("PUT"), DELETE("DELETE"), TRACE("TRACE"), PATCH("PATCH");
    private String method;

    HttpMethod(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }
}
