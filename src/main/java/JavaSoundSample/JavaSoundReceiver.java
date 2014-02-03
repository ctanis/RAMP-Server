package JavaSoundSample;

import org.puredata.core.PdReceiver;

public class JavaSoundReceiver implements PdReceiver {
    @Override
    public void print(String s) {
        System.out.print(s);
    }

    @Override
    public void receiveBang(String source) {
        System.out.print(source);
    }

    @Override
    public void receiveFloat(String source, float x) {
        System.out.printf("%s : %f\n", source, x);
    }

    @Override
    public void receiveSymbol(String source, String symbol) {
        System.out.printf("%s : %s\n", source, symbol);
    }

    @Override
    public void receiveList(String source, Object... args) {
        System.out.printf("%s : %s\n", source, args);
    }

    @Override
    public void receiveMessage(String source, String symbol, Object... args) {
        System.out.printf("%s : %s : %s\n", source, symbol, args);
    }
}