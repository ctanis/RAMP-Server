package RAMPServer;

import java.io.IOException;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.util.Headers;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import io.undertow.websockets.WebSocketConnectionCallback;
import org.puredata.core.PdBase;

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

        // Build and start an Undertow server.
        Undertow server = Undertow.builder().addListener(port, "0.0.0.0")
            .setHandler(path()
                .addPath("/", new RootPageHandler())
                .addPath("/echo", websocket(new EchoHandler()))
                .addPath("/stream", websocket(new StreamingHandler()))
            ).build();
        server.start();

        System.out.println();
        System.out.println(">> Started server at http://localhost:" + port + "/");
        System.out.println(">> Press Ctrl+C to exit.");

        try {
            // Open the Pure Data patch.
            int patch = PdBase.openPatch("build/resources/main/ramp.pd");

            // Create a reciever for listening to messages PD sends back.
            RAMPReceiver receiver = new RAMPReceiver();
            PdBase.setReceiver(receiver);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class RootPageHandler implements HttpHandler {
    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send("OK");
    }
}

class EchoHandler implements WebSocketConnectionCallback {
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
}

class StreamingHandler implements WebSocketConnectionCallback {
    @Override
    public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
        // Listen for OSC commands.
        channel.getReceiveSetter().set(new AbstractReceiveListener() {
            @Override
            protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) {
                // Parse the OSC message and forward it onto Libpd.
                String[] oscMessage = message.getData().split(" ");
                if (oscMessage.length > 1) {
                    try {
                        Float f = new Float(oscMessage[1]);
                        PdBase.sendMessage("osc", oscMessage[0], f);
                    } catch (NumberFormatException e) {
                        PdBase.sendMessage("osc", oscMessage[0], oscMessage[1]);
                    }
                } else {
                    PdBase.sendMessage("osc", oscMessage[0]);
                }
            }
        });
        channel.resumeReceives();

        RAMPThread thread = new RAMPThread(channel);
        thread.start();
    }
}