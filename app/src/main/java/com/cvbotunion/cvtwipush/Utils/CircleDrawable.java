package com.cvbotunion.cvtwipush.Utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;

/**
 * Created by gesangdianzi on 2017/6/18.
 */

public class CircleDrawable extends BitmapDrawable {

    private final Paint mPaint;

    public CircleDrawable(Resources resources, Bitmap bitmap){
        super(resources,bitmap);
        BitmapShader shader=new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setShader(shader);
        mPaint.setColor(Color.RED);
    }

    @Override
    public void draw(Canvas canvas) {
        int width = getBitmap().getWidth()+20;
        int height = getBitmap().getHeight()+20;
        int radius = Math.min(width/2, height/2);
        int x = (width>radius+radius)?width/2:radius;
        int y = (height>radius+radius)?height/2:radius;
        canvas.drawCircle(x,y,radius,mPaint);
    }

    /**
     * 这两个方法可以获取到drawable绘制的区域，不过ImageView的源码中调用的是下面的
     * @param bounds
     */
    @Override
    public void setBounds(Rect bounds) {
        super.setBounds(bounds);
    }

    /**
     * 这两个方法可以获取到drawable绘制的区域 Imagview中会调用该方法，设置宽高
     */
    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
    }
}