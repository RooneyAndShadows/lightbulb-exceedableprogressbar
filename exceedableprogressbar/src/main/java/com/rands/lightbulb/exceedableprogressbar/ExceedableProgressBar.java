package com.rands.lightbulb.exceedableprogressbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.rands.lightbulb.commons.utils.ResourceUtils;
import com.rands.java.commons.numeric.NumberUtils;

import androidx.databinding.BindingAdapter;


@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class ExceedableProgressBar extends FrameLayout {
    private int minX;
    private int minY;
    private int maxX;
    private int maxY;
    private float progressBarMaxWidth;
    private double min;
    private double max;
    private double progress;
    private int indeterminateColor;
    private int progressColor;
    private int exceededColor;
    private int percentageTextColor;
    private int topTextColor;
    private int topTextSize;
    private int percentageTextSize;
    private int progressBarHeight;
    private int progressBarRadius;
    private float progressBarTop;
    private float progressBarBottom;
    private int topTextBottomSpace;
    private float progressEndX;
    private String currentProgressText;
    private String plannedMaxText;
    private String percentageText;
    private Rect plannedMaxTextBounds;
    private Rect percentageTextBounds;
    private TextFormatter topTextFormatter;

    public ExceedableProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        readAttributes(context, attrs);
        initView();
    }

    private void initView() {
        FrameLayout container = (FrameLayout) inflate(getContext(), R.layout.progress_bar_view, this);
        initializeStrings();
        progressBarRadius = ResourceUtils.dpToPx(4);
        topTextBottomSpace = ResourceUtils.dpToPx(10);
    }

    public void setTopTextFormatter(TextFormatter progressTextFormatter) {
        this.topTextFormatter = progressTextFormatter;
        invalidate();
    }

    public void setIndeterminateColor(int indeterminateColor) {
        this.indeterminateColor = indeterminateColor;
        invalidate();
    }

    public void setProgressColor(int progressColor) {
        this.progressColor = progressColor;
        invalidate();
    }

    public void setExceededColor(int exceededColor) {
        this.exceededColor = exceededColor;
        invalidate();
    }

    public void setMin(double minimum) {
        this.min = minimum;
        if (min > max) {
            double tmp = min;
            min = max;
            max = tmp;
        }
        invalidate();
    }

    public void setMax(double maximum) {
        this.max = maximum;
        if (min > max) {
            double tmp = min;
            min = max;
            max = tmp;
        }
        invalidate();
    }

    public void setProgress(double progress) {
        this.progress = progress;
        if (this.progress < min) {
            this.progress = min;
        }
        invalidate();
    }

    @BindingAdapter("ExceedableProgressBarMin")
    public static void setMin(ExceedableProgressBar view, double minimum) {
        view.setMin(minimum);
    }

    @BindingAdapter("ExceedableProgressBarMax")
    public static void setMax(ExceedableProgressBar view, double maximum) {
        view.setMax(maximum);
    }

    @BindingAdapter("ExceedableProgressBarProgress")
    public static void setProgress(ExceedableProgressBar view, double progress) {
        view.setProgress(progress);
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getProgress() {
        return progress;
    }

    protected void readAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ExceedableProgressBar, 0, 0);
        try {
            min = a.getFloat(R.styleable.ExceedableProgressBar_ExceedableProgressBarMin, 0F);
            max = a.getFloat(R.styleable.ExceedableProgressBar_ExceedableProgressBarMax, 1F);
            progress = a.getFloat(R.styleable.ExceedableProgressBar_ExceedableProgressBarProgress, 0F);
            indeterminateColor = a.getColor(R.styleable.ExceedableProgressBar_ExceedableProgressBarColorIndeterminate, ResourceUtils.getColorById(getContext(), R.color.exceedable_pbar_indeterminate_color));
            progressColor = a.getColor(R.styleable.ExceedableProgressBar_ExceedableProgressBarColorProgress, ResourceUtils.getColorByAttribute(getContext(), android.R.attr.colorPrimary));
            exceededColor = a.getColor(R.styleable.ExceedableProgressBar_ExceedableProgressBarColorExceeded, ResourceUtils.getColorByAttribute(getContext(), android.R.attr.colorPrimaryDark));
            percentageTextColor = a.getColor(R.styleable.ExceedableProgressBar_ExceedableProgressBarColorPercentageText, ResourceUtils.getColorById(getContext(), R.color.exceedable_pbar_default_text_color));
            topTextColor = a.getColor(R.styleable.ExceedableProgressBar_ExceedableProgressBarColorTopText, ResourceUtils.getColorById(getContext(), R.color.exceedable_pbar_default_text_color));
            topTextSize = a.getDimensionPixelSize(R.styleable.ExceedableProgressBar_ExceedableProgressBarTopTextSize, ResourceUtils.spToPx(14));
            percentageTextSize = a.getDimensionPixelSize(R.styleable.ExceedableProgressBar_ExceedableProgressBarPercentageTextSize, ResourceUtils.spToPx(14));
        } finally {
            a.recycle();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        boolean isExceeded = getMax() < getProgress();
        minX = getPaddingLeft();
        maxX = getWidth() - getPaddingEnd();
        minY = getPaddingTop();
        maxY = getHeight() - getPaddingBottom();
        progressBarHeight = percentageTextBounds.height();
        progressBarMaxWidth = maxX - percentageTextBounds.width() - ResourceUtils.dpToPx(10);
        progressBarTop = maxY - (float) (percentageTextBounds.height());
        progressBarBottom = progressBarTop + progressBarHeight;
        if (progress <= 0) {
            progressEndX = minX;
        } else {
            if (isExceeded)
                progressEndX = (float) (progressBarMaxWidth * max / progress);
            else
                progressEndX = (float) (progressBarMaxWidth * progress / max);
        }
        if (progressEndX > progressBarMaxWidth)
            progressEndX = progressBarMaxWidth;
        drawProgress(canvas, isExceeded);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int wDesired = getPaddingLeft() + getPaddingRight() + Math.max(100, getSuggestedMinimumWidth());
        int wSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int w = 0;
        switch (wSpecMode) {
            case MeasureSpec.EXACTLY:
                w = MeasureSpec.getSize(widthMeasureSpec);
                break;
            case MeasureSpec.AT_MOST:
                w = Math.min(wDesired, MeasureSpec.getSize(widthMeasureSpec));
                break;
            case MeasureSpec.UNSPECIFIED:
                w = wDesired;
                break;
        }
        int viewHeight = Math.max(percentageTextBounds.height(), progressBarHeight) + plannedMaxTextBounds.height() + topTextBottomSpace;//Spacing for marker text
        int hDesired = getPaddingTop() + getPaddingBottom() + Math.max(viewHeight, getSuggestedMinimumHeight());
        int hSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int h = 0;
        switch (hSpecMode) {
            case MeasureSpec.EXACTLY:
                h = MeasureSpec.getSize(heightMeasureSpec);
                break;
            case MeasureSpec.AT_MOST:
                h = Math.min(hDesired, MeasureSpec.getSize(heightMeasureSpec));
                break;
            case MeasureSpec.UNSPECIFIED:
                h = hDesired;
                break;
        }
        setMeasuredDimension(w, h);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        initializeStrings();
    }

    private void initializeStrings() {
        //Planned max
        plannedMaxText = NumberUtils.getDoubleString(max, ' ', '.');
        //Percents
        double percentage = 0D;
        if (max > 0)
            percentage = (progress * 100) / max;
        percentageText = NumberUtils.getDoubleString(percentage, ' ', '.').concat(" %");
        if (topTextFormatter != null)
            plannedMaxText = topTextFormatter.format(plannedMaxText);
        plannedMaxTextBounds = getTextBounds(plannedMaxText, topTextSize, Typeface.DEFAULT);
        percentageTextBounds = getTextBounds(percentageText, percentageTextSize, Typeface.DEFAULT_BOLD);
    }

    private Rect getTextBounds(String text, int textSize, Typeface typeface) {
        Paint textPaint = new Paint();
        textPaint.setTextSize(textSize);
        textPaint.setElegantTextHeight(true);
        textPaint.setAntiAlias(true);
        //textPaint.setTypeface(typeface);
        Rect bounds = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), bounds);
        bounds.right = (int) textPaint.measureText(text);
        return bounds;
    }

    private void drawProgressBarStrings(Canvas canvas) {
        drawPercentageText(canvas);
        drawTopText(canvas);
    }

    private void drawTopText(Canvas canvas) {
        int textHeight = plannedMaxTextBounds.height();
        int textWidth = plannedMaxTextBounds.width();
        Paint textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(topTextSize);
        textPaint.setColor(topTextColor);
        canvas.drawText(plannedMaxText, maxX - textWidth, minY + textHeight, textPaint);
    }

    private void drawPercentageText(Canvas canvas) {
        int textWidth = percentageTextBounds.width();
        int textHeight = percentageTextBounds.height();
        float textStartX = maxX - textWidth;
        float textStartY = maxY;
        Paint textPaint = new Paint();
        //textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(percentageTextSize);
        textPaint.setColor(percentageTextColor);
        canvas.drawText(percentageText, textStartX, textStartY, textPaint);
    }

    private void drawProgress(Canvas canvas, boolean isExceeded) {
        int savedCount = canvas.save();
        drawIndeterminateLine(canvas);
        drawProgressLine(canvas, isExceeded);
        if (isExceeded)
            drawExceededLine(canvas);
        drawProgressBarStrings(canvas);
        canvas.restoreToCount(savedCount);
    }

    private void drawIndeterminateLine(Canvas canvas) {
        drawRoundedRect(canvas, minX, progressBarTop, progressBarMaxWidth, progressBarBottom, indeterminateColor, progressBarRadius, true, true, true, true);
    }

    private void drawProgressLine(Canvas canvas, boolean isExceeded) {
        drawRoundedRect(canvas, minX, progressBarTop, progressEndX, progressBarBottom, progressColor, progressBarRadius, true, !isExceeded, !isExceeded, true);
    }

    private void drawExceededLine(Canvas canvas) {
        boolean roundStart = progressEndX == minX;
        drawRoundedRect(canvas, progressEndX, progressBarTop, progressBarMaxWidth, progressBarBottom, exceededColor, progressBarRadius, roundStart, true, true, roundStart);
    }

    private void drawRect(Canvas canvas, float fromX, float toX, float fromY, float toY, int color) {
        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(color);
        canvas.drawRect(fromX, fromY,
                toX, toY,
                backgroundPaint);
    }

    private void drawRoundedRect(Canvas canvas, float startX, float startY, float toX, float toY, int color, float radius, boolean tl, boolean tr, boolean br, boolean bl) {
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawPath(RoundedRect(startX, startY, toX, toY, radius, radius, tl, tr, br, bl), paint);
    }


    private static Path RoundedRect(float left, float top, float right, float bottom, float rx, float ry, boolean tl, boolean tr, boolean br, boolean bl) {
        Path path = new Path();
        if (rx < 0) rx = 0;
        if (ry < 0) ry = 0;
        float width = right - left;
        float height = bottom - top;
        if (rx > width / 2) rx = width / 2;
        if (ry > height / 2) ry = height / 2;
        float widthMinusCorners = (width - (2 * rx));
        float heightMinusCorners = (height - (2 * ry));
        path.moveTo(right, top + ry);
        if (tr)
            path.rQuadTo(0, -ry, -rx, -ry);
        else {
            path.rLineTo(0, -ry);
            path.rLineTo(-rx, 0);
        }
        path.rLineTo(-widthMinusCorners, 0);
        if (tl)
            path.rQuadTo(-rx, 0, -rx, ry);
        else {
            path.rLineTo(-rx, 0);
            path.rLineTo(0, ry);
        }
        path.rLineTo(0, heightMinusCorners);
        if (bl)
            path.rQuadTo(0, ry, rx, ry);
        else {
            path.rLineTo(0, ry);
            path.rLineTo(rx, 0);
        }
        path.rLineTo(widthMinusCorners, 0);
        if (br)
            path.rQuadTo(rx, 0, rx, -ry);
        else {
            path.rLineTo(rx, 0);
            path.rLineTo(0, -ry);
        }
        path.rLineTo(0, -heightMinusCorners);
        path.close();
        return path;
    }

    public interface TextFormatter {
        String format(String progressText);
    }
}
