package JavaSoundSample;

import java.io.IOException;

import org.puredata.core.PdBase;

public class JavaSoundSample {
    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println(">> Starting server.");

        JavaSoundThread audioThread = new JavaSoundThread(44100, 2, 16);
        int patch = PdBase.openPatch("src/main/resources/test.pd");
        audioThread.start();
        Thread.sleep(5000);  // Sleep for five seconds; this is where the main application code would go in a real program.
        audioThread.interrupt();
        audioThread.join();
        PdBase.closePatch(patch);

        System.out.println(">> Exiting server.");
    }
}
