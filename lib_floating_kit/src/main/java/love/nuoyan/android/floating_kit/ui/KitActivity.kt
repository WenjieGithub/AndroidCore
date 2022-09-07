package love.nuoyan.android.floating_kit.ui

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import love.nuoyan.android.floating_kit.FloatingMagnetView
import love.nuoyan.android.floating_kit.extension.Extension
import love.nuoyan.android.floating_kit.R
import love.nuoyan.android.floating_kit.extension.SingleAdapter
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KitActivity : AppCompatActivity() {
    companion object {
        val itemInfoList = mutableListOf<ItemInfo>()
        fun addItem(name: String, src: Int, click: (activity: AppCompatActivity, view: View) -> Unit) {
            itemInfoList.add(ItemInfo(name, src, click))
        }
        init {
            addItem("环境切换", R.drawable.lib_floating_kit_env_icon){ activity, view ->  showEnvDialog(activity, view) }
            addItem("缓存清理", R.drawable.lib_floating_kit_delete_icon){ activity, view -> showCacheDialog(activity, view) }
            addItem("日志查看", R.drawable.lib_floating_kit_log_icon){ activity, view -> showLogPage(activity, view) }
            addItem("开发者选项", R.drawable.lib_floating_kit_dev_icon){ activity, view -> showDevPage(activity, view) }
            addItem("App 信息", R.drawable.lib_floating_kit_info_icon){ activity, view -> showAppInfo(activity, view) }
        }

        private fun showEnvDialog(activity: AppCompatActivity, view: View) {
            FloatingMagnetView.envConfig?.let {
                val array = it.array
                val num = array.indexOf(it.currentEnv)
                AlertDialog.Builder(activity)
                    .setTitle(R.string.lib_floating_kit_util_env)
                    .setSingleChoiceItems(array, num) { dialog, which ->
                        it.onCallback(array[which])
                        FloatingMagnetView.setFloatingViewInfo()
                        dialog.dismiss()
                    }
                    .create().show()
            }
            if (FloatingMagnetView.envConfig == null) {
                Toast.makeText(Extension.application, "初始化时请配置环境所需参数 initEnvironment  addEnvironmentCallback", Toast.LENGTH_LONG).show()
            }
        }

        private fun showCacheDialog(activity: AppCompatActivity, view: View) {
            AlertDialog.Builder(activity)
                .setTitle(R.string.lib_floating_kit_util_cache)
                .setMessage("清理缓存会清空用户空间中所有数据, 包括所设置的环境. ")
                .setPositiveButton("确定") { dialog, _ ->
                    GlobalScope.launch(Default) {
                        Extension.getAppCacheDir(Extension.application)?.let {
                            Extension.deleteDirectory(it)
                        }
                        Extension.getAppDir(Extension.application, "")?.let {
                            Extension.deleteDirectory(it)
                        }
                        withContext(Main) {
                            throw RuntimeException("清空缓存, 初始化环境, 退出应用")
                        }
                    }
                    dialog.dismiss()
                }
                .create().show()
        }

        private fun showLogPage(activity: AppCompatActivity, view: View) {
            val fragment = KitLogFragment()
            activity.supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, fragment)
                .addToBackStack(fragment.javaClass.simpleName)
                .commit()
        }

        private fun showDevPage(activity: AppCompatActivity, view: View) {
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                Extension.application.startActivity(intent)
            } catch (e: Exception) {
                try {
                    val componentName = ComponentName("com.android.settings", "com.android.settings.DevelopmentSettings")
                    val intent = Intent()
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.component = componentName
                    intent.action = "android.intent.action.View"
                    Extension.application.startActivity(intent)
                } catch (e1: Exception) {
                    try {
                        // 部分小米手机采用这种方式跳转
                        val intent = Intent("com.android.settings.APPLICATION_DEVELOPMENT_SETTINGS")
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        Extension.application.startActivity(intent)
                    } catch (e2: Exception) {
                        e2.printStackTrace()
                    }
                }
            }
        }

        private fun showAppInfo(activity: AppCompatActivity, view: View) {
            val fragment = KitAppInfoFragment()
            activity.supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, fragment)
                .addToBackStack(fragment.javaClass.simpleName)
                .commit()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT

        setContentView(R.layout.lib_floating_kit_activity_layout)
        findViewById<ImageView>(R.id.kit_back).setOnClickListener{ finish() }

        findViewById<RecyclerView>(R.id.list).apply {
            layoutManager = GridLayoutManager(context, 4)
            adapter = object : SingleAdapter<ItemInfo>(R.layout.lib_floating_kit_activity_layout_item, itemInfoList) {
                override fun onCreateViewHolder(holder: SingleViewHolder) {
                    holder.itemView.setOnClickListener {
                        getData(holder.adapterPosition).click(this@KitActivity, it)
                    }
                }
                override fun onBindViewHolder(holder: SingleViewHolder, t: ItemInfo, position: Int) {
                    (holder.itemView as? TextView)?.let {
                        it.text = t.name
                        it.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(this@KitActivity, t.src), null, null)
                    }
                }
            }
        }
    }
}

data class ItemInfo(
    val name: String,
    val src: Int,
    val click: (activity: AppCompatActivity, view: View) -> Unit
)