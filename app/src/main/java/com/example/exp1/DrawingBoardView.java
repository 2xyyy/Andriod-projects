package com.example.exp1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class DrawingBoardView extends View {

    class DrawPath {
        Path path;
        Paint paint;
    }

    private Paint mPaint; // 画笔
    private Path mPath;            // 当前路径
    private Bitmap mBitmap;        // 缓冲区（离屏位图）
    private Canvas mBufferCanvas;  // 绘制到位图上的Canvas
    private int currentColor = Color.rgb(0,0,0); // 当前颜色（默认黑色）

    private float mLastX, mLastY;  // 上一个点

    // 存放每一笔
    private final List<DrawPath> paths = new ArrayList<>();
    // 存放已撤销的路径（用于redo）
    private final List<DrawPath> undonePaths = new ArrayList<>();

    //带属性的构造函数，支持在XML布局中使用
    private Context context;
    public DrawingBoardView(Context context, @Nullable AttributeSet attrs){
        super(context, attrs);
        this.context = context;
        init();
        // 如需解析自定义属性，可在这里处理 attrs
    }

    private void init() {
        // 初始化画笔
        mPaint = new Paint();
        mPaint.setAntiAlias(true); // 抗锯齿，让边缘更平滑
        mPaint.setDither(true);         // 抖动，颜色更平滑
        mPaint.setColor(currentColor);  // 初始颜色
        mPaint.setStyle(Paint.Style.STROKE); // 画笔样式：描边
        mPaint.setStrokeJoin(Paint.Join.ROUND); // 线条转角圆滑
        mPaint.setStrokeCap(Paint.Cap.ROUND);   // 线头圆滑
        mPaint.setStrokeWidth(8);       // 默认线宽
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mBufferCanvas = new Canvas(mBitmap);
        mBufferCanvas.drawColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 先绘制所有已保存的路径
        mBufferCanvas.drawColor(Color.WHITE);
        for (DrawPath dp : paths) {
            mBufferCanvas.drawPath(dp.path, dp.paint);
        }
        // 再把缓冲区画到屏幕上
        canvas.drawBitmap(mBitmap, 0, 0, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 新建画笔副本（防止颜色被修改后影响历史）
                Paint newPaint = new Paint(mPaint);
                mPath = new Path();
                mPath.moveTo(x, y);

                mLastX = x;
                mLastY = y;

                DrawPath drawPath = new DrawPath();
                drawPath.path = mPath;
                drawPath.paint = newPaint;

                paths.add(drawPath);
                undonePaths.clear(); // 清空撤销栈
                break;

            case MotionEvent.ACTION_MOVE:
                float cx = (mLastX + x) / 2;
                float cy = (mLastY + y) / 2;
                mPath.quadTo(mLastX, mLastY, cx, cy);
                mLastX = x;
                mLastY = y;
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                invalidate();
                break;
        }
        return true;
    }

    /** 修改画笔颜色 */
    public void setPaintColor(int color) {
        currentColor = color;
        mPaint.setColor(color);
    }

    /** 清空画布 */
    public void clear() {
        paths.clear();
        undonePaths.clear();
        invalidate();
    }

    /** 撤销上一步 */
    public void undo() {
        if (paths.size() > 0) {
            DrawPath last = paths.remove(paths.size() - 1);
            undonePaths.add(last);
            invalidate();
        }
    }

    /** 重新恢复撤销的路径（可选） */
    public void redo() {
        if (undonePaths.size() > 0) {
            DrawPath path = undonePaths.remove(undonePaths.size() - 1);
            paths.add(path);
            invalidate();
        }
    }

    /** 返回Bitmap */
    public Bitmap getBitmap(){
        return mBitmap;
    }

    /** 判断画板是否为空 */
    public boolean isEmpty() {
        return paths.isEmpty();
    }
}