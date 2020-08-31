package com.wifio.basiclocation;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class PointerView extends View {
    private static final String TAG = "PointerView";
    float x = 0;
    float y = 0;
    Paint paint;
    Canvas canvas;
    public PointerView(Context context) {
        super(context);
//            mBitmap = Bitmap.createBitmap(400, 800, Bitmap.Config.ARGB_8888);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.canvas = canvas;
        if(x!=0 && y!=0) {
//                canvas.drawCircle(x, y, 40, paint);
            Log.i(TAG, "x: " + x + ", y:"+ y);
            Resources res = getResources();
            Bitmap bitmap = getBitmap(R.drawable.ic_location);
            canvas.drawBitmap(bitmap, (int) x - 100, (int) y - 150 , paint);
        }
    }

    private Bitmap getBitmap(int drawableRes) {
        Drawable drawable = getResources().getDrawable(drawableRes);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public void clearCanvas(){
        invalidate();
        x=0;
        y=0;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            x = event.getX();
            y = event.getY();
            invalidate();
        }
        return false;
    }

}
