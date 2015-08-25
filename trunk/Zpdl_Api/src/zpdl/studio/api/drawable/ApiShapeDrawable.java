package zpdl.studio.api.drawable;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;

public class ApiShapeDrawable extends ShapeDrawable {
    private Paint mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public ApiShapeDrawable(Shape s) {
        super(s);
        mStrokePaint.setStyle(Paint.Style.STROKE);
    }

    public Paint getStrokePaint() {
        return mStrokePaint;
    }

    @Override protected void onDraw(Shape s, Canvas c, Paint p) {
        s.draw(c, p);
        s.draw(c, mStrokePaint);
    }
}
