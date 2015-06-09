package pl.edu.agh.mobilne.ultrasound.pc.app;

import java.util.Arrays;

import pl.edu.agh.mobilne.ultrasound.core.TokenGenerator;

public class SendRunner {

    private static byte[] createToken() {
        String tokenString = TokenGenerator.getStringToken();
        byte[] tokenData = TokenGenerator.convertFromString(tokenString);

        System.out.println("Generating token");
        System.out.println(tokenString + "\t" + Arrays.toString(tokenData));

        return tokenData;
    }

    public static void main(String[] argv) throws Exception {
        SenderPC senderPC = new SenderPC(new byte[]{0});

        new Thread(senderPC).start();

        Thread.sleep(500);
        senderPC.setData(createToken());
        Thread.sleep(5000);
        senderPC.setData(createToken());
        Thread.sleep(5000);
        senderPC.setData(createToken());
        Thread.sleep(5000);
        senderPC.setData(createToken());
    }
}
