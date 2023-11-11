package io.vproxy.test;

import io.vproxy.base.util.ByteArray;
import io.vproxy.base.util.bytearray.MemorySegmentByteArray;
import org.junit.Before;
import org.junit.Test;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TestMemorySegmentByteArray {
    @SuppressWarnings("FieldCanBeLocal")
    private MemorySegment seg;
    private ByteArray array;

    @Before
    public void setUp() {
        //noinspection resource
        seg = Arena.ofConfined().allocate(24);
        array = new MemorySegmentByteArray(seg);
        seg.setUtf8String(0, "01234567890123456789012");
        seg.set(ValueLayout.JAVA_BYTE, 23, (byte) '3');
    }

    @Test
    public void get() {
        assertEquals('4', (char) array.get(4));
        assertEquals('5', (char) array.get(15));
        assertEquals('3', (char) array.get(23));
    }

    @Test
    public void set() {
        get();

        array.set(4, (byte) 'a');
        array.set(15, (byte) 'b');
        array.set(23, (byte) 'c');

        assertEquals('a', (char) array.get(4));
        assertEquals('b', (char) array.get(15));
        assertEquals('c', (char) array.get(23));
    }

    @Test
    public void length() {
        assertEquals(24, array.length());
    }

    @Test
    public void byteBufferPut() {
        var b = ByteBuffer.allocateDirect(16);
        array.byteBufferPut(b, 5, 8);
        b.flip();
        byte[] bytes = new byte[8];
        b.get(bytes);

        assertArrayEquals(new byte[]{
            '5', '6', '7', '8', '9', '0', '1', '2',
        }, bytes);
    }

    @Test
    public void byteBufferGet() {
        var b = ByteBuffer.allocateDirect(8);
        b.put(new byte[]{
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
        });
        b.flip();
        array.byteBufferGet(b, 5, 8);
        for (int i = 0; i < 8; ++i) {
            assertEquals('a' + i, array.get(5 + i));
        }
    }

    @Test
    public void copyInto() {
        var arr = ByteArray.allocate(16);
        for (int i = 0; i < 16; ++i) {
            arr.set(i, (byte) ('a' + i));
        }
        array.copyInto(arr, 4, 6, 8);
        assertEquals("abcd67890123mnop", arr.toString());
    }

    @Test
    public void toNewArray() {
        var res = array.toNewJavaArray();
        assertEquals("012345678901234567890123", new String(res, StandardCharsets.US_ASCII));
    }

    @Test
    public void int64() {
        var x = array.int64(2);
        assertEquals(
            (('2' & 0xffL) << 7 * 8) |
            (('3' & 0xffL) << 6 * 8) |
            (('4' & 0xffL) << 5 * 8) |
            (('5' & 0xffL) << 4 * 8) |
            (('6' & 0xffL) << 3 * 8) |
            (('7' & 0xffL) << 2 * 8) |
            (('8' & 0xffL) << 8) |
            ('9' & 0xffL),
            x
        );
    }

    @Test
    public void int64ReverseNetworkByteOrder() {
        var x = array.int64ReverseNetworkByteOrder(2);
        assertEquals(
            (('9' & 0xffL) << 7 * 8) |
            (('8' & 0xffL) << 6 * 8) |
            (('7' & 0xffL) << 5 * 8) |
            (('6' & 0xffL) << 4 * 8) |
            (('5' & 0xffL) << 3 * 8) |
            (('4' & 0xffL) << 2 * 8) |
            (('3' & 0xffL) << 8) |
            ('2' & 0xffL),
            x
        );
    }

    @Test
    public void int32() {
        var x = array.int32(2);
        assertEquals(
            (('2' & 0xff) << 3 * 8) |
            (('3' & 0xff) << 2 * 8) |
            (('4' & 0xff) << 8) |
            ('5' & 0xff),
            x
        );
    }

    @Test
    public void int32ReverseNetworkByteOrder() {
        var x = array.int32ReverseNetworkByteOrder(2);
        assertEquals(
            (('5' & 0xff) << 3 * 8) |
            (('4' & 0xff) << 2 * 8) |
            (('3' & 0xff) << 8) |
            ('2' & 0xff),
            x
        );
    }

    @Test
    public void uint16() {
        var x = array.uint16(2);
        assertEquals(
            (('2' & 0xff) << 8) |
            ('3' & 0xff),
            x
        );
    }

    @Test
    public void uint16ReverseNetworkByteOrder() {
        var x = array.uint16ReverseNetworkByteOrder(2);
        assertEquals(
            (('3' & 0xff) << 8) |
            ('2' & 0xff),
            x
        );
    }

    @Test
    public void setInt16() {
        assertEquals('2', array.get(2));
        assertEquals('3', array.get(3));

        array.int16(2,
            (('a' & 0xff) << 8) |
            ('b' & 0xff)
        );
        assertEquals('a', array.get(2));
        assertEquals('b', array.get(3));
    }

    @Test
    public void setInt32() {
        assertEquals('2', array.get(2));
        assertEquals('3', array.get(3));
        assertEquals('4', array.get(4));
        assertEquals('5', array.get(5));

        array.int32(2,
            (('a' & 0xff) << 3 * 8) |
            (('b' & 0xff) << 2 * 8) |
            (('c' & 0xff) << 8) |
            ('d' & 0xff)
        );
        assertEquals('a', array.get(2));
        assertEquals('b', array.get(3));
        assertEquals('c', array.get(4));
        assertEquals('d', array.get(5));
    }

    @Test
    public void setInt64() {
        assertEquals('2', array.get(2));
        assertEquals('3', array.get(3));
        assertEquals('4', array.get(4));
        assertEquals('5', array.get(5));
        assertEquals('6', array.get(6));
        assertEquals('7', array.get(7));
        assertEquals('8', array.get(8));
        assertEquals('9', array.get(9));

        array.int64(2,
            (('a' & 0xffL) << 7 * 8) |
            (('b' & 0xffL) << 6 * 8) |
            (('c' & 0xffL) << 5 * 8) |
            (('d' & 0xffL) << 4 * 8) |
            (('e' & 0xffL) << 3 * 8) |
            (('f' & 0xffL) << 2 * 8) |
            (('g' & 0xffL) << 8) |
            ('h' & 0xffL)
        );
        assertEquals('a', array.get(2));
        assertEquals('b', array.get(3));
        assertEquals('c', array.get(4));
        assertEquals('d', array.get(5));
        assertEquals('e', array.get(6));
        assertEquals('f', array.get(7));
        assertEquals('g', array.get(8));
        assertEquals('h', array.get(9));
    }
}
