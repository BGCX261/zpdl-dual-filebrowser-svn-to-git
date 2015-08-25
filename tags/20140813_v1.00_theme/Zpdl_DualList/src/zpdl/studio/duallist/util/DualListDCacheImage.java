package zpdl.studio.duallist.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import zpdl.studio.duallist.view.DualListItem;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

public class DualListDCacheImage extends DualListDCacheData {
    public static final int Version = 1;

    private static final int WIDTH      = 0x0001;
    private static final int HEIGHT     = 0x0002;
    private static final int THUMBNAIL  = 0x0004;

    private int     mFlag;

    private Integer mWidth;
    private Integer mHeight;
    private Bitmap  mThumbnail;

    public DualListDCacheImage() {
        super(DualListItem.IMAGE);

        mFlag       = 0;
        mWidth      = null;
        mHeight     = null;
        mThumbnail  = null;
    }

    public DualListDCacheImage(Bitmap thumbnail, Integer width, Integer height) {
        this();

        if(width != null) {
            mWidth = width;
            mFlag |=  WIDTH;
        }
        if(height != null) {
            mHeight = height;
            mFlag |=  HEIGHT;
        }
        if(thumbnail != null) {
            mThumbnail = thumbnail;
            mFlag |= THUMBNAIL;
        }
    }

    public Integer getWidth() {
        return mWidth;
    }

    public Integer getHeight() {
        return mHeight;
    }

    public Bitmap getThumbnail() {
        return mThumbnail;
    }

    @Override
    public int size() {
        int size = super.size() + IntByteSize * 2;
        if((mFlag & WIDTH) == WIDTH) {
            size += IntByteSize;
        }
        if((mFlag & HEIGHT) == HEIGHT) {
            size += IntByteSize;
        }
        if((mFlag & THUMBNAIL) == THUMBNAIL) {
            size += IntByteSize * 3 + mThumbnail.getConfig().toString().getBytes().length + mThumbnail.getByteCount();
        }
        return size;
    }

    @Override
    public void write(BufferedOutputStream out) throws IOException {
        super.write(out);

        out.write(intTobyte(Version));

        out.write(intTobyte(mFlag));

        if((mFlag & WIDTH) == WIDTH) {
            out.write(intTobyte(mWidth.intValue()));
        }
        if((mFlag & HEIGHT) == HEIGHT) {
            out.write(intTobyte(mHeight.intValue()));
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
        byte[] bReadInt = new byte[IntByteSize];

        in.read(bReadInt);
        int version = byteToInt(bReadInt);

        if(version == 1) {
            in.read(bReadInt);
            mFlag = byteToInt(bReadInt);

            if((mFlag & WIDTH) == WIDTH) {
                in.read(bReadInt);
                mWidth = Integer.valueOf(byteToInt(bReadInt));
            }
            if((mFlag & HEIGHT) == HEIGHT) {
                in.read(bReadInt);
                mHeight = Integer.valueOf(byteToInt(bReadInt));
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
