package com.hjg.mylibrary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 用于 当内部view不是圆角的时候，该layout可以将内部view的处于圆角部分的部分view隐藏
 * 当前用于当内部view进行放大操作，处理设为背景的圆角将无法展示，类似于cardview，但是cardview中不是一个layout
 */
public class RoundCropViewLayout extends RelativeLayout {
    private RoundViewDelegate delegate;
    private Path mPath;
    private Paint mPaint;
    private RectF mRectF;
    private float mRadius;
    private boolean isClipBackground = true;

    public RoundCropViewLayout(@NonNull Context context) {
        this(context, null);
    }

    public RoundCropViewLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundCropViewLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);

    }

    public void init(Context context, AttributeSet attrs) {
        delegate = new RoundViewDelegate(this, context, attrs);
        mRadius = delegate.getCornerRadius();
        mPath = new Path();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRectF = new RectF();
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
    }

    public void setRadius(float radius) {
        mRadius = radius;
        postInvalidate();
    }

    /**
     * use delegate to set attr
     */
    public RoundViewDelegate getDelegate() {
        return delegate;
    }

    @Override

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRectF.set(0, 0, w, h);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void draw(Canvas canvas) {
        if (Build.VERSION.SDK_INT >= 28) {
            draw28(canvas);
        } else {
            draw27(canvas);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (Build.VERSION.SDK_INT >= 28) {
            dispatchDraw28(canvas);
        } else {
            dispatchDraw27(canvas);
        }

    }

    private void draw27(Canvas canvas) {
        if (isClipBackground) {
            canvas.saveLayer(mRectF, null, Canvas.ALL_SAVE_FLAG);
            super.draw(canvas);
            canvas.drawPath(genPath(), mPaint);
            canvas.restore();
        } else {
            super.draw(canvas);
        }

    }

    private void draw28(Canvas canvas) {
        if (isClipBackground) {
            canvas.save();
            canvas.clipPath(genPath());
            super.draw(canvas);
            canvas.restore();
        } else {
            super.draw(canvas);
        }

    }

    private void dispatchDraw27(Canvas canvas) {
        canvas.saveLayer(mRectF, null, Canvas.ALL_SAVE_FLAG);
        super.dispatchDraw(canvas);
        canvas.drawPath(genPath(), mPaint);
        canvas.restore();
    }

    private void dispatchDraw28(Canvas canvas) {
        canvas.save();
        canvas.clipPath(genPath());
        super.dispatchDraw(canvas);
        canvas.restore();
    }

    private Path genPath() {
        mPath.reset();
        if (mRadius != 0) {
            mPath.addRoundRect(mRectF, dp2px(mRadius), dp2px(mRadius), Path.Direction.CW);
        } else {
            float[] radii = new float[]{dp2px(delegate.getCornerRadius_TL()), dp2px(delegate.getCornerRadius_TL()),
                    dp2px(delegate.getCornerRadius_TR()), dp2px(delegate.getCornerRadius_TR()),
                    dp2px(delegate.getCornerRadius_BL()), dp2px(delegate.getCornerRadius_BL()),
                    dp2px(delegate.getCornerRadius_BR()), dp2px(delegate.getCornerRadius_BR())};
            mPath.addRoundRect(mRectF, radii, Path.Direction.CW);
        }

        return mPath;
    }

    /**
     * 将dp转换成px
     *
     * @param dpValue
     * @return
     */
    public float dp2px(float dpValue) {
//        final float scale = getContext().getResources().getDisplayMetrics().density;
//        return (dpValue * scale + 0.5f);
        return dpValue;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (delegate.isWidthHeightEqual() && getWidth() > 0 && getHeight() > 0) {
            int max = Math.max(getWidth(), getHeight());
            int measureSpec = MeasureSpec.makeMeasureSpec(max, MeasureSpec.EXACTLY);
            super.onMeasure(measureSpec, measureSpec);
            return;
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (delegate.isRadiusHalfHeight()) {
            delegate.setCornerRadius(getHeight() / 2);
        } else {
            delegate.setBgSelector();
        }
    }


}