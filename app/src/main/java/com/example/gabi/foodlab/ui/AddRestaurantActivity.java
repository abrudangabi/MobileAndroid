package com.example.gabi.foodlab.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.gabi.foodlab.R;
import com.example.gabi.foodlab.manager.RestaurantManager;
import com.example.gabi.foodlab.model.Restaurant;
import com.example.gabi.foodlab.services.ApiService;
import com.squareup.okhttp.ResponseBody;

import java.util.UUID;

import javax.inject.Inject;

import roboguice.activity.RoboActionBarActivity;
import roboguice.activity.RoboActivity;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AddRestaurantActivity extends RoboActivity {

    private static final String TAG = AddRestaurantActivity.class.getSimpleName();

    private EditText restaurantName;
    private EditText restaurantLocation;
    private Spinner stars;
    private Button addRestaurantButton;

    @Inject
    private ApiService apiService;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_restaurant);
        Log.d(TAG, "onCreate");
        loadViews();

        addRestaurantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkConnected()) {
                    if (!restaurantName.getText().toString().isEmpty() &&
                            !restaurantLocation.getText().toString().isEmpty() &&
                            !stars.getSelectedItem().toString().isEmpty()) {

                        Restaurant restaurant = new Restaurant();
                        restaurant.setName(restaurantName.getText().toString());
                        restaurant.setLocation(restaurantLocation.getText().toString());
                        restaurant.setStars(stars.getSelectedItemPosition() + 1);

                        addToServer(restaurant);
                    } else {
                        Toast.makeText(AddRestaurantActivity.this, getString(R.string.fill_fields_message), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AddRestaurantActivity.this, "You are offline.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void addToServer(Restaurant restaurant) {
        startProgressDialog();

        Log.d(TAG, "add object to server");
        apiService.postRestaurant(restaurant)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(new Subscriber<ResponseBody>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        stopProgressDialog();
                        Log.e(TAG, "failed to add object to server", e);
                        Toast.makeText(AddRestaurantActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onNext(ResponseBody response) {
                        Log.d(TAG, "add object to server - success");
                        stopProgressDialog();

                        AlertDialog alertDialog = new AlertDialog.Builder(AddRestaurantActivity.this)
                                .setTitle("Info")
                                .setMessage(getString(R.string.add_success))
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        AddRestaurantActivity.this.finish();
                                    }
                                })
                                .create();
                        alertDialog.show();
                    }
                });
    }

    private void loadViews() {
        restaurantName = (EditText) findViewById(R.id.restaurant_name);
        restaurantName.setSingleLine(true);
        restaurantLocation = (EditText) findViewById(R.id.restaurant_location);
        restaurantLocation.setSingleLine(true);
        stars = (Spinner) findViewById(R.id.restaurant_stars);
        addRestaurantButton = (Button) findViewById(R.id.add_restaurant_button);
    }

    protected void startProgressDialog(){
        Log.d(getClass().getSimpleName(), "start progress dialog");
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
    }

    protected void stopProgressDialog(){
        if(progressDialog != null) {
            Log.d(getClass().getSimpleName(), "stop progress dialog");
            progressDialog.dismiss();
        }
    }

    protected boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

}
