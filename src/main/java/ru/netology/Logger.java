package ru.netology;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public enum Logger {
    INSTANCE;

    final String LOG_FILE_NAME = "log_server.txt";
    int count;

    Logger() {
        count = 1;
        try (FileWriter fileWriter = new FileWriter(LOG_FILE_NAME, true)) {
            fileWriter.write("\n");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    void log(String msg) {
        try (FileWriter fileWriter = new FileWriter(LOG_FILE_NAME, true)) {
            fileWriter.write(count++ + ". [" + new Date() + "] " + msg + "\n");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
