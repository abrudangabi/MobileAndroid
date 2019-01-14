package com.example.gabi.foodlab.adapters;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gabi.foodlab.R;
import com.example.gabi.foodlab.manager.RestaurantManager;
import com.example.gabi.foodlab.model.Restaurant;
import com.example.gabi.foodlab.services.ApiService;
import com.example.gabi.foodlab.ui.MainActivity;
import com.squareup.okhttp.ResponseBody;

import java.text.ParseException;
import java.util.List;

import javax.inject.Inject;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class RestaurantAdapter extends BaseAdapter {

    private static final String TAG = RestaurantAdapter.class.getSimpleName();


    private List<Restaurant> restaurants;
    private Context context;
    private RestaurantManager restaurantManager;

    private ApiService apiService;

    public RestaurantAdapter(Context context, List<Restaurant> restaurants, RestaurantManager restaurantManager, ApiService apiService){
        this.context = context;
        this.restaurants = restaurants;
        this.restaurantManager = restaurantManager;
        this.apiService = apiService;
    }

    @Override
    public int getCount() {
        return restaurants.size();
    }

    @Override
    public Restaurant getItem(int position) {
        return restaurants.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View birthdayLayout = LayoutInflater.from(context).inflate(R.layout.restaurant_list_cell, null);

        TextView name = (TextView) birthdayLayout.findViewById(R.id.restaurant_name);
        TextView location = (TextView) birthdayLayout.findViewById(R.id.restaurant_location);
        TextView stars = (TextView) birthdayLayout.findViewById(R.id.retaurant_stars);
        Button removeItem = (Button) birthdayLayout.findViewById(R.id.remove_item);

        final Restaurant restaurant = restaurants.get(position);
        name.setText(restaurant.getName());
        location.setText(restaurant.getLocation());
        stars.setText(String.valueOf(restaurant.getStars()));

        removeItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNetworkConnected()) {
                    remove(position, restaurant.getId());
                } else {
                    Toast.makeText(context, "You can't delete restaurant when you are offline",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        Log.d(TAG, "getView " + position);
        return birthdayLayout;
    }

    public void remove(int position, final String id){
        restaurants.remove(position);
        notifyDataSetChanged();

        apiService.delete(id)
                .doOnNext(new Action1<ResponseBody>() {
                    @Override
                    public void call(ResponseBody responseBody) {
                        restaurantManager.deleteRestaurantFromDB(id);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(new Subscriber<ResponseBody>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        notifyDataSetChanged();
                        Log.e(TAG, "failed to remove restaurant from server", e);
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onNext(ResponseBody restaurants) {
                        Log.d(TAG, "restaurant removed - success");
                        Toast.makeText(context, "Restaurant removed", Toast.LENGTH_LONG).show();
                        notifyDataSetChanged();
                    }
                });
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
}
