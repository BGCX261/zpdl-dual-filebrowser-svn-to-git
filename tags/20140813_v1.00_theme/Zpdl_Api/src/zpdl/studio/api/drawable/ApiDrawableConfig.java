package zpdl.studio.api.drawable;

import android.annotation.TargetApi;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;

public class ApiDrawableConfig {
    public int deviceDensityDpi;

    public int densityDpi;
    public float ratio;
    public float scale;

    public ApiDrawableConfig(Resources res) {
        DisplayMetrics metric = res.getDisplayMetrics();

        this.deviceDensityDpi = metric.densityDpi;
        scale = getDrawableScale(res.getConfiguration());
        float sDensity = scale * deviceDensityDpi;

        if(sDensity >= 560) {
            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                densityDpi = _getDENSITY_XXXHIGH();
            } else {
                densityDpi = 640;
            }
        } else if(sDensity >= 400) {
            densityDpi = DisplayMetrics.DENSITY_XXHIGH;
        } else {
            densityDpi = DisplayMetrics.DENSITY_XHIGH;
        }
        ratio = sDensity / densityDpi;
    }

    public static float getDrawableScale(Configuration configure) {
        int smallestScreenWidthDp = configure.smallestScreenWidthDp;

        if(smallestScreenWidthDp >= 800) {
            return 2.0f;
        } else if(smallestScreenWidthDp >= 600) {
            return 1.5f;
        } else {
            return 1.0f;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private int _getDENSITY_XXXHIGH() {
        return DisplayMetrics.DENSITY_XXXHIGH;
    }
}
