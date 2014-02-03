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

import java.util.Arrays;

public class JavaSoundThread extends Thread {
    // Audio file that we're going to process.
    // TODO: How do we work with multi-file songs?
    private AudioInputStream audioInputStream;
    private AudioFormat audioFormat;

    private final float sampleRate;  // Sample rate in Hz.
    private final int channels;      // Number of output channels.
    private final int ticks;         // Number of Pd ticks per buffer.

    private volatile boolean terminated;

    public JavaSoundThread(String audioPath) {
        setPriority(Thread.MAX_PRIORITY);

        // Attempt to load the audio file.
        try {
            audioInputStream = AudioSystem.getAudioInputStream(new File(audioPath));
            audioFormat      = audioInputStream.getFormat();
        } catch (IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }

        // NOTE: I think that `ticks` can be set to anything we want, to improve
        // performance.
        sampleRate = audioFormat.getSampleRate();
        channels   = audioFormat.getChannels();
        ticks      = audioFormat.getSampleSizeInBits();

        PdBase.openAudio(0, channels, (int)sampleRate);
        // PdBase.computeAudio(true);
    }

    @Override
    public void run() {
        terminated = false;
        try {
            perform();
        } catch (IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void interrupt() {
        terminated = true;  // Needed since the interrupted flag is cleared by JavaSound.
        super.interrupt();
    }

    private void perform() throws IOException, LineUnavailableException {
        int sampleSize = 2;

        // Create the buffers used to exchange data between Java and libpd.
        int frames        = PdBase.blockSize() * ticks;
        short[] inBuffer  = new short[frames * channels];
        short[] outBuffer = new short[frames * channels];

        // Create the buffers required for converting from byte[] to short[].
        byte[] rawSamples2    = new byte[inBuffer.length * sampleSize];
        ByteBuffer buf2       = ByteBuffer.wrap(rawSamples2);
        ShortBuffer shortBuf2 = buf2.asShortBuffer();

        // Create a SourceDataLine for outputting audio to your speakers.
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        SourceDataLine sourceDataLine = (SourceDataLine)AudioSystem.getLine(info);
        sourceDataLine.open(audioFormat);
        sourceDataLine.start();

        // Create the buffers required for SourceDataLine.
        byte[] rawSamples    = new byte[outBuffer.length * sampleSize];
        ByteBuffer buf       = ByteBuffer.wrap(rawSamples);
        ShortBuffer shortBuf = buf.asShortBuffer();

        // SourceDataLine.write seems to clear the interrupted flag, and
        // so Thread.interrupted() doesn't work here.
        //
        // NOTE: In the future we will check here to see if there's any more audio
        // to process.
        while (!terminated) {
            audioInputStream.read(rawSamples2);
            shortBuf2.rewind();
            shortBuf2.get(inBuffer);

            // System.out.println(Arrays.toString(inBuffer));

            // TODO: This returns a status code. Maybe we should check for it?
            PdBase.process(ticks, inBuffer, outBuffer);
            PdBase.pollPdMessageQueue();

            // TODO: outBuffer is empty.
            // System.out.println(Arrays.toString(outBuffer));

            // Write the sound data to the audio driver.
            // NOTE: This is where we will output audio to the Web Socket.
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
