package pl.edu.agh.mobilne.ultrasound.pc.app;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;

import pl.edu.agh.mobilne.ultrasound.core.AbstractDataReader;
import pl.edu.agh.mobilne.ultrasound.core.TokenGenerator;

public class ReceiverRunner {

    public static void main(String[] args) {

        PCDataReader dataReader;
        ReceiverPC receiver;

        PipedOutputStream outputStream;
        PipedInputStream inputStream;

        try {
            outputStream = new PipedOutputStream();
            inputStream = new PipedInputStream(outputStream);
            receiver = new ReceiverPC(outputStream);
            new Thread(receiver).start();

            dataReader = new PCDataReader(inputStream);
            new Thread(dataReader).start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static class PCDataReader extends AbstractDataReader {

        protected PCDataReader(PipedInputStream inputStream) {
            super(inputStream);
        }

        @Override
        protected void onSuccess(byte[] output) {
            System.out.println(TokenGenerator.convertFromByteArray(output) + "\t" + Arrays.toString(output));
        }

        @Override
        protected void onFailure(byte[] output, byte[] outputWithCrc) {
            System.err.println(TokenGenerator.convertFromByteArray(output) + "\t" + Arrays.toString(output));
            System.err.println(Arrays.toString(outputWithCrc));
        }
    }

}
