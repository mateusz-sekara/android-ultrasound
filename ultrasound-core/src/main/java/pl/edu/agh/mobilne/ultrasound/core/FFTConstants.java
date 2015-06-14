package pl.edu.agh.mobilne.ultrasound.core;

public interface FFTConstants {
    public static final int sampleRate = 48000;
    public static final int baseSampleRate = 256;

    public static final int frequency0 = 19000;
    public static final int baseFrequency = 20000;
    public static final int stepFrequency = 1000;

    public static final int recordMultiplier = 256;
    public static final int recordSampleRate = baseSampleRate * recordMultiplier;

    public static final int fftMultiplier = 4;
    public static final int fftVectorLength = baseSampleRate * fftMultiplier;
    public static final int fftToRecordMultiplier = recordMultiplier / fftMultiplier;
    public static final float freqIndexRange = ((float) sampleRate / (fftVectorLength));

    public static final int syncFftMultiplier = 8;
    public static final int syncFftVectorLength = fftVectorLength / syncFftMultiplier;
    public static final int syncFftToRecordMultiplier = fftToRecordMultiplier * syncFftMultiplier;
    public static final float syncFftFreqIndexRange = freqIndexRange / syncFftMultiplier;

    public static final int SILENCE = -1;
    public static final int ZERO = 0;
    public static final int OTHER = -2;
    public static final int ERROR = -3;
}
