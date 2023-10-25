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

    public static long allocateMemory(long size) {
        return U.allocateMemory(size);
    }

    public static void freeMemory(long address) {
        U.freeMemory(address);
    }
}
