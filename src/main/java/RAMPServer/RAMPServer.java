package RAMPServer;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

public class RAMPServer {
    public static void main(final String[] args) {

        // Use port 8080 if $PORT is not set.
        int port = 8080;
        if (System.getenv("PORT") != null) {
            port = Integer.valueOf(System.getenv("PORT"));
        }

        // Build and start the Undertow server.
        Undertow server = Undertow.builder()
                .addListener(port, "0.0.0.0")
                .setHandler(new HttpHandler() {
                    @Override
                    public void handleRequest(final HttpServerExchange exchange) throws Exception {
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                        exchange.getResponseSender().send("Hello World");
                    }
                }).build();
        server.start();

        System.out.println();
        System.out.println("Started server at http://localhost:" + port + "/. Hit ^C to stop.");
    }
}