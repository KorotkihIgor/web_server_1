import java.io.*;

public class Request {
    private String method;
    private String path;
    private BufferedInputStream in;

    public Request(String method, String path, BufferedInputStream in) {
        this.method = method;
        this.path = path;
        this.in = in;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }
}
