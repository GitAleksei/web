package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
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

        try (final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             final var out = new BufferedOutputStream(socket.getOutputStream())) {

            // read only request line for simplicity
            // must be in form GET /path HTTP/1.1
            var requestLine = in.readLine();
            final var parts = requestLine.split(" ");

            if (parts.length != 3) {
                // just close socket
                return;
            }

            final var path = parts[1];
            final var requestMethod = parts[0];
            StringBuilder stringBuilder = new StringBuilder();
            while (!(requestLine = in.readLine()).equals("")) {
                stringBuilder.append(requestLine);
            }
            final var requestHeadlines = stringBuilder.toString();
            Request request = new Request(requestMethod, path, requestHeadlines, in);
            Logger.INSTANCE.log(request.toString());

            if (!handlers.containsKey(request.getMethod())) {
                sendMethodNotAllowed(out);
            } else if (!handlers.get(request.getMethod()).containsKey(request.getFilePath())) {
                sendNotFound(out);
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

    private void sendNotFound(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    private void sendMethodNotAllowed(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 405 Method Not Allowed\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    public void addHandler(String requestMethod, String filePath, Handler handler) {
        if (!handlers.containsKey(requestMethod)) {
            handlers.put(requestMethod, new ConcurrentHashMap<>());
        }

        handlers.get(requestMethod).put(filePath, handler);
    }
}
