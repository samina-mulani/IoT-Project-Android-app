//package com.example.iot_project;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//
//
//public class BluetoothTScheduler extends BroadcastReceiver {
//
//    MbtScanner sbtscanner;
//
//
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//
//        sbtscanner = new MbtScanner(context.getApplicationContext());
//        sbtscanner.mbtScannerInit();
//        if (sbtscanner.mBluetoothAdapter.isDiscovering()) sbtscanner.mBluetoothAdapter.cancelDiscovery();
//        sbtscanner.scanResult();
//
//    }
//
//}