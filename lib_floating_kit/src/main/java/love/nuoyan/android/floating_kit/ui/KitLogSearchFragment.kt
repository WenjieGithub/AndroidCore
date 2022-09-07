package love.nuoyan.android.floating_kit.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import love.nuoyan.android.floating_kit.FloatingMagnetView
import love.nuoyan.android.floating_kit.R
import love.nuoyan.android.floating_kit.extension.LogInfo
import love.nuoyan.android.floating_kit.extension.SingleAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KitLogSearchFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.lib_floating_kit_log_search_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.kit_back).setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        val list = view.findViewById<RecyclerView>(R.id.kit_list)
        list.layoutManager = LinearLayoutManager(requireContext())

        val listAdapter = object : SingleAdapter<LogInfo>(R.layout.lib_floating_kit_log_layout_pager_item, null) {
            override fun onCreateViewHolder(holder: SingleViewHolder) {
                holder.itemView.setOnClickListener {
                    KitLogFragment.showDialog(requireContext(), getData(holder.adapterPosition))
                }
            }

            override fun onBindViewHolder(holder: SingleViewHolder, t: LogInfo, position: Int) {
                val titleView = holder.getView<TextView>(R.id.kit_title)
                val title = "${t.tag}   ${t.time}"
                titleView.text = title
                KitLogFragment.setTextColor(t.tag, titleView)
                val contentView = holder.getView<TextView>(R.id.kit_content)
                contentView.text = t.msg
            }
        }
        list.adapter = listAdapter
        upPageData(null, listAdapter)

        val search = view.findViewById<EditText>(R.id.kit_search)
        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    upPageData(null, listAdapter)
                } else {
                    upPageData(s.toString(), listAdapter)
                }
            }
        })
    }

    private fun upPageData(key: String?, adapter: SingleAdapter<LogInfo>) {
        lifecycleScope.launch(Dispatchers.Default) {
            val list = arrayListOf<LogInfo>()
            if (key.isNullOrEmpty()) {
                list.addAll(FloatingMagnetView.logList)
            } else {
                for (i in FloatingMagnetView.logList) {
                    val s = "${i.tag}${i.time}${i.thread}${i.method}${i.msg}"
                    if (s.contains(key, true)) {
                        list.add(list.size, i)
                    }
                }
            }
            withContext(Dispatchers.Main) {
                adapter.clearData()
                adapter.addData(list)
                adapter.notifyDataSetChanged()
            }
        }
    }
}