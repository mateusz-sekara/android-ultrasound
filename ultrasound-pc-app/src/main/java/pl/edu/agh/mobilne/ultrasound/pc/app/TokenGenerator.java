package pl.edu.agh.mobilne.ultrasound.pc.app;

import java.util.Arrays;
import java.util.Random;

public class TokenGenerator {

    private Random random = new Random();

    public byte[] getToken() {
        byte[] tokenData = new byte[16];

        random.nextBytes(tokenData);
        System.out.println(Arrays.toString(tokenData));

        return tokenData;
    }
}
