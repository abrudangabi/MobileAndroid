package com.example.gabi.foodlab.common;

import com.google.inject.AbstractModule;
import com.example.gabi.foodlab.services.ApiService;
import com.example.gabi.foodlab.services.ApiServiceProvider;

import roboguice.inject.SharedPreferencesName;

public class Module extends AbstractModule {

    @Override
    protected void configure() {
        bind(ApiService.class).toProvider(ApiServiceProvider.class);
        bindConstant().annotatedWith(SharedPreferencesName.class).to("preferences");

        requestStaticInjection(Module.class);
    }
}