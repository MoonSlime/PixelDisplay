package com.pixeldp.view.widgets;

import android.view.View;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;

import com.pixeldp.nodi.R;


public class HoloCircleSeekBar extends View {
    private static final String STATE_PARENT = "parent";
    private static final String STATE_ANGLE = "angle";
    private static final int TEXT_SIZE_DEFAULT_VALUE = 25;
    private static final int END_WHEEL_DEFAULT_VALUE = 360;
    public static final int COLOR_WHEEL_STROKE_WIDTH_DEF_VALUE = 12;
    public static final float POINTER_RADIUS_DEF_VALUE = 8;
    public static final int MAX_POINT_DEF_VALUE = 100;
    public static final int START_ANGLE_DEF_VALUE = 0;
    public static final int COLOR_BACK_STROKE_WIDTH_DEF_VALUE = 0;
    private static boolean SHOW_WHEEL = false;
    private static boolean SHOW_BACKGROUND = false;
    public static final boolean ENABLE_BACK_DEF_VALUE = true;
    private OnCircleSeekBarChangeListener mOnCircleSeekBarChangeListener;
    private Paint mColorWheelPaint;
    private Paint mPointerHaloPaint;
    private Paint mPointerColor;
    private int mColorWheelStrokeWidth;
    private boolean enableBackground;
    private float mPointerRadius;
    private RectF mColorWheelRectangle = new RectF();
    private RectF mColorBackRectangle = new RectF();
    private boolean mUserIsMovingPointer = false;
    private float mTranslationOffset;
    private float mColorWheelRadius;
    private Paint mBackPaint;
    private int mColorBackStrokeWidth;


    private float mAngle;
    private Paint textPaint;
    private String text;
    private int max = 100;
    private SweepGradient s;
    private Paint mArcColor;
    private int wheel_color, unactive_wheel_color, pointer_color, pointer_halo_color, text_size, text_color, back_color;
    private int init_position = -1;
    private boolean block_end = false;
    private float lastX;
    private int last_radians = 0;
    private boolean block_start = false;

    private int arc_finish_radians = 360;
    private int start_arc = 270;
    private float[] pointerPosition;
    private RectF mColorCenterHaloRectangle = new RectF();
    private int end_wheel;
    private boolean show_text = true;
    private Rect bounds = new Rect();

    public HoloCircleSeekBar(Context context) {
        super(context);
        init(null, 0);
    }

    public HoloCircleSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public HoloCircleSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.HoloCircleSeekBar, defStyle, 0);
        initAttributes(a);
        a.recycle();

        mColorWheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mColorWheelPaint.setShader(s);
        mColorWheelPaint.setColor(unactive_wheel_color);
        mColorWheelPaint.setStyle(Style.STROKE);
        mColorWheelPaint.setStrokeWidth(mColorWheelStrokeWidth);

        Paint mColorCenterHalo = new Paint(Paint.ANTI_ALIAS_FLAG);
        mColorCenterHalo.setColor(Color.CYAN);
        mColorCenterHalo.setAlpha(0xCC);


        mPointerHaloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPointerHaloPaint.setColor(pointer_halo_color);
        mPointerHaloPaint.setStrokeWidth(mPointerRadius + 10);


        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        textPaint.setColor(text_color);
        textPaint.setStyle(Style.FILL_AND_STROKE);
        textPaint.setTextAlign(Align.LEFT);
        textPaint.setTextSize(text_size);

        mPointerColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPointerColor.setStrokeWidth(mPointerRadius);
        mPointerColor.setColor(pointer_color);

        mArcColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        mArcColor.setColor(wheel_color);
        mArcColor.setStyle(Style.STROKE);
        mArcColor.setStrokeWidth(mColorWheelStrokeWidth);

        mBackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackPaint.setColor(back_color);
        mBackPaint.setStyle(Style.STROKE);
        mBackPaint.setStrokeWidth(mColorBackStrokeWidth);

        Paint mCircleTextColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCircleTextColor.setColor(Color.WHITE);
        mCircleTextColor.setStyle(Style.FILL);

        arc_finish_radians = (int) calculateAngleFromText(init_position) - 90;

        if (arc_finish_radians > end_wheel)
            arc_finish_radians = end_wheel;
        mAngle = calculateAngleFromRadians(arc_finish_radians > end_wheel ? end_wheel
                : arc_finish_radians);
        setTextFromAngle(calculateValueFromAngle(arc_finish_radians));

        invalidate();
    }

    private void setTextFromAngle(int angleValue) {
        this.text = String.valueOf(angleValue);
    }

    private void initAttributes(TypedArray a) {
        mColorWheelStrokeWidth = a.getInteger(R.styleable.HoloCircleSeekBar_wheel_size, COLOR_WHEEL_STROKE_WIDTH_DEF_VALUE);
        mPointerRadius = a.getDimension(R.styleable.HoloCircleSeekBar_pointer_size, POINTER_RADIUS_DEF_VALUE);
        mColorBackStrokeWidth = a.getInteger(R.styleable.HoloCircleSeekBar_background_size, COLOR_BACK_STROKE_WIDTH_DEF_VALUE);
        SHOW_BACKGROUND = a.getBoolean(R.styleable.HoloCircleSeekBar_show_background, true);
        max = a.getInteger(R.styleable.HoloCircleSeekBar_max, MAX_POINT_DEF_VALUE);

        String wheel_color_attr = a.getString(R.styleable.HoloCircleSeekBar_wheel_active_color);
        String wheel_unactive_color_attr = a.getString(R.styleable.HoloCircleSeekBar_wheel_unactive_color);
        String pointer_color_attr = a.getString(R.styleable.HoloCircleSeekBar_pointer_color);
        String pointer_halo_color_attr = a.getString(R.styleable.HoloCircleSeekBar_pointer_halo_color);
        String text_color_attr = a.getString(R.styleable.HoloCircleSeekBar_text_color);
        String back_color_attr = a.getString(R.styleable.HoloCircleSeekBar_background_color);

        text_size = a.getDimensionPixelSize(R.styleable.HoloCircleSeekBar_text_size, TEXT_SIZE_DEFAULT_VALUE);
        init_position = a.getInteger(R.styleable.HoloCircleSeekBar_init_position, 0);
        start_arc = a.getInteger(R.styleable.HoloCircleSeekBar_start_angle, START_ANGLE_DEF_VALUE);
        end_wheel = a.getInteger(R.styleable.HoloCircleSeekBar_end_angle, END_WHEEL_DEFAULT_VALUE);
        show_text = a.getBoolean(R.styleable.HoloCircleSeekBar_show_text, true);

        last_radians = end_wheel;

        if (init_position < start_arc)
            init_position = calculateTextFromStartAngle(start_arc);

        if (wheel_color_attr != null) {
            try {
                wheel_color = Color.parseColor(wheel_color_attr);
            } catch (IllegalArgumentException e) {
                wheel_color = Color.DKGRAY;
            }

        } else {
            wheel_color = Color.DKGRAY;
        }
        if (wheel_unactive_color_attr != null) {
            try {
                unactive_wheel_color = Color
                        .parseColor(wheel_unactive_color_attr);
            } catch (IllegalArgumentException e) {
                unactive_wheel_color = Color.CYAN;
            }

        } else {
            unactive_wheel_color = Color.CYAN;
        }

        if (pointer_color_attr != null) {
            try {
                pointer_color = Color.parseColor(pointer_color_attr);
            } catch (IllegalArgumentException e) {
                pointer_color = Color.CYAN;
            }

        } else {
            pointer_color = Color.CYAN;
        }

        if (pointer_halo_color_attr != null) {
            try {
                pointer_halo_color = Color.parseColor(pointer_halo_color_attr);
            } catch (IllegalArgumentException e) {
                pointer_halo_color = Color.CYAN;
            }

        } else {
            pointer_halo_color = Color.DKGRAY;
        }

        if (text_color_attr != null) {
            try {
                text_color = Color.parseColor(text_color_attr);
            } catch (IllegalArgumentException e) {
                text_color = Color.CYAN;
            }
        } else {
            text_color = Color.CYAN;
        }

        if (back_color_attr != null) {
            try {
                back_color = Color.parseColor(back_color_attr);
            } catch (IllegalArgumentException e) {
                back_color = Color.argb(128, 255, 255, 255);
            }
        } else {
            back_color = Color.argb(128, 255, 255, 255);
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.translate(mTranslationOffset, mTranslationOffset);
        canvas.drawArc(mColorBackRectangle, 0, 360, false, mBackPaint);
        canvas.drawArc(mColorWheelRectangle, start_arc + 270, end_wheel - (start_arc), false, mColorWheelPaint);
        canvas.drawArc(mColorWheelRectangle, start_arc + 270, (arc_finish_radians) > (end_wheel) ? end_wheel - (start_arc) : arc_finish_radians - start_arc, false, mArcColor);
        canvas.drawCircle(pointerPosition[0], pointerPosition[1], mPointerRadius, mPointerHaloPaint);
        canvas.drawCircle(pointerPosition[0], pointerPosition[1], (float) (mPointerRadius / 1.2), mPointerColor);

        textPaint.getTextBounds(text, 0, text.length(), bounds);
        if (show_text)
            canvas.drawText(text, (mColorWheelRectangle.centerX()) - (textPaint.measureText(text) / 2), mColorWheelRectangle.centerY() + bounds.height() / 2, textPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int min = Math.min(width, height);
        setMeasuredDimension(min, min);
        mTranslationOffset = min * 0.5f;
        mColorWheelRadius = mTranslationOffset - mPointerRadius;
        mColorBackRectangle.set(-mColorWheelRadius - 5, -mColorWheelRadius - 5, mColorWheelRadius + 5, mColorWheelRadius + 5);
        mColorWheelRectangle.set(-mColorWheelRadius, -mColorWheelRadius, mColorWheelRadius, mColorWheelRadius);
        mColorCenterHaloRectangle.set(-mColorWheelRadius / 2, -mColorWheelRadius / 2, mColorWheelRadius / 2, mColorWheelRadius / 2);
        updatePointerPosition();

    }

    private int calculateValueFromAngle(float angle) {
        float m = angle - start_arc;

        float f = (end_wheel - start_arc) / m;

        return (int) (max / f);
    }

    private int calculateTextFromStartAngle(float angle) {
        float f = (end_wheel - start_arc) / angle;

        return (int) (max / f);
    }

    private double calculateAngleFromText(int position) {
        if (position == 0 || position >= max)
            return (float) 90;

        double f = (double) max / (double) position;

        double f_r = 360 / f;

        return f_r + 90;
    }

    private int calculateRadiansFromAngle(float angle) {
        float unit = (float) (angle / (2 * Math.PI));
        if (unit < 0) {
            unit += 1;
        }
        int radians = (int) ((unit * 360) - ((360 / 4) * 3));
        if (radians < 0)
            radians += 360;
        return radians;
    }

    private float calculateAngleFromRadians(int radians) {
        return (float) (((radians + 270) * (2 * Math.PI)) / 360);
    }

    /**
     * Get the selected value
     *
     * @return the value between 0 and max
     */
    public int getValue() {
        return Integer.valueOf(text);
    }

    public void setMax(int max) {
        this.max = max;
        setTextFromAngle(calculateValueFromAngle(arc_finish_radians));
        updatePointerPosition();
        invalidate();
    }

    public void setValue(float newValue) {
        if (newValue == 0) {
            arc_finish_radians = start_arc;
        } else if (newValue == this.max) {
            arc_finish_radians = end_wheel;
        } else {
            float newAngle = (float) (360.0 * (newValue / max));
            arc_finish_radians = (int) calculateAngleFromRadians(calculateRadiansFromAngle(newAngle)) + 1;
        }

        mAngle = calculateAngleFromRadians(arc_finish_radians);
        setTextFromAngle(calculateValueFromAngle(arc_finish_radians));
        updatePointerPosition();
        invalidate();
    }

    private void updatePointerPosition() {
        pointerPosition = calculatePointerPosition(mAngle);
    }

    /**
     * Calculate the pointer's coordinates on the color wheel using the supplied
     * angle.
     *
     * @param angle The position of the pointer expressed as angle (in rad).
     * @return The coordinates of the pointer's center in our internal
     * coordinate system.
     */
    private float[] calculatePointerPosition(float angle) {
        // if (calculateRadiansFromAngle(angle) > end_wheel)
        // angle = calculateAngleFromRadians(end_wheel);
        float x = (float) (mColorWheelRadius * Math.cos(angle));
        float y = (float) (mColorWheelRadius * Math.sin(angle));

        return new float[]{x, y};
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        Bundle state = new Bundle();
        state.putParcelable(STATE_PARENT, superState);
        state.putFloat(STATE_ANGLE, mAngle);

        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle savedState = (Bundle) state;

        Parcelable superState = savedState.getParcelable(STATE_PARENT);
        super.onRestoreInstanceState(superState);

        mAngle = savedState.getFloat(STATE_ANGLE);
        arc_finish_radians = calculateRadiansFromAngle(mAngle);
        setTextFromAngle(calculateValueFromAngle(arc_finish_radians));
        updatePointerPosition();
    }

    public void setInitPosition(int init) {
        init_position = init;
        setTextFromAngle(init_position);
        mAngle = calculateAngleFromRadians(init_position);
        arc_finish_radians = calculateRadiansFromAngle(mAngle);
        updatePointerPosition();
        invalidate();
    }

    public void setOnSeekBarChangeListener(OnCircleSeekBarChangeListener l) {
        mOnCircleSeekBarChangeListener = l;
    }

    public int getMaxValue() {
        return max;
    }

    public interface OnCircleSeekBarChangeListener {

        void onProgressChanged(HoloCircleSeekBar seekBar, int progress, boolean fromUser);

        void onStartTrackingTouch(HoloCircleSeekBar seekBar);

        void onStopTrackingTouch(HoloCircleSeekBar seekBar);

    }

}