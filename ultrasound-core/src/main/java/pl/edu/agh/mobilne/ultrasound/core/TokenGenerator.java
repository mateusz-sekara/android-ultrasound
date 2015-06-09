package pl.edu.agh.mobilne.ultrasound.core;

import org.apache.commons.lang3.RandomStringUtils;

public class TokenGenerator {
    private static final int DEFAULT_TOKEN_LENGTH = 16;

    private TokenGenerator() {
        //only static use
    }

    public static byte[] getByteToken() {
        return getByteToken(DEFAULT_TOKEN_LENGTH);
    }

    public static byte[] getByteToken(int length) {
        String tokenString = getStringToken(length);
        return convertFromString(tokenString);
    }

    public static String getStringToken() {
        return getStringToken(DEFAULT_TOKEN_LENGTH);
    }

    public static String getStringToken(int length) {
        return RandomStringUtils.randomAscii(length);
    }

    public static String convertFromByteArray(byte[] tokenData) {
        StringBuilder stringBuilder = new StringBuilder();

        for (byte b : tokenData) {
            stringBuilder.append((char) b);
        }

        return stringBuilder.toString();
    }

    public static byte[] convertFromString(String tokenString) {
        char[] characterArray = tokenString.toCharArray();
        byte[] tokenData = new byte[characterArray.length];

        for (int i = 0; i < characterArray.length; ++i) {
            tokenData[i] = (byte) characterArray[i];
        }

        return tokenData;
    }

}
