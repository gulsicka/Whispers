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
import java.net.URI;
import java.net.URISyntaxException;
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

        if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
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
                                URI serverUri = null;
                                try {
                                    serverUri = new URI("ws://localhost:38301");
                                } catch (URISyntaxException e) {
                                    e.printStackTrace();
                                }

                                try{
                                    WebsocketClient.initialize(serverUri,context.getApplicationContext());
                                    WebsocketClient.setContext(context.getApplicationContext());

                                }
                                catch (Exception exception){
                                    Toast.makeText(context.getApplicationContext(),exception.toString(),Toast.LENGTH_SHORT).show();
                                }

                                Toast.makeText(context," service registered",Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(int i) {
                                Toast.makeText(context,"failed service registery",Toast.LENGTH_LONG).show();
                            }
                        });


                    }

                }
            });
        }
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
