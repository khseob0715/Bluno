package com.example.bluno.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.example.bluno.R;

import java.util.ArrayList;
import java.util.List;

public class CanvasView extends LinearLayout {

    private TextView[][] pixels = null;
    private int pixelRadius = 0;

    public int color = ColorUtils.setAlphaComponent(ContextCompat.getColor(getContext(), R.color.colorAccent), 255);
    public int brushSize = 1;

    public CanvasView(Context context) {
        super(context);
    }
    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public CanvasView(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }

    public int getColor() {
        return color;
    }

    public int getRows() {
        return pixels.length;
    }

    public int getColumns() {
        return pixels[0].length;
    }

    public void generate(ViewGroup body, int rows, int columns, int[] pixelColors) {
        // calculate best pixel size
        int calculatedHeight = (body.getMeasuredHeight() - 100) / rows;
        int calculatedWidth = (body.getMeasuredWidth() - 100) / columns;
        pixelRadius = calculatedHeight < calculatedWidth ? calculatedHeight : calculatedWidth;

        // setup canvas pixels
        pixels = new TextView[rows][columns];

        for (int i = 0; i < rows; i++) {
            // make rows
            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);

            for (int j = 0; j < columns; j++) {
                // make pixel
                TextView pixel = new TextView(getContext());
                pixel.setHeight(pixelRadius);
                pixel.setWidth(pixelRadius);
                int color = Color.WHITE;
                if (pixelColors != null)
                    color = pixelColors[i*columns+j];
                pixel.setBackgroundColor(color);
                pixels[i][j] = pixel;
                row.addView(pixel);
            }
            addView(row);
        }
    }

    // fill canvas to one color
    public void fill() {
        for (TextView[] row : pixels)
            for (TextView button : row)
                button.setBackgroundColor(color);
    }

    public void fill_white(int white_color) {
        for (TextView[] row : pixels)
            for (TextView button : row)
                button.setBackgroundColor(white_color);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        List<View> pixels = findPixels(event);
        for (View pixel: pixels)
            pixel.setBackgroundColor(color);
        return true;

    }
    // allow coloring multiple pixels on touch
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return true;
    }

    public int[] toArray() {
        int[] list = new int[pixels.length*pixels[0].length];
        int i = 0;
        for (TextView[] row : pixels) {
            for (TextView pixel : row) {
                list[i] = ((ColorDrawable) pixel.getBackground()).getColor();
                i++;
            }
        }

        return list;
    }

    @Override
    public String toString() {
        //String list = pixels.length+";"+pixels[0].length+";";
        String list = "";
        for (TextView[] row : pixels) {
            for (TextView pixel : row) {
                // ((ColorDrawable) pixel.getBackground()).getColor()   10진수 값.
                // 10진수를 Hex로
                // Integer.toHexString(((ColorDrawable) pixel.getBackground()).getColor());

                // list += ((ColorDrawable) pixel.getBackground()).getColor()+",";

                int r = (((ColorDrawable) pixel.getBackground()).getColor() & 0x00ff0000)>>16;
                int g = (((ColorDrawable) pixel.getBackground()).getColor() & 0x0000ff00)>>8;
                int b = (((ColorDrawable) pixel.getBackground()).getColor() & 0x000000ff)>>0;

                switch ((int)Math.log10(r)+1) {
                    case 0:
                        list += "000";
                        break;
                    case 1:
                        list += "00" + r;
                        break;
                    case 2:
                        list += "0" + r;
                        break;
                    case 3:
                        list += r;
                        break;
                }

                switch ((int)Math.log10(g)+1) {
                    case 0:
                        list += "000";
                        break;
                    case 1:
                        list += "00" + g;
                        break;
                    case 2:
                        list += "0" + g;
                        break;
                    case 3:
                        list += g;
                        break;
                }

                switch ((int)Math.log10(b)+1) {
                    case 0:
                        list += "000";
                        break;
                    case 1:
                        list += "00" + b;
                        break;
                    case 2:
                        list += "0" + b;
                        break;
                    case 3:
                        list += b;
                        break;
                }

                list += ",";
            }
        }

        return list.substring(0, list.length() - 1);
    }


    public List<View> findPixels(MotionEvent motionEvent) {
        // find pixels under current coordinates
        int x = (int) motionEvent.getRawX();
        int y = (int) motionEvent.getRawY();

        List<View> list = new ArrayList<>();
        int rad = (int) (pixelRadius*brushSize*1.05) /2;

        rowloop: for (TextView[] row : pixels) {
            for (TextView pixel : row) {
                int params[] = new int[2];
                pixel.getLocationOnScreen(params);

                if (brushSize > 1) {
                    if (Math.sqrt(Math.pow((x - params[0]), 2) + Math.pow((y - params[1]), 2)) <= rad)
                        list.add(pixel);
                    if (y > params[1]*rad )
                        return list;

                } else if ( y >= params[1] - pixelRadius*brushSize && y <= params[1] + pixelRadius*brushSize) {
                    if ( x >= params[0] - pixelRadius*brushSize && x <= params[0] + pixelRadius*brushSize) {
                        list.add(pixel);
                        return list;
                    }
                } else {
                    continue rowloop;
                }
            }
        }

        return list;
    }

    public TextView[][] getRotated() {
        final int m = pixels.length;
        final int n = pixels[0].length;
        final TextView[][] newpixels = new TextView[n][m];

        for (int r = 0; r < m; r++) {
            for (int c = 0; c < n; c++) {
                TextView pixel = new TextView(getContext());
                pixel.setHeight(pixelRadius);
                pixel.setWidth(pixelRadius);
                pixel.setBackgroundColor(((ColorDrawable) pixels[r][c].getBackground()).getColor());
                newpixels[c][m-1-r] = pixel;
            }
        }
        return newpixels;
    }

    public void load(TextView[][] newpixels) {
        removeAllViews();

        for (TextView[] pixelrow: newpixels) {
            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            for (TextView pixel: pixelrow) {
                row.addView(pixel);
            }
            addView(row);
        }

        pixels = newpixels;
    }
}