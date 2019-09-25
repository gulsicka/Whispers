package org.macmads.whispers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class RowModel {
    String name;
    public RowModel(String name)
    {
        this.name=name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}