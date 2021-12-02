package com.example.iot_project;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BuzzDevice extends AppCompatActivity {

    ListView listView;
    final List<String> ListElementsArrayList = new ArrayList<String>();
    AlertDialog.Builder builder;
    public static final int REQUEST_ENABLE_BT = 0;
    List<DeviceDetails> allRegistered;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if(intent == null) return;
            String action = intent.getAction();
            Log.d("MY_STATUS","In receiver");
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                if(deviceName == null) deviceName = "N/A";
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.d("DEVICE FOUND!!",deviceHardwareAddress);
                displayRegistered(deviceHardwareAddress);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                Log.d("MY_STATUS","Scan finished");
                TextView textview = (TextView) findViewById(R.id.textView2);
                textview.setText("Pick a device!");
                Toast.makeText(getApplicationContext(),"Bluetooth scan finished!",Toast.LENGTH_SHORT).show();
            }
        }
    };

    public void displayRegistered(String macAddr)
    {
        DeviceDetails device;
        for (int i = 0; i < allRegistered.size(); i++) {
            device = allRegistered.get(i);
            Log.d("REGISTERED: ",device.deviceName);
            if(device.deviceAddress.equals(macAddr)) {
                ListElementsArrayList.add(device.deviceName + "\n" + device.deviceAddress);
                listView.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, ListElementsArrayList));
            }
        }
    }

    public void connect(String macAddr)
    {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothDevice device = bluetoothManager.getAdapter().getRemoteDevice(macAddr);
        if(!(device == null))
        Log.d(" CONNECTING TO", device.getName() + "  " + device.getAddress());
        else Log.d("CONNECTING TO","IS NULL");

        BluetoothSocket tmp = null;
        BluetoothSocket mmSocket = null;

        // Get a BluetoothSocket for a connection with the
        // given BluetoothDevice
        try {
            ParcelUuid[] uuids = device.getUuids();
            if(uuids == null) tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            else tmp = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
            Method m = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
            tmp = (BluetoothSocket) m.invoke(device, 1);
        } catch (Exception e) {
            Log.e("BT CONNECT", "Create() failed!", e);
            Toast.makeText(getApplicationContext(),"Socket creation failed!",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mmSocket = tmp;
        if(mmSocket==null){ finish();return;}
        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

        try {
            mmSocket.connect();
            Log.d("BT CONNECT", "...Connection established and data link opened...");
        } catch (IOException e) {
            try {
                Toast.makeText(getApplicationContext(),"Could not connect!",Toast.LENGTH_SHORT).show();
                mmSocket.close();
            } catch (IOException e2) {
                Log.d("BT CONNECT", "Unable to close socket during connection failure " + e2.getMessage() + ".");
                Toast.makeText(getApplicationContext(),"Unable to close socket during connection failure!",Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        OutputStream tmpOut = null;
        try {
            tmpOut = mmSocket.getOutputStream();
            Log.d("BT CONNECT", "Opened output stream!");
        } catch (IOException e) {
            Log.e("BT CONNECT", "Error occurred when creating output stream", e);
            Toast.makeText(getApplicationContext(),"Output stream could not be created!",Toast.LENGTH_SHORT).show();
            try {
                mmSocket.close();
            } catch (IOException e2) {
                Log.e("BT CONNECT", "Unable to close socket during connection failure" + e2.getMessage() + ".");
                Toast.makeText(getApplicationContext(),"Could not close socket!",Toast.LENGTH_SHORT).show();
            }
            finish();
            return;
        }
        try {
            tmpOut.write("1".getBytes());
            Log.d("BT CONNECT", "Written data!");

        } catch (IOException e) {
            Log.e("BT CONNECT", "Error occurred when sending data", e);
            Toast.makeText(getApplicationContext(),"Could not write data into connection!",Toast.LENGTH_SHORT).show();
        }


        //End of communication - can close socket
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.d("BT CONNECT", "Unable to close socket during connection failure" + e.getMessage() + ".");
            Toast.makeText(getApplicationContext(),"Could not close socket!",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_buzz_device);
        TextView textview = (TextView) findViewById(R.id.textView2);
        textview.setText("Pick a registered device");
        AppDatabase appDb = AppDatabase.getInstance(getApplicationContext());
        allRegistered = appDb.deviceDao().getAll();
        Log.d("REG_SIZE: ", Integer.toString(allRegistered.size()));
        listView = findViewById(R.id.listView);
        builder = new AlertDialog.Builder(this);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);

        if(allRegistered.size()==0)
        {
            ListElementsArrayList.add("No registered devices");
            listView.setAdapter(new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,
                    ListElementsArrayList));
        }
        else {

            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (bluetoothAdapter == null)
            {
                Toast.makeText(getApplicationContext(),"Bluetooth Not Supported",Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            else{
                if (!bluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    // TODO - receive result and if negative, request again
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    Toast.makeText(getApplicationContext(),"Bluetooth Turned ON",Toast.LENGTH_SHORT).show();
                }
                if(bluetoothAdapter.isDiscovering()) bluetoothAdapter.cancelDiscovery();
                textview.setVisibility(View.VISIBLE);
                Log.d("MY_STATUS","Starting discovery");
                Toast.makeText(getApplicationContext(),"Please wait for scan to finish!",Toast.LENGTH_LONG);

                bluetoothAdapter.startDiscovery();
            }

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    //BUZZ
                    String deviceNameandAddress = (String) listView.getAdapter().getItem(position);
                    final String[] deviceDetails = deviceNameandAddress.split(System.lineSeparator());
                    final String deviceAddress = deviceDetails[1];

                    builder.setMessage("Do you want to buzz this device?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
//                                    Toast.makeText(getApplicationContext(),"Kindly wait till you get a 'DONE' message!",Toast.LENGTH_SHORT).show();
                                    connect(deviceAddress);
                                    Toast.makeText(getApplicationContext(),"Done!",Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    Toast.makeText(getApplicationContext(),"You chose to not buzz this device",Toast.LENGTH_SHORT).show();
                                }
                            });

                    AlertDialog alert = builder.create();
                    alert.setTitle("Confirm");
                    alert.show();

                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
