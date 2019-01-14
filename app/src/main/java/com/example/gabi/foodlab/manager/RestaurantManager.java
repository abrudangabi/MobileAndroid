package com.example.gabi.foodlab.manager;

import com.example.gabi.foodlab.entity.RestaurantEntity;
import com.example.gabi.foodlab.entity.RestaurantEntity$Table;
import com.example.gabi.foodlab.model.Restaurant;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Update;

import java.util.ArrayList;
import java.util.List;

public class RestaurantManager {

    public List<Restaurant> getRestaurantsFromDB() {
        List<RestaurantEntity> restaurantEntities = new Select().from(RestaurantEntity.class).queryList();
        List<Restaurant> restaurants = new ArrayList<>();

        for(RestaurantEntity r : restaurantEntities){
            Restaurant restaurant = new Restaurant(r.getUniqueIdentifier(), r.getName(), r.getLocation(), r.getStars());
            restaurants.add(restaurant);
        }
        return restaurants;
    }

    public void saveRestaurant(Restaurant restaurant) {
        RestaurantEntity entity = restaurant.getEntity();
        entity.save();
    }

    public void saveRestaurants(List<Restaurant> restaurantList) {
        Delete.table(RestaurantEntity.class);
        for (Restaurant restaurant : restaurantList) {
            saveRestaurant(restaurant);
        }
    }

    public void deleteAllRestaurantsFromDB(){
        Delete.table(RestaurantEntity.class);
    }

    public void deleteRestaurantFromDB(String id){
        new Delete().from(RestaurantEntity.class).where(Condition.column(RestaurantEntity$Table.UNIQUEIDENTIFIER).is(id)).query();
    }

    public RestaurantEntity getRestaurantById(String id){
        return new Select().from(RestaurantEntity.class).where(Condition.column(RestaurantEntity$Table.UNIQUEIDENTIFIER).is(id)).querySingle();
    }

    public void updateRestaurant(RestaurantEntity entity){
        new Update<>(RestaurantEntity.class).set(
                Condition.column(RestaurantEntity$Table.NAME).is(entity.getName()),
                Condition.column(RestaurantEntity$Table.LOCATION).is(entity.getLocation()),
                Condition.column(RestaurantEntity$Table.STARS).is(entity.getStars()))
                .where(Condition.column(RestaurantEntity$Table.UNIQUEIDENTIFIER).is(entity.getUniqueIdentifier()))
                .query();
    }
}
