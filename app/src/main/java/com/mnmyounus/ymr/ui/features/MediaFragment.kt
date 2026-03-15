package com.mnmyounus.ymr.ui.features
import android.os.Bundle; import android.view.*
import androidx.fragment.app.Fragment; import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.mnmyounus.ymr.databinding.FragmentFeaturesBinding

class MediaFragment : Fragment() {
    private var _b: FragmentFeaturesBinding? = null
    private val b get() = _b!!
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentFeaturesBinding.inflate(i, c, false); return b.root
    }
    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)
        // Hide chips (not needed in media context)
        b.chipGroupApps.visibility = View.GONE
        val tabs = listOf("Photos" to "photos", "Videos" to "videos", "Audio" to "audio", "Docs" to "docs")
        b.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = tabs.size
            override fun createFragment(pos: Int) = MediaListFragment.newInstance(tabs[pos].second)
        }
        TabLayoutMediator(b.tabLayout, b.viewPager) { tab, pos -> tab.text = tabs[pos].first }.attach()
    }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
