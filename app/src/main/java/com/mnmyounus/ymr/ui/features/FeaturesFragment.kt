package com.mnmyounus.ymr.ui.features
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.mnmyounus.ymr.databinding.FragmentFeaturesBinding
import com.mnmyounus.ymr.util.PrefsUtil

class FeaturesFragment : Fragment() {
    private var _b: FragmentFeaturesBinding? = null
    private val b get() = _b!!

    private val tabTitles = listOf("Messages", "Media", "Status", "Calls", "Recorder")

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentFeaturesBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)

        b.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = tabTitles.size
            override fun createFragment(pos: Int): Fragment = when (pos) {
                0 -> MessagesFragment()
                1 -> MediaFragment()
                2 -> StatusFragment()
                3 -> CallsFragment()
                4 -> RecorderFragment()
                else -> MessagesFragment()
            }
        }

        // CRITICAL: limit to 1 so only current tab loads — prevents all tabs loading at once
        b.viewPager.offscreenPageLimit = 1

        TabLayoutMediator(b.tabLayout, b.viewPager) { tab, pos ->
            tab.text = tabTitles[pos]
        }.attach()

        // App filter chips
        b.chipGroupApps.setOnCheckedStateChangeListener { _, ids ->
            val filter = when {
                ids.contains(b.chipWA.id)   -> "com.whatsapp"
                ids.contains(b.chipIG.id)   -> "com.instagram.android"
                ids.contains(b.chipTG.id)   -> "org.telegram.messenger"
                ids.contains(b.chipFB.id)   -> "com.facebook.orca"
                ids.contains(b.chipSnap.id) -> "com.snapchat.android"
                else -> ""
            }
            PrefsUtil.setFilter(requireContext(), filter)
        }
        b.chipAll.isChecked = true
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
