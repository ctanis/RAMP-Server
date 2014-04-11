package RAMPServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

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

import javax.sound.sampled.spi.AudioFileWriter;

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
        AudioInputStream audioInputStream;
        AudioFormat      audioFormat;
        float            sampleRate;      // Sample rate in Hz.
        int              inputChannels;   // Number of input channels.
        int              outputChannels;  // Number of output channels.
        int              ticks;           // Number of Pd ticks per buffer.

        // Attempt to load the audio file.
        try {
            // Open the Pure Data patch.
            int patch = PdBase.openPatch("build/resources/main/plain.pd");

            File input = new File("build/resources/main/mario.wav");
            audioInputStream = AudioSystem.getAudioInputStream(input);
            audioFormat = audioInputStream.getFormat();
            // Create a reciever for listening to messages PD sends back.
            RAMPReceiver receiver = new RAMPReceiver();
            PdBase.setReceiver(receiver);

            // NOTE: I think that `ticks` can be set to anything we want...
            sampleRate = audioFormat.getSampleRate();
            inputChannels = audioFormat.getChannels();
            outputChannels = audioFormat.getChannels();
            ticks = audioFormat.getSampleSizeInBits();

            PdBase.openAudio(inputChannels, outputChannels, (int)sampleRate);
            PdBase.computeAudio(true);

            int sampleSize = 2;

            // Create the buffers used to exchange data between Java and libpd.
            int frames = PdBase.blockSize() * ticks;
            short[] pdInBuffer = new short[frames * inputChannels];
            short[] pdOutBuffer = new short[frames * outputChannels];

            // Create the buffers required for converting from byte[] to short[].
            byte[] rawInput = new byte[pdInBuffer.length * sampleSize];
            ShortBuffer rawInputAsShorts = ByteBuffer.wrap(rawInput).asShortBuffer();

            // Create the buffers required for converting from short[] to byte[].
            byte[] rawOutput = new byte[pdOutBuffer.length * sampleSize];
            ShortBuffer rawOutputAsShorts = ByteBuffer.wrap(rawOutput).asShortBuffer();

            // audioInputStream.read retuns -1 when there's no more data.
            int remaining = 1;
            while (remaining != -1) {
                // Copy the next song chunk from audioInputStream into rawInput.
                remaining = audioInputStream.read(rawInput);

                // Convert and place the rawInput (bytes) into pdInBuffer (shorts).
                rawInputAsShorts.rewind();
                rawInputAsShorts.get(pdInBuffer);

                // TODO: This returns a status code. Maybe we should check for it?
                PdBase.process(ticks, pdInBuffer, pdOutBuffer);

                // Convert and place the pdOutBuffer (shorts) into rawOutput (bytes).
                rawOutputAsShorts.rewind();
                rawOutputAsShorts.put(pdOutBuffer);

                WebSockets.sendBinaryBlocking(ByteBuffer.wrap(rawOutput), channel);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }
}