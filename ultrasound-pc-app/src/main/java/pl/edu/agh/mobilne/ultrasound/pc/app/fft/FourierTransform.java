package pl.edu.agh.mobilne.ultrasound.pc.app.fft;

//TODO create common version and move to core module
public abstract class FourierTransform {
    protected int timeSize;
    protected int sampleRate;
    protected float bandWidth;
    protected float[] real;
    protected float[] imag;
    protected float[] spectrum;

    FourierTransform(int timeSize, float sampleRate) {
        this.timeSize = timeSize;
        this.sampleRate = (int) sampleRate;
        bandWidth = (2f / timeSize) * ((float) sampleRate / 2f);
        allocateArrays();
    }

    protected abstract void allocateArrays();

    protected void fillSpectrum() {
        for (int i = 0; i < spectrum.length; i++) {
            spectrum[i] = (float) Math.sqrt(real[i] * real[i] + imag[i] * imag[i]);
        }
    }

    public float getBandWidth() {
        return bandWidth;
    }

    public int freqToIndex(float freq) {
        // special case: freq is lower than the bandwidth of spectrum[0]
        if (freq < getBandWidth() / 2) return 0;
        // special case: freq is within the bandwidth of spectrum[spectrum.length - 1]
        if (freq > sampleRate / 2 - getBandWidth() / 2) return spectrum.length - 1;
        // all other cases
        float fraction = freq / (float) sampleRate;
        int i = Math.round(timeSize * fraction);
        return i;
    }

    public float indexToFreq(int i) {
        float bw = getBandWidth();
        // special case: the width of the first bin is half that of the others.
        //               so the center frequency is a quarter of the way.
        if (i == 0) return bw * 0.25f;
        // special case: the width of the last bin is half that of the others.
        if (i == spectrum.length - 1) {
            float lastBinBeginFreq = (sampleRate / 2) - (bw / 2);
            float binHalfWidth = bw * 0.25f;
            return lastBinBeginFreq + binHalfWidth;
        }
        // the center frequency of the ith band is simply i*bw
        // because the first band is half the width of all others.
        // treating it as if it wasn't offsets us to the middle
        // of the band.
        return i * bw;
    }

    public float[] getSpectrum() {
        return spectrum;
    }

}
