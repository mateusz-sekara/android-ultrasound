package pl.edu.agh.mobilne.ultrasound.pc.app;

import java.io.IOException;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

import pl.edu.agh.mobilne.ultrasound.core.FFTConstants;
import pl.edu.agh.mobilne.ultrasound.core.fft.FFT;

import static pl.edu.agh.mobilne.ultrasound.core.Utils.computeFrequency;

public class ReceiverPC implements Runnable {

    private static final int bufferSize = FFTConstants.recordSampleRate;
    private static final byte mainBuffer[] = new byte[bufferSize];
    private static final byte syncBuffer[] = new byte[bufferSize];

    private OutputStream baos;

    private TargetDataLine line;

    private FFT mainFFT;

    private FFT syncFFT;

    public ReceiverPC(OutputStream outputStream) {
        try {
            baos = outputStream;
            mainFFT = new FFT(FFTConstants.fftVectorLength, FFTConstants.sampleRate, 4.5);
            syncFFT = new FFT(FFTConstants.syncFftVectorLength, FFTConstants.sampleRate, 4.5);
            final AudioFormat format = new AudioFormat(FFTConstants.sampleRate, 8/*samplesize*/, 1, true,
                    true);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        line.start();

        int syncFoundPosition = syncAndReturnBufferPosition();
        if (syncFoundPosition < 0) {
            return;
        }

        int count = readFirstLineAfterSync(syncFoundPosition);
        while (true) {
            if (count == bufferSize) {
                processData();
            } else {
                System.err.println("Wrong buffer size");
                return;
            }
            count = line.read(mainBuffer, 0, bufferSize);
        }
    }

    private int syncAndReturnBufferPosition() {
        int found;
        outer:
        while (true) {
            int count = line.read(syncBuffer, 0, bufferSize);
            if (count == bufferSize) {
                for (int k = 0; k < FFTConstants.syncFftToRecordMultiplier; k++) {
                    syncFFT.forward(syncBuffer, FFTConstants.syncFftVectorLength * k, FFTConstants.syncFftVectorLength);
                    final boolean has19k = syncFFT.hasFrequency(Math.round(FFTConstants.frequency0 / FFTConstants.syncFftFreqIndexRange) * FFTConstants.syncFftFreqIndexRange);
                    if (has19k) {
                        found = FFTConstants.syncFftVectorLength * k;
                        System.out.println("Found on position: " + found);
                        break outer;
                    }
                }
            } else {
                System.err.println("Wrong buffer size");
                return -1;
            }
        }
        return found;
    }

    private int readFirstLineAfterSync(int syncFoundPosition) {
        try {
            System.arraycopy(syncBuffer, syncFoundPosition, mainBuffer, 0, bufferSize - syncFoundPosition);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("found: " + syncFoundPosition);
            return -1;
        }
        if (syncFoundPosition > 0) {
            return bufferSize - syncFoundPosition + line.read(mainBuffer, bufferSize - syncFoundPosition, syncFoundPosition);
        } else {
            return bufferSize;
        }
    }

    private void processData() {
        for (int k = 0; k < FFTConstants.fftToRecordMultiplier; k++) {
            mainFFT.forward(mainBuffer, FFTConstants.fftVectorLength * k, FFTConstants.fftVectorLength);
            int output = 0;
            if (!mainFFT.hasFrequency(computeFrequency(FFTConstants.frequency0))) { // is not 0
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
                baos.write(output);
            } catch (IOException e) {
                //do nothing
            }
        }
    }
}
