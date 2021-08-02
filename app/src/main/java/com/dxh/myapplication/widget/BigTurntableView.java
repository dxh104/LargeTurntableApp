package com.dxh.myapplication.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Scroller;

import androidx.annotation.Nullable;

import com.dxh.myapplication.R;


/**
 * Create by DXH on 2021/07/19
 */
public class BigTurntableView extends View {
    private int bigTurntableView_bgColor = Color.parseColor("#ffff00");//转盘背景色
    private int bigTurntableView_selectRegionColor = Color.parseColor("#330000ff");//选中区域颜色

    private int bigTurntableView_lineColor = Color.parseColor("#ff0000");//分割线的颜色
    private int bigTurntableView_lineStrokeWidth = 2;//分割线的宽度

    private int bigTurntableView_arrowLineColor = Color.parseColor("#0000ff");//箭头线的颜色

    private int bigTurntableView_textSize = 50;//文字的字体大小
    private int bigTurntableView_textColor = Color.parseColor("#ff0000");//文字的颜色
    private String bigTurntableView_content = "";//aa|bb|cc 传入转盘文字数据

    private Paint mPaint;
    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;
    private boolean isShowSelected = false;//是否展示选中的
    private boolean isUseTouchEvent = true;//触摸事件是否有效 默认有效
    private float avgDegree;//平均角度大小 根据内容自动计算
    private float rotateDegree;//旋转角度 默认第一条数据会在上方
    private String[] bisectionContent;//数据数组
    private volatile int contentIndex = 0;//当前选中内容下标


    public BigTurntableView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, getStyleableView());
        init(context, attrs);
        typedArray.recycle();
    }

    public void init(Context context, @Nullable AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, getStyleableView());
        onBaseInit(context, attrs, typedArray);
        typedArray.recycle();
    }

    protected int[] getStyleableView() {
        return R.styleable.BigTurntableView;
    }

    protected void onBaseInit(Context context, @Nullable AttributeSet attrs, TypedArray typedArray) {
        bigTurntableView_bgColor = typedArray.getColor(R.styleable.BigTurntableView_bigTurntableView_bgColor, bigTurntableView_bgColor);
        bigTurntableView_selectRegionColor = typedArray.getColor(R.styleable.BigTurntableView_bigTurntableView_selectRegionColor, bigTurntableView_selectRegionColor);
        bigTurntableView_lineColor = typedArray.getColor(R.styleable.BigTurntableView_bigTurntableView_lineColor, bigTurntableView_lineColor);
        bigTurntableView_lineStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.BigTurntableView_bigTurntableView_lineStrokeWidth, bigTurntableView_lineStrokeWidth);
        bigTurntableView_arrowLineColor = typedArray.getColor(R.styleable.BigTurntableView_bigTurntableView_arrowLineColor, bigTurntableView_arrowLineColor);
        bigTurntableView_textSize = typedArray.getDimensionPixelSize(R.styleable.BigTurntableView_bigTurntableView_textSize, bigTurntableView_textSize);
        bigTurntableView_textColor = typedArray.getColor(R.styleable.BigTurntableView_bigTurntableView_textColor, bigTurntableView_textColor);
        bigTurntableView_content = typedArray.getString(R.styleable.BigTurntableView_bigTurntableView_content);
        if (TextUtils.isEmpty(bigTurntableView_content)) {
            bigTurntableView_content = "0|1|2|3|4|5";
        }
        bisectionContent = bigTurntableView_content.split("\\|");
        avgDegree = 360 / bisectionContent.length;
        rotateDegree = -avgDegree / 2;//默认选择角度
        mScroller = new Scroller(getContext(), new AccelerateDecelerateInterpolator());
        mPaint = new Paint();
    }

    private int measuredWidth;
    private int measuredHeight;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measuredWidth = getMeasuredWidth();
        measuredHeight = getMeasuredHeight();
    }

    @SuppressLint("NewApi")
    @Override
    protected void onDraw(Canvas canvas) {
        int cX = measuredWidth / 2;
        int cY = measuredHeight / 2;
        int cR = measuredHeight / 2;
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));//抗锯齿
        mPaint.setAntiAlias(true);//抗锯齿
        mPaint.setStyle(Paint.Style.FILL);
        canvas.save();
        mPaint.setColor(bigTurntableView_bgColor);
        canvas.drawCircle(cX, cY, cR, mPaint);//画背景色
        canvas.restore();
        canvas.save();
        float selectBeginDegree;
        int offsetIndex = 0;
        if (rotateDegree <= 0) {
            offsetIndex = (int) -(Math.abs(rotateDegree) / avgDegree);
            contentIndex = (int) (Math.abs(rotateDegree) / avgDegree) % bisectionContent.length;
        } else {
            offsetIndex = (int) (Math.abs(rotateDegree) / avgDegree) + 1;
            contentIndex = (int) (Math.abs(rotateDegree) / avgDegree) % bisectionContent.length + 1;
            contentIndex = bisectionContent.length - contentIndex;
        }
        selectBeginDegree = rotateDegree - avgDegree * offsetIndex - avgDegree * bisectionContent.length / 4;
        if (isShowSelected) {
            mPaint.setColor(bigTurntableView_selectRegionColor);
            Path mPath = new Path();
            mPath.addArc(cX - cR, cY - cR, cX + cR, cY + cR, selectBeginDegree, avgDegree);
            mPath.lineTo(cX, cY);
            mPath.close();
            canvas.drawPath(mPath, mPaint);//画选中的扇形区域颜色
        }
        mPaint.setStrokeWidth(bigTurntableView_lineStrokeWidth);
        mPaint.setColor(bigTurntableView_lineColor);
        canvas.rotate(rotateDegree, cX, cY);
        for (int i = 0; i < bisectionContent.length; i++) {
            if (avgDegree == 360)
                break;
            canvas.drawLine(cX, 0, cX, cY, mPaint);//画区域线
            canvas.rotate(avgDegree, cX, cY);
        }
        canvas.rotate(avgDegree / 2, cX, cY);
        mPaint.setColor(bigTurntableView_textColor);
        for (int i = 0; i < bisectionContent.length; i++) {
            if (avgDegree == 360)
                break;
            mPaint.setTextSize(bigTurntableView_textSize);
            canvas.drawText(bisectionContent[i], cX - bisectionContent[i].length() * bigTurntableView_textSize / 2, cY / 2, mPaint);//画文字
            canvas.rotate(avgDegree, cX, cY);
        }
        canvas.restore();
        canvas.save();
        mPaint.setColor(bigTurntableView_arrowLineColor);
        mPaint.setStrokeWidth(bigTurntableView_lineStrokeWidth * 2);
        canvas.drawLine(cX, 0, cX, cY, mPaint);
        canvas.drawLine(cX, 0, cX - 25, 25, mPaint);
        canvas.drawLine(cX, 0, cX + 25, 25, mPaint);//画箭头
        canvas.restore();
    }

    private float lastX = 0f;
    private float lastY = 0f;
    private float disX, disY;
    private boolean isAllowShowSelected = false;//初始化时不允许展示选中

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isUseTouchEvent)
            return super.onTouchEvent(event);
        float viewX = event.getX();
        float viewY = event.getY();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                isAllowShowSelected = true;
                mScroller.forceFinished(true);
                addVelocityTracker(event);
                lastX = viewX;
                lastY = viewY;
                break;
            case MotionEvent.ACTION_MOVE:
                hideSelected();
                isAllowShowSelected = false;
                addVelocityTracker(event);
                disX = viewX - lastX;
                disY = viewY - lastY;
                lastX = viewX;
                lastY = viewY;
                rotateDegree = (int) (rotateDegree + disY);
//                rotateDegree = (rotateDegree + 360 * 10000) % 360;
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // 在触点抬起后再继续滑动一定距离
                int xVelocity = getXScrollVelocity();
                int yVelocity = getYScrollVelocity();
                mScroller.fling(0, lastScrollCurrY, (int) (xVelocity), (int) (yVelocity), -Integer.MIN_VALUE, Integer.MAX_VALUE, -Integer.MIN_VALUE, Integer.MAX_VALUE);
                invalidate();
                recycleVelocityTracker();
                break;
        }
        return true;
    }


    private int lastScrollCurrY = 0;
    private int computeScrollDistance = 0;

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            computeScrollDistance = mScroller.getCurrY() - lastScrollCurrY;
            lastScrollCurrY = mScroller.getCurrY();
            rotateDegree = (int) (rotateDegree + computeScrollDistance);
            if (mScroller.isFinished()) {
                isShowSelected = true;
                isAllowShowSelected = true;
            }
            if (!isAllowShowSelected) {
                invalidate();
            }
        }
        if (isAllowShowSelected) {
            showSelected();
            if (onSelectedListener != null) {
                onSelectedListener.onSelected(Math.abs(contentIndex), getBisectionContent());
            }
            isAllowShowSelected = false;
        }

    }

    /**
     * 添加用户的速度跟踪器
     */
    private void addVelocityTracker(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    /**
     * 移除用户速度跟踪器
     */
    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    /**
     * 获取X方向的滑动速度,大于0向右滑动，反之向左
     */
    private int getXScrollVelocity() {
        mVelocityTracker.computeCurrentVelocity(500);
        int velocity = (int) mVelocityTracker.getXVelocity();
        return velocity;
    }

    /**
     * 获取Y方向的滑动速度,大于0向下滑动，反之向上
     */
    private int getYScrollVelocity() {
        mVelocityTracker.computeCurrentVelocity(500);
        int velocity = (int) mVelocityTracker.getYVelocity();
        return velocity;
    }

    private void showSelected() {
        isShowSelected = true;
        invalidate();
    }

    private void hideSelected() {
        isShowSelected = false;
        invalidate();
    }

    //自动选择一次
    public void startAutoSelect(OnSelectedListener onSelectedListener) {
        this.onSelectedListener = onSelectedListener;
        post(new Runnable() {
            @Override
            public void run() {
                hideSelected();
                int dis = (int) ((Math.random()) * 1000 + 1000);
                mScroller.startScroll(0, lastScrollCurrY, 0, dis, 1000 * 3);
                invalidate();
            }
        });
    }

    public void setBigTurntableView_bgColor(int bigTurntableView_bgColor) {
        this.bigTurntableView_bgColor = bigTurntableView_bgColor;
        invalidate();
    }

    public void setBigTurntableView_selectRegionColor(int bigTurntableView_selectRegionColor) {
        this.bigTurntableView_selectRegionColor = bigTurntableView_selectRegionColor;
        invalidate();
    }

    public void setBigTurntableView_lineColor(int bigTurntableView_lineColor) {
        this.bigTurntableView_lineColor = bigTurntableView_lineColor;
        invalidate();
    }

    public void setBigTurntableView_lineStrokeWidth(int bigTurntableView_lineStrokeWidth) {
        this.bigTurntableView_lineStrokeWidth = bigTurntableView_lineStrokeWidth;
    }

    public void setBigTurntableView_arrowLineColor(int bigTurntableView_arrowLineColor) {
        this.bigTurntableView_arrowLineColor = bigTurntableView_arrowLineColor;
        invalidate();
    }

    public void setBigTurntableView_textSize(int bigTurntableView_textSize) {
        this.bigTurntableView_textSize = bigTurntableView_textSize;
        invalidate();
    }

    public void setBigTurntableView_textColor(int bigTurntableView_textColor) {
        this.bigTurntableView_textColor = bigTurntableView_textColor;
        invalidate();
    }

    public void setBigTurntableView_content(String bigTurntableView_content) {
        this.bigTurntableView_content = bigTurntableView_content;
        invalidate();
    }

    public void setUseTouchEvent(boolean useTouchEvent) {
        isUseTouchEvent = useTouchEvent;
    }

    public String[] getBisectionContent() {
        return bisectionContent;
    }

    private OnSelectedListener onSelectedListener;

    public interface OnSelectedListener {
        void onSelected(int posion, String[] bisectionContent);
    }
}
