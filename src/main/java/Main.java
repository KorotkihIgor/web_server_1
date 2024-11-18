
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws IOException {
        Server server = new Server(9999);
        server.addHandler("GET", "/index.html", GetRequest::handle);
        server.addHandler("GET", "/spring.png", GetRequest::handle);
        server.addHandler("GET", "/messages.html", GetRequest::handle);
        server.addHandler("GET", "/forms.html", GetRequest::handle);
        server.addHandler("POST", "/?default-get.html", PostRequest::handle);
        server.addHandler("GET", "/default-get.html", GetRequest::handle);
        ExecutorService threadPool = Executors.newFixedThreadPool(64);
        threadPool.submit(server);
        threadPool.shutdown();
    }
}
