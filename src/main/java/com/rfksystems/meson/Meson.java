/*
 * Copyright 2018 RFK Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

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

/**
 * Meson identity container and public API.
 */
@JsonSerialize(using = MesonSerializer.class)
@JsonDeserialize(using = MesonDeserializer.class)
public class Meson implements Serializable, Comparable<Meson> {
    /**
     * Size of the time field in number of bytes.
     */
    public static final int TIME_SIZE_BYTES = 6;

    /**
     * Size of the generator id field in number of bytes.
     */
    public static final int GENERATOR_ID_SIZE_BYTES = 4;

    /**
     * Size of the incremental sequence in number of bytes.
     */
    public static final int COUNTER_SIZE_BYTES = 4;

    /**
     * Total size of Meson identity in number of bytes.
     */
    public static final int BUFFER_SIZE_BYTES = TIME_SIZE_BYTES + GENERATOR_ID_SIZE_BYTES + COUNTER_SIZE_BYTES;

    /**
     * Topmost value for incremental sequence value, after which the value will be reset to a random number.
     */
    public static final int BORDERLINE_COUNTER_VALUE = Integer.MAX_VALUE - 1000;

    /**
     * Largest possible timestamp value Meson identity supports.
     */
    public static final long MAX_TIME = 281474976710655L;

    /**
     * Smallest possible timestamp value Meson identity supports.
     */
    public static final long MIN_TIME = 0L;

    private static final long serialVersionUID = 4304780938665765028L;

    /**
     * Instance counter. For every created Meson identity using this.
     */
    private final static AtomicInteger COUNTER = new AtomicInteger(new SecureRandom().nextInt());
    private final long time;
    private final int sequence;
    private final byte[] generatorId;

    /**
     * Create a new Meson identity for current time, with current machine identity and current sequence.
     */
    public Meson() {
        this.time = System.currentTimeMillis();
        this.sequence = getSequenceAndIncrement();
        this.generatorId = MesonGeneratorIdentity.get();
    }

    /**
     * Create instance of Meson identity for known time, generator identity and sequence id
     *
     * @param time        UNIX timestamp in milliseconds since epoch start, with minimum value of {@link Meson#MIN_TIME} and
     *                    maximum value of {@link Meson#MAX_TIME}.
     * @param generatorId ID of the generator, exactly {@link Meson#GENERATOR_ID_SIZE_BYTES} in size.
     * @param sequence    Sequence number of this identity, signed 32 bit integer between 0 and {@link Integer#MAX_VALUE}.
     */
    public Meson(final long time, final byte[] generatorId, final int sequence) {
        this.time = time;
        this.sequence = sequence;
        this.generatorId = generatorId;
        validate();
    }

    /**
     * Create instance of Meson identity from a hex string.
     *
     * @param string String representation of Meson identity, compact or formatted.
     */
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

    /**
     * Create instance of Meson from {@link ByteBuffer} with byte representation of Meson identity.
     *
     * @param byteBuffer {@link ByteBuffer} representation of Meson identity.
     */
    public Meson(final ByteBuffer byteBuffer) {
        this(byteBuffer.array());
    }

    /**
     * Create instance of Meson from byte array with byte representation of Meson identity.
     *
     * @param bytes byte array representation of Meson identity.
     */
    public Meson(final byte[] bytes) {
        if (BUFFER_SIZE_BYTES != bytes.length) {
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

    /**
     * Create a byte array representation of Meson identity without creating {@link Meson} object.
     *
     * @return byte array representation of Meson identity.
     */
    public static byte[] directToByteArray() {
        return toByteArray(
                System.currentTimeMillis(),
                MesonGeneratorIdentity.get(),
                getSequenceAndIncrement()
        );
    }

    /**
     * Create a {@link ByteBuffer} representation of Meson identity without creating {@link Meson} object.
     *
     * @return {@link ByteBuffer} representation of Meson identity.
     */
    public static ByteBuffer directToByteBuffer() {
        return ByteBuffer.wrap(directToByteArray());
    }

    /**
     * Create a compact-format hex String representation of Meson identity without creating {@link Meson} object.
     *
     * @return Compact-format String representation of Meson identity.
     */
    public static String directToHexString() {
        return bytesToHex(directToByteArray());
    }

    /**
     * Create a pretty-format hex String representation of Meson identity without creating {@link Meson} object.
     *
     * @return Pretty-format String representation of Meson identity.
     */
    public static String directToFormatString() {
        return bytesToHex(uInt48ToBytes(System.currentTimeMillis()))
                + "-" + bytesToHex(MesonGeneratorIdentity.get())
                + "-" + bytesToHex(intToBytes(getSequenceAndIncrement()));
    }

    /**
     * Retrieve current sequence number and increment the counter to next value.
     *
     * @return current sequence number.
     */
    private static int getSequenceAndIncrement() {
        final int sequence = COUNTER.getAndIncrement();

        if (sequence > BORDERLINE_COUNTER_VALUE) {
            COUNTER.set(new SecureRandom().nextInt());
        }

        return sequence;
    }

    /**
     * Create a byte array representation of Meson identity given time, sequence and generator ID
     *
     * @param time        UNIX timestamp in milliseconds since epoch start, with minimum value of {@link Meson#MIN_TIME} and
     *                    maximum value of {@link Meson#MAX_TIME}.
     * @param generatorId ID of the generator, exactly {@link Meson#GENERATOR_ID_SIZE_BYTES} in size.
     * @param sequence    Sequence number of this identity, signed 32 bit integer between 0 and {@link Integer#MAX_VALUE}.
     * @return byte array representation of Meson identity.
     */
    private static byte[] toByteArray(
            final long time,
            final byte[] generatorId,
            final int sequence
    ) {
        final byte[] timestampUInt48 = uInt48ToBytes(time);
        final byte[] counterInt32 = intToBytes(sequence);

        final byte[] id = new byte[BUFFER_SIZE_BYTES];
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

    /**
     * Get a number - current position of internal counter for Meson sequence numbers.
     *
     * @return current value of the counter.
     */
    public static int currentCounterValue() {
        return COUNTER.get();
    }

    /**
     * Get a byte array representation of current generator identity.
     *
     * @return byte array representation current generator identity.
     */
    public static byte[] generatorIdAsBytes() {
        return Arrays.copyOf(MesonGeneratorIdentity.get(), 4);
    }

    /**
     * Get a hex string representation of current generator identity.
     *
     * @return hex string representation current generator identity.
     */
    public static String generatorIdAsHex() {
        return bytesToHex(MesonGeneratorIdentity.get());
    }

    /**
     * Create a byte array representation of this Meson identity.
     *
     * @return byte array containing bytes that represent this Meson identity.
     */
    public byte[] toByteArray() {
        return toByteArray(time, generatorId, sequence);
    }

    /**
     * Create a {@link ByteBuffer} representation of this Meson identity.
     *
     * @return {@link ByteBuffer} containing bytes that represent this Meson identity.
     */
    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(toByteArray());
    }

    /**
     * Create a compact-format String representation of this Meson identity.
     *
     * @return String containing bytes that represent this Meson identity.
     */
    public String toHexString() {
        return bytesToHex(toByteArray());
    }

    /**
     * Create a pretty-format String representation of this Meson identity.
     *
     * @return String containing bytes that represent this Meson identity.
     */
    public String toFormatString() {
        return getTimeHex() + "-" + getGeneratorIdHex() + "-" + getSequenceHex();
    }

    /**
     * Get a timestamp of when this Meson identity was created.
     *
     * @return UNIX timestamp in milliseconds since epoch start, with minimum value of {@link Meson#MIN_TIME} and
     * maximum value of {@link Meson#MAX_TIME}
     */
    public long getTime() {
        return time;
    }

    /**
     * Create a instance of {@link Instant} of when this Meson identity was created.
     *
     * @return Instance of {@link Instant} that corresponds Meson identity creation time.
     */
    public Instant getInstant() {
        return Instant.ofEpochMilli(time);
    }

    /**
     * Create a instance of {@link LocalDateTime} of when this Meson identity was created.
     *
     * @return Instance of {@link LocalDateTime} that corresponds Meson identity creation time.
     */
    public LocalDateTime getLocalDateTime() {
        return LocalDateTime.from(getInstant());
    }

    /**
     * Create a byte array representation of when this Meson was created.
     *
     * @return byte array that holds a timestamp - 48-bit Big-Endian number.
     */
    public byte[] getTimeBytes() {
        return uInt48ToBytes(time);
    }

    /**
     * Create a hex encoded String representation of when this Meson was created.
     *
     * @return hex encoded String that holds a timestamp.
     */
    public String getTimeHex() {
        return bytesToHex(getTimeBytes());
    }

    /**
     * Get an integer representation of sequence number of this {@link Meson} instance.
     *
     * @return sequence number value as integer
     */
    public int getSequence() {
        return sequence;
    }

    /**
     * Get an byte array representation of sequence number of this {@link Meson} instance.
     *
     * @return sequence number value as byte array
     */
    public byte[] getSequenceBytes() {
        return intToBytes(sequence);
    }

    /**
     * Get an byte array representation of sequence number of this {@link Meson} instance.
     *
     * @return sequence number value as hex string
     */
    public String getSequenceHex() {
        return bytesToHex(getSequenceBytes());
    }

    /**
     * Get an byte array representation of generator id of this {@link Meson} instance.
     *
     * @return generator id value as byte array
     */
    public byte[] getGeneratorId() {
        return generatorId;
    }

    /**
     * Get an hex string representation of generator id of this {@link Meson} instance.
     *
     * @return generator id value as hex string
     */
    public String getGeneratorIdHex() {
        return bytesToHex(generatorId);
    }

    @Override
    public int compareTo(final Meson other) {
        if (null == other) {
            throw new NullPointerException();
        }

        final byte[] selfBytes = toByteArray();
        final byte[] otherBytes = other.toByteArray();

        for (int i = 0; i < BUFFER_SIZE_BYTES; i++) {
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

        if (GENERATOR_ID_SIZE_BYTES != this.generatorId.length) {
            throw new IllegalArgumentException(String.format("Generator ID must be %d bytes", GENERATOR_ID_SIZE_BYTES));
        }

        if (0 > sequence) {
            throw new IllegalArgumentException("Sequence must start from positive zero");
        }
    }
}
