package org.rao.source.genid.util;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.RandomUtil;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

/**
 * @author Rao
 * @Date 2025/2/8
 **/
public class UuidV7 {

    /**
     * {@link SecureRandom} 的单例
     *
     * @author looly
     */
    private static class Holder {
        static final SecureRandom NUMBER_GENERATOR = RandomUtil.getSecureRandom();
    }

    public static UUID randomUUID() {
        byte[] value = randomBytes();
        ByteBuffer buf = ByteBuffer.wrap(value);
        long high = buf.getLong();
        long low = buf.getLong();
        return new UUID(high, low);
    }

    public static byte[] randomBytes() {
        // random bytes
        byte[] value = new byte[16];
        // 用性能更好的hutool的
//        final Random ng = Holder.NUMBER_GENERATOR;
        Random ng = RandomUtil.getRandom();
        ng.nextBytes(value);

        // current timestamp in ms
        long timestamp = System.currentTimeMillis();

        // timestamp
        value[0] = (byte) ((timestamp >> 40) & 0xFF);
        value[1] = (byte) ((timestamp >> 32) & 0xFF);
        value[2] = (byte) ((timestamp >> 24) & 0xFF);
        value[3] = (byte) ((timestamp >> 16) & 0xFF);
        value[4] = (byte) ((timestamp >> 8) & 0xFF);
        value[5] = (byte) (timestamp & 0xFF);

        // version and variant
        value[6] = (byte) ((value[6] & 0x0F) | 0x70);
        value[8] = (byte) ((value[8] & 0x3F) | 0x80);

        return value;
    }

    public static long extractTimestamp(UUID uuid) {
        byte[] bytes = new byte[16];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());

        // Extract the timestamp from the UUID bytes
        long timestamp = 0L;
        timestamp |= (bytes[0] & 0xFFL) << 40;
        timestamp |= (bytes[1] & 0xFFL) << 32;
        timestamp |= (bytes[2] & 0xFFL) << 24;
        timestamp |= (bytes[3] & 0xFFL) << 16;
        timestamp |= (bytes[4] & 0xFFL) << 8;
        timestamp |= (bytes[5] & 0xFFL);

        return timestamp;
    }


    /**
     * 测试用例
     * @param args
     */
    public static void main(String[] args) {
        UUID uuid = randomUUID();
        String uuidStr = uuid.toString();
        System.out.println("UUID: " + uuidStr);
        long timestamp = extractTimestamp(uuid);
        System.out.println("Timestamp: " + timestamp);

        Assert.isTrue(uuidStr.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"));
    }

//
//    @Test
//    public void testUUIDv7Format() {
//        org.dromara.hutool.core.data.id.UUID uuid = org.dromara.hutool.core.data.id.UUID.randomUUID7();
//        String uuidStr = uuid.toString();
//
//        // 验证UUID字符串格式是否符合标准
//        assertTrue(uuidStr.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"));
//    }
//
//    @Test
//    public void testUUIDv7Properties() {
//        org.dromara.hutool.core.data.id.UUID uuid = org.dromara.hutool.core.data.id.UUID.randomUUID7();
//
//        // 验证版本号是否为7
//        assertEquals(7, uuid.version());
//
//        // 验证变体是否为IETF variant
//        assertEquals(2, uuid.variant());
//
//    }
//
//    @RepeatedTest(10)
//    public void testUUIDv7Uniqueness() {
//        Set<org.dromara.hutool.core.data.id.UUID> uuids = new HashSet<>();
//
//        // 生成100万个UUIDv7，验证是否有重复
//        for (int i = 0; i < 1000000; i++) {
//            org.dromara.hutool.core.data.id.UUID uuid = org.dromara.hutool.core.data.id.UUID.randomUUID7();
//            assertFalse(uuids.contains(uuid));
//            uuids.add(uuid);
//        }
//    }
//
//
//    @Test
//    public void testUUIDv7Monotonicity() {
//        org.dromara.hutool.core.data.id.UUID prev = org.dromara.hutool.core.data.id.UUID.randomUUID7();
//
//        // 验证连续生成的1000个UUIDv7是否呈单调递增趋势
//        for (int i = 0; i < 1000; i++) {
//            org.dromara.hutool.core.data.id.UUID next = org.dromara.hutool.core.data.id.UUID.randomUUID7();
//            assertTrue(next.compareTo(prev) > 0);
//            prev = next;
//        }
//    }
//
//    /**
//     * UUIDv7的性能测试
//     */
//    @Test
//    public void testUUIDv7Benchmark() {
//        final StopWatch timer = DateUtil.createStopWatch();
//
//        // UUID v7 generation benchmark
//        timer.start("UUID v7 generation");
//        for (int i = 0; i < 1000000; i++) {
//            IdUtil.randomUUID7();
//        }
//        timer.stop();
//        Console.log("UUIDv7 generation time: {} ms", timer.getLastTaskTimeMillis());
//
//
//        timer.start("UUID v7 generation and formatting");
//        for (int i = 0; i < 1000000; i++) {
//            IdUtil.randomUUID7().replace("-", "");
//        }
//        timer.stop();
//        Console.log("UUIDv7 generation and formatting time: {} ms", timer.getLastTaskTimeMillis());
//    }
//
}
