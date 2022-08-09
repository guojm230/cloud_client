package com.example.base

import android.content.Context

object AppContext {
    private lateinit var appContext: Context
    private lateinit var activityContext: Context

    fun setAppContext(context: Context){
        this.appContext = context
    }

    fun getAppContext(): Context{
        return appContext
    }

    fun setActivityContext(context: Context){
        this.activityContext = context
    }

    fun getActivityContext(): Context{
        return activityContext
    }
}