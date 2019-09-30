package org.macmads.whispers;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;

import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Build;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.macmads.whispers.R;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 1001;

    WifiP2pManager manager;// call wifi direct's function
    Channel channel;// when wifip2p is initilized it returns a channel
    BroadcastReceiver receiver;// recieves acceptance of permissions granted by system e.g. grant location
    IntentFilter intentFilter;//

    //    BroadcastReceiver serviceReciever;// recieves acceptance of permissions granted by system e.g. grant location
    IntentFilter serviceFilter;

    final HashMap<String, String> dnsRecords = new HashMap<String, String>();
    public WifiManager wifiManager;

    List devicesList = new ArrayList<WifiP2pDevice>();
    ArrayAdapter deviceListAdapter;

    ListView devicesListView;


    //this reacts to the result from request of permission
    //handle the case when permission isnt granted in if
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Coarse location permission is not granted!",
                            Toast.LENGTH_SHORT).show();
                    System.out.println("Coarse location permission is not granted!");
                    finish();
                }
                break;
        }
    }

    private String formatIP(int ip) {//formats hotspots's ip
        return String.format(
                "%d.%d.%d.%d",
                (ip & 0xff),
                (ip >> 8 & 0xff),
                (ip >> 16 & 0xff),
                (ip >> 24 & 0xff)
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        devicesListView = (ListView) findViewById(R.id.wifiDevicesList);//
        deviceListAdapter = new ArrayAdapter<String>(this, R.layout.activity_devices_list_item, devicesList);
        devicesListView.setAdapter(deviceListAdapter);


        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);


        intentFilter = new IntentFilter();// tells broadcast reciever to recieve these actions
//        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
//        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
//        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
//        intentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);


//        serviceFilter = new IntentFilter();// tells broadcast reciever to recieve these actions
//        serviceFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
//        serviceReciever = new ServiceBroadcastReciever();
//        registerReceiver(serviceReciever,serviceFilter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MainActivity.PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
            // After this point you wait for callback in
            // onRequestPermissionsResult(int, String[], int[]) overridden method
        }
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);//gps cordinates data hay apnay, needed by wifi direct

        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        System.out.println("location: ");
        System.out.println(location);

        DnsSdTxtRecordListener txtListener = new DnsSdTxtRecordListener() {//
            @Override
            /* Callback includes:
             * fullDomain: full domain name: e.g "printer._ipp._tcp.local."
             * record: TXT record dta as a map of key/value pairs.
             * device: The device running the advertised service.
             */

            public void onDnsSdTxtRecordAvailable(//service discovery, this function recieves the hotspot's password and ip
                                                  //
                                                  String fullDomain, Map record, WifiP2pDevice device) {
//                Log.d(TAG, "DnsSdTxtRecord available -" + record.toString());
//                Toast.makeText(MainActivity.this, "DnsSdTxtRecord available -" + record.toString(),
//                        Toast.LENGTH_LONG).show();
                dnsRecords.put(device.deviceAddress, "wifi groups: " + ((String) record.get("wifi_ssid")));
                WifiConfiguration wifiConfig = new WifiConfiguration();
                wifiConfig.SSID = String.format("\"%s\"", record.get("wifi_ssid"));
                wifiConfig.preSharedKey = String.format("\"%s\"", record.get("wifi_password"));

                wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
//remember id
                int netId = wifiManager.addNetwork(wifiConfig);//adds wifi name to client's wifi list
                wifiManager.disconnect();
                wifiManager.enableNetwork(netId, true);
                wifiManager.reconnect();
                while (formatIP(wifiManager.getConnectionInfo().getIpAddress()).equals("0.0.0.0")) {//jb client host say connect hojai tou handle this k ak broadcast reciever daikhay k kia wo wifi connect hogaya hay aur kia wo usi say hogaya hay jis say hum chahtay thay
                    //gets wifi's pass and ip and connecting with them in here
                }
                //Toast.makeText(MainActivity.this, "server ip -" + formatIP(wifiManager.getDhcpInfo().gateway),
                //      Toast.LENGTH_LONG).show();
                //Toast.makeText(MainActivity.this, "server ip -" + formatIP(wifiManager.getDhcpInfo().ipAddress),
                //      Toast.LENGTH_LONG).show();
//                Toast.makeText(MainActivity.this, "server ip -" + formatIP(wifiManager.getDhcpInfo().gateway),
//                        Toast.LENGTH_LONG).show();
                URI serverUri = null;
                try {
                    serverUri = new URI("ws://"+formatIP(wifiManager.getDhcpInfo().gateway)+":38301");
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }

                WebsocketClient.initialize(serverUri,getApplicationContext());
//                WebsocketClient.setContext(getApplicationContext());
//                try{
//                    WebsocketClient.getInstance().connect();
//                }
//                catch (Exception exception){
//                    Toast.makeText(getApplicationContext(),exception.toString(),Toast.LENGTH_LONG).show();
//                }


//                Intent intent = new Intent(MainActivity.this,ChatActivity.class);
//                intent.putExtra("server_ip",formatIP(wifiManager.getDhcpInfo().gateway));
//                MainActivity.this.startActivity(intent);


                System.out.println("dns records recieved");

            }
        };


        DnsSdServiceResponseListener servListener = new DnsSdServiceResponseListener() {
            @Override
//gives details of the device that generated the ip and password, this way a client can directly to that hotspot devices(not used rn)
            public void onDnsSdServiceAvailable(String instanceName, String registrationType,
                                                final WifiP2pDevice resourceType) {

                System.out.println("dns service available");
                resourceType.deviceName = dnsRecords
                        .containsKey(resourceType.deviceAddress) ? dnsRecords
                        .get(resourceType.deviceAddress) : resourceType.deviceName;
                // Add to the custom adapter defined specifically for showing
                // wifi devices.


                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        deviceListAdapter.add(resourceType);
                        deviceListAdapter.notifyDataSetChanged();
                    }
                });


            }
        };

        manager.setDnsSdResponseListeners(channel, servListener, txtListener);

        WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        //checks if wifi p2p is even supported or not on a device
        manager.addServiceRequest(channel,
                serviceRequest,
                new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        // Success!
                        System.out.println("service request added successfully");
                    }

                    @Override
                    public void onFailure(int code) {
                        // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                    }
                });


        manager.discoverServices(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // Success!
                System.out.println("service discovery started successfully");
            }

            @Override
            public void onFailure(int code) {
                // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                System.out.println("service discovery failed");

            }
        });

        devicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {//not used
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                System.out.println(formatIP(wifiManager.getConnectionInfo().getIpAddress()));

//                Toast.makeText(MainActivity.this, "server ip -" + formatIP(wifiManager.getDhcpInfo().gateway),
//                        Toast.LENGTH_LONG).show();
//                System.out.println(adapterView.getAdapter().getItem(i));
//
//                WifiP2pDevice device = (WifiP2pDevice) adapterView.getAdapter().getItem(i);
//                WifiP2pConfig config = new WifiP2pConfig();
//                config.deviceAddress = device.deviceAddress;
//                config.wps.setup = WpsInfo.PBC;
//
//                manager.connect(channel, config, new WifiP2pManager.ActionListener() {
//
//                    @Override
//                    public void onSuccess() {
//                        // WiFiDirectBroadcastReceiver notifies us. Ignore for now.
//                        Toast.makeText(MainActivity.this, "Connection Successful.",
//                                Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onFailure(int reason) {
//                        Toast.makeText(MainActivity.this, "Connect failed. Retry.",
//                                Toast.LENGTH_SHORT).show();
//                    }
//                });

            }
        });
    }

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {//not used rn
        @Override
        public void onPeersAvailable(final WifiP2pDeviceList peers) {//discovers the devices areounf us, not wifi
            deviceListAdapter.clear();
            System.out.println("peers: ");
            System.out.println(peers.getDeviceList());
//            for (WifiP2pDevice peer:peers.getDeviceList()){
//                deviceListAdapter.add(peer.deviceName);
//            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    deviceListAdapter.addAll(peers.getDeviceList());
                    deviceListAdapter.notifyDataSetChanged();
                }
            });

        }
    };

    @Override
    protected void onResume() {//saved states
        super.onResume();
        registerReceiver(receiver, intentFilter);
//        registerReceiver(serviceReciever,serviceFilter);
    }

    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
//        unregisterReceiver(serviceReciever);
    }


    public void btnDiscoverPeersOnClick(View view) {
//        manager.requestDiscoveryState(channel,discoveryStateListener);
        deviceListAdapter.clear();
//        System.out.println("button pressed");
//        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
//            @Override
//            public void onSuccess() {
//                Toast.makeText(MainActivity.this, "peers discovery started successfully",
//                        Toast.LENGTH_SHORT).show();
//                System.out.println("peers discovery started successfully");
//            }
//
//            @Override
//            public void onFailure(int reasonCode) {
//                Toast.makeText(MainActivity.this, "peers discovery failed",
//                        Toast.LENGTH_SHORT).show();
//                System.out.println("peers discovery failed");
//            }
//        });
        manager.discoverServices(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // Success!
                System.out.println("service discovery started successfully");
            }

            @Override
            public void onFailure(int code) {
                // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                System.out.println("service discovery failed");

            }
        });
    }

    public void btnHandler(View view) {
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int i) {

            }
        });
        manager.createGroup(channel, new WifiP2pManager.ActionListener() {//this event's listner is in broadcast reciever

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver notifies us. Ignore for now.
//                Toast.makeText(MainActivity.this, "Group Creation Successful.",
//                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity.this, "Group Creation failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

}
