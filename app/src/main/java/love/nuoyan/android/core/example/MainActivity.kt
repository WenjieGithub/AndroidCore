package love.nuoyan.android.core.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import love.nuoyan.android.core.example.net.NetFragment
import love.nuoyan.android.core.example.qr.QrFragment
import love.nuoyan.android.utils.UtilsApp
import love.nuoyan.android.utils.UtilsSystemUI

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        UtilsSystemUI.setDecorFitsSystemWindows(window, false)
        findViewById<View>(R.id.qr_button).setOnClickListener {
            openFragment(QrFragment())
        }
        findViewById<View>(R.id.permission_button).setOnClickListener {
            openFragment(QrFragment())
//            UtilsSystemUI.hideSystemUI(this)
        }
        findViewById<View>(R.id.net_button).setOnClickListener {
            openFragment(NetFragment())
//            UtilsSystemUI.showSystemUI(this)
        }
    }

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .addToBackStack(fragment::class.java.simpleName)
            .replace(R.id.container, fragment)
            .commit()
    }
}