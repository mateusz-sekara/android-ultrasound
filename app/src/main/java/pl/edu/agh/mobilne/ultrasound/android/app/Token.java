package pl.edu.agh.mobilne.ultrasound.android.app;

import java.util.Arrays;

public class Token {

    private final byte[] innerToken;

    public Token(byte[] innerToken) {
        this.innerToken = innerToken;
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

    @Override
    public String toString() {
        //fixme add pretty printer
        return "Token{" +
                "innerToken=" + Arrays.toString(innerToken) +
                '}';
    }
}
