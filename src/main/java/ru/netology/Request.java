package ru.netology;

import java.io.BufferedReader;

public class Request {
    private final String method;
    private final String filePath;
    private final String headlines;
    private final BufferedReader body;

    public Request(String method, String filePath, String headlines, BufferedReader body) {
        this.method = method;
        this.filePath = filePath;
        this.headlines = headlines;
        this.body = body;
    }

    public String getMethod() {
        return method;
    }

    public String getFilePath() {
        return filePath;
    }
}
