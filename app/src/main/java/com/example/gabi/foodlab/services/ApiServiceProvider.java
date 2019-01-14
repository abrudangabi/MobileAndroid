package com.example.gabi.foodlab.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import java.io.IOException;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;


public class ApiServiceProvider implements Provider<ApiService> {

    protected static HttpLoggingInterceptor logging;
    private static OkHttpClient httpClient;
    private static Retrofit.Builder builder;

    // TODO PUNE URL TOT TIMPUL
    private static final String BASE_URL = "http://172.20.10.2:8080/";
    //private static final String BASE_URL = "http://192.168.1.2:8080/";
    //private static final String BASE_URL = "http://192.168.0.110:8080/";
    //private static final String BASE_URL = "http://192.168.0.104:8080/";

    @Inject
    public ApiServiceProvider(OkHttpClient httpClient) {

        logging = new HttpLoggingInterceptor();
        this.httpClient = httpClient;

        builder = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create());
    }

    @Override
    public ApiService get() {

        Log.d(getClass().getSimpleName(), " get call");
        httpClient.interceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();

                Request.Builder requestbuilder = original.newBuilder()
                        .header("Accept", "application/json");

                Request request = requestbuilder.build();
                return chain.proceed(request);
            }
        });

        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient.interceptors().add(logging);

        Retrofit retrofit = builder.client(httpClient).build();
        return retrofit.create(ApiService.class);
    }
}
