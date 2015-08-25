package zpdl.studio.duallist.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import zpdl.studio.duallist.view.DualListItem;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

public class DualListDCacheMP3 extends DualListDCacheData {
    public static final int Version = 1;

    private static final int DURATION   = 0x0001;
    private static final int ARTIST     = 0x0002;
    private static final int THUMBNAIL  = 0x0004;

    private int    mFlag;

    private Long   mDuration;
    private String mArtist;
    private Bitmap mThumbnail;

    public DualListDCacheMP3() {
        super(DualListItem.MP3);

        mFlag = 0;
        mDuration   = null;
        mArtist     = null;
        mThumbnail  = null;
    }

    public DualListDCacheMP3(Bitmap coverimage, Long duration, String artist) {
        this();

        if(duration != null) {
            mDuration = duration;
            mFlag |= DURATION;
        }
        if(artist != null) {
            mArtist = artist;
            mFlag |= ARTIST;
        }
        if(coverimage != null) {
            mThumbnail = coverimage;
            mFlag |= THUMBNAIL;
        }
    }

    public Long getDuration() {
        return mDuration;
    }

    public String getArtist() {
        return mArtist;
    }

    public Bitmap getThumbnail() {
        return mThumbnail;
    }

    @Override
    public int size() {
        int size = super.size() + IntByteSize * 2;
        if((mFlag & DURATION) == DURATION) {
            size += LongByteSize;
        }
        if((mFlag & ARTIST) == ARTIST) {
            size += IntByteSize + mArtist.getBytes().length;
        }
        if((mFlag & THUMBNAIL) == THUMBNAIL) {
            size += IntByteSize * 3 + mThumbnail.getConfig().toString().getBytes().length + mThumbnail.getByteCount();
        }
        return size;
    }

    public static int byteToString(byte[] b) {
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

    @Override
    public void write(BufferedOutputStream out) throws IOException {
        super.write(out);

        out.write(intTobyte(Version));

        out.write(intTobyte(mFlag));

        if((mFlag & DURATION) == DURATION) {
            out.write(longTobyte(mDuration));
        }
        if((mFlag & ARTIST) == ARTIST) {
            byte[] artist = mArtist.getBytes();
            out.write(intTobyte(artist.length));
            out.write(artist);
        }
        if((mFlag & THUMBNAIL) == THUMBNAIL) {
            byte[] config = mThumbnail.getConfig().toString().getBytes();
            out.write(intTobyte(mThumbnail.getWidth()));
            out.write(intTobyte(mThumbnail.getHeight()));
            out.write(intTobyte(config.length));
            out.write(config);

            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(mThumbnail.getByteCount());
            byteBuffer.rewind();
            mThumbnail.copyPixelsToBuffer(byteBuffer);
            out.write(intTobyte(byteBuffer.capacity()));
            out.write(byteBuffer.array());
            byteBuffer.clear();
        }
    }

    @Override
    public void read(BufferedInputStream in) throws IOException {
        byte[] bReadLong = new byte[LongByteSize];
        byte[] bReadInt = new byte[IntByteSize];
        in.read(bReadInt);
        int version = byteToInt(bReadInt);
        if(version == 1) {
            in.read(bReadInt);
            mFlag = byteToInt(bReadInt);

            if((mFlag & DURATION) == DURATION) {
                in.read(bReadLong);
                mDuration = Long.valueOf(byteTolong(bReadLong));
            }
            if((mFlag & ARTIST) == ARTIST) {
                in.read(bReadInt);
                byte[] bartist = new byte[byteToInt(bReadInt)];
                in.read(bartist);
                mArtist = new String(bartist);
            }
            if((mFlag & THUMBNAIL) == THUMBNAIL) {
                in.read(bReadInt);
                int thumbwidth = byteToInt(bReadInt);
                in.read(bReadInt);
                int thumbheight = byteToInt(bReadInt);

                Config config = null;
                try {
                    byte[] bReadConfig = new byte[IntByteSize];
                    in.read(bReadConfig);
                    int configSize = byteToInt(bReadConfig);

                    bReadConfig = new byte[configSize];
                    in.read(bReadConfig);
                    Field f = Config.class.getDeclaredField(new String(bReadConfig));
                    config = (Config) f.get(Config.class);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }

                in.read(bReadInt);
                int thumbSize = byteToInt(bReadInt);
                byte[] bReadThumb = new byte[thumbSize];
                in.read(bReadThumb);

                ByteBuffer byteBuffer = ByteBuffer.wrap(bReadThumb);
                byteBuffer.rewind();

                mThumbnail = Bitmap.createBitmap(thumbwidth, thumbheight, config);
                mThumbnail.copyPixelsFromBuffer(byteBuffer);
            }
        }
    }

    @Override
    public void recycle() {
        super.recycle();

        if(mThumbnail != null) {
            mThumbnail.recycle();
            mThumbnail = null;
        }
    }
}
