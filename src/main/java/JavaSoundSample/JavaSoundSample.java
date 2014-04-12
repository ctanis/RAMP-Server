package JavaSoundSample;

import java.io.IOException;

import org.puredata.core.PdBase;

public class JavaSoundSample {
    public static void main(String[] args) throws InterruptedException, IOException {
        // Open the Pure Data patch.
        System.out.println(">> Starting server.");
        int patch = PdBase.openPatch("build/resources/main/ramp.pd");

        // Create a reciever for listening to messages PD sends back.
        JavaSoundReceiver receiver = new JavaSoundReceiver();
        PdBase.setReceiver(receiver);

        // Start a new thread that processes the audio and outputs the results
        // to your computer's speakers.
        JavaSoundThread audioThread = new JavaSoundThread();
        audioThread.start();
        Thread.sleep(100000);
        audioThread.interrupt();
        audioThread.join();

        PdBase.closePatch(patch);
        System.out.println(">> Exiting server.");
    }
}
