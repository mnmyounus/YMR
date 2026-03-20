package com.mnmyounus.ymr.ui.home
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.mnmyounus.ymr.R
import com.mnmyounus.ymr.databinding.FragmentHomeBinding
import com.mnmyounus.ymr.service.MediaWatcherService
import com.mnmyounus.ymr.util.PrefsUtil
import com.mnmyounus.ymr.viewmodel.MainViewModel

class HomeFragment : Fragment() {
    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!
    private val vm: MainViewModel by activityViewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentHomeBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)
        val ctx = requireContext()

        vm.totalCount.observe(viewLifecycleOwner) { b.tvMsgCount.text = it.toString() }

        b.switchSvc.isChecked = PrefsUtil.isSvcEnabled(ctx)
        b.tvSvcStatus.text = if (PrefsUtil.isSvcEnabled(ctx)) getString(R.string.svc_on) else getString(R.string.svc_off)
        b.switchSvc.setOnCheckedChangeListener { _, on ->
            PrefsUtil.setSvcEnabled(ctx, on)
            b.tvSvcStatus.text = if (on) getString(R.string.svc_on) else getString(R.string.svc_off)
            updateDot(on)
        }
        updateDot(PrefsUtil.isSvcEnabled(ctx))

        // Start background watcher
        runCatching {
            val wi = Intent(ctx, MediaWatcherService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ctx.startForegroundService(wi)
            else ctx.startService(wi)
        }

        // All cards navigate to Features tab
        val goFeatures = { findNavController().navigate(R.id.featuresFragment) }
        b.cardMessages.setOnClickListener { goFeatures() }
        b.cardCalls.setOnClickListener    { goFeatures() }
        b.cardStatus.setOnClickListener   { goFeatures() }
        b.cardRecorder.setOnClickListener { goFeatures() }
    }

    private fun updateDot(on: Boolean) {
        val color = if (on) requireContext().getColor(R.color.green_ok)
                    else requireContext().getColor(R.color.red_warn)
        b.dotStatus.setBackgroundColor(color)
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
