package com.example.iot_project;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GetLocation extends AppCompatActivity {

    ListView listView;
    TextView textview;
    final List<String> ListElementsArrayList = new ArrayList<String>();
    AlertDialog.Builder builder;
    WebView browser;

    public void displayLocation(final String deviceName, final String macAddress)
    {
        final String[] location = {null,null};
        final String[] humanReadableAddress = {null};
        final String[] otherDetails = {null,null,null,null}; //timestamp, updater name, updater email, updater number
        //Send to server
        final RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url1 ="https://iot-project-314.herokuapp.com/getLocation?deviceAddress="+macAddress;

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url1,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            location[0] = (String) response.get("latitude");
                            location[1] = (String) response.get("longitude");
                            //TODO - Display below details
                            otherDetails[0] = (String) response.get("timestamp");
                            otherDetails[1] = (String) response.get("updaterName");
                            otherDetails[2] = (String) response.get("updaterEmail");
                            otherDetails[3] = (String) response.get("updaterNumber");
                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(),"Invalid response from server!",Toast.LENGTH_SHORT).show();
                        }

                        String url2 = "https://nominatim.openstreetmap.org/reverse?lat="+location[0]+"&lon="+location[1]+"&format=jsonv2";
                        Log.e("URL2",url2);
                        final JsonObjectRequest jsonObjectRequest2 = new JsonObjectRequest(Request.Method.GET, url2,null,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try {
                                            humanReadableAddress[0] = response.getString("display_name");
                                        } catch (JSONException e) {
                                            Toast.makeText(getApplicationContext(),"Unexpected error!",Toast.LENGTH_SHORT).show();
                                            finish();
                                            return;
                                            //e.printStackTrace();
                                        }
                                        String date = new java.util.Date(Long.parseLong(otherDetails[0]+"000")).toString();
                                        textview.setText(deviceName + " last located at:\n"+humanReadableAddress[0]+"\nLatitude : "+location[0]+"\nLongitude : "+location[1]+"\nUpdater Name : "+otherDetails[1]+"\nUpdater Email : "+otherDetails[2]+"\nUpdater Number : "+otherDetails[3]+"\nTime : "+date);
                                        browser.setWebViewClient(new WebViewClient());
                                        browser.loadUrl("https://maps.google.com/maps?q="+location[0]+",%20"+location[1]);
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("WEB", error.toString());
                                Toast.makeText(getApplicationContext(),"Unexpected error!",Toast.LENGTH_SHORT).show();
                                finish();
                                return;
                            }
                        });
                        queue.add(jsonObjectRequest2);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("WEB", error.toString());
                //Use value from local DB
                AppDatabase appDb = AppDatabase.getInstance(getApplicationContext());
                DeviceDetails deviceDetails = appDb.deviceDao().fetchDevice(macAddress);
                if(deviceDetails==null)
                {
                    Toast.makeText(getApplicationContext(),"Unexpected error!",Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                location[0] = deviceDetails.latitude;
                location[1] = deviceDetails.longitude;

                //get user details from shared pref
                SharedPreferences sharedPreferences = getSharedPreferences("metadata",Context.MODE_PRIVATE);
                String name = sharedPreferences.getString("UserName", "NA");
                String phone = sharedPreferences.getString("UserPhone", "NA");
                String email = sharedPreferences.getString("UserEmail", "NA");

                //TODO-Display below details
                otherDetails[0] = deviceDetails.timestamp;
                otherDetails[1] = name;
                otherDetails[2] = email;
                otherDetails[3] = phone;

                String url2 = "https://nominatim.openstreetmap.org/reverse?lat="+location[0]+"&lon="+location[1]+"&format=jsonv2";
                Log.d("URL2",url2);
                final JsonObjectRequest jsonObjectRequest2 = new JsonObjectRequest(Request.Method.GET, url2,null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    humanReadableAddress[0] = response.getString("display_name");
                                } catch (JSONException e) {
                                    Toast.makeText(getApplicationContext(),"Unexpected error!",Toast.LENGTH_SHORT).show();
                                    finish();
                                    return;
                                    //e.printStackTrace();
                                }
                                String date = new java.util.Date(Long.parseLong(otherDetails[0]+"000")).toString();
                                textview.setText(deviceName + " last located at:\n"+humanReadableAddress[0]+"\nLatitude : "+location[0]+"\nLongitude : "+location[1]+"\nUpdater Name : "+otherDetails[1]+"\nUpdater Email : "+otherDetails[2]+"\nUpdater Number : "+otherDetails[3]+"\nTime : "+date);
                                browser.setWebViewClient(new WebViewClient());
                                browser.loadUrl("https://maps.google.com/maps?q="+location[0]+",%20"+location[1]);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("WEB", error.toString());
                        Toast.makeText(getApplicationContext(),"Unexpected error!",Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                });
                queue.add(jsonObjectRequest2);
            }
        });

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_get_location);

        AppDatabase appDb = AppDatabase.getInstance(getApplicationContext());
        List<DeviceDetails> allRegistered = appDb.deviceDao().getAll();
        listView = findViewById(R.id.listView);
        textview = findViewById(R.id.textView4);
        textview.setMovementMethod(new ScrollingMovementMethod());
        textview.setText("Pick a registered device");
        browser = (WebView) findViewById(R.id.webView);
        browser.setVerticalScrollBarEnabled(true);
        browser.setHorizontalScrollBarEnabled(true);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.loadUrl("https://www.google.com/maps");

        DeviceDetails device;
        builder = new AlertDialog.Builder(this);

        if(allRegistered.size()==0)
        {
            ListElementsArrayList.add("No registered devices");
            listView.setAdapter(new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,
                    ListElementsArrayList));
        }
        else {
            for (int i = 0; i < allRegistered.size(); i++) {
                device = allRegistered.get(i);
                ListElementsArrayList.add(device.deviceName + "\n" + device.deviceAddress);
                listView.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,
                        ListElementsArrayList));
            }

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    //GET LOCATION FROM LOCAL DB + CLOUD + DISPLAY - lat and long as gmap?
                    String deviceNameandAddress = (String) listView.getAdapter().getItem(position);
                    final String[] deviceDetails = deviceNameandAddress.split(System.lineSeparator());
                    final String deviceName = deviceDetails[0];
                    final String deviceAddress = deviceDetails[1];

                    builder.setMessage("View last recorded location of this device?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    textview.setText("Please wait ...");
//                                    Toast.makeText(getApplicationContext(),"Kindly wait till you get a 'DONE' message!",Toast.LENGTH_SHORT).show();
                                    displayLocation(deviceName,deviceAddress);
                                    Toast.makeText(getApplicationContext(),"Done!",Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    Toast.makeText(getApplicationContext(),"You did not pick this device",Toast.LENGTH_SHORT).show();
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
    protected void onResume() {
        super.onResume();
        browser.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        browser.onPause();
    }
}
