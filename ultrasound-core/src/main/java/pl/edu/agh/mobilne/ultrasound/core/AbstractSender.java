package pl.edu.agh.mobilne.ultrasound.core;

import java.util.Arrays;
import java.util.zip.CRC32;

public abstract class AbstractSender implements Runnable {

    private CRC32 crc32Comp = new CRC32();

    private byte[] data;
    private long crc32;
    private boolean sent = true;

    private volatile boolean stopFlag = false;

    protected AbstractSender(byte[] initData) {
        prepare();
        prepareTones();
        setData(initData);
    }

    protected abstract void send4Bits(int data);

    protected abstract void prepare();

    protected abstract void prepareTones();

    public synchronized boolean setData(byte[] data) {
        if (!sent) {
            return false;
        }
        sent = false;
        this.data = Arrays.copyOf(data, data.length);
        crc32Comp.update(this.data);
        crc32 = crc32Comp.getValue();
        crc32Comp.reset();
        return true;
    }

    @Override
    public void run() {
        System.out.println("Starting sender thread");
        beforeStart();

        while (!stopFlag) {
            sendSyncData();
            sendData();
        }

        doCleanup();
    }

    protected void doCleanup() {

    }

    protected void beforeStart() {

    }

    public void stop() {
        System.out.println("Destroying sender thread");
        stopFlag = true;
    }

    private void sendSyncData() {
        send4Bits(FFTConstants.SILENCE);
        send4Bits(0);
        send4Bits(FFTConstants.SILENCE);
    }

    private synchronized void sendData() {
        for (byte singleByte : data) {
            // send low order bytes
            send4Bits(((singleByte >> 4) & 0x0F));
            // send high order bytes
            send4Bits((singleByte & 0x0F));
        }
        // send last 2 bytes of crc from highest to lowest
        for (int j = 3; j >= 0; j--) {
            send4Bits((int) ((crc32 >> (4 * j)) & 0x0F));
        }
        sent = true;
    }
}
