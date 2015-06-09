package pl.edu.agh.mobilne.ultrasound.android.lib.send;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.edu.agh.mobilne.ultrasound.android.lib.Constants;
import pl.edu.agh.mobilne.ultrasound.core.AbstractSender;
import pl.edu.agh.mobilne.ultrasound.core.FFTConstants;

import static pl.edu.agh.mobilne.ultrasound.core.Utils.computeFrequency;

class Sender extends AbstractSender {

    private AudioTrack audioTrack;

    private Map<Integer, short[]> samples;

    public Sender(byte[] initData) {
        super(initData);
    }

    @Override
    protected void send4Bits(int data) {
        for (int i = 0; i < 5; i++) {
            audioTrack.write(samples.get(data), 0, FFTConstants.fftSampleRate);
            audioTrack.play();
        }
    }

    @Override
    protected void beforeStart() {
    }

    @Override
    protected void prepare() {
        samples = new HashMap<Integer, short[]>();
        try {
            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    FFTConstants.sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, FFTConstants.fftSampleRate,
                    AudioTrack.MODE_STATIC);
        } catch (Exception e) {
            Log.e(Constants.LOG, "Error while initializing audio playback", e);
        }
    }

    @Override
    protected void prepareTones() {
        samples.put(0, genTone(computeFrequency(FFTConstants.frequencyOn)));
        samples.put(FFTConstants.SILENCE, new short[FFTConstants.fftSampleRate]);
        for (int i = 0; i < 4; i++) {
            samples.put((1 << i), genTone(computeFrequency(FFTConstants.baseFrequency + FFTConstants.stepFrequency * i)));
        }
        for (int i = 3; i < 16; i++) {
            if ((i & (i - 1)) != 0) {
                List<short[]> toAdd = new ArrayList<short[]>();
                Log.v(Constants.LOG, "\n" + i + ": ");
                for (int j = 0; j < 4; j++) {
                    if ((i & (1 << j)) != 0) {
                        toAdd.add(samples.get((1 << j)));
                        Log.v(Constants.LOG, (1 << j) + " ");
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
