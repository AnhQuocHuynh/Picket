package com.example.locket.feed;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

public class RecordingProgressView extends View {

    private Paint backgroundPaint;
    private Paint progressPaint;
    private RectF rectF;
    private float progress = 0f;
    private ValueAnimator animator;
    private long startTime = 0;
    private boolean isRecording = false;
    private static final int MAX_DURATION = 30000; // 30 giây
    private static final int BUFFER_SIZE = 30000; // 30 giây buffer

    public RecordingProgressView(Context context) {
        super(context);
        init();
    }

    public RecordingProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RecordingProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.parseColor("#33FFFFFF")); // Semi-transparent white
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(8f);
        backgroundPaint.setAntiAlias(true);

        progressPaint = new Paint();
        progressPaint.setColor(Color.parseColor("#FF6B35")); // Orange color
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(8f);
        progressPaint.setAntiAlias(true);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        rectF = new RectF();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int padding = 20;
        rectF.set(padding, padding, w - padding, h - padding);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Vẽ background circle
        canvas.drawArc(rectF, 0, 360, false, backgroundPaint);

        // Vẽ progress arc
        if (isRecording && progress > 0) {
            float sweepAngle = progress * 360f;
            canvas.drawArc(rectF, -90, sweepAngle, false, progressPaint);
        }
    }

    public void startRecording() {
        isRecording = true;
        startTime = System.currentTimeMillis();

        if (animator != null) {
            animator.cancel();
        }

        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(MAX_DURATION);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(animation -> {
            progress = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    public void stopRecording() {
        isRecording = false;
        progress = 0f;

        if (animator != null) {
            animator.cancel();
            animator = null;
        }

        invalidate();
    }

    public void updateProgress(long currentTime) {
        if (!isRecording) return;

        long elapsed = currentTime - startTime;

        if (elapsed <= MAX_DURATION) {
            // Trong 30 giây đầu, hiển thị progress bình thường
            progress = (float) elapsed / MAX_DURATION;
        } else {
            // Sau 30 giây, tạo hiệu ứng circular buffer
            long bufferTime = elapsed % BUFFER_SIZE;
            progress = (float) bufferTime / BUFFER_SIZE;
        }

        invalidate();
    }

    public boolean isRecording() {
        return isRecording;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }
}