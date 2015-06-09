package pl.edu.agh.mobilne.ultrasound.pc.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import pl.edu.agh.mobilne.ultrasound.core.FFTConstants;

import static pl.edu.agh.mobilne.ultrasound.core.Utils.computeFrequency;

public class SenderPC implements Runnable {

    private SourceDataLine line;

    private CRC32 crc32Comp = new CRC32();

    private byte[] data;
    private long crc32;
    private boolean sent = true;

    private Map<Integer, byte[]> samples = new HashMap<Integer, byte[]>();
    private byte silence[];

    public SenderPC(byte[] initData) {
        prepare();
        prepareTones();
        setData(initData);
    }

    private void prepare() {
        try {
            final AudioFormat format = new AudioFormat(FFTConstants.sampleRate, 8, 1, true, true);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
        line.start();
        while (true) {
            sendSyncData();
            sendData();
        }
    }

    private void sendSyncData() {
        for (int i = 0; i < 5; i++) {
            line.write(silence, 0, FFTConstants.fftSampleRate);
        }
        for (int i = 0; i < 5; i++) {
            line.write(samples.get(0), 0, FFTConstants.fftSampleRate);
        }
        for (int i = 0; i < 5; i++) {
            line.write(silence, 0, FFTConstants.fftSampleRate);
        }
    }

    private synchronized void sendData() {
        for (byte singleByte : data) {
            // send low order bytes
            for (int i = 0; i < 5; i++) {
                line.write(samples.get(((singleByte >> 4) & 0x0F)), 0, FFTConstants.fftSampleRate);
            }
            // send high order bytes
            for (int i = 0; i < 5; i++) {
                line.write(samples.get((singleByte & 0x0F)), 0, FFTConstants.fftSampleRate);
            }
        }
        // send last 2 bytes of crc from highest to lowest
        for (int j = 3; j >= 0; j--) {
            for (int i = 0; i < 5; i++) {
                line.write(samples.get((int) ((crc32 >> (4 * j)) & 0x0F)), 0, FFTConstants.fftSampleRate);
            }
        }
        sent = true;
    }

    private void prepareTones() {
        samples.put(0, genTone(computeFrequency(FFTConstants.frequencyOn)));
        silence = new byte[FFTConstants.fftSampleRate];
        for (int i = 0; i < 4; i++) {
            samples.put((1 << i), genTone(computeFrequency(FFTConstants.baseFrequency + FFTConstants.stepFrequency * i)));
        }
        for (int i = 3; i < 16; i++) {
            if ((i & (i - 1)) != 0) {
                List<byte[]> toAdd = new ArrayList<byte[]>();
                for (int j = 0; j < 4; j++) {
                    if ((i & (1 << j)) != 0) {
                        toAdd.add(samples.get((1 << j)));
                    }
                }
                samples.put(i, sum(toAdd));
            }
        }
    }

    private byte[] sum(List<byte[]> samples) {
        byte[] result = new byte[FFTConstants.fftSampleRate];
        for (int i = 0; i < FFTConstants.fftSampleRate; i++) {
            for (byte[] sample : samples) {
                result[i] += sample[i] / samples.size();
            }
        }
        return result;
    }

    private byte[] genTone(double freq) {
        byte[] sample = new byte[FFTConstants.fftSampleRate];
        // fill out the array
        for (int i = 0; i < FFTConstants.fftSampleRate; ++i) {
            sample[i] = (byte) (Math.sin(2 * Math.PI * i / (FFTConstants.sampleRate / freq)) * 127);
        }
        return sample;
    }
}
