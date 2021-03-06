package perturbation.perturbator;

import java.math.BigInteger;

/**
 * Created by spirals on 09/03/16.
 * All of the methods will return a pseudo random (from java.util.Random()) of the appropriate type
 * except for pboolean which will return the complementary of the value.
 */
public class RndPerturbatorImpl implements Perturbator {

    @Override
    public boolean pboolean(boolean value) {
        return new java.util.Random().nextBoolean();
    }

    @Override
    public byte pbyte(byte value) {
        return (byte) (new java.util.Random().nextInt());
    }

    @Override
    public short pshort(short value) {
        return (short) (new java.util.Random().nextInt());
    }

    @Override
    public int pint(int value) {
        return new java.util.Random().nextInt();
    }

    @Override
    public long plong(long value) {
        return new java.util.Random().nextLong();
    }

    @Override
    public char pchar(char value) {
        return  (char)(new java.util.Random().nextInt());
    }

    @Override
    public float pfloat(float value) {
        return  new java.util.Random().nextFloat();
    }

    @Override
    public double pdouble(double value) {
        return (double)(new java.util.Random().nextFloat());
    }

    @Override
    public BigInteger pBigInteger(BigInteger value) {
        return BigInteger.valueOf(new java.util.Random().nextLong());
    }

    @Override
    public String toString() {
        return "RAND";
    }
}
