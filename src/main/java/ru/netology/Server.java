package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static final String GET = "GET";
    public static final String POST = "POST";
    private final ExecutorService threadPool;
    private final Map<String, Map<String, Handler>> handlers;

    public Server(int numberOfThreads) {
        Logger.INSTANCE.log("Init server");
        threadPool = Executors.newFixedThreadPool(numberOfThreads);
        handlers = new ConcurrentHashMap<>();
    }

    public void listen(int port) {
        Logger.INSTANCE.log("Server start to listen");
        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                final var socket = serverSocket.accept();
                threadPool.submit(() -> connectionHandling(socket));
            }
        } catch (IOException ex) {
            Logger.INSTANCE.log(Arrays.toString(ex.getStackTrace()) + " " + ex.getMessage());
        }
    }

    public void connectionHandling(Socket socket) {
        Logger.INSTANCE.log("Socket connected " + socket);

        try (final var in = new BufferedInputStream(socket.getInputStream());
             final var out = new BufferedOutputStream(socket.getOutputStream())) {

            // лимит на request line + заголовки

            in.mark(Conf.LIMIT_OF_HEADERS);
            final var buffer = new byte[Conf.LIMIT_OF_HEADERS];
            final var read = in.read(buffer);

            // ищем request line
            final var requestLineDelimiter = new byte[]{'\r', '\n'};
            final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
            if (requestLineEnd == -1) {
                badRequest(out);
                Logger.INSTANCE.log("Bad request line");
                return;
            }

            // читаем request line
            final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
            if (requestLine.length != 3) {
                badRequest(out);
                Logger.INSTANCE.log("Bad request line");
                return;
            }

            final var method = requestLine[0];
            if (!handlers.containsKey(method)) {
                badRequest(out);
                Logger.INSTANCE.log("Method not processed: " + method);
                return;
            }
            System.out.println(method);

            final var path = requestLine[1];
            if (!path.startsWith("/")) {
                badRequest(out);
                Logger.INSTANCE.log("Bad path");
                return;
            }
            System.out.println(path);

            // ищем заголовки
            final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
            final var headersStart = requestLineEnd + requestLineDelimiter.length;
            final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
            if (headersEnd == -1) {
                badRequest(out);
                return;
            }

            // отматываем на начало буфера
            in.reset();
            // пропускаем requestLine
            in.skip(headersStart);

            final var headersBytes = in.readNBytes(headersEnd - headersStart);
            final var headers = Arrays.asList(new String(headersBytes).split("\r\n"));
            System.out.println(headers);

            Request request;
            String body = null;
            // для GET тела нет
            if (!method.equals(GET)) {
                in.skip(headersDelimiter.length);
                // вычитываем Content-Length, чтобы прочитать body
                final var contentLength = extractHeader(headers, "Content-Length");
                if (contentLength.isPresent()) {
                    final var length = Integer.parseInt(contentLength.get());
                    final var bodyBytes = in.readNBytes(length);

                    body = new String(bodyBytes);
                    System.out.println(body);
                }
            }
            request = new Request(method, path, headers, body);

            Logger.INSTANCE.log(request.toString());

            if (!handlers.get(request.getMethod()).containsKey(request.getFilePath())) {
                badRequest(out);
            } else {
                handlers.get(request.getMethod()).get(request.getFilePath())
                        .handle(request, out);
            }
        } catch (IOException ex) {
            Logger.INSTANCE.log(Arrays.toString(ex.getStackTrace()) + " " + ex.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException ex) {
                Logger.INSTANCE.log(Arrays.toString(ex.getStackTrace()) + " " + ex.getMessage());
            }
        }
    }

    public void addHandler(String requestMethod, String filePath, Handler handler) {
        if (!handlers.containsKey(requestMethod)) {
            handlers.put(requestMethod, new ConcurrentHashMap<>());
        }

        handlers.get(requestMethod).put(filePath, handler);
    }

    public static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }

    private static void badRequest(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    // from google guava with modifications
    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}
