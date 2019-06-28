package com.chhd.volumewaveview;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * 音量条形与波浪
 *
 * @author 陈伟强 (2019/5/7)
 */
public class VolumeWaveView extends View {

    private final String TAG = this.getClass().getSimpleName();

    private Paint paint = new Paint();
    private RectF rect = new RectF();

    /** 高亮颜色 */
    private int highLightColor;
    /** 非高亮颜色 */
    private int unHighLightColor;
    /** 宽度 */
    private float columnWidth;
    /** 间隔 */
    private float columnOffset;
    /** 数量 */
    private int columnCount;
    /** 高亮数量 */
    private int highLightCount;
    /** 是否暂停状态 */
    private boolean isPause;

    private ObjectAnimator animator = null;
    private List<Column> columns = new ArrayList<>();

    public VolumeWaveView(Context context) {
        this(context, null);
    }

    public VolumeWaveView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VolumeWaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.VolumeWaveView);
        columnWidth = ta.getDimension(R.styleable.VolumeWaveView_vwv_column_width, dp2px(4));
        columnOffset = ta.getDimension(R.styleable.VolumeWaveView_vwv_column_offset, dp2px(8));
        highLightColor = ta.getColor(R.styleable.VolumeWaveView_vwv_light_color,
                Color.parseColor("#ff5a82e6"));
        unHighLightColor = ta.getColor(R.styleable.VolumeWaveView_vwv_un_light_color,
                Color.parseColor("#ffd8d8d8"));
        isPause = ta.getBoolean(R.styleable.VolumeWaveView_vwv_is_pause, false);
        ta.recycle();

        paint.setAntiAlias(true);

        registerVolumeChangeReceiver();

    }

    /** 监听多媒体音量变化 */
    private void registerVolumeChangeReceiver() {
        getContext().getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, volumeObserver);
    }

    private void unregisterVolumeChangeReceiver() {
        getContext().getContentResolver().unregisterContentObserver(volumeObserver);
    }

    /** 根据多媒体音量，决定高亮线条的数量 */
    private void initHighLightLineNum() {
        AudioManager manager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        highLightCount = (int) (columnCount * (currentVolume * 1f / maxVolume));
    }

    /** 多媒体音量变化回调 */
    private ContentObserver volumeObserver = new ContentObserver(new Handler()) {

        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            initHighLightLineNum();
            invalidate();
        }
    };

    private int dp2px(float dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initColumns();
        initHighLightLineNum();
        initAnim();
        start();
    }

    @Override
    protected void onDetachedFromWindow() {
        stop();
        unregisterVolumeChangeReceiver();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {
            start();
        } else {
            stop();
        }
    }

    private void initAnim() {
        if (animator == null) {
            animator = ObjectAnimator.ofFloat(this, "columnHeight", 0, 1);
            animator.setDuration(1000);
            animator.setRepeatMode(ObjectAnimator.RESTART);
            animator.setRepeatCount(ObjectAnimator.INFINITE);
        }
    }

    private void initColumns() {
        columns.clear();
        columnCount = (int) Math.ceil(getMeasuredWidth() * 1.0 / (columnWidth + columnOffset));
        for (int i = 0; i < columnCount; i++) {
            columns.add(new Column());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(highLightColor);
        canvas.translate(0, getMeasuredHeight() * 1f / 2);
        for (int i = 0; i < highLightCount; i++) {
            rect.left = i * (columnWidth + columnOffset);
            rect.top = -columns.get(i).cur * 0.5f;
            rect.right = rect.left + columnWidth;
            rect.bottom = columns.get(i).cur * 0.5f;
            canvas.drawRoundRect(rect, 6, 6, paint);
        }

        paint.setColor(unHighLightColor);
        for (int i = highLightCount; i < columnCount; i++) {
            rect.left = i * (columnWidth + columnOffset);
            rect.top = -columns.get(i).cur * 0.5f;
            rect.right = rect.left + columnWidth;
            rect.bottom = columns.get(i).cur * 0.5f;
            canvas.drawRoundRect(rect, 6, 6, paint);
        }
    }

    /** 恢复（启动） */
    public void resume() {
        isPause = false;
        start();
    }

    /** 恢复（启动） */
    private void start() {
        if (animator == null || isPause) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (animator.isStarted() && animator.isPaused()) {
                animator.resume();
            } else if (!animator.isStarted()) {
                animator.start();
            }
        } else {
            if (!animator.isStarted()) {
                animator.start();
            }
        }
    }

    /** 暂停 */
    public void pause() {
        isPause = true;
        stop();
    }

    public void stop() {
        if (animator == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            animator.pause();
        } else {
            animator.cancel();
        }
    }

    private void setColumnHeight(float ratio) {
        for (int i = 0; i < columnCount; i++) {
            Column column = columns.get(i);
            float height = column.cur;
            float step = column.step;
            height += step;
            if (height >= column.max) {
                height = column.max;
                step = -step;
            }
            if (height <= column.min) {
                height = column.min;
                step = -step;
            }
            column.step = step;
            column.cur = height;
        }
        invalidate();
    }

    private class Column {

        float min; // 最小高度
        float max; // 最大高度
        float cur; // 当前高度
        float step; // 每次递加递减的高度

        Column() {
            int height = getMeasuredHeight();
            min = (float) (height * Math.random() * 0.4);
            max = (float) (height * (0.6 + Math.random() * 0.4));
            cur = (float) (min + (max - min) * Math.random());
            step = (max - min) * 0.05f * randomPlusMinus();
        }

        /** 随机生成1或-1 */
        private int randomPlusMinus() {
            double random = Math.random();
            if (random - 0.5 > 0) {
                return (int) Math.ceil(random - 0.5);
            } else if (random - 0.5 < 0) {
                return (int) Math.floor(random - 0.5);
            }
            return 1;
        }
    }
}
