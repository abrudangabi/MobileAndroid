package com.example.gabi.foodlab.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.gabi.foodlab.R;
import com.example.gabi.foodlab.entity.RestaurantEntity;
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

public class EditRestaurantActivity  extends RoboActivity {

    private static final String TAG = AddRestaurantActivity.class.getSimpleName();
    private static final String RESTAURANT = "restaurant";

    private EditText restaurantName;
    private EditText restaurantLocation;
    private Spinner stars;
    private Button addRestaurantButton;
    private RestaurantManager restaurantManager;

    private ProgressDialog progressDialog;

    @Inject
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_restaurant);
        Log.d(TAG, "onCreate");
        loadViews();
        String restaurantId = "";
        if (getIntent().getExtras() != null)
            restaurantId = getIntent().getExtras().getString(RESTAURANT, "");
        restaurantManager = new RestaurantManager();

        if(!restaurantId.isEmpty()){
            RestaurantEntity restaurantEntity = restaurantManager.getRestaurantById(restaurantId);
            if(restaurantEntity != null) {
                restaurantName.setText(restaurantEntity.getName());
                restaurantLocation.setText(restaurantEntity.getLocation());
                stars.setPrompt(restaurantEntity.getName());
            }
        }

        final String finalRestaurantId = restaurantId;
        addRestaurantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNetworkConnected()) {
                    if (!restaurantName.getText().toString().isEmpty() &&
                            !restaurantLocation.getText().toString().isEmpty() &&
                            !stars.getSelectedItem().toString().isEmpty()) {
                        Restaurant restaurant = new Restaurant(finalRestaurantId, restaurantName.getText().toString(),
                                restaurantLocation.getText().toString(),
                                stars.getSelectedItemPosition() + 1);

                        modifyRestaurant(restaurant);
                    } else {
                        Toast.makeText(EditRestaurantActivity.this,
                                getString(R.string.fill_fields_message),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EditRestaurantActivity.this, "You are offline", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void modifyRestaurant(Restaurant restaurant) {
        startProgressDialog();

        Log.d(TAG, "modify object to server");
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
                        Log.e(TAG, "failed to modify object to server", e);
                        Toast.makeText(EditRestaurantActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onNext(ResponseBody response) {
                        Log.d(TAG, "object modified");
                        stopProgressDialog();

                        AlertDialog alertDialog = new AlertDialog.Builder(EditRestaurantActivity.this)
                                .setTitle("Info")
                                .setMessage(getString(R.string.modified_success))
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        EditRestaurantActivity.this.finish();
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
        addRestaurantButton = (Button) findViewById(R.id.edit_restaurant_button);
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