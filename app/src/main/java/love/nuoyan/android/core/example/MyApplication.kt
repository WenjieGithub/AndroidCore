package love.nuoyan.android.core.example

import android.app.Application
import love.nuoyan.android.floating_kit.FloatingMagnetView

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        FloatingMagnetView.init(this)
    }
}