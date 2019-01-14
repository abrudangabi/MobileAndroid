package com.example.gabi.foodlab.database;

import com.raizlabs.android.dbflow.annotation.Database;


@Database(name = FoodPlacesDatabase.NAME, version = FoodPlacesDatabase.VERSION)
public class FoodPlacesDatabase {
    public static final String NAME = "foodplaces";
    public static final int VERSION = 1;
}
