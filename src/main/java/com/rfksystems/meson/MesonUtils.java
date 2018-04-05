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

class MesonUtils {
    private static final char[] HEX_DICT = new char[]{
            '0', '1', '2', '3',
            '4', '5', '6', '7',
            '8', '9', 'a', 'b',
            'c', 'd', 'e', 'f'
    };

    static String bytesToHex(byte[] bytes) {
        final char[] hexChars = new char[bytes.length * 2];

        for (int j = 0; j < bytes.length; j++) {
            final int v = bytes[j] & 0xFF;

            hexChars[j * 2] = HEX_DICT[v >>> 4];
            hexChars[j * 2 + 1] = HEX_DICT[v & 0x0F];
        }

        return new String(hexChars);
    }

    static byte[] hexToBytes(final String hexString) {
        if (0 != hexString.length() % 2) {
            throw new IllegalArgumentException("Hex string needs to be even-length: " + hexString);
        }

        final byte[] buffer = new byte[Meson.BUFFER_SIZE_BYTES];

        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = (byte) Integer.parseInt(hexString.substring(i * 2, i * 2 + 2), 16);
        }

        return buffer;
    }

    static byte[] intToBytes(final int value) {
        return new byte[]{
                (byte) ((value >> 24) & 0XFFL),
                (byte) ((value >> 16) & 0XFFL),
                (byte) ((value >> 8) & 0XFFL),
                (byte) (value & 0XFFL),
        };
    }

    static byte[] uInt48ToBytes(final long value) {
        return new byte[]{
                (byte) ((value >> 40) & 0XFFL),
                (byte) ((value >> 32) & 0XFFL),
                (byte) ((value >> 24) & 0XFFL),
                (byte) ((value >> 16) & 0XFFL),
                (byte) ((value >> 8) & 0XFFL),
                (byte) (value & 0XFFL),
        };
    }

    static byte[] longToBytes(final long value) {
        return new byte[]{
                (byte) ((value >> 56) & 0XFFL),
                (byte) ((value >> 48) & 0XFFL),
                (byte) ((value >> 40) & 0XFFL),
                (byte) ((value >> 32) & 0XFFL),
                (byte) ((value >> 24) & 0XFFL),
                (byte) ((value >> 16) & 0XFFL),
                (byte) ((value >> 8) & 0XFFL),
                (byte) (value & 0XFFL),
        };
    }

    static long longFromUInt48(byte[] bytes, final int offset) {
        return ((bytes[offset] & 0xFFL) << 40
                | (bytes[offset + 1] & 0xFFL) << 32
                | (bytes[offset + 2] & 0xFFL) << 24
                | (bytes[offset + 3] & 0xFFL) << 16
                | (bytes[offset + 4] & 0xFFL) << 8
                | (bytes[offset + 5] & 0xFFL));
    }

    static int intFromBytes(byte[] bytes, final int offset) {
        return ((bytes[offset] & 0xFF) << 24
                | (bytes[offset + 1] & 0xFF) << 16
                | (bytes[offset + 2] & 0xFF) << 8
                | (bytes[offset + 3] & 0xFF));
    }
}
