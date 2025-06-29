package com.example.locket.drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class DrawingView extends View {
    private List<DrawingPath> paths;
    private DrawingPath currentPath;
    private Paint paint;
    private int currentColor = Color.BLACK;
    private float strokeWidth = 5f;
    private Bitmap bitmap;
    private Canvas bitmapCanvas;

    // Thêm tính năng replay
    private List<DrawingStep> drawingSteps;
    private boolean isReplaying = false;
    private int currentReplayStep = 0;
    private long replayStartTime = 0;
    private static final long REPLAY_DELAY = 400; // 400ms giữa các nét vẽ (chậm hơn)

    public DrawingView(Context context) {
        super(context);
        init();
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paths = new ArrayList<>();
        drawingSteps = new ArrayList<>();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(currentColor);
        paint.setStrokeWidth(strokeWidth);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw background
        canvas.drawColor(Color.WHITE);

        if (isReplaying) {
            // Vẽ các bước đã replay
            for (int i = 0; i < currentReplayStep && i < paths.size(); i++) {
                DrawingPath path = paths.get(i);
                paint.setColor(path.getColor());
                paint.setStrokeWidth(path.getStrokeWidth());
                canvas.drawPath(path.getPath(), paint);
            }
        } else {
            // Draw all paths
            for (DrawingPath path : paths) {
                paint.setColor(path.getColor());
                paint.setStrokeWidth(path.getStrokeWidth());
                canvas.drawPath(path.getPath(), paint);
            }

            // Draw current path
            if (currentPath != null) {
                paint.setColor(currentPath.getColor());
                paint.setStrokeWidth(currentPath.getStrokeWidth());
                canvas.drawPath(currentPath.getPath(), paint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isReplaying) return false; // Không cho phép vẽ khi đang replay

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentPath = new DrawingPath(currentColor, strokeWidth);
                currentPath.addPoint(x, y);
                return true;

            case MotionEvent.ACTION_MOVE:
                currentPath.addPoint(x, y);
                break;

            case MotionEvent.ACTION_UP:
                currentPath.addPoint(x, y);
                paths.add(currentPath);

                // Lưu bước vẽ với timestamp
                drawingSteps.add(new DrawingStep(currentPath, System.currentTimeMillis()));
                currentPath = null;
                break;

            default:
                return false;
        }

        invalidate();
        return true;
    }

    public void setColor(int color) {
        currentColor = color;
    }

    public void setStrokeWidth(float width) {
        strokeWidth = width;
    }

    public void clear() {
        paths.clear();
        drawingSteps.clear();
        currentPath = null;
        isReplaying = false;
        currentReplayStep = 0;
        invalidate();
    }

    public void undo() {
        if (!paths.isEmpty()) {
            paths.remove(paths.size() - 1);
            if (!drawingSteps.isEmpty()) {
                drawingSteps.remove(drawingSteps.size() - 1);
            }
            invalidate();
        }
    }

    public Bitmap getBitmap() {
        Bitmap result = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.drawColor(Color.WHITE);

        for (DrawingPath path : paths) {
            paint.setColor(path.getColor());
            paint.setStrokeWidth(path.getStrokeWidth());
            canvas.drawPath(path.getPath(), paint);
        }

        return result;
    }

    public boolean hasContent() {
        return !paths.isEmpty();
    }

    // Lấy dữ liệu replay để truyền qua Bundle
    public List<DrawingPath> getReplayPaths() {
        return new ArrayList<>(paths);
    }

    // Load dữ liệu replay từ DrawingFragment
    public void loadReplayData(List<DrawingPath> replayPaths) {
        if (replayPaths != null) {
            paths.clear();
            paths.addAll(replayPaths);
            invalidate();
        }
    }

    // Tính năng replay
    public void startReplay() {
        if (paths.isEmpty()) return;

        isReplaying = true;
        currentReplayStep = 0;
        replayStartTime = System.currentTimeMillis();

        // Bắt đầu replay với delay
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isReplaying && currentReplayStep < paths.size()) {
                    currentReplayStep++;
                    invalidate();

                    if (currentReplayStep < paths.size()) {
                        postDelayed(this, REPLAY_DELAY);
                    } else {
                        // Kết thúc replay
                        isReplaying = false;
                        if (onReplayListener != null) {
                            onReplayListener.onReplayFinished();
                        }
                    }
                }
            }
        }, REPLAY_DELAY);
    }

    public void stopReplay() {
        isReplaying = false;
        currentReplayStep = 0;
        invalidate();
    }

    public boolean isReplaying() {
        return isReplaying;
    }

    // Interface cho callback khi replay kết thúc
    public interface OnReplayListener {
        void onReplayFinished();
    }

    private OnReplayListener onReplayListener;

    public void setOnReplayListener(OnReplayListener listener) {
        this.onReplayListener = listener;
    }

    public static class DrawingPath implements java.io.Serializable {
        private transient Path path;
        private int color;
        private float strokeWidth;
        private List<Float> points; // Lưu các điểm để có thể tái tạo Path

        public DrawingPath(int color, float strokeWidth) {
            this.color = color;
            this.strokeWidth = strokeWidth;
            this.path = new Path();
            this.points = new ArrayList<>();
        }

        public Path getPath() {
            if (path == null && points != null) {
                // Tái tạo Path từ points
                path = new Path();
                if (!points.isEmpty()) {
                    path.moveTo(points.get(0), points.get(1));
                    for (int i = 2; i < points.size(); i += 2) {
                        path.lineTo(points.get(i), points.get(i + 1));
                    }
                }
            }
            return path;
        }

        public int getColor() {
            return color;
        }

        public float getStrokeWidth() {
            return strokeWidth;
        }

        // Thêm điểm vào path và lưu vào points
        public void addPoint(float x, float y) {
            if (path != null) {
                if (points.isEmpty()) {
                    path.moveTo(x, y);
                } else {
                    path.lineTo(x, y);
                }
            }
            points.add(x);
            points.add(y);
        }
    }

    private static class DrawingStep {
        private DrawingPath path;
        private long timestamp;

        public DrawingStep(DrawingPath path, long timestamp) {
            this.path = path;
            this.timestamp = timestamp;
        }

        public DrawingPath getPath() {
            return path;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}