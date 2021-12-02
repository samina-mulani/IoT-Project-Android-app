//package com.example.iot_project;
//
//import android.app.ProgressDialog;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.os.Environment;
//import android.widget.Toast;
//
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.PrintWriter;
//
//public class MbtScanner {
//    public String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/btTracker";
//    FileWriter fileWriter;
//    File file;
//
//
//    Context mContext;
//
//    BluetoothAdapter mBluetoothAdapter;
//    IntentFilter filter;
//    BroadcastReceiver mReceiver;
//    boolean stillScanning = false;
//
//    String s;
//    int count =0;
//
//    public MbtScanner(Context mContext) {
//        this.mContext = mContext;
//    }
//
//
//    public void mbtScan (){
//        mBluetoothAdapter.startDiscovery();
//        stillScanning = true;
//    }
//
//    public void mbtScannerInit(){
//        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//    }
//
//
//    public void scanResult() {
//        mReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                String action = intent.getAction();
//                // When discovery finds a device
//
//
//                // IF DEVICE FOUND
//                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                    // Get the BluetoothDevice object from the Intent
//
//
//                    // CAPTURE DEVICE DATA TO LISTVIEW
//                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//
//                    count++;
//
//                    String sName = device.getName();
//                    String sAddress = device.getAddress();
//
//                    //DO SMTH WITH DATA
//
//                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
//                    stillScanning = false;
//                }
//            }
//        };
//
//        mContext.registerReceiver(mReceiver, filter);
//        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
//        mContext.registerReceiver(mReceiver, filter);
//    }
//}
