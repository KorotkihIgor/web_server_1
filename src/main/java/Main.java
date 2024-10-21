
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Main {
    public static void main(String[] args) throws IOException {
        Server server = new Server(9999);
        ExecutorService threadPool = Executors.newFixedThreadPool(64);
        threadPool.submit(server);
        threadPool.shutdown();
    }
}
