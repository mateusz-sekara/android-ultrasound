package pl.edu.agh.mobilne.ultrasound.pc.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import pl.edu.agh.mobilne.ultrasound.core.AbstractSender;
import pl.edu.agh.mobilne.ultrasound.core.FFTConstants;

import static pl.edu.agh.mobilne.ultrasound.core.Utils.computeFrequency;

public class SenderPC extends AbstractSender {

    private SourceDataLine line;

    private Map<Integer, byte[]> samples;

    public SenderPC(byte[] initData) {
        super(initData);
    }

    @Override
    protected void send4Bits(int data) {
        line.write(samples.get(data), 0, FFTConstants.fftSampleRate);
    }

    protected void prepare() {
        try {
            samples = new HashMap<Integer, byte[]>();
            final AudioFormat format = new AudioFormat(FFTConstants.sampleRate, 8, 1, true, true);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void prepareTones() {
        samples.put(0, genTone(computeFrequency(FFTConstants.frequencyOn)));
        samples.put(FFTConstants.SILENCE, new byte[FFTConstants.fftSampleRate]);
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
