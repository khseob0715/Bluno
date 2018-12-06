package com.example.bluno.model;

public class Item {

    String name;
    String description;
    int index;
    PixelColor[] pixelColor;

    int r,g,b;

    public Item(String name, String description, int index, int[] r, int[] g, int[] b){
        this.name = name;
        this.description = description;
        this.index = index;

        this.pixelColor = new PixelColor[index];
        for(int i = 0 ; i < index ; i++) {
            pixelColor[i] = new PixelColor(i,r,g,b);
        }

    }

    public String getName(){
        return name;
    }

    public String getDescription(){
        return description;
    }

    public int getIndex(){
        return index;
    }

}
