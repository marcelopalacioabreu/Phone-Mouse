package com.example.shrey.phonemouse;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.view.View;

public class CanvasView extends View {
    private ShapeDrawable mDrawable;
    private double x = 0;
    private double y = 0;

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void updatePos(double x, double y) {
        this.x = x;
        this.y = y;
        invalidate();
    }


    protected void onDraw(Canvas canvas) {
       super.onDraw(canvas);
       int width = canvas.getWidth();
       int height = canvas.getHeight();

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.RED);

        canvas.drawCircle(width/2+(float)x,height/2-(float)y,30, paint);
    }
}