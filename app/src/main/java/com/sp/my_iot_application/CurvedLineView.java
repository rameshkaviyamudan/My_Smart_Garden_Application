package com.sp.my_iot_application;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CurvedLineView extends View {
    private Paint paint;
    private float centerX, centerY, outerRadius, innerRadius;
    private float dotX, dotY; // Coordinates of the dot relative to the center

    public CurvedLineView(Context context) {
        super(context);
        init();
    }

    public CurvedLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.parseColor("#4C0E0E")); // Set the color of the line
        paint.setStrokeWidth(5f); // Set the width of the line
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
    }

    public void setCircles(float outerRadius, float innerRadius) {
        this.outerRadius = outerRadius;
        this.innerRadius = innerRadius;

        // Set dot initial position on the imaginary circle
        dotX = outerRadius;
        dotY = 0;

        // Trigger a redraw when the circle parameters are set
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Set center coordinates based on the dimensions of the view
        centerX = w / 2f;
        centerY = h / 2f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw the outer circle
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(centerX, centerY, outerRadius, paint);

        // Draw the inner circle
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(centerX, centerY, innerRadius, paint);

        // Draw the dot on the track
        paint.setStyle(Paint.Style.FILL);

        // Calculate the dot position on the imaginary circle
        float dotCircleRadius = 300;
        float dotCircleX = centerX;
        float dotCircleY = centerY;

        float dotAngle = (float) Math.atan2(dotY, dotX);
        float dotCircleCenterX = dotCircleX + dotCircleRadius * (float) Math.cos(dotAngle);
        float dotCircleCenterY = dotCircleY + dotCircleRadius * (float) Math.sin(dotAngle);

        // Set dot position relative to the center
        float adjustedDotX = dotCircleCenterX + dotX;
        float adjustedDotY = dotCircleCenterY + dotY;

        canvas.drawCircle(adjustedDotX, adjustedDotY, paint.getStrokeWidth(), paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                // Update dot position based on touch event
                updateDotPosition(event.getX(), event.getY());
                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }

    public void updateDotPosition(float touchX, float touchY) {
        // Calculate the dot position relative to the center
        dotX = touchX - centerX;
        dotY = touchY - centerY;

        // Limit the dot movement to the imaginary circle
        float dotRadius = (float) Math.sqrt(dotX * dotX + dotY * dotY);
        if (dotRadius > 300) {
            // Normalize the vector and set it to the edge of the circle
            float scaleFactor = 300 / dotRadius;
            dotX *= scaleFactor;
            dotY *= scaleFactor;
        }
    }
    public void setDotPosition(float x, float y) {
        // Ensure the dot stays within the track
        if (isInsideTrack(x, y)) {
            dotX = x;
            dotY = y;
            invalidate();
        }
    }

    private boolean isInsideTrack(float x, float y) {
        // Check if the point is inside the track (between the circles)
        float distance = calculateDistanceFromCenter(x, y);
        return distance > innerRadius && distance < outerRadius;
    }

    private float calculateDistanceFromCenter(float x, float y) {
        float distanceX = x - centerX;
        float distanceY = y - centerY;
        return (float) Math.sqrt(distanceX * distanceX + distanceY * distanceY);
    }
}
