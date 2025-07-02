package com.example.locket.feed.drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DrawingView extends View {
    private Paint paint = new Paint();
    private Path path = new Path();
    private List<PathWithPaint> paths = new ArrayList<>();
    private List<PathWithPaint> undonePaths = new ArrayList<>();
    private int currentColor = Color.BLACK;
    private float currentStrokeWidth = 8f;
    private Bitmap backgroundBitmap = null;

    public DrawingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(8f);
    }

    public void setColor(int color) {
        currentColor = color;
        paint.setColor(color);
    }

    public void setStrokeWidth(float width) {
        currentStrokeWidth = width;
        paint.setStrokeWidth(width);
    }

    public void undo() {
        if (!paths.isEmpty()) {
            undonePaths.add(paths.remove(paths.size() - 1));
            path.reset();
            invalidate();
        }
    }

    public void redo() {
        if (!undonePaths.isEmpty()) {
            paths.add(undonePaths.remove(undonePaths.size() - 1));
            path.reset();
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (backgroundBitmap != null) {
            canvas.drawBitmap(backgroundBitmap, 0, 0, null);
        }
        for (PathWithPaint p : paths) {
            canvas.drawPath(p.path, p.paint);
        }
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path = new Path();
                path.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
                paths.add(new PathWithPaint(new Path(path), new Paint(paint)));
                undonePaths.clear();
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }

    public void clear() {
        path.reset();
        paths.clear();
        undonePaths.clear();
        invalidate();
    }

    public int getCurrentColor() {
        return currentColor;
    }

    public void setBitmap(Bitmap bitmap) {
        if (bitmap == null) return;
        backgroundBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        invalidate();
    }

    private static class PathWithPaint {
        Path path;
        Paint paint;
        PathWithPaint(Path path, Paint paint) {
            this.path = path;
            this.paint = new Paint(paint);
        }
    }
} 