package love.nuoyan.android.core.example

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import love.nuoyan.android.floating_kit.FloatingMagnetView
import love.nuoyan.android.utils.Utils

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        FloatingMagnetView.init(this)
        Utils.init(this, CoroutineScope(SupervisorJob() + Dispatchers.Default), true, "", "test")
    }
}