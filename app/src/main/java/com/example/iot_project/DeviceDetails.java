package com.example.iot_project;

import android.location.Address;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;

@Entity(tableName = "deviceDetails")
public class DeviceDetails {
    @PrimaryKey @NonNull
    public String deviceAddress;

    @ColumnInfo(name = "deviceName")
    public String deviceName;

    @ColumnInfo(name = "timestamp")
    public String timestamp;

    @ColumnInfo(name = "longitude")
    public String longitude;

    @ColumnInfo(name = "latitude")
    public String latitude;

    public DeviceDetails(String deviceAddress, String deviceName, String timestamp, String longitude, String latitude){
        this.deviceAddress = deviceAddress;
        this.deviceName = deviceName;
        this.timestamp = timestamp;
        this.longitude = longitude;
        this.latitude = latitude;
    }
}

