package com.tempscaner.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class CircleView extends View {

    private int animValue;
    private int strokeWidth = 30;

    public CircleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        animValue = 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(Color.parseColor("#B3216D"));

        RectF rectF = new RectF();
        rectF.set(strokeWidth,strokeWidth,getWidth() - strokeWidth  ,getWidth() - strokeWidth);

        canvas.drawArc(rectF,animValue,80,false,paint);
    }

    public void setValue(int animatedValue) {
        animValue = animatedValue;
        invalidate();
    }

}