package com.example.findme.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.findme.R;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class SpreadView extends View {
    private int radius, maxRadius, distance, centerX=0, centerY=0, delayMilliseconds=50;
    private Paint centerPaint, spreadPaint;
    private ArrayList<Integer> alphas, spreadRadius;

    public SpreadView(Context context) {
        this(context,null,0);
    }

    public SpreadView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w/2;
        centerY = h/2;
    }

    public SpreadView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        alphas = new ArrayList<>();
        spreadRadius = new ArrayList<>();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SpreadView, defStyleAttr, 0);
        radius = a.getInt(R.styleable.SpreadView_spread_radius, radius);
        maxRadius = a.getInt(R.styleable.SpreadView_spread_max_radius, maxRadius);
        int centerColor = a.getColor(R.styleable.SpreadView_spread_center_color, ContextCompat.getColor(context, R.color.colorAccent));
        int spreadColor = a.getColor(R.styleable.SpreadView_spread_spread_color, ContextCompat.getColor(context, R.color.colorAccent));
        distance = a.getInt(R.styleable.SpreadView_spread_distance, distance);
        a.recycle();
        centerPaint = new Paint();
        centerPaint.setColor(centerColor);
        centerPaint.setAntiAlias(true);
        // Initially opaque with diffusion distance of 0
        alphas.add(255);
        spreadRadius.add(0);
        spreadPaint = new Paint();
        spreadPaint.setAntiAlias(true);
        spreadPaint.setAlpha(255);
        spreadPaint.setColor(spreadColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setStyle(Paint.Style.FILL);
        canvas.drawPaint(textPaint);

        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(80);
        for (int i = 0; i < spreadRadius.size(); i++) {
            int alpha = alphas.get(i);
            spreadPaint.setAlpha(alpha);
            int width = spreadRadius.get(i);
            // Drawing a diffused circle
            canvas.drawCircle(centerX, centerY, radius + width, spreadPaint);
            // Every time the radius of diffusion circle increases, the circular transparency decreases.
            if (alpha > 0 && width < 300) {
                alpha = alpha - distance > 0 ? alpha - distance : 1;
                alphas.set(i, alpha);
                spreadRadius.set(i, width + distance);
            }
        }
        // When the radius of the outermost diffusion circle reaches the maximum radius, a new diffusion circle is added.
        if (spreadRadius.get(spreadRadius.size() - 1) > maxRadius) {
            spreadRadius.add(0);
            alphas.add(255);
        }
        // Over eight diffusion circles, delete the first drawn circle, that is, the outermost circle
        if (spreadRadius.size() >= 8) {
            alphas.remove(0);
            spreadRadius.remove(0);
        }
        // The circle in the middle
        canvas.drawCircle(centerX, centerY, radius, centerPaint);
        canvas.drawText("SOS", centerX-80, centerY+20, textPaint);
        // Delayed updating to achieve diffuse visual impairment
        postInvalidateDelayed(delayMilliseconds);
    }
}
