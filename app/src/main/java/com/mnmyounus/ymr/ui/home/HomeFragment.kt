package com.mnmyounus.ymr.ui.home
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.mnmyounus.ymr.databinding.FragmentHomeBinding
import com.mnmyounus.ymr.service.CallRecorderService
import com.mnmyounus.ymr.service.MediaWatcherService
import com.mnmyounus.ymr.util.PrefsUtil
import com.mnmyounus.ymr.viewmodel.MainViewModel

class HomeFragment : Fragment() {
    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!
    private val vm: MainViewModel by activityViewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentHomeBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)
        val ctx = requireContext()

        // Observe message count
        vm.totalCount.observe(viewLifecycleOwner) { b.tvMsgCount.text = it.toString() }

        // Service switch
        b.switchSvc.isChecked = PrefsUtil.isSvcEnabled(ctx)
        b.switchSvc.setOnCheckedChangeListener { _, checked ->
            PrefsUtil.setSvcEnabled(ctx, checked)
            b.tvSvcStatus.text = if (checked) getString(com.mnmyounus.ymr.R.string.svc_on)
                                 else getString(com.mnmyounus.ymr.R.string.svc_off)
            updateDot(checked)
        }
        updateDot(PrefsUtil.isSvcEnabled(ctx))

        // Start media watcher
        val wi = Intent(ctx, MediaWatcherService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ctx.startForegroundService(wi)
        else ctx.startService(wi)

        // Card navigation
        b.cardMessages.setOnClickListener {
            (activity as? androidx.navigation.NavController) ?: run {
                requireActivity().let {
                    androidx.navigation.Navigation.findNavController(it, com.mnmyounus.ymr.R.id.navHost)
                        .navigate(com.mnmyounus.ymr.R.id.featuresFragment)
                }
            }
        }
        b.cardRecorder.setOnClickListener {
            androidx.navigation.Navigation.findNavController(requireActivity(), com.mnmyounus.ymr.R.id.navHost)
                .navigate(com.mnmyounus.ymr.R.id.featuresFragment)
        }
        b.cardStatus.setOnClickListener {
            androidx.navigation.Navigation.findNavController(requireActivity(), com.mnmyounus.ymr.R.id.navHost)
                .navigate(com.mnmyounus.ymr.R.id.featuresFragment)
        }
        b.cardCalls.setOnClickListener {
            androidx.navigation.Navigation.findNavController(requireActivity(), com.mnmyounus.ymr.R.id.navHost)
                .navigate(com.mnmyounus.ymr.R.id.featuresFragment)
        }
    }

    private fun updateDot(on: Boolean) {
        b.dotStatus.setBackgroundColor(
            if (on) requireContext().getColor(com.mnmyounus.ymr.R.color.green_ok)
            else requireContext().getColor(com.mnmyounus.ymr.R.color.red_warn)
        )
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
