package com.example.gabi.foodlab.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gabi.foodlab.R;
import com.example.gabi.foodlab.model.User;
import com.example.gabi.foodlab.services.ApiService;
import com.squareup.okhttp.ResponseBody;

import javax.inject.Inject;

import roboguice.activity.RoboActionBarActivity;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@ContentView(R.layout.activity_login)
public class LoginActivity extends RoboActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    @InjectView(R.id.login_button)
    private Button loginButton;

    @InjectView(R.id.username)
    private EditText username;

    @InjectView(R.id.password)
    private EditText password;

    @Inject
    private ApiService apiService;

    @Inject
    private SharedPreferences sharedPreferences;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        username.setSingleLine(true);
        password.setSingleLine(true);
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        if (sharedPreferences.getBoolean("LOGGED_IN", false) == true) {
            startMainActivity();
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkConnected()) {
                    if (username.getText().toString().isEmpty() && password.getText().toString().isEmpty()) {
                        AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this)
                                .setTitle("Error")
                                .setMessage(getString(R.string.login_error_message))
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .create();
                        alertDialog.show();
                    } else {
                        User user = new User();
                        user.setUsername(username.getText().toString());
                        user.setPassword(password.getText().toString());
                        loginToServer(user);
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "You are offline.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void loginToServer(User user) {
        startProgressDialog();
        System.out.println("Aici e login");
        apiService.login(user)
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
                        Log.e(TAG, "failed to login", e);

                        if (e.getMessage().trim().equals("HTTP 400".trim())) {
                            Toast.makeText(LoginActivity.this, "Bad credentials", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onNext(ResponseBody response) {
                        Log.d(TAG, "login - success");
                        stopProgressDialog();
                        sharedPreferences.edit().putBoolean("LOGGED_IN", true).apply();
                        startMainActivity();
                    }
                });
    }

    private void startMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        LoginActivity.this.startActivity(mainIntent);
        this.finish();
    }

    protected void startProgressDialog() {
        Log.d(getClass().getSimpleName(), "start progress dialog");
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
    }

    protected void stopProgressDialog() {
        if (progressDialog != null) {
            Log.d(getClass().getSimpleName(), "stop progress dialog");
            progressDialog.dismiss();
        }
    }

    protected boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
}