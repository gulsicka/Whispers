package org.macmads.whispers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

public class ServiceBroadcastReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("service action received");
        String action = intent.getAction();
        Toast.makeText(context.getApplicationContext(), "discovery received "+action, Toast.LENGTH_SHORT).show();
        if (WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, -1);
            Toast.makeText(context.getApplicationContext(), "discovery changed", Toast.LENGTH_SHORT).show();
            if (state == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED) {
                Toast.makeText(context.getApplicationContext(), "discovery started", Toast.LENGTH_SHORT).show();

            } else if (state == WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED) {
                Toast.makeText(context.getApplicationContext(), "discovery stopped", Toast.LENGTH_SHORT).show();
                System.out.println("discovery stopped");

            }


        }
    }
}
