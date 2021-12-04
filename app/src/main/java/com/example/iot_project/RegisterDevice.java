package com.example.iot_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.Response;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

public class RegisterDevice extends AppCompatActivity {

    public static final int REQUEST_ENABLE_BT = 0;

    List<BluetoothDevice> scannedList = new ArrayList<BluetoothDevice>();
    ListView listView;
    final List<String> ListElementsArrayList = new ArrayList<String>();
    AlertDialog.Builder builder;
    private FusedLocationProviderClient fusedLocationClient;
    private final CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if(intent == null) return;
            String action = intent.getAction();
            Log.d("MY_STATUS","In receiver");
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                scannedList.add(device);
                String deviceName = device.getName();
                if(deviceName == null) deviceName = "N/A";
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.d("DEVICE FOUND!!",deviceHardwareAddress);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                Log.d("MY_STATUS","Scan finished");
                TextView textview = findViewById(R.id.textView3);
                textview.setText("Pick a device!");
                Toast.makeText(getApplicationContext(),"Bluetooth scan finished!",Toast.LENGTH_SHORT).show();
                for(int i=0;i<scannedList.size();i++)
                {
                    //Log.d("BT_DEVICE",scannedList.get(i).getName());
                    ListElementsArrayList.add(scannedList.get(i).getName()+ "\n" + scannedList.get(i).getAddress());
                    listView.setAdapter(new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,
                            ListElementsArrayList));
                }
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_register_device);
        listView = findViewById(R.id.listView);
        TextView textview = findViewById(R.id.textView3);
        textview.setVisibility(View.INVISIBLE);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        builder = new AlertDialog.Builder(this);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null)
        {
            Toast.makeText(getApplicationContext(),"Bluetooth Not Supported",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        else{
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(receiver, filter);

            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                // TODO - receive result and if negative, request again
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                Toast.makeText(getApplicationContext(),"Bluetooth Turned ON",Toast.LENGTH_SHORT).show();
            }
            if(bluetoothAdapter.isDiscovering()) bluetoothAdapter.cancelDiscovery();
            Log.d("MY_STATUS","Starting discovery");
            Toast.makeText(getApplicationContext(),"Please wait for scan to finish!",Toast.LENGTH_SHORT).show();
            textview.setVisibility(View.VISIBLE);
            bluetoothAdapter.startDiscovery();
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                String deviceNameandAddress = (String) listView.getAdapter().getItem(position);
                final String[] deviceDetails = deviceNameandAddress.split(System.lineSeparator());

                builder.setMessage("Do you want to register this device?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                Toast.makeText(getApplicationContext(),"Kindly wait till you get a message saying device has been registered!",Toast.LENGTH_SHORT).show();
                                storeDeviceDetails(deviceDetails);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                Toast.makeText(getApplicationContext(),"You chose not to register",Toast.LENGTH_SHORT).show();
                            }
                        });

                AlertDialog alert = builder.create();
                alert.setTitle("Confirm");
                alert.show();
            }
        });
    }

    public void storeDeviceDetails(String[] deviceDetails) {
        final String deviceName = deviceDetails[0];
        final String deviceAddress = deviceDetails[1];
        Log.d("POST REQ",deviceName);

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {

            // Main code
            Task<Location> currentLocationTask = fusedLocationClient.getCurrentLocation(
                    PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.getToken()
            );

            currentLocationTask.addOnCompleteListener((new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {

                    String result = "";

                    if (task.isSuccessful()) {
                        // Task completed successfully
                        Location location = task.getResult();
                        result = "Location (success): " +
                                location.getLatitude() +
                                ", " +
                                location.getLongitude();
                        Long tsLong = System.currentTimeMillis()/1000;
                        String ts = tsLong.toString();
                        //SAVE TO DB
                        AppDatabase appDb = AppDatabase.getInstance(getApplicationContext());
                        appDb.deviceDao().insertAll(new DeviceDetails(deviceAddress,deviceName,ts,Double.toString(location.getLongitude()),Double.toString(location.getLatitude())));

                        //Send to server
                        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                        String url ="https://iot-project-314.herokuapp.com/registerDevice";

                        //get user details from shared pref
                        SharedPreferences sharedPreferences = getSharedPreferences("metadata",Context.MODE_PRIVATE);
                        String name = sharedPreferences.getString("UserName", "NA");
                        String phone = sharedPreferences.getString("UserPhone", "NA");
                        String email = sharedPreferences.getString("UserEmail", "NA");

                        //Body of POST request
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("deviceAddress",deviceAddress);
                        params.put("deviceName",deviceName);
                        params.put("ownerName",name);
                        params.put("ownerNumber",phone);
                        params.put("ownerEmail",email);
                        params.put("timestamp",ts);
                        params.put("latitude",Double.toString(location.getLatitude()));
                        params.put("longitude",Double.toString(location.getLongitude()));
                        JSONObject jo = new JSONObject(params);

                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url,jo,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        Toast.makeText(getApplicationContext(),"Successfully registered!",Toast.LENGTH_SHORT).show();

                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {Log.e("WEB", error.toString());
                                Toast.makeText(getApplicationContext(),"Could not send update to server!",Toast.LENGTH_SHORT).show();
                            }
                        });

                        // Add the request to the RequestQueue.
                        queue.add(jsonObjectRequest);

                    } else {
                        // Task failed with an exception
                        Exception exception = task.getException();
                        result = "Exception thrown: " + exception;
                        Toast.makeText(getApplicationContext(),"Could not get location!",Toast.LENGTH_SHORT).show();
                    }

                    Log.d("GPS", "getCurrentLocation() result: " + result);
                }
            }));
        } else {
            // TODO: Request fine location permission
            Log.d("GPS", "Request fine location permission.");
            Toast.makeText(getApplicationContext(),"Grant location permission!",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
