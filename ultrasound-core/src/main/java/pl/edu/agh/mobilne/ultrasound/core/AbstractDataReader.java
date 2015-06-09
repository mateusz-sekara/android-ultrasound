package pl.edu.agh.mobilne.ultrasound.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.util.Arrays;
import java.util.zip.CRC32;

public abstract class AbstractDataReader implements Runnable {

    private volatile boolean stopFlag = false;

    private CRC32 crcCalc = new CRC32();
    private ByteArrayOutputStream baos;
    private DataProcessor dataProcessor;

    protected AbstractDataReader(PipedInputStream inputStream) {
        this.baos = new ByteArrayOutputStream();
        this.dataProcessor = new DataProcessor(inputStream, baos);
    }

    @Override
    public void run() {
        try {
            while (!stopFlag) {
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
            //fixme logger
            e.printStackTrace();
        } finally {
            Utils.closeQuietly(baos);
        }
    }

    public synchronized void stop() {
        stopFlag = true;
    }

    protected abstract void onSuccess(byte[] outputWithCrc);

    protected abstract void onFailure(byte[] outputWithCrc);

    private void checkCrc(byte[] outputWithCrc) {
        //fixme logger
        System.out.println(Arrays.toString(outputWithCrc));

        byte[] output = Arrays.copyOf(outputWithCrc, outputWithCrc.length - 2);
        crcCalc.update(output);
        long crc32 = crcCalc.getValue();
        crcCalc.reset();
        byte highCrc16Byte = (byte) ((crc32 >> 8) & 0xFF);
        byte lowCrc16Byte = (byte) (crc32 & 0xFF);

        if (highCrc16Byte == outputWithCrc[outputWithCrc.length - 2] &&
                lowCrc16Byte == outputWithCrc[outputWithCrc.length - 1]) {
            onSuccess(outputWithCrc);
        } else {
            onFailure(outputWithCrc);
        }
    }
}
