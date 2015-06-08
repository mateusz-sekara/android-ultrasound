package pl.edu.agh.mobilne.ultrasound.pc.app.fft;


//TODO create common version and move to core module
public class FFT extends FourierTransform {
    public FFT(int timeSize, float sampleRate) {
        super(timeSize, sampleRate);
        if ((timeSize & (timeSize - 1)) != 0)
            throw new IllegalArgumentException(
                    "FFT: timeSize must be a power of two.");
        buildReverseTable();
        buildTrigTables();
    }

    protected void allocateArrays() {
        spectrum = new float[timeSize / 2 + 1];
        real = new float[timeSize];
        imag = new float[timeSize];
    }

    private void fft() {
        for (int halfSize = 1; halfSize < real.length; halfSize *= 2) {
            float phaseShiftStepR = cos(halfSize);
            float phaseShiftStepI = sin(halfSize);
            // current phase shift
            float currentPhaseShiftR = 1.0f;
            float currentPhaseShiftI = 0.0f;
            for (int fftStep = 0; fftStep < halfSize; fftStep++) {
                for (int i = fftStep; i < real.length; i += 2 * halfSize) {
                    int off = i + halfSize;
                    float tr = (currentPhaseShiftR * real[off]) - (currentPhaseShiftI * imag[off]);
                    float ti = (currentPhaseShiftR * imag[off]) + (currentPhaseShiftI * real[off]);
                    real[off] = real[i] - tr;
                    imag[off] = imag[i] - ti;
                    real[i] += tr;
                    imag[i] += ti;
                }
                float tmpR = currentPhaseShiftR;
                currentPhaseShiftR = (tmpR * phaseShiftStepR) - (currentPhaseShiftI * phaseShiftStepI);
                currentPhaseShiftI = (tmpR * phaseShiftStepI) + (currentPhaseShiftI * phaseShiftStepR);
            }
        }
    }

    public void forward(byte[] buffer) {
        if (buffer.length != timeSize) {
            throw new IllegalArgumentException("FFT.forward: The length of the passed sample buffer must be equal to timeSize().");
        }
        // copy samples to real/imag in bit-reversed order
        bitReverseSamples(buffer, 0, buffer.length);
        // perform the fft
        fft();
        // fill the spectrum buffer with amplitudes
        fillSpectrum();
    }

    public void forward(byte[] buffer, int bufferOffset, int length) {
        if (buffer.length < bufferOffset + length && length != timeSize) {
            throw new IllegalArgumentException("FFT.forward: The length of the passed sample buffer must be equal to timeSize().");
        }
        // copy samples to real/imag in bit-reversed order
        bitReverseSamples(buffer, bufferOffset, length);
        // perform the fft
        fft();
        // fill the spectrum buffer with amplitudes
        fillSpectrum();
    }

    private int[] reverse;

    private void buildReverseTable() {
        int N = timeSize;
        reverse = new int[N];

        // set up the bit reversing table
        reverse[0] = 0;
        for (int limit = 1, bit = N / 2; limit < N; limit <<= 1, bit >>= 1)
            for (int i = 0; i < limit; i++)
                reverse[i + limit] = reverse[i] + bit;
    }

    // copies the values in the samples array into the real array
    // in bit reversed order. the imag array is filled with zeros.
    private void bitReverseSamples(byte[] samples, int bufferOffset, int length) {
        for (int i = 0; i < length; i++) {
            real[i] = (float) samples[bufferOffset + reverse[i]];
            imag[i] = 0.0f;
        }
    }

    // lookup tables

    private float[] sinlookup;
    private float[] coslookup;

    private float sin(int i) {
        return sinlookup[i];
    }

    private float cos(int i) {
        return coslookup[i];
    }

    private void buildTrigTables() {
        int N = timeSize;
        sinlookup = new float[N];
        coslookup = new float[N];
        for (int i = 0; i < N; i++) {
            sinlookup[i] = (float) Math.sin(-(float) Math.PI / i);
            coslookup[i] = (float) Math.cos(-(float) Math.PI / i);
        }
    }

    public boolean hasFrequency(float frequency) {
        int index = freqToIndex(frequency);
        int indexMin = (index - 5 > 0) ? index - 5 : 0;
        int indexMax = (index + 5 < spectrum.length) ? index + 5 : spectrum.length - 1;
        int indexFreqMin = (index - 1 > 0) ? index - 1 : 0;
        int indexFreqMax = (index + 1 < spectrum.length) ? index + 1 : spectrum.length - 1;
        float avg = 0;
        for (int i = indexMin; i <= indexMax; i++) {
            if (i < indexFreqMin || i > indexFreqMax) {
                avg += spectrum[i];
            }
        }
        avg /= indexMax - indexMin - (indexFreqMax - indexFreqMin);
        /*float freqAvg = 0;
        for(int i = indexFreqMin; i <= indexFreqMax; i++) {
            freqAvg += spectrum[i];
        }
        freqAvg /= indexFreqMax - indexFreqMin + 1;*/
        if (spectrum[index] > avg * 5) {
            //for(int i = indexMin; i <= indexMax; i++) {
            System.out.print(frequency + "Hz: " + spectrum[index] + ";    ");
            //}
            //System.out.println();
        }

        return spectrum[index] > avg * 5; // TODO: check multiplier
    }
}
