package com.mnmyounus.ymr.ui.features
import android.os.Bundle; import android.view.*
import androidx.core.os.bundleOf; import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels; import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mnmyounus.ymr.R
import com.mnmyounus.ymr.adapter.ConversationAdapter
import com.mnmyounus.ymr.databinding.FragmentMessagesBinding
import com.mnmyounus.ymr.viewmodel.MainViewModel

class MessagesFragment : Fragment() {
    private var _b: FragmentMessagesBinding? = null; private val b get() = _b!!
    private val vm: MainViewModel by activityViewModels()
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentMessagesBinding.inflate(i,c,false); return b.root
    }
    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v,s)
        val adapter = ConversationAdapter { conv ->
            findNavController().navigate(R.id.to_chat, bundleOf(
                "packageName" to conv.packageName, "senderName" to conv.senderName, "appName" to conv.appName))
        }
        b.recyclerMessages.layoutManager = LinearLayoutManager(requireContext())
        b.recyclerMessages.adapter = adapter
        vm.conversations.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            b.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
