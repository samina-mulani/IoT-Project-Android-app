package com.example.iot_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class FillUserInfo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_user_info);
    }

    public void storeUserInfo(View view) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        EditText nameEditText = (EditText) findViewById(R.id.userName);
        if(TextUtils.isEmpty(nameEditText.getText())){
            CharSequence text = "Name cannot be blank!";
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }
        String userName = nameEditText.getText().toString();

        EditText phoneEditText = (EditText) findViewById(R.id.userPhone);
        if(TextUtils.isEmpty(phoneEditText.getText())){
            CharSequence text = "Phone number cannot be blank!";
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }
        String userPhone = phoneEditText.getText().toString();

        EditText emailEditText = (EditText) findViewById(R.id.userEmail);
        if(TextUtils.isEmpty(emailEditText.getText())){
            CharSequence text = "Email cannot be blank!";
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }
        String userEmail = emailEditText.getText().toString();
        SharedPreferences sharedPref = getSharedPreferences("metadata",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("isSet", true);
        editor.putString("UserName", userName);
        editor.putString("UserPhone", userPhone);
        editor.putString("UserEmail", userEmail);
        editor.commit();
        logUserDetails();
        finish();
    }

    public void logUserDetails(){
        SharedPreferences sharedPreferences = getSharedPreferences("metadata",Context.MODE_PRIVATE);;
        Boolean isSet = sharedPreferences.getBoolean("isSet", false);
        String name = sharedPreferences.getString("UserName", "NA");
        String phone = sharedPreferences.getString("UserPhone", "NA");
        String email = sharedPreferences.getString("UserEmail", "NA");
        Log.d("isSet1",Boolean.toString(isSet));
        Log.d("NAME1",name);
        Log.d("PHONE1",phone);
        Log.d("EMAIL1",email);
    }
}
