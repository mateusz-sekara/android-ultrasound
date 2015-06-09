package pl.edu.agh.mobilne.ultrasound.android.lib.send;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;

import pl.edu.agh.mobilne.ultrasound.core.FFTConstants;

import static pl.edu.agh.mobilne.ultrasound.core.Utils.computeFrequency;

class SenderAndroid implements Runnable {

    private AudioTrack audioTrack;

    private CRC32 crc32Comp = new CRC32();

    private byte[] data;
    private long crc32;
    private boolean sent = true;

    private Map<Integer, short[]> samples = new HashMap<Integer, short[]>();
    private short silence[];

    SenderAndroid(byte[] initData) {
        prepare();
        prepareTones();
        setData(initData);
    }

    private void prepare() {
        try {

            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    FFTConstants.sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, FFTConstants.fftSampleRate,
                    AudioTrack.MODE_STATIC);
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
        while (true) {
            sendSyncData();
            sendData();
        }
    }

    private void sendSyncData() {
        for (int i = 0; i < 5; i++) {
            audioTrack.write(silence, 0, FFTConstants.fftSampleRate);
            audioTrack.play();
        }
        for (int i = 0; i < 5; i++) {
            audioTrack.write(samples.get(0), 0, FFTConstants.fftSampleRate);
            audioTrack.play();
        }
        for (int i = 0; i < 5; i++) {
            audioTrack.write(silence, 0, FFTConstants.fftSampleRate);
            audioTrack.play();
        }
    }

    private synchronized void sendData() {
        for (byte singleByte : data) {
            // send low order bytes
            for (int i = 0; i < 5; i++) {
                audioTrack.write(samples.get(((singleByte >> 4) & 0x0F)), 0, FFTConstants.fftSampleRate);
                audioTrack.play();
            }
            // send high order bytes
            for (int i = 0; i < 5; i++) {
                audioTrack.write(samples.get((singleByte & 0x0F)), 0, FFTConstants.fftSampleRate);
                audioTrack.play();
            }
        }
        // send last 2 bytes of crc from highest to lowest
        for (int j = 4; j > 0; j--) {
            for (int i = 0; i < 5; i++) {
                audioTrack.write(samples.get((int) ((crc32 >> (4 * j)) & 0x0F)), 0, FFTConstants.fftSampleRate);
                audioTrack.play();
            }
        }
    }

    private void prepareTones() {
        samples.put(0, genTone(computeFrequency(FFTConstants.frequencyOn)));
        silence = new short[FFTConstants.fftSampleRate];
        for (int i = 0; i < 4; i++) {
            samples.put((1 << i), genTone(computeFrequency(FFTConstants.baseFrequency + FFTConstants.stepFrequency * i)));
        }
        for (int i = 3; i < 16; i++) {
            if ((i & (i - 1)) != 0) {
                List<short[]> toAdd = new ArrayList<short[]>();
                System.out.print("\n" + i + ": ");
                for (int j = 0; j < 4; j++) {
                    if ((i & (1 << j)) != 0) {
                        toAdd.add(samples.get((1 << j)));
                        System.out.print((1 << j) + " ");
                    }
                }
                samples.put(i, sum(toAdd));
            }
        }
    }

    private short[] sum(List<short[]> samples) {
        short[] result = new short[FFTConstants.fftSampleRate];
        for (int i = 0; i < FFTConstants.fftSampleRate; i++) {
            for (short[] sample : samples) {
                result[i] += sample[i] / samples.size();
            }
        }
        return result;
    }

    private short[] genTone(double freq) {
        short[] sample = new short[FFTConstants.fftSampleRate];
        // fill out the array
        for (int i = 0; i < FFTConstants.fftSampleRate; ++i) {
            sample[i] = (short) (Math.sin(2 * Math.PI * i / (FFTConstants.sampleRate / freq)) * 32767);
        }
        return sample;
    }
}
