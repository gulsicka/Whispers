package org.macmads.whispers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.Toast;

public class WifiConnectionBroadcastReciever extends BroadcastReceiver {
    WifiManager wifiManager;
    MainActivity activity;

    public WifiConnectionBroadcastReciever(MainActivity activity) {
        this.activity = activity;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            NetworkInfo networkInfo =
                    intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {

                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                if (wifiInfo != null) {
                    String ssid = wifiInfo.getSSID();
                    Toast.makeText(context.getApplicationContext(), "connected to " + ssid, Toast.LENGTH_SHORT).show();
                    Intent i = new Intent();
                    i.setClassName("org.macmads.whispers", "org.macmads.whispers.ChatActivity");
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.putExtra("server_ip", activity.formatIP(wifiManager.getDhcpInfo().gateway));
                    i.putExtra("nickName", "anonymous user");
                    context.startActivity(i);
                }
                // Wifi is connected
//                Toast.makeText(context.getApplicationContext(), "network: Wifi is connected: " + String.valueOf(networkInfo),Toast.LENGTH_SHORT).show();
            }
        } else if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            NetworkInfo networkInfo =
                    intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI &&
                    !networkInfo.isConnected()) {
                // Wifi is disconnected
//                Toast.makeText(context.getApplicationContext(), "connectivity: Wifi is disconnected: " + String.valueOf(networkInfo),Toast.LENGTH_SHORT).show();
            }

        }
//        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
//        if (info != null && info.isConnected()) {
//            // Do your work.
//
//
//            // e.g. To check the Network Name or other info:
//            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//
//            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//            if (wifiInfo != null) {
//                String ssid = wifiInfo.getSSID();
//                Toast.makeText(context.getApplicationContext(), "connected to " + ssid, Toast.LENGTH_SHORT).show();
//            }
//
//            //Toast.makeText(MainActivity.this, "server ip -" + formatIP(wifiManager.getDhcpInfo().gateway),
//            //      Toast.LENGTH_LONG).show();
//            //Toast.makeText(MainActivity.this, "server ip -" + formatIP(wifiManager.getDhcpInfo().ipAddress),
//            //      Toast.LENGTH_LONG).show();
//                Toast.makeText(MainActivity.this, "server ip -" + formatIP(wifiManager.getDhcpInfo().gateway),
//                        Toast.LENGTH_LONG).show();
//                Intent i = new Intent();
//                i.setClassName("org.macmads.whispers", "org.macmads.whispers.ChatActivity");
//                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                i.putExtra("server_ip", activity.formatIP(wifiManager.getDhcpInfo().gateway));
//                i.putExtra("nickName",activity.nickName.getText().toString() );
//                context.startActivity(i);
//
//
//        }


    }
}
