package com.example.weatherapp

import android.app.Application
import com.example.weatherapp.di.ApplicationComponent
import com.example.weatherapp.di.DaggerApplicationComponent

class WeatherApp: Application() {

    lateinit var applicationComponent: ApplicationComponent

    override fun onCreate() {
        super.onCreate()
        applicationComponent = DaggerApplicationComponent.factory().create(context = this)
    }
}