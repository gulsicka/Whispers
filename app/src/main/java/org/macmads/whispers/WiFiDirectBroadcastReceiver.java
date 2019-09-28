package org.macmads.whispers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.widget.Toast;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {


    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private MainActivity mActivity;


    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,
                                       MainActivity activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi P2P is enabled
                System.out.println("wifi p2p is enabled");
            } else {
                // Wi-Fi P2P is not enable
                System.out.println("wifi p2p is not enabled");
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
            //if (mManager != null) { // listview mai devices ane aingi
               // mManager.requestPeers(mChannel, mActivity.peerListListener);
            //}
//wifi group jb bnta hay, tou yea neechay dono invoke hotay hay
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
            System.out.println("connection state changed");
            mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
                    if (wifiP2pGroup != null) {
                        String ssid = wifiP2pGroup.getNetworkName();
                        String password = wifiP2pGroup.getPassphrase();
                        Toast.makeText(context.getApplicationContext(), "group available", Toast.LENGTH_SHORT);
                        System.out.println("ssid: ");
                        System.out.println(ssid);
                        System.out.println("password: ");
                        System.out.println(password);
                    }

                }
            });


        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
            System.out.println("device state changed");
            mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
                    if (wifiP2pGroup != null) {
                        //this is hotspot's own ip and password
                        String ssid = wifiP2pGroup.getNetworkName();
                        String password = wifiP2pGroup.getPassphrase();
                        Toast.makeText(context, "group available", Toast.LENGTH_SHORT).show();
                        System.out.println("ssid: ");
                        System.out.println(ssid);
                        System.out.println("password: ");
                        System.out.println(password);
                        Map record = new HashMap();
                        record.put("wifi_ssid", ssid);
                        record.put("wifi_password", password);
                        //puts username and password record in service info
                        WifiP2pDnsSdServiceInfo serviceInfo = WifiP2pDnsSdServiceInfo.newInstance("_test", "_presence._tcp", record);
                        mManager.addLocalService(mChannel, serviceInfo, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                System.out.println("service added successfully");
                                String ipAddress = "0.0.0.0";
                                InetSocketAddress inetSockAddress = new InetSocketAddress(ipAddress, 38301);

                                WebsocketServer webSocketServer = new WebsocketServer(inetSockAddress);
                                webSocketServer.start();
                                Intent i = new Intent();
                                i.setClassName("org.macmads.whispers", "org.macmads.whispers.ChatActivity");
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                i.putExtra("server_ip","localhost");
                                context.startActivity(i);
                            }

                            @Override
                            public void onFailure(int i) {

                            }
                        });


                    }

                }
            });
        }
    }
}
