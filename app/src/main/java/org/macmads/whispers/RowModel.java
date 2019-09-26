package org.macmads.whispers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.p2p.WifiP2pDevice;

public class RowModel {
    String name;
    WifiP2pDevice device;
    public RowModel(String name, WifiP2pDevice device)
    {
        this.name = name;
        this.device = device;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}