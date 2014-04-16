package RAMPServer;

import java.io.IOException;
import java.lang.System;
import java.lang.Thread;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.ShortBuffer;

import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import org.puredata.core.PdBase;

public class RAMPThread extends Thread {
    WebSocketChannel channel;

    float sampleRate;      // Sample rate in Hz.
    int   inputChannels;   // Number of input channels.
    int   outputChannels;  // Number of output channels.
    int   ticks;           // Number of Pd ticks per buffer.
    int   patch;

    public RAMPThread(WebSocketChannel channel) {
        try {
            // Open the Pure Data patch.
            patch = PdBase.openPatch("build/resources/main/ramp.pd");

            // Create a reciever for listening to messages PD sends back.
            RAMPReceiver receiver = new RAMPReceiver();
            PdBase.setReceiver(receiver);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.channel = channel;

        sampleRate = 48000;
        inputChannels = 2;
        outputChannels = 2;
        ticks = 16;

        PdBase.openAudio(inputChannels, outputChannels, (int)sampleRate);
        PdBase.computeAudio(true);

        PdBase.sendMessage("osc", "/master/vol", -0.98f);
    }

    @Override
    public void interrupt() {
        PdBase.closePatch(patch);
        super.interrupt();
    }

    @Override
    public void run() {
        // Attempt to load the audio file.
        try {
            int sampleSize = 2;

            // Create the buffers used to exchange data between Java and libpd.
            int frames = PdBase.blockSize() * ticks;
            short[] pdInBuffer = new short[0];
            short[] pdOutBuffer = new short[frames * outputChannels];

            // Create the buffers required for converting from short[] to byte[].
            byte[] rawOutput = new byte[pdOutBuffer.length * sampleSize];
            ShortBuffer rawOutputAsShorts = ByteBuffer.wrap(rawOutput).asShortBuffer();

            // Loop forever!
            while (!this.isInterrupted()) {
                // TODO: This returns a status code. Maybe we should check for it?
                PdBase.process(ticks, pdInBuffer, pdOutBuffer);
                PdBase.pollPdMessageQueue();

                // Convert and place the pdOutBuffer (shorts) into rawOutput (bytes).
                rawOutputAsShorts.rewind();
                rawOutputAsShorts.put(pdOutBuffer);

                try {
                    WebSockets.sendBinaryBlocking(ByteBuffer.wrap(rawOutput), channel);
                } catch (ClosedChannelException e) {
                    this.interrupt();
                }

                // Libpd processes the song way faster than it plays.
                // TODO: This is a total hack. We have *got* to find a better way.
                try {
                    Thread.sleep(17);
                } catch (InterruptedException e) {
                    this.interrupt();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}