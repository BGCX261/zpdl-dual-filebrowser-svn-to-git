package com.google.android.gms.ads;

import android.content.Context;
import android.view.View;

public class AdView extends View {
    public AdView(Context context) {
        super(context);
    }
    public void resume() {}
    public void pause() {}
    public void destroy() {}
    public void setAdListener(AdListener l) {}
    public void setVisibility(int v) {}
    public void loadAd(AdRequest adRequest) {}
}
