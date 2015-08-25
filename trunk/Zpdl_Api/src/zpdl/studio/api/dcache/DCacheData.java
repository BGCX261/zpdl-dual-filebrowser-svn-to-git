package zpdl.studio.api.dcache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public abstract class DCacheData {
    protected static final int IntByteSize  = Integer.SIZE / Byte.SIZE;
    protected static final int LongByteSize = Long.SIZE / Byte.SIZE;

    abstract public int size();

    abstract public void write(BufferedOutputStream out) throws IOException;

    abstract public void read(BufferedInputStream in) throws IOException;

    abstract public void recycle();

    public static long byteTolong(byte[] b) {
        final ByteBuffer bb = ByteBuffer.wrap(b);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getLong();
    }

    public static byte[] longTobyte(long l) {
        final ByteBuffer bb = ByteBuffer.allocate(LongByteSize);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putLong(l);
        return bb.array();
    }

    public static int byteToInt(byte[] b) {
        final ByteBuffer bb = ByteBuffer.wrap(b);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getInt();
    }

    public static byte[] intTobyte(int i) {
        final ByteBuffer bb = ByteBuffer.allocate(IntByteSize);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(i);
        return bb.array();
    }
}
