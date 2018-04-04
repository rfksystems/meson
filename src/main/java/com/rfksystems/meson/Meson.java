package com.rfksystems.meson;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.rfksystems.meson.jackson.MesonDeserializer;
import com.rfksystems.meson.jackson.MesonSerializer;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static com.rfksystems.meson.MesonUtils.*;

@JsonSerialize(using = MesonSerializer.class)
@JsonDeserialize(using = MesonDeserializer.class)
public class Meson implements Serializable, Comparable<Meson> {
    public static final int TIME_SIZE_BYTES = 6;
    public static final int GENERATOR_ID_SIZE = 4;
    public static final int COUNTER_SIZE = 4;
    public static final int BUFFER_SIZE = TIME_SIZE_BYTES + GENERATOR_ID_SIZE + COUNTER_SIZE;
    public static final int BORDERLINE_COUNTER = Integer.MAX_VALUE - 1000;
    public static final long MAX_TIME = 281474976710655L;
    public static final long MIN_TIME = 0L;
    private static final long serialVersionUID = 4304780938665765028L;
    private final static AtomicInteger COUNTER = new AtomicInteger(new SecureRandom().nextInt());
    private final long time;
    private final int sequence;
    private final byte[] generatorId;

    public Meson() {
        this.time = System.currentTimeMillis();
        this.sequence = COUNTER.getAndIncrement();
        this.generatorId = MesonGeneratorIdentity.get();

        if (sequence > BORDERLINE_COUNTER) {
            COUNTER.set(new SecureRandom().nextInt());
        }
    }

    public Meson(final long time, final byte[] generatorId, final int sequence) {
        this.time = time;
        this.sequence = sequence;
        this.generatorId = generatorId;
        validate();
    }

    public Meson(final String string) {
        final String hexString = -1 == string.indexOf('-') ? string : string.replaceAll("-", "");

        if (28 != hexString.length()) {
            throw new IllegalArgumentException();
        }

        final byte[] bytes = hexToBytes(hexString);

        this.time = longFromUInt48(bytes, 0);

        this.generatorId = new byte[]{
                bytes[6],
                bytes[7],
                bytes[8],
                bytes[9]
        };

        this.sequence = intFromBytes(bytes, 10);
        validate();
    }

    public Meson(final ByteBuffer byteBuffer) {
        this(byteBuffer.array());
    }

    public Meson(final byte[] bytes) {
        if (BUFFER_SIZE != bytes.length) {
            throw new IllegalArgumentException();
        }

        this.time = longFromUInt48(bytes, 0);

        this.generatorId = new byte[]{
                bytes[6],
                bytes[7],
                bytes[8],
                bytes[9]
        };

        this.sequence = intFromBytes(bytes, 10);
        validate();
    }

    public static byte[] directToByteArray() {
        return toByteArray(
                System.currentTimeMillis(),
                getNextSequence(),
                MesonGeneratorIdentity.get()
        );
    }

    public static ByteBuffer directToByteBuffer() {
        return ByteBuffer.wrap(directToByteArray());
    }

    public static String directToHexString() {
        return bytesToHex(directToByteArray());
    }

    public static String directToFormatString() {
        return bytesToHex(uInt48ToBytes(System.currentTimeMillis()))
                + "-" + bytesToHex(MesonGeneratorIdentity.get())
                + "-" + bytesToHex(intToBytes(getNextSequence()));
    }

    private static int getNextSequence() {
        final int sequence = COUNTER.getAndIncrement();

        if (sequence > BORDERLINE_COUNTER) {
            COUNTER.set(new SecureRandom().nextInt());
        }

        return sequence;
    }

    private static byte[] toByteArray(final long time, final int sequence, final byte[] generatorId) {
        final byte[] timestampUInt48 = uInt48ToBytes(time);
        final byte[] counterInt32 = intToBytes(sequence);

        final byte[] id = new byte[BUFFER_SIZE];
        id[0] = timestampUInt48[0];
        id[1] = timestampUInt48[1];
        id[2] = timestampUInt48[2];
        id[3] = timestampUInt48[3];
        id[4] = timestampUInt48[4];
        id[5] = timestampUInt48[5];

        id[6] = generatorId[0];
        id[7] = generatorId[1];
        id[8] = generatorId[2];
        id[9] = generatorId[3];

        id[10] = counterInt32[0];
        id[11] = counterInt32[1];
        id[12] = counterInt32[2];
        id[13] = counterInt32[3];

        return id;
    }

    public byte[] toByteArray() {
        return toByteArray(time, sequence, generatorId);
    }

    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(toByteArray());
    }

    public String toHexString() {
        return bytesToHex(toByteArray());
    }

    public String toFormatString() {
        return getTimeHex() + "-" + getGeneratorIdHex() + "-" + getSequenceHex();
    }

    public long getTime() {
        return time;
    }

    public Instant getInstant() {
        return Instant.ofEpochMilli(time);
    }

    public LocalDateTime getLocalDateTime() {
        return LocalDateTime.from(getInstant());
    }

    public byte[] getTimeBytes() {
        return uInt48ToBytes(time);
    }

    public String getTimeHex() {
        return bytesToHex(getTimeBytes());
    }

    public int getSequence() {
        return sequence;
    }

    public byte[] getSequenceBytes() {
        return intToBytes(sequence);
    }

    public String getSequenceHex() {
        return bytesToHex(getSequenceBytes());
    }

    public byte[] getGeneratorId() {
        return generatorId;
    }

    public String getGeneratorIdHex() {
        return bytesToHex(generatorId);
    }

    public int getCounterCurrentValue() {
        return COUNTER.get();
    }

    @Override
    public int compareTo(final Meson other) {
        if (null == other) {
            throw new NullPointerException();
        }

        final byte[] selfBytes = toByteArray();
        final byte[] otherBytes = other.toByteArray();

        for (int i = 0; i < BUFFER_SIZE; i++) {
            if (selfBytes[i] != otherBytes[i]) {
                return ((selfBytes[i] & 0xff) < (otherBytes[i] & 0xff)) ? -1 : 1;
            }
        }

        return 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, sequence, Arrays.hashCode(generatorId));
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Meson)) {
            return false;
        }

        final Meson meson = (Meson) other;

        return time == meson.time &&
                sequence == meson.sequence &&
                Arrays.equals(generatorId, meson.generatorId);
    }

    @Override
    public String toString() {
        return "Meson{" + toHexString() + '}';
    }

    private void validate() {
        if (time > MAX_TIME || time < MIN_TIME) {
            throw new IllegalArgumentException(String.format(
                    "Time must be between MIN_TIME %d and MAX_TIME %d", MIN_TIME, MAX_TIME));
        }

        if (GENERATOR_ID_SIZE != this.generatorId.length) {
            throw new IllegalArgumentException(String.format("Generator ID must be %d bytes", GENERATOR_ID_SIZE));
        }

        if (0 > sequence) {
            throw new IllegalArgumentException("Sequence must start from positive zero");
        }
    }
}
