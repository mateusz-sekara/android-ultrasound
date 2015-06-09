package pl.edu.agh.mobilne.ultrasound.core;

import java.io.Closeable;
import java.io.IOException;

public class Utils {

    public static float computeFrequency(int closestFreq) {
        return Math.round(closestFreq / FFTConstants.freqIndexRange) * FFTConstants.freqIndexRange;
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
            }
        }
    }
}
