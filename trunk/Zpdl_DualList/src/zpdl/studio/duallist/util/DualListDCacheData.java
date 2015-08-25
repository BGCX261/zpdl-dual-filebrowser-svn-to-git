package zpdl.studio.duallist.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

import zpdl.studio.api.dcache.DCacheData;
import zpdl.studio.duallist.view.DualListItem;

public class DualListDCacheData extends DCacheData {
    private int Type;

    public DualListDCacheData() {
        Type = DualListItem.UNKNOWN;
    }

    public DualListDCacheData(int type) {
        Type = type;
    }

    public static DualListDCacheData readData(BufferedInputStream in) throws IOException {
        DualListDCacheData data = new DualListDCacheData();
        data.read(in);

        switch(data.getType()) {
            case DualListItem.VIDEO : {
                data.recycle();
                data = new DualListDCacheVideo();
                data.read(in);
            } break;
            case DualListItem.IMAGE : {
                data.recycle();
                data = new DualListDCacheImage();
                data.read(in);
            } break;
            case DualListItem.MP3 : {
                data.recycle();
                data = new DualListDCacheMP3();
                data.read(in);
            } break;
        }
        return data;
    }

    public int getType() {
        return Type;
    }

    @Override
    public int size() {
        return IntByteSize;
    }

    @Override
    public void write(BufferedOutputStream out) throws IOException {
        out.write(intTobyte(Type));
    }

    @Override
    public void read(BufferedInputStream in) throws IOException {
        byte[] b = new byte[IntByteSize];
        in.read(b);
        Type = byteToInt(b);
    }

    @Override
    public void recycle() {
    }
}
