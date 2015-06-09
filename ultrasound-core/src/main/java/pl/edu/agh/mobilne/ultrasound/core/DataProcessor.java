package pl.edu.agh.mobilne.ultrasound.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

//fixme replace sysouts with logger which will be available on both android and pc version
public class DataProcessor {

    private static final int FAULT_TOLERANCE = 3;

    private State state = State.NOT_INITIALIZED;
    private InputStream is;
    private OutputStream os;

    private byte[] data = new byte[1024];

    private int pos;
    private int count;

    public DataProcessor(InputStream is, OutputStream os) {
        this.is = is;
        this.os = os;
    }

    public void processData() throws IOException {
        if (state == State.NOT_INITIALIZED) {
            findFirstSilence();
        }
        if (state == State.INITIALIZATION_SILENCE_1) {
            forwardInitAndFindZero();
        }
        if (state == State.INITIALIZATION_ZERO) {
            forwardInitAndFindSecondSilence();
        }
        if (state == State.INITIALIZATION_SILENCE_2) {
            forwardInitAndFindFirstData();
        }
        if (state == State.INITIALIZED) {
            while (true) {
                byte highOrderBytes = getNextByte();
                byte lowOrderBytes = getNextByte();
                if (highOrderBytes == FFTConstants.ERROR || lowOrderBytes == FFTConstants.ERROR) {
                    state = State.NOT_INITIALIZED;
                    return;
                }
                if (highOrderBytes == FFTConstants.SILENCE && lowOrderBytes == FFTConstants.ZERO) {
                    state = State.INITIALIZATION_SILENCE_2;
                    return;
                }
                int output = ((int) highOrderBytes) * 16 + lowOrderBytes;
                os.write(output);
            }
        }

    }

    private void findFirstSilence() {
        while (true) {
            if (getCurrentByte() == FFTConstants.SILENCE) {
                break;
            } else {
                getCurrentByteAndInc();
            }
        }
        state = State.INITIALIZATION_SILENCE_1;
    }

    private void forwardInitAndFindZero() {
        if (initIterateAndFind(FFTConstants.SILENCE, FFTConstants.ZERO)) {
            state = State.INITIALIZATION_ZERO;
        }
    }

    private void forwardInitAndFindSecondSilence() {
        if (initIterateAndFind(FFTConstants.ZERO, FFTConstants.SILENCE)) {
            state = State.INITIALIZATION_SILENCE_2;
        }
    }

    private void forwardInitAndFindFirstData() {
        if (initIterateAndFind(FFTConstants.SILENCE, FFTConstants.OTHER)) {
            state = State.INITIALIZED;
        }
    }

    private boolean initIterateAndFind(int actualByte, int toFindByte) {
        int actualFoundCount = 0;
        int errors = 0;
        for (int i = 0; i < 5; i++) {
            byte processedByte = getCurrentByte();
            if (processedByte == actualByte) {
                actualFoundCount++;
                getCurrentByteAndInc();
            } else if (actualFoundCount >= 4) {
                if (processedByte == toFindByte || toFindByte == FFTConstants.OTHER) {
                    break;
                } else {
                    error();
                    return false;
                }
            } else {
                errors++;
                getCurrentByteAndInc();
            }
        }
        if (errors > 1) {
            error();
            return false;
        }
        return true;
    }

    private byte getNextByte() {
        byte[] dataBytesCount = new byte[17]; // 1/2 byte + silence bit
        for (int i = 0; i < 5; i++) {
            byte processedByte = getCurrentByteAndInc();
            if (processedByte == FFTConstants.SILENCE) {
                dataBytesCount[16]++;
            } else {
                dataBytesCount[processedByte]++;
            }
        }
        for (byte i = 0; i < 16; i++) {
            if (dataBytesCount[i] >= FAULT_TOLERANCE) {
                return i;
            }
        }
        if (dataBytesCount[16] >= FAULT_TOLERANCE) {
            return FFTConstants.SILENCE;
        }
        return FFTConstants.ERROR;
    }

    private void error() {
        System.out.println("Not found initialization part: " + state);
        state = State.NOT_INITIALIZED;
    }

    private byte getCurrentByteAndInc() {
        checkBuffer();
        return data[pos++];
    }

    private byte getCurrentByte() {
        checkBuffer();
        return data[pos];
    }

    private void checkBuffer() {
        if (pos >= count) {
            pos = 0;
            try {
                count = is.read(data, 0, data.length);
                if (count < 0) {
                    throw new IllegalArgumentException("EOF in state: " + state);
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new IllegalArgumentException("error during reading from stream in state: " + state, e);
            }
        }
    }

    private enum State {
        NOT_INITIALIZED, INITIALIZATION_SILENCE_1, INITIALIZATION_ZERO, INITIALIZATION_SILENCE_2, INITIALIZED
    }
}
