package com.qiaobin.developtools.httpclient;

import lombok.Data;
import org.springframework.util.StringUtils;

import javax.net.ssl.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * @author qiaobinwang@qq.com
 * @version 1.0.0
 * @date 2020-05-07 18:46
 */
public class HttpClient {
    private static final String DEFAULT_UPLOAD_TYPE = "application/octet-stream";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String MULTIPART_FORM_DATA = "multipart/form-data";
    private static final String FORM_URL_ENCODED = "application/x-www-form-urlencoded";
    private static final char[] BOUNDARY_CHARS = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final int BOUNDARY_LENGTH = 32;
    private List<Body> body = new ArrayList<>();
    private HttpRequest request = new HttpRequest();
    private Proxy proxy = Proxy.NO_PROXY;
    private static final Header[] type = new Header[0];

    @Data
    public static class Body {
        private String key;
        private String value;
        private InputStream stream;
        private String contentType;

        public boolean hasStream() {
            return this.stream != null;
        }

        public static Body build(String key, String value) {
            Body body = new Body();
            body.setKey(key);
            body.setValue(value);
            return body;
        }

        public static Body build(String key, String filename, InputStream stream) {
            Body body = build(key, filename);
            body.setStream(stream);
            return body;
        }

        public static Body build(String key, String filename, InputStream stream, String contentType) {
            Body body = build(key, filename, stream);
            body.setContentType(contentType);
            return body;
        }
    }


    public HttpClient connection(String url) {
        request.setUrl(url);
        return this;
    }

    public HttpClient verify(boolean https) {
        request.setVerify(https);
        return this;
    }

    public HttpClient body(String body) {
        request.setBody(body);
        return this;
    }


    public HttpClient body(String key, String value) {
        body.add(Body.build(key, value));
        return this;
    }

    public HttpClient body(String key, String value, InputStream stream) {
        body.add(Body.build(key, value, stream));
        return this;
    }

    public HttpClient body(String key, String value, InputStream stream, String contentType) {
        body.add(Body.build(key, value, stream, contentType));
        return this;
    }

    public HttpClient proxy(String host, Integer port) {
        if (!StringUtils.isEmpty(host)) {
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
        }
        return this;
    }

    public HttpClient proxy(SocketAddress address) {
        if (address != null) {
            proxy = new Proxy(Proxy.Type.HTTP, address);
        }
        return this;
    }

    public HttpClient setHeader(String key, String value) {
        request.setHeader(key, value);
        return this;
    }

    public HttpClient putHeader(String key, String value) {
        request.putHeader(key, value);
        return this;
    }

    public HttpClient cookie(Map<String, String> param) {
        StringBuilder builder = new StringBuilder();
        param.forEach((key, value) -> {
            builder.append(key);
            builder.append("=");
            builder.append(value);
            builder.append(";");
        });
        setHeader("Cookie", builder.toString());
        return this;
    }

    public HttpClient setCookie(String cookie) {
        request.setHeader("Cookie", cookie);
        return this;
    }

    public HttpClient setCookie(String key, String value) {
        Header cookie = request.getHeader("Cookie");
        if (cookie == null) {
            setCookie(key + "=" + value + ";");
            return this;
        }
        setCookie(cookie.getValue() + key + "=" + value + ";");
        return this;
    }


    public HttpClient header(Map<String, String> param) {
        request.headers(param);
        return this;
    }

    public HttpClient connectTimeout(int timeout) {
        request.setConnectTimeout(timeout);
        return this;
    }

    public HttpClient readTimeout(int timeout) {
        request.setReadTimeout(timeout);
        return this;
    }

    public HttpClient requestMethod(HttpMethod method) {
        request.setRequestMethod(method);
        return this;
    }

    private URLConnection connection() {
        try {
            URLConnection connection = new URL(request.getUrl()).openConnection(proxy);
            if (request.isVerify()) {
                TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                return null;
                            }

                            public void checkClientTrusted(
                                    java.security.cert.X509Certificate[] certs, String authType) {
                            }

                            public void checkServerTrusted(
                                    java.security.cert.X509Certificate[] certs, String authType) {
                            }
                        }
                };
                SSLContext sc = null;
                try {
                    sc = SSLContext.getInstance("TLSv1.2");
                    sc.init(null, trustAllCerts, new java.security.SecureRandom());
                } catch (NoSuchAlgorithmException | KeyManagementException e) {
                    e.printStackTrace();
                }
                ((HttpsURLConnection) connection).setSSLSocketFactory(sc.getSocketFactory());
                HostnameVerifier hv = new HostnameVerifier() {
                    @Override
                    public boolean verify(String urlHostName, SSLSession session) {
                        return true;
                    }
                };
                ((HttpsURLConnection) connection).setHostnameVerifier(hv);
            }
            return connection;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String encodeName(String val) {
        if (val == null)
            return null;
        return val.replace("\"", "%22");
    }

    private static boolean needsMultipart(List<Body> body) {
        for (Body data : body) {
            if (data.hasStream()) {
                return true;
            }
        }
        return false;
    }

    static String bound() {
        final StringBuilder mime = new StringBuilder();
        final Random rand = new Random();
        for (int i = 0; i < BOUNDARY_LENGTH; i++) {
            mime.append(BOUNDARY_CHARS[rand.nextInt(BOUNDARY_CHARS.length)]);
        }
        return mime.toString();
    }

    private String setOutputContentType(HttpRequest request) {
        String bound = null;
        if (request.hasHeader(CONTENT_TYPE)) {
            Header header = request.getHeader(CONTENT_TYPE);
            if (header.getValue().contains(CONTENT_TYPE) && header.getValue().contains(MULTIPART_FORM_DATA)) {
                bound = bound();
                request.setHeader(CONTENT_TYPE, MULTIPART_FORM_DATA + "; boundary=" + bound);
            }
        } else if (needsMultipart(body)) {
            bound = bound();
            request.setHeader(CONTENT_TYPE, MULTIPART_FORM_DATA + "; boundary=" + bound);
        } else {
            request.setHeader(CONTENT_TYPE, FORM_URL_ENCODED + "; charset=" + request.getCharset());
        }
        return bound;
    }

    private static void send(final InputStream in, final OutputStream out) throws IOException {
        final byte[] buffer = new byte[1024 * 32];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
    }

    private void write(OutputStream stream, String bound) throws IOException {
        final Collection<Body> body = this.body;
        if (Objects.nonNull(bound)) {
            for (Body data : body) {
                stream.write("--".getBytes(Charset.forName(request.getCharset())));
                stream.write(bound.getBytes(Charset.forName(request.getCharset())));
                stream.write("\r\n".getBytes(Charset.forName(request.getCharset())));
                stream.write("Content-Disposition: form-data; name=\"".getBytes(request.getCharset()));
                stream.write(encodeName(data.getKey()).getBytes(request.getCharset())); // encodes " to %22
                stream.write("\"".getBytes(request.getCharset()));
                if (data.hasStream()) {
                    stream.write("; filename=\"".getBytes(request.getCharset()));
                    stream.write(encodeName(data.getValue()).getBytes(request.getCharset()));
                    stream.write("\"\r\nContent-Type: ".getBytes(request.getCharset()));
                    stream.write(data.getContentType() != null ? data.getContentType().getBytes(request.getCharset()) : DEFAULT_UPLOAD_TYPE.getBytes(request.getCharset()));
                    stream.write("\r\n\r\n".getBytes(request.getCharset()));
                    send(data.stream, stream);
                } else {
                    stream.write("\r\n\r\n".getBytes(request.getCharset()));
                    stream.write(data.getValue().getBytes(request.getCharset()));
                }
                stream.write("\r\n".getBytes(request.getCharset()));
            }
            stream.write("--".getBytes(request.getCharset()));
            stream.write(bound.getBytes(request.getCharset()));
            stream.write("--".getBytes(request.getCharset()));
        } else if (request.getBody() != null) {
            stream.write(request.getBody().getBytes(request.getCharset()));
        } else {
            boolean first = true;
            Iterator<Body> iterator = body.iterator();
            while (iterator.hasNext()) {
                Body next = iterator.next();
                if (!first)
                    stream.write('&');
                else
                    first = false;
                stream.write(URLEncoder.encode(next.getKey(), request.getCharset()).getBytes(request.getCharset()));
                stream.write('=');
                stream.write(URLEncoder.encode(next.getValue(), request.getCharset()).getBytes(request.getCharset()));
            }
        }
    }

    private ByteArrayOutputStream read(InputStream input) throws IOException {
        if (input == null) {
            return null;
        }
        byte[] buffer = new byte[32];
        int length;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        while ((length = input.read(buffer)) > 0) {
            output.write(buffer, 0, length);
        }
        return output;
    }

    private boolean isGzip(final Map<String, List<String>> fields) {
        if (Objects.isNull(fields)) {
            return false;
        }
        for (String key : fields.keySet()) {
            if ("Content-Encoding".equalsIgnoreCase(key) && fields.get(key).contains("gzip")) {
                return true;
            }
        }
        return false;
    }

    public HttpResponse execute() {
        HttpURLConnection connection = (HttpURLConnection) connection();
        HttpResponse response = new HttpResponse();
        try {
            assert connection != null;
            connection.setReadTimeout(request.getReadTimeout());
            connection.setConnectTimeout(request.getReadTimeout());
            //connection.setUseCaches(false);
            //connection.setInstanceFollowRedirects(true);
            String bound = setOutputContentType(request);
            request.getHeaders().forEach(header -> {
                connection.addRequestProperty(header.getKey(), header.getValue());
            });
            switch (request.getRequestMethod()) {
                case POST:
                case PUT:
                case PATCH:
                    connection.setDoOutput(true);
                    connection.setUseCaches(false);
                    break;
                default:
                    connection.setDoOutput(false);
            }
            connection.setDoInput(true);
            connection.setRequestMethod(request.getRequestMethod().getMethod());
            connection.connect();
            if (connection.getDoOutput()) {
                write(connection.getOutputStream(), bound);
                connection.getOutputStream().flush();
            }
            int status = connection.getResponseCode();
            InputStream stream;
            if (status == 200) {
                stream = connection.getInputStream();
            } else {
                stream = connection.getErrorStream();
            }
            if (isGzip(connection.getHeaderFields())) {
                stream = new GZIPInputStream(stream);
            }
            response.setStream(read(stream));
            response.setStatus(status);
            Map<String, List<String>> fields = connection.getHeaderFields();
            if (Objects.nonNull(fields)) {
                List<Header> headers = new ArrayList<>();
                fields.forEach((key, values) -> {
                    if (Objects.isNull(key)) {
                        return;
                    }
                    for (int i = 0; i < values.size(); i++) {
                        Header header = new Header();
                        header.setKey(key);
                        header.setValue(values.get(i));
                        headers.add(header);
                    }
                });
                response.setHeaders(headers.toArray(type));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            destructor(connection);
        }
        return response;
    }

    private void destructor(HttpURLConnection connection) {
        try {
            if (connection.getDoInput()) {
                InputStream inputStream = connection.getInputStream();
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } catch (Exception ignored) {
        }
        try {
            if (connection.getDoOutput()) {
                OutputStream outputStream = connection.getOutputStream();
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (Exception ignored) {
        }
        try {
            InputStream errorStream = connection.getErrorStream();
            if (errorStream != null) {
                errorStream.close();
            }
        } catch (Exception ignored) {
        }
        connection.disconnect();
    }
}
