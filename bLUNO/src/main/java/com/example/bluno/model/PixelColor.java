package com.example.bluno.model;

import android.util.Log;

public class PixelColor {

    int index;
    int r_color;
    int g_color;
    int b_color;

    public PixelColor(int index, int r_color[], int g_color[], int b_color[]){
        this.index = index;

        this.r_color = r_color[index];
        this.g_color = g_color[index];
        this.b_color = b_color[index];

        Log.e("E","call creator"+index);
    }

    public int getB_color() {
        return b_color;
    }

    public int getIndex(){
        return index;
    }

    public int getG_color() {
        return g_color;
    }

    public int getR_color() {
        return r_color;
    }

}
