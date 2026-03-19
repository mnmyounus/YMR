package com.mnmyounus.ymr.ui.features
import android.os.Bundle; import android.view.*
import androidx.fragment.app.Fragment; import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mnmyounus.ymr.adapter.MessageAdapter
import com.mnmyounus.ymr.databinding.FragmentChatBinding
import com.mnmyounus.ymr.viewmodel.MainViewModel

class ChatFragment : Fragment() {
    private var _b: FragmentChatBinding? = null; private val b get() = _b!!
    private val vm: MainViewModel by activityViewModels()
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentChatBinding.inflate(i,c,false); return b.root
    }
    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v,s)
        val pkg    = arguments?.getString("packageName") ?: return
        val sender = arguments?.getString("senderName")  ?: return
        activity?.title = sender
        val adapter = MessageAdapter()
        b.recyclerChat.layoutManager = LinearLayoutManager(requireContext()).also{it.stackFromEnd=true}
        b.recyclerChat.adapter = adapter
        vm.getChat(pkg, sender).observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            if (list.isNotEmpty()) b.recyclerChat.scrollToPosition(list.size - 1)
        }
    }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
