package com.example.gabi.foodlab.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.gabi.foodlab.R;
import com.example.gabi.foodlab.adapters.RestaurantAdapter;
import com.example.gabi.foodlab.manager.RestaurantManager;
import com.example.gabi.foodlab.model.Restaurant;
import com.example.gabi.foodlab.services.ApiService;

import java.util.List;

import javax.inject.Inject;

import roboguice.activity.RoboActionBarActivity;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

@ContentView(R.layout.activity_main)
public class MainActivity extends RoboActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String RESTAURANT = "restaurant";

    @InjectView(R.id.animate_image)
    private ImageView imageAnimate;

    private Animation animSlideUp;
    private Animation animSlideDown;


    @Inject
    private RestaurantManager restaurantManager;

    @Inject
    private ApiService apiService;

    private RestaurantAdapter restaurantAdapter;
    private ListView listView;

    @Inject
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");

        listView = (ListView) findViewById(R.id.restaurant_list);

        setAnimations();
        imageAnimate.startAnimation(animSlideUp);

        imageAnimate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageAnimate.startAnimation(animSlideDown);
            }
        });


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        /*this.setActionBar(toolbar);
        this.setSupportActionBar(toolbar);*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, EditRestaurantActivity.class);
                intent.putExtra(RESTAURANT, restaurantAdapter.getItem(position).getId());
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_add_restaurant) {
            Intent intent = new Intent(this, AddRestaurantActivity.class);
            startActivity(intent);
        }

        if (id == R.id.nav_logout) {
            Intent mainIntent = new Intent(MainActivity.this, LoginActivity.class);
            MainActivity.this.startActivity(mainIntent);
            sharedPreferences.edit().putBoolean("LOGGED_IN", false).apply();
            this.finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        apiService.getRestaurants()
                .doOnNext(new Action1<List<Restaurant>>() {
                    @Override
                    public void call(List<Restaurant> restaurants) {
                        restaurantManager.saveRestaurants(restaurants);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(new Subscriber<List<Restaurant>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        List<Restaurant> restaurants = restaurantManager.getRestaurantsFromDB();
                        restaurantAdapter = new RestaurantAdapter(MainActivity.this, restaurants, restaurantManager, apiService);
                        listView.setAdapter(restaurantAdapter);

                        Log.e(TAG, "failed to get restaurants from server", e);
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onNext(List<Restaurant> restaurants) {
                        Log.d(TAG, "get restaurants from server - success");

                        restaurantAdapter = new RestaurantAdapter(MainActivity.this, restaurants, restaurantManager, apiService);
                        listView.setAdapter(restaurantAdapter);
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    private void setAnimations() {
        animSlideUp = AnimationUtils.loadAnimation(this, R.anim.anim_slide_up);
        animSlideDown = AnimationUtils.loadAnimation(this, R.anim.anim_slide_down);
        animSlideDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                imageAnimate.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
}
