package com.mnmyounus.ymr.ui.features
import android.Manifest; import android.content.pm.PackageManager; import android.os.Bundle
import android.provider.CallLog; import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat; import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.mnmyounus.ymr.adapter.CallAdapter; import com.mnmyounus.ymr.adapter.CallRecord
import com.mnmyounus.ymr.databinding.FragmentCallsBinding

class CallsFragment : Fragment() {
    private var _b: FragmentCallsBinding? = null; private val b get() = _b!!
    private var filter = "weekly"
    private val permLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
        if(it.values.all{v->v}) loadCalls()
    }
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentCallsBinding.inflate(i,c,false); return b.root
    }
    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v,s)
        val adapter = CallAdapter()
        b.recyclerCalls.layoutManager = LinearLayoutManager(requireContext()); b.recyclerCalls.adapter = adapter
        b.chipGroup.setOnCheckedStateChangeListener{_,ids->
            filter = when{ ids.contains(b.chipMonthly.id)->"monthly"; ids.contains(b.chipYearly.id)->"yearly"; else->"weekly" }
            if(hasPerm()) showCalls(adapter)
        }
        b.btnGrantPerm.setOnClickListener{permLauncher.launch(arrayOf(Manifest.permission.READ_CALL_LOG,Manifest.permission.READ_CONTACTS))}
        if(hasPerm()) loadCalls() else { b.layoutEmpty.visibility=View.VISIBLE; b.recyclerCalls.visibility=View.GONE }
    }
    private fun hasPerm()=ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_CALL_LOG)==PackageManager.PERMISSION_GRANTED
    private fun loadCalls() { showCalls(b.recyclerCalls.adapter as CallAdapter) }
    private fun showCalls(adapter: CallAdapter) {
        val calls = fetchCalls(); adapter.submitList(calls)
        b.tvIncoming.text = calls.count{it.type==CallLog.Calls.INCOMING_TYPE}.toString()
        b.tvOutgoing.text = calls.count{it.type==CallLog.Calls.OUTGOING_TYPE}.toString()
        b.tvMissed.text   = calls.count{it.type==CallLog.Calls.MISSED_TYPE}.toString()
        b.recyclerCalls.visibility = View.VISIBLE
        b.layoutEmpty.visibility = if(calls.isEmpty()) View.VISIBLE else View.GONE
    }
    private fun fetchCalls(): List<CallRecord> {
        val now=System.currentTimeMillis()
        val from = when(filter){ "monthly"->now-30L*86400000; "yearly"->now-365L*86400000; else->now-7L*86400000 }
        val result = mutableListOf<CallRecord>()
        try{
            requireContext().contentResolver.query(CallLog.Calls.CONTENT_URI,
                arrayOf(CallLog.Calls.CACHED_NAME,CallLog.Calls.NUMBER,CallLog.Calls.TYPE,CallLog.Calls.DATE,CallLog.Calls.DURATION),
                "${CallLog.Calls.DATE}>=?",arrayOf(from.toString()),"${CallLog.Calls.DATE} DESC"
            )?.use{c->
                while(c.moveToNext()) result.add(CallRecord(c.getString(0)?:"",c.getString(1)?:"",c.getInt(2),c.getLong(3),c.getLong(4)))
            }
        }catch(_:Exception){}
        return result
    }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
