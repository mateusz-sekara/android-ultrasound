package pl.edu.agh.mobilne.ultrasound.core;

public class Utils {

    public static float computeFrequency(int closestFreq) {
        return Math.round(closestFreq / FFTConstants.freqIndexRange) * FFTConstants.freqIndexRange;
    }
}
