package com.mnmyounus.ymr.ui.features
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.mnmyounus.ymr.R
import com.mnmyounus.ymr.databinding.FragmentFeaturesBinding
import com.mnmyounus.ymr.util.PrefsUtil

class FeaturesFragment : Fragment() {
    private var _b: FragmentFeaturesBinding? = null
    private val b get() = _b!!

    // All tab names and fragment factories
    private val tabs = listOf(
        "Messages" to { MessagesFragment() },
        "Media"    to { MediaFragment() },
        "Status"   to { StatusFragment() },
        "Calls"    to { CallsFragment() },
        "Recorder" to { RecorderFragment() }
    )

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentFeaturesBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)

        b.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = tabs.size
            override fun createFragment(pos: Int) = tabs[pos].second()
        }

        TabLayoutMediator(b.tabLayout, b.viewPager) { tab, pos ->
            tab.text = tabs[pos].first
        }.attach()

        // App filter chips
        val chipMap = mapOf(
            b.chipAll  to "",
            b.chipWA   to "com.whatsapp",
            b.chipIG   to "com.instagram.android",
            b.chipTG   to "org.telegram.messenger",
            b.chipFB   to "com.facebook.orca",
            b.chipSnap to "com.snapchat.android"
        )
        b.chipGroupApps.setOnCheckedStateChangeListener { _, ids ->
            val pkg = chipMap.entries.firstOrNull { ids.contains(it.key.id) }?.value ?: ""
            PrefsUtil.setFilter(requireContext(), pkg)
        }
        // Set initial selection
        b.chipAll.isChecked = true
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
