package com.tencent.cloud.tuikit.roomkit.videoseat.ui.view;

import static com.tencent.cloud.tuikit.roomkit.videoseat.Constants.VOLUME_NO_SOUND;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.tencent.cloud.tuikit.roomkit.R;

public class UserVolumePromptView extends View {
    // 音量变化范围 [0, 100]， UI 上的变化效果映射为 [0, 20]，小 UI 无需太细的变动
    private static final int VOLUME_STEP         = 1;
    private static final int VOLUME_TOTAL_STEP   = 100 / VOLUME_STEP;
    private static final int VOLUME_SHOW_TIME_MS = 500;

    private Paint mPaint;

    private int mVolume; // 范围 [0 ~ 10]，小 ui 无需太细的区分；
    private int mVolumeAreaLeft;
    private int mVolumeAreaTop;
    private int mVolumeAreaRight;
    private int mVolumeAreaBottom;
    private int mVolumeAreaRadius;
    private int mLineWidth;

    private Drawable mDrawableOpen;
    private Drawable mDrawableClose;

    private Handler mMainHandler;

    public UserVolumePromptView(Context context) {
        this(context, null);
    }

    public UserVolumePromptView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);

     //   setBackground(context.getResources().getDrawable(R.drawable.tuiroomkit_video_seat_mic_open));
        mPaint = new Paint();
        mPaint.setColor(0xFFA5FE33);

        mVolume = VOLUME_NO_SOUND;

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.UserVolumePromptView);
        mDrawableOpen = typedArray.getDrawable(R.styleable.UserVolumePromptView_backgroundStateOpen);
        if (mDrawableOpen == null) {
            mDrawableOpen = context.getResources().getDrawable(R.drawable.tuiroomkit_video_seat_mic_open);
        }
        mDrawableClose = typedArray.getDrawable(R.styleable.UserVolumePromptView_backgroundStateClose);
        if (mDrawableClose == null) {
            mDrawableClose = context.getResources().getDrawable(R.drawable.tuiroomkit_video_seat_mic_close);
        }
        mLineWidth = typedArray.getDimensionPixelOffset(R.styleable.UserVolumePromptView_boardLineWidth, 0);
        typedArray.recycle();
    }

    /**
     * 更新音量的 ui 效果。
     *
     * @param volume 当前 mic 的音量，范围 [0 ~ 100]
     */
    public void updateVolumeEffect(int volume) {
        if (!isAttachedToWindow()) {
            return;
        }
        volume = volume < 0 ? 0 : volume;
        volume = volume > 100 ? 100 : volume;

        int greenVolume = volume / VOLUME_STEP;
        greenVolume += volume % VOLUME_STEP == 0 ? 0 : 1;
        if (mVolume == greenVolume) {
            return;
        }
        mVolume = greenVolume;
        invalidate();

        if (mMainHandler != null && mVolume != VOLUME_NO_SOUND) {
            mMainHandler.removeCallbacksAndMessages(null);
            mMainHandler.postDelayed(mClearGreenVolume, VOLUME_SHOW_TIME_MS);
        }
    }

    public void enableVolumeEffect(boolean enable) {
        setBackground(enable ? mDrawableOpen : mDrawableClose);
        if (!enable && mVolume != VOLUME_NO_SOUND) {
            updateVolumeEffect(VOLUME_NO_SOUND);
        }
    }

    Runnable mClearGreenVolume = new Runnable() {
        @Override
        public void run() {
            mVolume = VOLUME_NO_SOUND;
            invalidate();
        }
    };

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
            mMainHandler = null;
        }
        mVolume = VOLUME_NO_SOUND;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // 这参数是根据背景图 UI 设计上的比例换算得出，保证 View 在不同宽高下的显示效果
        int width = right - left;
        int height = bottom - top;
        mVolumeAreaLeft = (width << 1) / 7 + mLineWidth;
        mVolumeAreaTop = (int) (height * 0.050) + mLineWidth;
        mVolumeAreaRight = width - mVolumeAreaLeft;
        mVolumeAreaBottom = (int) (height * 0.76) - mLineWidth;
        mVolumeAreaRadius = Math.min(mVolumeAreaRight - mVolumeAreaLeft, mVolumeAreaBottom - mVolumeAreaTop) >> 1;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        RectF rect = new RectF(mVolumeAreaLeft, mVolumeAreaTop, mVolumeAreaRight, mVolumeAreaBottom);
        int greenHeight = (mVolumeAreaBottom - mVolumeAreaTop) * mVolume / VOLUME_TOTAL_STEP;
        canvas.clipRect(mVolumeAreaLeft, mVolumeAreaBottom - greenHeight, mVolumeAreaRight, mVolumeAreaBottom);
        canvas.drawRoundRect(rect, mVolumeAreaRadius, mVolumeAreaRadius, mPaint);
    }
}
