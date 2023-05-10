package love.nuoyan.android.core.example

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import love.nuoyan.android.floating_kit.FloatingMagnetView
import love.nuoyan.android.utils.LifecycleCallback
import love.nuoyan.android.utils.Utils
import love.nuoyan.android.utils.UtilsLifecycle
import love.nuoyan.android.utils.UtilsLog

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        FloatingMagnetView.init(this)
        Utils.init(this, CoroutineScope(SupervisorJob() + Dispatchers.Default), true, "", "test")
        UtilsLifecycle.registerActivityLifecycle(this, object : LifecycleCallback() {
            override fun onBack() {
                super.onBack()
                UtilsLog.finish()
            }
        })
    }
}