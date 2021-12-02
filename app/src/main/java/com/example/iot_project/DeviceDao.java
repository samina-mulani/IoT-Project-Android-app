package com.example.iot_project;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RoomDatabase;

@Dao
public interface DeviceDao {
    @Query("SELECT * FROM deviceDetails")
    List<DeviceDetails> getAll();

    @Query("SELECT * FROM deviceDetails WHERE deviceAddress = :deviceAddress")
    DeviceDetails fetchDevice(String deviceAddress);

    @Query("UPDATE deviceDetails SET latitude= :latitude , longitude = :longitude , timestamp = :ts WHERE deviceAddress = :deviceAddress")
    int updateLocation(String deviceAddress, String latitude, String longitude, String ts);

//    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
//            "last_name LIKE :last LIMIT 1")
//    DeviceDetails findByName(String first, String last);

//    @Query("SELECT DISTINCT deviceAddress,deviceName FROM deviceDetails")
//    List<UniqueDevice> getAllRegistered();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(DeviceDetails device);

    @Delete
    void delete(DeviceDetails device);
}

