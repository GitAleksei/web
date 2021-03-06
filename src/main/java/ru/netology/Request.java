package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class Request {
    private final String method;
    private final String filePath;
    private final List<String> headers;
    private final String body;
    private List<NameValuePair> queryParams;

    public Request(String method, String filePath, List<String> headers, String body) {
        this.method = method;
        this.headers = headers;
        this.body = body;

        int indexOfStart = filePath.indexOf('?');
        if (indexOfStart == -1) {
            this.filePath = filePath;
        } else {
            this.filePath = filePath.substring(0, indexOfStart);
            queryParams = URLEncodedUtils.parse(filePath.substring(indexOfStart + 1),
                    StandardCharsets.UTF_8);
        }

        if (method.equals("POST")) {
            final var contentType = Server.extractHeader(headers, "Content-Type");
            if (contentType.isPresent()) {
                if (contentType.get().equals("application/x-www-form-urlencoded")) {
                    queryParams = URLEncodedUtils.parse(body, StandardCharsets.UTF_8);
                }
            }
        }
    }

    public String getMethod() {
        return method;
    }

    public String getFilePath() {
        return filePath;
    }

    public List<NameValuePair> getQueryParams() {
        return queryParams;
    }

    public String getFirstQueryParam(String name) {
        for (NameValuePair nameValuePair : queryParams) {
            if (nameValuePair.getName().equals(name)) {
                return nameValuePair.getValue();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Request{" +
                "method='" + method + '\'' +
                ", filePath='" + filePath + '\''+
                ", queryParams=" + queryParams +
                '}';
    }
}
