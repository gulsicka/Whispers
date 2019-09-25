package org.macmads.whispers;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;

import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Build;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.macmads.whispers.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 1001;

    WifiP2pManager manager;
    Channel channel;
    BroadcastReceiver receiver;
    IntentFilter intentFilter;
    final HashMap<String, String> dnsRecords = new HashMap<String, String>();


    RecyclerView recyclerView;
    RvAdapter DL_Adapter;
    List<RowModel> list;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.wifiDevicesList);
        list=new ArrayList<>();
        DL_Adapter = new RvAdapter(list);
        RecyclerView.LayoutManager mLayoutManager = new
                LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(DL_Adapter);


        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MainActivity.PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
            // After this point you wait for callback in
            // onRequestPermissionsResult(int, String[], int[]) overridden method
        }
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        System.out.println("location: ");
        System.out.println(location);

        DnsSdTxtRecordListener txtListener = new DnsSdTxtRecordListener() {
            @Override
            /* Callback includes:
             * fullDomain: full domain name: e.g "printer._ipp._tcp.local."
             * record: TXT record dta as a map of key/value pairs.
             * device: The device running the advertised service.
             */

            public void onDnsSdTxtRecordAvailable(
                    String fullDomain, Map record, WifiP2pDevice device) {
//                Log.d(TAG, "DnsSdTxtRecord available -" + record.toString());
                Toast.makeText(MainActivity.this, "DnsSdTxtRecord available -" + record.toString(),
                        Toast.LENGTH_LONG).show();
                dnsRecords.put(device.deviceAddress, "wifi groups: "+((String) record.get("wifi_ssid")));
                System.out.println("dns records recieved");

            }
        };


        DnsSdServiceResponseListener servListener = new DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType,
                                                WifiP2pDevice resourceType) {

                System.out.println("dns service available");
                resourceType.deviceName = dnsRecords
                        .containsKey(resourceType.deviceAddress) ? dnsRecords
                        .get(resourceType.deviceAddress) : resourceType.deviceName;
                // Add to the custom adapter defined specifically for showing
                // wifi devices.
//                WiFiDirectServicesList fragment = (WiFiDirectServicesList) getFragmentManager()
//                        .findFragmentById(R.id.frag_peerlist);
//                WiFiDevicesAdapter adapter = ((WiFiDevicesAdapter) fragment
//                        .getListAdapter());
//
//                adapter.add(resourceType);
//                adapter.notifyDataSetChanged();
//                deviceListAdapter.clear();
                RowModel row = new RowModel(resourceType.deviceName);
                if(list.contains(row) != true) {
                    list.add(row);
                    recyclerView.getAdapter().notifyItemInserted(list.size());
                    recyclerView.smoothScrollToPosition(list.size());
                }


            }
        };

        manager.setDnsSdResponseListeners(channel, servListener, txtListener);

        WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
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

            }
        });


       /* devicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                System.out.println(adapterView.getAdapter().getItem(i));

                WifiP2pDevice device = (WifiP2pDevice) adapterView.getAdapter().getItem(i);
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;

                manager.connect(channel, config, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        // WiFiDirectBroadcastReceiver notifies us. Ignore for now.
                        Toast.makeText(MainActivity.this, "Connection Successful.",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(MainActivity.this, "Connect failed. Retry.",
                                Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });*/
    }

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peers) {
            //deviceListAdapter.clear();
            System.out.println("peers: ");
            System.out.println(peers.getDeviceList());
           for (WifiP2pDevice peer:peers.getDeviceList()){
               RowModel row = new RowModel(peer.deviceName);
               if(list.contains(row) != true) {
                   list.add(row);
                   recyclerView.getAdapter().notifyItemInserted(list.size());
                   recyclerView.smoothScrollToPosition(list.size());
               }
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }

    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }


    public void btnDiscoverPeersOnClick(View view) {
//        manager.requestDiscoveryState(channel,discoveryStateListener);
        System.out.println("button pressed");
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "peers discovery started successfully",
                        Toast.LENGTH_SHORT).show();
                System.out.println("peers discovery started successfully");
            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(MainActivity.this, "peers discovery failed",
                        Toast.LENGTH_SHORT).show();
                System.out.println("peers discovery failed");
            }
        });
    }

    public void btnHandler(View view) {
        manager.createGroup(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver notifies us. Ignore for now.
                Toast.makeText(MainActivity.this, "Group Creation Successful.",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity.this, "Group Creation failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

}
