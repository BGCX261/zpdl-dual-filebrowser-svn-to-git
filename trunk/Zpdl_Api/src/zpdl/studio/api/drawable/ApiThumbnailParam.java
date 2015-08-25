package zpdl.studio.api.drawable;

import android.graphics.Bitmap;

public class ApiThumbnailParam {
    public Bitmap thumbnail;
    public int width;
    public int height;

    public ApiThumbnailParam(Bitmap thumbnail, int width, int height) {
        this.thumbnail = thumbnail;
        this.width = width;
        this.height = height;
    }
}
