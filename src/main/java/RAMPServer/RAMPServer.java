package RAMPServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

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

import static io.undertow.Handlers.path;
import static io.undertow.Handlers.resource;
import static io.undertow.Handlers.websocket;

import javax.sound.sampled.spi.AudioFileWriter;

public class RAMPServer {
    public static byte[] toByteArray(File file) {
        int length = (int)file.length();
        byte[] array = new byte[length];

        try {
            int offset = 0;
            InputStream in = new FileInputStream(file);
            while (offset < length) {
                int count = in.read(array, offset, (length - offset));
                offset += count;
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return array;
    }

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
        // Load the song into a byte buffer.
        File input = new File("build/resources/main/mario.wav");
        byte[] buffer = RAMPServer.toByteArray(input);

        // System.out.println(buffer.length);
        // 7235522

        // Split the song into 5 second increments.
        byte[] a = Arrays.copyOfRange(buffer, 0, 452220);
        byte[] b = Arrays.copyOfRange(buffer, 452220, 452220*2);
        byte[] c = Arrays.copyOfRange(buffer, 452220*2, 452220*3);
        byte[] d = Arrays.copyOfRange(buffer, 452220*3, 452220*4);
        byte[] e = Arrays.copyOfRange(buffer, 452220*4, 452220*5);
        byte[] f = Arrays.copyOfRange(buffer, 452220*5, 452220*6);
        byte[] g = Arrays.copyOfRange(buffer, 452220*6, 452220*7);
        byte[] h = Arrays.copyOfRange(buffer, 452220*7, 452220*8);
        byte[] i = Arrays.copyOfRange(buffer, 452220*8, 452220*9);
        byte[] j = Arrays.copyOfRange(buffer, 452220*9, 452220*10);
        byte[] k = Arrays.copyOfRange(buffer, 452220*10, 452220*11);
        byte[] l = Arrays.copyOfRange(buffer, 452220*11, 452220*12);
        byte[] m = Arrays.copyOfRange(buffer, 452220*12, 452220*13);
        byte[] n = Arrays.copyOfRange(buffer, 452220*13, 452220*14);
        byte[] o = Arrays.copyOfRange(buffer, 452220*14, 452220*15);
        byte[] p = Arrays.copyOfRange(buffer, 452220*15, buffer.length);

        // Send each 5 second increment to the browser.
        WebSockets.sendBinary(ByteBuffer.wrap(a), channel, null);
        WebSockets.sendBinary(ByteBuffer.wrap(b), channel, null);
        WebSockets.sendBinary(ByteBuffer.wrap(c), channel, null);
        WebSockets.sendBinary(ByteBuffer.wrap(d), channel, null);
        WebSockets.sendBinary(ByteBuffer.wrap(e), channel, null);
        WebSockets.sendBinary(ByteBuffer.wrap(f), channel, null);
        WebSockets.sendBinary(ByteBuffer.wrap(g), channel, null);
        WebSockets.sendBinary(ByteBuffer.wrap(h), channel, null);
        WebSockets.sendBinary(ByteBuffer.wrap(i), channel, null);
        WebSockets.sendBinary(ByteBuffer.wrap(j), channel, null);
        WebSockets.sendBinary(ByteBuffer.wrap(k), channel, null);
        WebSockets.sendBinary(ByteBuffer.wrap(l), channel, null);
        WebSockets.sendBinary(ByteBuffer.wrap(m), channel, null);
        WebSockets.sendBinary(ByteBuffer.wrap(n), channel, null);
        WebSockets.sendBinary(ByteBuffer.wrap(o), channel, null);
        WebSockets.sendBinary(ByteBuffer.wrap(p), channel, null);
    }
}