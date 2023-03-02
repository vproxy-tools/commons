package io.vproxy.base.util.unsafe;

import io.vproxy.base.util.Logger;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

public class SunUnsafe {
    private static final Unsafe U;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            U = (Unsafe) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Logger.shouldNotHappen("Reflection failure: get unsafe failed " + e);
            throw new RuntimeException(e);
        }
    }

    public static void invokeCleaner(ByteBuffer byteBuffer) {
        U.invokeCleaner(byteBuffer);
    }

    public static byte getByte(long address) {
        return U.getByte(address);
    }

    public static void putByte(long address, byte b) {
        U.putByte(address, b);
    }

    public static void copyMemory(long dst, long src, int len) {
        U.copyMemory(src, dst, len);
    }
}
