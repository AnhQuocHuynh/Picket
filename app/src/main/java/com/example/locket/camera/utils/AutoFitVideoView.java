package com.example.locket.camera.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class AutoFitVideoView extends VideoView {
    private int mVideoWidth;
    private int mVideoHeight;

    public AutoFitVideoView(Context context) {
        super(context);
    }

    public AutoFitVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoFitVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);

        if (mVideoWidth > 0 && mVideoHeight > 0) {
            float videoRatio = (float) mVideoWidth / mVideoHeight;
            float viewRatio = (float) width / height;

            if (videoRatio > viewRatio) {
                height = (int) (width / videoRatio);
            } else {
                width = (int) (height * videoRatio);
            }
        }
        setMeasuredDimension(width, height);
    }

    public void setVideoSize(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
        requestLayout();
        invalidate();
    }
}