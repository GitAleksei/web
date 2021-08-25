package ru.netology;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static ru.netology.Conf.*;

public class Main {
  public static void main(String[] args) {
    Server server = new Server(NUMBER_OF_THREADS);

    for (String validPath : VALID_PATH) {
      if (validPath.equals("/classic.html")) {
        server.addHandler("GET", "/classic.html", (request, responseStream) -> {
          final var filePath = Path.of(".", "public", request.getFilePath());
          final var mimeType = Files.probeContentType(filePath);
          final var template = Files.readString(filePath);
          final var content = template.replace(
                  "{time}",
                  LocalDateTime.now().toString()
          ).getBytes();
          responseStream.write((
                  "HTTP/1.1 200 OK\r\n" +
                          "Content-Type: " + mimeType + "\r\n" +
                          "Content-Length: " + content.length + "\r\n" +
                          "Connection: close\r\n" +
                          "\r\n"
          ).getBytes());
          responseStream.write(content);
          responseStream.flush();
        });
      } else {
        server.addHandler("GET", validPath, (request, responseStream) -> {
          final var filePath = Path.of(".", "public", request.getFilePath());
          final var mimeType = Files.probeContentType(filePath);
          final var length = Files.size(filePath);
          responseStream.write((
                  "HTTP/1.1 200 OK\r\n" +
                          "Content-Type: " + mimeType + "\r\n" +
                          "Content-Length: " + length + "\r\n" +
                          "Connection: close\r\n" +
                          "\r\n"
          ).getBytes());
          Files.copy(filePath, responseStream);
          responseStream.flush();
        });
      }
    }

    server.listen(PORT);
  }
}


