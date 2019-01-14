package com.example.gabi.foodlab;

import android.app.Application;
import android.content.Context;

import com.raizlabs.android.dbflow.config.FlowManager;

import roboguice.RoboGuice;

public class FoodPlacesApplication extends Application {

    @Override
    public void onCreate(){
        super.onCreate();

        FlowManager.init(this);

        RoboGuice.setUseAnnotationDatabases(false);
        RoboGuice.injectMembers(this, this);
    }

    @Override
    public void onTerminate(){
        super.onTerminate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}
