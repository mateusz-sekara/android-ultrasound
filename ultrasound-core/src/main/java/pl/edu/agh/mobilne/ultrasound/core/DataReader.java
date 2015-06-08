package pl.edu.agh.mobilne.ultrasound.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.util.Arrays;
import java.util.zip.CRC32;

public class DataReader implements Runnable {
    private CRC32 crcCalc = new CRC32();

    private ByteArrayOutputStream baos;

    private DataProcessor dataProcessor;

    public DataReader(PipedInputStream is) {
        this.baos = new ByteArrayOutputStream();
        this.dataProcessor = new DataProcessor(is, baos);
    }

    @Override
    public void run() {
        try {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // do nothing
                }
                dataProcessor.processData();
                byte[] outputWithCrc = baos.toByteArray();

                if (outputWithCrc.length > 2) {
                    checkCrc(outputWithCrc);
                }

                baos.reset();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void checkCrc(byte[] outputWithCrc) {
        System.out.println(Arrays.toString(outputWithCrc));
        byte[] output = Arrays.copyOf(outputWithCrc, outputWithCrc.length - 2);
        crcCalc.update(output);
        long crc32 = crcCalc.getValue();
        crcCalc.reset();
        byte highCrc16Byte = (byte) ((crc32 >> 8) & 0xFF);
        byte lowCrc16Byte = (byte) (crc32 & 0xFF);
        if (highCrc16Byte == outputWithCrc[outputWithCrc.length - 2] && lowCrc16Byte == outputWithCrc[outputWithCrc.length - 1]) {
            System.out.println(Arrays.toString(output));
        } else {
            System.out.println("crc error");
        }
    }
}
