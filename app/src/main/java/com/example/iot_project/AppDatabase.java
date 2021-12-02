package com.example.iot_project;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {DeviceDetails.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DB_NAME = "deviceDetails_db";
    private static AppDatabase instance;
    public static synchronized AppDatabase getInstance(Context context){
        if(instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(),AppDatabase.class,DB_NAME).fallbackToDestructiveMigration().allowMainThreadQueries().build();
        }
        return instance;
    }
    public abstract DeviceDao deviceDao();
}
