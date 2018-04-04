package com.rfksystems.meson;

import org.bson.types.ObjectId;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;


public class MesonTest {
    private static final int I100M = 100000000;
    private static final int I1M = 1000000;
    private static final byte[] TEST_GENERATOR_ID = {0x1, 0x2, 0x3, 0x4};
    private static final byte[] TEST_GENERATOR_TIME = {0x0, 0x0, 0x0, 0x0, 0x0, 0x64};
    private static final byte[] TEST_GENERATOR_SEQ = {0x0, 0x0, 0x0, 0xffffffc8};
    private static final byte[] TEST_GENERATOR_BYTES = {0x0, 0x0, 0x0, 0x0, 0x0, 0x64, 0x1, 0x2, 0x3, 0x4, 0x0, 0x0, 0x0, 0xffffffc8};

    @Test
    public void test_two_new_instances_not_equal() throws Exception {
        final Meson mesonA = new Meson();
        final Meson mesonB = new Meson();

        assertThat(mesonA).isNotEqualTo(mesonB);
    }

    @Test
    public void test_static() throws Exception {
        final byte[] bytes = Meson.directToByteArray();
        assertThat(bytes.length).isEqualTo(14);

        final ByteBuffer byteBuffer = Meson.directToByteBuffer();
        assertThat(byteBuffer.array().length).isEqualTo(14);

        final String hexString = Meson.directToHexString();
        assertThat(hexString.length()).isEqualTo(28);

        final String formatString = Meson.directToFormatString();
        assertThat(formatString.length()).isEqualTo(30);
    }

    @Test
    public void test_from_values() throws Exception {
        final Meson meson = new Meson(0x64, TEST_GENERATOR_ID, 0xc8);
        assertCommon(meson);
    }

    @Test
    public void test_from_format_string() throws Exception {
        final Meson meson = new Meson("000000000064-01020304-000000c8");
        assertCommon(meson);
    }

    @Test
    public void test_from_hex_string() throws Exception {
        final Meson meson = new Meson("00000000006401020304000000c8");
        assertCommon(meson);
    }

    @Test
    public void test_from_bytes() throws Exception {
        final Meson meson = new Meson(TEST_GENERATOR_BYTES);
        assertCommon(meson);
    }

    @Test
    public void test_from_byte_buffer() throws Exception {
        final Meson meson = new Meson(ByteBuffer.wrap(TEST_GENERATOR_BYTES));
        assertCommon(meson);
    }

    @Test
    public void test_concurrent_unique() throws Exception {
        final List<String> t1Hex = new ArrayList<>();
        final List<String> t2Hex = new ArrayList<>();
        final List<String> t3Hex = new ArrayList<>();
        final List<String> t4Hex = new ArrayList<>();

        final Runnable t1 = () -> {
            for (int i = 0; i < I1M; i++) {
                t1Hex.add(new Meson().toHexString());
            }
        };

        final Runnable t2 = () -> {
            for (int i = 0; i < I1M; i++) {
                t2Hex.add(new Meson().toHexString());
            }
        };

        final Runnable t3 = () -> {
            for (int i = 0; i < I1M; i++) {
                t3Hex.add(new Meson().toHexString());
            }
        };

        final Runnable t4 = () -> {
            for (int i = 0; i < I1M; i++) {
                t4Hex.add(new Meson().toHexString());
            }
        };

        final ExecutorService executor = Executors.newFixedThreadPool(4);

        final Future<?> submit1 = executor.submit(t1);
        final Future<?> submit2 = executor.submit(t2);
        final Future<?> submit3 = executor.submit(t3);
        final Future<?> submit4 = executor.submit(t4);

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.MINUTES);

        System.out.println("Done generating");

        assertThat(submit1.isDone()).isTrue();
        assertThat(submit2.isDone()).isTrue();
        assertThat(submit3.isDone()).isTrue();
        assertThat(submit4.isDone()).isTrue();

        assertThat(submit1.isCancelled()).isFalse();
        assertThat(submit2.isCancelled()).isFalse();
        assertThat(submit3.isCancelled()).isFalse();
        assertThat(submit4.isCancelled()).isFalse();

        assertWithMessage("T1 count is as expected").that(t1Hex).hasSize(I1M);
        assertWithMessage("T2 count is as expected").that(t2Hex).hasSize(I1M);
        assertWithMessage("T3 count is as expected").that(t3Hex).hasSize(I1M);
        assertWithMessage("T4 count is as expected").that(t4Hex).hasSize(I1M);

        assertWithMessage("T1 count is as expected").that(t1Hex.stream().distinct().count()).isEqualTo(I1M);
        assertWithMessage("T2 count is as expected").that(t2Hex.stream().distinct().count()).isEqualTo(I1M);
        assertWithMessage("T3 count is as expected").that(t3Hex.stream().distinct().count()).isEqualTo(I1M);
        assertWithMessage("T4 count is as expected").that(t4Hex.stream().distinct().count()).isEqualTo(I1M);

        final long count = Stream
                .concat(
                        Stream.concat(t1Hex.stream(), t2Hex.stream()),
                        Stream.concat(t3Hex.stream(), t4Hex.stream())
                )
                .distinct()
                .count();

        assertWithMessage("T count is as expected").that(count).isEqualTo(I1M * 4);
    }

    @Test
    public void benchmark_to_object_id() throws Exception {
        final long timeToCreateObjectId = runTimed(() -> {
            for (int i = 0; i < I100M; i++) {
                ObjectId.get();
            }
        });

        final long timeToCreateMeson = runTimed(() -> {
            for (int i = 0; i < I100M; i++) {
                new Meson();
            }
        });

        System.out.printf("Time to create 100m Meson: %dms\n", timeToCreateMeson);
        System.out.printf("Time to create 100m ObjectId: %dms\n", timeToCreateObjectId);

        final double runRatio = (double) timeToCreateMeson / (double) timeToCreateObjectId;

        assertWithMessage("Ratio of runs should be less than 2x").that(runRatio).isLessThan(2.0);
    }

    private void assertCommon(final Meson meson) {
        assertThat(meson.toFormatString()).isEqualTo("000000000064-01020304-000000c8");
        assertThat(meson.toHexString()).isEqualTo("00000000006401020304000000c8");
        assertThat(meson.toByteArray()).isEqualTo(TEST_GENERATOR_BYTES);
        assertThat(meson.toByteBuffer().array()).isEqualTo(TEST_GENERATOR_BYTES);

        assertThat(meson.getTime()).isEqualTo(0x64);
        assertThat(meson.getGeneratorId()).isEqualTo(TEST_GENERATOR_ID);
        assertThat(meson.getSequence()).isEqualTo(0xc8);

        assertThat(meson.getTimeHex()).isEqualTo("000000000064");
        assertThat(meson.getGeneratorIdHex()).isEqualTo("01020304");
        assertThat(meson.getSequenceHex()).isEqualTo("000000c8");

        assertThat(meson.getTimeBytes()).isEqualTo(TEST_GENERATOR_TIME);
        assertThat(meson.getSequenceBytes()).isEqualTo(TEST_GENERATOR_SEQ);
    }

    private long runTimed(final Runnable runnable) {
        final long start = System.currentTimeMillis();
        runnable.run();
        return System.currentTimeMillis() - start;
    }
}
