package pl.edu.agh.mobilne.ultrasound.android.app.model;

import java.util.Arrays;

public class Token {

    private final byte[] innerToken;

    public Token(byte[] innerToken) {
        this.innerToken = innerToken;
    }

    public Token(String tokenString) {
        char[] characterArray = tokenString.toCharArray();
        innerToken = new byte[characterArray.length];

        for (int i = 0; i < characterArray.length; ++i) {
            //possible overflow
            innerToken[i] = (byte) (characterArray[i] - Byte.MIN_VALUE);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Token token = (Token) o;

        if (!Arrays.equals(innerToken, token.innerToken)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return innerToken != null ? Arrays.hashCode(innerToken) : 0;
    }

    private String characterFromByte(byte b) {
        return Character.toString((char) ((int) b + Byte.MIN_VALUE));
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        for (byte b : innerToken) {
            stringBuilder.append(characterFromByte(b));
        }

        return stringBuilder.toString();
    }
}
