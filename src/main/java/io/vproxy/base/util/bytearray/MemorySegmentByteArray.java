package io.vproxy.base.util.bytearray;

import io.vproxy.base.util.ByteArray;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MemorySegmentByteArray extends AbstractByteArray implements ByteArray {
    private static final Class<?> MemorySegment;
    private static final Object JAVA_BYTE;
    private static final MethodHandle getByte;
    private static final MethodHandle setByte;
    private static final MethodHandle byteSize;
    private static final MethodHandle asByteBuffer;
    private static final MethodHandle asSlice;
    private static final MethodHandle copyFrom;
    private static final MethodHandle getLong;
    private static final MethodHandle getInt;
    private static final MethodHandle getShort;
    private static final MethodHandle setLong;
    private static final MethodHandle setInt;
    private static final MethodHandle setShort;

    static {
        try {
            var lookup = MethodHandles.lookup();

            var CLASSNAME = "java.lang.foreign.MemorySegment";
            MemorySegment = Class.forName(CLASSNAME);
            var ValueLayout = Class.forName("java.lang.foreign.ValueLayout");

            JAVA_BYTE = ValueLayout.getField("JAVA_BYTE").get(null);
            {
                var ValueLayout$OfLong = Class.forName("java.lang.foreign.ValueLayout$OfLong");

                var _LONG_BIG_ENDIAN = ValueLayout.getField("JAVA_LONG_UNALIGNED").get(null);
                _LONG_BIG_ENDIAN = ValueLayout$OfLong.getMethod("withOrder", ByteOrder.class).invoke(_LONG_BIG_ENDIAN, ByteOrder.BIG_ENDIAN);
                LONG_BIG_ENDIAN = _LONG_BIG_ENDIAN;

                var _LONG_LITTLE_ENDIAN = ValueLayout.getField("JAVA_LONG_UNALIGNED").get(null);
                _LONG_LITTLE_ENDIAN = ValueLayout$OfLong.getMethod("withOrder", ByteOrder.class).invoke(_LONG_BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN);
                LONG_LITTLE_ENDIAN = _LONG_LITTLE_ENDIAN;

                getLong = lookup.findVirtual(MemorySegment, "get", MethodType.methodType(long.class, ValueLayout$OfLong, long.class));
                setLong = lookup.findVirtual(MemorySegment, "set", MethodType.methodType(void.class, ValueLayout$OfLong, long.class, long.class));
            }
            {
                var ValueLayout$OfInt = Class.forName("java.lang.foreign.ValueLayout$OfInt");

                var _INT_BIG_ENDIAN = ValueLayout.getField("JAVA_INT_UNALIGNED").get(null);
                _INT_BIG_ENDIAN = ValueLayout$OfInt.getMethod("withOrder", ByteOrder.class).invoke(_INT_BIG_ENDIAN, ByteOrder.BIG_ENDIAN);
                INT_BIG_ENDIAN = _INT_BIG_ENDIAN;

                var _INT_LITTLE_ENDIAN = ValueLayout.getField("JAVA_INT_UNALIGNED").get(null);
                _INT_LITTLE_ENDIAN = ValueLayout$OfInt.getMethod("withOrder", ByteOrder.class).invoke(_INT_LITTLE_ENDIAN, ByteOrder.LITTLE_ENDIAN);
                INT_LITTLE_ENDIAN = _INT_LITTLE_ENDIAN;

                getInt = lookup.findVirtual(MemorySegment, "get", MethodType.methodType(int.class, ValueLayout$OfInt, long.class));
                setInt = lookup.findVirtual(MemorySegment, "set", MethodType.methodType(void.class, ValueLayout$OfInt, long.class, int.class));
            }
            {
                var ValueLayout$OfShort = Class.forName("java.lang.foreign.ValueLayout$OfShort");

                var _SHORT_BIG_ENDIAN = ValueLayout.getField("JAVA_SHORT_UNALIGNED").get(null);
                _SHORT_BIG_ENDIAN = ValueLayout$OfShort.getMethod("withOrder", ByteOrder.class).invoke(_SHORT_BIG_ENDIAN, ByteOrder.BIG_ENDIAN);
                SHORT_BIG_ENDIAN = _SHORT_BIG_ENDIAN;

                var _SHORT_LITTLE_ENDIAN = ValueLayout.getField("JAVA_SHORT_UNALIGNED").get(null);
                _SHORT_LITTLE_ENDIAN = ValueLayout$OfShort.getMethod("withOrder", ByteOrder.class).invoke(_SHORT_LITTLE_ENDIAN, ByteOrder.LITTLE_ENDIAN);
                SHORT_LITTLE_ENDIAN = _SHORT_LITTLE_ENDIAN;

                getShort = lookup.findVirtual(MemorySegment, "get", MethodType.methodType(short.class, ValueLayout$OfShort, long.class));
                setShort = lookup.findVirtual(MemorySegment, "set", MethodType.methodType(void.class, ValueLayout$OfShort, long.class, short.class));
            }

            var ValueLayout$OfByte = Class.forName("java.lang.foreign.ValueLayout$OfByte");

            getByte = lookup.findVirtual(MemorySegment, "get", MethodType.methodType(byte.class, ValueLayout$OfByte, long.class));
            setByte = lookup.findVirtual(MemorySegment, "set", MethodType.methodType(void.class, ValueLayout$OfByte, long.class, byte.class));
            byteSize = lookup.findVirtual(MemorySegment, "byteSize", MethodType.methodType(long.class));
            asByteBuffer = lookup.findVirtual(MemorySegment, "asByteBuffer", MethodType.methodType(ByteBuffer.class));
            asSlice = lookup.findVirtual(MemorySegment, "asSlice", MethodType.methodType(MemorySegment, long.class, long.class));
            copyFrom = lookup.findVirtual(MemorySegment, "copyFrom", MethodType.methodType(MemorySegment, MemorySegment));
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    protected final Object seg;

    public MemorySegmentByteArray(Object seg) {
        if (!MemorySegment.isInstance(seg)) {
            throw new IllegalArgumentException(seg.getClass().getName() + " is not MemorySegment");
        }
        this.seg = seg;
    }

    public Object getMemorySegment() {
        return seg;
    }

    @Override
    public byte get(int idx) {
        try {
            return (byte) getByte.invoke(seg, JAVA_BYTE, idx);
        } catch (Throwable e) {
            throw new UnsupportedOperationException(e);
        }
    }

    @Override
    public ByteArray set(int idx, byte value) {
        try {
            setByte.invoke(seg, JAVA_BYTE, idx, value);
        } catch (Throwable e) {
            throw new UnsupportedOperationException(e);
        }
        return this;
    }

    @Override
    public int length() {
        try {
            return (int) ((long) byteSize.invoke(seg));
        } catch (Throwable e) {
            throw new UnsupportedOperationException(e);
        }
    }

    @Override
    public void byteBufferPut(ByteBuffer dst, int off, int len) {
        try {
            dst.put(((ByteBuffer) asByteBuffer.invoke(seg)).limit(off + len).position(off));
        } catch (Throwable e) {
            throw new UnsupportedOperationException(e);
        }
    }

    @Override
    public void byteBufferGet(ByteBuffer src, int off, int len) {
        try {
            ((ByteBuffer) asByteBuffer.invoke(seg)).limit(off + len).position(off).put(src);
        } catch (Throwable e) {
            throw new UnsupportedOperationException(e);
        }
    }

    @Override
    public void copyInto(ByteArray dst, int dstOff, int srcOff, int srcLen) {
        if (!(dst instanceof MemorySegmentByteArray)) {
            super.copyInto(dst, dstOff, srcOff, srcLen);
            return;
        }
        var segBuf = (MemorySegmentByteArray) dst;
        Object segBufSegSlice;
        try {
            segBufSegSlice = asSlice.invoke(segBuf.seg, dstOff, srcLen);
        } catch (Throwable e) {
            throw new UnsupportedOperationException(e);
        }
        Object thisSegSlice;
        try {
            thisSegSlice = asSlice.invoke(this.seg, srcOff, srcLen);
        } catch (Throwable e) {
            throw new UnsupportedOperationException(e);
        }
        try {
            copyFrom.invoke(segBufSegSlice, thisSegSlice);
        } catch (Throwable e) {
            throw new UnsupportedOperationException(e);
        }
    }

    @Override
    protected void doToNewJavaArray(byte[] dst, int dstOff, int srcOff, int srcLen) {
        ByteBuffer buf;
        try {
            buf = (ByteBuffer) asByteBuffer.invoke(seg);
        } catch (Throwable e) {
            throw new UnsupportedOperationException(e);
        }
        buf.limit(srcOff + srcLen).position(srcOff).get(dst, dstOff, srcLen);
    }

    private static final Object LONG_BIG_ENDIAN;
    private static final Object LONG_LITTLE_ENDIAN;

    @Override
    public long int64(int offset) {
        try {
            return (long) getLong.invoke(seg, LONG_BIG_ENDIAN, offset);
        } catch (Throwable e) {
            throw new UnsupportedOperationException(e);
        }
    }

    @Override
    public long int64ReverseNetworkByteOrder(int offset) {
        try {
            return (long) getLong.invoke(seg, LONG_LITTLE_ENDIAN, offset);
        } catch (Throwable e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private static final Object INT_BIG_ENDIAN;
    private static final Object INT_LITTLE_ENDIAN;

    @Override
    public int int32(int offset) {
        try {
            return (int) getInt.invoke(seg, INT_BIG_ENDIAN, offset);
        } catch (Throwable e) {
            throw new UnsupportedOperationException(e);
        }
    }

    @Override
    public int int32ReverseNetworkByteOrder(int offset) {
        try {
            return (int) getInt.invoke(seg, INT_LITTLE_ENDIAN, offset);
        } catch (Throwable e) {
            throw new UnsupportedOperationException(e);
        }
    }

    @Override
    public long uint32(int offset) {
        return int32(offset) & 0xffffffffL;
    }

    @Override
    public long uint32ReverseNetworkByteOrder(int offset) {
        return int32ReverseNetworkByteOrder(offset) & 0xffffffffL;
    }

    private static final Object SHORT_BIG_ENDIAN;
    private static final Object SHORT_LITTLE_ENDIAN;

    @Override
    public int uint16(int offset) {
        try {
            return ((short) getShort.invoke(seg, SHORT_BIG_ENDIAN, offset)) & 0xffff;
        } catch (Throwable e) {
            throw new UnsupportedOperationException(e);
        }
    }

    @Override
    public int uint16ReverseNetworkByteOrder(int offset) {
        try {
            return ((short) getShort.invoke(seg, SHORT_LITTLE_ENDIAN, offset)) & 0xffff;
        } catch (Throwable e) {
            throw new UnsupportedOperationException(e);
        }
    }

    @Override
    public ByteArray int16(int offset, int val) {
        try {
            setShort.invoke(seg, SHORT_BIG_ENDIAN, offset, (short) val);
        } catch (Throwable e) {
            throw new UnsupportedOperationException(e);
        }
        return this;
    }

    @Override
    public ByteArray int16ReverseNetworkByteOrder(int offset, int val) {
        try {
            setShort.invoke(seg, SHORT_LITTLE_ENDIAN, offset, (short) val);
        } catch (Throwable e) {
            throw new UnsupportedOperationException(e);
        }
        return this;
    }

    @Override
    public ByteArray int32(int offset, int val) {
        try {
            setInt.invoke(seg, INT_BIG_ENDIAN, offset, val);
        } catch (Throwable e) {
            throw new UnsupportedOperationException(e);
        }
        return this;
    }

    @Override
    public ByteArray int32ReverseNetworkByteOrder(int offset, int val) {
        try {
            setInt.invoke(seg, INT_LITTLE_ENDIAN, offset, val);
        } catch (Throwable e) {
            throw new UnsupportedOperationException(e);
        }
        return this;
    }

    @Override
    public ByteArray int64(int offset, long val) {
        try {
            setLong.invoke(seg, LONG_BIG_ENDIAN, offset, val);
        } catch (Throwable e) {
            throw new UnsupportedOperationException(e);
        }
        return this;
    }

    @Override
    public ByteArray int64ReverseNetworkByteOrder(int offset, long val) {
        try {
            setLong.invoke(seg, LONG_LITTLE_ENDIAN, offset, val);
        } catch (Throwable e) {
            throw new UnsupportedOperationException(e);
        }
        return this;
    }

    @Override
    public ByteArray sub(int fromInclusive, int len) {
        Object newSeg;
        try {
            newSeg = asSlice.invoke(seg, fromInclusive, len);
        } catch (Throwable e) {
            throw new UnsupportedOperationException(e);
        }
        return new MemorySegmentByteArray(newSeg);
    }
}
