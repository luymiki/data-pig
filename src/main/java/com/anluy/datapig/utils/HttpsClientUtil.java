package com.anluy.datapig.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Map;

/**
 * https请求工具类
 * hc.zeng
 */
public class HttpsClientUtil {


    /**
     * 发送http的json请求并返回的结果字符串
     *
     * @param url
     * @param params
     * @param charset
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public static ResponseEntity jsonGet(String url, Object params, Charset charset) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        return jsonGet(url, params, null, charset);
    }

    /**
     * 发送http的json请求并返回的结果字符串
     *
     * @param url
     * @param params
     * @param headers
     * @param charset
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public static ResponseEntity jsonGet(String url, Object params, Map<String, String> headers, Charset charset) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        return request(url, HttpMethod.GET, params, headers, MediaType.APPLICATION_JSON, charset);
    }

    /**
     * 发送http的json请求并返回的结果字符串
     *
     * @param url
     * @param params
     * @param charset
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public static ResponseEntity jsonPost(String url, Object params, Charset charset) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        return jsonPost(url, params, null, charset);
    }

    /**
     * 发送http的json请求并返回的结果字符串
     *
     * @param url
     * @param params
     * @param headers
     * @param charset
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public static ResponseEntity jsonPost(String url, Object params, Map<String, String> headers, Charset charset) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        return request(url, HttpMethod.POST, params, headers, MediaType.APPLICATION_JSON, charset);
    }

    /**
     * 发送http请求并返回的结果字符串
     *
     * @param url
     * @param params
     * @param charset
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public static ResponseEntity get(String url, Object params, MediaType mediaType, Charset charset) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        return get(url, params, null, mediaType, charset);
    }

    /**
     * 发送http请求并返回的结果字符串
     *
     * @param url
     * @param params
     * @param headers
     * @param charset
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public static ResponseEntity get(String url, Object params, Map<String, String> headers, MediaType mediaType, Charset charset) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        return request(url, HttpMethod.GET, params, headers, mediaType, charset);
    }

    /**
     * 发送http请求并返回的结果字符串
     *
     * @param url
     * @param params
     * @param charset
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public static ResponseEntity post(String url, Object params, MediaType mediaType, Charset charset) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        return post(url, params, null, mediaType, charset);
    }

    /**
     * 发送http请求并返回的结果字符串
     *
     * @param url
     * @param params
     * @param headers
     * @param charset
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public static ResponseEntity post(String url, Object params, Map<String, String> headers, MediaType mediaType, Charset charset) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        return request(url, HttpMethod.POST, params, headers, mediaType, charset);
    }

    /**
     * 请求http并将返回的结果转化为json
     *
     * @param url
     * @param params
     * @param headers
     * @return
     * @throws Exception
     */
    public static ResponseEntity request(String url, HttpMethod httpMethod, Object params, Map<String, String> headers, MediaType mediaType, Charset charset) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        HttpURLConnection connection = null;
        String paramsStr = null;
        if (params != null) {
            StringBuffer sb = new StringBuffer();
            if (params instanceof Map) {
                Map<String, String> paramMap = (Map<String, String>) params;
                Iterator<String> it = paramMap.keySet().iterator();
                for (int i = 0; it.hasNext(); i++) {
                    String k = it.next();
                    if (i > 0) {
                        sb.append("&");
                    }
                    sb.append(k).append("=").append(URLEncoder.encode(paramMap.get(k), charset.name()));
                }
            } else if (params instanceof String) {
                switch (mediaType.toString()){
                    case MediaType.APPLICATION_JSON_VALUE:
                    case MediaType.APPLICATION_JSON_UTF8_VALUE:{
                        sb.append(params);
                        break;
                    }
                    default:{
                        sb.append(URLEncoder.encode((String) params, charset.name()));
                        break;
                    }
                }

            } else {
                sb.append(params);
            }
            paramsStr = sb.toString();
        }
        switch (httpMethod) {
            case GET: {
                String url_ = url;
                if (StringUtils.isNotBlank(paramsStr)) {
                    url_ += "?" + paramsStr;
                }
                connection = HttpsClientUtil.getConnection(url_, httpMethod, headers, mediaType, charset);
                break;
            }
            case POST: {
                connection = HttpsClientUtil.getConnection(url, httpMethod, headers, mediaType, charset);
                if (StringUtils.isNotBlank(paramsStr)) {
                    DataOutputStream dops = new DataOutputStream(connection.getOutputStream());
                    dops.writeBytes(paramsStr);
                    dops.flush();
                    dops.close();
                }
                break;
            }
            default: {

                break;
            }
        }
        if (connection == null) {
            throw new RuntimeException("Not Support HttpMethod.{get,post}");
        }

        return printResponseData(connection, charset);
    }


    /**
     * 获取http连接
     *
     * @param urlStr
     * @param headers
     * @param mediaType
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    private static HttpURLConnection getConnection(String urlStr, HttpMethod httpMethod, Map<String, String> headers, MediaType mediaType, Charset charset) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        nullHostNameVerifier();
        URL url = new URL(urlStr);
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        urlConn.setDoInput(true);
        urlConn.setDoOutput(true);
        urlConn.setRequestMethod(httpMethod.name());
        urlConn.setUseCaches(true);
        urlConn.setInstanceFollowRedirects(true);
        urlConn.setRequestProperty("Content-Type", mediaType.toString() + ";charset=" + charset.name());
        if (headers != null && !headers.isEmpty()) {
            headers.forEach((k, v) -> {
                urlConn.setRequestProperty(k, v);
            });
        }
        urlConn.connect();
        return urlConn;

    }

    /**
     * 解析流
     *
     * @param urlConn
     * @return
     * @throws IOException
     */
    private static ResponseEntity printResponseData(HttpURLConnection urlConn, Charset charset) throws IOException {
        int code = urlConn.getResponseCode();
        HttpStatus status = HttpStatus.valueOf(code);
        InputStream is = null;
        //请求不成功读取错误流
        switch (status) {
            case OK: {
                is = urlConn.getInputStream();
                break;
            }
            default: {
                is = urlConn.getErrorStream();
                break;
            }
        }
        if (is == null) {
            throw new RuntimeException(urlConn.getURL().toString() + " " + urlConn.getResponseMessage() + "(" + code + ")");
        }
        // 读取请求发返回信息
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset.name()));
        StringBuilder sb = new StringBuilder();
        String line = null;
//        while(!reader.ready()){
//            try {
//                Thread.sleep(1000L);
//            } catch (InterruptedException e) {
//                throw new RuntimeException("等待连接异常",e);
//            }
//        }
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        urlConn.disconnect();

        return new ResponseEntity(sb.toString(), status);
    }

    /**
     * https请求绕过验证
     *
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public static void nullHostNameVerifier() throws NoSuchAlgorithmException, KeyManagementException {
        HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustManagers, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }


    static TrustManager[] trustManagers = new TrustManager[]{new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }};

    public static class NullHostNameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }
    }
}

