package com.example.iot_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

public class MainActivity extends AppCompatActivity {

//    public static final String USER_INFO = "com.example.iot_project.USER_INFO";
    private static final int LCN_PERMISSION_CODE = 100;
    public static final int REQUEST_ENABLE_BT = 0;
    final List<String> ListElementsArrayList = new ArrayList<String>();
    private FusedLocationProviderClient fusedLocationClient;
    private final CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
//    AlarmManager alarmManager;
//    Intent intent;
//    public PendingIntent pendingIntent;
//
//    MbtScanner mbtscanner;
//    BluetoothTScheduler bluetoothTScheduler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPreferences = getSharedPreferences("metadata", Context.MODE_PRIVATE);
        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, LCN_PERMISSION_CODE);
        Boolean isSet = sharedPreferences.getBoolean("isSet", false);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if(!isSet)
        {
            setUserDetails();
            logUserDetails();
        }

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null)
        {
            Toast.makeText(getApplicationContext(),"Bluetooth Not Supported",Toast.LENGTH_SHORT).show();
           finish();
           return;
        }
        else {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                // TODO - receive result and if negative, request again
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                Toast.makeText(getApplicationContext(), "Bluetooth Turned ON", Toast.LENGTH_SHORT).show();
            }
        }

        //Fetch Global DB and store in Shared Preferences
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url ="https://iot-project-314.herokuapp.com/getListRegistered";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray myList = (JSONArray) response.get("list");
                            SharedPreferences sharedPref = getSharedPreferences("globalList",Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putInt("length",myList.length());
                            for(int i=0;i<myList.length();i++)
                            {
                                Log.d("WEB_REQ",myList.getString(i));
                                editor.putString("device"+Integer.toString(i),myList.getString(i));
                            }
                            editor.commit();
                            logGlobalDevices();
                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(),"Server Error!",Toast.LENGTH_SHORT).show();
                        }
                        Toast.makeText(getApplicationContext(),"Initialization complete!",Toast.LENGTH_SHORT).show();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {Log.e("WEB", error.toString());
                Toast.makeText(getApplicationContext(),"Could not connect to server!",Toast.LENGTH_SHORT).show();
            }
        });

        // Add the request to the RequestQueue.
        //queue.add(jsonObjectRequest);

        //REPEATED SCAN SET
//        mbtscanner = new MbtScanner(this);

//        setScheduler();
//        mbtscanner.mbtScannerInit();
//        mbtscanner.scanResult();
//        setBtReceiver();
    }

    public void logGlobalDevices(){
        SharedPreferences sharedPref = getSharedPreferences("globalList",Context.MODE_PRIVATE);
        int len = sharedPref.getInt("length",0);
        for(int i=0;i<len;i++)
        {
            Log.d("device"+Integer.toString(i),sharedPref.getString("device"+Integer.toString(i),"N/A"));
        }
    }

    public void logUserDetails(){
        SharedPreferences sharedPreferences = getSharedPreferences("metadata",Context.MODE_PRIVATE);
        Boolean isSet = sharedPreferences.getBoolean("isSet", false);
        String name = sharedPreferences.getString("UserName", "NA");
        String phone = sharedPreferences.getString("UserPhone", "NA");
        String email = sharedPreferences.getString("UserEmail", "NA");
        Log.d("isSet",Boolean.toString(isSet));
        Log.d("NAME",name);
        Log.d("PHONE",phone);
        Log.d("EMAIL",email);
    }

    public Boolean isLocallyRegistered(String macAddress)
    {
        DeviceDetails device;
        AppDatabase appDb = AppDatabase.getInstance(getApplicationContext());
        List<DeviceDetails> allRegistered = appDb.deviceDao().getAll();
        for (int i = 0; i < allRegistered.size(); i++){
            device = allRegistered.get(i);
            if(device.deviceAddress.equals(macAddress))
                return true;
        }
        return false;
    }

    public Boolean isGloballyRegistered(String macAddress){

        SharedPreferences sharedPref = getSharedPreferences("globalList",Context.MODE_PRIVATE);
        int len = sharedPref.getInt("length",0);
        String deviceAddress;
        for(int i=0;i<len;i++)
        {
            deviceAddress = sharedPref.getString("device"+Integer.toString(i),"N/A");
            Log.d("GLOBAL_DEVICES",deviceAddress);
            if(deviceAddress.equals(macAddress))
                return true;
        }
        return false;
    }

    public void update(final String macAddress) {

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

//                        //UPDATE LOCAL DB
//                        AppDatabase appDb = AppDatabase.getInstance(getApplicationContext());
//                        appDb.deviceDao().updateLocation(macAddress,Double.toString(location.getLatitude()),Double.toString(location.getLongitude()),ts);

                        //Send to server
                        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                        String url ="https://iot-project-314.herokuapp.com/sendLocationUpdate"; //DUMMY URL FOR NOW

                        //get user details from shared pref
                        SharedPreferences sharedPreferences = getSharedPreferences("metadata",Context.MODE_PRIVATE);
                        String name = sharedPreferences.getString("UserName", "NA");
                        String phone = sharedPreferences.getString("UserPhone", "NA");
                        String email = sharedPreferences.getString("UserEmail", "NA");

                        //Body of POST request
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("deviceAddress",macAddress);
                        params.put("deviceName","N/A");
                        params.put("timestamp",ts);
                        params.put("updaterName",name);
                        params.put("updaterNumber",phone);
                        params.put("updaterEmail",email);
                        params.put("latitude",Double.toString(location.getLatitude()));
                        params.put("longitude",Double.toString(location.getLongitude()));
                        JSONObject jo = new JSONObject(params);

                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url,jo,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        Toast.makeText(getApplicationContext(),"Successfully updated!",Toast.LENGTH_SHORT).show();

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

    public void checkPermission(String permission, int requestCode)
    {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { permission }, requestCode);
        }
        else {
            Toast.makeText(MainActivity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LCN_PERMISSION_CODE) {

            // Checking whether user granted the permission or not.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // Showing the toast message
                Toast.makeText(MainActivity.this, "Location Permission Granted", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(MainActivity.this, "Location Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
//        else if (requestCode == STORAGE_PERMISSION_CODE) {
//            if (grantResults.length > 0
//                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(RegisterDevice.this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
//            }
//            else {
//                Toast.makeText(RegisterDevice.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
//            }
//        }
    }

    public void setUserDetails(){
        Intent intent = new Intent(this, FillUserInfo.class);
        startActivityForResult(intent,0);
    }

    public void registerDevice(View view) {
        Intent intent = new Intent(this, RegisterDevice.class);
        startActivity(intent);
    }

    public void buzzDevice(View view) {
        Intent intent = new Intent(this, BuzzDevice.class);
        startActivity(intent);
    }

    public void getLocation(View view) {
        Intent intent = new Intent(this, GetLocation.class);
        startActivity(intent);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if(intent == null) return;
            String action = intent.getAction();
            Log.d("MY_STATUS","In receiver");
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                final String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.d("DEVICE FOUND!!",deviceHardwareAddress);
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String url ="https://iot-project-314.herokuapp.com/getListRegistered";
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url,null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    JSONArray myList = (JSONArray) response.get("list");
                                    SharedPreferences sharedPref = getSharedPreferences("globalList",Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putInt("length",myList.length());
                                    for(int i=0;i<myList.length();i++)
                                    {
                                        Log.d("WEB_REQ",myList.getString(i));
                                        editor.putString("device"+Integer.toString(i),myList.getString(i));
                                    }
                                    editor.commit();
                                    logGlobalDevices();
                                    if(isGloballyRegistered(deviceHardwareAddress)||isLocallyRegistered(deviceHardwareAddress))
                                    {
                                        update(deviceHardwareAddress);
                                    }
                                } catch (JSONException e) {
                                    Toast.makeText(getApplicationContext(),"Server Error!",Toast.LENGTH_SHORT).show();
                                }

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {Log.e("WEB", error.toString());
                        if(isGloballyRegistered(deviceHardwareAddress)||isLocallyRegistered(deviceHardwareAddress))
                        {
                            update(deviceHardwareAddress);
                        }
                        Toast.makeText(getApplicationContext(),"Could not connect to server!",Toast.LENGTH_SHORT).show();
                    }
                });

                // Add the request to the RequestQueue.
                queue.add(jsonObjectRequest);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                Log.d("MY_STATUS","Scan finished");
                Toast.makeText(getApplicationContext(),"Bluetooth scan finished!",Toast.LENGTH_SHORT).show();
            }
        }
    };

    public void scanAndUpdate(View view) {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null)
        {
            Toast.makeText(getApplicationContext(),"Bluetooth Not Supported",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        else {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                // TODO - receive result and if negative, request again
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                Toast.makeText(getApplicationContext(), "Bluetooth Turned ON", Toast.LENGTH_SHORT).show();
            }
            if (bluetoothAdapter.isDiscovering()) bluetoothAdapter.cancelDiscovery();
            Log.d("MY_STATUS", "Starting discovery");
            Toast.makeText(getApplicationContext(), "Please wait for scan to finish!", Toast.LENGTH_SHORT).show();

            bluetoothAdapter.startDiscovery();
        }
    }
}
