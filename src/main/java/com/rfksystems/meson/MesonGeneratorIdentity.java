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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Enumeration;
import java.util.zip.CRC32;

import static com.rfksystems.meson.MesonUtils.intToBytes;
import static com.rfksystems.meson.MesonUtils.longToBytes;
import static java.lang.management.ManagementFactory.getRuntimeMXBean;

class MesonGeneratorIdentity {
    private final static byte[] IDENTITY = createMachineId();

    private static byte[] createMachineId() {
        final CRC32 crc32 = new CRC32();

        try {
            crc32.update(tryGetCGroup());
            crc32.update(tryGetNetworkHostname());
            crc32.update(createProcessIdentifier());
        } catch (final Throwable t) {
            // No-op
        }

        final Enumeration<NetworkInterface> interfaces;

        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (final SocketException e) {
            return intToBytes((int) crc32.getValue());
        }

        while (interfaces.hasMoreElements()) {
            final NetworkInterface anInterface = interfaces.nextElement();

            try {
                final byte[] hardwareAddress = anInterface.getHardwareAddress();

                if (null != hardwareAddress) {
                    crc32.update(hardwareAddress);
                }
            } catch (final IOException e) {
                //No-op
            }

            try {
                if (anInterface.isUp()) {
                    final Enumeration<InetAddress> addresses = anInterface.getInetAddresses();

                    while (addresses.hasMoreElements()) {
                        final InetAddress address = addresses.nextElement();

                        if (address.isAnyLocalAddress() || address.isLoopbackAddress()) {
                            continue;
                        }

                        crc32.update(address.getHostAddress().getBytes(Charset.forName("UTF-8")));
                    }
                }
            } catch (final SocketException e) {
                // No-op
            }
        }

        return intToBytes((int) crc32.getValue());
    }

    private static byte[] tryGetCGroup() throws IOException, NoSuchAlgorithmException {
        final File file = new File("/proc/1/cgroup");

        if (!file.exists() || !file.isFile() || !file.canRead()) {
            return new byte[0];
        }

        final FileInputStream fileInputStream = new FileInputStream(file);
        final FileChannel channel = fileInputStream.getChannel();
        final CRC32 crc = new CRC32();

        final int length = (int) channel.size();
        final MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, length);

        for (int p = 0; p < length; p++) {
            final byte c = buffer.get(p);
            crc.update(c);
        }

        return intToBytes((int) crc.getValue());
    }

    private static byte[] tryGetNetworkHostname() {
        try {
            return InetAddress.getLocalHost().getHostName().getBytes(Charset.forName("UTF-8"));
        } catch (final UnknownHostException | NullPointerException e) {
            return longToBytes(new SecureRandom().nextLong());
        }
    }

    private static byte[] createProcessIdentifier() {
        try {
            final String processName = getRuntimeMXBean().getName();

            if (processName.contains("@")) {
                return intToBytes(Integer.parseInt(processName.substring(0, processName.indexOf('@'))));
            }

            return intToBytes(getRuntimeMXBean().getName().hashCode());
        } catch (final Throwable t) {
            return longToBytes(new SecureRandom().nextInt());
        }
    }

    static byte[] get() {
        return IDENTITY;
    }
}
