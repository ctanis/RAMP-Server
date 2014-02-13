package RAMPServer;

import io.undertow.Undertow;
import io.undertow.util.Headers;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.spi.WebSocketHttpExchange;

import static io.undertow.Handlers.path;
import static io.undertow.Handlers.resource;
import static io.undertow.Handlers.websocket;

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
                .setHandler(path()
                        .addPrefixPath("/", websocket(new WebSocketConnectionCallback() {
                            @Override
                            public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
                                channel.getReceiveSetter().set(new AbstractReceiveListener() {

                                    @Override
                                    protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) {
                                        WebSockets.sendText(message.getData(), channel, null);
                                    }
                                });
                                channel.resumeReceives();
                            }
                        }))
                        .addPrefixPath("/status", new HttpHandler() {

                            @Override
                            public void handleRequest(final HttpServerExchange exchange) throws Exception {
                                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                                exchange.getResponseSender().send("OK");
                            }
                        }))
                        .build();
        server.start();

        System.out.println();
        System.out.println("Started server at http://localhost:" + port + "/. Press Ctrl+C to exit.");
    }
}