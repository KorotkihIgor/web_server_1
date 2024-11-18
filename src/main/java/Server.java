
import java.io.*;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Server implements Runnable {
    public static int port;
    ConcurrentHashMap<String, ConcurrentHashMap<String, Handler>> handlers = new ConcurrentHashMap<>();
    private static final String GET = "GET";
    private static final String POST = "POST";

    public Server(int port) {
        Server.port = port;
    }

    public void run() {
        final var allowedMethods = List.of(GET, POST);
        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                try (
                        final var socket = serverSocket.accept();
                        final var in = new BufferedInputStream(socket.getInputStream());
                        final var out = new BufferedOutputStream(socket.getOutputStream());
                ) {
                    // must be in form GET /path HTTP/1.1
                    // лимит на request line + заголовки
                    final var limit = 4096;
                    in.mark(limit);
                    final var buffer = new byte[limit];
                    final var read = in.read(buffer);
                    // ищем request line
                    final var requestLineDelimiter = new byte[]{'\r', '\n'};
                    final var requestLineEnd = PostRequest.indexOf(buffer, requestLineDelimiter, 0, read);
                    if (requestLineEnd == -1) {
                        ResponseClass.errorRequest(out);
                        continue;
                    }
                    // читаем request line
                    final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
                    System.out.println(Arrays.toString(requestLine));
                    if (requestLine.length != 3) {
                        ResponseClass.errorRequest(out);
                        continue;
                    }
                    final var method = requestLine[0];
                    if (!allowedMethods.contains(method)) {
                        ResponseClass.errorRequest(out);
                        continue;
                    }
                    System.out.println(method);
                    final var path = requestLine[1];
                    Request request = new Request(method, path);
                    System.out.println("path = " + request.getPath());
                    var queryParams = request.getQueryParams();
                    System.out.println("queryParams = " + queryParams);
                    var getQueryParam = request.getQueryParam("title");
                    System.out.println("getQueryParam = " + getQueryParam);
                    if (handlers.containsKey(request.getMethod())) {
                        if (handlers.get(request.getMethod()).containsKey(request.getPath())) {
                            handlers.get(request.getMethod()).get(request.getPath()).handle(request, out);
                        } else {
                            ResponseClass.errorRequest(out);
                        }
                    }
                    final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
                    final var headersStart = requestLineEnd + requestLineDelimiter.length;
                    final var headersEnd = PostRequest.indexOf(buffer, headersDelimiter, headersStart, read);
                    if (headersEnd == -1) {
                        ResponseClass.errorRequest(out);
                        continue;
                    }
                    // отматываем на начало буфера
                    in.reset();
                    // пропускаем requestLine
                    in.skip(headersStart);
                    final var headersBytes = in.readNBytes(headersEnd - headersStart);
                    final var headers = Arrays.asList(new String(headersBytes).split("\r\n"));
                    System.out.println("headers = " + headers);
                    // для GET тела нет
                    if (!method.equals(GET)) {
                        in.skip(headersDelimiter.length);
                        // вычитываем Content-Length, чтобы прочитать body
                        final var contentLength = PostRequest.extractHeader(headers, "Content-Length");
                        if (contentLength.isPresent()) {
                            final var length = Integer.parseInt(contentLength.get());
                            final var bodyBytes = in.readNBytes(length);
                            final var body = new String(bodyBytes);
                            System.out.println(body);
                        }
                    }
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addHandler(String method, String path, Handler handler) {
        if (!handlers.containsKey(method)) {
            handlers.put(method, new ConcurrentHashMap<>());
        }
        handlers.get(method).put(path, handler);
    }
}

