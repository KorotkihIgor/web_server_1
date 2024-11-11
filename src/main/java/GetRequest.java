import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GetRequest extends Request {
    public GetRequest(String method, String path, BufferedInputStream in) {
        super(method, path, in);
    }

    public static void handle(Request request, BufferedOutputStream responseStream) throws IOException {
        final var filePath = Path.of(".", "public", request.getPath());
        final var mimeType = Files.probeContentType(filePath);
        final var length = Files.size(filePath);
        ResponseClass.goodRequest(responseStream, mimeType, length);
        Files.copy(filePath, responseStream);
        responseStream.flush();
    }
}