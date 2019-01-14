package com.example.gabi.foodlab.services;

import com.example.gabi.foodlab.model.Restaurant;
import com.example.gabi.foodlab.model.User;
import com.squareup.okhttp.ResponseBody;

import java.util.List;

import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import rx.Observable;

public interface ApiService {

    @POST("/api/login")
    Observable<ResponseBody> login(@Body User user);

    @GET("/api/restaurants")
    Observable<List<Restaurant>> getRestaurants();

    @POST("/api/restaurant")
    Observable<ResponseBody> postRestaurant(@Body Restaurant restaurant);

    @DELETE("/api/restaurant/{restaurant_id}")
    Observable<ResponseBody> delete(@Path("restaurant_id") String restaurantId);
}