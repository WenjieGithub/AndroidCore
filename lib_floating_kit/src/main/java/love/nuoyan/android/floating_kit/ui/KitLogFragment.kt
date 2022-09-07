package love.nuoyan.android.floating_kit.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import love.nuoyan.android.floating_kit.FloatingMagnetView
import love.nuoyan.android.floating_kit.R
import love.nuoyan.android.floating_kit.extension.Extension
import love.nuoyan.android.floating_kit.extension.LogInfo
import love.nuoyan.android.floating_kit.extension.SingleAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KitLogFragment : Fragment(), View.OnClickListener {
    companion object {
        internal val mTagList = arrayListOf(Pair("全部", 0))

        @SuppressLint("InflateParams", "SetTextI18n")
        internal fun showDialog(context: Context, info: LogInfo) {
            val view = LayoutInflater.from(context).inflate(R.layout.lib_floating_kit_log_layout_pager_item_dialog, null)
            val v1 = view.findViewById<TextView>(R.id.kit_title)
            val v2 = view.findViewById<TextView>(R.id.kit_thread)
            val v3 = view.findViewById<TextView>(R.id.kit_method)
            val v4 = view.findViewById<TextView>(R.id.kit_msg)
            v1.text = "${info.tag}   ${info.time}"
            v2.text = info.thread
            v3.text = info.method
            v4.text = info.msg
            setTextColor(info.tag, v1)
            AlertDialog.Builder(context)
                .setView(view)
                .create().show()
        }

        internal fun setTextColor(tag: String, vararg views: TextView) {
            for ((t, color) in mTagList) {
                if (t == tag) {
                    for (v in views) {
                        v.setTextColor(color)
                    }
                    return
                }
            }
            ContextCompat.getColor(Extension.application, R.color.lib_floating_kit_log_other).let {
                for (v in views) {
                    v.setTextColor(it)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.lib_floating_kit_log_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.kit_back).setOnClickListener(this)
        view.findViewById<View>(R.id.kit_search).setOnClickListener(this)

        val tab = view.findViewById<TabLayout>(R.id.kit_tab)
        val pager = view.findViewById<ViewPager2>(R.id.kit_pager)

        pager.adapter = object : SingleAdapter<Pair<String, Int>>(R.layout.lib_floating_kit_log_layout_pager, mTagList) {
            override fun onCreateViewHolder(holder: SingleViewHolder) {
                val listView = holder.getView<RecyclerView>(R.id.kit_list)
                listView.layoutManager = LinearLayoutManager(requireContext())

                val listAdapter = object : SingleAdapter<LogInfo>(R.layout.lib_floating_kit_log_layout_pager_item, null) {
                    override fun onCreateViewHolder(holder: SingleViewHolder) {
                        holder.itemView.setOnClickListener {
                            showDialog(requireContext(), getData(holder.adapterPosition))
                        }
                    }

                    override fun onBindViewHolder(holder: SingleViewHolder, t: LogInfo, position: Int) {
                        val titleView = holder.getView<TextView>(R.id.kit_title)
                        val title = "${t.tag}   ${t.time}"
                        titleView.text = title
                        setTextColor(t.tag, titleView)
                        val contentView = holder.getView<TextView>(R.id.kit_content)
                        contentView.text = t.msg
                    }
                }
                listView.adapter = listAdapter

                FloatingMagnetView.logLiveData.observe(viewLifecycleOwner) {
                    upPageData(holder.adapterPosition, listAdapter)
                }
            }

            override fun onBindViewHolder(holder: SingleViewHolder, t: Pair<String, Int>, position: Int) {
                val listView = holder.getView<RecyclerView>(R.id.kit_list)
                (listView.adapter as? SingleAdapter<LogInfo>)?.let {
                    upPageData(position, it)
                }
            }
        }

        TabLayoutMediator(tab, pager) { tabLayout, position ->
            val tabText = TextView(requireContext())
            tabText.gravity = Gravity.CENTER
            tabText.textSize = 15f
            tabText.text = mTagList[position].first
            tabText.setTextColor(Color.BLACK)
            tabLayout.customView = tabText
        }.attach()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.kit_back -> requireActivity().supportFragmentManager.popBackStack()
            R.id.kit_search -> {
                val fragment = KitLogSearchFragment()
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .addToBackStack(fragment.javaClass.simpleName)
                    .commit()
            }
        }
    }

    private fun upPageData(position: Int, adapter: SingleAdapter<LogInfo>) {
        lifecycleScope.launch(Default) {
            if (position >= 0) {
                val list = arrayListOf<LogInfo>()
                if (position == 0) {
                    list.addAll(FloatingMagnetView.logList)
                } else {
                    for (i in FloatingMagnetView.logList) {
                        if (i.tag == mTagList[position].first) {
                            list.add(list.size, i)
                        }
                    }
                }
                withContext(Main) {
                    adapter.clearData()
                    adapter.addData(list)
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }
}