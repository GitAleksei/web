package ru.netology;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class Conf {
    private static final String SETTINGS_FILE_NAME =
            "src/main/resources/conf.properties";

    public static int PORT;
    public static List<String> VALID_PATH;
    public static int NUMBER_OF_THREADS;
    public static int LIMIT_OF_HEADERS;

    static {
        Properties properties = new Properties();

        try (FileReader fileReader = new FileReader(SETTINGS_FILE_NAME)) {
            properties.load(fileReader);

            PORT = Integer.parseInt(properties.getProperty("PORT"));
            NUMBER_OF_THREADS = Integer.parseInt(properties.getProperty("NUMBER_OF_THREADS"));
            VALID_PATH = List.of(properties.getProperty("VALID_PATH").split(" "));
            LIMIT_OF_HEADERS = Integer.parseInt(properties.getProperty("LIMIT_OF_HEADERS"));

            Logger.INSTANCE.log("Read \"" + SETTINGS_FILE_NAME + "\"");
        } catch (IOException ex) {
            Logger.INSTANCE.log(Arrays.toString(ex.getStackTrace()) + " " + ex.getMessage());
        }
    }
}
