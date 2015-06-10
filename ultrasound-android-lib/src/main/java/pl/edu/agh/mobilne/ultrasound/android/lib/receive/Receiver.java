package pl.edu.agh.mobilne.ultrasound.android.lib.receive;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;

import pl.edu.agh.mobilne.ultrasound.android.lib.Constants;
import pl.edu.agh.mobilne.ultrasound.core.FFTConstants;
import pl.edu.agh.mobilne.ultrasound.core.fft.FFT;

import static pl.edu.agh.mobilne.ultrasound.core.Utils.computeFrequency;


class Receiver implements Runnable {

    private static final int bufferSize = FFTConstants.recordSampleRate;
    private static final short mainBuffer[] = new short[bufferSize];
    private static final short syncBuffer[] = new short[bufferSize];

    private volatile boolean stopFlag = false;

    private OutputStream outputStream;
    private AudioRecord audioRecord;
    private FFT mainFFT;
    private FFT syncFFT;

    Receiver(OutputStream outputStream) {
        this.outputStream = outputStream;
        try {
            mainFFT = new FFT(FFTConstants.fftSampleRate, FFTConstants.sampleRate, 3.5);
            syncFFT = new FFT(FFTConstants.smallFftSampleRate, FFTConstants.sampleRate, 3.5);
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, FFTConstants.sampleRate,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, FFTConstants.recordSampleRate * 2);
        } catch (Exception e) {
            Log.e(Constants.LOG, "Error while initializing audio recorder", e);
        }
    }

    @Override
    public void run() {
        Log.d(Constants.LOG, "Starting receiver thread");
        try {
            audioRecord.startRecording();

            int syncFoundPosition = syncAndReturnBufferPosition();
            if (syncFoundPosition < 0) {
                return;
            }

            int count = readFirstLineAfterSync(syncFoundPosition);
            while (!stopFlag) {
                if (count == bufferSize) {
                    processData();
                } else {
                    Log.e(Constants.LOG, "Wrong buffer size");
                    return;
                }
                count = audioRecord.read(mainBuffer, 0, bufferSize);
            }
        } finally {
            //cleanup
            audioRecord.release();
        }
    }

    public void stop() {
        Log.d(Constants.LOG, "Destroying receiver thread");
        stopFlag = false;
    }

    private int syncAndReturnBufferPosition() {
        int found;
        outer:
        while (true) {
            int count = audioRecord.read(syncBuffer, 0, bufferSize);
            if (count == bufferSize) {
                for (int k = 0; k < FFTConstants.smallFftToRecordMultiplier; k++) {
                    syncFFT.forward(syncBuffer, FFTConstants.smallFftSampleRate * k, FFTConstants.smallFftSampleRate);
                    final boolean has19k = syncFFT.hasFrequency(Math.round(FFTConstants.frequencyOn / FFTConstants.smallFftFreqIndexRange) * FFTConstants.smallFftFreqIndexRange);
                    if (has19k) {
                        found = FFTConstants.smallFftSampleRate * k;
                        Log.d(Constants.LOG, "Found on position - " + found);
                        break outer;
                    }
                }
            } else {
                Log.e(Constants.LOG, "Wrong buffer size");
                return -1;
            }
        }
        return found;
    }

    private int readFirstLineAfterSync(int syncFoundPosition) {
        try {
            System.arraycopy(syncBuffer, syncFoundPosition, mainBuffer, 0, bufferSize - syncFoundPosition);
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(Constants.LOG, "Found - " + syncFoundPosition);
            return -1;
        }
        if (syncFoundPosition > 0) {
            return bufferSize - syncFoundPosition + audioRecord.read(mainBuffer, bufferSize - syncFoundPosition, syncFoundPosition);
        } else {
            return bufferSize;
        }
    }

    private void processData() {
        for (int k = 0; k < FFTConstants.fftToRecordMultiplier; k++) {
            mainFFT.forward(mainBuffer, FFTConstants.fftSampleRate * k, FFTConstants.fftSampleRate);
            int output = 0;
            if (!mainFFT.hasFrequency(computeFrequency(FFTConstants.frequencyOn))) { // is not 0
                for (int i = 3; i >= 0; i--) {
                    output = output << 1;
                    if (mainFFT.hasFrequency(computeFrequency(FFTConstants.baseFrequency + FFTConstants.stepFrequency * i))) {
                        output += 1;
                    }
                }
                if (output == 0) {
                    output = -1; // silence
                }
            }
            try {
                outputStream.write(output);
            } catch (IOException e) {
                //do nothing
            }
        }
    }
}
