package JavaSoundSample;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.puredata.core.PdBase;

public class JavaSoundThread extends Thread {
    private final float sampleRate;     // Sample rate in Hz.
    private final int   inputChannels;  // Number of input channels.
    private final int   outputChannels; // Number of output channels.
    private final int   ticks;          // Number of Pd ticks per buffer.

    private volatile boolean terminated;

    public JavaSoundThread() {
        setPriority(Thread.MAX_PRIORITY);

        sampleRate = 48000;
        inputChannels = 2;
        outputChannels = 2;
        ticks = 16;

        PdBase.openAudio(inputChannels, outputChannels, (int)sampleRate);
        PdBase.computeAudio(true);
    }

    @Override
    public void run() {
        terminated = false;
        try {
            perform();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void interrupt() {
        // Needed since the interrupt flag is cleared by JavaSound.
        terminated = true;

        super.interrupt();
    }

    private void perform() throws IOException, LineUnavailableException {
        int sampleSize = 2;
        int frames = PdBase.blockSize() * ticks;

        // Create the buffers used to exchange data between Java and libpd.
        short[] inBuffer = new short[0];
        short[] outBuffer = new short[frames * outputChannels];

        // Create a SourceDataLine for outputting audio to your speakers.
        AudioFormat audioFormat = new AudioFormat(sampleRate, 8 * sampleSize, outputChannels, true, true);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        SourceDataLine sourceDataLine = (SourceDataLine)AudioSystem.getLine(info);
        sourceDataLine.open(audioFormat);
        sourceDataLine.start();

        // Create the buffers required for SourceDataLine.
        byte[] rawSamples = new byte[outBuffer.length * sampleSize];
        ShortBuffer shortBuf = ByteBuffer.wrap(rawSamples).asShortBuffer();

        PdBase.sendMessage("osc", "/position", 0.0f);
        PdBase.sendMessage("osc", "/master/vol", -0.95f);
        PdBase.sendMessage("osc", "/play");

        // SourceDataLine.write seems to clear the interrupted flag, and
        // so Thread.interrupted() doesn't work here.
        while (!terminated) {
            PdBase.process(ticks, inBuffer, outBuffer);
            PdBase.pollPdMessageQueue();

            // Write the sound data to the audio driver.
            shortBuf.rewind();
            shortBuf.put(outBuffer);
            sourceDataLine.write(rawSamples, 0, rawSamples.length);
        }

        // Shutdown the SourceDataLine.
        sourceDataLine.drain();
        sourceDataLine.stop();
        sourceDataLine.close();
    }
}
